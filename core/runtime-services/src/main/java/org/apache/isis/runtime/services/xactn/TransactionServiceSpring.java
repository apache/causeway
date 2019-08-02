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

package org.apache.isis.runtime.services.xactn;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import org.apache.isis.applib.services.xactn.TransactionId;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.runtime.system.transaction.IsisTransactionAspectSupport;
import org.apache.isis.runtime.system.transaction.IsisTransactionObject;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Singleton
@Log4j2
public class TransactionServiceSpring implements TransactionService {

    // single TransactionTemplate shared amongst all methods in this instance
    private final TransactionTemplate transactionTemplate;

    // use constructor-injection to supply the PlatformTransactionManager
    public TransactionServiceSpring(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);

    }

    @Override
    public void flushTransaction() {

        val txObject = currentTransactionObject(false);

        if(txObject==null) {
            return;
        }

        log.debug("about to flush tx");
        txObject.flush();
    }

    @Override
    public TransactionId currentTransactionId() {

        val txObject = currentTransactionObject(false);

        if(txObject==null) {
            return null;
        }

        log.debug("about to get current tx-id");
        return txObject.getTransactionId();
    }

    @Override @Nonnull
    public TransactionState currentTransactionState() {

        val txObject = currentTransactionObject(false);

        if(txObject==null || txObject.getCurrentTransaction()==null) {
            return TransactionState.NONE;
        }

        val state = txObject.getCurrentTransaction().getTransactionState();
        return state != null
                ? state
                        : TransactionState.NONE;
    }

    @Override
    public void executeWithinTransaction(Runnable task) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            // the code in this method executes in a transactional context
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                task.run();
            }
        });
    }

    @Override
    public <T> T executeWithinTransaction(Supplier<T> task) {
        return transactionTemplate.execute(new TransactionCallback<T>() {
            // the code in this method executes in a transactional context
            @Override
            public T doInTransaction(TransactionStatus status) {
                return task.get();
            }
        });
    }

    // -- HELPER

    private IsisTransactionObject currentTransactionObject(boolean warnIfNone) {

        val txObject = IsisTransactionAspectSupport.currentTransactionObject().orElse(null);

        if(txObject==null) {
            if(warnIfNone) {
                log.warn("no current txStatus present");
                _Exceptions.dumpStackTrace(System.out, 0, 1000);
            }
            return null;
        }

        return txObject;

    }




}
