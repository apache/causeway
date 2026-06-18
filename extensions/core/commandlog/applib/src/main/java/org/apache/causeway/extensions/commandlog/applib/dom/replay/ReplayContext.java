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
package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.command.CommandExecutorService;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.scratchpad.Scratchpad;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.spi.CommandReplayMappingListener;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.extensions.commandlog.applib.spi.CommandReplayReferenceDataService;

import org.springframework.lang.Nullable;

import lombok.Value;
import lombok.experimental.Accessors;

import javax.inject.Inject;

/**
 * Bundles dependencies for the replay logic.
 */
@Value @Accessors(fluent = true)
public final class ReplayContext {
        RepositoryService repositoryService;
        InteractionService interactionService;
        TransactionService transactionService;
        CommandLogEntryRepository commandLogEntryRepository;
        CommandExecutorService commandExecutorService;
        ClockService clockService;
        List<CommandReplayMappingListener> commandReplayMappingListeners;
        Scratchpad scratchpad;
        MetaModelService metaModelService;
        CausewayConfiguration causewayConfiguration;
        List<CommandReplayReferenceDataService> commandReplayReferenceDataServices;

    SpecificationLoader specificationLoader;

    public ReplayContext(
            final RepositoryService repositoryService,
            final InteractionService interactionService,
            final TransactionService transactionService,
            final CommandLogEntryRepository commandLogEntryRepository,
            final CommandExecutorService commandExecutorService,
            final ClockService clockService,
            final List<CommandReplayMappingListener> commandReplayMappingListeners,
            final Scratchpad scratchpad,
            final MetaModelService metaModelService,
            final CausewayConfiguration causewayConfiguration,
            final List<CommandReplayReferenceDataService> commandReplayReferenceDataServices) {
        this(
                repositoryService,
                interactionService,
                transactionService,
                commandLogEntryRepository,
                commandExecutorService,
                clockService,
                commandReplayMappingListeners,
                scratchpad,
                metaModelService,
                causewayConfiguration,
                commandReplayReferenceDataServices,
                null);
    }

    public ReplayContext(
            final RepositoryService repositoryService,
            final InteractionService interactionService,
            final TransactionService transactionService,
            final CommandLogEntryRepository commandLogEntryRepository,
            final CommandExecutorService commandExecutorService,
            final ClockService clockService,
            final List<CommandReplayMappingListener> commandReplayMappingListeners,
            final Scratchpad scratchpad,
            final MetaModelService metaModelService,
            final CausewayConfiguration causewayConfiguration,
            final List<CommandReplayReferenceDataService> commandReplayReferenceDataServices,
            final SpecificationLoader specificationLoader) {
        this.repositoryService = repositoryService;
        this.interactionService = interactionService;
        this.transactionService = transactionService;
        this.commandLogEntryRepository = commandLogEntryRepository;
        this.commandExecutorService = commandExecutorService;
        this.clockService = clockService;
        this.commandReplayMappingListeners = commandReplayMappingListeners;
        this.scratchpad = scratchpad;
        this.metaModelService = metaModelService;
        this.causewayConfiguration = causewayConfiguration;
        this.commandReplayReferenceDataServices = commandReplayReferenceDataServices;
        this.specificationLoader = specificationLoader;
    }

    public Optional<CommandLogEntry> lookupCommandLogEntry(final @Nullable UUID interactionId) {
    	return interactionId!=null
			? commandLogEntryRepository().findByInteractionId(interactionId)
			: Optional.empty();
    }

}
