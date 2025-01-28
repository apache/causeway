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

import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.causeway.applib.services.publishing.spi.EntityChanges;
import org.apache.causeway.applib.services.publishing.spi.EntityChangesSubscriber;
import org.apache.causeway.applib.services.user.UserService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.having.HasEnabling;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.core.transaction.changetracking.EntityChangesPublisher;
import org.apache.causeway.core.transaction.changetracking.HasEnlistedEntityChanges;

import org.jspecify.annotations.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Default implementation of {@link EntityChangesPublisher}
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".EntityChangesPublisherDefault")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class EntityChangesPublisherDefault implements EntityChangesPublisher {

    private final List<EntityChangesSubscriber> subscribers;
    private final ClockService clockService;
    private final UserService userService;
    private final InteractionLayerTracker iaTracker;

    private Can<EntityChangesSubscriber> enabledSubscribers = Can.empty();

    @PostConstruct
    public void init() {
        enabledSubscribers = Can.ofCollection(subscribers)
                .filter(HasEnabling::isEnabled);
    }

    @Override
    public void publishChangingEntities(HasEnlistedEntityChanges hasEnlistedEntityChanges) {

        var payload = getPayload(hasEnlistedEntityChanges);
        var handle = _Xray.enterEntityChangesPublishing(
                iaTracker,
                payload,
                enabledSubscribers,
                ()->getCannotPublishReason(payload));

        payload.ifPresent(entityChanges->{
            for (var subscriber : enabledSubscribers) {
                subscriber.onChanging(entityChanges);
            }
        });

        _Xray.exitPublishing(handle);
    }

    // -- HELPER

    private Optional<EntityChanges> getPayload(final @NonNull HasEnlistedEntityChanges hasEnlistedEntityChanges) {
        if (enabledSubscribers.isEmpty()) {
            return Optional.empty();
        }

        return hasEnlistedEntityChanges.getEntityChanges(
                clockService.getClock().nowAsJavaSqlTimestamp(), // current time
                userService.currentUserNameElseNobody()          // current user
        );
    }

    // x-ray support
    private @Nullable String getCannotPublishReason(final @NonNull Optional<EntityChanges> payload) {
        return enabledSubscribers.isEmpty()
                ? "no subscribers"
                : !payload.isPresent()
                        ? "no changes had been enlisted"
                        : null;
    }

}
