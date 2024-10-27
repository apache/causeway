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
package org.apache.causeway.core.runtimeservices.publish;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.causeway.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.causeway.applib.services.publishing.spi.EntityPropertyChangeSubscriber;
import org.apache.causeway.applib.services.user.UserService;
import org.apache.causeway.applib.services.xactn.TransactionId;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.having.HasEnabling;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.services.objectlifecycle.HasEnlistedEntityPropertyChanges;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.core.security.util.XrayUtil;
import org.apache.causeway.core.transaction.changetracking.EntityPropertyChangePublisher;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Default implementation of {@link EntityPropertyChangePublisher}.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".EntityPropertyChangePublisherDefault")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class EntityPropertyChangePublisherDefault implements EntityPropertyChangePublisher {

    private final List<EntityPropertyChangeSubscriber> subscribers;
    private final UserService userService;
    private final ClockService clockService;
    private final TransactionService transactionService;
    private final InteractionLayerTracker iaTracker;
    private final Provider<HasEnlistedEntityPropertyChanges> hasEnlistedEntityPropertyChangesProvider;
    private final CausewayConfiguration causewayConfiguration;

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

        var currentTime = clockService.getClock().nowAsJavaSqlTimestamp();
        var currentUser = userService.currentUserNameElseNobody();
        var currentTransactionId = transactionService.currentTransactionId().orElse(TransactionId.empty());

        var enlistedPropertyChanges = hasEnlistedEntityPropertyChanges().getPropertyChanges(
                currentTime,
                currentUser,
                currentTransactionId);
        var duplicates = new ArrayList<EntityPropertyChange>();
        var uniquePropertyChanges = enlistedPropertyChanges
                .toSet(duplicates::add) // ensure uniqueness
                .stream()
                .collect(Can.toCan());
        if(log.isWarnEnabled() && ! duplicates.isEmpty()) {
            log.warn("Duplicate enlisted property changes discovered\n{}", duplicates);
        }

        XrayUtil.SequenceHandle xrayHandle = null;
        try {
            xrayHandle = _Xray.enterEntityPropertyChangePublishing(
                    iaTracker,
                    uniquePropertyChanges,
                    enabledSubscribers,
                    () -> getCannotPublishReason(uniquePropertyChanges)
            );

            if (uniquePropertyChanges.size() <= causewayConfiguration.getCore().getRuntimeServices().getEntityPropertyChangePublisher().getBulk().getThreshold()) {
                uniquePropertyChanges.forEach(propertyChange -> {
                    for (var subscriber : enabledSubscribers) {
                        subscriber.onChanging(propertyChange);
                    }
                });
            } else {
                for (var subscriber : enabledSubscribers) {
                    subscriber.onChanging(uniquePropertyChanges);
                }
            }
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
