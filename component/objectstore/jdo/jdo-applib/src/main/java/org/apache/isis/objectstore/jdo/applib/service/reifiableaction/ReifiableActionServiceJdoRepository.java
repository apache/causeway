/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.objectstore.jdo.applib.service.reifiableaction;

import java.util.List;
import java.util.UUID;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.reifiableaction.ReifiableAction;
import org.apache.isis.applib.services.reifiableaction.ReifiableActionContext;


public class ReifiableActionServiceJdoRepository extends AbstractFactoryAndRepository {

    @Programmatic
    public ReifiableActionJdo findByTransactionId(final UUID transactionId) {
        persistCurrentReifiableActionIfRequired();
        return firstMatch(
                new QueryDefault<ReifiableActionJdo>(ReifiableActionJdo.class, 
                        "findByTransactionId", 
                        "transactionId", transactionId.toString()));
    }

    @Programmatic
    public List<ReifiableActionJdo> findCurrent() {
        persistCurrentReifiableActionIfRequired();
        return allMatches(
                new QueryDefault<ReifiableActionJdo>(ReifiableActionJdo.class, "findCurrent"));
    }
    
    @Programmatic
    public List<ReifiableActionJdo> findCompleted() {
        persistCurrentReifiableActionIfRequired();
        return allMatches(
                new QueryDefault<ReifiableActionJdo>(ReifiableActionJdo.class, "findCompleted"));
    }

    private void persistCurrentReifiableActionIfRequired() {
        if(reifiableActionContext == null || reifiableActionService == null) {
            return;
        } 
        final ReifiableAction reifiableAction = reifiableActionContext.getReifiableAction();
        final ReifiableActionJdo reifiableActionJdo = reifiableActionService.asUserInitiatedReifiedReifiableActionJdo(reifiableAction);
        if(reifiableActionJdo == null) {
            return;
        } 
        persistIfNotAlready(reifiableActionJdo);
    }

    // //////////////////////////////////////

    
    @javax.inject.Inject
    private ReifiableActionServiceJdo reifiableActionService;
    
    @javax.inject.Inject
    private ReifiableActionContext reifiableActionContext;

}
