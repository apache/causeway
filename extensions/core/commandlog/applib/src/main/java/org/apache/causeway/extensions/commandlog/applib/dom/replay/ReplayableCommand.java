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

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.transaction.annotation.Propagation;

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
import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.jaxb.JavaTimeXMLGregorianCalendarMarshalling;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.command.CommandRecordingSuppressed;
import org.apache.causeway.applib.services.command.CommandExecutorService.InteractionContextPolicy;
import org.apache.causeway.applib.services.wrapper.DisabledException;
import org.apache.causeway.applib.services.wrapper.HiddenException;
import org.apache.causeway.applib.services.wrapper.InvalidException;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Refs.ObjectReference;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.io.JsonUtils;
import org.apache.causeway.commons.io.TextUtils;
import org.apache.causeway.commons.io.YamlUtils;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.ReplayableCommandParticipant.Role;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.MemberDto;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc;
import org.apache.causeway.valuetypes.asciidoc.builder.AsciiDocBuilder;
import org.apache.causeway.valuetypes.asciidoc.builder.AsciiDocFactory;

import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Viewmodel that wraps a {@link CommandLogEntry}.
 */
@DomainObject(introspection = Introspection.ANNOTATION_REQUIRED)
@DomainObjectLayout//(cssClassFa = "terminal")
@Named(ReplayableCommand.LOGICAL_TYPE_NAME)
public final class ReplayableCommand
implements ViewModel, Comparable<ReplayableCommand>, CommandRecordingSuppressed {

    public static abstract class ActionDomainEvent<T>
            extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<T> { }

    private final UUID interactionId;
    @Programmatic
    public UUID interactionId() { return interactionId; }

	private final ReplayContext replayContext;
    @Programmatic
    public ReplayContext replayContext() { return replayContext; }

    private final ReplayableCommandParticipantTracker participantTracker;


	private final ObjectReference<CommandRecord> recordRef;
    @Programmatic
    public ObjectReference<CommandRecord> recordRef() { return recordRef; }

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE + ".ReplayableCommand";

    // decoupled from the underlying entity
    @Value @Accessors(fluent = true)
    final static class CommandRecord {

	    final CommandDto commandDto;
	    final ReplayState replayState;

        boolean canReplayOrRetryOrMarkForExclusion() {
            return replayState.isPendingOrFailed();
        }
        boolean canReplayOrRetry() {
            return replayState.isReplayable();
        }
        public String faQuickIcon() {
            return switch (replayState) {
                case UNDEFINED -> "solid terminal .col-indigo";
                case EXPORTED -> "solid terminal .col-indigo, solid circle-arrow-right .ov-size-80 .ov-right-45 .ov-bottom-45 .col-dodgerblue";
                case PENDING -> "solid terminal .col-indigo, solid circle-pause       .ov-size-80 .ov-right-45 .ov-bottom-45 .col-gold";
                case OK -> "solid terminal .col-indigo, solid circle-check       .ov-size-80 .ov-right-45 .ov-bottom-45 .col-green";
                case FAILED -> "solid terminal .col-indigo, solid circle-exclamation .ov-size-80 .ov-right-45 .ov-bottom-45 .col-red";
                case EXCLUDED -> "solid terminal .col-indigo, solid circle-xmark       .ov-size-80 .ov-right-45 .ov-bottom-45 .col-grey";
            };
        }
    }

    @Inject
    public ReplayableCommand(
            final String memento,
            final ReplayContext replayContext) {
        this(UUID.fromString(memento), replayContext);
    }

    ReplayableCommand(
            final UUID interactionId,
            final ReplayContext replayContext) {
        this(interactionId, replayContext, null);
    }

    ReplayableCommand(
            final UUID interactionId,
            final ReplayContext replayContext,
            final ReplayableCommandParticipantTracker participantTracker) {
        this.interactionId = interactionId;
        this.replayContext = replayContext;
        this.participantTracker = participantTracker;
        this.recordRef = new ObjectReference<>(null);
    }

    @ObjectSupport public String title() {
        final var timestamp = getTimestampIfAny().map(ChronoZonedDateTime::toInstant).map(Instant::toString).map(x -> " @ " + x).orElse("");
        return getTargetType() + ":" + getTargetId() + " #" + getMember() + timestamp;
    }

    @ObjectSupport public ObjectSupport.IconResource icon(final ObjectSupport.IconSize iconSize) {
        return commandRecord()
                .map(CommandRecord::faQuickIcon)
                .map(FontAwesomeLayers::fromQuickNotation)
                .map(ObjectSupport.FontAwesomeIconResource::new)
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

    @Programmatic
    public String getTargetType() {
        return commandRecord()
            .map(CommandRecord::commandDto)
            .map(commandDto->commandDto.getTargets().getOid().get(0))
            .map(OidDto::getType)
            .orElse(null);
    }

    @Programmatic
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
            describedAs = "Replayable Action or Property, that was executed as captured by the original Command")
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
            sequence = "5",
            fieldSetId = "details",
            describedAs = "Whether this command stores a recorded result bookmark")
    public boolean getHasResult() {
        return commandLogEntry()
                .map(CommandLogEntry::getResult)
                .isPresent();
    }

    @Property
    @PropertyLayout(
            sequence = "5.1",
            fieldSetId = "details",
            hidden = Where.OBJECT_FORMS,
            describedAs = "Whether this command uses only participants known at this point in the manager sequence")
    public boolean isKnownParticipants() {
        if (participantTracker == null) {
            return false;
        }
        return commandLogEntry()
                .map(participantTracker::isKnownParticipants)
                .orElse(false);
    }

    @Collection
    @CollectionLayout(sequence = "6", named = "Participants")
    public List<ReplayableCommandParticipant> getParticipants() {
        return commandLogEntry()
                .map(this::participantsFor)
                .orElseGet(List::of);
    }

    private List<ReplayableCommandParticipant> participantsFor(final CommandLogEntry commandLogEntry) {
        var participants = new ArrayList<ReplayableCommandParticipant>();
        var commandDto = commandLogEntry.getCommandDto();
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
                .filter(java.util.Objects::nonNull)
                .map(Bookmark::forOidDto)
                .forEach(bookmark -> participants.add(participant(
                        commandLogEntry, Role.TARGET, null, bookmark)));
    }

    private void addReferenceParameterParticipants(
            final List<ReplayableCommandParticipant> participants,
            final CommandLogEntry commandLogEntry,
            final CommandDto commandDto) {
        if (commandDto == null || !(commandDto.getMember() instanceof ActionDto actionDto)) {
            return;
        }
        Optional.ofNullable(actionDto.getParameters())
                .stream()
                .flatMap(parameters -> parameters.getParameter().stream())
                .filter(parameter -> parameter != null
                        && parameter.getType() == ValueType.REFERENCE
                        && parameter.getReference() != null)
                .forEach(parameter -> participants.add(participant(
                        commandLogEntry,
                        Role.PARAMETER,
                        parameter.getName(),
                        Bookmark.forOidDto(parameter.getReference()))));
    }

    private void addResultParticipant(
            final List<ReplayableCommandParticipant> participants,
            final CommandLogEntry commandLogEntry) {
        if (commandLogEntry.getResult() != null) {
            participants.add(participant(
                    commandLogEntry, Role.RESULT, null, commandLogEntry.getResult()));
        }
    }

    private ReplayableCommandParticipant participant(
            final CommandLogEntry commandLogEntry,
            final Role role,
            final String parameterName,
            final Bookmark recordedBookmark) {
        var owningInteractionId = commandLogEntry.getInteractionId() != null
                ? commandLogEntry.getInteractionId()
                : interactionId;
        var actualBookmark = actualBookmarkFor(
                role, recordedBookmark, commandLogEntry.getReplayState()).orElse(null);
        return new ReplayableCommandParticipant(
                owningInteractionId,
                role,
                parameterName,
                recordedBookmark,
                actualBookmark,
                replayContext);
    }

    private Optional<Bookmark> actualBookmarkFor(
            final Role role,
            final Bookmark recordedBookmark,
            final ReplayState replayState) {
        if (role == Role.RESULT && replayState != ReplayState.OK) {
            return Optional.empty();
        }
        return replayContext.resultRemappingService().lookup(recordedBookmark)
                .or(() -> replayState == ReplayState.OK
                        ? Optional.of(recordedBookmark)
                        : Optional.empty());
    }

    @Property
    @PropertyLayout(
            sequence = "9",
            fieldSetId = "dto",
            hidden = Where.ALL_TABLES,
            labelPosition = LabelPosition.NONE,
            describedAs = "DTO of the original (replayable) Command")
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

    ReplayableCommand previous() {
        return adjacent(false).orElse(this);
    }

    String disablePrevious() {
        return adjacent(false).isEmpty() ? "No previous replayable command" : null;
    }

    ReplayableCommand next() {
        return adjacent(true).orElse(this);
    }

    String disableNext() {
        return adjacent(true).isEmpty() ? "No next replayable command" : null;
    }

    private Optional<ReplayableCommand> adjacent(final boolean forward) {
        return getTimestampIfAny()
                .map(ChronoZonedDateTime::toInstant)
                .map(Timestamp::from)
                .stream()
                .flatMap(timestamp -> (forward
                        ? replayContext.commandLogEntryRepository()
                                .findForegroundSinceTimestamp(timestamp)
                        : replayContext.commandLogEntryRepository()
                                .findForegroundBeforeTimestamp(timestamp, null))
                        .stream())
                .filter(entry -> !interactionId.equals(entry.getInteractionId()))
                .filter(entry -> ReplayableCommandEligibility.isEligible(
                        entry, replayContext.applicationFeatureRepository()))
                .findFirst()
                .map(entry -> new ReplayableCommand(entry.getInteractionId(), replayContext));
    }


    ReplayableCommand makeExportable() {
        if(disableMakeExportable()!=null)
            return this; // safeguard when called programmatically
        commandLogEntry()
                .filter(commandLogEntry->ReplayState.isExported(commandLogEntry.getReplayState()))
                .ifPresent(commandLogEntry->{
                    commandLogEntry.setReplayState(ReplayState.UNDEFINED);
                    invalidateCachedRecord();
                });
        return this;
    }
    String disableMakeExportable() {
        return commandRecord()
                .map(rec->ReplayState.isExported(rec.replayState()))
                .orElse(false)
                ? null
                : "Cannot make exportable, if not EXPORTED";
    }


    ReplayableCommand excludeFromReplay() {
        if(disableExcludeFromReplay()!=null)
            return ReplayableCommand.this; // safeguard when called programmatically
        commandLogEntry()
                .filter(ReplayableCommand::canReplayOrRetryOrMarkForExclusion)
                .ifPresent(commandLogEntry->{
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
                .ifPresent(commandLogEntry->{
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
        if(disableReplayOrRetry()!=null)
            return Try.success(null); // guard against disallowed invocation
        return commandLogEntry()
            .filter(ReplayableCommand::canReplayOrRetry)
            .map(commandLogEntry -> tryReplay(
                    replayContext.resultRemappingService().remapped(commandLogEntry.getCommandDto()))
                .mapSuccessAsNullable(__ -> this))
            // if nothing to do, return with an 'empty success'
            .orElseGet(()->Try.success(null));
    }

    String disableReplayOrRetry() {
        final boolean replayable = commandRecord()
                .map(CommandRecord::canReplayOrRetry)
                .orElse(false);
        return replayable
                ? ReplayPendingBackgroundCommands.disableReason(replayContext)
                : "Cannot replay, unless PENDING, OK or FAILED";
    }

    // -- HELPER

    /**
     * Replays given command in its own transaction and handles {@link ReplayState} transition to
     * either {@link ReplayState#OK} or {@link ReplayState#FAILED}.
     */
    private Try<Bookmark> tryReplay(final CommandDto commandDto) {
        var tryResultBookmark = replayContext.transactionService()
            .callTransactional(Propagation.REQUIRES_NEW, () -> {
                var actualResult = replayContext.commandExecutorService()
                    .executeCommand(InteractionContextPolicy.SWITCH_USER_AND_TIME, commandDto)
                    // if we have a replay failure, this throws, which will roll back the surrounding transaction
                    .valueAsNullableElseFail();
                onReplaySuccess(actualResult);
                return actualResult;
            })
            // a handled replay failure is recorded (in its own transaction) and mapped to a successful
            // outcome, so that a bounded/multiple replay continues to the next command rather than halting
            // the whole batch on the first failure.
            .mapFailureToSuccess(ex -> {
                replayContext.transactionService()
                    .runTransactional(Propagation.REQUIRES_NEW, () -> onReplayError(ex));
                return null;
            });

        // in any outcome case (OK or FAILED) the ReplayState may have changed, hence invalidate local cache
        invalidateCachedRecord();

        return tryResultBookmark;
    }

    private void invalidateCachedRecord() {
        recordRef.update(__->null); // invalidate cache
    }

    private Optional<CommandRecord> commandRecord() {
        return Optional.ofNullable(recordRef.computeIfAbsent(()->
            commandLogEntry()
                .filter(commandLogEntry->commandLogEntry.getCommandDto()!=null)
                .filter(commandLogEntry->commandLogEntry.getReplayState()!=null)
                .map(commandLogEntry->new CommandRecord(
                        commandLogEntry.getCommandDto(),
                        commandLogEntry.getReplayState()))
                .orElse(null)));
    }

    Optional<CommandLogEntry> commandLogEntry() {
        return replayContext.lookupCommandLogEntry(interactionId());
    }

    private static boolean canReplayOrRetryOrMarkForExclusion(final CommandLogEntry commandLogEntry) {
        return ReplayState.isPendingOrFailed(commandLogEntry.getReplayState());
    }

    private static boolean canReplayOrRetry(final CommandLogEntry commandLogEntry) {
        return ReplayState.isReplayable(commandLogEntry.getReplayState());
    }

    /**
     * Handles the replay error case: records the failure analysis on the entry (which persists the
     * {@link ReplayState#FAILED} state, the failure reason, and the exception), classified with a typed
     * prefix so the recorded reason distinguishes a hidden/disabled target from invalid input.
     */
    private void onReplayError(final Throwable ex) {
        commandLogEntry() // refetch from persistence
            .ifPresent(entry->entry.saveAnalysis(typedAnalysisPrefix(ex) + messageOf(ex)));
    }
    private static String typedAnalysisPrefix(final Throwable ex) {
        if(ex instanceof HiddenException
                || ex instanceof DisabledException) return "Disabled: ";
        if(ex instanceof InvalidException) return "Invalid: ";
        return "";
    }
    private static String messageOf(final Throwable ex) {
        return ex.getMessage() != null
                ? ex.getMessage()
                : ex.toString();
    }
    /**
     * Handles the happy replay case.
     */
    private void onReplaySuccess(final Bookmark actualResult) {
        commandLogEntry() // refetch from persistence
            .ifPresent(entry -> {
                entry.saveAnalysis(null);
                replayContext.resultRemappingService().notifyReplayResult(
                    entry.getResult(),
                    actualResult,
                    entry.getInteractionId());
            });
    }

}
