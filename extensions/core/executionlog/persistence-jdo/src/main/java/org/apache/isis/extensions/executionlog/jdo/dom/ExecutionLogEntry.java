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
package org.apache.isis.extensions.executionlog.jdo.dom;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.UUID;

import javax.inject.Named;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.jaxb.PersistentEntitiesAdapter;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.util.TitleBuffer;
import org.apache.isis.extensions.executionlog.applib.IsisModuleExtExecutionLogApplib;
import org.apache.isis.extensions.executionlog.applib.dom.ExecutionLogEntryPK;
import org.apache.isis.extensions.executionlog.applib.dom.ExecutionLogEntryType;
import org.apache.isis.schema.ixn.v2.InteractionDto;

import lombok.Getter;
import lombok.Setter;

@PersistenceCapable(
        identityType= IdentityType.APPLICATION,
        schema = "isispublishmq",
        table="PublishedEvent",
        objectIdClass= ExecutionLogEntryPK.class)
@Queries( {
    @Query(
            name="findByTransactionId", language="JDOQL",
            value="SELECT "
                    + "FROM com.ecpnv.platform.v1.extensions.executionlog.dom.jdo.events.PublishedEvent "
                    + "WHERE transactionId == :transactionId "
                    + "ORDER BY timestamp DESC, sequence DESC"),
    @Query(
            name="findByTransactionIdAndSequence", language="JDOQL",
            value="SELECT "
                    + "FROM com.ecpnv.platform.v1.extensions.executionlog.dom.jdo.events.PublishedEvent "
                    + "WHERE transactionId == :transactionId "
                    + "&&    sequence      == :sequence "),
    @Query(
            name="findByTargetAndTimestampBetween", language="JDOQL",
            value="SELECT "
                    + "FROM com.ecpnv.platform.v1.extensions.executionlog.dom.jdo.events.PublishedEvent "
                    + "WHERE targetStr == :targetStr "
                    + "&& timestamp >= :from "
                    + "&& timestamp <= :to "
                    + "ORDER BY timestamp DESC, transactionId DESC, sequence DESC"),
    @Query(
            name="findByTargetAndTimestampAfter", language="JDOQL",
            value="SELECT "
                    + "FROM com.ecpnv.platform.v1.extensions.executionlog.dom.jdo.events.PublishedEvent "
                    + "WHERE targetStr == :targetStr "
                    + "&& timestamp >= :from "
                    + "ORDER BY timestamp DESC, transactionId DESC, sequence DESC"),
    @Query(
            name="findByTargetAndTimestampBefore", language="JDOQL",
            value="SELECT "
                    + "FROM com.ecpnv.platform.v1.extensions.executionlog.dom.jdo.events.PublishedEvent "
                    + "WHERE targetStr == :targetStr "
                    + "&& timestamp <= :to "
                    + "ORDER BY timestamp DESC, transactionId DESC, sequence DESC"),
    @Query(
            name="findByTarget", language="JDOQL",
            value="SELECT "
                    + "FROM com.ecpnv.platform.v1.extensions.executionlog.dom.jdo.events.PublishedEvent "
                    + "WHERE targetStr == :targetStr "
                    + "ORDER BY timestamp DESC, transactionId DESC, sequence DESC"),
    @Query(
            name="findByTimestampBetween", language="JDOQL",
            value="SELECT "
                    + "FROM com.ecpnv.platform.v1.extensions.executionlog.dom.jdo.events.PublishedEvent "
                    + "WHERE timestamp >= :from "
                    + "&&    timestamp <= :to "
                    + "ORDER BY timestamp DESC, transactionId DESC, sequence DESC"),
    @Query(
            name="findByTimestampAfter", language="JDOQL",
            value="SELECT "
                    + "FROM com.ecpnv.platform.v1.extensions.executionlog.dom.jdo.events.PublishedEvent "
                    + "WHERE timestamp >= :from "
                    + "ORDER BY timestamp DESC, transactionId DESC, sequence DESC"),
    @Query(
            name="findByTimestampBefore", language="JDOQL",
            value="SELECT "
                    + "FROM com.ecpnv.platform.v1.extensions.executionlog.dom.jdo.events.PublishedEvent "
                    + "WHERE timestamp <= :to "
                    + "ORDER BY timestamp DESC, transactionId DESC, sequence DESC"),
    @Query(
            name="find", language="JDOQL",
            value="SELECT "
                    + "FROM com.ecpnv.platform.v1.extensions.executionlog.dom.jdo.events.PublishedEvent "
                    + "ORDER BY timestamp DESC, transactionId DESC, sequence DESC"),
    @Query(
            name="findRecentByUser", language="JDOQL",
            value="SELECT "
                    + "FROM com.ecpnv.platform.v1.extensions.executionlog.dom.jdo.events.PublishedEvent "
                    + "WHERE user == :user "
                    + "ORDER BY timestamp DESC, transactionId DESC, sequence DESC "
                    + "RANGE 0,30"),
    @Query(
            name="findRecentByTarget", language="JDOQL",
            value="SELECT "
                    + "FROM com.ecpnv.platform.v1.extensions.executionlog.dom.jdo.events.PublishedEvent "
                    + "WHERE targetStr == :targetStr "
                    + "ORDER BY timestamp DESC, transactionId DESC, sequence DESC "
                    + "RANGE 0,30")
})
@Named("isispublishmq.PublishedEvent")
@DomainObject(
        editing = Editing.DISABLED
)
@XmlJavaTypeAdapter(PersistentEntitiesAdapter.class)
public class ExecutionLogEntry extends org.apache.isis.extensions.executionlog.applib.dom.ExecutionLogEntry {


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
