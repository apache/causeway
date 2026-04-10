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

import java.util.Optional;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.commons.internal.base._StableValue;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.schema.cmd.v2.CommandDto;

/**
 * Viewmodel that wraps a {@link CommandLogEntry}.
 */
@DomainObject(introspection = Introspection.ANNOTATION_REQUIRED)
@DomainObjectLayout(cssClassFa = "terminal")
@Named(ReplayableCommand.LOGICAL_TYPE_NAME)
public record ReplayableCommand(
        String commandLogEntryId,
        BookmarkService bookmarkService,
        CommandLogEntryRepository commandLogEntryRepository,
        _StableValue<CommandRecord> recordRef) implements ViewModel {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE + ".ReplayableCommand";

    // decouple from the underlying entity
    record CommandRecord(
            CommandDto commandDto,
            ReplayState replayState) {
    }

    @Inject
    public ReplayableCommand(
            final String memento,
            final BookmarkService bookmarkService,
            final CommandLogEntryRepository commandLogEntryRepository) {
        this(memento, bookmarkService, commandLogEntryRepository, new _StableValue<>());
    }

    @ObjectSupport public String title() {
        return "Replayable Command";
    }

    @Property
    public UUID getInteractionId() {
        return commandRecord()
            .map(CommandRecord::commandDto)
            .map(CommandDto::getInteractionId)
            .map(UUID::fromString)
            .orElse(null);
    }

    @Override
    public String viewModelMemento() {
        return commandLogEntryId;
    }

    // -- HELPER

    private Optional<CommandRecord> commandRecord() {
        return commandLogEntry()
                .map(commandLogEntry->new CommandRecord(commandLogEntry.getCommandDto(), commandLogEntry.getReplayState()));
    }

    private Optional<CommandLogEntry> commandLogEntry() {
        return bookmarkService.lookup(commandLogEntryBookmark(), CommandLogEntry.class);
    }

    private Bookmark commandLogEntryBookmark() {
        return bookmarkService.bookmarkFor(CommandLogEntry.class, commandLogEntryId)
                .orElseThrow(()->_Exceptions.unrecoverable(
                        "framework error: cannot create bookmark for CommandLogEntry using id '%s'",
                        commandLogEntryId));
    }

}
