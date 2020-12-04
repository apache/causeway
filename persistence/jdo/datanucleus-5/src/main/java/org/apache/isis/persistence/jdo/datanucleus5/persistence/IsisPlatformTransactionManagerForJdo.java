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
package org.apache.isis.persistence.jdo.datanucleus5.persistence;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.runtime.iactn.InteractionFactory;
import org.apache.isis.core.runtime.iactn.InteractionTracker;
import org.apache.isis.core.runtime.persistence.transaction.IsisTransactionAspectSupport;
import org.apache.isis.core.runtime.persistence.transaction.IsisTransactionObject;
import org.apache.isis.core.runtime.persistence.transaction.IsisTransactionObject.IsisInteractionScopeType;
import org.apache.isis.core.runtime.persistence.transaction.events.TransactionAfterBeginEvent;
import org.apache.isis.core.runtime.persistence.transaction.events.TransactionAfterCommitEvent;
import org.apache.isis.core.runtime.persistence.transaction.events.TransactionAfterRollbackEvent;
import org.apache.isis.core.runtime.persistence.transaction.events.TransactionBeforeBeginEvent;
import org.apache.isis.core.runtime.persistence.transaction.events.TransactionBeforeCommitEvent;
import org.apache.isis.core.runtime.persistence.transaction.events.TransactionBeforeRollbackEvent;
import org.apache.isis.core.runtime.session.init.InitialisationSession;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named("isisJdoDn5.IsisPlatformTransactionManagerForJdo")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("JdoDN5")
@Log4j2
public class IsisPlatformTransactionManagerForJdo extends AbstractPlatformTransactionManager {


    private static final long serialVersionUID = 1L;

    private final InteractionFactory isisInteractionFactory;
    private final EventBusService eventBusService;
    private final InteractionTracker isisInteractionTracker;

    @Inject
    public IsisPlatformTransactionManagerForJdo(
            final InteractionFactory isisInteractionFactory,
            final EventBusService eventBusService,
            final InteractionTracker isisInteractionTracker) {
        this.isisInteractionFactory = isisInteractionFactory;
        this.eventBusService = eventBusService;
        this.isisInteractionTracker = isisInteractionTracker;
    }

    @Override
    protected Object doGetTransaction() throws TransactionException {

        val isInInteraction = isisInteractionTracker.isInInteractionSession();
        log.debug("doGetTransaction isInSession={}", isInInteraction);

        val transactionBeforeBegin = 
                IsisTransactionAspectSupport
                .currentTransactionObject()
                .map(IsisTransactionObject::getCurrentTransaction)
                .orElse(null);
        
        if(!isInInteraction) {
            
            if(Utils.isJUnitTest()) {
            
                val authenticationSession = isisInteractionTracker.currentAuthenticationSession()
                        .orElseGet(InitialisationSession::new);

                log.debug("open new session authenticationSession={}", authenticationSession);
                isisInteractionFactory.openInteraction(authenticationSession);
                
                return IsisTransactionObject.of(transactionBeforeBegin, IsisInteractionScopeType.TEST_SCOPED);

            } else {

                throw _Exceptions.illegalState("No IsisInteraction available. "
                        + "Transactions are expected to be nested within the life-cycle of an IsisInteraction.");
                
            }
            
        }

        return IsisTransactionObject.of(transactionBeforeBegin, IsisInteractionScopeType.REQUEST_SCOPED);

    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        IsisTransactionObject txObject = (IsisTransactionObject) transaction;

        log.debug("doBegin {}", definition);
        eventBusService.post(new TransactionBeforeBeginEvent(txObject));

        val tx = transactionManagerJdo().beginTransaction();
        txObject.setCurrentTransaction(tx);
        IsisTransactionAspectSupport.putTransactionObject(txObject);

        eventBusService.post(new TransactionAfterBeginEvent(txObject));
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        IsisTransactionObject txObject = (IsisTransactionObject) status.getTransaction();

        log.debug("doCommit {}", status);
        eventBusService.post(new TransactionBeforeCommitEvent(txObject));

        transactionManagerJdo().commitTransaction(txObject);

        eventBusService.post(new TransactionAfterCommitEvent(txObject));

        cleanUp(txObject);
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        IsisTransactionObject txObject = (IsisTransactionObject) status.getTransaction();

        log.debug("doRollback {}", status);
        eventBusService.post(new TransactionBeforeRollbackEvent(txObject));

        transactionManagerJdo().abortTransaction(txObject);

        eventBusService.post(new TransactionAfterRollbackEvent(txObject));

        cleanUp(txObject);
    }

    // -- HELPER
    
    private void cleanUp(IsisTransactionObject txObject) {
        txObject.getCountDownLatch().countDown();
        txObject.setCurrentTransaction(null);
        if(txObject.getIsisInteractionScopeType() == IsisInteractionScopeType.TEST_SCOPED) {
            isisInteractionFactory.closeSessionStack();
        }
        IsisTransactionAspectSupport.clearTransactionObject();
    }
    
    private IsisTransactionManagerJdo transactionManagerJdo() {
        return isisInteractionTracker.currentInteractionSession()
                .map(interaction->interaction.getAttribute(IsisPersistenceSessionJdo.class))
                .map(IsisPersistenceSessionJdoBase.class::cast)
                .map(ps->ps.transactionManager)
                .orElseThrow(()->_Exceptions.unrecoverable("no current IsisPersistenceSessionJdoBase available"));
    }

}
