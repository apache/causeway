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
package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.spi;

import java.util.Map.Entry;
import java.util.Set;

import org.apache.isis.applib.clock.Clock;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.runtimes.dflt.objectstores.jdo.applib.AuditService;
import org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.facets.object.auditable.AuditableFacet;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.TransactionalResource;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransaction;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.MessageBroker;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.UpdateNotifier;

public class DataNucleusTransaction extends IsisTransaction {

    private final AuditService auditService;
    
    public DataNucleusTransaction(
                final IsisTransactionManager transactionManager, 
                final MessageBroker messageBroker, 
                final UpdateNotifier updateNotifier, 
                final TransactionalResource objectStore, 
                final AuditService auditService) {
        super(transactionManager, messageBroker, updateNotifier, objectStore);
        this.auditService = auditService;
    }

    @Override
    protected void doAudit(final Set<Entry<AdapterAndProperty, PreAndPostValues>> auditEntries) {
        if(auditService == null) {
            super.doAudit(auditEntries);
            return;
        }
        final String currentUser = getAuthenticationSession().getUserName();
        final long currentTimestampEpoch = currentTimestampEpoch();
        for (Entry<AdapterAndProperty, PreAndPostValues> auditEntry : auditEntries) {
            audit(currentUser, currentTimestampEpoch, auditEntry);
        }
    }

    private long currentTimestampEpoch() {
        return Clock.getTime();
    }

    private void audit(final String currentUser, final long currentTimestampEpoch, final Entry<AdapterAndProperty, PreAndPostValues> auditEntry) {
        final AdapterAndProperty aap = auditEntry.getKey();
        final ObjectAdapter adapter = aap.getAdapter();
        if(!adapter.getSpecification().containsFacet(AuditableFacet.class)) {
            return;
        }
        final RootOid oid = (RootOid) adapter.getOid();
        final String objectType = oid.getObjectSpecId().asString();
        final String identifier = oid.getIdentifier();
        final PreAndPostValues papv = auditEntry.getValue();
        final String preValue = asString(papv.getPre());
        final String postValue = asString(papv.getPost());
        auditService.audit(currentUser, currentTimestampEpoch, objectType, identifier, preValue, postValue);
    }

    private static String asString(Object object) {
        return object != null? object.toString(): null;
    }


    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

}
