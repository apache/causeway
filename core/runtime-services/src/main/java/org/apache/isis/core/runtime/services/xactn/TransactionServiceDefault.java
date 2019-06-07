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

package org.apache.isis.core.runtime.services.xactn;

import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import javax.inject.Singleton;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.xactn.Transaction;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManagerJdoInternal;

@Singleton
public class TransactionServiceDefault implements TransactionService {


    @Override
    public void flushTransaction() {
        persistenceSessionServiceInternal.flush();
    }

    @Override
    public void nextTransaction() {
        nextTransaction((Command)null);
    }

    @Override
    public void nextTransaction(final Command commandIfAny) {
        nextTransaction(TransactionService.Policy.UNLESS_MARKED_FOR_ABORT, commandIfAny);
    }

    @Override
    public void nextTransaction(TransactionService.Policy policy) {
        nextTransaction(policy, null);
    }

    @Override
    public void nextTransaction(TransactionService.Policy policy, final Command commandIfAny) {
        final TransactionState transactionState = getTransactionState();
        switch (transactionState) {
        case NONE:
            break;
        case IN_PROGRESS:
            persistenceSessionServiceInternal.commit();
            break;
        case MUST_ABORT:
            switch (policy) {
            case UNLESS_MARKED_FOR_ABORT:
                throw new IsisException("Transaction is marked to abort");
            case ALWAYS:
                persistenceSessionServiceInternal.abortTransaction();
                final Transaction currentTransaction = currentTransaction();
                if(currentTransaction instanceof IsisTransaction) {
                    ((IsisTransaction)currentTransaction).clearAbortCause();
                }
                break;
            }
            break;
        case COMMITTED:
            break;
        case ABORTED:
            break;
        }

        persistenceSessionServiceInternal.beginTran(commandIfAny);
    }

    @Override
    public void executeWithinTransaction(Runnable task) {
        isisTransactionManager().executeWithinTransaction(task);
    }

    @Override
    public <T> T executeWithinTransaction(Supplier<T> task) {
        return isisTransactionManager().executeWithinTransaction(task);
    }
    
    IsisTransactionManagerJdoInternal isisTransactionManager() {
        return IsisContext.getTransactionManagerJdo().get();
    }
    
    @Override
    public Transaction currentTransaction() {
        return persistenceSessionServiceInternal.currentTransaction();
    }

    @Override
    public CountDownLatch currentTransactionLatch() {
        return persistenceSessionServiceInternal.currentTransactionLatch();
    }

    @Override
    public TransactionState getTransactionState() {
        return persistenceSessionServiceInternal.getTransactionState();
    }

    @javax.inject.Inject
    PersistenceSessionServiceInternal persistenceSessionServiceInternal;


}
