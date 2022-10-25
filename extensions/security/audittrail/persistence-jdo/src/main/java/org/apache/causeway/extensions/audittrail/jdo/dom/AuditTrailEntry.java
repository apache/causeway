/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.causeway.extensions.audittrail.jdo.dom;

import java.util.UUID;

import javax.inject.Named;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.Indices;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.extensions.audittrail.applib.dom.AuditTrailEntry.Nq;

import lombok.Getter;
import lombok.Setter;
@PersistenceCapable(
        identityType = IdentityType.DATASTORE,
        schema = AuditTrailEntry.SCHEMA,
        table = AuditTrailEntry.TABLE)
@Indices({
        @Index(name = "AuditTrailEntry__target_propertyId_timestamp_IDX", members = { "target", "propertyId", "timestamp" }, unique = "false"),
        @Index(name = "AuditTrailEntry__target_timestamp_IDX", members = { "target", "timestamp" }, unique = "false"),
        @Index(name = "AuditTrailEntry__timestamp_IDX", members = { "timestamp" }, unique = "false"),
        @Index(name = "AuditTrailEntry__interactionId_IDX", members = { "interactionId" }, unique = "false")
})
@Queries( {
    @Query(
            name = Nq.FIND_BY_INTERACTION_ID,
            value = "SELECT "
                  + "  FROM " + AuditTrailEntry.FQCN + " "
                  + " WHERE interactionId == :interactionId"),
    @Query(
            name = Nq.FIND_FIRST_BY_TARGET,
            value = "SELECT "
                  + "  FROM " + AuditTrailEntry.FQCN + " "
                  + " WHERE target == :target "
                  + " ORDER BY timestamp ASC "
                  + " RANGE 0,2"),
    @Query(
            name = Nq.FIND_RECENT_BY_TARGET,
            value = "SELECT "
                  + "  FROM " + AuditTrailEntry.FQCN + " "
                  + " WHERE target == :target "
                  + " ORDER BY timestamp DESC "
                  + " RANGE 0,100"),
    @Query(
            name = Nq.FIND_BY_TARGET_AND_TIMESTAMP_BETWEEN,
            value = "SELECT "
                  + "  FROM " + AuditTrailEntry.FQCN + " "
                  + " WHERE target == :target "
                  + "    && timestamp >= :from "
                  + "    && timestamp <= :to "
                  + "ORDER BY timestamp DESC"),
    @Query(
            name = Nq.FIND_BY_TARGET_AND_TIMESTAMP_AFTER,
            value = "SELECT "
                  + "  FROM " + AuditTrailEntry.FQCN + " "
                  + " WHERE target == :target "
                  + "    && timestamp >= :from "
                  + " ORDER BY timestamp DESC"),
    @Query(
            name  = Nq.FIND_BY_TARGET_AND_TIMESTAMP_BEFORE,
            value = "SELECT "
                  + "  FROM " + AuditTrailEntry.FQCN + " "
                  + " WHERE target == :target "
                  + "    && timestamp <= :to "
                  + " ORDER BY timestamp DESC"),
    @Query(
            name  = Nq.FIND_BY_TARGET,
            value = "SELECT "
                  + "  FROM " + AuditTrailEntry.FQCN + " "
                  + " WHERE target == :target "
                  + " ORDER BY timestamp DESC"),
    @Query(
            name  = Nq.FIND_BY_TIMESTAMP_BETWEEN,
            value = "SELECT "
                  + "  FROM " + AuditTrailEntry.FQCN + " "
                  + " WHERE timestamp >= :from "
                  + "    && timestamp <= :to "
                  + " ORDER BY timestamp DESC"),
    @Query(
            name  = Nq.FIND_BY_TIMESTAMP_AFTER,
            value = "SELECT "
                  + "  FROM " + AuditTrailEntry.FQCN + " "
                  + " WHERE timestamp >= :from "
                  + " ORDER BY timestamp DESC"),
    @Query(
            name  = Nq.FIND_BY_TIMESTAMP_BEFORE,
            value = "SELECT "
                  + "  FROM " + AuditTrailEntry.FQCN + " "
                  + " WHERE timestamp <= :to "
                  + " ORDER BY timestamp DESC"),
    @Query(
            name  = Nq.FIND,
            value = "SELECT "
                  + "  FROM " + AuditTrailEntry.FQCN + " "
                  + " ORDER BY timestamp DESC"),
    @Query(
            name = Nq.FIND_MOST_RECENT,
            value = "SELECT "
                  + "  FROM " + AuditTrailEntry.FQCN + " "
                  + " ORDER BY timestamp DESC, interactionId DESC, sequence DESC"
                  + " RANGE 0,100"),
    @Query(
            name = Nq.FIND_RECENT_BY_TARGET_AND_PROPERTY_ID,
            value = "SELECT "
                  + "  FROM " + AuditTrailEntry.FQCN + " "
                  + " WHERE target == :target "
                  + "    && propertyId == :propertyId "
                  + " ORDER BY timestamp DESC "
                  + " RANGE 0,30")
})
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@Version(strategy = VersionStrategy.VERSION_NUMBER, column = "version")
@Named(AuditTrailEntry.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED,
        entityChangePublishing = Publishing.DISABLED
)
public class AuditTrailEntry
extends org.apache.causeway.extensions.audittrail.applib.dom.AuditTrailEntry {

    static final String FQCN = "org.apache.causeway.extensions.audittrail.jdo.dom.AuditTrailEntry";


    @Column(allowsNull = Username.ALLOWS_NULL, length = Username.MAX_LENGTH)
    @Username
    @Getter @Setter
    private String username;


    @Column(allowsNull = Timestamp.ALLOWS_NULL)
    @Timestamp
    @Getter @Setter
    private java.sql.Timestamp timestamp;


    @Column(allowsNull = InteractionId.ALLOWS_NULL, length = InteractionId.MAX_LENGTH)
    @InteractionId
    @Getter @Setter
    private UUID interactionId;


    @Column(allowsNull = Sequence.ALLOWS_NULL)
    @Sequence
    @Getter @Setter
    private int sequence;


    @Persistent
    @Column(allowsNull = Target.ALLOWS_NULL, length = Target.MAX_LENGTH)
    @Target
    @Getter @Setter
    private Bookmark target;


    @Column(allowsNull = LogicalMemberIdentifier.ALLOWS_NULL, length = LogicalMemberIdentifier.MAX_LENGTH)
    @LogicalMemberIdentifier
    @Getter @Setter
    private String logicalMemberIdentifier;


    @Column(allowsNull = PropertyId.ALLOWS_NULL, length = PropertyId.MAX_LENGTH)
    @PropertyId
    @Getter @Setter
    private String propertyId;


    @Column(allowsNull = PreValue.ALLOWS_NULL, length = PreValue.MAX_LENGTH)
    @PreValue
    @Getter @Setter
    private String preValue;


    @Column(allowsNull = PostValue.ALLOWS_NULL, length = PostValue.MAX_LENGTH)
    @PostValue
    @Getter @Setter
    private String postValue;

}
