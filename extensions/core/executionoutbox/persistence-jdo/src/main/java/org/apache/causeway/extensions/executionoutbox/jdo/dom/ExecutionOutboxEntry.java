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
package org.apache.causeway.extensions.executionoutbox.jdo.dom;

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

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.extensions.executionoutbox.applib.dom.ExecutionOutboxEntry.Nq;
import org.apache.causeway.extensions.executionoutbox.applib.dom.ExecutionOutboxEntryType;
import org.apache.causeway.schema.ixn.v2.InteractionDto;

import lombok.Getter;
import lombok.Setter;

@PersistenceCapable(
        identityType= IdentityType.APPLICATION,
        schema = ExecutionOutboxEntry.SCHEMA,
        table = ExecutionOutboxEntry.TABLE,
        objectIdClass= ExecutionOutboxEntryPK.class)
@Indices({
        @Index(name = "ExecutionOutboxEntry__timestamp__IDX", members = { "timestamp" }),
        @Index(name = "ExecutionOutboxEntry__target__timestamp__IDX", members = { "target", "timestamp" }),
        @Index(name = "ExecutionOutboxEntry__username__timestamp__IDX", members = { "username", "timestamp" }),
})
@Queries( {
    @Query(
            name = Nq.FIND_BY_INTERACTION_ID_AND_SEQUENCE,
            value = "SELECT "
                  + "  FROM " + ExecutionOutboxEntry.FQCN + " "
                  + " WHERE interactionId == :interactionId "
                  + "    && sequence      == :sequence "),
    @Query(
            name = Nq.FIND_OLDEST,
            value = "SELECT "
                  + "  FROM " + ExecutionOutboxEntry.FQCN + " "
                  + " ORDER BY timestamp ASC, interactionId ASC, sequence DESC"
                    + " RANGE 0,100"),
})
@Named(ExecutionOutboxEntry.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED,
        entityChangePublishing = Publishing.DISABLED
)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
public class ExecutionOutboxEntry extends org.apache.causeway.extensions.executionoutbox.applib.dom.ExecutionOutboxEntry {


    public static final String FQCN = "org.apache.causeway.extensions.executionlog.jdo.dom.ExecutionLogEntry";
    @PrimaryKey
    @InteractionId
    @Column(allowsNull = InteractionId.ALLOWS_NULL, length=InteractionId.MAX_LENGTH)
    @Getter @Setter
    private UUID interactionId;


    @PrimaryKey
    @Sequence
    @Column(allowsNull = Sequence.ALLOWS_NULL)
    @Getter @Setter
    private int sequence;


    @Column(allowsNull = ExecutionType.ALLOWS_NULL, length = ExecutionType.MAX_LENGTH)
    @ExecutionType
    @Getter @Setter
    private ExecutionOutboxEntryType executionType;


    @Column(allowsNull = Username.ALLOWS_NULL, length = Username.MAX_LENGTH)
    @Username
    @Getter @Setter
    private String username;


    @Persistent
    @Column(allowsNull = Timestamp.ALLOWS_NULL)
    @Timestamp
    @Getter @Setter
    private java.sql.Timestamp timestamp;


    @Persistent
    @Column(allowsNull = Target.ALLOWS_NULL, length = Target.MAX_LENGTH)
    @Target
    @Getter @Setter
    private Bookmark target;


    @Column(allowsNull = LogicalMemberIdentifier.ALLOWS_NULL, length= LogicalMemberIdentifier.MAX_LENGTH)
    @LogicalMemberIdentifier
    @Getter
    private String logicalMemberIdentifier;
    public void setLogicalMemberIdentifier(final String logicalMemberIdentifier) {
        this.logicalMemberIdentifier = Util.abbreviated(logicalMemberIdentifier, LogicalMemberIdentifier.MAX_LENGTH);
    }


    @Persistent
    @Column(allowsNull = InteractionDtoAnnot.ALLOWS_NULL, jdbcType = "CLOB", sqlType = "LONGVARCHAR")
    @InteractionDtoAnnot
    @Getter @Setter
    private InteractionDto interactionDto;


    @Persistent
    @Column(allowsNull = StartedAt.ALLOWS_NULL)
    @StartedAt
    @Getter @Setter
    private java.sql.Timestamp startedAt;


    @Persistent
    @Column(allowsNull = CompletedAt.ALLOWS_NULL)
    @CompletedAt
    @Getter @Setter
    private java.sql.Timestamp completedAt;



}
