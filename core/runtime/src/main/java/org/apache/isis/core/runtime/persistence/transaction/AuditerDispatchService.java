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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.audit.AuditerService;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.facets.object.audit.AuditableFacet;

import lombok.val;

/**
 * Wrapper around {@link org.apache.isis.applib.services.audit.AuditerService}.
 */
@Service
@Named("isisRuntime.AuditerDispatchService")
@Order(OrderPrecedence.EARLY)
@Primary
@Qualifier("Default")
public class AuditerDispatchService {
    
    @Inject private List<AuditerService> auditerServices;
    @Inject private ChangedObjectsService changedObjectsService;
    @Inject private UserService userService;
    @Inject private ClockService clockService;
    @Inject private TransactionService transactionService;
    
    private Can<AuditerService> enabledAuditerServices;
    
    @PostConstruct
    public void init() {
        enabledAuditerServices = Can.ofCollection(auditerServices)
                .filter(AuditerService::isEnabled);
    }

    private boolean canAudit() {
        return enabledAuditerServices.isNotEmpty();
    }

    public void audit() {
        if(!canAudit()) {
            return;
        }
        final Set<Map.Entry<AdapterAndProperty, PreAndPostValues>> changedObjectProperties =
                changedObjectsService.getChangedObjectProperties();

        final String currentUser = userService.getUser().getName();
        final java.sql.Timestamp currentTime = clockService.nowAsJavaSqlTimestamp();

        for (Map.Entry<AdapterAndProperty, PreAndPostValues> auditEntry : changedObjectProperties) {
            auditChangedProperty(currentTime, currentUser, auditEntry);
        }
    }

    private void auditChangedProperty(
            final java.sql.Timestamp timestamp,
            final String user,
            final Map.Entry<AdapterAndProperty, PreAndPostValues> auditEntry) {

        final AdapterAndProperty aap = auditEntry.getKey();
        final ObjectAdapter adapter = aap.getAdapter();

        final AuditableFacet auditableFacet = adapter.getSpecification().getFacet(AuditableFacet.class);
        if(auditableFacet == null || auditableFacet.isDisabled()) {
            return;
        }

        final Bookmark target = aap.getBookmark();
        final String propertyId = aap.getPropertyId();
        final String memberId = aap.getMemberId();

        final PreAndPostValues papv = auditEntry.getValue();
        final String preValue = papv.getPreString();
        final String postValue = papv.getPostString();

        final String targetClass = CommandUtil.targetClassNameFor(adapter);

        val txId = transactionService.currentTransactionId();

        final UUID transactionId = txId.getUniqueId();
        final int sequence = txId.getSequence();

        for (val auditerService : enabledAuditerServices) {
            auditerService
            .audit(transactionId, sequence, targetClass, target, memberId, propertyId, preValue, postValue, user, timestamp);
        }
    }



}
