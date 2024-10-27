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
package org.apache.causeway.extensions.commandlog.applib.subscriber;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.publishing.spi.CommandSubscriber;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ExecuteIn;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Implementation of {@link CommandSubscriber} responsible for persisting the {@link Command} as a
 * {@link CommandLogEntry}.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CommandSubscriberForCommandLog.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.MIDPOINT) // after JdoPersistenceLifecycleService
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class CommandSubscriberForCommandLog implements CommandSubscriber {

    static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandSubscriberForCommandLog";

    final CommandLogEntryRepository commandLogEntryRepository;
    final RepositoryService repositoryService;
    final CausewayConfiguration causewayConfiguration;
    final ClockService clockService;

    @Override
    public boolean isEnabled() {
        return causewayConfiguration.getExtensions().getCommandLog().getPersist().isEnabled();
    }

    @Override
    public void onReady(final Command command) {

        if (!isEnabled()) {
            return;
        }

        var existingCommandLogEntryIfAny =
                commandLogEntryRepository.findByInteractionId(command.getInteractionId());
        if(existingCommandLogEntryIfAny.isPresent()) {

            var commandLogEntry = existingCommandLogEntryIfAny.get();
            switch (commandLogEntry.getExecuteIn()) {
                case FOREGROUND:
                    // this isn't really expected to happen ... we just log the fact if it does and the value is different
                    if(log.isWarnEnabled()) {
                        var existingCommandDto = existingCommandLogEntryIfAny.get().getCommandDto();

                        var existingCommandDtoXml = Try.call(()->CommandDtoUtils.dtoMapper().toString(existingCommandDto))
                                .getValue().orElse("Dto to Xml failure");
                        var commandDtoXml = Try.call(()->CommandDtoUtils.dtoMapper().toString(command.getCommandDto()))
                                .getValue().orElse("Dto to Xml failure");

                        if(!existingCommandDtoXml.equals(commandDtoXml)) {
                            log.warn("existing: \n{}", existingCommandDtoXml);
                            log.warn("proposed: \n{}", commandDtoXml);
                        }
                    }
                    break;
                case BACKGROUND:
                    // this is expected behaviour; the command was already persisted by
                    // BackgroundService.PersistCommandExecutorService when BackgroundService#submit(...) was called;
                    // so there's no need to do anything else.
                    break;
            }

        } else {
            var parentInteractionId = command.getParentInteractionId(); // will be null in most (all?) cases
            commandLogEntryRepository.createEntryAndPersist(command, parentInteractionId, ExecuteIn.FOREGROUND);
        }

    }

    @Override
    public void onStarted(final Command command) {

        if (!isEnabled()) {
            return;
        }

        commandLogEntryRepository.findByInteractionId(command.getInteractionId())
            .ifPresent(commandLogEntry -> {
                commandLogEntry.sync(command);
            });
    }

    @Override
    public void onCompleted(final Command command) {

        if (!isEnabled()) {
            return;
        }

        commandLogEntryRepository.findByInteractionId(command.getInteractionId())
            .ifPresent(commandLogEntry -> {
                commandLogEntry.sync(command);
            });
    }

}
