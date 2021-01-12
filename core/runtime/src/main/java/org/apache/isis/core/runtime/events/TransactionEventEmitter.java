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
package org.apache.isis.core.runtime.events;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.interaction.scope.InteractionScopeAware;
import org.apache.isis.core.interaction.session.InteractionSession;
import org.apache.isis.core.transaction.events.TransactionAfterCompletionEvent;
import org.apache.isis.core.transaction.events.TransactionBeforeCompletionEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TransactionEventEmitter 
implements TransactionSynchronization, InteractionScopeAware {

    private final EventBusService eventBusService;
    
    @Override
    public void beforeCompletion() {
        eventBusService.post(TransactionBeforeCompletionEvent.instance());
    }

    @Override
    public void afterCompletion(int status) {
        eventBusService.post(TransactionAfterCompletionEvent.forStatus(status));
    }
    
    @Override
    public void afterEnteringTransactionalBoundary(
            InteractionSession interactionSession, 
            boolean isSynchronizationActive) {
        if(isSynchronizationActive) {
            TransactionSynchronizationManager.registerSynchronization(this);
        }
    }

}
