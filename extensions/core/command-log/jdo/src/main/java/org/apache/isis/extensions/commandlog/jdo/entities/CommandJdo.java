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
package org.apache.isis.extensions.commandlog.jdo.entities;

import java.sql.Timestamp;

import javax.jdo.annotations.IdentityType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.types.MemberIdentifierType;
import org.apache.isis.extensions.commandlog.applib.command.CommandLog;
import org.apache.isis.extensions.commandlog.applib.command.ReplayState;
import org.apache.isis.extensions.commandlog.jdo.IsisModuleExtCommandLogJdo;
import org.apache.isis.schema.cmd.v2.CommandDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@javax.jdo.annotations.PersistenceCapable(
        identityType = IdentityType.APPLICATION,
        schema = "isisExtensionsCommandLog",
        table = "Command")
@javax.jdo.annotations.Queries( {
    @javax.jdo.annotations.Query(
            name="findByInteractionIdStr",
            value="SELECT "
                    + "FROM " + CommandJdo.FQCN
                    + " WHERE interactionIdStr == :interactionIdStr "),
    @javax.jdo.annotations.Query(
            name="findByParent",
            value="SELECT "
                    + "FROM " + CommandJdo.FQCN
                    + " WHERE parent == :parent "),
    @javax.jdo.annotations.Query(
            name="findCurrent",
            value="SELECT "
                    + "FROM " + CommandJdo.FQCN
                    + " WHERE completedAt == null "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findCompleted",
            value="SELECT "
                    + "FROM " + CommandJdo.FQCN
                    + " WHERE completedAt != null "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findRecentByTarget",
            value="SELECT "
                    + "FROM " + CommandJdo.FQCN
                    + " WHERE target == :target "
                    + "ORDER BY this.timestamp DESC "
                    + "RANGE 0,30"),
    @javax.jdo.annotations.Query(
            name="findByTargetAndTimestampBetween",
            value="SELECT "
                    + "FROM " + CommandJdo.FQCN
                    + " WHERE target == :target "
                    + "&& timestamp >= :from "
                    + "&& timestamp <= :to "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTargetAndTimestampAfter",
            value="SELECT "
                    + "FROM " + CommandJdo.FQCN
                    + " WHERE target == :target "
                    + "&& timestamp >= :from "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTargetAndTimestampBefore",
            value="SELECT "
                    + "FROM " + CommandJdo.FQCN
                    + " WHERE target == :target "
                    + "&& timestamp <= :to "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTarget",
            value="SELECT "
                    + "FROM " + CommandJdo.FQCN
                    + " WHERE target == :target "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTimestampBetween",
            value="SELECT "
                    + "FROM " + CommandJdo.FQCN
                    + " WHERE timestamp >= :from "
                    + "&&    timestamp <= :to "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTimestampAfter",
            value="SELECT "
                    + "FROM " + CommandJdo.FQCN
                    + " WHERE timestamp >= :from "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTimestampBefore",
            value="SELECT "
                    + "FROM " + CommandJdo.FQCN
                    + " WHERE timestamp <= :to "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="find",
            value="SELECT "
                    + "FROM " + CommandJdo.FQCN
                    + " ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findRecentByUsername",
            value="SELECT "
                    + "FROM " + CommandJdo.FQCN
                    + " WHERE username == :username "
                    + "ORDER BY this.timestamp DESC "
                    + "RANGE 0,30"),
    @javax.jdo.annotations.Query(
            name="findFirst",
            value="SELECT "
                    + "FROM " + CommandJdo.FQCN
                    + " WHERE startedAt   != null "
                    + "   && completedAt != null "
                    + "ORDER BY this.timestamp ASC "
                    + "RANGE 0,2"),
        // this should be RANGE 0,1 but results in DataNucleus submitting "FETCH NEXT ROW ONLY"
        // which SQL Server doesn't understand.  However, as workaround, SQL Server *does* understand FETCH NEXT 2 ROWS ONLY
    @javax.jdo.annotations.Query(
            name="findSince",
            value="SELECT "
                    + "FROM " + CommandJdo.FQCN
                    + " WHERE timestamp > :timestamp "
                    + "   && startedAt != null "
                    + "   && completedAt != null "
                    + "ORDER BY this.timestamp ASC"),
    // most recent (replayed) command previously replicated from primary to
    // secondary.  This should always exist except for the very first times
    // (after restored the prod DB to secondary).
    @javax.jdo.annotations.Query(
            name="findMostRecentReplayed",
            value="SELECT "
                    + "FROM " + CommandJdo.FQCN
                    + " WHERE (replayState == 'OK' || replayState == 'FAILED') "
                    + "ORDER BY this.timestamp DESC "
                    + "RANGE 0,2"), // this should be RANGE 0,1 but results in DataNucleus submitting "FETCH NEXT ROW ONLY"
                                    // which SQL Server doesn't understand.  However, as workaround, SQL Server *does* understand FETCH NEXT 2 ROWS ONLY
    // the most recent completed command, as queried on the
    // secondary, corresponding to the last command run on primary before the
    // production database was restored to the secondary
    @javax.jdo.annotations.Query(
            name="findMostRecentCompleted",
            value="SELECT "
                    + "FROM " + CommandJdo.FQCN
                    + " WHERE startedAt   != null "
                    + "   && completedAt != null "
                    + "ORDER BY this.timestamp DESC "
                    + "RANGE 0,2"),
        // this should be RANGE 0,1 but results in DataNucleus submitting "FETCH NEXT ROW ONLY"
        // which SQL Server doesn't understand.  However, as workaround, SQL Server *does* understand FETCH NEXT 2 ROWS ONLY
    @javax.jdo.annotations.Query(
            name="findNotYetReplayed",
            value="SELECT "
                    + "FROM " + CommandJdo.FQCN
                    + " WHERE replayState == 'PENDING' "
                    + "ORDER BY this.timestamp ASC "
                    + "RANGE 0,10"),    // same as batch size
//        @javax.jdo.annotations.Query(
//                name="findReplayableInErrorMostRecent",
//                value="SELECT "
//                        + "FROM " + CommandJdo.FQCN
//                        + " WHERE replayState == 'FAILED' "
//                        + "ORDER BY this.timestamp DESC "
//                        + "RANGE 0,2"),
//    @javax.jdo.annotations.Query(
//            name="findReplayableMostRecentStarted",
//            value="SELECT "
//                    + "FROM " + CommandJdo.FQCN
//                    + " WHERE replayState = 'PENDING' "
//                    + "ORDER BY this.timestamp DESC "
//                    + "RANGE 0,20"),
})
@javax.jdo.annotations.Indices({
        @javax.jdo.annotations.Index(name = "CommandJdo__startedAt__timestamp__IDX", members = { "startedAt", "timestamp" }),
        @javax.jdo.annotations.Index(name = "CommandJdo__timestamp__IDX", members = { "timestamp" }),
//        @javax.jdo.annotations.Index(name = "CommandJdo__replayState__timestamp__startedAt_IDX", members = { "replayState", "timestamp", "startedAt"}),
//        @javax.jdo.annotations.Index(name = "CommandJdo__replayState__startedAt__completedAt_IDX", members = {"startedAt", "replayState", "completedAt"}),
})

