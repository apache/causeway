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
package org.apache.isis.core.runtimeservices.publish;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.publishing.spi.EntityPropertyChangeSubscriber;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.having.HasEnabling;
import org.apache.isis.core.runtime.persistence.changetracking.EntityPropertyChangePublisher;
import org.apache.isis.core.runtime.persistence.changetracking.HasEnlistedEntityPropertyChanges;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named("isisRuntimeServices.EntityPropertyChangePublisherDefault")
@Order(OrderPrecedence.EARLY)
@Primary
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
//@Log4j2
public class EntityPropertyChangePublisherDefault implements EntityPropertyChangePublisher {
    
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

    @Override
    public void publishChangedProperties(
            final HasEnlistedEntityPropertyChanges hasEnlistedEntityPropertyChanges) {
        
        if(!canPublish()) { 
            return; 
        }
        
        val currentTime = clockService.getClock().javaSqlTimestamp();
        val currentUser = userService.currentUserNameElseNobody();
        val currentTransactionId = transactionService.currentTransactionId();
        
        hasEnlistedEntityPropertyChanges.streamPropertyChanges(
                currentTime, 
                currentUser,
                currentTransactionId)
        .forEach(propertyChange->{
            for (val subscriber : enabledSubscribers) {
                subscriber.onChanging(propertyChange);
            }
        });
    }

    // -- HELPER
    
    private boolean canPublish() {
        return enabledSubscribers.isNotEmpty();
    }


}
