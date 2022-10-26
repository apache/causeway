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
package org.apache.causeway.extensions.executionoutbox.jpa.dom;

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
import org.apache.causeway.extensions.executionoutbox.applib.dom.ExecutionOutboxEntry.Nq;
import org.apache.causeway.extensions.executionoutbox.applib.dom.ExecutionOutboxEntryType;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;
import org.apache.causeway.persistence.jpa.integration.typeconverters.applib.CausewayBookmarkConverter;
import org.apache.causeway.persistence.jpa.integration.typeconverters.schema.v2.CausewayInteractionDtoConverter;
import org.apache.causeway.schema.ixn.v2.InteractionDto;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        schema = ExecutionOutboxEntry.SCHEMA,
        name = ExecutionOutboxEntry.TABLE,
        indexes = {
                @Index(name = "ExecutionOutboxEntry__timestamp__IDX", columnList = "timestamp"),
                @Index(name = "ExecutionOutboxEntry__target__timestamp__IDX", columnList = "target, timestamp"),
                @Index(name = "ExecutionOutboxEntry__username__timestamp__IDX", columnList = "username, timestamp"),
        }
)
@NamedQueries( {
    @NamedQuery(
            name = Nq.FIND_BY_INTERACTION_ID_AND_SEQUENCE,
            query = "SELECT ele "
                  + "  FROM ExecutionOutboxEntry ele "
                  + " WHERE ele.pk.interactionId = :interactionId "
                  + "   AND ele.pk.sequence      = :sequence "),
    @NamedQuery(
            name = Nq.FIND_OLDEST,
            query = "SELECT ele "
                  + "  FROM ExecutionOutboxEntry ele "
                  + " ORDER BY ele.timestamp ASC, ele.pk.interactionId ASC, ele.pk.sequence DESC"),  // programmatic range 0,100
})
@Named(ExecutionOutboxEntry.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED,
        entityChangePublishing = Publishing.DISABLED
)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@EntityListeners(CausewayEntityListener.class)
public class ExecutionOutboxEntry extends org.apache.causeway.extensions.executionoutbox.applib.dom.ExecutionOutboxEntry {


    @EmbeddedId
    ExecutionOutboxEntryPK pk;


    @Transient
    @InteractionId
    @Override
    public UUID getInteractionId() {
        return pk != null ? pk.getInteractionId() : null;
    }
    @Transient
    @Override
    public void setInteractionId(UUID interactionId) {
        pk = new ExecutionOutboxEntryPK(interactionId, getSequence());
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
        pk = new ExecutionOutboxEntryPK(getInteractionId(), sequence);
    }


    @Column(nullable = ExecutionType.NULLABLE, length = ExecutionType.MAX_LENGTH)
    @Enumerated(EnumType.STRING)
    @ExecutionType
    @Getter @Setter
    private ExecutionOutboxEntryType executionType;


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
