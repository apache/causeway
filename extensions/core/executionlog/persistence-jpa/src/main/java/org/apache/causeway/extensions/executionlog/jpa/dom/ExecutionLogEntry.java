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
package org.apache.causeway.extensions.executionlog.jpa.dom;

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
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntry.Nq;
import org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntryType;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;
import org.apache.causeway.persistence.jpa.integration.typeconverters.applib.CausewayBookmarkConverter;
import org.apache.causeway.persistence.jpa.integration.typeconverters.schema.v2.CausewayInteractionDtoConverter;
import org.apache.causeway.schema.ixn.v2.InteractionDto;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        schema = ExecutionLogEntry.SCHEMA,
        name = ExecutionLogEntry.TABLE,
        indexes = {
                @Index(name = "ExecutionLogEntry__timestamp__IDX", columnList = "timestamp"),
                @Index(name = "ExecutionLogEntry__target_timestamp__IDX", columnList = "target, timestamp"),
                @Index(name = "ExecutionLogEntry__username_timestamp__IDX", columnList = "username, timestamp"),
        }
)
@NamedQueries( {
    @NamedQuery(
            name = Nq.FIND_BY_INTERACTION_ID,
            query = "SELECT ele "
                  + "  FROM ExecutionLogEntry ele "
                  + " WHERE ele.pk.interactionId = :interactionId "
                  + " ORDER BY ele.timestamp DESC, ele.pk.sequence DESC"),
    @NamedQuery(
            name = Nq.FIND_BY_INTERACTION_ID_AND_SEQUENCE,
            query = "SELECT ele "
                  + "  FROM ExecutionLogEntry ele "
                  + " WHERE ele.pk.interactionId = :interactionId "
                  + "   AND ele.pk.sequence      = :sequence "),
    @NamedQuery(
            name = Nq.FIND_BY_TARGET_AND_TIMESTAMP_BETWEEN,
            query = "SELECT ele "
                  + "  FROM ExecutionLogEntry ele "
                  + " WHERE ele.target = :target "
                  + "   AND ele.timestamp >= :timestampFrom "
                  + "   AND ele.timestamp <= :timestampTo "
                  + " ORDER BY ele.timestamp DESC, ele.pk.interactionId DESC, ele.pk.sequence DESC"),
    @NamedQuery(
            name = Nq.FIND_BY_TARGET_AND_TIMESTAMP_AFTER,
            query = "SELECT ele "
                  + "  FROM ExecutionLogEntry ele "
                  + " WHERE ele.target = :target "
                  + "   AND ele.timestamp >= :timestamp "
                  + " ORDER BY ele.timestamp DESC, ele.pk.interactionId DESC, ele.pk.sequence DESC"),
    @NamedQuery(
            name = Nq.FIND_BY_TARGET_AND_TIMESTAMP_BEFORE,
            query = "SELECT ele "
                  + "  FROM ExecutionLogEntry ele "
                  + " WHERE ele.target = :target "
                  + "   AND ele.timestamp <= :timestamp "
                  + " ORDER BY ele.timestamp DESC, ele.pk.interactionId DESC, ele.pk.sequence DESC"),
    @NamedQuery(
            name = Nq.FIND_BY_TARGET,
            query = "SELECT ele "
                  + "  FROM ExecutionLogEntry ele "
                  + " WHERE ele.target = :target "
                  + " ORDER BY ele.timestamp DESC, ele.pk.interactionId DESC, ele.pk.sequence DESC"),
    @NamedQuery(
            name = Nq.FIND_BY_TIMESTAMP_BETWEEN,
            query = "SELECT ele "
                  + "  FROM ExecutionLogEntry ele "
                  + " WHERE ele.timestamp >= :from "
                  + "   AND ele.timestamp <= :to "
                  + " ORDER BY ele.timestamp DESC, ele.pk.interactionId DESC, ele.pk.sequence DESC"),
    @NamedQuery(
            name = Nq.FIND_BY_TIMESTAMP_AFTER,
            query = "SELECT ele "
                  + "  FROM ExecutionLogEntry ele "
                  + " WHERE ele.timestamp >= :from "
                  + " ORDER BY ele.timestamp DESC, ele.pk.interactionId DESC, ele.pk.sequence DESC"),
    @NamedQuery(
            name = Nq.FIND_BY_TIMESTAMP_BEFORE,
            query = "SELECT ele "
                  + "  FROM ExecutionLogEntry ele "
                  + " WHERE ele.timestamp <= :to "
                  + " ORDER BY ele.timestamp DESC, ele.pk.interactionId, ele.pk.sequence DESC"),
    @NamedQuery(
            name  = Nq.FIND,
            query = "SELECT ele "
                  + "  FROM ExecutionLogEntry ele "
                  + " ORDER BY ele.timestamp DESC"),
    @NamedQuery(
            name = Nq.FIND_MOST_RECENT,
            query = "SELECT ele "
                  + "  FROM ExecutionLogEntry ele "
                  + " ORDER BY ele.timestamp DESC, ele.pk.interactionId DESC, ele.pk.sequence DESC"),  // programmatic limit 100
    @NamedQuery(
            name = Nq.FIND_RECENT_BY_USERNAME,
            query = "SELECT ele "
                  + "  FROM ExecutionLogEntry ele "
                  + " WHERE ele.username = :username "
                  + " ORDER BY ele.timestamp DESC, ele.pk.interactionId DESC, ele.pk.sequence DESC "), // programmatic limit 30
    @NamedQuery(
            name = Nq.FIND_RECENT_BY_TARGET,
            query = "SELECT ele "
                  + "  FROM ExecutionLogEntry ele "
                  + " WHERE ele.target = :target "
                  + " ORDER BY ele.timestamp DESC, ele.pk.interactionId DESC, ele.pk.sequence DESC ")  // programmatic limit 30
})
@Named(ExecutionLogEntry.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED,
        entityChangePublishing = Publishing.DISABLED
)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@EntityListeners(CausewayEntityListener.class)
public class ExecutionLogEntry extends org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntry {


