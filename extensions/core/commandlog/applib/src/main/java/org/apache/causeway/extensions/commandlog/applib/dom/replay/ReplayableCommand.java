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
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.commons.internal.base._StableValue;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.io.JsonUtils;
import org.apache.causeway.commons.io.TextUtils;
import org.apache.causeway.commons.io.YamlUtils;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.MemberDto;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc;
import org.apache.causeway.valuetypes.asciidoc.builder.AsciiDocBuilder;
import org.apache.causeway.valuetypes.asciidoc.builder.AsciiDocFactory;

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
    @PropertyLayout(
            sequence = "1",
            fieldSetId = "details",
            describedAs = "UUID of the original (replayabel) Command")
    public UUID getInteractionId() {
        return commandRecord()
            .map(CommandRecord::commandDto)
            .map(CommandDto::getInteractionId)
            .map(UUID::fromString)
            .orElse(null);
    }

    @Property
    @PropertyLayout(
            sequence = "2.1",
            fieldSetId = "details",
            describedAs = "Target Type of the original (replayabel) Command")
    public String getTargetType() {
        return commandRecord()
            .map(CommandRecord::commandDto)
            .map(commandDto->commandDto.getTargets().getOid().get(0))
            .map(OidDto::getType)
            .orElse(null);
    }

    @Property
    @PropertyLayout(
            sequence = "2.2",
            fieldSetId = "details",
            describedAs = "Target ID of the original (replayabel) Command")
    public String getTargetId() {
        return commandRecord()
            .map(CommandRecord::commandDto)
            .map(commandDto->commandDto.getTargets().getOid().get(0))
            .map(OidDto::getId)
            .map(id->_Strings.ellipsifyAtEnd(id, 10, "..."))
            .orElse(null);
    }

    @Property
    @PropertyLayout(
            sequence = "3.1",
            fieldSetId = "details",
            describedAs = "Replayabel Action or Property, that was executed as captured by the original Command")
    public String getMember() {
        return commandRecord()
            .map(CommandRecord::commandDto)
            .map(CommandDto::getMember)
            .map(MemberDto::getLogicalMemberIdentifier)
            .map(TextUtils::cutter)
            .map(cutter->cutter.keepAfter("#").getValue())
            .orElse(null);
    }

    @Property
    @PropertyLayout(
            sequence = "4",
            fieldSetId = "details",
            describedAs = "Replay State of the original (replayabel) Command. "
                    + "When imported initially is PENDING. "
                    + "Then after replay its either OK or FAILED. "
                    + "Can be manually set to EXCLUDED, which marks it to be ignored for replay.")
    public ReplayState getReplayState() {
        return commandRecord()
            .map(CommandRecord::replayState)
            .orElse(null);
    }

    @Property
    @PropertyLayout(
            sequence = "9",
            fieldSetId = "dto",
            hidden = Where.ALL_TABLES,
            labelPosition = LabelPosition.NONE,
            describedAs = "DTO of the original (replayabel) Command")
    public AsciiDoc getDto() {
        return commandRecord()
            .map(CommandRecord::commandDto)
            .map(commandDto->YamlUtils.toStringUtf8(commandDto,
                JsonUtils::onlyIncludeNonNull))
            .map(yaml->new AsciiDocBuilder()
                    .append(doc->AsciiDocFactory.sourceBlock(doc, "yaml", yaml))
                    .buildAsValue())
            .orElseGet(()->new AsciiDoc("empty"));
    }

    // -- ACTIONS

    //TODO hide if not PENDING
    @Action
    @ActionLayout()
            //hidden = Where.NOWHERE) // show in tables //TODO NPE bug
    public ReplayableCommand replay() {
        commandLogEntry()
            .filter(entry->entry.getReplayState() == ReplayState.PENDING)
            .ifPresent(entry->{
                //TODO actually replay
                entry.setReplayState(ReplayState.OK);
            });
        return this;
    }

    //TODO hide if not PENDING
    @Action
    @ActionLayout(
            //hidden = Where.NOWHERE, // show in tables //TODO NPE bug
            describedAs = "Marks selected Commands to be EXCLUDED from replay.")
    public ReplayableCommand excludeFromReplay() {
        commandLogEntry()
            .filter(entry->entry.getReplayState() == ReplayState.PENDING)
            .ifPresent(entry->entry.setReplayState(ReplayState.EXCLUDED));
        return this;
    }

    // -- VM STATE

    @Override
    public String viewModelMemento() {
        return commandLogEntryId;
    }

    // -- HELPER

    private Optional<CommandRecord> commandRecord() {
        return Optional.ofNullable(recordRef.orElseSet(()->
            commandLogEntry()
                .map(commandLogEntry->new CommandRecord(
                        commandLogEntry.getCommandDto(),
                        commandLogEntry.getReplayState()))
                .orElse(null)));
    }

    Optional<CommandLogEntry> commandLogEntry() {
        return bookmarkService.lookup(commandLogEntryBookmark(), CommandLogEntry.class);
    }

    private Bookmark commandLogEntryBookmark() {
        return bookmarkService.bookmarkFor(CommandLogEntry.class, commandLogEntryId)
                .orElseThrow(()->_Exceptions.unrecoverable(
                        "framework error: cannot create bookmark for CommandLogEntry using id '%s'",
                        commandLogEntryId));
    }

}
