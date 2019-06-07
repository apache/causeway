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

package org.apache.isis.core.runtime.system.session;

import java.util.Optional;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.context.session.RuntimeContextBase;
import org.apache.isis.core.runtime.system.context.session.RuntimeEventService;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManagerJdoInternal;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.MessageBroker;

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

	private RuntimeEventService runtimeEventService;
	
	/**
     * Set to System.nanoTime() when session opens.
     */
	@Getter private long openedAtSystemNanos = -1L;
    
	public IsisSession(
			final RuntimeEventService runtimeEventService,
			final AuthenticationSession authenticationSession) {
		
		super(IsisContext.getConfiguration(),
				IsisContext.getServiceInjector(),
				IsisContext.getServiceRegistry(),
				IsisContext.getSpecificationLoader(),
				authenticationSession,
				IsisContext.getObjectAdapterProvider());
		
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
	
	public static Optional<IsisTransactionManagerJdoInternal> transactionManager() {
		return current()
				.map(IsisSession::getTransactionManager);
	}
	
	// -- OPEN
    
    void open() {
    	openedAtSystemNanos = System.nanoTime();
    	_Context.threadLocalPut(IsisSession.class, this);
    	runtimeEventService.fireSessionOpened(this);
    }

    // -- CLOSE
    
    /**
     * Closes session.
     */
    void close() {
        runtimeEventService.fireSessionClosing(this);
        _Context.threadLocalCleanup();
    }

    // -- FLUSH
//    void flush() {
//    	runtimeEventService.fireSessionFlushing(this);
//    }
    
    // -- TRANSACTION

    /**
     * Convenience method that returns the {@link IsisTransaction} of the
     * session, if any.
     */
    public IsisTransaction getCurrentTransaction() {
        return getTransactionManager().getCurrentTransaction();
    }

    
    


    // -- toString
    @Override
    public String toString() {
        final ToString asString = new ToString(this);
        asString.append("authenticationSession", getAuthenticationSession());
        asString.append("persistenceSession", PersistenceSession.current(PersistenceSession.class));
        asString.append("transaction", getCurrentTransaction());
        return asString.toString();
    }


    // -- Dependencies (from constructor)

    private IsisTransactionManagerJdoInternal getTransactionManager() {
        return IsisContext.getTransactionManagerJdo().get();
    }

	


}
