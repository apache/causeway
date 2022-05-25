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
package org.apache.isis.extensions.commandlog.jpa.dom;

import javax.inject.Named;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.persistence.jpa.applib.integration.IsisEntityListener;
import org.apache.isis.schema.cmd.v2.CommandDto;

import static org.apache.isis.extensions.commandlog.jpa.dom.CommandLogEntry.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        schema = CommandLogEntry.SCHEMA,
        name = CommandLogEntry.TABLE,
        indexes = {
                @Index(name = "CommandJdo__startedAt__timestamp__IDX", columnList = "startedAt, timestamp" ),
                @Index(name = "CommandJdo__timestamp__IDX", columnList = "timestamp"),
        }
)
@NamedQueries({
    @NamedQuery(
            name  = Nq.FIND_BY_INTERACTION_ID_STR,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE cl.interactionIdStr = :interactionIdStr"),
    @NamedQuery(
            name  = Nq.FIND_BY_PARENT,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE cl.parent = :parent "),
    @NamedQuery(
            name  = Nq.FIND_CURRENT,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE cl.completedAt is null "
                  + " ORDER BY cl.timestamp DESC"),
    @NamedQuery(
            name  = Nq.FIND_COMPLETED,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE cl.completedAt is not null "
                  + " ORDER BY cl.timestamp DESC"),
    @NamedQuery(
            name  = Nq.FIND_RECENT_BY_TARGET,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE cl.target = :target "
                  + " ORDER BY cl.timestamp DESC"), // programmatic LIMIT 30
    @NamedQuery(
            name  = Nq.FIND_BY_TARGET_AND_TIMESTAMP_BETWEEN,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE cl.target = :target "
                  + "   AND cl.timestamp >= :from "
                  + "   AND cl.timestamp <= :to "
                  + " ORDER BY cl.timestamp DESC"),
    @NamedQuery(
            name  = Nq.FIND_BY_TARGET_AND_TIMESTAMP_AFTER,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE cl.target = :target "
                  + "  AND cl.timestamp >= :from "
                  + " ORDER BY cl.timestamp DESC"),
    @NamedQuery(
            name  = Nq.FIND_BY_TARGET_AND_TIMESTAMP_BEFORE,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE cl.target = :target "
                  + "   AND cl.timestamp <= :to "
                  + " ORDER BY cl.timestamp DESC"),
    @NamedQuery(
            name  = Nq.FIND_BY_TARGET,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE cl.target = :target "
                  + " ORDER BY cl.timestamp DESC"),
    @NamedQuery(
            name  = Nq.FIND_BY_TIMESTAMP_BETWEEN,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE cl.timestamp >= :from "
                  + "   AND  cl.timestamp <= :to "
                  + " ORDER BY cl.timestamp DESC"),
    @NamedQuery(
            name  = Nq.FIND_BY_TIMESTAMP_AFTER,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE cl.timestamp >= :from "
                  + " ORDER BY cl.timestamp DESC"),
    @NamedQuery(
            name  = Nq.FIND_BY_TIMESTAMP_BEFORE,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE cl.timestamp <= :to "
                  + " ORDER BY cl.timestamp DESC"),
    @NamedQuery(
            name  = Nq.FIND,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " ORDER BY cl.timestamp DESC"),
    @NamedQuery(
            name  = Nq.FIND_RECENT_BY_USERNAME,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE cl.username = :username "
                  + " ORDER BY cl.timestamp DESC"), // programmatic LIMIT 30
    @NamedQuery(
            name  = Nq.FIND_FIRST,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE cl.startedAt   is not null "
                  + "   AND cl.completedAt is not null "
                  + " ORDER BY cl.timestamp ASC"), // programmatic LIMIT 1
    @NamedQuery(
            name  = Nq.FIND_SINCE,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE cl.timestamp > :timestamp "
                  + "   AND cl.startedAt is not null "
                  + "   AND cl.completedAt is not null "
                  + " ORDER BY cl.timestamp ASC"),
    // most recent (replayed) command previously replicated from primary to
    // secondary.  This should always exist except for the very first times
    // (after restored the prod DB to secondary).
    @NamedQuery(
            name  = Nq.FIND_MOST_RECENT_REPLAYED,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE (cl.replayState = 'OK' OR cl.replayState = 'FAILED') "
                  + " ORDER BY cl.timestamp DESC"), // programmatic LIMIT 1
    @NamedQuery(
            name  = Nq.FIND_MOST_RECENT_COMPLETED,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE cl.startedAt   is not null "
                  + "   AND cl.completedAt is not null "
                  + " ORDER BY cl.timestamp DESC"), // programmatic LIMIT 1
    @NamedQuery(
            name  = Nq.FIND_NOT_YET_REPLAYED,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE cl.replayState = 'PENDING' "
                  + " ORDER BY cl.timestamp ASC"), // programmatic LIMIT 10
})
@Named(CommandLogEntry.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED
)
@EntityListeners(IsisEntityListener.class)
@NoArgsConstructor
public class CommandLogEntry extends org.apache.isis.extensions.commandlog.applib.dom.CommandLogEntry {

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

    @Id
    @Column(nullable = InteractionIdStr.NULLABLE, name = InteractionIdStr.NAME, length = InteractionIdStr.MAX_LENGTH)
    @InteractionIdStr
    @Getter @Setter
    private String interactionIdStr;


    @Column(nullable = Username.NULLABLE, length = Username.MAX_LENGTH)
    @Username
    @Getter @Setter
    private String username;


    @Column(nullable = Timestamp.NULLABLE)
    @Timestamp
    @Getter @Setter
    private java.sql.Timestamp timestamp;


    @Column(nullable=true, length = ReplayState.MAX_LENGTH) @Enumerated(EnumType.STRING)
    @ReplayState
    @Getter @Setter
    private org.apache.isis.extensions.commandlog.applib.dom.ReplayState replayState;


    @Column(nullable=ReplayStateFailureReason.NULLABLE, length=ReplayStateFailureReason.MAX_LENGTH)
    @ReplayStateFailureReason
    @Getter @Setter
    private String replayStateFailureReason;


    @ManyToOne
    @JoinColumn(name = Parent.NAME, nullable = Parent.NULLABLE)
    @Parent
    @Getter
    private CommandLogEntry parent;
    @Override
    public void setParent(final org.apache.isis.extensions.commandlog.applib.dom.CommandLogEntry parent) {
        this.parent = (CommandLogEntry)parent;
    }


    @Column(nullable = Target.NULLABLE, length = Target.MAX_LENGTH)
    @Target
    @Getter @Setter
    private Bookmark target;


    @Column(nullable = LogicalMemberIdentifier.NULLABLE, length = LogicalMemberIdentifier.MAX_LENGTH)
    @LogicalMemberIdentifier
    @Getter @Setter
    private String logicalMemberIdentifier;


    @Lob @Basic(fetch = FetchType.LAZY)
    @Column(nullable = CommandDtoAnnot.NULLABLE, columnDefinition = "CLOB")
    @CommandDtoAnnot
    @Getter @Setter
    private CommandDto commandDto;


    @Column(nullable = StartedAt.NULLABLE)
    @StartedAt
    @Getter @Setter
    private java.sql.Timestamp startedAt;


    @Column(nullable = CompletedAt.NULLABLE)
    @CompletedAt
    @Getter @Setter
    private java.sql.Timestamp completedAt;


    @Column(nullable = Result.NULLABLE, length = Result.MAX_LENGTH)
    @Result
    @Getter @Setter
    private Bookmark result;


    @Lob @Basic(fetch = FetchType.LAZY)
    @Column(nullable = Exception.NULLABLE, columnDefinition = "CLOB")
    @Getter @Setter
    private String exception;

}
