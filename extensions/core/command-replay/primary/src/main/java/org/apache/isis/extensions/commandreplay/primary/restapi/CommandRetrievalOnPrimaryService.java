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
package org.apache.isis.extensions.commandreplay.primary.restapi;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.annotation.Profile;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.extensions.commandlog.applib.IsisModuleExtCommandLogApplib;
import org.apache.isis.extensions.commandlog.applib.command.CommandLog;
import org.apache.isis.extensions.commandlog.applib.command.ICommandLogRepository;
import org.apache.isis.extensions.commandlog.applib.command.ICommandLogRepository.NotFoundException;
import org.apache.isis.extensions.commandreplay.primary.IsisModuleExtCommandReplayPrimary;
import org.apache.isis.schema.cmd.v2.CommandDto;

/**
 * @since 2.0 {@index}
 */
@DomainService(
    nature = NatureOfService.REST
)
@Named(IsisModuleExtCommandReplayPrimary.NAMESPACE + ".CommandRetrievalOnPrimaryService")
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@Profile("command-replay-primary")
public class CommandRetrievalOnPrimaryService {

    public static abstract class ActionDomainEvent
            extends IsisModuleExtCommandLogApplib.ActionDomainEvent<CommandRetrievalOnPrimaryService> { }

    public static class FindCommandsOnPrimaryFromDomainEvent extends ActionDomainEvent { }

    @Inject ICommandLogRepository<? extends CommandLog> commandLogRepository;

    /**
     * TODO: outdated info ...
     * These actions should be called with HTTP Accept Header set to:
     * <code>application/xml;profile="urn:org.restfulobjects:repr-types/action-result";x-ro-domain-type="org.apache.isis.schema.cmd.v1.CommandsDto"</code>
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

        return commandLogRepository.findCommandsOnPrimaryElseFail(interactionId, batchSize)
                .stream()
                .map(CommandLog::getCommandDto)
                .collect(Collectors.toList());
    }
    @MemberSupport public Integer default1FindCommandsOnPrimaryAsDto() {
        return 25;
    }

}

