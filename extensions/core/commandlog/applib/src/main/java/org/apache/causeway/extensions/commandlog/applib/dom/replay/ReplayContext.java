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

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.command.CommandExecutorService;
import org.apache.causeway.applib.services.iactn.InteractionService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.spi.CommandReplayReferenceDataService;

/**
 * Bundles dependencies for the replay logic.
 */
public record ReplayContext(
        RepositoryService repositoryService,
        InteractionService interactionService,
        TransactionService transactionService,
        CommandLogEntryRepository commandLogEntryRepository,
        CommandExecutorService commandExecutorService,
        ClockService clockService,
        ResultRemappingService resultRemappingService,
        @Nullable BookmarkService bookmarkService,
        @Nullable ApplicationFeatureRepository applicationFeatureRepository,
        @Nullable CausewayConfiguration causewayConfiguration,
        @Nullable SpecificationLoader specificationLoader,
        List<CommandReplayReferenceDataService> commandReplayReferenceDataServices) {

    public ReplayContext(
            final RepositoryService repositoryService,
            final InteractionService interactionService,
            final TransactionService transactionService,
            final CommandLogEntryRepository commandLogEntryRepository,
            final CommandExecutorService commandExecutorService,
            final ClockService clockService,
            final ResultRemappingService resultRemappingService) {
        this(repositoryService, interactionService, transactionService, commandLogEntryRepository,
                commandExecutorService, clockService, resultRemappingService, null, null,
                null, null, List.of());
    }

    public ReplayContext(
            final RepositoryService repositoryService,
            final InteractionService interactionService,
            final TransactionService transactionService,
            final CommandLogEntryRepository commandLogEntryRepository,
            final CommandExecutorService commandExecutorService,
            final ClockService clockService,
            final ResultRemappingService resultRemappingService,
            final @Nullable BookmarkService bookmarkService,
            final @Nullable ApplicationFeatureRepository applicationFeatureRepository) {
        this(repositoryService, interactionService, transactionService, commandLogEntryRepository,
                commandExecutorService, clockService, resultRemappingService, bookmarkService,
                applicationFeatureRepository, null, null, List.of());
    }

    public Optional<CommandLogEntry> lookupCommandLogEntry(final @Nullable UUID interactionId) {
        return interactionId!=null
            ? commandLogEntryRepository().findByInteractionId(interactionId)
            : Optional.empty();
    }

    public boolean isRecordingSupportEnabled() {
        return causewayConfiguration != null
                && causewayConfiguration.extensions().commandLog().recordingSupport().isEnabled();
    }

    boolean isDomainService(final @Nullable Bookmark bookmark) {
        return bookmark != null
                && specificationLoader != null
                && specificationLoader.specForLogicalTypeName(bookmark.logicalTypeName())
                .map(specification -> specification.isDomainService())
                .orElse(false);
    }

    boolean isExportRoot(final @Nullable Bookmark bookmark) {
        return isDomainService(bookmark)
                || CommandReplayReferenceDataService.isReferenceData(
                        commandReplayReferenceDataServices, bookmark);
    }
}
