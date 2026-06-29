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

import javax.inject.Named;

import org.apache.causeway.applib.Identifier;
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
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.jaxb.JavaTimeXMLGregorianCalendarMarshalling;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.command.CommandExecutorService.InteractionContextPolicy;
import org.apache.causeway.applib.services.command.CommandRecordingSuppressed;
import org.apache.causeway.applib.services.queryresultscache.QueryResultsCacheControl;
import org.apache.causeway.applib.services.wrapper.DisabledException;
import org.apache.causeway.applib.services.wrapper.HiddenException;
import org.apache.causeway.applib.services.wrapper.InvalidException;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Refs.ObjectReference;
import org.apache.causeway.commons.io.JsonUtils;
import org.apache.causeway.commons.io.TextUtils;
import org.apache.causeway.commons.io.YamlUtils;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.IdentifierUtil;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.MemberDto;
import org.apache.causeway.schema.common.v2.OidsDto;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.ReplayableCommandParticipant.Role;
import org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc;
import org.apache.causeway.valuetypes.asciidoc.builder.AsciiDocBuilder;
import org.apache.causeway.valuetypes.asciidoc.builder.AsciiDocFactory;

import org.jspecify.annotations.NonNull;

import org.springframework.transaction.annotation.Propagation;

import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
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


    private final ObjectReference<CommandRecord> recordRef = new ObjectReference<>(null);


    private final ReplayableCommandParticipantTracker replayableCommandParticipantTracker;

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

    ReplayableCommand(
            final UUID interactionId,
            final ReplayContext replayContext) {
        this.interactionId = interactionId;
        this.replayContext = replayContext;
        this.replayableCommandParticipantTracker = ReplayableCommandParticipantTracker.current(replayContext.scratchpad()).orElse(null);
    }

    @ObjectSupport
    public String title() {
        final var timestamp = getTimestampIfAny().map(ChronoZonedDateTime::toInstant).map(Instant::toString).map(x -> " @ " + x).orElse("");
        return targetTitlePrefix() + " #" + getMember() + timestamp;
    }

    private String targetTitlePrefix() {
        return targetBookmarkIfAny()
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
            sequence = "3.0",
            fieldSetId = "details",
            hidden = Where.PARENTED_TABLES,
            describedAs = "Target of the command")
    public String getTarget() {
        return targetBookmarkIfAny()
                .orElse(null);
    }

    @Property
    @PropertyLayout(
            named = "Target",
            sequence = "3.0",
            fieldSetId = "details",
            hidden = Where.OBJECT_FORMS,
            describedAs = "Target of the command")
    public String getTargetAbbreviated() {
        return targetBookmarkIfAny()
                .map(ReplayableCommand::abbreviatedIfRequired)
                .orElse(null);
    }

    private static @NonNull String abbreviatedIfRequired(String x) {
        final var abbreviateIfLongerThan = 80;
        return x.length() > abbreviateIfLongerThan ? x.substring(0, abbreviateIfLongerThan) + "..." : x;
    }

    private @NonNull Optional<String> targetBookmarkIfAny() {
        return commandRecord()
                .map(CommandRecord::commandDto)
                .map(CommandDto::getTargets)
                .map(OidsDto::getOid)   // returns a list of OidDto's, in fact
                .flatMap(oidDtoList -> Optional.ofNullable(oidDtoList.isEmpty() ? null : oidDtoList.get(0)))
                .map(Bookmark::forOidDto)
                .map(Bookmark::stringify);
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
            describedAs = "Whether this command uses only known participants as target or action params.  This determines whether the command is exportable/replayable in context of all commands since a baseline.",
            hidden = Where.OBJECT_FORMS
    )
    public boolean isKnownParticipants() {
        if (replayableCommandParticipantTracker == null) {
            return false;
        }
        return commandLogEntry()
                .map(replayableCommandParticipantTracker::isKnownParticipants)
                .orElse(false);
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
        addActualTargetParticipants(participants, commandDto, commandLogEntry);
        addActualReferenceParameterParticipants(participants, commandLogEntry, commandDto);
        addActualResultParticipant(participants, commandLogEntry);
        return participants;
    }

    private void addActualTargetParticipants(
            final List<ReplayableCommandParticipant> participants,
            final CommandDto commandDto,
            final CommandLogEntry commandLogEntry) {
        Optional.ofNullable(commandDto)
                .map(CommandDto::getTargets)
                .stream()
                .flatMap(targets -> targets.getOid().stream())
                .forEach(target -> addActualParticipant(
                        participants,
                        commandLogEntry,
                        Role.TARGET,
                        null,
                        Bookmark.forOidDto(target)));
    }

    private void addActualReferenceParameterParticipants(
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
                .forEach(parameter -> addActualParticipant(
                        participants,
                        commandLogEntry,
                        Role.PARAMETER,
                        parameter.getName(),
                        Bookmark.forOidDto(parameter.getReference())));
    }

    private void addActualParticipant(
            final List<ReplayableCommandParticipant> participants,
            final CommandLogEntry commandLogEntry,
            final Role role,
            final String parameterName,
            final Bookmark recordedBookmark) {
        final var replayState = commandLogEntry.getReplayState();
        participants.add(participant(
                commandLogEntry,
                role,
                parameterName,
                recordedBookmark,
                actualBookmarkFor(recordedBookmark, replayState).orElse(null)));
    }

    private void addActualResultParticipant(
            final List<ReplayableCommandParticipant> participants,
            final CommandLogEntry commandLogEntry) {
        if (commandLogEntry.getResult() == null) {
            return;
        }
        final Bookmark recordedResult = commandLogEntry.getResult();
        addActualParticipant(participants, commandLogEntry, Role.RESULT, null, recordedResult);
    }

    private Optional<Bookmark> actualBookmarkFor(
            final Bookmark recordedBookmark, ReplayState replayState) {

        final var resultRemappingService = replayContext().resultRemappingService();

        return resultRemappingService.findActualBookmark(recordedBookmark)
                .or(() -> replayState.isExecutedOk()
                        ? Optional.of(recordedBookmark)
                        : Optional.empty());

    }

    private boolean isDomainService(final Bookmark bookmark) {
        final var bookmarkService = replayContext.bookmarkService();
        return Optional.ofNullable(bookmarkService.lookup(bookmark))
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
                replayContext
        );
    }


    // -- ACTIONS


    ReplayableCommand unexclude(ReplayState replayState) {
        if(getReplayState() != ReplayState.EXCLUDED) {
            return this; // safeguard when called programmatically
        }
        commandLogEntry()
                .ifPresent(commandLogEntry -> {
                    commandLogEntry.setReplayState(replayState);
                    invalidateCachedRecord();
                });
        return this;
    }


    ReplayableCommand exclude() {
        if (getReplayState() == ReplayState.EXCLUDED) {
            return ReplayableCommand.this; // safeguard when called programmatically
        }
        commandLogEntry()
                .ifPresent(commandLogEntry -> {
                    commandLogEntry.setReplayState(ReplayState.EXCLUDED);
                    invalidateCachedRecord();
                });
        return ReplayableCommand.this;
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
        if (!commandRecord()
                .map(CommandRecord::canReplayOrRetry)
                .orElse(false)) {
            return "Cannot replay or retry unless replay state is PENDING, OK, or FAILED";
        }
        return ReplayPendingBackgroundCommands.disableReason(replayContext);
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
                .filter(this::isDoOp)
                .findFirst();
    }

    private Optional<CommandLogEntry> nextCommandLogEntry(final Timestamp timestamp) {
        return replayContext.commandLogEntryRepository()
                .findForegroundSinceTimestamp(timestamp, null)
                .stream()
                .filter(entry -> !interactionId.equals(entry.getInteractionId()))
                .filter(this::isDoOp)
                .findFirst();
    }

    private boolean isDoOp(final CommandLogEntry entry) {
        return Util.isDoOp(entry, replayContext.specificationLoader());
    }

    // -- HELPER


    /**
     * Replays given command in its own transaction and handles {@link ReplayState} transition to
     * either {@link ReplayState#OK} or {@link ReplayState#FAILED}.
     */
    CommandDto commandDtoPossiblyRemappedForReplay(final CommandLogEntry commandLogEntry) {
        return replayContext().resultRemappingService().remapped(commandLogEntry.getCommandDto());
    }

    private Try<Bookmark> tryReplay(final CommandDto commandDto) {
        final var queryResultsCache = replayContext.queryResultsCache();
        var tryResultBookmark = replayContext.transactionService().callTransactional(Propagation.REQUIRES_NEW, () -> {
                    queryResultsCache.onTransactionEnded(); // clear the cache
                    final Bookmark actualResult = replayContext.commandExecutorService()
                            // calls transactionService which uses its own try to potentially mark _this_ transaction for rollback.
                            .executeCommand(InteractionContextPolicy.SWITCH_USER_AND_TIME, commandDto)
                            // if we have a replay failure, this throws, which will also roll back the surrounding transaction
                            .valueAsNullableElseFail();
                    onReplaySuccess(actualResult);
                    return actualResult;
                })
                .mapFailureToSuccess(ex -> {
                    // use a new transaction to record the failure.
                    replayContext.transactionService().runTransactional(Propagation.REQUIRES_NEW, () -> {
                        queryResultsCache.onTransactionEnded(); // clear the cache
                        onReplayError(ex);
                    });
                    return null;
                });

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

    /**
     * Handles the replay error case.
     */
    private void onReplayError(final Throwable ex) {
        commandLogEntry() // refetch from persistence
                .ifPresent(entry -> {
                    String prefix = "";
                    if(ex instanceof HiddenException) {
                        prefix = "Disabled: ";
                    } else if(ex instanceof DisabledException) {
                        prefix = "Disabled: ";
                    } else if(ex instanceof InvalidException) {
                        prefix = "Invalid: ";
                    }
                    entry.saveAnalysis(prefix + ex.getMessage());
                });
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
        replayContext.resultRemappingService().notifyReplayResult(recordedResult, actualResult, commandLogEntry.getInteractionId());
    }

    @UtilityClass
    static class Util {

        boolean isDoOp(
                final CommandLogEntry entry,
                final SpecificationLoader specificationLoader) {
            if (entry == null) {
                return false;
            }
            if (!isSafeAction(entry, specificationLoader)) {
                return true;
            }
            return entry.getResult() != null;
        }

        private boolean isSafeAction(
                final CommandLogEntry entry,
                final SpecificationLoader specificationLoader) {
            if (specificationLoader == null
                    || entry.getCommandDto() == null
                    || !(entry.getCommandDto().getMember() instanceof ActionDto)) {
                return false;
            }
            return Optional.ofNullable(entry.getLogicalMemberIdentifier())
                    .flatMap(logicalMemberIdentifier -> safeActionSemantics(specificationLoader, logicalMemberIdentifier))
                    .orElse(false);
        }

        private Optional<Boolean> safeActionSemantics(
                final SpecificationLoader specificationLoader,
                final String logicalMemberIdentifier) {
            try {
                final var identifier = IdentifierUtil.memberIdentifierFor(
                        specificationLoader,
                        Identifier.Type.ACTION,
                        logicalMemberIdentifier);
                return specificationLoader.loadFeature(identifier)
                        .filter(ObjectAction.class::isInstance)
                        .map(ObjectAction.class::cast)
                        .map(ObjectAction::getSemantics)
                        .map(SemanticsOf::isSafeInNature);
            } catch (final RuntimeException ex) {
                return Optional.empty();
            }
        }
    }
}