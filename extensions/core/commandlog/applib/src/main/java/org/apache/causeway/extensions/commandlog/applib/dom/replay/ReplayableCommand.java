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

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.jaxb.JavaTimeXMLGregorianCalendarMarshalling;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.command.CommandExecutorService.InteractionContextPolicy;
import org.apache.causeway.applib.services.command.CommandRecordingSuppressed;
import org.apache.causeway.applib.services.scratchpad.Scratchpad;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Refs.ObjectReference;
import org.apache.causeway.commons.io.JsonUtils;
import org.apache.causeway.commons.io.TextUtils;
import org.apache.causeway.commons.io.YamlUtils;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.extensions.commandlog.applib.spi.CommandReplayMappingListener;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.MemberDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.ReplayableCommandParticipant.Role;
import org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc;
import org.apache.causeway.valuetypes.asciidoc.builder.AsciiDocBuilder;
import org.apache.causeway.valuetypes.asciidoc.builder.AsciiDocFactory;

import org.springframework.transaction.annotation.Propagation;

import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * Viewmodel that wraps a {@link CommandLogEntry}.
 */
@DomainObject(introspection = Introspection.ANNOTATION_REQUIRED)
@DomainObjectLayout//(cssClassFa = "terminal")
@Named(ReplayableCommand.LOGICAL_TYPE_NAME)
@Log4j2
public final class ReplayableCommand implements ViewModel, Comparable<ReplayableCommand>, CommandRecordingSuppressed {

    public static abstract class ActionDomainEvent<T>
            extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<T> {
    }

    private final UUID interactionId;

    @Programmatic
    public UUID interactionId() {
        return interactionId;
    }

    private final ReplayContext replayContext;

    @Programmatic
    public ReplayContext replayContext() {
        return replayContext;
    }


    private final ObjectReference<CommandRecord> recordRef;

    @Programmatic
    public ObjectReference<CommandRecord> recordRef() {
        return recordRef;
    }

    private final CommandExportManager commandExportManager;

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE + ".ReplayableCommand";

    // decoupled from the underlying entity
    @Value
    @Accessors(fluent = true)
    final static class CommandRecord {

        final CommandDto commandDto;
        final ReplayState replayState;

        boolean canReplayOrRetry() {
            return replayState.isReplayOrRetryEnabled();
        }

        boolean canReplayOrRetryOrMarkForExclusion() {
            return replayState.isPendingOrFailed();
        }

        public String faQuickIcon() {
            switch (replayState) {
                case UNDEFINED:
                    return "solid terminal .col-indigo";
                case EXPORTED:
                    return "solid terminal .col-indigo, solid circle-arrow-right .ov-size-80 .ov-right-45 .ov-bottom-45 .col-dodgerblue";
                case PENDING:
                    return "solid terminal .col-indigo, solid circle-pause       .ov-size-80 .ov-right-45 .ov-bottom-45 .col-gold";
                case OK:
                    return "solid terminal .col-indigo, solid circle-check       .ov-size-80 .ov-right-45 .ov-bottom-45 .col-green";
                case FAILED:
                    return "solid terminal .col-indigo, solid circle-exclamation .ov-size-80 .ov-right-45 .ov-bottom-45 .col-red";
                case EXCLUDED:
                    return "solid terminal .col-indigo, solid circle-xmark       .ov-size-80 .ov-right-45 .ov-bottom-45 .col-grey";
            }
            ;
            return null;
        }

        //v2 backport, using png screenshots from v4
        public String iconSuffix() {
            switch (replayState) {
                case UNDEFINED:
                    return "";
                case EXPORTED:
                    return "exported";
                case PENDING:
                    return "pending";
                case OK:
                    return "ok";
                case FAILED:
                    return "failed";
                case EXCLUDED:
                    return "excluded";
            }
            ;
            return null;
        }
    }

    public ReplayableCommand(
            final String memento,
            final ReplayContext replayContext) {
        this(UUID.fromString(memento), replayContext);
    }

    @Inject
    public ReplayableCommand(
            final String memento,
            final ReplayContext replayContext,
            final Scratchpad scratchpad) {
        this(UUID.fromString(memento), replayContext, scratchpad);
    }

    ReplayableCommand(
            final UUID interactionId,
            final ReplayContext replayContext) {
        this(interactionId, replayContext, new ObjectReference<>(null), null);
    }

    ReplayableCommand(
            final UUID interactionId,
            final ReplayContext replayContext,
            final Scratchpad scratchpad) {
        this(interactionId, replayContext, new ObjectReference<>(null),
                CommandExportManager.currentExportManager(scratchpad).orElse(null));
    }