    @EmbeddedId
    ExecutionLogEntryPK pk;


    @Transient
    @InteractionId
    @Override
    public UUID getInteractionId() {
        return pk != null ? pk.getInteractionId() : null;
    }
    @Transient
    @Override
    public void setInteractionId(UUID interactionId) {
        pk = new ExecutionLogEntryPK(interactionId, getSequence());
    }

    @Transient
    @Sequence
    @Override
    public int getSequence() {
        return pk != null? pk.getSequence() : 0;
    }
    @Transient
    @Override
    public void setSequence(int sequence) {
        pk = new ExecutionLogEntryPK(getInteractionId(), sequence);
    }


    @Column(nullable = ExecutionType.NULLABLE, length = ExecutionType.MAX_LENGTH)
    @Enumerated(EnumType.STRING)
    @ExecutionType
    @Getter @Setter
    private ExecutionLogEntryType executionType;


    @Column(nullable = Username.NULLABLE, length = Username.MAX_LENGTH)
    @Username
    @Getter @Setter
    private String username;


    @Column(nullable = Timestamp.NULLABLE)
    @Timestamp
    @Getter @Setter
    private java.sql.Timestamp timestamp;


    @Convert(converter = CausewayBookmarkConverter.class)
    @Column(nullable = Target.NULLABLE, length = Target.MAX_LENGTH)
    @Target
    @Getter @Setter
    private Bookmark target;


    @Column(nullable = LogicalMemberIdentifier.NULLABLE, length= LogicalMemberIdentifier.MAX_LENGTH)
    @LogicalMemberIdentifier
    @Getter
    private String logicalMemberIdentifier;
    public void setLogicalMemberIdentifier(final String logicalMemberIdentifier) {
        this.logicalMemberIdentifier = Util.abbreviated(logicalMemberIdentifier, LogicalMemberIdentifier.MAX_LENGTH);
    }

    @Convert(converter = CausewayInteractionDtoConverter.class)
    @Lob @Basic(fetch = FetchType.LAZY)
    @Column(nullable = InteractionDtoAnnot.NULLABLE, columnDefinition = "CLOB")
    @InteractionDtoAnnot
    @Getter @Setter
    private InteractionDto interactionDto;


    @Column(nullable = StartedAt.NULLABLE)
    @StartedAt
    @Getter @Setter
    private java.sql.Timestamp startedAt;


    @Column(nullable = CompletedAt.NULLABLE)
    @CompletedAt
    @Getter @Setter
    private java.sql.Timestamp completedAt;



}
