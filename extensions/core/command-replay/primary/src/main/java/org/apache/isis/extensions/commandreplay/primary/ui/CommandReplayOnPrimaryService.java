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

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.core.annotation.Order;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.exceptions.RecoverableException;
import org.apache.isis.applib.services.commanddto.conmap.ContentMappingServiceForCommandsDto;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdoRepository;
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
    objectType = "isis.ext.commandReplayPrimary.CommandReplayOnPrimaryService"
)
@DomainServiceLayout(
    named = "Activity",
    menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@Named("isis.ext.commandReplayPrimary.CommandReplayOnPrimaryService")
@Order(OrderPrecedence.MIDPOINT)
@RequiredArgsConstructor
//@Log4j2
public class CommandReplayOnPrimaryService {

    @Inject final CommandJdoRepository commandServiceRepository;
    @Inject final JaxbService jaxbService;
    @Inject final MessageService messageService;
    @Inject final ContentMappingServiceForCommandsDto contentMappingServiceForCommandsDto;
    @Inject final CommandRetrievalService commandRetrievalService;

    public static abstract class ActionDomainEvent
            extends IsisModuleExtCommandReplayPrimary.ActionDomainEvent<CommandReplayOnPrimaryService> { }


    public static class FindCommandsDomainEvent extends ActionDomainEvent { }
    public static class NotFoundException extends RecoverableException {
        private static final long serialVersionUID = 1L;
        @Getter
        private final UUID uniqueId;
        public NotFoundException(final UUID uniqueId) {
            super("Command not found");
            this.uniqueId = uniqueId;
        }
    }

    /**
     * These actions should be called with HTTP Accept Header set to:
     * <code>application/xml;profile="urn:org.restfulobjects:repr-types/action-result";x-ro-domain-type="org.apache.isis.schema.cmd.v1.CommandsDto"</code>
     *
     * @param uniqueId - to search from.  This transactionId will <i>not</i> be included in the response.
     * @param batchSize - the maximum number of commands to return.  If not specified, all found will be returned.
     * @throws NotFoundException - if the command with specified transaction cannot be found.
     */
    @Action(domainEvent = FindCommandsDomainEvent.class, semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa = "fa-search")
    @MemberOrder(sequence="40")
    public List<CommandJdo> findCommands(
            @Nullable
            @ParameterLayout(named="Unique Id")
            final UUID uniqueId,
            @Nullable
            @ParameterLayout(named="Batch size")
            final Integer batchSize)
            throws NotFoundException {
        return commandRetrievalService.findCommandsOnPrimaryFrom(uniqueId, batchSize);
    }
    public Integer default1FindCommandsOnPrimaryFrom() {
        return commandRetrievalService.default1FindCommandsOnPrimaryFrom();
    }



    public static class DownloadCommandsDomainEvent extends ActionDomainEvent { }
    /**
     * These actions should be called with HTTP Accept Header set to:
     * <code>application/xml;profile="urn:org.restfulobjects:repr-types/action-result";x-ro-domain-type="org.apache.isis.schema.cmd.v1.CommandsDto"</code>
     *
     * @param uniqueId - to search from.  This transactionId will <i>not</i> be included in the response.
     * @param batchSize - the maximum number of commands to return.  If not specified, all found will be returned.
     * @throws NotFoundException - if the command with specified transaction cannot be found.
     */
    @Action(domainEvent = DownloadCommandsDomainEvent.class, semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa = "fa-download")
    @MemberOrder(sequence="50")
    public Clob downloadCommands(
            @Nullable
            final UUID uniqueId,
            @Nullable
            final Integer batchSize,
            final String filenamePrefix) {
        final List<CommandJdo> commands = commandServiceRepository.findSince(uniqueId, batchSize);
        if(commands == null) {
            messageService.informUser("No commands found");
        }

        final CommandsDto commandsDto =
                contentMappingServiceForCommandsDto.map(commands);

        final String fileName = String.format(
                "%s_%s.xml", filenamePrefix, elseDefault(uniqueId));

        final String xml = jaxbService.toXml(commandsDto);
        return new Clob(fileName, "application/xml", xml);
    }
    public Integer default1DownloadCommands() {
        return 25;
    }
    public String default2DownloadCommands() {
        return "commands_from";
    }



    public static class DownloadCommandByIdDomainEvent extends ActionDomainEvent { }
    /**
     * This action should be called with HTTP Accept Header set to:
     * <code>application/xml;profile="urn:org.restfulobjects:repr-types/action-result";x-ro-domain-type="org.apache.isis.schema.cmd.v1.CommandDto"</code>
     *
     * @param uniqueId - to download.
     * @throws NotFoundException - if the command with specified transaction cannot be found.
     */
    @Action(domainEvent = DownloadCommandByIdDomainEvent.class, semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa = "fa-download")
    @MemberOrder(sequence="50")
    public Clob downloadCommandById(
            final UUID uniqueId,
            final String filenamePrefix) {

        return commandServiceRepository.findByUniqueId(uniqueId)
                .map(commandJdo -> {

                    final CommandDto commandDto = commandJdo.getCommandDto();

                    final String fileName = String.format(
                            "%s_%s.xml", filenamePrefix, elseDefault(uniqueId));

                    final String xml = jaxbService.toXml(commandDto);
                    return new Clob(fileName, "application/xml", xml);

                }).orElseGet(() -> {
                    messageService.informUser("No command found");
                    return null;
                });
    }
    public String default1DownloadCommandById() {
        return "command";
    }


    private static String elseDefault(final UUID uuid) {
        return uuid != null ? uuid.toString() : "00000000-0000-0000-0000-000000000000";
    }


}

