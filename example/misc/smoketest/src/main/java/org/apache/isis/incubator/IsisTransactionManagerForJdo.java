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
package org.apache.isis.incubator;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Singleton @Log4j2
public class IsisTransactionManagerForJdo implements PlatformTransactionManager {
	
	@Inject private IsisSessionFactory isisSessionFactory;
	@Inject private TransactionService transactionService;

	@Override
	public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
		
		log.debug("get a transaction {}", ()->""+definition);
		
		if(!IsisSession.isInSession()) {
			isisSessionFactory.openSession(new InitialisationSession());	
		}
		
		transactionService.nextTransaction();
		
		val isNewTransaction = true;

		return new SimpleTransactionStatus(isNewTransaction) {
			
			@Override
			public void flush() {
				val tx = transactionService.currentTransaction();
				if(tx.getTransactionState().canFlush()) {
					tx.flush();
				}
			}

			@Override
			public boolean isCompleted() {
				val tx = transactionService.currentTransaction();
				return tx.getTransactionState().isComplete();
			}
		};
	}

	@Override
	public void commit(TransactionStatus status) throws TransactionException {
		log.debug("about to commit {}", ()->""+status);
		
		transactionService.nextTransaction();
		((SimpleTransactionStatus)status).setCompleted();
	}

	@Override
	public void rollback(TransactionStatus status) throws TransactionException {
		log.debug("about to rollback {}", ()->""+status);
		log.warn("not implemented yet");
	}

	
}
