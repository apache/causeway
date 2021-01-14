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
package org.apache.isis.core.runtimeservices.transaction;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.xactn.TransactionId;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.functional.Result;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.interaction.session.InteractionTracker;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named("isisRuntimeServices.TransactionServiceSpring")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Spring")
@Log4j2
public class TransactionServiceSpring implements TransactionService {

    private final Can<PlatformTransactionManager> platformTransactionManagers;
    private final InteractionTracker interactionTracker;

    @Inject
    public TransactionServiceSpring(
            final List<PlatformTransactionManager> platformTransactionManagers,
            final InteractionTracker interactionTracker) {
        
        this.platformTransactionManagers = Can.ofCollection(platformTransactionManagers);
        log.info("PlatformTransactionManagers: {}", platformTransactionManagers);
        
        this.interactionTracker = interactionTracker;
    }

    // -- SPRING INTEGRATION
    
    @Override
    public <T> Result<T> callTransactional(TransactionDefinition def, Callable<T> callable) {

        val txManager = transactionManagerForElseFail(def);
        
        val tx = txManager.getTransaction(def);

        val result = Result.ofNullable(callable);
        
        if(result.isFailure()) {
            txManager.rollback(tx);
        } else {
            txManager.commit(tx);
        }

        return result;
    }
    
    @Override
    public void nextTransaction() {
        
        val txManager = singletonTransactionManagerElseFail(); 
        
        val txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        // either reuse existing or create new
        val txStatus = txManager.getTransaction(txTemplate);
        if(txStatus.isNewTransaction()) {
            // we have created a new transaction, so we are done
            return;
        }
        // we are reusing an exiting transaction, so end it and create a new one afterwards
        if(txStatus.isRollbackOnly()) {
            txManager.rollback(txStatus);
        } else {
            txManager.commit(txStatus);
        }
        
        // begin a new transaction
        txManager.getTransaction(txTemplate);
    }
    
    @Override
    public void flushTransaction() {
        log.debug("about to flush tx");
        currentTransactionStatus()
            .ifPresent(TransactionStatus::flush);
    }

    @Override
    public Optional<TransactionId> currentTransactionId() {
        return interactionTracker.getConversationId()
                .map(uuid->TransactionId.of(uuid, 0, "")); //TODO track tx completion-counts 
    }

    @Override
    public TransactionState currentTransactionState() {

        return currentTransactionStatus()
        .map(txStatus->{
        
            if(txStatus.isCompleted()) {
                return txStatus.isRollbackOnly()
                        ? TransactionState.ABORTED
                        : TransactionState.COMMITTED;
            }
            
            return txStatus.isRollbackOnly()
                    ? TransactionState.MUST_ABORT
                    : TransactionState.IN_PROGRESS;
            
        })
        .orElse(TransactionState.NONE);
    }
    
    // -- HELPER
    
    private PlatformTransactionManager transactionManagerForElseFail(TransactionDefinition def) {
        if(def instanceof TransactionTemplate) {
            val txManager = ((TransactionTemplate)def).getTransactionManager();
            if(txManager!=null) {
                return txManager;
            }
        }
        return platformTransactionManagers.getSingleton()
                .orElseThrow(()->
                    platformTransactionManagers.getCardinality().isMultiple()
                        ? _Exceptions.illegalState("Multiple PlatformTransactionManagers are configured, "
                                + "make sure a PlatformTransactionManager is provided via the TransactionTemplate argument.")
                        : _Exceptions.illegalState("Needs a PlatformTransactionManager."));
    }
    
    private PlatformTransactionManager singletonTransactionManagerElseFail() {
        return platformTransactionManagers.getSingleton()
                .orElseThrow(()->
                    platformTransactionManagers.getCardinality().isMultiple()
                        ? _Exceptions.illegalState("Multiple PlatformTransactionManagers are configured, "
                                + "cannot reason about which one to use.")
                        : _Exceptions.illegalState("Needs a PlatformTransactionManager."));
    }

    private Optional<TransactionStatus> currentTransactionStatus() {
        
        val txManager = singletonTransactionManagerElseFail();
        val txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_MANDATORY);

        // not strictly required, but to prevent stack-trace creation later on
        if(!TransactionSynchronizationManager.isActualTransactionActive()) {
            return Optional.empty();
        }
        
        // get current transaction else throw an exception
        return Result.of(()->
                //XXX creating stack-traces is expensive
                txManager.getTransaction(txTemplate))
                .value();
        
    }


}
