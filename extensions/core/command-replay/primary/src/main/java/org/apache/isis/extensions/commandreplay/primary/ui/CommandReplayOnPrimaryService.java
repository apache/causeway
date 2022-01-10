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
package org.apache.isis.extensions.commandreplay.primary.ui;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.exceptions.RecoverableException;
import org.apache.isis.applib.services.commanddto.conmap.ContentMappingServiceForCommandsDto;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.extensions.commandlog.model.command.CommandModel;
import org.apache.isis.extensions.commandlog.model.command.CommandModelRepository;
import org.apache.isis.extensions.commandreplay.primary.IsisModuleExtCommandReplayPrimary;
import org.apache.isis.extensions.commandreplay.primary.restapi.CommandRetrievalService;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.CommandsDto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @since 2.0 {@index}
 */
@DomainService(
    nature = NatureOfService.VIEW,
    logicalTypeName = CommandReplayOnPrimaryService.LOGICAL_TYPE_NAME
)
@DomainServiceLayout(
    named = "Activity",
    menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@Named(CommandReplayOnPrimaryService.LOGICAL_TYPE_NAME)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor
//@Log4j2
public class CommandReplayOnPrimaryService {

    public static final String LOGICAL_TYPE_NAME = IsisModuleExtCommandReplayPrimary.NAMESPACE + ".CommandReplayOnPrimaryService";

    @Inject final CommandModelRepository<? extends CommandModel> commandModelRepository;
    @Inject final JaxbService jaxbService;
    @Inject final MessageService messageService;
    @Inject final ContentMappingServiceForCommandsDto contentMappingServiceForCommandsDto;
    @Inject final CommandRetrievalService commandRetrievalService;

    public static abstract class ActionDomainEvent<T> extends IsisModuleExtCommandReplayPrimary.ActionDomainEvent<T> { }


    public static class NotFoundException extends RecoverableException {
        private static final long serialVersionUID = 1L;
        @Getter
        private final UUID interactionId;
        public NotFoundException(final UUID interactionId) {
            super("Command not found");
            this.interactionId = interactionId;
        }
    }

    @Action(domainEvent = findCommands.ActionEvent.class, semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa = "fa-search", sequence="40")
    public class findCommands{

        public class ActionEvent extends ActionDomainEvent<findCommands> { }

        /**
         * These actions should be called with HTTP Accept Header set to:
         * <code>application/xml;profile="urn:org.restfulobjects:repr-types/action-result";x-ro-domain-type="org.apache.isis.schema.cmd.v1.CommandsDto"</code>
         *
         * @param interactionId - to search from.  This transactionId will <i>not</i> be included in the response.
         * @param batchSize - the maximum number of commands to return.  If not specified, all found will be returned.
         * @throws NotFoundException - if the command with specified transaction cannot be found.
         */
        @MemberSupport public List<? extends CommandModel> act(
                @Nullable
                @ParameterLayout(named="Interaction Id")
                final UUID interactionId,
                @Nullable
                @ParameterLayout(named="Batch size")
                final Integer batchSize)
                throws NotFoundException {
            return commandRetrievalService.findCommandsOnPrimaryFrom(interactionId, batchSize);
        }
        @MemberSupport public Integer default1Act() {
            return commandRetrievalService.default1FindCommandsOnPrimaryFrom();
        }

    }




    @Action(domainEvent = downloadCommands.ActionEvent.class, semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa = "fa-download", sequence="50")
    public class downloadCommands {

        public class ActionEvent extends ActionDomainEvent<downloadCommands> { }

        /**
         * These actions should be called with HTTP Accept Header set to:
         * <code>application/xml;profile="urn:org.restfulobjects:repr-types/action-result";x-ro-domain-type="org.apache.isis.schema.cmd.v1.CommandsDto"</code>
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
            final List<? extends CommandModel> commands = commandModelRepository.findSince(interactionId, batchSize);
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



    @Action(domainEvent = downloadCommandById.ActionEvent.class, semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa = "fa-download", sequence="50")
    public class downloadCommandById {

        public class ActionEvent extends ActionDomainEvent<downloadCommandById> { }

        /**
         * This action should be called with HTTP Accept Header set to:
         * <code>application/xml;profile="urn:org.restfulobjects:repr-types/action-result";x-ro-domain-type="org.apache.isis.schema.cmd.v1.CommandDto"</code>
         *
         * @param interactionId - to download.
         * @throws NotFoundException - if the command with specified transaction cannot be found.
         */
        @MemberSupport public Clob act(
                final UUID interactionId,
                final String filenamePrefix) {

            return commandModelRepository.findByInteractionId(interactionId)
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

