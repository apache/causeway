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

package org.apache.isis.core.runtimeservices.xactn;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.xactn.TransactionId;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.commons.functional.Result;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.runtime.persistence.transaction.IsisTransactionAspectSupport;
import org.apache.isis.core.runtime.persistence.transaction.IsisTransactionObject;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named("isisRuntimeServices.TransactionServiceSpring")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Spring")
@Log4j2
public class TransactionServiceSpring implements TransactionService {

    private final PlatformTransactionManager platformTransactionManager;

    // single TransactionTemplate shared amongst all methods in this instance
    private final TransactionTemplate transactionTemplate;

    @Inject
    public TransactionServiceSpring(PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
        this.transactionTemplate = new TransactionTemplate(platformTransactionManager);
    }

    @Override
    public void flushTransaction() {

        val txObject = currentTransactionObject(WarnIfNonePolicy.IGNORE);

        if(txObject==null) {
            return;
        }

        log.debug("about to flush tx");
        txObject.flush();
    }

    @Override
    public TransactionId currentTransactionId() {

        val txObject = currentTransactionObject(WarnIfNonePolicy.IGNORE);

        if(txObject==null) {
            return null;
        }

        log.debug("about to get current tx-id");
        return txObject.getTransactionId();
    }

    @Override @Nonnull
    public TransactionState currentTransactionState() {

        val txObject = currentTransactionObject(WarnIfNonePolicy.IGNORE);

        if(txObject==null || txObject.getCurrentTransaction()==null) {
            return TransactionState.NONE;
        }

        val state = txObject.getCurrentTransaction().getTransactionState();
        return state != null
                ? state
                        : TransactionState.NONE;
    }

    @Override
    public void nextTransaction() {
        val status = platformTransactionManager.getTransaction(transactionTemplate);
        if(status.isCompleted()) {
            return;
        }
        if(status.isRollbackOnly()) {
            platformTransactionManager.rollback(status);
        } else {
            platformTransactionManager.commit(status);
        }
        // begins a new transaction
        platformTransactionManager.getTransaction(transactionTemplate);
    }

    @Override
    public <T> Result<T> executeWithinTransaction(final @NonNull Callable<T> callable) {

        if(currentTransactionState() != TransactionState.NONE) {
            Result.of(()->{
                val t = callable.call();
                flushTransaction();
                return t;
            });
        }

        return Result.of(()->executeWithinNewTransaction(callable));
    }

    // -- HELPER

    enum WarnIfNonePolicy {
        IGNORE,
        LOG
    }

    private <T> T executeWithinNewTransaction(Callable<T> callable) {

        return transactionTemplate.execute(new TransactionCallback<T>() {
            // the code in this method executes in a transactional context
            @SneakyThrows
            @Override
            public T doInTransaction(TransactionStatus status) {
                return callable.call();
            }
        });
    }
    
    private IsisTransactionObject currentTransactionObject(WarnIfNonePolicy warnIfNonePolicy) {

        val txObject = IsisTransactionAspectSupport.currentTransactionObject().orElse(null);

        if(txObject==null) {
            if(warnIfNonePolicy == WarnIfNonePolicy.LOG) {
                log.warn("no current txStatus present");
                _Exceptions.dumpStackTrace(System.out, 0, 1000);
            }
            return null;
        }

        return txObject;

    }




}
