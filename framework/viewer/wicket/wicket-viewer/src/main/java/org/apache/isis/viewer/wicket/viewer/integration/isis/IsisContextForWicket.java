/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.viewer.wicket.viewer.integration.isis;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;

import org.apache.log4j.Logger;
import org.apache.wicket.Session;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.runtimes.dflt.runtime.system.ContextCategory;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.internal.InitialisationSession;
import org.apache.isis.runtimes.dflt.runtime.system.session.IsisSession;
import org.apache.isis.runtimes.dflt.runtime.system.session.IsisSessionFactory;
import org.apache.isis.viewer.wicket.viewer.integration.wicket.AuthenticatedWebSessionForIsis;

/**
 * Implementation of Isis' {@link IsisContext}, associating a
 * {@link IsisSession} with a Wicket {@link Session}.
 * 
 * <p>
 * This implementation also takes multi-threading into account, so that the
 * browser can submit multiple requests on the same session simultaneously (eg
 * to render an image of a pojo).
 */
public class IsisContextForWicket extends IsisContext {

    private static final Logger LOG = Logger.getLogger(IsisContextForWicket.class);
    
    public static class WicketContextCategory extends ContextCategory {

        @Override
        public boolean canSpecifyViewers(final List<String> viewers) {
            return false;
        }

        @Override
        public void initContext(final IsisSessionFactory sessionFactory) {
            new IsisContextForWicket(ContextReplacePolicy.NOT_REPLACEABLE, SessionClosePolicy.EXPLICIT_CLOSE, sessionFactory);
        }
    }

    private static final class GetSessionIdFunction implements Function<Session, String> {
        @Override
        public String apply(final Session from) {
            return from.getId();
        }
    }

    /**
     * Only used while bootstrapping, corresponding to the
     * {@link InitialisationSession}.
     */
    private IsisSession bootstrapSession;
    /**
     * Maps (our custom) {@link AuthenticatedWebSessionForIsis Wicket session}s
     * to vanilla {@link IsisSession}s.
     */
    private final Map<AuthenticatedWebSessionForIsis, IsisSession> sessionMap = Maps.newHashMap();

    protected IsisContextForWicket(final ContextReplacePolicy replacePolicy, final SessionClosePolicy sessionClosePolicy, final IsisSessionFactory sessionFactory) {
        super(replacePolicy, sessionClosePolicy, sessionFactory);
    }

    @Override
    public String[] allSessionIds() {
        final Collection<String> transform = Collections2.transform(sessionMap.keySet(), new GetSessionIdFunction());
        return transform.toArray(new String[0]);
    }

    @Override
    protected void closeAllSessionsInstance() {
        throw new NotYetImplementedException();
    }

    @Override
    protected IsisSession getSessionInstance(final String sessionId) {
        throw new NotYetImplementedException();
    }

    @Override
    public IsisSession getSessionInstance() {
        // special case handling if still bootstrapping
        if (bootstrapSession != null) {
            return bootstrapSession;
        }

        final Session session = Session.get();
        final IsisSession isisSession = sessionMap.get(session);
        return isisSession;
    }

    @Override
    public IsisSession openSessionInstance(final AuthenticationSession session) {

        // special case handling if still bootstrapping
        if (session instanceof InitialisationSession) {
            bootstrapSession = getSessionFactory().openSession(session);
            bootstrapSession.open();
            return bootstrapSession;
        }

        // otherwise, regular processing
        return openSessionOrRegisterUsageOnExisting(session);
    }

    private synchronized IsisSession openSessionOrRegisterUsageOnExisting(final AuthenticationSession authSession) {
        // we don't apply any session close policy here;
        // there could be multiple threads using a session.

        final AuthenticatedWebSessionForIsis webSession = (AuthenticatedWebSessionForIsis) Session.get();
        synchronized (webSession) {
            final String webSessionId = webSession.getId();
        
            final int before = webSession.getThreadUsage();
            webSession.registerUseByThread();
            final int after = webSession.getThreadUsage();
    
            final String logMsg; 
            IsisSession isisSession = sessionMap.get(webSession);
            if (isisSession == null) {
                isisSession = getSessionFactoryInstance().openSession(authSession);
                // put into map prior to opening, so that subsequent calls to
                // getSessionInstance() will find this new session.
                sessionMap.put(webSession, isisSession);
                isisSession.open();
                
                logMsg = "NEW    ";
            } else {
                logMsg = "BUMP_UP";
            }
            LOG.debug(String.format("webSession: %s OPEN  %d -> %d %s %s", webSessionId, before, after, logMsg, isisSession.getId()));
    
            return isisSession;
        }
    }

    @Override
    public synchronized void closeSessionInstance() {
        // special case handling if still bootstrapping
        if (bootstrapSession != null) {

            bootstrapSession.close();
            bootstrapSession = null;
            return;
        }

        // otherwise, regular processing
        closeSessionOrDeregisterUsageOnExisting();
    }

    private synchronized void closeSessionOrDeregisterUsageOnExisting() {
        final AuthenticatedWebSessionForIsis webSession = (AuthenticatedWebSessionForIsis) Session.get();
        synchronized (webSession) {
            final String webSessionId = webSession.getId();
            
            final int before = webSession.getThreadUsage();
            final boolean shouldClose = webSession.deregisterUseByThread();
            final int after = webSession.getThreadUsage();

            final IsisSession isisSession = sessionMap.get(webSession);
            
            final String logMsg; 
            if (isisSession == null) {
                // nothing to be done !?!?
                logMsg = "NO_SESSION";
            } else {
                if (shouldClose) {
                    isisSession.close();
                    // remove after closing, so that any calls to getSessionInstance()
                    // made while closing will still find this session
                    sessionMap.remove(webSession);
                    logMsg = "DISCARDING";
                } else {
                    logMsg = "BUMP_DOWN ";
                }
            }

            LOG.debug(String.format("webSession: %s CLOSE %d -> %d %s %s", webSessionId, before, after, logMsg, (isisSession != null? isisSession.getId(): "[null]")));
        }
    }

    @Override
    public String debugTitle() {
        return "Wicket Context";
    }

}
