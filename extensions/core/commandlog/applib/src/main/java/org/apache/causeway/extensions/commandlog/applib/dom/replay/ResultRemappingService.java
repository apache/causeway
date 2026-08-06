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

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.spi.CommandReplayMappingListener;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.common.v2.ValueType;

import lombok.extern.slf4j.Slf4j;

/**
 * Applies replay bookmark mappings to independent command DTO copies and dispatches result observations.
 */
@Service
@Named(CausewayModuleExtCommandLogApplib.NAMESPACE + ".ResultRemappingService")
@Slf4j
public record ResultRemappingService(
        List<CommandReplayMappingListener> commandReplayMappingListeners) {

    @Inject
    public ResultRemappingService {
        commandReplayMappingListeners = List.copyOf(commandReplayMappingListeners);
    }

    @Programmatic
    public CommandDto remapped(final CommandDto recordedCommandDto) {
        var executionDto = CommandDtoUtils.copy(recordedCommandDto);
        if (executionDto == null) {
            return null;
        }
        remapTargets(executionDto);
        remapReferenceParameters(executionDto);
        return executionDto;
    }

    @Programmatic
    public CommandDtoUtils.CommandExportDto remapped(
            final CommandDtoUtils.CommandExportDto recordedExportDto) {
        if (recordedExportDto == null) {
            return null;
        }
        var exportCopy = new CommandDtoUtils.CommandExportDto();
        exportCopy.setCommand(remapped(recordedExportDto.getCommand()));
        exportCopy.setResult(copyAndRemap(recordedExportDto.getResult()));
        return exportCopy;
    }

    @Programmatic
    public Optional<Bookmark> lookup(final Bookmark recordedBookmark) {
        if (recordedBookmark == null) {
            return Optional.empty();
        }
        for (var listener : commandReplayMappingListeners) {
            try {
                var replacement = Optional.ofNullable(listener.lookup(recordedBookmark))
                        .orElseGet(Optional::empty);
                if (replacement.isPresent()) {
                    return replacement;
                }
            } catch (RuntimeException ex) {
                log.warn("Command replay mapping lookup failed for bookmark '{}'", recordedBookmark, ex);
            }
        }
        return Optional.empty();
    }

    @Programmatic
    public void notifyReplayResult(
            final Bookmark recordedResult,
            final Bookmark actualResult,
            final UUID interactionId) {
        if (recordedResult == null || actualResult == null) {
            return;
        }
        commandReplayMappingListeners.forEach(
                listener -> listener.onReplayResult(recordedResult, actualResult, interactionId));
    }

    private void remapTargets(final CommandDto executionDto) {
        Optional.ofNullable(executionDto.getTargets())
                .stream()
                .flatMap(targets -> targets.getOid().stream())
                .forEach(this::remapOid);
    }

    private void remapReferenceParameters(final CommandDto executionDto) {
        if (!(executionDto.getMember() instanceof ActionDto actionDto)) {
            return;
        }
        Optional.ofNullable(actionDto.getParameters())
                .stream()
                .flatMap(parameters -> parameters.getParameter().stream())
                .filter(ResultRemappingService::isPopulatedReference)
                .map(ParamDto::getReference)
                .forEach(this::remapOid);
    }

    private static boolean isPopulatedReference(final ParamDto parameter) {
        return parameter != null
                && parameter.getType() == ValueType.REFERENCE
                && parameter.getReference() != null;
    }

    private void remapOid(final OidDto oidDto) {
        if (oidDto == null) {
            return;
        }
        lookup(Bookmark.forOidDto(oidDto))
                .ifPresent(replacement -> copyToOid(replacement, oidDto));
    }

    private CommandDtoUtils.BookmarkDto copyAndRemap(
            final CommandDtoUtils.BookmarkDto recordedBookmarkDto) {
        if (recordedBookmarkDto == null) {
            return null;
        }
        var bookmarkCopy = new CommandDtoUtils.BookmarkDto();
        bookmarkCopy.setType(recordedBookmarkDto.getType());
        bookmarkCopy.setId(recordedBookmarkDto.getId());
        lookup(recordedBookmarkDto.toBookmark())
                .ifPresent(replacement -> copyToBookmarkDto(replacement, bookmarkCopy));
        return bookmarkCopy;
    }

    private static void copyToOid(final Bookmark bookmark, final OidDto oidDto) {
        oidDto.setType(bookmark.logicalTypeName());
        oidDto.setId(bookmark.identifier());
    }

    private static void copyToBookmarkDto(
            final Bookmark bookmark,
            final CommandDtoUtils.BookmarkDto bookmarkDto) {
        bookmarkDto.setType(bookmark.logicalTypeName());
        bookmarkDto.setId(bookmark.identifier());
    }
}
