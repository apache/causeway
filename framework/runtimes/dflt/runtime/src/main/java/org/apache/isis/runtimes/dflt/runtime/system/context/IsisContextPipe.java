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

package org.apache.isis.runtimes.dflt.runtime.system.context;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.runtimes.dflt.runtime.system.session.IsisSession;
import org.apache.isis.runtimes.dflt.runtime.system.session.IsisSessionFactory;

/**
 * A specialised IsisContext implementation that provides two sets of
 * components: one for the server; and one for the client. This simply
 * determines the current thread and if that thread is the server thread then it
 * provides server data. For any other thread the client data is used.
 */
public class IsisContextPipe extends IsisContextMultiUser {

    public static IsisContext createInstance(final IsisSessionFactory sessionFactory) {
        return new IsisContextPipe(sessionFactory);
    }

    private IsisSession clientSession;
    private IsisSession serverSession;

    private Thread server;

    // ///////////////////////////////////////////////////
    // Constructor
    // ///////////////////////////////////////////////////

    private IsisContextPipe(final IsisSessionFactory sessionFactory) {
        super(sessionFactory);
    }

    // ///////////////////////////////////////////////////
    // Server (not API)
    // ///////////////////////////////////////////////////

    public void setServer(final Thread server) {
        this.server = server;
    }

    private boolean isCurrentThreadServer() {
        return Thread.currentThread() == server;
    }

    // ///////////////////////////////////////////////////
    // getCurrent() Hook
    // ///////////////////////////////////////////////////

    @Override
    protected IsisSession getSessionInstance(final String sessionId) {
        return null;
    }

    @Override
    public IsisSession getSessionInstance() {
        if (isCurrentThreadServer()) {
            return serverSession;
        } else {
            return clientSession;
        }
    }

    @Override
    public IsisSession openSessionInstance(final AuthenticationSession authenticationSession) {
        applySessionClosePolicy();
        final IsisSession newSession = getSessionFactoryInstance().openSession(authenticationSession);
        if (isCurrentThreadServer()) {
            serverSession = newSession;
        } else {
            clientSession = newSession;
        }
        return newSession;
    }

    // ///////////////////////////////////////////////////
    // shutdown
    // ///////////////////////////////////////////////////

    @Override
    public void closeAllSessionsInstance() {
    }

    // ///////////////////////////////////////////////////
    // Execution Context Ids
    // ///////////////////////////////////////////////////

    @Override
    public String[] allSessionIds() {
        return new String[] { clientSession.getId(), serverSession.getId() };
    }

    // ///////////////////////////////////////////////////
    // Debugging
    // ///////////////////////////////////////////////////

    @Override
    public String debugTitle() {
        return "Isis (pipe) " + Thread.currentThread().getName();
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        super.debugData(debug);
        debug.appendln("Server thread", server);
    }

}
