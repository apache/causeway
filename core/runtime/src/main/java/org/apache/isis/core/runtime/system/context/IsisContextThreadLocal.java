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

import java.util.IdentityHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

/**
 * Multi-user implementation of {@link IsisContext} that stores a set of components for
 * each thread in use.
 */
public class IsisContextThreadLocal extends IsisContext {

    private static final Logger LOG = LoggerFactory.getLogger(IsisContextThreadLocal.class);

    public static IsisContext createInstance(final IsisSessionFactory sessionFactory) {
        return new IsisContextThreadLocal(sessionFactory);
    }

    private final Map<Thread, IsisSession> sessionsByThread = new IdentityHashMap<>();

    
    // //////////////////////////////////////////////
    // Constructor
    // //////////////////////////////////////////////

    protected IsisContextThreadLocal(final IsisSessionFactory sessionFactory) {
        this(ContextReplacePolicy.NOT_REPLACEABLE, SessionClosePolicy.EXPLICIT_CLOSE, sessionFactory);
    }

    protected IsisContextThreadLocal(final ContextReplacePolicy contextReplacePolicy, final SessionClosePolicy sessionClosePolicy, final IsisSessionFactory sessionFactory) {
        super(contextReplacePolicy, sessionClosePolicy, sessionFactory);
    }


    // /////////////////////////////////////////////////////////
    // Session
    // /////////////////////////////////////////////////////////

    @Override
    public void closeAllSessionsInstance() {
        shutdownAllThreads();
    }

    protected void shutdownAllThreads() {
        synchronized (sessionsByThread) {
            for (final Map.Entry<Thread, IsisSession> entry : sessionsByThread.entrySet()) {
                LOG.info("Shutting down thread: {}", entry.getKey().getName());
                final IsisSession data = entry.getValue();
                data.closeAll();
            }
        }
    }

    @Override
    protected void doClose() {
        sessionsByThread.remove(Thread.currentThread());
    }

    // /////////////////////////////////////////////////////////
    // Execution Context Ids
    // /////////////////////////////////////////////////////////

    @Override
    public String[] allSessionIds() {
        final String[] ids = new String[sessionsByThread.size()];
        int i = 0;
        for (final IsisSession data  : sessionsByThread.values()) {
            ids[i++] = data.getId();
        }
        return ids;
    }

    // /////////////////////////////////////////////////////////
    // Debugging
    // /////////////////////////////////////////////////////////

    @Override
    public String debugTitle() {
        return "Isis (by thread) " + Thread.currentThread().getName();
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        super.debugData(debug);
        debug.appendTitle("Threads based Contexts");
        for (final Map.Entry<Thread, IsisSession> entry : sessionsByThread.entrySet()) {
            final Thread thread = entry.getKey();
            final IsisSession data = entry.getValue();
            debug.appendln(thread.toString(), data);
        }
    }

    @Override
    protected IsisSession getSessionInstance(final String executionContextId) {
        for (final IsisSession data : sessionsByThread.values()) {
            if (data.getId().equals(executionContextId)) {
                return data;
            }
        }
        return null;
    }

    // /////////////////////////////////////////////////////////
    // open, close
    // /////////////////////////////////////////////////////////

    /**
     * Is only intended to be called through
     * {@link IsisContext#openSession(AuthenticationSession)}.
     * 
     * <p>
     * Implementation note: an alternative design would have just been to bind
     * onto a thread local.
     */
    @Override
    public IsisSession openSessionInstance(final AuthenticationSession authenticationSession) {
        final Thread thread = Thread.currentThread();
        synchronized (sessionsByThread) {
            applySessionClosePolicy();
            final IsisSession session = getSessionFactoryInstance().openSession(authenticationSession);
            if (LOG.isDebugEnabled()) {
                LOG.debug("  opening session " + session + " (count " + sessionsByThread.size() + ") for " + authenticationSession.getUserName());
            }
            saveSession(thread, session);
            session.open();
            return session;
        }
    }

    protected IsisSession createAndOpenSession(final Thread thread, final AuthenticationSession authenticationSession) {
        final IsisSession session = getSessionFactoryInstance().openSession(authenticationSession);
        session.open();
        if (LOG.isInfoEnabled()) {
            LOG.info("  opening session " + session + " (count " + sessionsByThread.size() + ") for " + authenticationSession.getUserName());
        }
        return session;
    }

    private IsisSession saveSession(final Thread thread, final IsisSession session) {
        synchronized (sessionsByThread) {
            sessionsByThread.put(thread, session);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("  saving session " + session + "; now have " + sessionsByThread.size() + " sessions");
        }
        return session;
    }

    // /////////////////////////////////////////////////////////
    // getCurrent() (Hook)
    // /////////////////////////////////////////////////////////

    /**
     * Get {@link IsisSession execution context} used by the current thread.
     * 
     * @see #openSessionInstance(AuthenticationSession)
     */
    @Override
    public IsisSession getSessionInstance() {
        final Thread thread = Thread.currentThread();
        final IsisSession session = sessionsByThread.get(thread);
        return session;
    }

}
