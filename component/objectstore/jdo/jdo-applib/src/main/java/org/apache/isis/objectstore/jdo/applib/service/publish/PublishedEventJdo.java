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

package org.apache.isis.objectstore.jdo.applib.service.publish;

import java.util.UUID;

import javax.jdo.annotations.IdentityType;

import org.datanucleus.management.jmx.ManagementManager;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.MemberGroupLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.MultiLine;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.NotPersisted;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.publish.EventType;
import org.apache.isis.objectstore.jdo.applib.service.JdoColumnLength;
import org.apache.isis.objectstore.jdo.applib.service.Util;

@javax.jdo.annotations.PersistenceCapable(
        identityType=IdentityType.APPLICATION,
        table="IsisPublishedEvent", 
        objectIdClass=PublishedEventJdoPK.class)
@javax.jdo.annotations.Queries( {
    @javax.jdo.annotations.Query(
            name="findByStateOrderByTimestamp", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.publish.PublishedEventJdo "
                    + "WHERE state == :state ORDER BY timestamp"),
    @javax.jdo.annotations.Query(
            name="findByTransactionId", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.publish.PublishedEventJdo "
                    + "WHERE transactionId == :transactionId")
})
@MemberGroupLayout(
        left={"Identifiers","Target","Detail"})
@Immutable
@ObjectType("IsisPublishedEvent")
public class PublishedEventJdo implements HasTransactionId {

    public static enum State {
        QUEUED, PROCESSED
    }
    

    // //////////////////////////////////////
    // user (property)
    // //////////////////////////////////////

    private String user;
    
    @javax.jdo.annotations.Column(allowsNull="false", length=50)
    @MemberOrder(name="Identifiers", sequence = "10")
    @Hidden(where=Where.PARENTED_TABLES)
    public String getUser() {
        return user;
    }
    
    public void setUser(final String user) {
        this.user = user;
    }
    

    // //////////////////////////////////////
    // timestamp (property)
    // //////////////////////////////////////

    private java.sql.Timestamp timestamp;

    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="false")
    @MemberOrder(name="Identifiers", sequence = "20")
    @Hidden(where=Where.PARENTED_TABLES)
    public java.sql.Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final java.sql.Timestamp timestamp) {
        this.timestamp = timestamp;
    }
    


    // //////////////////////////////////////
    // transactionId
    // //////////////////////////////////////

    private UUID transactionId;

    /**
     * The unique identifier (a GUID) of the transaction in which this published event was persisted.
     * 
     * <p>
     * The combination of ({@link #getTransactionId() transactionId}, {@link #getSequence() sequence}) makes up the
     * primary key.
     */
    @javax.jdo.annotations.PrimaryKey
    @javax.jdo.annotations.Column(allowsNull="false",length=JdoColumnLength.TRANSACTION_ID)
    @MemberOrder(name="Identifiers", sequence = "30")
    @Hidden(where=Where.PARENTED_TABLES)
    @Override
    public UUID getTransactionId() {
        return transactionId;
    }

    @Override
    public void setTransactionId(final UUID transactionId) {
        this.transactionId = transactionId;
    }

    
    // //////////////////////////////////////
    // sequence
    // //////////////////////////////////////

    private int sequence;

    /**
     * The 0-based additional identifier of a published event within the given {@link #getTransactionId() transaction}.
     * 
     * <p>
     * The combination of ({@link #getTransactionId() transactionId}, {@link #getSequence() sequence}) makes up the
     * primary key.
     */
    @javax.jdo.annotations.PrimaryKey
    @MemberOrder(name="Identifiers", sequence = "40")
    public int getSequence() {
        return sequence;
    }

    public void setSequence(final int sequence) {
        this.sequence = sequence;
    }
    

    // //////////////////////////////////////
    // title
    // //////////////////////////////////////

    private String title;

    @javax.jdo.annotations.Column(allowsNull="false", length=JdoColumnLength.PublishedEvent.TITLE)
    @Title
    @MemberOrder(name="Target", sequence = "10")
    @Named("Object")
    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }
    
    // //////////////////////////////////////

    
    @ActionSemantics(Of.SAFE)
    @MemberOrder(name="Title", sequence="1")
    @Named("Open")
    public Object openTitleObject() {
        String title2 = getTitle();
        int indexOf = title2.indexOf("^");
        if(indexOf != -1) {
            title2 = title2.substring(0, indexOf);
        }
        return Util.lookupBookmark(Util.bookmarkFor(title2), bookmarkService, container);
    }
    public boolean hideOpenTitleObject() {
        return getTitle() == null;
    }

    // //////////////////////////////////////
    // eventType (property)
    // //////////////////////////////////////

    private EventType eventType;

    @javax.jdo.annotations.Column(allowsNull="false", length=JdoColumnLength.PublishedEvent.EVENT_TYPE)
    @MemberOrder(name="Detail",sequence = "20")
    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(final EventType eventType) {
        this.eventType = eventType;
    }
    

    // //////////////////////////////////////
    // state (property)
    // //////////////////////////////////////

    private State state;

    @javax.jdo.annotations.Column(allowsNull="false", length=JdoColumnLength.PublishedEvent.STATE)
    @MemberOrder(name="Detail", sequence = "30")
    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
    }
    private PublishedEventJdo setStateAndReturn(State state) {
        setState(state);
        return this;
    }
    

    // //////////////////////////////////////
    // serializedFormZipped (property)
    // serializedForm (derived property)
    // //////////////////////////////////////

    @javax.jdo.annotations.NotPersistent
    @NotPersisted
    @MultiLine(numberOfLines=20)
    @Hidden(where=Where.ALL_TABLES)
    @MemberOrder(name="Detail", sequence = "40")
    public String getSerializedForm() {
        return IoUtils.fromUtf8ZippedBytes("serializedForm", getSerializedFormZipped());
    }

    public void setSerializedForm(final String serializedForm) {
        final byte[] zippedBytes = IoUtils.toUtf8ZippedBytes("serializedForm", serializedForm);
        setSerializedFormZipped(zippedBytes);
    }

    // //////////////////////////////////////

    
    @javax.jdo.annotations.Column
    private byte[] serializedFormZipped;

    @Programmatic // ignored by Isis
    public byte[] getSerializedFormZipped() {
        return serializedFormZipped;
    }

    public void setSerializedFormZipped(final byte[] serializedFormZipped) {
        this.serializedFormZipped = serializedFormZipped;
    }

    

    // //////////////////////////////////////
    // processed (action)
    // reQueue   (action)
    // delete    (action)
    // //////////////////////////////////////

 
    @Bulk
    @ActionSemantics(Of.IDEMPOTENT)
    @MemberOrder( name="State", sequence="10")
    public PublishedEventJdo processed() {
        return setStateAndReturn(State.PROCESSED);
    }


    @Bulk
    @ActionSemantics(Of.IDEMPOTENT)
    @MemberOrder(name="State", sequence="11")
    public PublishedEventJdo reQueue() {
        return setStateAndReturn(State.QUEUED);
    }

    @Bulk
    @MemberOrder(name="State", sequence="12")
    public void delete() {
        container.removeIfNotAlready(this);
    }
    

    // //////////////////////////////////////

    @javax.inject.Inject
    private BookmarkService bookmarkService;

    @javax.inject.Inject
    private DomainObjectContainer container;

}
