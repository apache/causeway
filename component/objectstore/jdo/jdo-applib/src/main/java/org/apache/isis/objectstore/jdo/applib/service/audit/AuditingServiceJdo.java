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

import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.audit.AuditingService2;

@Named("Auditing")
public class AuditingServiceJdo extends AbstractFactoryAndRepository implements AuditingService2 {
    
    @ActionSemantics(Of.SAFE)
    public List<AuditEntry> list() {
        return allInstances(AuditEntry.class);
    }

    /**
     * This method will never be called by Isis because the service implements, instead, {@link AuditingService2}.
     * 
     * @deprecated
     */
    @Deprecated
    @Programmatic
    public void audit(String user, long currentTimestampEpoch, String objectType, String identifier, String preValue, String postValue) {
        audit(user, currentTimestampEpoch, objectType, identifier, null, preValue, postValue);
    }

    @Programmatic
    @Override
    public void audit(String user, long currentTimestampEpoch, String objectType, String identifier, String propertyId, String preValue, String postValue) {
        AuditEntry auditEntry = newTransientInstance(AuditEntry.class);
        auditEntry.setTimestampEpoch(currentTimestampEpoch);
        auditEntry.setUser(user);
        auditEntry.setObjectType(objectType);
        auditEntry.setPropertyId(propertyId);
        auditEntry.setIdentifier(identifier);
        auditEntry.setPreValue(preValue);
        auditEntry.setPostValue(postValue);
        persist(auditEntry);
    }

}
