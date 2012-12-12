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

package org.apache.isis.core.runtime.system.context;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

/**
 * Provides <i>access to</i> the current {@link IsisSession} in a single-user
 * {@link DeploymentType deployment} (and thus implemented as a <tt>static</tt>
 * singleton).
 */
public class IsisContextStatic extends IsisContext {

    private IsisSession session;

    // //////////////////////////////////////////////
    // Singleton & Constructor
    // //////////////////////////////////////////////

    public static IsisContext createInstance(final IsisSessionFactory sessionFactory) {
        return new IsisContextStatic(ContextReplacePolicy.NOT_REPLACEABLE, SessionClosePolicy.EXPLICIT_CLOSE, sessionFactory);
    }

    /**
     * Intended for testing; the singleton can be replaced and sessions are
     * autoclosed.
     */
    public static IsisContext createRelaxedInstance(final IsisSessionFactory sessionFactory) {
        return new IsisContextStatic(ContextReplacePolicy.REPLACEABLE, SessionClosePolicy.AUTO_CLOSE, sessionFactory);
    }

    protected IsisContextStatic(final ContextReplacePolicy replacePolicy, final SessionClosePolicy sessionClosePolicy, final IsisSessionFactory sessionFactory) {
        super(replacePolicy, sessionClosePolicy, sessionFactory);
    }

    @Override
    public IsisSession getSessionInstance() {
        return session;
    }

    // //////////////////////////////////////////////
    // open, close
    // //////////////////////////////////////////////

    @Override
    public IsisSession openSessionInstance(final AuthenticationSession authenticationSession) {
        applySessionClosePolicy();
        session = getSessionFactoryInstance().openSession(authenticationSession);
        session.open();
        return session;
    }

    @Override
    public void doClose() {
        session = null;
    }

    // //////////////////////////////////////////////
    // sessionId(s)
    // //////////////////////////////////////////////

    @Override
    protected IsisSession getSessionInstance(final String sessionId) {
        return getSessionInstance();
    }

    @Override
    public String[] allSessionIds() {
        return new String[] { getSessionInstance().getId() };
    }

    // //////////////////////////////////////////////
    // Session
    // //////////////////////////////////////////////

    @Override
    public void closeAllSessionsInstance() {
        final IsisSession sessionInstance = getSessionInstance();
        if (sessionInstance != null) {
            sessionInstance.closeAll();
        }
    }

    // //////////////////////////////////////////////
    // Debugging
    // //////////////////////////////////////////////

    @Override
    public String debugTitle() {
        return "Static Context";
    }

    public void debug(final DebugBuilder debug) {
        debug.appendAsHexln("hash", hashCode());
        session.debugState(debug);
    }

    public void debugAll(final DebugBuilder debug) {
        debug(debug);
        debug.appendln();

        debug(debug, getPersistenceSession());
    }

    private void debug(final DebugBuilder debug, final Object object) {
        if (object instanceof DebuggableWithTitle) {
            final DebuggableWithTitle d = (DebuggableWithTitle) object;
            debug.appendTitle(d.debugTitle());
            d.debugData(debug);
        } else {
            debug.appendln("no debug for " + object);
        }
    }

}
