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
package org.apache.isis.extensions.commandlog.jpa.entities;

import java.sql.Timestamp;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
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
import org.apache.isis.applib.types.MemberIdentifierType;
import org.apache.isis.extensions.commandlog.applib.command.CommandLog;
import org.apache.isis.extensions.commandlog.applib.command.ReplayState;
import org.apache.isis.extensions.commandlog.jpa.IsisModuleExtCommandLogJpa;
import org.apache.isis.persistence.jpa.applib.integration.IsisEntityListener;
import org.apache.isis.schema.cmd.v2.CommandDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        schema = "isisExtensionsCommandLog",
        name = "Command",
        indexes = {
                @Index(name = "CommandJdo__startedAt__timestamp__IDX", columnList = "startedAt, timestamp" ),
                @Index(name = "CommandJdo__timestamp__IDX", columnList = "timestamp"),
//              @javax.jdo.annotations.Index(name = "CommandJdo__replayState__timestamp__startedAt_IDX", members = { "replayState", "timestamp", "startedAt"}),
//              @javax.jdo.annotations.Index(name = "CommandJdo__replayState__startedAt__completedAt_IDX", members = {"startedAt", "replayState", "completedAt"}),
        }
)
@NamedQueries({
    @NamedQuery(
            name="findByInteractionIdStr",
            query=CommandJpa.SELECT_FROM
                    + "WHERE cl.interactionIdStr = :interactionIdStr"),
    @NamedQuery(
            name="findByParent",
            query=CommandJpa.SELECT_FROM
                    + "WHERE cl.parent = :parent "),
    @NamedQuery(
            name="findCurrent",
            query=CommandJpa.SELECT_FROM
                    + "WHERE cl.completedAt is null "
                    + "ORDER BY cl.timestamp DESC"),
    @NamedQuery(
            name="findCompleted",
            query=CommandJpa.SELECT_FROM
                    + "WHERE cl.completedAt is not null "
                    + "ORDER BY cl.timestamp DESC"),
    @NamedQuery(
            name="findRecentByTarget",
            query=CommandJpa.SELECT_FROM
                    + "WHERE cl.target = :target "
                    + "ORDER BY cl.timestamp DESC"), // programmatic LIMIT 30
    @NamedQuery(
            name="findByTargetAndTimestampBetween",
            query=CommandJpa.SELECT_FROM
                    + "WHERE cl.target = :target "
                    + " AND cl.timestamp >= :from "
                    + " AND cl.timestamp <= :to "
                    + "ORDER BY cl.timestamp DESC"),
    @NamedQuery(
            name="findByTargetAndTimestampAfter",
            query=CommandJpa.SELECT_FROM
                    + "WHERE cl.target = :target "
                    + " AND cl.timestamp >= :from "
                    + "ORDER BY cl.timestamp DESC"),
    @NamedQuery(
            name="findByTargetAndTimestampBefore",
            query=CommandJpa.SELECT_FROM
                    + "WHERE cl.target = :target "
                    + " AND cl.timestamp <= :to "
                    + "ORDER BY cl.timestamp DESC"),
    @NamedQuery(
            name="findByTarget",
            query=CommandJpa.SELECT_FROM
                    + "WHERE cl.target = :target "
                    + "ORDER BY cl.timestamp DESC"),
    @NamedQuery(
            name="findByTimestampBetween",
            query=CommandJpa.SELECT_FROM
                    + "WHERE cl.timestamp >= :from "
                    + " AND  cl.timestamp <= :to "
                    + "ORDER BY cl.timestamp DESC"),
    @NamedQuery(
            name="findByTimestampAfter",
            query=CommandJpa.SELECT_FROM
                    + "WHERE cl.timestamp >= :from "
                    + "ORDER BY cl.timestamp DESC"),
    @NamedQuery(
            name="findByTimestampBefore",
            query=CommandJpa.SELECT_FROM
                    + "WHERE cl.timestamp <= :to "
                    + "ORDER BY cl.timestamp DESC"),
    @NamedQuery(
            name="find",
            query=CommandJpa.SELECT_FROM
                    + "ORDER BY cl.timestamp DESC"),
    @NamedQuery(
            name="findRecentByUsername",
            query=CommandJpa.SELECT_FROM
                    + "WHERE cl.username = :username "
                    + "ORDER BY cl.timestamp DESC"), // programmatic LIMIT 30
    @NamedQuery(
            name="findFirst",
            query=CommandJpa.SELECT_FROM
                    + "WHERE cl.startedAt   is not null "
                    + "   AND cl.completedAt is not null "
                    + "ORDER BY cl.timestamp ASC"), // programmatic LIMIT 1
    @NamedQuery(
            name="findSince",
            query=CommandJpa.SELECT_FROM
                    + "WHERE cl.timestamp > :timestamp "
                    + "   AND cl.startedAt is not null "
                    + "   AND cl.completedAt is not null "
                    + "ORDER BY cl.timestamp ASC"),
    // most recent (replayed) command previously replicated from primary to
    // secondary.  This should always exist except for the very first times
    // (after restored the prod DB to secondary).
    @NamedQuery(
            name="findMostRecentReplayed",
            query=CommandJpa.SELECT_FROM
                    + "WHERE (cl.replayState = 'OK' OR cl.replayState = 'FAILED') "
                    + "ORDER BY cl.timestamp DESC"), // programmatic LIMIT 1
    @NamedQuery(
            name="findMostRecentCompleted",
            query=CommandJpa.SELECT_FROM
                    + "WHERE cl.startedAt   is not null "
                    + "   AND cl.completedAt is not null "
                    + "ORDER BY cl.timestamp DESC"), // programmatic LIMIT 1
    @NamedQuery(
            name="findNotYetReplayed",
            query=CommandJpa.SELECT_FROM
                    + "WHERE cl.replayState = 'PENDING' "
                    + "ORDER BY cl.timestamp ASC"), // programmatic LIMIT 10
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
        logicalTypeName = CommandJpa.LOGICAL_TYPE_NAME,
        editing = Editing.DISABLED)
@EntityListeners(IsisEntityListener.class)
@NoArgsConstructor
public class CommandJpa extends CommandLog {

    public final static String LOGICAL_TYPE_NAME = IsisModuleExtCommandLogJpa.NAMESPACE + ".CommandJpa";

    public final static String SELECT_FROM = "SELECT cl FROM CommandJpa cl ";

    /**
     * Intended for use on primary system.
     *
     * @param command
     */
    public CommandJpa(final Command command) {
        super(command);
    }

    /**
     * Intended for use on secondary (replay) system.
     *
     * @param commandDto - obtained from the primary system as a representation of a command invocation
     * @param replayState - controls whether this is to be replayed
     * @param targetIndex - if the command represents a bulk action, then it is flattened out when replayed; this indicates which target to execute against.
     */
    public CommandJpa(
            final CommandDto commandDto,
            final ReplayState replayState,
            final int targetIndex) {
        super(commandDto, replayState, targetIndex);
    }

    @Id
    @Column(nullable=false, name = "interactionId", length = 36)
    @Getter @Setter
    private String interactionIdStr;

    @Column(nullable=false, length = 50)
    @Getter @Setter
    private String username;

    @Column(nullable=false)
    @Getter @Setter
    private Timestamp timestamp;

    @Column(nullable=true, length=10)
    @Getter @Setter
    private ReplayState replayState;

    @Column(nullable=true, length=255)
    @Getter @Setter
    private String replayStateFailureReason;

    @ManyToOne
    @JoinColumn(name="parentId", nullable=true)
    private CommandJpa parent;
    @Override
    public CommandJpa getParent() {
        return parent;
    }
    @Override
    public void setParent(final CommandLog parent) {
        this.parent = (CommandJpa)parent;
    }

    @Column(nullable=true, length = 2000, name="target")
    @Getter @Setter
    private Bookmark target;

    @Column(nullable=false, length = MemberIdentifierType.Meta.MAX_LEN)
    @Getter @Setter
    private String logicalMemberIdentifier;

    @Lob @Basic(fetch=FetchType.LAZY)
    @Column(nullable=true, columnDefinition="CLOB")
    @Getter @Setter
    private CommandDto commandDto;

    @Column(nullable=true)
    @Getter @Setter
    private Timestamp startedAt;

    @Column(nullable=true)
    @Getter @Setter
    private Timestamp completedAt;

    @Column(nullable=true, length = 2000, name="result")
    @Getter @Setter
    private Bookmark result;

    @Lob @Basic(fetch=FetchType.LAZY)
    @Column(nullable=true, columnDefinition="CLOB")
    @Getter @Setter
    private String exception;

}
