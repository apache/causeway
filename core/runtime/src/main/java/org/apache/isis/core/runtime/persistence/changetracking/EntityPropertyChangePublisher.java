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
package org.apache.isis.core.runtime.persistence.changetracking;

import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.isis.applib.services.publishing.spi.EntityPropertyChangeSubscriber;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.having.HasEnabling;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Notifies {@link org.apache.isis.applib.services.publishing.spi.EntityPropertyChangeSubscriber}s.
 */
@Service
@Named("isisRuntime.EntityPropertyChangePublisher")
@Order(OrderPrecedence.EARLY)
@Primary
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class EntityPropertyChangePublisher {
    
    private final List<EntityPropertyChangeSubscriber> subscribers;
    private final UserService userService;
    private final ClockService clockService;
    private final TransactionService transactionService;
    
    private Can<EntityPropertyChangeSubscriber> enabledSubscribers;
    
    @PostConstruct
    public void init() {
        enabledSubscribers = Can.ofCollection(subscribers)
                .filter(HasEnabling::isEnabled);
    }

    public void dispatchEntityAudits(final HasEnlistedEntityPropertyChangeRecords hasEnlistedForAuditing) {
        if(!canDispatch()) { 
            return; 
        }
        
        val currentUser = userService.getUser().getName();
        val currentTime = clockService.nowAsJavaSqlTimestamp();
        val propertyChangeRecords = hasEnlistedForAuditing.getPropertyChangeRecords();
    
        log.debug("about to process {} property changes", ()->propertyChangeRecords.size());
        
        for (val propertyChangeRecord : propertyChangeRecords) {
            publishChangedProperty(currentTime, currentUser, propertyChangeRecord);
        }
    }

    // -- HELPER
    
    private boolean canDispatch() {
        return enabledSubscribers.isNotEmpty();
    }
    
    private void publishChangedProperty(
            final java.sql.Timestamp timestamp,
            final String user,
            final PropertyChangeRecord propertyChangeRecord) {

        val adapterAndProperty = propertyChangeRecord.getAdapterAndProperty();
        val spec = adapterAndProperty.getAdapter().getSpecification();

        final Bookmark target = adapterAndProperty.getBookmark();
        final String propertyId = adapterAndProperty.getPropertyId();
        final String memberId = adapterAndProperty.getMemberId();

        final PreAndPostValues papv = propertyChangeRecord.getPreAndPostValues();
        final String preValue = papv.getPreString();
        final String postValue = papv.getPostString();

        final String targetClass = CommandUtil.targetClassNameFor(spec);

        val txId = transactionService.currentTransactionId();

        final UUID transactionId = txId.getUniqueId();
        final int sequence = txId.getSequence();

        for (val subscriber : enabledSubscribers) {
            subscriber.onChanging(
                    EntityPropertyChange
                        .of(transactionId, sequence, targetClass, target, 
                                memberId, propertyId, preValue, postValue, user, timestamp));
        }
    }



}
