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
package org.apache.isis.extensions.commandlog.applib.command;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import javax.persistence.Column;
import javax.persistence.Transient;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.mixins.system.DomainChangeRecord;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandOutcomeHandler;
import org.apache.isis.applib.services.commanddto.conmap.UserDataKeys;
import org.apache.isis.applib.services.tablecol.TableColumnOrderForCollectionTypeAbstract;
import org.apache.isis.applib.util.TitleBuffer;
import org.apache.isis.commons.functional.Try;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.extensions.commandlog.applib.IsisModuleExtCommandLogApplib;
import org.apache.isis.extensions.commandlog.applib.util.BigDecimalUtils;
import org.apache.isis.extensions.commandlog.applib.util.StringUtils;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.MapDto;

import lombok.NoArgsConstructor;
import lombok.val;

/**
 * A persistent representation of a {@link Command}.
 *
 * <p>
 *     Use cases requiring persistence including auditing, and for replay of
 *     commands for regression testing purposes.
 * </p>
 *
 * Note that this class doesn't subclass from {@link Command} ({@link Command}
 * is not an interface).
 */
@DomainObject(
        logicalTypeName = CommandLog.LOGICAL_TYPE_NAME,
        editing = Editing.DISABLED
)
@DomainObjectLayout(
        named = "Command",
        titleUiEvent = ICommandLog.TitleUiEvent.class,
        iconUiEvent = ICommandLog.IconUiEvent.class,
        cssClassUiEvent = ICommandLog.CssClassUiEvent.class,
        layoutUiEvent = ICommandLog.LayoutUiEvent.class
)
//@Log4j2
@NoArgsConstructor
public abstract class CommandLog
implements
    ICommandLog,
    DomainChangeRecord {

    public final static String LOGICAL_TYPE_NAME = IsisModuleExtCommandLogApplib.NAMESPACE + ".CommandLog";

    /**
     * Intended for use on primary system.
     *
     * @param command
     */
    public CommandLog(final Command command) {

        setInteractionIdStr(command.getInteractionId().toString());
        setUsername(command.getUsername());
        setTimestamp(command.getTimestamp());

        setCommandDto(command.getCommandDto());
        setTarget(command.getTarget());
        setLogicalMemberIdentifier(command.getLogicalMemberIdentifier());

        setStartedAt(command.getStartedAt());
        setCompletedAt(command.getCompletedAt());

        setResult(command.getResult());
        setException(command.getException());

        setReplayState(ReplayState.UNDEFINED);
    }


    /**
     * Intended for use on secondary (replay) system.
     *
     * @param commandDto - obtained from the primary system as a representation of a command invocation
     * @param replayState - controls whether this is to be replayed
     * @param targetIndex - if the command represents a bulk action, then it is flattened out when replayed; this indicates which target to execute against.
     */
    public CommandLog(
            final CommandDto commandDto,
            final ReplayState replayState,
            final int targetIndex) {

        setInteractionIdStr(commandDto.getInteractionId());
        setUsername(commandDto.getUser());
        setTimestamp(JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(commandDto.getTimestamp()));

        setCommandDto(commandDto);
        setTarget(Bookmark.forOidDto(commandDto.getTargets().getOid().get(targetIndex)));
        setLogicalMemberIdentifier(commandDto.getMember().getLogicalMemberIdentifier());

        // the hierarchy of commands calling other commands is only available on the primary system, and is
        setParent(null);

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

    @Service
    public static class TitleProvider {

        private final DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        @EventListener(TitleUiEvent.class)
        public void on(final TitleUiEvent ev) {
            if(ev.getTranslatableTitle() != null
                    && ev.getSource() != null
                    && ev.getSource().getTimestamp() != null
                    && (Objects.equals(ev.getTitle(), "Command Jdo")
                            || Objects.equals(ev.getTitle(), "Command Jpa")
                    )) {
                ev.setTitle(title((CommandLog)ev.getSource()));
            }
        }

        private String title(final CommandLog source) {
            return new TitleBuffer()
            .append(formatter.format(source.getTimestamp().toLocalDateTime()))
            .append(" ").append(source.getLogicalMemberIdentifier())
            .toString();
        }
    }


    public static class InteractionIdDomainEvent extends PropertyDomainEvent<String> { }
    /**
     * Implementation note: persisted as a string rather than a UUID as fails
     * to persist if using h2 (perhaps would need to be mapped differently).
     * @see <a href="https://www.datanucleus.org/products/accessplatform/jdo/mapping.html#_other_types">www.datanucleus.org</a>
     */
    @Property(domainEvent = InteractionIdDomainEvent.class)
    @PropertyLayout(named = "Interaction Id")
    public abstract String getInteractionIdStr();
    public abstract void setInteractionIdStr(String interactionIdStr);

    @Transient
    @javax.jdo.annotations.NotPersistent
    @Programmatic
    @Override
    public UUID getInteractionId() {return UUID.fromString(getInteractionIdStr());}


    public static class UsernameDomainEvent extends PropertyDomainEvent<String> { }
    @Property(domainEvent = UsernameDomainEvent.class)
    @Override
    public abstract String getUsername();
    public abstract void setUsername(String userName);


    public static class TimestampDomainEvent extends PropertyDomainEvent<Timestamp> { }
    @Property(domainEvent = TimestampDomainEvent.class)
    @Override
    public abstract Timestamp getTimestamp();
    public abstract void setTimestamp(Timestamp timestamp);


    @Transient
    @javax.jdo.annotations.NotPersistent
    @Override
    public ChangeType getType() {
        return ChangeType.COMMAND;
    }

    public static class ReplayStateDomainEvent extends PropertyDomainEvent<ReplayState> { }
    /**
     * For a replayed command, what the outcome was.
     */
    @Property(domainEvent = ReplayStateDomainEvent.class)
    @Override
    public abstract ReplayState getReplayState();

    public static class ReplayStateFailureReasonDomainEvent extends PropertyDomainEvent<ReplayState> { }
    /**
     * For a {@link ReplayState#FAILED failed} replayed command, what the reason was for the failure.
     */
    @Column(nullable=true, length=255)
    @Property(domainEvent = ReplayStateFailureReasonDomainEvent.class)
    @PropertyLayout(hidden = Where.ALL_TABLES, multiLine = 5)
    public abstract String getReplayStateFailureReason();
    public abstract void setReplayStateFailureReason(String replayStateFailureReason);
    @MemberSupport public boolean hideReplayStateFailureReason() {
        return getReplayState() == null || !getReplayState().isFailed();
    }

    public static class ParentDomainEvent extends PropertyDomainEvent<Command> { }
    @Property(domainEvent = ParentDomainEvent.class)
    @PropertyLayout(hidden = Where.ALL_TABLES)
    public abstract <C extends CommandLog> C getParent();
    public abstract void setParent(CommandLog parent);

    public static class TargetDomainEvent extends PropertyDomainEvent<String> { }
    @Override
    @Property(domainEvent = TargetDomainEvent.class)
    @PropertyLayout(named = "Object")
    public abstract Bookmark getTarget();
    public abstract void setTarget(Bookmark target);

    @Transient
    @javax.jdo.annotations.NotPersistent
    public String getTargetStr() {
        return Optional.ofNullable(getTarget()).map(Bookmark::toString).orElse(null);
    }

    @Transient
    @javax.jdo.annotations.NotPersistent
    @Override
    public String getTargetMember() {
        return getCommandDto().getMember().getLogicalMemberIdentifier();
    }

    @Transient
    @javax.jdo.annotations.NotPersistent
    @Property(domainEvent = TargetDomainEvent.class)
    @PropertyLayout(named = "Member")
    public String getLocalMember() {
        val targetMember = getTargetMember();
        return targetMember.substring(targetMember.indexOf("#") + 1);
    }

    public static class LogicalMemberIdentifierDomainEvent extends PropertyDomainEvent<String> { }
    @Override
    @Property(domainEvent = LogicalMemberIdentifierDomainEvent.class)
    @PropertyLayout(hidden = Where.ALL_TABLES)
    public abstract String getLogicalMemberIdentifier();
    public abstract void setLogicalMemberIdentifier(String logicalMemberIdentifier);

    public static class CommandDtoDomainEvent extends PropertyDomainEvent<CommandDto> { }
    @Property(domainEvent = CommandDtoDomainEvent.class)
    @PropertyLayout(multiLine = 9)
    @Override
    public abstract CommandDto getCommandDto();
    public abstract void setCommandDto(CommandDto commandDto);

    public static class StartedAtDomainEvent extends PropertyDomainEvent<Timestamp> { }
    @Override
    @Property(domainEvent = StartedAtDomainEvent.class)
    public abstract Timestamp getStartedAt();
    public abstract void setStartedAt(Timestamp startedAt);

    public static class CompletedAtDomainEvent extends PropertyDomainEvent<Timestamp> { }
    @Override
    @Property(domainEvent = CompletedAtDomainEvent.class)
    public abstract Timestamp getCompletedAt();
    public abstract void setCompletedAt(Timestamp completedAt);

    public static class DurationDomainEvent extends PropertyDomainEvent<BigDecimal> { }
    /**
     * The number of seconds (to 3 decimal places) that this interaction lasted.
     *
     * <p>
     * Populated only if it has {@link #getCompletedAt() completed}.
     */
    @Transient
    @javax.jdo.annotations.NotPersistent
    @javax.validation.constraints.Digits(integer=5, fraction=3)
    @Property(domainEvent = DurationDomainEvent.class)
    public BigDecimal getDuration() {
        return BigDecimalUtils.durationBetween(getStartedAt(), getCompletedAt());
    }


    public static class IsCompleteDomainEvent extends PropertyDomainEvent<Boolean> { }
    @Transient
    @javax.jdo.annotations.NotPersistent
    @Property(domainEvent = IsCompleteDomainEvent.class)
    @PropertyLayout(hidden = Where.OBJECT_FORMS)
    public boolean isComplete() {
        return getCompletedAt() != null;
    }


    public static class ResultSummaryDomainEvent extends PropertyDomainEvent<String> { }
    @Transient
    @javax.jdo.annotations.NotPersistent
    @Property(domainEvent = ResultSummaryDomainEvent.class)
    @PropertyLayout(hidden = Where.OBJECT_FORMS, named = "Result")
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

    public static class ResultDomainEvent extends PropertyDomainEvent<String> { }
    @Override
    @Property(domainEvent = ResultDomainEvent.class)
    @PropertyLayout(hidden = Where.ALL_TABLES, named = "Result Bookmark")
    public abstract Bookmark getResult();
    public abstract void setResult(Bookmark result);

    public static class ExceptionDomainEvent extends PropertyDomainEvent<String> { }
    /**
     * Stack trace of any exception that might have occurred if this interaction/transaction aborted.
     *
     * <p>
     * Not part of the applib API, because the default implementation is not persistent
     * and so there's no object that can be accessed to be annotated.
     */
    @Override
    @Property(domainEvent = ExceptionDomainEvent.class)
    @PropertyLayout(hidden = Where.ALL_TABLES, multiLine = 5, named = "Exception (if any)")
    public abstract String getException();
    public abstract void setException(final String exception);
    @Transient
    @javax.jdo.annotations.NotPersistent
    public void setException(final Throwable exception) {
        setException(_Exceptions.asStacktrace(exception));
    }

    public static class IsCausedExceptionDomainEvent extends PropertyDomainEvent<Boolean> { }
    @Transient
    @javax.jdo.annotations.NotPersistent
    @Property(domainEvent = IsCausedExceptionDomainEvent.class)
    @PropertyLayout(hidden = Where.OBJECT_FORMS)
    public boolean isCausedException() {
        return getException() != null;
    }

    @Transient
    @javax.jdo.annotations.NotPersistent
    @Override
    public String getPreValue() {
        return null;
    }

    @Transient
    @javax.jdo.annotations.NotPersistent
    @Override
    public String getPostValue() {
        return null;
    }


    @Override
    public void saveAnalysis(final String analysis) {
        if (analysis == null) {
            setReplayState(ReplayState.OK);
        } else {
            setReplayState(ReplayState.FAILED);
            setReplayStateFailureReason(StringUtils.trimmed(analysis, 255));
        }

    }

    @Override
    public String toString() {
        return toFriendlyString();
    }

    @Override
    public CommandOutcomeHandler outcomeHandler() {
        return new CommandOutcomeHandler() {
            @Override
            public Timestamp getStartedAt() {
                return CommandLog.this.getStartedAt();
            }

            @Override
            public void setStartedAt(final Timestamp startedAt) {
                CommandLog.this.setStartedAt(startedAt);
            }

            @Override
            public void setCompletedAt(final Timestamp completedAt) {
                CommandLog.this.setCompletedAt(completedAt);
            }

            @Override
            public void setResult(final Try<Bookmark> resultBookmark) {
                CommandLog.this.setResult(resultBookmark.getValue().orElse(null));
                CommandLog.this.setException(resultBookmark.getFailure().orElse(null));
            }

        };
    }


    @Service
    @javax.annotation.Priority(PriorityPrecedence.LATE - 10) // before the framework's own default.
    public static class TableColumnOrderDefault extends TableColumnOrderForCollectionTypeAbstract<CommandLog> {

        public TableColumnOrderDefault() { super(CommandLog.class); }

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
                "timestamp", "target", "targetMember", "username", "complete", "resultSummary", "interactionIdStr"
            );
        }
    }
}

