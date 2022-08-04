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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.isis.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.isis.applib.services.publishing.spi.EntityPropertyChangeSubscriber;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.services.xactn.TransactionId;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.having.HasEnabling;
import org.apache.isis.core.metamodel.services.objectlifecycle.HasEnlistedEntityPropertyChanges;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;
import org.apache.isis.core.security.util.XrayUtil;
import org.apache.isis.core.transaction.changetracking.EntityPropertyChangePublisher;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.springframework.lang.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import java.util.List;

@Service
@Named(IsisModuleCoreRuntimeServices.NAMESPACE + ".EntityPropertyChangePublisherDefault")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
//@Log4j2
public class EntityPropertyChangePublisherDefault implements EntityPropertyChangePublisher {

    private final List<EntityPropertyChangeSubscriber> subscribers;
    private final UserService userService;
    private final ClockService clockService;
    private final TransactionService transactionService;
    private final InteractionLayerTracker iaTracker;
    private final Provider<HasEnlistedEntityPropertyChanges> hasEnlistedEntityPropertyChangesProvider;

    private Can<EntityPropertyChangeSubscriber> enabledSubscribers = Can.empty();

    @PostConstruct
    public void init() {
        enabledSubscribers = Can.ofCollection(subscribers)
                .filter(HasEnabling::isEnabled);
    }

    private HasEnlistedEntityPropertyChanges hasEnlistedEntityPropertyChanges() {
        return hasEnlistedEntityPropertyChangesProvider.get();
    }

    @Override
    public void publishChangedProperties() {

        transactionService.flushTransaction();

        if(enabledSubscribers.isEmpty()) {
            return;
        }

        val currentTime = clockService.getClock().nowAsJavaSqlTimestamp();
        val currentUser = userService.currentUserNameElseNobody();
        val currentTransactionId = transactionService.currentTransactionId().orElse(TransactionId.empty());

        val propertyChanges = hasEnlistedEntityPropertyChanges().getPropertyChanges(
                currentTime,
                currentUser,
                currentTransactionId);

        XrayUtil.SequenceHandle xrayHandle = null;
        try {
            xrayHandle = _Xray.enterEntityPropertyChangePublishing(
                    iaTracker,
                    propertyChanges,
                    enabledSubscribers,
                    () -> getCannotPublishReason(propertyChanges)
            );

            propertyChanges.forEach(propertyChange->{
                for (val subscriber : enabledSubscribers) {
                    subscriber.onChanging(propertyChange);
                }
            });
        } finally {
            _Xray.exitPublishing(xrayHandle);
        }
    }


    // -- HELPER

    // x-ray support
    private @Nullable String getCannotPublishReason(final @NonNull Can<EntityPropertyChange> payload) {
        return enabledSubscribers.isEmpty()
                ? "no subscribers"
                : payload.isEmpty()
                        ? "no changes had been enlisted"
                        : null;
    }

}
