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
package org.apache.isis.extensions.commandlog.jdo.dom;

import javax.inject.Named;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.Indices;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.extensions.commandlog.jdo.IsisModuleExtCommandLogJdo;
import org.apache.isis.schema.cmd.v2.CommandDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@PersistenceCapable(
        identityType = IdentityType.APPLICATION,
        schema = CommandLogEntry.SCHEMA,
        table = CommandLogEntry.TABLE)
@Indices({
        @Index(name = "CommandJdo__startedAt__timestamp__IDX", members = { "startedAt", "timestamp" }),
        @Index(name = "CommandJdo__timestamp__IDX", members = { "timestamp" }),
//        @javax.jdo.annotations.Index(name = "CommandJdo__replayState__timestamp__startedAt_IDX", members = { "replayState", "timestamp", "startedAt"}),
//        @javax.jdo.annotations.Index(name = "CommandJdo__replayState__startedAt__completedAt_IDX", members = {"startedAt", "replayState", "completedAt"}),
})
@Queries( {
    @Query(
            name  = "findByInteractionIdStr",
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE interactionIdStr == :interactionIdStr "),
    @Query(
            name  = "findByParent",
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE parent == :parent "),
    @Query(
            name  = "findCurrent",
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE completedAt == null "
                  + " ORDER BY this.timestamp DESC"),
    @Query(
            name  = "findCompleted",
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE completedAt != null "
                  + " ORDER BY this.timestamp DESC"),
    @Query(
            name  = "findRecentByTarget",
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE target == :target "
                  + " ORDER BY this.timestamp DESC "
                  + " RANGE 0,30"),
    @Query(
            name  = "findByTargetAndTimestampBetween",
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE target == :target "
                  + "    && timestamp >= :from "
                  + "    && timestamp <= :to "
                  + " ORDER BY this.timestamp DESC"),
    @Query(
            name  = "findByTargetAndTimestampAfter",
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE target == :target "
                  + "    && timestamp >= :from "
                  + " ORDER BY this.timestamp DESC"),
    @Query(
            name  = "findByTargetAndTimestampBefore",
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE target == :target "
                  + "    && timestamp <= :to "
                  + " ORDER BY this.timestamp DESC"),
    @Query(
            name  = "findByTarget",
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE target == :target "
                  + " ORDER BY this.timestamp DESC"),
    @Query(
            name  = "findByTimestampBetween",
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE timestamp >= :from "
                  + "    && timestamp <= :to "
                  + " ORDER BY this.timestamp DESC"),
    @Query(
            name  = "findByTimestampAfter",
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE timestamp >= :from "
                  + " ORDER BY this.timestamp DESC"),
    @Query(
            name  = "findByTimestampBefore",
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE timestamp <= :to "
                  + " ORDER BY this.timestamp DESC"),
    @Query(
            name  = "find",
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " ORDER BY this.timestamp DESC"),
    @Query(
            name  = "findRecentByUsername",
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE username == :username "
                  + " ORDER BY this.timestamp DESC "
                  + " RANGE 0,30"),
    @Query(
            name  = "findFirst",
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE startedAt   != null "
                  + "    && completedAt != null "
                  + " ORDER BY this.timestamp ASC "
                  + " RANGE 0,2"), // this should be RANGE 0,1 but results in DataNucleus submitting "FETCH NEXT ROW ONLY"
                                   // which SQL Server doesn't understand.  However, as workaround, SQL Server *does* understand FETCH NEXT 2 ROWS ONLY
    @Query(
            name  = "findSince",
            value = "SELECT "
                  + "FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE timestamp > :timestamp "
                  + "   && startedAt != null "
                  + "   && completedAt != null "
                  + "ORDER BY this.timestamp ASC"),

    // most recent (replayed) command previously replicated from primary to
    // secondary.  This should always exist except for the very first times
    // (after restored the prod DB to secondary).
    @Query(
            name  = "findMostRecentReplayed",
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE (replayState == 'OK' || replayState == 'FAILED') "
                  + " ORDER BY this.timestamp DESC "
                  + " RANGE 0,2"), // this should be RANGE 0,1 but results in DataNucleus submitting "FETCH NEXT ROW ONLY"
                                   // which SQL Server doesn't understand.  However, as workaround, SQL Server *does* understand FETCH NEXT 2 ROWS ONLY

    // the most recent completed command, as queried on the
    // secondary, corresponding to the last command run on primary before the
    // production database was restored to the secondary
    @Query(
            name  = "findMostRecentCompleted",
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE startedAt   != null "
                  + "    && completedAt != null "
                  + " ORDER BY this.timestamp DESC "
                  + " RANGE 0,2"), // this should be RANGE 0,1 but results in DataNucleus submitting "FETCH NEXT ROW ONLY"
                                   // which SQL Server doesn't understand.  However, as workaround, SQL Server *does* understand FETCH NEXT 2 ROWS ONLY

    @Query(
            name  = "findNotYetReplayed",
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE replayState == 'PENDING' "
                  + " ORDER BY this.timestamp ASC "
                  + " RANGE 0,10"),    // same as batch size
})
@Named(CommandLogEntry.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED
)
//@Log4j2
@NoArgsConstructor
public class CommandLogEntry
extends org.apache.isis.extensions.commandlog.applib.dom.CommandLogEntry {

    protected final static String FQCN = "org.apache.isis.extensions.commandlog.jdo.entities.CommandJdo";

    /**
     * Intended for use on primary system.
     *
     * @param command
     */
    public CommandLogEntry(final Command command) {
        super(command);
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
            final org.apache.isis.extensions.commandlog.applib.dom.ReplayState replayState,
            final int targetIndex) {
        super(commandDto, replayState, targetIndex);
    }

    @PrimaryKey
    @Column(allowsNull = InteractionIdStr.ALLOWS_NULL, name = InteractionIdStr.NAME, length = InteractionIdStr.MAX_LENGTH)
    @InteractionIdStr
    @Getter @Setter
    private String interactionIdStr;


    @Column(allowsNull = Username.ALLOWS_NULL, length = Username.MAX_LENGTH)
    @Username
    @Getter @Setter
    private String username;


    @Column(allowsNull = Timestamp.ALLOWS_NULL)
    @Timestamp
    @Getter @Setter
    private java.sql.Timestamp timestamp;


    @Column(allowsNull = ReplayState.ALLOWS_NULL, length=ReplayState.MAX_LENGTH)
    @ReplayState
    @Getter @Setter
    private org.apache.isis.extensions.commandlog.applib.dom.ReplayState replayState;


    @Column(allowsNull = ReplayStateFailureReason.ALLOWS_NULL, length = ReplayStateFailureReason.MAX_LENGTH)
    @ReplayStateFailureReason
    @Getter @Setter
    private String replayStateFailureReason;


    @Column(name = Parent.NAME, allowsNull = Parent.ALLOWS_NULL)
    @Parent
    @Getter
    private CommandLogEntry parent;
    @Override
    public void setParent(final org.apache.isis.extensions.commandlog.applib.dom.CommandLogEntry parent) {
        this.parent = (CommandLogEntry)parent;
    }


    @Persistent
    @Column(allowsNull = Target.ALLOWS_NULL, length = Target.MAX_LENGTH)
    @Target
    @Getter @Setter
    private Bookmark target;


    @Column(allowsNull = LogicalMemberIdentifier.ALLOWS_NULL, length = LogicalMemberIdentifier.MAX_LENGTH)
    @LogicalMemberIdentifier
    @Getter @Setter
    private String logicalMemberIdentifier;


    @Persistent
    @Column(allowsNull = CommandDtoAnnot.ALLOWS_NULL, jdbcType = "CLOB")
    @CommandDtoAnnot
    @Getter @Setter
    private org.apache.isis.schema.cmd.v2.CommandDto commandDto;


    @Column(allowsNull = StartedAt.ALLOWS_NULL)
    @StartedAt
    @Getter @Setter
    private java.sql.Timestamp startedAt;


    @Column(allowsNull = CompletedAt.ALLOWS_NULL)
    @CompletedAt
    @Getter @Setter
    private java.sql.Timestamp completedAt;


    @Persistent
    @Column(allowsNull = Result.ALLOWS_NULL, length = Result.MAX_LENGTH)
    @Result
    @Getter @Setter
    private Bookmark result;


    @Persistent
    @Column(allowsNull = Exception.ALLOWS_NULL, jdbcType = "CLOB")
    @Getter @Setter
    private String exception;

}

