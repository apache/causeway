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

package org.apache.isis.runtime.session;

import java.util.Optional;

import org.apache.isis.applib.services.xactn.TransactionId;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.core.commons.internal.context._Context;
import org.apache.isis.metamodel.commons.ToString;
import org.apache.isis.metamodel.context.MetaModelContext;
import org.apache.isis.runtime.context.session.RuntimeContextBase;
import org.apache.isis.runtime.context.session.RuntimeEventService;
import org.apache.isis.runtime.persistence.session.PersistenceSession;
import org.apache.isis.security.api.authentication.AuthenticationSession;
import org.apache.isis.security.api.authentication.MessageBroker;

import lombok.Getter;

/**
 * Holds the current set of components for a specific execution context (such as on a thread).
 *
 * <p>
 * The <code>IsisContext</code> class is responsible for locating the current execution context.
 *
 * @see IsisSessionFactory
 */
public class IsisSession extends RuntimeContextBase {

    private final RuntimeEventService runtimeEventService;
    
    @Getter(onMethod = @__(@Override))
    private final AuthenticationSession authenticationSession;

    /**
     * Set to System.nanoTime() when session opens.
     */
    @Getter private long openedAtSystemNanos = -1L;

    /**
     * 
     * @param runtimeEventService
     * @param authenticationSession
     * @implNote package private constructor, to let only IsisSessionFactory have access to it, 
     * since it must keep track of all opened sessions
     */
    IsisSession(
            final MetaModelContext mmc,
            final RuntimeEventService runtimeEventService,
            final AuthenticationSession authenticationSession) {

        super(mmc);
        this.authenticationSession = authenticationSession; // binds this session to given authenticationSession 
        this.runtimeEventService = runtimeEventService;
    }

    // -- CURRENT

    public static IsisSession currentOrElseNull() {
        return current().orElse(null);
    }

    public static Optional<IsisSession> current() {
        return _Context.threadLocalGet(IsisSession.class)
                .getSingleton();
    }

    public static boolean isInSession() {
        return currentOrElseNull() != null;
    }

    // -- SHORTCUTS

    public static Optional<AuthenticationSession> authenticationSession() {
        return current()
                .map(IsisSession::getAuthenticationSession);
    }

    public static Optional<MessageBroker> messageBroker() {
        return authenticationSession()
                .map(AuthenticationSession::getMessageBroker);
    }

    // -- OPEN

    private Runnable cleanupHandle;  

    void open() {
        openedAtSystemNanos = System.nanoTime();
        cleanupHandle = _Context.threadLocalPut(IsisSession.class, this);
        runtimeEventService.fireSessionOpened(this);
    }

    // -- CLOSE

    /**
     * Closes session.
     */
    void close() {
        runtimeEventService.fireSessionClosing(this);
        if(cleanupHandle!=null) {
            cleanupHandle.run();
        }
    }

    // -- FLUSH
    //    void flush() {
    //    	runtimeEventService.fireSessionFlushing(this);
    //    }

    // -- TRANSACTION

    public TransactionId getCurrentTransactionId() {
        return transactionService.currentTransactionId();
    }

    public TransactionState getCurrentTransactionState() {
        return transactionService.currentTransactionState();
    }


    // -- toString
    @Override
    public String toString() {
        final ToString asString = new ToString(this);
        asString.append("authenticationSession", getAuthenticationSession());
        asString.append("persistenceSession", PersistenceSession.current(PersistenceSession.class));
        asString.append("transaction", getCurrentTransactionId());
        return asString.toString();
    }


}
