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

import javax.jdo.annotations.IdentityType;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.MultiLine;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.publish.EventType;

@javax.jdo.annotations.PersistenceCapable(identityType=IdentityType.APPLICATION)
@javax.jdo.annotations.Queries( {
    @javax.jdo.annotations.Query(
            name="publishedevent_of_state", language="JDOQL",  
            value="SELECT FROM org.apache.isis.objectstore.jdo.applib.service.publish.PublishedEvent WHERE state == :state ORDER BY timestamp")
})
@Immutable
public class PublishedEvent {

    public static enum State {
        QUEUED, PROCESSED
    }
    
    // {{ Title (property)
    private String title;

    @Title
    @Hidden
    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }
    // }}


    // {{ Timestamp (property)
    private long timestamp;

    @MemberOrder(sequence = "1")
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }
    // }}

    // {{ Id (property)
    @javax.jdo.annotations.PrimaryKey
    private String id;

    @MemberOrder(sequence = "2")
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }
    // }}

    
    // {{ TransactionId (property)
    private String transactionId;

    /**
     * Programmatic because information also available in the {@link #getId() id}.
     */
    @Programmatic
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(final String transactionId) {
        this.transactionId = transactionId;
    }
    // }}

    
    // {{ Sequence (property)
    private int sequence;

    /**
     * Programmatic because information also available in the {@link #getId() id}.
     */
    @Programmatic
    public int getSequence() {
        return sequence;
    }

    public void setSequence(final int sequence) {
        this.sequence = sequence;
    }
    // }}


    // {{ EventType (property)
    private EventType eventType;

    @MemberOrder(sequence = "3")
    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(final EventType eventType) {
        this.eventType = eventType;
    }
    // }}

    
    // {{ User (property)
    private String user;
    
    @MemberOrder(sequence = "4")
    public String getUser() {
        return user;
    }
    
    public void setUser(final String user) {
        this.user = user;
    }
    // }}
    
    
    // {{ State (property)
    private State state;

    @MemberOrder(sequence = "5")
    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
    }
    private PublishedEvent setStateAndReturn(State state) {
        setState(state);
        return this;
    }
    // }}

    
    // {{ SerializedForm (property)
    @javax.jdo.annotations.Column(length=4000)
    private String serializedForm;

    @MultiLine(numberOfLines=20)
    @Hidden(where=Where.ALL_TABLES)
    @MemberOrder(sequence = "6")
    public String getSerializedForm() {
        return serializedForm;
    }

    public void setSerializedForm(final String propertyName) {
        this.serializedForm = propertyName;
    }
    // }}

    @Bulk
    @ActionSemantics(Of.IDEMPOTENT)
    @MemberOrder(sequence="10")
    public PublishedEvent processed() {
        return setStateAndReturn(State.PROCESSED);
    }


    @Bulk
    @ActionSemantics(Of.IDEMPOTENT)
    @MemberOrder(sequence="11")
    public PublishedEvent reQueue() {
        return setStateAndReturn(State.QUEUED);
    }

    @Bulk
    @MemberOrder(sequence="12")
    public void delete() {
        container.removeIfNotAlready(this);
    }
    

    // {{ injected: DomainObjectContainer
    private DomainObjectContainer container;

    public void setDomainObjectContainer(final DomainObjectContainer container) {
        this.container = container;
    }
    // }}




}