    ReplayableCommand(
            final UUID interactionId,
            final ReplayContext replayContext,
            final ObjectReference<CommandRecord> recordRef) {
        this(interactionId, replayContext, recordRef, null);
    }

    ReplayableCommand(
            final UUID interactionId,
            final ReplayContext replayContext,
            final ObjectReference<CommandRecord> recordRef,
            final CommandExportManager commandExportManager) {
        this.interactionId = interactionId;
        this.replayContext = replayContext;
        this.recordRef = recordRef;
        this.commandExportManager = commandExportManager;
    }

    @ObjectSupport
    public String title() {
        final var timestamp = getTimestampIfAny().map(ChronoZonedDateTime::toInstant).map(Instant::toString).map(x -> " @ " + x).orElse("");
        return targetTitlePrefix() + " #" + getMember() + timestamp;
    }

    private String targetTitlePrefix() {
        return commandRecord()
                .map(CommandRecord::commandDto)
                .map(CommandDto::getTargets)
                .filter(targets -> !targets.getOid().isEmpty())
                .map(targets -> targets.getOid().get(0))
                .map(target -> target.getType() + ":" + target.getId())
                .orElse("");
    }

    //requires v4
//    @ObjectSupport public ObjectSupport.IconResource icon(final ObjectSupport.IconSize iconSize) {
//        return commandRecord()
//                .map(CommandRecord::faQuickIcon)
//                .map(FontAwesomeLayers::fromQuickNotation)
//                .map(ObjectSupport.FontAwesomeIconResource::new)
//                .orElse(null);
//    }
    @ObjectSupport
    public String iconName() {
        return commandRecord()
                .map(CommandRecord::iconSuffix)
                .orElse(null);
    }

    @Property
    @PropertyLayout(
            sequence = "1.1",
            fieldSetId = "details",
            describedAs = "UUID of the original (replayable) Command")
    public UUID getInteractionId() {
        return interactionId;
    }

    @Property
    @PropertyLayout(
            sequence = "1.2",
            fieldSetId = "details",
            describedAs = "Timestamp of the original (replayable) Command")
    public ZonedDateTime getTimestamp() {
        return getTimestampIfAny()
                .orElse(null);
    }

    @Programmatic
    public Optional<ZonedDateTime> getTimestampIfAny() {
        return commandRecord()
                .map(CommandRecord::commandDto)
                .map(CommandDto::getTimestamp)
                .map(JavaTimeXMLGregorianCalendarMarshalling::toZonedDateTime);
    }

    @Property
    @PropertyLayout(
            sequence = "3.1",
            fieldSetId = "details",
            describedAs = "Replayable Action or Property, that was executed as captured by the original Command")
    public String getMember() {
        return commandRecord()
                .map(CommandRecord::commandDto)
                .map(CommandDto::getMember)
                .map(MemberDto::getLogicalMemberIdentifier)
                .map(TextUtils::cutter)
                .map(cutter -> cutter.keepAfter("#").getValue())
                .orElse(null);
    }

    @Property
    @PropertyLayout(
            sequence = "4",
            fieldSetId = "details",
            describedAs = "Replay State of the original (replayable) Command. "
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
            sequence = "4.1",
            fieldSetId = "details",
            describedAs = "Whether this command stores a result bookmark.")
    public boolean getHasResult() {
        return commandLogEntry()
                .map(CommandLogEntry::getResult)
                .isPresent();
    }

    @Property
    @PropertyLayout(
            sequence = "4.2",
            fieldSetId = "details",
            describedAs = "Whether this command is exportable from the current command export manager context.",
            hidden = Where.OBJECT_FORMS
    )
    public Boolean getExportable() {
        if (commandExportManager == null) {
            return null;
        }
        return commandLogEntry()
                .map(commandExportManager::isExportable)
                .orElse(null);
    }

    @Property
    @PropertyLayout(
            sequence = "9",
            fieldSetId = "dto",
            hidden = Where.ALL_TABLES,
            labelPosition = LabelPosition.NONE,
            describedAs = "Export DTO of the original (replayable) Command")
    public AsciiDoc getDto() {
        return commandLogEntry()
                .filter(commandLogEntry -> commandLogEntry.getCommandDto() != null)
                .map(commandLogEntry -> CommandDtoUtils.CommandExportDto.of(
                        commandLogEntry.getCommandDto(),
                        commandLogEntry.getResult()))
                .map(commandExportDto -> YamlUtils.toStringUtf8(commandExportDto,
                        JsonUtils::onlyIncludeNonNull))
                .map(ReplayableCommand::asYamlSourceBlock)
                .orElseGet(() -> new AsciiDoc("empty"));
    }

