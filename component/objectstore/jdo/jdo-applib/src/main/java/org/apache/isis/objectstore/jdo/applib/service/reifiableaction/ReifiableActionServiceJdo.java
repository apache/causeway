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

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.services.reifiableaction.ReifiableAction;
import org.apache.isis.applib.services.reifiableaction.spi.ReifiableActionService;

public class ReifiableActionServiceJdo extends AbstractService implements ReifiableActionService {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(ReifiableActionServiceJdo.class);

    /**
     * Creates an {@link ReifiableActionJdo}, initializing its 
     * {@link ReifiableAction#setNature(ReifiableAction.Nature) nature} to be
     * {@link ReifiableAction.Nature#OTHER rendering}.
     */
    @Programmatic
    @Override
    public ReifiableAction create() {
        ReifiableActionJdo reifiableAction = newTransientInstance(ReifiableActionJdo.class);
        reifiableAction.setNature(ReifiableAction.Nature.OTHER);
        return reifiableAction;
    }

    @Programmatic
    @Override
    public void startTransaction(final ReifiableAction reifiableAction, final UUID transactionId) {
        if(reifiableAction instanceof ReifiableActionJdo) {
            // should be the case, since this service created the object in the #create() method
            final ReifiableActionJdo reifiableActionJdo = (ReifiableActionJdo) reifiableAction;
            final UUID currentTransactionId = reifiableActionJdo.getTransactionId();
            if(currentTransactionId != null && !currentTransactionId.equals(transactionId)) {
                // the logic in IsisTransaction means that any subsequent transactions within a given reifiable action
                // should reuse the xactnId of the first transaction created within that interaction.
                throw new IllegalStateException("Attempting to set a different transactionId on reifiable action");
            }
            reifiableActionJdo.setTransactionId(transactionId);
        }
    }

    @Programmatic
    @Override
    public void complete(final ReifiableAction reifiableAction) {
        ReifiableActionJdo reifiableActionJdo = asUserInitiatedReifiedReifiableActionJdo(reifiableAction);
        if(reifiableActionJdo == null) {
            return;
        }
            
        reifiableActionJdo.setCompletedAt(Clock.getTimeAsJavaSqlTimestamp());
        persistIfNotAlready(reifiableActionJdo);
    }

    /**
     * Not API, factored out from {@link ReifiableActionServiceJdoRepository}.
     */
    ReifiableActionJdo asUserInitiatedReifiedReifiableActionJdo(final ReifiableAction reifiableAction) {
        if(!(reifiableAction instanceof ReifiableActionJdo)) {
            // ought not to be the case, since this service created the object in the #create() method
            return null;
        }
        if(reifiableAction.getNature() != ReifiableAction.Nature.USER_INITIATED) {
            return null;
        } 
        final ReifiableActionJdo reifiableActionJdo = (ReifiableActionJdo) reifiableAction;
        if(!reifiableActionJdo.isReify()) {
            return null;
        } 
        return reifiableActionJdo;
    }
    
    
    
    
}
