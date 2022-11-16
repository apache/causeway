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
package org.apache.causeway.extensions.commandlog.jdo.dom;

import java.util.UUID;

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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.Domain;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry.Nq;
import org.apache.causeway.schema.cmd.v2.CommandDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@PersistenceCapable(
        identityType = IdentityType.APPLICATION,
        schema = CommandLogEntry.SCHEMA,
        table = CommandLogEntry.TABLE)
@Indices({
        @Index(name = "CommandLogEntry__startedAt_timestamp__IDX", members = { "startedAt", "timestamp" }),
        @Index(name = "CommandLogEntry__timestamp__IDX", members = { "timestamp" }),
})
@Queries( {
    @Query(
            name  = Nq.FIND_BY_INTERACTION_ID,
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE interactionId == :interactionId "),
    @Query(
            name  = Nq.FIND_RECENT_BY_TARGET,
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE target == :target "
                  + " ORDER BY timestamp DESC "
                  + " RANGE 0,30"),
    @Query(
            name  = Nq.FIND_RECENT_BY_TARGET_OR_RESULT,
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE target == :targetOrResult "
                  + "    || result == :targetOrResult "
                  + " ORDER BY timestamp DESC "
                  + " RANGE 0,30"),
    @Query(
            name  = Nq.FIND_BY_TARGET_AND_TIMESTAMP_BETWEEN,
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE target == :target "
                  + "    && timestamp >= :from "
                  + "    && timestamp <= :to "
                  + " ORDER BY timestamp DESC"),
    @Query(
            name  = Nq.FIND_BY_TARGET_AND_TIMESTAMP_AFTER,
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE target == :target "
                  + "    && timestamp >= :from "
                  + " ORDER BY timestamp DESC"),
    @Query(
            name  = Nq.FIND_BY_TARGET_AND_TIMESTAMP_BEFORE,
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE target == :target "
                  + "    && timestamp <= :to "
                  + " ORDER BY timestamp DESC"),
    @Query(
            name  = Nq.FIND_BY_TARGET,
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE target == :target "
                  + " ORDER BY timestamp DESC"),
    @Query(
            name  = Nq.FIND_BY_TIMESTAMP_BETWEEN,
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE timestamp >= :from "
                  + "    && timestamp <= :to "
                  + " ORDER BY timestamp DESC"),
    @Query(
            name  = Nq.FIND_BY_TIMESTAMP_AFTER,
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE timestamp >= :from "
                  + " ORDER BY timestamp DESC"),
    @Query(
            name  = Nq.FIND_BY_TIMESTAMP_BEFORE,
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE timestamp <= :to "
                  + " ORDER BY timestamp DESC"),
    @Query(
            name  = Nq.FIND,
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " ORDER BY timestamp DESC"),
    @Query(
            name = Nq.FIND_MOST_RECENT,
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " ORDER BY timestamp DESC, interactionId DESC"
                  + " RANGE 0,100"),
    @Query(
            name  = Nq.FIND_RECENT_BY_USERNAME,
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE username == :username "
                  + " ORDER BY timestamp DESC "
                  + " RANGE 0,30"),
    @Query(
            name  = Nq.FIND_BY_PARENT_INTERACTION_ID,
            value = "SELECT "
                    + "  FROM " + CommandLogEntry.FQCN + " "
                    + " WHERE parentInteractionId == :parentInteractionId "),
    @Query(
            name  = Nq.FIND_CURRENT,
            value = "SELECT "
                    + "  FROM " + CommandLogEntry.FQCN + " "
                    + " WHERE completedAt == null "
                    + " ORDER BY timestamp DESC"),
    @Query(
            name  = Nq.FIND_COMPLETED,
            value = "SELECT "
                    + "  FROM " + CommandLogEntry.FQCN + " "
                    + " WHERE completedAt != null "
                    + " ORDER BY timestamp DESC"),
    @Query(
            name  = Nq.FIND_FIRST,
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE startedAt   != null "
                  + "    && completedAt != null "
                  + " ORDER BY timestamp ASC "
                  + " RANGE 0,2"), // this should be RANGE 0,1 but results in DataNucleus submitting "FETCH NEXT ROW ONLY"
                                   // which SQL Server doesn't understand.  However, as workaround, SQL Server *does* understand FETCH NEXT 2 ROWS ONLY
    @Query(
            name  = Nq.FIND_SINCE,
            value = "SELECT "
                  + "FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE timestamp > :timestamp "
                  + "   && startedAt != null "
                  + "   && completedAt != null "
                  + "ORDER BY timestamp ASC"),
    @Query(
            name  = Nq.FIND_BACKGROUND_AND_NOT_YET_STARTED,
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE executeIn == 'BACKGROUND' "
                  + "    && startedAt == null "
                  + " ORDER BY timestamp ASC "),
    @Query(
            name  = Nq.FIND_RECENT_BACKGROUND_BY_TARGET,
            value = "SELECT "
                    + "  FROM " + CommandLogEntry.FQCN + " "
                    + " WHERE executeIn == 'BACKGROUND' "
                    + "    && target    == :target "
                    + " ORDER BY timestamp DESC"),
    @Query(
            name  = Nq.FIND_MOST_RECENT_REPLAYED,
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE (replayState == 'OK' || replayState == 'FAILED') "
                  + " ORDER BY timestamp DESC "
                  + " RANGE 0,2"), // this should be RANGE 0,1 but results in DataNucleus submitting "FETCH NEXT ROW ONLY"
                                   // which SQL Server doesn't understand.  However, as workaround, SQL Server *does* understand FETCH NEXT 2 ROWS ONLY

    @Query(
            name  = Nq.FIND_MOST_RECENT_COMPLETED,
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE startedAt   != null "
                  + "    && completedAt != null "
                  + " ORDER BY timestamp DESC "
                  + " RANGE 0,2"), // this should be RANGE 0,1 but results in DataNucleus submitting "FETCH NEXT ROW ONLY"
                                   // which SQL Server doesn't understand.  However, as workaround, SQL Server *does* understand FETCH NEXT 2 ROWS ONLY
    @Query(
            name  = Nq.FIND_BY_REPLAY_STATE,
            value = "SELECT "
                  + "  FROM " + CommandLogEntry.FQCN + " "
                  + " WHERE replayState == :replayState "
                  + " ORDER BY timestamp ASC "
                  + " RANGE 0,10"),    // same as batch size
})
@Named(CommandLogEntry.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED,
        entityChangePublishing = Publishing.DISABLED
)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@NoArgsConstructor
public class CommandLogEntry
extends org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry {

    protected final static String FQCN = "org.apache.causeway.extensions.commandlog.jdo.dom.CommandLogEntry";


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
        super(commandDto, replayState, targetIndex);
    }

    @PrimaryKey
    @Column(allowsNull = InteractionId.ALLOWS_NULL, length = InteractionId.MAX_LENGTH)
    @InteractionId
    @Getter @Setter
    private UUID interactionId;


    @Column(allowsNull = Username.ALLOWS_NULL, length = Username.MAX_LENGTH)
    @Username
    @Getter @Setter
    private String username;


    @Column(allowsNull = Timestamp.ALLOWS_NULL)
    @Timestamp
    @Getter @Setter
    private java.sql.Timestamp timestamp;


    @Persistent
    @Column(allowsNull = Target.ALLOWS_NULL, length = Target.MAX_LENGTH)
    @Target
    @Getter @Setter
    private Bookmark target;


    @Column(allowsNull = ExecuteIn.ALLOWS_NULL, length = ExecuteIn.MAX_LENGTH)
    @ExecuteIn
    @Getter @Setter
    private org.apache.causeway.extensions.commandlog.applib.dom.ExecuteIn executeIn;


    @Column(allowsNull = Parent.ALLOWS_NULL, length = InteractionId.MAX_LENGTH)
    @Domain.Exclude
    @Getter @Setter
    private UUID parentInteractionId;


    @Column(allowsNull = LogicalMemberIdentifier.ALLOWS_NULL, length = LogicalMemberIdentifier.MAX_LENGTH)
    @LogicalMemberIdentifier
    @Getter @Setter
    private String logicalMemberIdentifier;


    @Persistent
    @Column(allowsNull = CommandDtoAnnot.ALLOWS_NULL, jdbcType = "CLOB")
    @CommandDtoAnnot
    @Getter @Setter
    private CommandDto commandDto;


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
    @Exception
    @Getter @Setter
    private String exception;


    @Column(allowsNull = ReplayState.ALLOWS_NULL, length = ReplayState.MAX_LENGTH)
    @ReplayState
    @Getter @Setter
    private org.apache.causeway.extensions.commandlog.applib.dom.ReplayState replayState;


    @Column(allowsNull = ReplayStateFailureReason.ALLOWS_NULL, length = ReplayStateFailureReason.MAX_LENGTH)
    @ReplayStateFailureReason
    @Getter @Setter
    private String replayStateFailureReason;

}
