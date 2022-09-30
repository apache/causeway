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

import java.util.UUID;

import javax.inject.Named;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.jaxb.PersistentEntityAdapter;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.persistence.jpa.applib.integration.IsisEntityListener;
import org.apache.isis.persistence.jpa.integration.typeconverters.applib.IsisBookmarkConverter;
import org.apache.isis.persistence.jpa.integration.typeconverters.schema.v2.IsisCommandDtoConverter;
import org.apache.isis.schema.cmd.v2.CommandDto;

import static org.apache.isis.extensions.commandlog.jpa.dom.CommandLogEntry.Nq;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        schema = CommandLogEntry.SCHEMA,
        name = CommandLogEntry.TABLE,
        indexes = {
                @Index(name = "CommandLogEntry__startedAt_timestamp__IDX", columnList = "startedAt, timestamp" ),
                @Index(name = "CommandLogEntry__timestamp__IDX", columnList = "timestamp"),
                @Index(name = "CommandLogEntry__target__IDX", columnList = "target"),
                @Index(name = "CommandLogEntry__target_startedAt__IDX", columnList = "target, startedAt"),
        }
)
@NamedQueries({
    @NamedQuery(
            name  = Nq.FIND_BY_INTERACTION_ID,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE cl.pk.interactionId = :interactionId"),
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
            name  = Nq.FIND_RECENT_BY_TARGET_OR_RESULT,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE cl.target = :targetOrResult "
                  + "    OR cl.result = :targetOrResult "
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
            name = Nq.FIND_MOST_RECENT,
            query = "SELECT cl "
                    + "  FROM CommandLogEntry cl "
                    + " ORDER BY cl.timestamp DESC, cl.pk.interactionId DESC"), // programmatic LIMIT 30
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
                  + " WHERE (cl.replayState = org.apache.isis.extensions.commandlog.applib.dom.ReplayState.OK OR cl.replayState = org.apache.isis.extensions.commandlog.applib.dom.ReplayState.FAILED) "
                  + " ORDER BY cl.timestamp DESC"), // programmatic LIMIT 1
    @NamedQuery(
            name  = Nq.FIND_MOST_RECENT_COMPLETED,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE cl.startedAt   is not null "
                  + "   AND cl.completedAt is not null "
                  + " ORDER BY cl.timestamp DESC"), // programmatic LIMIT 1
    @NamedQuery(
            name  = Nq.FIND_BY_REPLAY_STATE,
            query = "SELECT cl "
                  + "  FROM CommandLogEntry cl "
                  + " WHERE cl.replayState = :replayState "
                  + " ORDER BY cl.timestamp ASC"), // programmatic LIMIT 10
})
@Named(CommandLogEntry.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED,
        entityChangePublishing = Publishing.DISABLED
)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@EntityListeners(IsisEntityListener.class)
@NoArgsConstructor
public class CommandLogEntry extends org.apache.isis.extensions.commandlog.applib.dom.CommandLogEntry {

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


    @EmbeddedId
    private CommandLogEntryPK pk;


    @Transient
    @InteractionId
    @Override
    public UUID getInteractionId() {
        return pk != null ? pk.getInteractionId() : null;
    }
    @Transient
    @Override
    public void setInteractionId(UUID interactionId) {
        this.pk = new CommandLogEntryPK(interactionId);
    }


    @Column(nullable = Username.NULLABLE, length = Username.MAX_LENGTH)
    @Username
    @Getter @Setter
    private String username;


    @Column(nullable = Timestamp.NULLABLE)
    @Timestamp
    @Getter @Setter
    private java.sql.Timestamp timestamp;


    @Convert(converter = IsisBookmarkConverter.class)
    @Column(nullable = Target.NULLABLE, length = Target.MAX_LENGTH)
    @Target
    @Getter @Setter
    private Bookmark target;


    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = Parent.NAME, nullable = Parent.NULLABLE, referencedColumnName = InteractionId.NAME)
    })
    @Parent
    @Getter
    private CommandLogEntry parent;
    @Override
    public void setParent(final org.apache.isis.extensions.commandlog.applib.dom.CommandLogEntry parent) {
        this.parent = (CommandLogEntry)parent;
    }


    @Column(nullable = LogicalMemberIdentifier.NULLABLE, length = LogicalMemberIdentifier.MAX_LENGTH)
    @LogicalMemberIdentifier
    @Getter @Setter
    private String logicalMemberIdentifier;


    @Convert(converter = IsisCommandDtoConverter.class)
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


    @Convert(converter = IsisBookmarkConverter.class)
    @Column(nullable = Result.NULLABLE, length = Result.MAX_LENGTH)
    @Result
    @Getter @Setter
    private Bookmark result;


    @Lob @Basic(fetch = FetchType.LAZY)
    @Column(nullable = Exception.NULLABLE, columnDefinition = "CLOB")
    @Exception
    @Getter @Setter
    private String exception;


    @Column(nullable = ReplayState.NULLABLE, length = ReplayState.MAX_LENGTH)
    @Enumerated(EnumType.STRING)
    @ReplayState
    @Getter @Setter
    private org.apache.isis.extensions.commandlog.applib.dom.ReplayState replayState;


    @Column(nullable = ReplayStateFailureReason.NULLABLE, length = ReplayStateFailureReason.MAX_LENGTH)
    @ReplayStateFailureReason
    @Getter @Setter
    private String replayStateFailureReason;

}