//    @javax.jdo.annotations.Query(
//            name="findReplayableInErrorMostRecent",
//            value="SELECT "
//                    + "FROM " + CommandJdo.FQCN
//                    + " WHERE replayState == 'FAILED' "
//                    + "ORDER BY this.timestamp DESC "
//                    + "RANGE 0,2"),
//    @javax.jdo.annotations.Query(
//            name="findReplayableMostRecentStarted",
//            value="SELECT "
//                    + "FROM " + CommandJdo.FQCN
//                    + " WHERE replayState = 'PENDING' "
//                    + "ORDER BY this.timestamp DESC "
//                    + "RANGE 0,20"),
@DomainObject(
        logicalTypeName = CommandJdo.LOGICAL_TYPE_NAME,
        editing = Editing.DISABLED
)
//@Log4j2
@NoArgsConstructor
public class CommandJdo
extends CommandLog {

    protected final static String LOGICAL_TYPE_NAME = IsisModuleExtCommandLogJdo.NAMESPACE + ".CommandJdo";
    protected final static String FQCN = "org.apache.isis.extensions.commandlog.jdo.entities.CommandJdo";

    /**
     * Intended for use on primary system.
     *
     * @param command
     */
    public CommandJdo(final Command command) {
        super(command);
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
        super(commandDto, replayState, targetIndex);
    }

    @javax.jdo.annotations.PrimaryKey
    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="false", name = "interactionId", length = 36)
    @Getter @Setter
    private String interactionIdStr;

    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="false", length = 50)
    @Getter @Setter
    private String username;

    @javax.jdo.annotations.Column(allowsNull="false")
    @Getter @Setter
    private Timestamp timestamp;

    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true", length=10)
    @Getter @Setter
    private ReplayState replayState;

    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true", length=255)
    @Getter @Setter
    private String replayStateFailureReason;

    @javax.jdo.annotations.Column(name="parentId", allowsNull="true")
    @Getter @Setter
    private CommandLog parent;

    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true", length = 2000, name="target")
    @Getter @Setter
    private Bookmark target;

    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="false", length = MemberIdentifierType.Meta.MAX_LEN)
    @Getter @Setter
    private String logicalMemberIdentifier;

    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true", jdbcType="CLOB")
    @Getter @Setter
    private CommandDto commandDto;

    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true")
    @Getter @Setter
    private Timestamp startedAt;

    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true")
    @Getter @Setter
    private Timestamp completedAt;

    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true", length = 2000, name="result")
    @Getter @Setter
    private Bookmark result;

    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true", jdbcType="CLOB")
    @Getter @Setter
    private String exception;

}

