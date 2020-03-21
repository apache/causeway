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
package org.apache.isis.core.runtime.persistence.transaction;

import java.util.concurrent.CountDownLatch;

import org.springframework.transaction.support.SmartTransactionObject;

import org.apache.isis.applib.services.xactn.Transaction;
import org.apache.isis.applib.services.xactn.TransactionId;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.val;

@ToString
public class IsisTransactionObject implements SmartTransactionObject {
    
    public static enum IsisInteractionScopeType {
        /** an IsisInteraction was already present when creating this txObj */  
        REQUEST_SCOPED,
        /** an IsisInteraction was auto-created when creating this txObj, 
         * so we need to take core of closing it; most likely in the context of testing */
        TEST_SCOPED
    }

    public static IsisTransactionObject of(Transaction currentTransaction, IsisInteractionScopeType isisInteractionScopeType) {
        val txObject = new IsisTransactionObject();
        txObject.setCurrentTransaction(currentTransaction);
        txObject.setIsisInteractionScopeType(isisInteractionScopeType);
        return txObject;
    }

    @Getter @Setter Transaction currentTransaction;
    @Getter @Setter IsisInteractionScopeType isisInteractionScopeType;


    @Override
    public boolean isRollbackOnly() {
        return currentTransaction.getTransactionState().mustAbort();
    }

    @Override
    public void flush() {
        if(currentTransaction!=null) {
            currentTransaction.flush();
        }
    }

    public TransactionId getTransactionId() {
        if(currentTransaction!=null) {
            return currentTransaction.getId();
        }
        return null;
    }	

    // -- RESET

    public void clear() {
        transactionNestingLevel = 0;
        setCurrentTransaction(null);
    }

    // -- THREAD SYNCHRONICATION

    /**
     * A latch that allows threads to wait on. The latch count drops to zero once 
     * this transaction completes.
     */
    @Getter private final CountDownLatch countDownLatch = new CountDownLatch(1);	

    // -- NESTING

    @Getter private int transactionNestingLevel = 0;

    public int incTransactionNestingLevel() {
        return ++transactionNestingLevel;
    }

    public int decTransactionNestingLevel() {
        if(transactionNestingLevel==0) {
            return 0;
        }
        return --transactionNestingLevel;
    }

    public boolean isTopLevel() {
        return transactionNestingLevel == 0;
    }

}
