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

import org.springframework.lang.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.annotation.*;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.exceptions.RecoverableException;
import org.apache.isis.extensions.commandlog.model.command.CommandModel;
import org.apache.isis.extensions.commandlog.model.command.CommandModelRepository;
import org.apache.isis.extensions.commandreplay.primary.IsisModuleExtCommandReplayPrimary;

import lombok.Getter;

/**
 * @since 2.0 {@index}
 */
@DomainService(
    nature = NatureOfService.REST,
    logicalTypeName = CommandRetrievalService.LOGICAL_TYPE_NAME
)
@Named(CommandRetrievalService.LOGICAL_TYPE_NAME)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class CommandRetrievalService {

    public static final String LOGICAL_TYPE_NAME = IsisModuleExtCommandReplayPrimary.NAMESPACE + ".CommandRetrievalService";

    public static abstract class ActionDomainEvent
            extends IsisModuleExtCommandReplayPrimary.ActionDomainEvent<CommandRetrievalService> { }

    public static class FindCommandsOnPrimaryFromDomainEvent extends ActionDomainEvent { }
    public static class NotFoundException extends RecoverableException {
        private static final long serialVersionUID = 1L;
        @Getter
        private final UUID interactionId;
        public NotFoundException(final UUID interactionId) {
            super("Command not found");
            this.interactionId = interactionId;
        }
    }

    /**
     * These actions should be called with HTTP Accept Header set to:
     * <code>application/xml;profile="urn:org.restfulobjects:repr-types/action-result";x-ro-domain-type="org.apache.isis.schema.cmd.v1.CommandsDto"</code>
     *
     * @param interactionId - to search from.  This interactionId will <i>not</i> be included in the response.
     * @param batchSize - the maximum number of commands to return.  If not specified, all found will be returned.
     * @throws NotFoundException - if the command with specified transaction cannot be found.
     */
    @Action(domainEvent = FindCommandsOnPrimaryFromDomainEvent.class, semantics = SemanticsOf.SAFE)
    public List<? extends CommandModel> findCommandsOnPrimaryFrom(
            @Nullable
            @ParameterLayout(named="Interaction Id")
            final UUID interactionId,
            @Nullable
            @ParameterLayout(named="Batch size")
            final Integer batchSize)
            throws NotFoundException {
        final List<? extends CommandModel> commands = commandModelRepository.findSince(interactionId, batchSize);
        if(commands == null) {
            throw new NotFoundException(interactionId);
        }
        return commands;
    }
    public Integer default1FindCommandsOnPrimaryFrom() {
        return 25;
    }

    @Inject CommandModelRepository<? extends CommandModel> commandModelRepository;
}

