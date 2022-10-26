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
package org.apache.causeway.extensions.commandreplay.primary.ui;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.commanddto.conmap.ContentMappingServiceForCommandsDto;
import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository.NotFoundException;
import org.apache.causeway.extensions.commandreplay.primary.CausewayModuleExtCommandReplayPrimary;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.CommandsDto;

import lombok.RequiredArgsConstructor;

/**
 * @since 2.0 {@index}
 */
@DomainService(
    nature = NatureOfService.VIEW
)
@DomainServiceLayout(
    named = "Activity",
    menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@Named(CausewayModuleExtCommandReplayPrimary.NAMESPACE + ".CommandReplayOnPrimaryService")
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@Profile("commandreplay-primary")
@RequiredArgsConstructor
//@Log4j2
public class CommandReplayOnPrimaryService {

    @Inject final CommandLogEntryRepository<? extends CommandLogEntry> commandLogEntryRepository;
    @Inject final JaxbService jaxbService;
    @Inject final MessageService messageService;
    @Inject final ContentMappingServiceForCommandsDto contentMappingServiceForCommandsDto;

    public static abstract class ActionDomainEvent<T>
    extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<T> { }


    @Action(domainEvent = findCommands.ActionDomainEvent.class, semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa = "fa-search", sequence="40")
    public class findCommands{

        public class ActionDomainEvent extends CommandReplayOnPrimaryService.ActionDomainEvent<findCommands> { }

        /**
         * These actions should be called with HTTP Accept Header set to:
         * <code>application/xml;profile="urn:org.restfulobjects:repr-types/action-result";x-ro-domain-type="org.apache.causeway.schema.cmd.v2.CommandsDto"</code>
         *
         * @param interactionId - to search from.  This transactionId will <i>not</i> be included in the response.
         * @param batchSize - the maximum number of commands to return.  If not specified, all found will be returned.
         * @throws NotFoundException - if the command with specified transaction cannot be found.
         */
        @MemberSupport public List<? extends CommandLogEntry> act(
                @Nullable
                @ParameterLayout(named="Interaction Id")
                final UUID interactionId,
                @Nullable
                @ParameterLayout(named="Batch size")
                final Integer batchSize)
                throws NotFoundException {
            return commandLogEntryRepository.findCommandsOnPrimaryElseFail(interactionId, batchSize);
        }
        @MemberSupport public Integer default1Act() {
            return 25;
        }

    }

    @Action(domainEvent = downloadCommands.ActionDomainEvent.class, semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa = "fa-download", sequence="50")
    public class downloadCommands {

        public class ActionDomainEvent extends CommandReplayOnPrimaryService.ActionDomainEvent<downloadCommands> { }

        /**
         * These actions should be called with HTTP Accept Header set to:
         * <code>application/xml;profile="urn:org.restfulobjects:repr-types/action-result";x-ro-domain-type="org.apache.causeway.schema.cmd.v2.CommandsDto"</code>
         *
         * @param interactionId - to search from.  This transactionId will <i>not</i> be included in the response.
         * @param batchSize - the maximum number of commands to return.  If not specified, all found will be returned.
         * @throws NotFoundException - if the command with specified transaction cannot be found.
         */
        @MemberSupport public Clob act(
                @Nullable
                final UUID interactionId,
                @Nullable
                final Integer batchSize,
                final String filenamePrefix) {
            final List<? extends CommandLogEntry> commands = commandLogEntryRepository.findSince(interactionId, batchSize);
            if(commands == null) {
                messageService.informUser("No commands found");
            }

            final CommandsDto commandsDto =
                    contentMappingServiceForCommandsDto.map(commands);

            final String fileName = String.format(
                    "%s_%s.xml", filenamePrefix, elseDefault(interactionId));

            final String xml = jaxbService.toXml(commandsDto);
            return new Clob(fileName, "application/xml", xml);
        }
        @MemberSupport public Integer default1Act() {
            return 25;
        }
        @MemberSupport public String default2Act() {
            return "commands_from";
        }
    }



    @Action(domainEvent = downloadCommandById.ActionDomainEvent.class, semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa = "fa-download", sequence="50")
    public class downloadCommandById {

        public class ActionDomainEvent extends CommandReplayOnPrimaryService.ActionDomainEvent<downloadCommandById> { }

        /**
         * This action should be called with HTTP Accept Header set to:
         * <code>application/xml;profile="urn:org.restfulobjects:repr-types/action-result";x-ro-domain-type="org.apache.causeway.schema.cmd.v2.CommandDto"</code>
         *
         * @param interactionId - to download.
         * @throws NotFoundException - if the command with specified transaction cannot be found.
         */
        @MemberSupport public Clob act(
                final UUID interactionId,
                final String filenamePrefix) {

            return commandLogEntryRepository.findByInteractionId(interactionId)
                    .map(commandJdo -> {

                        final CommandDto commandDto = commandJdo.getCommandDto();

                        final String fileName = String.format(
                                "%s_%s.xml", filenamePrefix, elseDefault(interactionId));

                        final String xml = jaxbService.toXml(commandDto);
                        return new Clob(fileName, "application/xml", xml);

                    }).orElseGet(() -> {
                        messageService.informUser("No command found");
                        return null;
                    });
        }
        @MemberSupport public String default1Act() {
            return "command";
        }

    }

    private static String elseDefault(final UUID uuid) {
        return uuid != null ? uuid.toString() : "00000000-0000-0000-0000-000000000000";
    }


}