    private static AsciiDoc asYamlSourceBlock(final String yaml) {
        return new AsciiDocBuilder()
                .append(doc -> AsciiDocFactory.sourceBlock(doc, "yaml", yaml))
                .buildAsValue();
    }

    @Collection
    @CollectionLayout(sequence = "10", named = "Participants")
    public List<ReplayableCommandParticipant> getParticipants() {
        return commandLogEntry()
                .map(this::participantsFor)
                .orElseGet(List::of);
    }

    private List<ReplayableCommandParticipant> participantsFor(final CommandLogEntry commandLogEntry) {
        final List<ReplayableCommandParticipant> participants = new ArrayList<>();
        final CommandDto commandDto = commandLogEntry.getCommandDto();
        addTargetParticipants(participants, commandLogEntry, commandDto);
        addReferenceParameterParticipants(participants, commandLogEntry, commandDto);
        addResultParticipant(participants, commandLogEntry);
        return participants;
    }

    private void addTargetParticipants(
            final List<ReplayableCommandParticipant> participants,
            final CommandLogEntry commandLogEntry,
            final CommandDto commandDto) {
        Optional.ofNullable(commandDto)
                .map(CommandDto::getTargets)
                .stream()
                .flatMap(targets -> targets.getOid().stream())
                .forEach(target -> addParticipant(
                        participants,
                        commandLogEntry,
                        Role.TARGET,
                        null,
                        Bookmark.forOidDto(target)));
    }

    private void addReferenceParameterParticipants(
            final List<ReplayableCommandParticipant> participants,
            final CommandLogEntry commandLogEntry,
            final CommandDto commandDto) {
        if (commandDto == null || !(commandDto.getMember() instanceof ActionDto)) {
            return;
        }
        Optional.ofNullable(((ActionDto) commandDto.getMember()).getParameters())
                .stream()
                .flatMap(parameters -> parameters.getParameter().stream())
                .filter(parameter -> parameter.getType() == ValueType.REFERENCE)
                .filter(parameter -> parameter.getReference() != null)
                .forEach(parameter -> addParticipant(
                        participants,
                        commandLogEntry,
                        Role.PARAMETER,
                        parameter.getName(),
                        Bookmark.forOidDto(parameter.getReference())));
    }

    private void addParticipant(
            final List<ReplayableCommandParticipant> participants,
            final CommandLogEntry commandLogEntry,
            final Role role,
            final String parameterName,
            final Bookmark recordedBookmark) {
        participants.add(participant(
                commandLogEntry,
                role,
                parameterName,
                recordedBookmark,
                actualBookmarkFor(commandLogEntry, role, recordedBookmark).orElse(null)));
    }

    private void addResultParticipant(
            final List<ReplayableCommandParticipant> participants,
            final CommandLogEntry commandLogEntry) {
        if (commandLogEntry.getResult() == null) {
            return;
        }
        final Bookmark recordedResult = commandLogEntry.getResult();
        addParticipant(participants, commandLogEntry, Role.RESULT, null, recordedResult);
    }

    private Optional<Bookmark> actualBookmarkFor(
            final CommandLogEntry commandLogEntry,
            final Role role,
            final Bookmark recordedBookmark) {
        if (role == Role.TARGET && isDomainService(recordedBookmark)) {
            return Optional.of(recordedBookmark);
        }
        if (role == Role.TARGET || role == Role.PARAMETER) {
            return findActualBookmark(commandLogEntry, recordedBookmark)
                    .or(() -> commandLogEntry.getReplayState() == ReplayState.OK
                            || commandLogEntry.getReplayState() == ReplayState.UNDEFINED
                            || commandLogEntry.getReplayState() == ReplayState.EXPORTED
                            ? Optional.of(recordedBookmark)
                            : Optional.empty());
        }
        if (commandLogEntry.getReplayState() != ReplayState.OK) {
            return Optional.empty();
        }
        return findActualBookmark(commandLogEntry, recordedBookmark)
                .or(() -> Optional.of(recordedBookmark));
    }

