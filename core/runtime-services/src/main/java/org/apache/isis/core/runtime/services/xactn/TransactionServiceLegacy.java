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

import java.util.function.Supplier;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.xactn.Transaction;
import org.apache.isis.applib.services.xactn.TransactionId;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManagerJdoInternal;
import org.apache.isis.metamodel.services.persistsession.PersistenceSessionServiceInternal;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName = "of")
class TransactionServiceLegacy {

    void flushTransaction() {
        persistenceSessionServiceInternal.flush();
    }

    
    void nextTransaction(TransactionService.Policy policy, final Command commandIfAny) {
        final TransactionState transactionState = currentTransactionState();
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

	
    void executeWithinTransaction(Runnable task) {
        isisTransactionManager().executeWithinTransaction(task);
    }

    
    <T> T executeWithinTransaction(Supplier<T> task) {
        return isisTransactionManager().executeWithinTransaction(task);
    }
    
    IsisTransactionManagerJdoInternal isisTransactionManager() {
        return IsisContext.getTransactionManagerJdo().get();
    }
    
    
    TransactionId currentTransactionId() {
    	val tx = currentTransaction();
    	return tx!=null ? tx.getId() : null;
    }

    
    TransactionState currentTransactionState() {
        return persistenceSessionServiceInternal.getTransactionState();
    }
    
    // -- HELPER
    
    private Transaction currentTransaction() {
		return persistenceSessionServiceInternal.currentTransaction();
	}

    @NonNull
    private final PersistenceSessionServiceInternal persistenceSessionServiceInternal;


}
