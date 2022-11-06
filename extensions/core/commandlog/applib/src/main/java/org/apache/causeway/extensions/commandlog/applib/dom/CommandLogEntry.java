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
package org.apache.causeway.extensions.commandlog.applib.dom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.Digits;

import org.apache.causeway.applib.annotation.*;
import org.apache.causeway.commons.internal.base._Casts;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.causeway.applib.mixins.system.DomainChangeRecord;
import org.apache.causeway.applib.mixins.system.HasInteractionId;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.command.CommandOutcomeHandler;
import org.apache.causeway.applib.services.commanddto.HasCommandDto;
import org.apache.causeway.applib.services.commanddto.conmap.UserDataKeys;
import org.apache.causeway.applib.services.tablecol.TableColumnOrderForCollectionTypeAbstract;
import org.apache.causeway.applib.util.ObjectContracts;
import org.apache.causeway.applib.util.TitleBuffer;
import org.apache.causeway.applib.util.ToString;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.base._Temporals;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.MapDto;

import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * A persistent representation of a {@link Command}, being the intention to edit a property or invoke an action.
 *
 * <p>
 *     Use cases requiring persistence including auditing, and for replay of
 *     commands for regression testing purposes.
 * </p>
 *
 * Note that this class doesn't subclass from {@link Command} ({@link Command}
 * is not an interface), but it does implement {@link HasCommandDto}, providing access to
 * {@link CommandDto}, a serialized representation of the {@link Command}.
 *
 * @since 2.x {@index}
 */
