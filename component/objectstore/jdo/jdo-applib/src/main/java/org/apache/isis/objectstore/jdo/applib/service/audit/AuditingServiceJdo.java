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
package org.apache.isis.objectstore.jdo.applib.service.audit;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.audit.AuditingService3;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.interaction.Interaction;
import org.apache.isis.applib.services.interaction.InteractionContext;

public class AuditingServiceJdo extends AbstractFactoryAndRepository implements AuditingService3 {

    @Programmatic
    public void audit(java.sql.Timestamp timestamp, String user, Bookmark target, String propertyId, String preValue, String postValue) {
        AuditEntryJdo auditEntry = newTransientInstance(AuditEntryJdo.class);
        auditEntry.setTimestamp(timestamp);
        auditEntry.setUser(user);
        Interaction interaction = this.interactionContext.getInteraction();
        if(interaction instanceof HasTransactionId) {
            HasTransactionId hasTransactionId = (HasTransactionId) interaction;
            auditEntry.setTransactionId(hasTransactionId.getTransactionId());
        }
        auditEntry.setTarget(target);
        auditEntry.setPropertyId(propertyId);
        auditEntry.setPreValue(preValue);
        auditEntry.setPostValue(postValue);
        persistIfNotAlready(auditEntry);
    }

    
    // //////////////////////////////////////

    
    @javax.inject.Inject
    private InteractionContext interactionContext;
    
}
