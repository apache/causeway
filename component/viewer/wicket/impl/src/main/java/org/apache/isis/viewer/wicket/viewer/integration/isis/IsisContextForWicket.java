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
import org.apache.isis.core.runtime.system.ContextCategory;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
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

    private static final class GetSessionIdFunction implements Function<SessionKey, String> {
        @Override
        public String apply(final SessionKey from) {
            return from.getId();
        }
    }

    private enum SessionType {
        WICKET {
            @Override
            public String getId(SessionKey sessionKey) {
                return sessionKey.wicketSession.getId();
            }

            @Override
            public IsisSession beginInteraction(final SessionKey sessionKey, final AuthenticationSession authSession, final IsisSessionFactory sessionFactory, final Map<SessionKey, IsisSession> sessionMap) {
                final AuthenticatedWebSessionForIsis wicketSession = sessionKey.wicketSession;
                synchronized (wicketSession) {
                    // we don't apply any session close policy here;
                    // there could be multiple threads using a session.

                    final String wicketSessionId = wicketSession.getId();
                
                    final int before = wicketSession.getThreadUsage();
                    wicketSession.registerUseByThread();
                    final int after = wicketSession.getThreadUsage();
            
                    String logMsg = ""; 
                    IsisSession isisSession = sessionMap.get(sessionKey);
                    try {
                        if (isisSession != null) {
                            logMsg = "BUMP_UP";
                        } else {
                            isisSession = sessionFactory.openSession(authSession);
                            // put into map prior to opening, so that subsequent calls to
                            // getSessionInstance() will find this new session.
                            sessionMap.put(sessionKey, isisSession);
                            isisSession.open();
                            
                            logMsg = "NEW    ";
                        }
                
                        return isisSession;
                    } finally {
                        if(LOG.isDebugEnabled()) {
                            LOG.debug(String.format("wicketSession: %s OPEN  %d -> %d %s %s %s", wicketSessionId, before, after, logMsg, authSession.getUserName(), isisSession.getId()));
                        }
                    }
                }
            }

            @Override
            public void endInteraction(SessionKey sessionKey, final Map<SessionKey, IsisSession> sessionMap) {
                final AuthenticatedWebSessionForIsis wicketSession = sessionKey.wicketSession;
                synchronized (wicketSession) {
                    final String wicketSessionId = wicketSession.getId();
                    
                    final int before = wicketSession.getThreadUsage();
                    final boolean shouldClose = wicketSession.deregisterUseByThread();
                    final int after = wicketSession.getThreadUsage();

                    final IsisSession isisSession = sessionMap.get(sessionKey);
                    AuthenticationSession authSession = null;
                    String logMsg = ""; 
                    try {
                        if (isisSession == null) {
                            // nothing to be done !?!?
                            logMsg = "NO_SESSION";
                            return; 
                        }
                        authSession = isisSession.getAuthenticationSession();
                        
                        if (!shouldClose) {
                            logMsg = "BUMP_DOWN ";
                            // don't remove from map    
                            return;
                        } 
                        
                        isisSession.close();
                        logMsg = "DISCARDING";
                        
                        // the remove happens after closing, any calls to getSessionInstance()
                        // made while closing will still find this session
                        sessionMap.remove(sessionKey);
                        
                    } finally {
                        if(LOG.isDebugEnabled()) {
                            LOG.debug(String.format("wicketSession: %s CLOSE %d -> %d %s %s %s", wicketSessionId, before, after, logMsg, (authSession != null? authSession.getUserName(): "[null]"), (isisSession != null? isisSession.getId(): "[null]")));
                        }
                    }
                }
            }

            @Override
            public boolean equals(SessionKey sessionKey, SessionKey other) {
                return sessionKey.wicketSession == other.wicketSession;
            }

            @Override
            public int hashCode(SessionKey sessionKey) {
                return sessionKey.wicketSession.hashCode();
            }
        },
        THREAD {
            @Override
            public String getId(SessionKey sessionKey) {
                return ""+sessionKey.thread.getId();
            }

            @Override
            public IsisSession beginInteraction(final SessionKey sessionKey, final AuthenticationSession authSession, final IsisSessionFactory sessionFactory, final Map<SessionKey, IsisSession> sessionMap) {
                // auto-close if required
                endInteraction(sessionKey, sessionMap);
                
                final String threadName = sessionKey.thread.getName();
                final IsisSession isisSession = sessionFactory.openSession(authSession);
                try {
                    sessionMap.put(sessionKey, isisSession);
                    isisSession.open();
                    return isisSession;
                } finally {
                    if(LOG.isDebugEnabled()) {
                        LOG.debug(String.format("threadSession: %s OPEN  %s %s", threadName, authSession.getUserName(), isisSession.getId()));
                    }
                }
            }
            

            @Override
            public void endInteraction(SessionKey sessionKey, Map<SessionKey, IsisSession> sessionMap) {
                final IsisSession isisSession = sessionMap.get(sessionKey);
                if(isisSession == null) {
                    return; // nothing to do
                }
                final String threadName = sessionKey.thread.getName();
                final AuthenticationSession authSession = isisSession.getAuthenticationSession();
                try {
                    
                    isisSession.close();
                    sessionMap.remove(sessionKey);
                } finally {
                    if(LOG.isDebugEnabled()) {
                        LOG.debug(String.format("threadSession: %s CLOSE %s %s", threadName, authSession.getUserName(), isisSession.getId()));
                    }
                }
            }

            @Override
            public boolean equals(SessionKey sessionKey, SessionKey other) {
                return sessionKey.thread == other.thread;
            }

            @Override
            public int hashCode(SessionKey sessionKey) {
                return sessionKey.thread.hashCode();
            }
        };

        public abstract String getId(SessionKey sessionKey);

        public abstract IsisSession beginInteraction(final SessionKey sessionKey, final AuthenticationSession authSession, final IsisSessionFactory sessionFactory, final Map<SessionKey, IsisSession> sessionMap);

        public abstract void endInteraction(SessionKey sessionKey, final Map<SessionKey, IsisSession> sessionMap);

        public abstract boolean equals(SessionKey sessionKey, SessionKey other);
        public abstract int hashCode(SessionKey sessionKey);
    }
    
    private static class SessionKey {
        private final SessionType type;
        private final AuthenticatedWebSessionForIsis wicketSession;
        private final Thread thread;
        private SessionKey(SessionType type, Session wicketSession, Thread thread) {
            this.type = type;
            this.wicketSession = (AuthenticatedWebSessionForIsis) wicketSession;
            this.thread = thread;
        }
        public String getId() {
            return type.getId(this);
        }
        static SessionKey get() {
            return Session.exists()? new SessionKey(SessionType.WICKET, Session.get(), null): new SessionKey(SessionType.THREAD, null, Thread.currentThread());
        }
        public IsisSession beginInteraction(final AuthenticationSession authSession, final IsisSessionFactory sessionFactory, final Map<SessionKey, IsisSession> sessionMap) {
            synchronized (sessionMap) {
                return type.beginInteraction(this, authSession, sessionFactory, sessionMap);
            }
        }
        public void endInteraction(final Map<SessionKey, IsisSession> sessionMap) {
            synchronized (sessionMap) {
                type.endInteraction(this, sessionMap);
            }
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SessionKey other = (SessionKey) obj;
            if (type != other.type)
                return false;
            return type.equals(this, other);
        }
        @Override
        public int hashCode() {
            return type.hashCode(this);
        }
        @Override
        public String toString() {
            return "SessionKey[" + type + "]:id=" + getId();
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
    private final Map<SessionKey, IsisSession> sessionMap = Maps.newHashMap();

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
        SessionKey sessionKey = SessionKey.get();
        return sessionMap.get(sessionKey);
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
        SessionKey sessionKey = SessionKey.get();
        return sessionKey.beginInteraction(authSession, getSessionFactoryInstance(), sessionMap);
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
        SessionKey sessionKey = SessionKey.get();
        sessionKey.endInteraction(sessionMap);
    }

    @Override
    public String debugTitle() {
        return "Wicket Context";
    }

}
