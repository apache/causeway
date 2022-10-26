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
package org.apache.causeway.extensions.commandreplay.primary.restapi;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository.NotFoundException;
import org.apache.causeway.extensions.commandreplay.primary.CausewayModuleExtCommandReplayPrimary;
import org.apache.causeway.schema.cmd.v2.CommandDto;

/**
 * @since 2.0 {@index}
 */
@DomainService(
    nature = NatureOfService.REST
)
@Named(CausewayModuleExtCommandReplayPrimary.NAMESPACE + ".CommandRetrievalOnPrimaryService")
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@Profile("commandreplay-primary")
public class CommandRetrievalOnPrimaryService {

    public static abstract class ActionDomainEvent
            extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<CommandRetrievalOnPrimaryService> { }

    public static class FindCommandsOnPrimaryFromDomainEvent extends ActionDomainEvent { }

    @Inject CommandLogEntryRepository<? extends CommandLogEntry> commandLogEntryRepository;

    /**
     * TODO: outdated info ...
     * These actions should be called with HTTP Accept Header set to:
     * <code>application/xml;profile="urn:org.restfulobjects:repr-types/action-result";x-ro-domain-type="org.apache.causeway.schema.cmd.v1.CommandsDto"</code>
     *
     * @param interactionId - to search from.  This interactionId will <i>not</i> be included in the response.
     * @param batchSize - the maximum number of commands to return.  If not specified, all found will be returned.
     * @throws NotFoundException - if the command with specified transaction cannot be found.
     */
    @Action(
            domainEvent = FindCommandsOnPrimaryFromDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            typeOf = CommandDto.class)
    public List<CommandDto> findCommandsOnPrimaryAsDto(

            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named="Interaction Id")
            final UUID interactionId,

            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named="Batch size")
            final Integer batchSize) throws NotFoundException {

        return commandLogEntryRepository.findCommandsOnPrimaryElseFail(interactionId, batchSize)
                .stream()
                .map(CommandLogEntry::getCommandDto)
                .collect(Collectors.toList());
    }
    @MemberSupport public Integer default1FindCommandsOnPrimaryAsDto() {
        return 25;
    }

}