    private boolean isDomainService(final Bookmark bookmark) {
        return bookmarkService != null
                && Optional.ofNullable(bookmarkService.lookup(bookmark))
                .flatMap(x -> x)
                .map(Object::getClass)
                .map(cls -> cls.isAnnotationPresent(org.apache.causeway.applib.annotation.DomainService.class))
                .orElse(false);
    }

    private ReplayableCommandParticipant participant(
            final CommandLogEntry commandLogEntry,
            final Role role,
            final String parameterName,
            final Bookmark recordedBookmark,
            final Bookmark actualBookmark) {
        final UUID owningInteractionId = commandLogEntry.getInteractionId() != null
                ? commandLogEntry.getInteractionId()
                : interactionId;
        return new ReplayableCommandParticipant(
                owningInteractionId,
                role,
                parameterName,
                recordedBookmark,
                actualBookmark,
                bookmarkService,
                replayContext);
    }

    private Optional<Bookmark> findActualBookmark(
            final CommandLogEntry commandLogEntry,
            final Bookmark recordedBookmark) {
        return _NullSafe.stream(replayContext.commandReplayMappingListeners())
                .map(listener -> lookupActualBookmark(listener, commandLogEntry, recordedBookmark))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private Optional<Bookmark> lookupActualBookmark(
            final CommandReplayMappingListener listener,
            final CommandLogEntry commandLogEntry,
            final Bookmark recordedBookmark) {
        try {
            return Optional.ofNullable(listener.lookup(commandLogEntry, recordedBookmark))
                    .orElseGet(Optional::empty);
        } catch (Exception ex) {
            log.warn("Command replay participant mapping listener failed", ex);
            return Optional.empty();
        }
    }

    // -- ACTIONS


    ReplayableCommand makeExportable() {
        if (disableMakeExportable() != null) {
            return this; // safeguard when called programmatically
        }
        commandLogEntry()
                .filter(commandLogEntry -> ReplayState.isExported(commandLogEntry.getReplayState()))
                .ifPresent(commandLogEntry -> {
                    commandLogEntry.setReplayState(ReplayState.UNDEFINED);
                    invalidateCachedRecord();
                });
        return this;
    }

    String disableMakeExportable() {
        return commandRecord()
                .map(rec -> ReplayState.isExported(rec.replayState()))
                .orElse(false)
                ? null
                : "Cannot make exportable, if not EXPORTED";
    }


    ReplayableCommand excludeFromReplay() {
        if (disableExcludeFromReplay() != null) {
            return ReplayableCommand.this; // safeguard when called programmatically
        }
        commandLogEntry()
                .filter(ReplayableCommand::canReplayOrRetryOrMarkForExclusion)
                .ifPresent(commandLogEntry -> {
                    commandLogEntry.setReplayState(ReplayState.EXCLUDED);
                    invalidateCachedRecord();
                });
        return ReplayableCommand.this;
    }

    String disableExcludeFromReplay() {
        return commandRecord()
                .map(CommandRecord::canReplayOrRetryOrMarkForExclusion)
                .orElse(false)
                ? null
                : "Cannot mark for exclusion, if neither PENDING nor FAILED";
    }


    @Programmatic
    void deleteObj() {
        commandLogEntry()
                .ifPresent(commandLogEntry -> {
                    replayContext.repositoryService().remove(commandLogEntry);
                    invalidateCachedRecord();
                });
    }


    // -- EXECUTION ORDER GOVERNED BY TIMESTAMP

    private static final Comparator<ReplayableCommand> TIMESTAMP_COMPARATOR =
            Comparator.nullsLast(Comparator.comparing(ReplayableCommand::getTimestamp));

    @Override
    public int compareTo(final ReplayableCommand other) {
        return TIMESTAMP_COMPARATOR.compare(this, other);
    }

    // -- VM STATE

    @Override
    public String viewModelMemento() {
        return interactionId.toString();
    }

    // -- UTIL

    Try<ReplayableCommand> tryReplayOrRetry() {
        if (disableReplayOrRetry() != null)
            return Try.success(null); // guard against disallowed invocation
        return commandLogEntry()
                .filter(ReplayableCommand::canReplayOrRetry)
                .map(this::commandDtoPossiblyRemappedForReplay)
                .map(commandDto -> tryReplay(commandDto)
                        .mapSuccessAsNullable(__ -> this))
                // if nothing to do, return with an 'empty success'
                .orElseGet(() -> Try.success(null));
    }

    String disableReplayOrRetry() {
        return commandRecord()
                .map(CommandRecord::canReplayOrRetry)
                .orElse(false)
                ? null
                : "Cannot replay or retry unless replay state is PENDING, OK, or FAILED";
    }

    ReplayableCommand previous() {
        return adjacentCommand(-1)
                .orElse(this);
    }

    String disablePrevious() {
        return adjacentCommand(-1).isPresent()
                ? null
                : "No previous command";
    }

    ReplayableCommand next() {
        return adjacentCommand(1)
                .orElse(this);
    }

    String disableNext() {
        return adjacentCommand(1).isPresent()
                ? null
                : "No next command";
    }

    private Optional<ReplayableCommand> adjacentCommand(final int direction) {
        return adjacentCommandLogEntry(direction)
                .map(CommandLogEntry::getInteractionId)
                .map(adjacentInteractionId -> new ReplayableCommand(adjacentInteractionId, replayContext));
    }

    private Optional<CommandLogEntry> adjacentCommandLogEntry(final int direction) {
        return commandLogEntry()
                .map(CommandLogEntry::getTimestamp)
                .flatMap(timestamp -> direction < 0
                        ? previousCommandLogEntry(timestamp)
                        : nextCommandLogEntry(timestamp));
    }

    private Optional<CommandLogEntry> previousCommandLogEntry(final Timestamp timestamp) {
        return replayContext.commandLogEntryRepository()
                .findForegroundBeforeTimestamp(timestamp, null)
                .stream()
                .filter(this::isReplayable)
                .findFirst();
    }

    private Optional<CommandLogEntry> nextCommandLogEntry(final Timestamp timestamp) {
        return replayContext.commandLogEntryRepository()
                .findForegroundSinceTimestamp(timestamp, null)
                .stream()
                .filter(entry -> !interactionId.equals(entry.getInteractionId()))
                .filter(this::isReplayable)
                .findFirst();
    }

    private boolean isReplayable(final CommandLogEntry entry) {
        return ReplayableCommandEligibility.isReplayable(entry, replayContext.specificationLoader());
    }

    // -- HELPER


    /**
     * Replays given command in its own transaction and handles {@link ReplayState} transition to
     * either {@link ReplayState#OK} or {@link ReplayState#FAILED}.
     */
    CommandDto commandDtoPossiblyRemappedForReplay(final CommandLogEntry commandLogEntry) {
        final CommandDto commandDto = copyCommandDto(commandLogEntry.getCommandDto());
        remapTargets(commandLogEntry, commandDto);
        remapReferenceParameters(commandLogEntry, commandDto);
        return commandDto;
    }

    private CommandDto copyCommandDto(final CommandDto commandDto) {
        return Try.call(() -> CommandDtoUtils.dtoMapper().read(CommandDtoUtils.dtoMapper().toString(commandDto)))
                .ifFailureFail()
                .getValue()
                .orElse(commandDto);
    }

    private void remapTargets(
            final CommandLogEntry commandLogEntry,
            final CommandDto commandDto) {
        Optional.ofNullable(commandDto.getTargets())
                .stream()
                .flatMap(targets -> targets.getOid().stream())
                .forEach(target -> remapTarget(commandLogEntry, target));
    }

    private void remapTarget(
            final CommandLogEntry commandLogEntry,
            final OidDto target) {
        final Bookmark recordedTarget = Bookmark.forOidDto(target);
        _NullSafe.stream(replayContext.commandReplayMappingListeners())
                .forEach(listener -> remapTarget(listener, commandLogEntry, target, recordedTarget));
    }

    private void remapTarget(
            final CommandReplayMappingListener listener,
            final CommandLogEntry commandLogEntry,
            final OidDto target,
            final Bookmark recordedTarget) {
        try {
            Optional.ofNullable(listener.lookup(commandLogEntry, recordedTarget))
                    .orElseGet(Optional::empty)
                    .ifPresent(replacement -> copyBookmarkToOidDto(replacement, target));
        } catch (Exception ex) {
            log.warn("Command replay target remapping listener failed", ex);
        }
    }

    private void remapReferenceParameters(
            final CommandLogEntry commandLogEntry,
            final CommandDto commandDto) {
        if (!(commandDto.getMember() instanceof ActionDto)) {
            return;
        }
        Optional.ofNullable(((ActionDto) commandDto.getMember()).getParameters())
                .stream()
                .flatMap(parameters -> parameters.getParameter().stream())
                .forEach(parameter -> remapReferenceParameter(commandLogEntry, parameter));
    }

    private void remapReferenceParameter(
            final CommandLogEntry commandLogEntry,
            final ParamDto parameter) {
        if (parameter.getType() != ValueType.REFERENCE || parameter.getReference() == null) {
            return;
        }
        final Bookmark recordedReference = Bookmark.forOidDto(parameter.getReference());
        _NullSafe.stream(replayContext.commandReplayMappingListeners())
                .forEach(listener -> remapReferenceParameter(
                        listener, commandLogEntry, parameter, recordedReference));
    }

    private void remapReferenceParameter(
            final CommandReplayMappingListener listener,
            final CommandLogEntry commandLogEntry,
            final ParamDto parameter,
            final Bookmark recordedReference) {
        try {
            Optional.ofNullable(listener.lookup(commandLogEntry, recordedReference))
                    .orElseGet(Optional::empty)
                    .ifPresent(replacement -> copyBookmarkToOidDto(replacement, parameter.getReference()));
        } catch (Exception ex) {
            log.warn("Command replay reference parameter remapping listener failed", ex);
        }
    }

    private void copyBookmarkToOidDto(
            final Bookmark bookmark,
            final OidDto oidDto) {
        oidDto.setType(bookmark.getLogicalTypeName());
        oidDto.setId(bookmark.getIdentifier());
    }

    private Try<Bookmark> tryReplay(final CommandDto commandDto) {
        var tryResultBookmark = replayContext.transactionService()
                .callTransactional(Propagation.REQUIRES_NEW, () -> {
                    final Bookmark actualResult = replayContext.commandExecutorService()
                            .executeCommand(InteractionContextPolicy.SWITCH_USER_AND_TIME, commandDto)
                            // if we have a replay failure, this throws, which will roll back the surrounding transaction
                            .valueAsNullableElseFail();
                    onReplaySuccess(actualResult);
                    return actualResult;
                });

        tryResultBookmark.ifFailure(ex -> replayContext.transactionService()
                .callTransactional(Propagation.REQUIRES_NEW, () -> {
                    onReplayError(ex);
                    return null;
                }));

        // in any outcome case (OK or FAILED) the ReplayState may have changed, hence invalidate local cache
        invalidateCachedRecord();

        return tryResultBookmark;
    }

    private void invalidateCachedRecord() {
        recordRef.update(__ -> null); // invalidate cache
    }

    private Optional<CommandRecord> commandRecord() {
        return Optional.ofNullable(recordRef.computeIfAbsent(() ->
                commandLogEntry()
                        .filter(commandLogEntry -> commandLogEntry.getCommandDto() != null)
                        .filter(commandLogEntry -> commandLogEntry.getReplayState() != null)
                        .map(commandLogEntry -> new CommandRecord(
                                commandLogEntry.getCommandDto(),
                                commandLogEntry.getReplayState()))
                        .orElse(null)));
    }

    Optional<CommandLogEntry> commandLogEntry() {
        return replayContext.lookupCommandLogEntry(interactionId());
    }

    private static boolean canReplayOrRetry(final CommandLogEntry commandLogEntry) {
        return ReplayState.isReplayOrRetryEnabled(commandLogEntry.getReplayState());
    }

    private static boolean canReplayOrRetryOrMarkForExclusion(final CommandLogEntry commandLogEntry) {
        return ReplayState.isPendingOrFailed(commandLogEntry.getReplayState());
    }

    /**
     * Handles the replay error case.
     */
    private void onReplayError(final Throwable ex) {
        commandLogEntry() // refetch from persistence
                .ifPresent(entry -> entry.saveAnalysis(ex.toString()));
    }

    /**
     * Handles the happy replay case.
     */
    private void onReplaySuccess(final Bookmark actualResult) {
        commandLogEntry() // refetch from persistence
                .ifPresent(entry -> {
                    entry.saveAnalysis(null);
                    notifyReplayResult(entry, actualResult);
                });
    }

    void notifyReplayResult(
            final CommandLogEntry commandLogEntry,
            final Bookmark actualResult) {
        final Bookmark recordedResult = commandLogEntry.getResult();
        if (recordedResult == null || actualResult == null) {
            return;
        }
        _NullSafe.stream(replayContext.commandReplayMappingListeners())
                .forEach(listener -> notifyReplayResult(listener, recordedResult, actualResult, commandLogEntry));
    }

    private void notifyReplayResult(
            final CommandReplayMappingListener listener,
            final Bookmark recordedResult,
            final Bookmark actualResult,
            final CommandLogEntry commandLogEntry) {
        listener.onReplayResult(recordedResult, actualResult, commandLogEntry);
    }

    @Inject BookmarkService bookmarkService;
}