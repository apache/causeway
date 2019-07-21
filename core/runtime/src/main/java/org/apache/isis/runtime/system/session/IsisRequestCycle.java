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
package org.apache.isis.runtime.system.session;

import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.persistence.PersistenceSession;
import org.apache.isis.security.authentication.AuthenticationSession;

import lombok.val;

/**
 * TODO [2033] intent is to remove direct dependencies upon Persistence/Transaction for the viewer-modules.   
 * 
 * @since 2.0
 */
public class IsisRequestCycle implements AutoCloseable {

	// -- SUPPORTING ISIS TRANSACTION FILTER FOR RESTFUL OBJECTS ...
	
	public static IsisRequestCycle next() {
		return new IsisRequestCycle();
	}
	
	private IsisRequestCycle() {
    	
	}
	
	public void beforeServletFilter() {
		val isisTransactionManager = IsisContext.getTransactionManagerJdo().orElse(null);
        // no-op if no session or transaction manager available.
        if(isisTransactionManager==null) {
            return;
        }
        isisTransactionManager.startTransaction();
		
	}

	public void afterServletFilter() {
		// relying on the caller to close this cycle in a finally block
	}
	
	@Override
	public void close() {
		
		val isisSessionFactory = IsisContext.getSessionFactory();
		val isisTransactionManager = IsisContext.getTransactionManagerJdo().orElse(null);
		val inTransaction =
				isisSessionFactory !=null && 
				isisTransactionManager!=null &&
				isisSessionFactory.isInTransaction(); 
		
        if(inTransaction) {
            // user/logout will have invalidated the current transaction and also persistence session.
            try {
                isisTransactionManager.endTransaction();
            } catch (Exception ex) {
                // ignore.  Any exceptions will have been mapped into a suitable response already.
            }
        }
		
	}
	
	// -- SUPPORTING WEB REQUEST CYCLE FOR ISIS ...

	public static void onBeginRequest(AuthenticationSession authenticationSession) {
		
		val isisSessionFactory = IsisContext.getSessionFactory();
		isisSessionFactory.openSession(authenticationSession);
		
		IsisContext.getTransactionManagerJdo()
				.ifPresent(txMan->txMan.startTransaction());
	}

	public static void onRequestHandlerExecuted() {
		
		val isisTransactionManager = IsisContext.getTransactionManagerJdo().orElse(null);
		if (isisTransactionManager==null) {
			return;
		}
		
        try {
            // will commit (or abort) the transaction;
            // an abort will cause the exception to be thrown.
        	isisTransactionManager.endTransaction();
        	
        } catch(Exception ex) {

        	// will redirect to error page after this,
            // so make sure there is a new transaction ready to go.
            if(isisTransactionManager.getCurrentTransaction().getState().isComplete()) {
            	isisTransactionManager.startTransaction();
            }
            
            throw ex;
        }
		
	}

	public static void onEndRequest() {
		
		val isisTransactionManager = IsisContext.getTransactionManagerJdo().orElse(null);
		if (isisTransactionManager==null) {
			return;
		}
		
        try {
        	isisTransactionManager.endTransaction();
        } finally {
        	val isisSessionFactory = IsisContext.getSessionFactory();
        	isisSessionFactory.closeSession();
        }
        
	}
	
	// -- SUPPORTING FORM EXECUTOR DEFAULT ...

	public static void onResultAdapterObtained() {
		val isisSession = IsisSession.currentOrElseNull();
		if (isisSession==null) {
			return;
		}
		
		PersistenceSession.current(PersistenceSession.class)
		.stream()
		.forEach(ps->ps.flush());
		
		//isisSession.flush();
	}


	
	// -- 

}
