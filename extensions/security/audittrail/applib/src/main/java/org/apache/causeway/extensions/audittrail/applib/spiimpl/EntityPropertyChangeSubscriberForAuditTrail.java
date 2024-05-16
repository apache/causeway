/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.causeway.extensions.audittrail.applib.spiimpl;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.commons.collections.Can;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.causeway.applib.services.publishing.spi.EntityPropertyChangeSubscriber;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.audittrail.applib.CausewayModuleExtAuditTrailApplib;
import org.apache.causeway.extensions.audittrail.applib.dom.AuditTrailEntry;
import org.apache.causeway.extensions.audittrail.applib.dom.AuditTrailEntryRepository;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of the Causeway {@link EntityPropertyChangeSubscriber} creates a log
 * entry to the database (the {@link AuditTrailEntry} entity) each time a
 * user either logs on or logs out, or if their session expires.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(EntityPropertyChangeSubscriberForAuditTrail.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
//@Log4j2
public class EntityPropertyChangeSubscriberForAuditTrail implements EntityPropertyChangeSubscriber {

    static final String LOGICAL_TYPE_NAME = CausewayModuleExtAuditTrailApplib.NAMESPACE + ".EntityPropertyChangeSubscriberForAuditTrail";

    final TransactionService transactionService;
    final AuditTrailEntryRepository auditTrailEntryRepository;
    final CausewayConfiguration causewayConfiguration;

    @Override
    public boolean isEnabled() {
        return causewayConfiguration.getExtensions().getAuditTrail().getPersist().isEnabled();
    }

    @Override
    public void onChanging(final EntityPropertyChange entityPropertyChange) {
        if (!isEnabled()) {
            return;
        }
        auditTrailEntryRepository.createFor(entityPropertyChange);
    }

    @Override
    public void onBulkChanging(Can<EntityPropertyChange> entityPropertyChanges) {
        if (!isEnabled()) {
            return;
        }
        auditTrailEntryRepository.createForBulk(entityPropertyChanges);
    }

}
