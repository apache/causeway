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
package org.apache.isis.core.transaction.manager;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.transaction.events.TransactionBeginEvent;
import org.apache.isis.core.transaction.events.TransactionEndedEvent;
import org.apache.isis.core.transaction.events.TransactionEndingEvent;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class ApplicationLayerAwareTransactionManager 
implements PlatformTransactionManager {

    private final PlatformTransactionManager txManager;
    private final EventBusService eventBusService;
    
    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
       val txStatus = txManager.getTransaction(definition);
       if(txStatus.isNewTransaction()) {
           eventBusService.post(new TransactionBeginEvent(txStatus));
       }
       return txStatus; 
    }

    @Override
    public void commit(TransactionStatus txStatus) throws TransactionException {
        eventBusService.post(new TransactionEndingEvent(txStatus));
        txManager.commit(txStatus);
        eventBusService.post(new TransactionEndedEvent(txStatus));
    }

    @Override
    public void rollback(TransactionStatus txStatus) throws TransactionException {
        eventBusService.post(new TransactionEndingEvent(txStatus));
        txManager.rollback(txStatus);
        eventBusService.post(new TransactionEndedEvent(txStatus));
    }

}
