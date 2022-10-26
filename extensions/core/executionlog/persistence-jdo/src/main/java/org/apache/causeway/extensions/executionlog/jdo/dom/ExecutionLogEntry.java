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
package org.apache.causeway.extensions.executionlog.jdo.dom;

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
import org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntry.Nq;
import org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntryType;
import org.apache.causeway.schema.ixn.v2.InteractionDto;

import lombok.Getter;
import lombok.Setter;

@PersistenceCapable(
        identityType= IdentityType.APPLICATION,
        schema = ExecutionLogEntry.SCHEMA,
        table = ExecutionLogEntry.TABLE,
        objectIdClass= ExecutionLogEntryPK.class)
@Indices({
        @Index(name = "ExecutionLogEntry__timestamp__IDX", members = { "timestamp" }),
        @Index(name = "ExecutionLogEntry__target_timestamp__IDX", members = { "target", "timestamp" }),
        @Index(name = "ExecutionLogEntry__username_timestamp__IDX", members = { "username", "timestamp" }),
})
@Queries( {
    @Query(
            name = Nq.FIND_BY_INTERACTION_ID,
            value = "SELECT "
                  + "  FROM " + ExecutionLogEntry.FQCN + " "
                  + " WHERE interactionId == :interactionId "
                  + " ORDER BY timestamp DESC, sequence DESC"),
    @Query(
            name = Nq.FIND_BY_INTERACTION_ID_AND_SEQUENCE,
            value = "SELECT "
                  + "  FROM " + ExecutionLogEntry.FQCN + " "
                  + " WHERE interactionId == :interactionId "
                  + "    && sequence      == :sequence "),
    @Query(
            name = Nq.FIND_BY_TARGET_AND_TIMESTAMP_BETWEEN,
            value = "SELECT "
                  + "  FROM " + ExecutionLogEntry.FQCN + " "
                  + " WHERE target == :target "
                  + "    && timestamp >= :timestampFrom "
                  + "    && timestamp <= :timestampTo "
                  + " ORDER BY timestamp DESC, interactionId DESC, sequence DESC"),
    @Query(
            name = Nq.FIND_BY_TARGET_AND_TIMESTAMP_AFTER,
            value = "SELECT "
                  + "  FROM " + ExecutionLogEntry.FQCN + " "
                  + " WHERE target == :target "
                  + "    && timestamp >= :timestamp "
                  + " ORDER BY timestamp DESC, interactionId DESC, sequence DESC"),
    @Query(
            name = Nq.FIND_BY_TARGET_AND_TIMESTAMP_BEFORE,
            value = "SELECT "
                  + "  FROM " + ExecutionLogEntry.FQCN + " "
                  + " WHERE target == :target "
                  + "    && timestamp <= :timestamp "
                  + " ORDER BY timestamp DESC, interactionId DESC, sequence DESC"),
    @Query(
            name = Nq.FIND_BY_TARGET,
            value = "SELECT "
                  + "  FROM " + ExecutionLogEntry.FQCN + " "
                  + " WHERE target == :target "
                  + " ORDER BY timestamp DESC, interactionId DESC, sequence DESC"),
    @Query(
            name = Nq.FIND_BY_TIMESTAMP_BETWEEN,
            value = "SELECT "
                  + "  FROM " + ExecutionLogEntry.FQCN + " "
                  + " WHERE timestamp >= :from "
                  + "    && timestamp <= :to "
                  + " ORDER BY timestamp DESC, interactionId DESC, sequence DESC"),
    @Query(
            name = Nq.FIND_BY_TIMESTAMP_AFTER,
            value = "SELECT "
                  + "  FROM " + ExecutionLogEntry.FQCN + " "
                  + " WHERE timestamp >= :from "
                  + " ORDER BY timestamp DESC, interactionId DESC, sequence DESC"),
    @Query(
            name = Nq.FIND_BY_TIMESTAMP_BEFORE,
            value = "SELECT "
                  + "  FROM " + ExecutionLogEntry.FQCN + " "
                  + " WHERE timestamp <= :to "
                  + " ORDER BY timestamp DESC, interactionId DESC, sequence DESC"),
    @Query(
            name  = Nq.FIND,
            value = "SELECT "
                  + "  FROM " + ExecutionLogEntry.FQCN + " "
                  + " ORDER BY timestamp DESC"),
    @Query(
            name = Nq.FIND_MOST_RECENT,
            value = "SELECT "
                  + "  FROM " + ExecutionLogEntry.FQCN + " "
                  + " ORDER BY timestamp DESC, interactionId DESC, sequence DESC"
                  + " RANGE 0,100"),
    @Query(
            name = Nq.FIND_RECENT_BY_USERNAME,
            value = "SELECT "
                  + "  FROM " + ExecutionLogEntry.FQCN + " "
                  + " WHERE username == :username "
                  + " ORDER BY timestamp DESC, interactionId DESC, sequence DESC "
                  + " RANGE 0,30"),
    @Query(
            name = Nq.FIND_RECENT_BY_TARGET,
            value = "SELECT "
                  + "  FROM " + ExecutionLogEntry.FQCN + " "
                  + " WHERE target == :target "
                  + " ORDER BY timestamp DESC, interactionId DESC, sequence DESC "
                  + " RANGE 0,30")
})
@Named(ExecutionLogEntry.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED,
        entityChangePublishing = Publishing.DISABLED
)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
public class ExecutionLogEntry extends org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntry {


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
    private ExecutionLogEntryType executionType;


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
