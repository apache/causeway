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

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.causeway.applib.services.publishing.spi.CommandSubscriber;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.having.HasEnabling;
import org.apache.causeway.core.metamodel.services.publishing.CommandPublisher;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * Default implementation of {@link CommandPublisher}.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".CommandPublisherDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Log4j2
public class CommandPublisherDefault implements CommandPublisher {

    final List<CommandSubscriber> subscribers;
    final Provider<InteractionLayerTracker> interactionServiceProvider;

    final Can<CommandSubscriber> enabledSubscribers;

    @Inject
    public CommandPublisherDefault(
            final List<CommandSubscriber> subscribers,
            final Provider<InteractionLayerTracker> interactionServiceProvider) {
        this.subscribers = subscribers;
        this.interactionServiceProvider = interactionServiceProvider;

        enabledSubscribers = Can.ofCollection(subscribers)
                .filter(HasEnabling::isEnabled);
    }

    @Override
    public void ready(@NonNull final Command command) {

        var handle = _Xray.enterCommandReadyPublishing(
                interactionServiceProvider.get(),
                command,
                enabledSubscribers,
                ()->getCannotPublishReason(command));

        if(canPublish(command) && command.getPublishingPhase().isReady()) {
            log.debug("about to PUBLISH command {}: {} to {}", "ready", command, enabledSubscribers);
            enabledSubscribers.forEach(subscriber -> subscriber.onReady(command));
        }

        _Xray.exitPublishing(handle);
    }

    @Override
    public void start(@NonNull final Command command) {

        var handle = _Xray.enterCommandStartedPublishing(
                interactionServiceProvider.get(),
                command,
                enabledSubscribers,
                ()->getCannotPublishReason(command));

        if(canPublish(command) && command.getPublishingPhase().isStarted()) {
            log.debug("about to PUBLISH command {}: {} to {}", "started", command, enabledSubscribers);
            enabledSubscribers.forEach(subscriber -> subscriber.onStarted(command));
        }

        _Xray.exitPublishing(handle);
    }

    @Override
    public void complete(final @NonNull Command command) {

        var handle = _Xray.enterCommandCompletedPublishing(
                interactionServiceProvider.get(),
                command,
                enabledSubscribers,
                ()->getCannotPublishReason(command));

        if(canPublish(command) && command.getPublishingPhase().isCompleted()) {
            log.debug("about to PUBLISH command {}: {} to {}", "completed", command, enabledSubscribers);
            enabledSubscribers.forEach(subscriber -> subscriber.onCompleted(command));
        }

        _Xray.exitPublishing(handle);
    }

    // -- HELPER

    private boolean canPublish(final Command command) {
        return enabledSubscribers.isNotEmpty()
                && command.getLogicalMemberIdentifier() != null; // eg null when seed fixtures
    }

    // x-ray support
    private @Nullable String getCannotPublishReason(final @NonNull Command command) {
        return enabledSubscribers.isEmpty()
                ? "no subscribers"
                : !command.getPublishingPhase().isReady()
                        ? String.format(
                                "publishing not enabled for given command\n%s",
                                _Xray.toText(command))
                        : command.getLogicalMemberIdentifier() == null
                                ? String.format(
                                        "no logical-member-id for given command\n%s",
                                        _Xray.toText(command))
                                : null;
    }

}

