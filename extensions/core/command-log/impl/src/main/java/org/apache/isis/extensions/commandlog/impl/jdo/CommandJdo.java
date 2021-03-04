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
package org.apache.isis.extensions.commandlog.impl.jdo;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import javax.jdo.annotations.IdentityType;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.mixins.system.DomainChangeRecord;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandOutcomeHandler;
import org.apache.isis.applib.services.commanddto.HasCommandDto;
import org.apache.isis.applib.services.commanddto.conmap.UserDataKeys;
import org.apache.isis.applib.services.tablecol.TableColumnOrderForCollectionTypeAbstract;
import org.apache.isis.applib.types.MemberIdentifierType;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.TitleBuffer;
import org.apache.isis.commons.functional.Result;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.extensions.commandlog.impl.IsisModuleExtCommandLogImpl;
import org.apache.isis.extensions.commandlog.impl.util.BigDecimalUtils;
import org.apache.isis.extensions.commandlog.impl.util.StringUtils;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.MapDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@javax.jdo.annotations.PersistenceCapable(
        identityType=IdentityType.APPLICATION,
        schema = "isisExtensionsCommandLog",
        table = "Command")
@javax.jdo.annotations.Queries( {
    @javax.jdo.annotations.Query(
            name="findByUniqueIdStr",
            value="SELECT "
                    + "FROM org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo "
                    + "WHERE uniqueIdStr == :uniqueIdStr "),
    @javax.jdo.annotations.Query(
            name="findByParent",
            value="SELECT "
                    + "FROM org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo "
                    + "WHERE parent == :parent "),
    @javax.jdo.annotations.Query(
            name="findCurrent",
            value="SELECT "
                    + "FROM org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo "
                    + "WHERE completedAt == null "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findCompleted",
            value="SELECT "
                    + "FROM org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo "
                    + "WHERE completedAt != null "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findRecentByTarget",
            value="SELECT "
                    + "FROM org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo "
                    + "WHERE target == :target "
                    + "ORDER BY this.timestamp DESC, uniqueIdStr DESC "
                    + "RANGE 0,30"),
    @javax.jdo.annotations.Query(
            name="findByTargetAndTimestampBetween",
            value="SELECT "
                    + "FROM org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo "
                    + "WHERE target == :target "
                    + "&& timestamp >= :from "
                    + "&& timestamp <= :to "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTargetAndTimestampAfter",
            value="SELECT "
                    + "FROM org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo "
                    + "WHERE target == :target "
                    + "&& timestamp >= :from "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTargetAndTimestampBefore",
            value="SELECT "
                    + "FROM org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo "
                    + "WHERE target == :target "
                    + "&& timestamp <= :to "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTarget",
            value="SELECT "
                    + "FROM org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo "
                    + "WHERE target == :target "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTimestampBetween",
            value="SELECT "
                    + "FROM org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo "
                    + "WHERE timestamp >= :from "
                    + "&&    timestamp <= :to "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTimestampAfter",
            value="SELECT "
                    + "FROM org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo "
                    + "WHERE timestamp >= :from "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTimestampBefore",
            value="SELECT "
                    + "FROM org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo "
                    + "WHERE timestamp <= :to "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="find",
            value="SELECT "
                    + "FROM org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findRecentByUsername",
            value="SELECT "
                    + "FROM org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo "
                    + "WHERE username == :username "
                    + "ORDER BY this.timestamp DESC "
                    + "RANGE 0,30"),
    @javax.jdo.annotations.Query(
            name="findFirst",
            value="SELECT "
                    + "FROM org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo "
                    + "WHERE startedAt   != null "
                    + "   && completedAt != null "
                    + "ORDER BY this.timestamp ASC "
                    + "RANGE 0,2"),
        // this should be RANGE 0,1 but results in DataNucleus submitting "FETCH NEXT ROW ONLY"
        // which SQL Server doesn't understand.  However, as workaround, SQL Server *does* understand FETCH NEXT 2 ROWS ONLY
    @javax.jdo.annotations.Query(
            name="findSince",
            value="SELECT "
                    + "FROM org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo "
                    + "WHERE timestamp > :timestamp "
                    + "   && startedAt != null "
                    + "   && completedAt != null "
                    + "ORDER BY this.timestamp ASC"),
    // most recent (replayed) command previously replicated from primary to
    // secondary.  This should always exist except for the very first times
    // (after restored the prod DB to secondary).
    @javax.jdo.annotations.Query(
            name="findMostRecentReplayed",
            value="SELECT "
                    + "FROM org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo "
                    + "WHERE (replayState == 'OK' || replayState == 'FAILED') "
                    + "ORDER BY this.timestamp DESC "
                    + "RANGE 0,2"), // this should be RANGE 0,1 but results in DataNucleus submitting "FETCH NEXT ROW ONLY"
                                    // which SQL Server doesn't understand.  However, as workaround, SQL Server *does* understand FETCH NEXT 2 ROWS ONLY
    // the most recent completed command, as queried on the
    // secondary, corresponding to the last command run on primary before the
    // production database was restored to the secondary
    @javax.jdo.annotations.Query(
            name="findMostRecentCompleted",
            value="SELECT "
                    + "FROM org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo "
                    + "WHERE startedAt   != null "
                    + "   && completedAt != null "
                    + "ORDER BY this.timestamp DESC "
                    + "RANGE 0,2"),
        // this should be RANGE 0,1 but results in DataNucleus submitting "FETCH NEXT ROW ONLY"
        // which SQL Server doesn't understand.  However, as workaround, SQL Server *does* understand FETCH NEXT 2 ROWS ONLY
    @javax.jdo.annotations.Query(
            name="findNotYetReplayed",
            value="SELECT "
                    + "FROM org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo "
                    + "WHERE replayState == 'PENDING' "
                    + "ORDER BY this.timestamp ASC "
                    + "RANGE 0,10"),    // same as batch size
//        @javax.jdo.annotations.Query(
//                name="findReplayableInErrorMostRecent",
//                value="SELECT "
//                        + "FROM org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo "
//                        + "WHERE replayState == 'FAILED' "
//                        + "ORDER BY this.timestamp DESC "
//                        + "RANGE 0,2"),
//    @javax.jdo.annotations.Query(
//            name="findReplayableMostRecentStarted",
//            value="SELECT "
//                    + "FROM org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo "
//                    + "WHERE replayState = 'PENDING' "
//                    + "ORDER BY this.timestamp DESC "
//                    + "RANGE 0,20"),
})
@javax.jdo.annotations.Indices({
        @javax.jdo.annotations.Index(name = "CommandJdo__startedAt__timestamp__IDX", members = { "startedAt", "timestamp" }),
        @javax.jdo.annotations.Index(name = "CommandJdo__timestamp__IDX", members = { "timestamp" }),
//        @javax.jdo.annotations.Index(name = "CommandJdo__replayState__timestamp__startedAt_IDX", members = { "replayState", "timestamp", "startedAt"}),
//        @javax.jdo.annotations.Index(name = "CommandJdo__replayState__startedAt__completedAt_IDX", members = {"startedAt", "replayState", "completedAt"}),
})
@DomainObject(
        objectType = "isis.ext.commandLog.Command",
        editing = Editing.DISABLED
)
@DomainObjectLayout(
        named = "Command",
        titleUiEvent = CommandJdo.TitleUiEvent.class,
        iconUiEvent = CommandJdo.IconUiEvent.class,
        cssClassUiEvent = CommandJdo.CssClassUiEvent.class,
        layoutUiEvent = CommandJdo.LayoutUiEvent.class
)
//@Log4j2
@NoArgsConstructor
public class CommandJdo
        implements DomainChangeRecord, Comparable<CommandJdo>, HasCommandDto {

    public static class TitleUiEvent extends IsisModuleExtCommandLogImpl.TitleUiEvent<CommandJdo> { }
    public static class IconUiEvent extends IsisModuleExtCommandLogImpl.IconUiEvent<CommandJdo> { }
    public static class CssClassUiEvent extends IsisModuleExtCommandLogImpl.CssClassUiEvent<CommandJdo> { }
    public static class LayoutUiEvent extends IsisModuleExtCommandLogImpl.LayoutUiEvent<CommandJdo> { }

    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtCommandLogImpl.PropertyDomainEvent<CommandJdo, T> { }
    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtCommandLogImpl.CollectionDomainEvent<CommandJdo, T> { }
    public static abstract class ActionDomainEvent extends IsisModuleExtCommandLogImpl.ActionDomainEvent<CommandJdo> { }

    /**
     * Intended for use on primary system.
     *
     * @param command
     */
    public CommandJdo(final Command command) {

        setUniqueIdStr(command.getInteractionId().toString());
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
    public CommandJdo(
            final CommandDto commandDto,
            final ReplayState replayState,
            final int targetIndex) {

        setUniqueIdStr(commandDto.getTransactionId());
        setUsername(commandDto.getUser());
        setTimestamp(JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(commandDto.getTimestamp()));

        setCommandDto(commandDto);
        setTarget(Bookmark.from(commandDto.getTargets().getOid().get(targetIndex)));
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

        @EventListener(TitleUiEvent.class)
        public void on(TitleUiEvent ev) {
            if(!Objects.equals(ev.getTitle(), "Command Jdo") || ev.getTranslatableTitle() != null) {
                return;
            }
            ev.setTitle(title(ev.getSource()));
        }

        private static String title(CommandJdo source) {
            // nb: not thread-safe
            // formats defined in https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
            val format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            val buf = new TitleBuffer();
            buf.append(format.format(source.getTimestamp()));
            buf.append(" ").append(source.getLogicalMemberIdentifier());
            return buf.toString();
        }
    }


    public static class UniqueIdDomainEvent extends PropertyDomainEvent<String> { }
    /**
     * Implementation note: persisted as a string rather than a UUID as fails
     * to persist if using h2 (perhaps would need to be mapped differently).
     * @see <a href="https://www.datanucleus.org/products/accessplatform/jdo/mapping.html#_other_types">www.datanucleus.org</a>
     */
    @javax.jdo.annotations.PrimaryKey
    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="false", name = "uniqueId", length = 36)
    @Property(domainEvent = UniqueIdDomainEvent.class)
    @PropertyLayout(named = "UniqueId")
    @Getter @Setter
    private String uniqueIdStr;
    @Programmatic
    public UUID getInteractionId() {return UUID.fromString(getUniqueIdStr());}


    public static class UsernameDomainEvent extends PropertyDomainEvent<String> { }
    @javax.jdo.annotations.Column(allowsNull="false", length = 50)
    @Property(domainEvent = UsernameDomainEvent.class)
    @Getter @Setter
    private String username;


    public static class TimestampDomainEvent extends PropertyDomainEvent<Timestamp> { }
    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="false")
    @Property(domainEvent = TimestampDomainEvent.class)
    @Getter @Setter
    private Timestamp timestamp;



    @Override
    public ChangeType getType() {
        return ChangeType.COMMAND;
    }


    public static class ReplayStateDomainEvent extends PropertyDomainEvent<ReplayState> { }
    /**
     * For a replayed command, what the outcome was.
     */
    @javax.jdo.annotations.Column(allowsNull="true", length=10)
    @Property(domainEvent = ReplayStateDomainEvent.class)
    @Getter @Setter
    private ReplayState replayState;


    public static class ReplayStateFailureReasonDomainEvent extends PropertyDomainEvent<ReplayState> { }
    /**
     * For a {@link ReplayState#FAILED failed} replayed command, what the reason was for the failure.
     */
    @javax.jdo.annotations.Column(allowsNull="true", length=255)
    @Property(domainEvent = ReplayStateFailureReasonDomainEvent.class)
    @PropertyLayout(hidden = Where.ALL_TABLES, multiLine = 5)
    @Getter @Setter
    private String replayStateFailureReason;
    public boolean hideReplayStateFailureReason() {
        return getReplayState() == null || !getReplayState().isFailed();
    }


    public static class ParentDomainEvent extends PropertyDomainEvent<Command> { }
    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(name="parentId", allowsNull="true")
    @Property(domainEvent = ParentDomainEvent.class)
    @PropertyLayout(hidden = Where.ALL_TABLES)
    @Getter @Setter
    private CommandJdo parent;


    public static class TargetDomainEvent extends PropertyDomainEvent<String> { }
    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true", length = 2000, name="target")
    @Property(domainEvent = TargetDomainEvent.class)
    @PropertyLayout(named = "Object")
    @Getter @Setter
    private Bookmark target;

    public String getTargetStr() {
        return Optional.ofNullable(getTarget()).map(Bookmark::toString).orElse(null);
    }

    @Override
    public String getTargetMember() {
        return getCommandDto().getMember().getLogicalMemberIdentifier();
    }

    @Property(domainEvent = TargetDomainEvent.class)
    @PropertyLayout(named = "Member")
    public String getLocalMember() {
        val targetMember = getTargetMember();
        return targetMember.substring(targetMember.indexOf("#") + 1);
    }

    public static class LogicalMemberIdentifierDomainEvent extends PropertyDomainEvent<String> { }
    @Property(domainEvent = LogicalMemberIdentifierDomainEvent.class)
    @PropertyLayout(hidden = Where.ALL_TABLES)
    @javax.jdo.annotations.Column(allowsNull="false", length = MemberIdentifierType.Meta.MAX_LEN)
    @Getter @Setter
    private String logicalMemberIdentifier;


    public static class CommandDtoDomainEvent extends PropertyDomainEvent<CommandDto> { }
    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true", jdbcType="CLOB")
    @Property(domainEvent = CommandDtoDomainEvent.class)
    @PropertyLayout(multiLine = 9)
    @Getter @Setter
    private CommandDto commandDto;


    public static class StartedAtDomainEvent extends PropertyDomainEvent<Timestamp> { }
    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true")
    @Property(domainEvent = StartedAtDomainEvent.class)
    @Getter @Setter
    private Timestamp startedAt;


    public static class CompletedAtDomainEvent extends PropertyDomainEvent<Timestamp> { }
    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true")
    @Property(domainEvent = CompletedAtDomainEvent.class)
    @Getter @Setter
    private Timestamp completedAt;


    public static class DurationDomainEvent extends PropertyDomainEvent<BigDecimal> { }
    /**
     * The number of seconds (to 3 decimal places) that this interaction lasted.
     *
     * <p>
     * Populated only if it has {@link #getCompletedAt() completed}.
     */
    @javax.jdo.annotations.NotPersistent
    @javax.validation.constraints.Digits(integer=5, fraction=3)
    @Property(domainEvent = DurationDomainEvent.class)
    public BigDecimal getDuration() {
        return BigDecimalUtils.durationBetween(getStartedAt(), getCompletedAt());
    }


    public static class IsCompleteDomainEvent extends PropertyDomainEvent<Boolean> { }
    @javax.jdo.annotations.NotPersistent
    @Property(domainEvent = IsCompleteDomainEvent.class)
    @PropertyLayout(hidden = Where.OBJECT_FORMS)
    public boolean isComplete() {
        return getCompletedAt() != null;
    }


    public static class ResultSummaryDomainEvent extends PropertyDomainEvent<String> { }
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
    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true", length = 2000, name="result")
    @Property(domainEvent = ResultDomainEvent.class)
    @PropertyLayout(hidden = Where.ALL_TABLES, named = "Result Bookmark")
    @Getter @Setter
    private Bookmark result;

    public static class ExceptionDomainEvent extends PropertyDomainEvent<String> { }
    /**
     * Stack trace of any exception that might have occurred if this interaction/transaction aborted.
     *
     * <p>
     * Not part of the applib API, because the default implementation is not persistent
     * and so there's no object that can be accessed to be annotated.
     */
    @javax.jdo.annotations.Column(allowsNull="true", jdbcType="CLOB")
    @Property(domainEvent = ExceptionDomainEvent.class)
    @PropertyLayout(hidden = Where.ALL_TABLES, multiLine = 5, named = "Exception (if any)")
    @Getter
    private String exception;
    public void setException(String exception) {
        this.exception = exception;
    }
    public void setException(final Throwable exception) {
        setException(_Exceptions.asStacktrace(exception));
    }

    public static class IsCausedExceptionDomainEvent extends PropertyDomainEvent<Boolean> { }
    @javax.jdo.annotations.NotPersistent
    @Property(domainEvent = IsCausedExceptionDomainEvent.class)
    @PropertyLayout(hidden = Where.OBJECT_FORMS)
    public boolean isCausedException() {
        return getException() != null;
    }


    @Override
    public String getPreValue() {
        return null;
    }

    @Override
    public String getPostValue() {
        return null;
    }


    public void saveAnalysis(String analysis) {
        if (analysis == null) {
            setReplayState(ReplayState.OK);
        } else {
            setReplayState(ReplayState.FAILED);
            setReplayStateFailureReason(StringUtils.trimmed(analysis, 255));
        }

    }


    @Override
    public String toString() {
        return ObjectContracts
                .toString("uniqueId", CommandJdo::getInteractionId)
                .thenToString("username", CommandJdo::getUsername)
                .thenToString("timestamp", CommandJdo::getTimestamp)
                .thenToString("target", CommandJdo::getTarget)
                .thenToString("logicalMemberIdentifier", CommandJdo::getLogicalMemberIdentifier)
                .thenToStringOmitIfAbsent("startedAt", CommandJdo::getStartedAt)
                .thenToStringOmitIfAbsent("completedAt", CommandJdo::getCompletedAt)
                .toString(this);
    }

    @Override
    public int compareTo(final CommandJdo other) {
        return this.getTimestamp().compareTo(other.getTimestamp());
    }

    public CommandOutcomeHandler outcomeHandler() {
        return new CommandOutcomeHandler() {
            @Override
            public Timestamp getStartedAt() {
                return CommandJdo.this.getStartedAt();
            }

            @Override
            public void setStartedAt(final Timestamp startedAt) {
                CommandJdo.this.setStartedAt(startedAt);
            }

            @Override
            public void setCompletedAt(final Timestamp completedAt) {
                CommandJdo.this.setCompletedAt(completedAt);
            }

            @Override
            public void setResult(final Result<Bookmark> resultBookmark) {
                CommandJdo.this.setResult(resultBookmark.getValue().orElse(null));
                CommandJdo.this.setException(resultBookmark.getFailure().orElse(null));
            }

        };
    }

    @Service
    @Order(OrderPrecedence.LATE - 10) // before the framework's own default.
    public static class TableColumnOrderDefault extends TableColumnOrderForCollectionTypeAbstract<CommandJdo> {

        public TableColumnOrderDefault() { super(CommandJdo.class); }

        @Override
        protected List<String> orderParented(Object parent, String collectionId, List<String> propertyIds) {
            return ordered(propertyIds);
        }

        @Override
        protected List<String> orderStandalone(List<String> propertyIds) {
            return ordered(propertyIds);
        }

        private List<String> ordered(List<String> propertyIds) {
            return Arrays.asList(
                "timestamp", "target", "targetMember", "username", "complete", "resultSummary", "uniqueIdStr"
            );
        }
    }
}

