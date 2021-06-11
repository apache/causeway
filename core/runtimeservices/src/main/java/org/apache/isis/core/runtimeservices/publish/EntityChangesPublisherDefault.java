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
import java.util.Optional;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.publishing.spi.EntityChanges;
import org.apache.isis.applib.services.publishing.spi.EntityChangesSubscriber;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.having.HasEnabling;
import org.apache.isis.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.isis.core.transaction.changetracking.EntityChangesPublisher;
import org.apache.isis.core.transaction.changetracking.HasEnlistedEntityChanges;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named("isis.runtimeservices.EntityChangesPublisherDefault")
@Order(OrderPrecedence.EARLY)
@Primary
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
//@Log4j2
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

        val payload = getPayload(hasEnlistedEntityChanges);
        val handle = _Xray.enterEntityChangesPublishing(
                iaTracker,
                payload,
                enabledSubscribers,
                ()->getCannotPublishReason(payload));

        payload.ifPresent(entityChanges->{
            for (val subscriber : enabledSubscribers) {
                subscriber.onChanging(entityChanges);
            }
        });

        _Xray.exitPublishing(handle);
    }

    // -- HELPER

    private Optional<EntityChanges> getPayload(HasEnlistedEntityChanges hasEnlistedEntityChanges) {
        return enabledSubscribers.isEmpty()
                ? Optional.empty()
                : hasEnlistedEntityChanges.getEntityChanges(
                        clockService.getClock().javaSqlTimestamp(), // current time
                        userService.currentUserNameElseNobody()); // current user
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
