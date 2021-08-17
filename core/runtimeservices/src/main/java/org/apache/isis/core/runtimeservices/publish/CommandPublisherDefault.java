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

import org.springframework.lang.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.Command.CommandPublishingPhase;
import org.apache.isis.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.isis.applib.services.publishing.spi.CommandSubscriber;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.having.HasEnabling;
import org.apache.isis.core.metamodel.services.publishing.CommandPublisher;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named("isis.runtimeservices.CommandPublisherDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class CommandPublisherDefault implements CommandPublisher {

    private final List<CommandSubscriber> subscribers;
    private final InteractionLayerTracker iaTracker;

    private Can<CommandSubscriber> enabledSubscribers = Can.empty();

    @PostConstruct
    public void init() {
        enabledSubscribers = Can.ofCollection(subscribers)
                .filter(HasEnabling::isEnabled);
    }

    @Override
    public void complete(final @NonNull Command command) {

        val handle = _Xray.enterCommandPublishing(
                iaTracker,
                command,
                enabledSubscribers,
                ()->getCannotPublishReason(command));

        if(canPublish(command)) {
            log.debug("about to PUBLISH command: {} to {}", command, enabledSubscribers);
            enabledSubscribers.forEach(subscriber -> subscriber.onCompleted(command));
            command.updater().setPublishingPhase(CommandPublishingPhase.COMPLETED); // one shot
        }

        _Xray.exitPublishing(handle);
    }

    // -- HELPER

    private boolean canPublish(final Command command) {
        return enabledSubscribers.isNotEmpty()
                && command.getPublishingPhase().isReady()
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

