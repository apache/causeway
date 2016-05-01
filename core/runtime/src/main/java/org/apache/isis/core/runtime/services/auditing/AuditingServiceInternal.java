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
package org.apache.isis.core.runtime.services.auditing;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.audit.AuditingService3;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.facets.object.audit.AuditableFacet;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;

/**
 * Wrapper around {@link org.apache.isis.applib.services.audit.AuditingService3}.  Is a no-op if there is no injected service.
 */
@DomainService(nature = NatureOfService.DOMAIN)
public class AuditingServiceInternal {


    @Programmatic
    public boolean canAudit() {
        return auditingServiceIfAny != null;
    }

    @Programmatic
    public void audit(
            final Set<Map.Entry<IsisTransaction.AdapterAndProperty, IsisTransaction.PreAndPostValues>> changedObjectProperties) {

        if(!canAudit()) {
            return;
        }

        final String currentUser = userService.getUser().getName();
        final java.sql.Timestamp currentTime = clockService.nowAsJavaSqlTimestamp();

        for (Map.Entry<IsisTransaction.AdapterAndProperty, IsisTransaction.PreAndPostValues> auditEntry : changedObjectProperties) {
            auditChangedProperty(currentTime, currentUser, auditEntry);
        }

    }

    private void auditChangedProperty(
            final java.sql.Timestamp timestamp,
            final String user,
            final Map.Entry<IsisTransaction.AdapterAndProperty, IsisTransaction.PreAndPostValues> auditEntry) {

        final IsisTransaction.AdapterAndProperty aap = auditEntry.getKey();
        final ObjectAdapter adapter = aap.getAdapter();

        final AuditableFacet auditableFacet = adapter.getSpecification().getFacet(AuditableFacet.class);
        if(auditableFacet == null || auditableFacet.isDisabled()) {
            return;
        }

        final Bookmark target = aap.getBookmark();
        final String propertyId = aap.getPropertyId();
        final String memberId = aap.getMemberId();

        final IsisTransaction.PreAndPostValues papv = auditEntry.getValue();
        final String preValue = papv.getPreString();
        final String postValue = papv.getPostString();


        final String targetClass = CommandUtil.targetClassNameFor(adapter);

        final Command command = commandContext.getCommand();
        final UUID transactionId = command.getTransactionId();

        auditingServiceIfAny
                .audit(transactionId, targetClass, target, memberId, propertyId, preValue, postValue, user, timestamp);
    }

    /**
     * could be null if none has been registered.
     */
    @Inject
    private AuditingService3 auditingServiceIfAny;

    @Inject
    UserService userService;

    @Inject
    ClockService clockService;

    @Inject
    CommandContext commandContext;


}