@Named(CommandLogEntry.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED,
        entityChangePublishing = Publishing.DISABLED
)
@DomainObjectLayout(
        titleUiEvent = CommandLogEntry.TitleUiEvent.class,
        iconUiEvent = CommandLogEntry.IconUiEvent.class,
        cssClassUiEvent = CommandLogEntry.CssClassUiEvent.class,
        layoutUiEvent = CommandLogEntry.LayoutUiEvent.class
)
@NoArgsConstructor
public abstract class CommandLogEntry
implements Comparable<CommandLogEntry>, DomainChangeRecord, HasCommandDto {

    public final static String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandLogEntry";
    public static final String SCHEMA = CausewayModuleExtCommandLogApplib.SCHEMA;
    public static final String TABLE = "CommandLogEntry";

    public static class TitleUiEvent extends CausewayModuleExtCommandLogApplib.TitleUiEvent<CommandLogEntry> { }
    public static class IconUiEvent extends CausewayModuleExtCommandLogApplib.IconUiEvent<CommandLogEntry> { }
    public static class CssClassUiEvent extends CausewayModuleExtCommandLogApplib.CssClassUiEvent<CommandLogEntry> { }
    public static class LayoutUiEvent extends CausewayModuleExtCommandLogApplib.LayoutUiEvent<CommandLogEntry> { }

    public static abstract class PropertyDomainEvent<T> extends CausewayModuleExtCommandLogApplib.PropertyDomainEvent<CommandLogEntry, T> { }
    public static abstract class CollectionDomainEvent<T> extends CausewayModuleExtCommandLogApplib.CollectionDomainEvent<CommandLogEntry, T> { }
    public static abstract class ActionDomainEvent extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<CommandLogEntry> { }

    @UtilityClass
    public static class Nq {
        public static final String FIND_BY_INTERACTION_ID               = LOGICAL_TYPE_NAME + ".findByInteractionId";
        public static final String FIND_BY_PARENT_INTERACTION_ID        = LOGICAL_TYPE_NAME + ".findByParentInteractionId";
        public static final String FIND_CURRENT                         = LOGICAL_TYPE_NAME + ".findCurrent";
        public static final String FIND_COMPLETED                       = LOGICAL_TYPE_NAME + ".findCompleted";
        public static final String FIND_RECENT_BY_TARGET                = LOGICAL_TYPE_NAME + ".findRecentByTarget";
        public static final String FIND_RECENT_BY_TARGET_OR_RESULT      = LOGICAL_TYPE_NAME + ".findRecentByTargetOrResult";
        public static final String FIND_BY_TARGET_AND_TIMESTAMP_BETWEEN = LOGICAL_TYPE_NAME + ".findByTargetAndTimestampBetween";
        public static final String FIND_BY_TARGET_AND_TIMESTAMP_AFTER   = LOGICAL_TYPE_NAME + ".findByTargetAndTimestampAfter";
        public static final String FIND_BY_TARGET_AND_TIMESTAMP_BEFORE  = LOGICAL_TYPE_NAME + ".findByTargetAndTimestampBefore";
        public static final String FIND_BY_TARGET                       = LOGICAL_TYPE_NAME + ".findByTarget";
        public static final String FIND_BY_TIMESTAMP_BETWEEN            = LOGICAL_TYPE_NAME + ".findByTimestampBetween";
        public static final String FIND_BY_TIMESTAMP_AFTER              = LOGICAL_TYPE_NAME + ".findByTimestampAfter";
        public static final String FIND_BY_TIMESTAMP_BEFORE             = LOGICAL_TYPE_NAME + ".findByTimestampBefore";
        public static final String FIND                                 = LOGICAL_TYPE_NAME + ".find";
        public static final String FIND_MOST_RECENT                     = LOGICAL_TYPE_NAME + ".findMostRecent";
        public static final String FIND_RECENT_BY_USERNAME              = LOGICAL_TYPE_NAME + ".findRecentByUsername";
        public static final String FIND_FIRST                           = LOGICAL_TYPE_NAME + ".findFirst";
        public static final String FIND_SINCE                           = LOGICAL_TYPE_NAME + ".findSince";
        /**
         * The most recent (replayed) command previously replicated from primary to secondary.
         *
         * <p>
         *     This should always exist except for the very first times (after restored the prod DB to secondary).
         * </p>
         */
        public static final String FIND_MOST_RECENT_REPLAYED            = LOGICAL_TYPE_NAME + ".findMostRecentReplayed";
        /**
         * The most recent completed command, as queried on the secondary, corresponding to the last command run on
         * primary before the production database was restored to the secondary.
         */
        public static final String FIND_MOST_RECENT_COMPLETED           = LOGICAL_TYPE_NAME + ".findMostRecentCompleted";
        public static final String FIND_BY_REPLAY_STATE                 = LOGICAL_TYPE_NAME + ".findNotYetReplayed";
        public static final String FIND_BACKGROUND_AND_NOT_YET_STARTED  = LOGICAL_TYPE_NAME + ".findBackgroundAndNotYetStarted";
        public static final String FIND_RECENT_BACKGROUND_BY_TARGET     = LOGICAL_TYPE_NAME + ".findRecentBackgroundByTarget";
    }


    @Programmatic
    public void init(final Command command) {

        setInteractionId(command.getInteractionId());
        setUsername(command.getUsername());
        setTimestamp(command.getTimestamp());

        setCommandDto(command.getCommandDto());
        setTarget(command.getTarget());
        setLogicalMemberIdentifier(command.getLogicalMemberIdentifier());

        setStartedAt(command.getStartedAt());
        setCompletedAt(command.getCompletedAt());

        setResult(command.getResult());
        setException(command.getException());

        setReplayState(org.apache.causeway.extensions.commandlog.applib.dom.ReplayState.UNDEFINED);
    }


    /**
     * Intended for use on secondary (replay) system.
     *
     * @param commandDto - obtained from the primary system as a representation of a command invocation
     * @param replayState - controls whether this is to be replayed
     * @param targetIndex - if the command represents a bulk action, then it is flattened out when replayed; this indicates which target to execute against.
     */
    public CommandLogEntry(
            final CommandDto commandDto,
            final org.apache.causeway.extensions.commandlog.applib.dom.ReplayState replayState,
            final int targetIndex) {

        setInteractionId(UUID.fromString(commandDto.getInteractionId()));
        setUsername(commandDto.getUsername());
        setTimestamp(JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(commandDto.getTimestamp()));

        setCommandDto(commandDto);
        setTarget(Bookmark.forOidDto(commandDto.getTargets().getOid().get(targetIndex)));
        setLogicalMemberIdentifier(commandDto.getMember().getLogicalMemberIdentifier());

        // the hierarchy of commands calling other commands is only available on the primary system.
        setParentInteractionId(null);

        setStartedAt(JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(commandDto.getTimings().getStartedAt()));
        setCompletedAt(JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(commandDto.getTimings().getCompletedAt()));

        copyOver(commandDto, UserDataKeys.RESULT, value -> this.setResult(Bookmark.parse(value).orElse(null)));
        copyOver(commandDto, UserDataKeys.EXCEPTION, this::setException);

        setReplayState(replayState);
    }

    static void copyOver(
            final CommandDto commandDto,
            final String key, final Consumer<String> consume) {
        commandDto.getUserData().getEntry()
                .stream()
                .filter(x -> Objects.equals(x.getKey(), key))
                .map(MapDto.Entry::getValue)
                .filter(Objects::nonNull)
                .filter(x -> x.length() > 0)
                .findFirst()
                .ifPresent(consume);
    }


    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @ObjectSupport public String title() {
        return new TitleBuffer()
                .append(formatter.format(getTimestamp().toLocalDateTime()))
                .append(" ")
                .append(getLogicalMemberIdentifier())
                .toString();
    }


    @DomainChangeRecord.Type
    @Override
    public ChangeType getType() {
        return ChangeType.COMMAND;
    }


    @Property(
            domainEvent = InteractionId.DomainEvent.class
    )
    @DomainChangeRecord.InteractionId
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface InteractionId {
        class DomainEvent extends PropertyDomainEvent<UUID> {}
        String NAME = "interactionId";
        int MAX_LENGTH = HasInteractionId.InteractionId.MAX_LENGTH;
        boolean NULLABLE = HasInteractionId.InteractionId.NULLABLE;
        String ALLOWS_NULL = HasInteractionId.InteractionId.ALLOWS_NULL;
    }
    @Override
    @InteractionId
    public abstract UUID getInteractionId();
    public abstract void setInteractionId(UUID interactionId);


    @Property(
            domainEvent = Username.DomainEvent.class
    )
    @DomainChangeRecord.Username
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Username {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = DomainChangeRecord.Username.MAX_LENGTH;
        boolean NULLABLE = DomainChangeRecord.Username.NULLABLE;
        String ALLOWS_NULL = DomainChangeRecord.Username.ALLOWS_NULL;
    }
    @Override
    @Username
    public abstract String getUsername();
    public abstract void setUsername(String userName);



    @Property(
            domainEvent = Timestamp.DomainEvent.class
    )
    @DomainChangeRecord.Timestamp
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Timestamp {
        class DomainEvent extends PropertyDomainEvent<java.sql.Timestamp> {}
        boolean NULLABLE = DomainChangeRecord.Timestamp.NULLABLE;
        String ALLOWS_NULL = DomainChangeRecord.Timestamp.ALLOWS_NULL;
    }
    @Timestamp
    @Override
    public abstract java.sql.Timestamp getTimestamp();
    public abstract void setTimestamp(java.sql.Timestamp timestamp);



    @Property(
            domainEvent = Target.DomainEvent.class
    )
    @DomainChangeRecord.Target
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Target {
        class DomainEvent extends PropertyDomainEvent<Bookmark> {}
        int MAX_LENGTH = DomainChangeRecord.Target.MAX_LENGTH;
        boolean NULLABLE = DomainChangeRecord.Target.NULLABLE;
        String ALLOWS_NULL = DomainChangeRecord.Target.ALLOWS_NULL;
    }
    @Override
    @Target
    public abstract Bookmark getTarget();
    public abstract void setTarget(Bookmark target);



    @Property(
            domainEvent = ExecuteIn.DomainEvent.class
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ExecuteIn {
        class DomainEvent extends PropertyDomainEvent<org.apache.causeway.extensions.commandlog.applib.dom.ExecuteIn> {}
        int MAX_LENGTH = 10;
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    /**
     * Whether the command was executed immediately in the current thread of execution, or scheduled to be
     * executed at some time later in a &quot;background&quot; thread of execution.
     */
    @ExecuteIn
    public abstract org.apache.causeway.extensions.commandlog.applib.dom.ExecuteIn getExecuteIn();
    public abstract void setExecuteIn(org.apache.causeway.extensions.commandlog.applib.dom.ExecuteIn replayState);


    /**
     * The interactionId of the parent command, if any.
     *
     * <p>
     *     We store only the id rather than a reference to the parent, because the
     *     {@link org.apache.causeway.extensions.commandlog.applib.subscriber.CommandSubscriberForCommandLog}'s
     *     callback is only called at the end of the transaction, meaning that the {@link CommandLogEntry} of the
     *     &quot;parent&quot; will be persisted only after any of its child background {@link CommandLogEntry}s are
     *     to be persisted (within the body of the underlying action).
     * </p>
     *
     * @see #getParent()
     */
    @Domain.Exclude
    public abstract UUID getParentInteractionId();
    public abstract void setParentInteractionId(UUID parentInteractionId);



    @Property(
            domainEvent = Parent.DomainEvent.class,
            optionality = Optionality.OPTIONAL
    )
    @PropertyLayout(
            hidden = Where.ALL_TABLES
    )
    @Parameter(
            optionality = Optionality.OPTIONAL
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Parent {
        class DomainEvent extends PropertyDomainEvent<CommandLogEntry> {}
        String NAME = "parentInteractionId";
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    @Parent
    public <C extends CommandLogEntry> C getParent() {
        if (getParentInteractionId() == null) {
            return null;
        }
        val parentCommandLogEntryIfAny = commandLogEntryRepository.findByInteractionId(getParentInteractionId());
        val commandLogEntry = parentCommandLogEntryIfAny.orElse(null);
        return _Casts.uncheckedCast(commandLogEntry);
    }

    @Inject CommandLogEntryRepository<? extends CommandLogEntry> commandLogEntryRepository;


    @Property(
            domainEvent = LogicalMemberIdentifier.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @DomainChangeRecord.LogicalMemberIdentifier
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface LogicalMemberIdentifier {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = DomainChangeRecord.LogicalMemberIdentifier.MAX_LENGTH;
        boolean NULLABLE = DomainChangeRecord.LogicalMemberIdentifier.NULLABLE;
        String ALLOWS_NULL = DomainChangeRecord.LogicalMemberIdentifier.ALLOWS_NULL;

    }
    @Override
    @LogicalMemberIdentifier
    public abstract String getLogicalMemberIdentifier();
    public abstract void setLogicalMemberIdentifier(String logicalMemberIdentifier);




    @Property(
            domainEvent = CommandDtoAnnot.DomainEvent.class
    )
    @HasCommandDto.CommandDtoAnnot
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CommandDtoAnnot {
        class DomainEvent extends PropertyDomainEvent<CommandDto> {}
        boolean NULLABLE = HasCommandDto.CommandDtoAnnot.NULLABLE;
        String ALLOWS_NULL = HasCommandDto.CommandDtoAnnot.ALLOWS_NULL;
    }
    @CommandDtoAnnot
    @Override
    public abstract CommandDto getCommandDto();
    public abstract void setCommandDto(CommandDto commandDto);



    @Property(
            domainEvent = StartedAt.DomainEvent.class,
            editing = Editing.DISABLED,
            optionality = Optionality.OPTIONAL
    )
    @Parameter(
            optionality = Optionality.OPTIONAL
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface StartedAt {
        class DomainEvent extends PropertyDomainEvent<java.sql.Timestamp> {}
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    @StartedAt
    public abstract java.sql.Timestamp getStartedAt();
    public abstract void setStartedAt(java.sql.Timestamp startedAt);



    @Property(
            domainEvent = CompletedAt.DomainEvent.class,
            editing = Editing.DISABLED,
            optionality = Optionality.OPTIONAL
    )
    @Parameter(
            optionality = Optionality.OPTIONAL
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CompletedAt {
        class DomainEvent extends PropertyDomainEvent<java.sql.Timestamp> {}
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    @CompletedAt
    public abstract java.sql.Timestamp getCompletedAt();
    public abstract void setCompletedAt(java.sql.Timestamp completedAt);



    @Property(
            domainEvent = Duration.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @Digits(integer=Duration.DIGITS_INTEGER, fraction=Duration.DIGITS_FRACTION)
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Duration {
        class DomainEvent extends PropertyDomainEvent<BigDecimal> {}
        int DIGITS_INTEGER = 5;
        int DIGITS_FRACTION = 3;
    }
    /**
     * The number of seconds (to 3 decimal places) that this command lasted, derived from
     *      * {@link #getStartedAt()} and {@link #getCompletedAt()}.
     *
     * <p>
     * Populated only if it has {@link #getCompletedAt() completed}.
     */
    @Digits(integer=5, fraction=3)
    @Duration
    public BigDecimal getDuration() {
        return _Temporals.secondsBetweenAsDecimal(getStartedAt(), getCompletedAt())
                .orElse(null);
    }



    @Property(
            domainEvent = IsComplete.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            hidden = Where.OBJECT_FORMS
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface IsComplete {
        class DomainEvent extends PropertyDomainEvent<Boolean> {}
    }
    @IsComplete
    public boolean isComplete() {
        return getCompletedAt() != null;
    }



    @Property(
            domainEvent = Result.DomainEvent.class
    )
    @PropertyLayout(
            hidden = Where.ALL_TABLES,
            named = "Result Bookmark"
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Result {
        class DomainEvent extends PropertyDomainEvent<Bookmark> {}
        int MAX_LENGTH = 2000;
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    @Result
    public abstract Bookmark getResult();
    public abstract void setResult(Bookmark result);




    @Property(
            domainEvent = Exception.DomainEvent.class
    )
    @PropertyLayout(
            hidden = Where.ALL_TABLES,
            multiLine = 3,
            named = "Exception (if any)"
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Exception {
        class DomainEvent extends PropertyDomainEvent<String> {}
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    /**
     * Stack trace of any exception that might have occurred if this interaction/transaction aborted.
     *
     * <p>
     * Not part of the applib API, because the default implementation is not persistent
     * and so there's no object that can be accessed to be annotated.
     */
    @Exception
    public abstract String getException();
    public abstract void setException(final String exception);

    public void setException(final Throwable exception) {
        setException(_Exceptions.asStacktrace(exception));
    }



    @Property(
            domainEvent = ResultSummary.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            hidden = Where.OBJECT_FORMS,
            named = "Result"
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ResultSummary {
        class DomainEvent extends PropertyDomainEvent<String> {}
    }
    @ResultSummary
    public String getResultSummary() {
        if(getCompletedAt() == null) {
            return "";
        }
        if(!_Strings.isNullOrEmpty(getException())) {
            return "EXCEPTION";
        }
        if(getResult() != null) {
            return "OK";
        } else {
            return "OK (VOID)";
        }
    }



    @Property(
            domainEvent = IsCaused.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            hidden = Where.OBJECT_FORMS
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface IsCaused {
        class DomainEvent extends PropertyDomainEvent<Boolean> {}
    }
    @IsCaused
    public boolean isCausedException() {
        return getException() != null;
    }


    @Property(
            domainEvent = ReplayState.DomainEvent.class
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ReplayState {
        class DomainEvent extends PropertyDomainEvent<org.apache.causeway.extensions.commandlog.applib.dom.ReplayState> {}
        int MAX_LENGTH = 10;
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    /**
     * For a replayed command, what the outcome was.
     */
    @ReplayState
    public abstract org.apache.causeway.extensions.commandlog.applib.dom.ReplayState getReplayState();
    public abstract void setReplayState(org.apache.causeway.extensions.commandlog.applib.dom.ReplayState replayState);



    @Property(
            domainEvent = ReplayStateFailureReason.DomainEvent.class,
            optionality = Optionality.OPTIONAL,
            maxLength = ReplayStateFailureReason.MAX_LENGTH
    )
    @PropertyLayout(hidden = Where.ALL_TABLES, multiLine = 5)
    @Parameter(
            optionality = Optionality.OPTIONAL,
            maxLength = ReplayStateFailureReason.MAX_LENGTH
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ReplayStateFailureReason {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = 255;
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    /**
     * For a {@link org.apache.causeway.extensions.commandlog.applib.dom.ReplayState#FAILED failed} replayed command, what the reason was for the failure.
     */
    @ReplayStateFailureReason
    public abstract String getReplayStateFailureReason();
    public abstract void setReplayStateFailureReason(String replayStateFailureReason);
    @MemberSupport public boolean hideReplayStateFailureReason() {
        return getReplayState() == null || !getReplayState().isFailed();
    }


    @Programmatic
    public void saveAnalysis(final String analysis) {
        if (analysis == null) {
            setReplayState(org.apache.causeway.extensions.commandlog.applib.dom.ReplayState.OK);
        } else {
            setReplayState(org.apache.causeway.extensions.commandlog.applib.dom.ReplayState.FAILED);
            setReplayStateFailureReason(_Strings.trimmed(analysis, 255));
        }

    }

    @Override
    public int compareTo(final CommandLogEntry other) {
        return this.getTimestamp().compareTo(other.getTimestamp());
    }


    static final ToString<CommandLogEntry> stringifier = ObjectContracts
            .toString("interactionId", CommandLogEntry::getInteractionId)
            .thenToString("username", CommandLogEntry::getUsername)
            .thenToString("timestamp", CommandLogEntry::getTimestamp)
            .thenToString("target", CommandLogEntry::getTarget)
            .thenToString("logicalMemberIdentifier", CommandLogEntry::getLogicalMemberIdentifier)
            .thenToStringOmitIfAbsent("startedAt", CommandLogEntry::getStartedAt)
            .thenToStringOmitIfAbsent("completedAt", CommandLogEntry::getCompletedAt);

    @Override
    public String toString() {
        return stringifier.toString(this);
    }

    @Programmatic
    public CommandOutcomeHandler outcomeHandler() {
        return new CommandOutcomeHandler() {

            @Override
            public java.sql.Timestamp getStartedAt() {
                return CommandLogEntry.this.getStartedAt();
            }

            @Override
            public void setStartedAt(final java.sql.Timestamp startedAt) {
                CommandLogEntry.this.setStartedAt(startedAt);
            }

            @Override
            public void setCompletedAt(final java.sql.Timestamp completedAt) {
                CommandLogEntry.this.setCompletedAt(completedAt);
            }

            @Override
            public void setResult(Try<Bookmark> result) {
                result.ifSuccess(bookmarkIfAny -> bookmarkIfAny.ifPresent(CommandLogEntry.this::setResult));
                result.ifFailure(CommandLogEntry.this::setException);
            }
        };
    }


    @Service
    @Priority(PriorityPrecedence.LATE - 10) // before the framework's own default.
    public static class TableColumnOrderDefault extends TableColumnOrderForCollectionTypeAbstract<CommandLogEntry> {

        public TableColumnOrderDefault() { super(CommandLogEntry.class); }

        @Override
        protected List<String> orderParented(final Object parent, final String collectionId, final List<String> propertyIds) {
            return ordered(propertyIds);
        }

        @Override
        protected List<String> orderStandalone(final List<String> propertyIds) {
            return ordered(propertyIds);
        }

        private List<String> ordered(final List<String> propertyIds) {
            return Arrays.asList(
                "timestamp", "target", "logicalMemberIdentifier", "username", "complete", "resultSummary", "duration", "interactionId"
            );
        }
    }

}

