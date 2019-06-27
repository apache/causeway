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
package org.apache.isis.jdo.transaction;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.xactn.Transaction;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionAspectSupport;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionObject;
import org.apache.isis.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.security.authentication.AuthenticationSession;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Singleton @Log4j2
public class IsisPlatformTransactionManagerForJdo extends AbstractPlatformTransactionManager {
	
	private static final long serialVersionUID = 1L;
	
	@Inject private IsisSessionFactory isisSessionFactory;
	@Inject private PersistenceSessionServiceInternal persistenceSessionServiceInternal;
	@Inject private ServiceRegistry serviceRegistry;

	@Override
	protected Object doGetTransaction() throws TransactionException {
		
		val isInSession = IsisSession.isInSession();
		log.debug("doGetTransaction isInSession={}", isInSession);
		
		if(!isInSession) {

			// get authenticationSession from IoC, or fallback to InitialisationSession 
			val authenticationSession = serviceRegistry.select(AuthenticationSession.class)
					.getFirst()
					.orElseGet(InitialisationSession::new);
					
			log.debug("open new session authenticationSession={}", authenticationSession);
			
			isisSessionFactory.openSession(authenticationSession);
			val transactionBeforeBegin = (Transaction)null;
			return IsisTransactionObject.of(transactionBeforeBegin);
		} else {
			val transactionBeforeBegin = persistenceSessionServiceInternal.currentTransaction(); 
			return IsisTransactionObject.of(transactionBeforeBegin);
		}
		
	}
	
	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
		IsisTransactionObject txObject = (IsisTransactionObject) transaction;
		
		log.debug("doBegin {}", definition);
		
		persistenceSessionServiceInternal.beginTran();
		txObject.setCurrentTransaction(persistenceSessionServiceInternal.currentTransaction());
		IsisTransactionAspectSupport.putTransactionObject(txObject);
	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
		IsisTransactionObject txObject = (IsisTransactionObject) status.getTransaction();

		log.debug("doCommit {}", status);
		
		persistenceSessionServiceInternal.commit();
		txObject.setCurrentTransaction(null);
		IsisTransactionAspectSupport.clearTransactionObject();
	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
		IsisTransactionObject txObject = (IsisTransactionObject) status.getTransaction();
		
		log.debug("doRollback {}", status);
		
		persistenceSessionServiceInternal.abortTransaction();
		txObject.setCurrentTransaction(null);
		IsisTransactionAspectSupport.clearTransactionObject();
	}

	
}
