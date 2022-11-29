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

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.extensions.commandlog.applib.dom.ExecuteIn;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.publishing.spi.CommandSubscriber;
import org.apache.causeway.applib.util.JaxbUtil;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Implementation of {@link CommandSubscriber} responsible for persisting the {@link Command} as a
 * {@link CommandLogEntry}.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandSubscriberForCommandLog")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT) // after JdoPersistenceLifecycleService
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class CommandSubscriberForCommandLog implements CommandSubscriber {

    final CommandLogEntryRepository<? extends CommandLogEntry> commandLogEntryRepository;
    final CausewayConfiguration causewayConfiguration;

    @Override
    public void onCompleted(final Command command) {

        // skip if no changes AND skipping is allowed
        if (causewayConfiguration.getExtensions().getCommandLog().getPublishPolicy().isOnlyIfSystemChanged()
                && !command.isSystemStateChanged()) {
            return;
        }

        val existingCommandLogEntryIfAny =
                commandLogEntryRepository.findByInteractionId(command.getInteractionId());
        if(existingCommandLogEntryIfAny.isPresent()) {
            val commandLogEntry = existingCommandLogEntryIfAny.get();
            switch (commandLogEntry.getExecuteIn()) {
                case FOREGROUND:
                    // this isn't expected to happen ... we just log the fact if it does
                    if(log.isWarnEnabled()) {
                        val existingCommandDto = existingCommandLogEntryIfAny.get().getCommandDto();

                        val existingCommandDtoXml = JaxbUtil.toXml(existingCommandDto)
                                .getValue().orElse("Dto to Xml failure");
                        val commandDtoXml = JaxbUtil.toXml(command.getCommandDto())
                                .getValue().orElse("Dto to Xml failure");

                        log.warn("existing: \n{}", existingCommandDtoXml);
                        log.warn("proposed: \n{}", commandDtoXml);
                    }
                    break;
                case BACKGROUND:
                    // this is expected behaviour; the command was already persisted when initially scheduled; we don't
                    // need to do anything else.
                    break;
            }
        } else {
            val parentInteractionId = command.getParentInteractionId();
            commandLogEntryRepository.createEntryAndPersist(command, parentInteractionId, ExecuteIn.FOREGROUND);
        }
    }

}
