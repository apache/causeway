/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.objectstore.jdo.applib.service.background;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

import javax.inject.Inject;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.MemberGroupLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.MultiLine;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.TypicalLength;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.objectstore.jdo.applib.service.JdoColumnLength;
import org.apache.isis.objectstore.jdo.applib.service.Util;


@javax.jdo.annotations.PersistenceCapable(
        identityType=IdentityType.APPLICATION, 
        table="IsisBackgroundTask",
        objectIdClass=BackgroundTaskJdoPK.class)
@javax.jdo.annotations.Queries( {
    @javax.jdo.annotations.Query(
            name="findByTransactionId", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.background.BackgroundTaskJdo "
                    + "WHERE transactionId == :transactionId")
})
@ObjectType("IsisBackgroundTask")
@Named("Background Task")
@MemberGroupLayout(
        columnSpans={6,0,6}, 
        left={"Identifiers","Timings"},
        right={"Detail"})
@Immutable
public class BackgroundTaskJdo implements HasTransactionId {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(BackgroundTaskJdo.class);


    // //////////////////////////////////////
    // transactionId (property)
    // //////////////////////////////////////

    
    private UUID transactionId;

    /**
     * The unique identifier (a GUID) of the transaction in which this background task was persisted.
     * 
     * <p>
     * The combination of ({@link #getTransactionId() transactionId}, {@link #getSequence() sequence}) makes up the
     * primary key.
     */
    @javax.jdo.annotations.PrimaryKey
    @javax.jdo.annotations.Column(allowsNull="false", length=JdoColumnLength.TRANSACTION_ID)
    @TypicalLength(36)
    @Hidden(where=Where.PARENTED_TABLES)
    @MemberOrder(name="Identifiers",sequence = "10")
    @Override
    public UUID getTransactionId() {
        return transactionId;
    }

    @Override
    public void setTransactionId(final UUID transactionId) {
        this.transactionId = transactionId;
    }


    // //////////////////////////////////////
    // sequence (property)
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
    @MemberOrder(name="Identifiers", sequence = "20")
    public int getSequence() {
        return sequence;
    }

    public void setSequence(final int sequence) {
        this.sequence = sequence;
    }


    // //////////////////////////////////////
    // user (property)
    // //////////////////////////////////////

    private String user;

    /**
     * The user that invoked the initial interaction that gave rise to this background task, and also the credentials
     * with which the task will/has run.
     */
    @javax.jdo.annotations.Column(allowsNull="false", length=JdoColumnLength.USER_NAME)
    @Title(sequence="2", prepend=", ")
    @MemberOrder(name="Identifiers", sequence = "30")
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

    private Timestamp timestamp;

    /**
     * The date/time at which this background task was created.
     */
    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="false")
    @MemberOrder(name="Identifiers", sequence = "40")
    @Hidden(where=Where.PARENTED_TABLES)
    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final Timestamp createdAt) {
        this.timestamp = createdAt;
    }




    // //////////////////////////////////////
    // target (property)
    // openTargetObject (action)
    // //////////////////////////////////////

    @Programmatic
    public Bookmark getTarget() {
        return Util.bookmarkFor(getTargetStr());
    }
    
    @Programmatic
    public void setTarget(Bookmark target) {
        setTargetStr(Util.asString(target));
    }

    // //////////////////////////////////////
    
    private String targetStr;
    @javax.jdo.annotations.Column(allowsNull="false", length=JdoColumnLength.BOOKMARK, name="target")
    @Named("Target")
    @MemberOrder(name="Detail", sequence="10")
    public String getTargetStr() {
        return targetStr;
    }

    public void setTargetStr(final String targetStr) {
        this.targetStr = targetStr;
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @MemberOrder(name="TargetStr", sequence="1")
    @Named("Open")
    public Object openTargetObject() {
        return Util.lookupBookmark(getTarget(), bookmarkService, container);
    }
    public boolean hideOpenTargetObject() {
        return getTarget() == null;
    }

    
    // //////////////////////////////////////
    // actionIdentifier (property)
    // //////////////////////////////////////

    private String actionIdentifier;
    
    /**
     * The identifier of the action that this background task will/has run.
     * 
     * <p>
     * This information is also available within the {@link #getMemento()}, but is redundantly stored here also
     * to enable analytics.
     */
    @javax.jdo.annotations.Column(allowsNull="false", length=JdoColumnLength.ACTION_IDENTIFIER)
    @Title(sequence="1")
    @TypicalLength(60)
    @MemberOrder(name="Detail",sequence = "20")
    public String getActionIdentifier() {
        return actionIdentifier;
    }

    public void setActionIdentifier(final String actionIdentifier) {
        this.actionIdentifier = Util.abbreviated(actionIdentifier, JdoColumnLength.ACTION_IDENTIFIER);
    }


    // //////////////////////////////////////
    // memento (property)
    // //////////////////////////////////////
    
    private String memento;
    
    @javax.jdo.annotations.Column(allowsNull="false", length=JdoColumnLength.BackgroundTask.MEMENTO)
    @MultiLine(numberOfLines=20)
    @Hidden(where=Where.ALL_TABLES)
    @MemberOrder(name="Detail",sequence = "30")
    public String getMemento() {
        return memento;
    }
    
    public void setMemento(final String memento) {
        this.memento = memento;
    }


    // //////////////////////////////////////
    // startedAt (property)
    // //////////////////////////////////////
    
    private Timestamp startedAt;
    
    /**
     * The date/time at which this background task started.
     */
    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true")
    @MemberOrder(name="Timings", sequence = "3")
    public Timestamp getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(final Timestamp startedAt) {
        this.startedAt = startedAt;
    }
    
    
    // //////////////////////////////////////
    // completedAt (property)
    // //////////////////////////////////////

    private Timestamp completedAt;

    /**
     * The date/time at which this background task completed.
     */
    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true")
    @MemberOrder(name="Timings", sequence = "4")
    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(final Timestamp completed) {
        this.completedAt = completed;
    }



    // //////////////////////////////////////
    // duration (derived property)
    // //////////////////////////////////////

    /**
     * The number of seconds (to 3 decimal places) that this interaction lasted.
     * 
     * <p>
     * Populated only if it has {@link #getCompletedAt() completed}.
     */
    @javax.validation.constraints.Digits(integer=5, fraction=3)
    @Named("Duration")
    @MemberOrder(name="Timings", sequence = "7")
    public BigDecimal getDuration() {
        return Util.durationBetween(getStartedAt(), getCompletedAt());
    }



    // //////////////////////////////////////
    // complete (derived property)
    // //////////////////////////////////////
    

    @javax.jdo.annotations.NotPersistent
    @MemberOrder(name="Timings", sequence = "8")
    public boolean isComplete() {
        return getCompletedAt() != null;
    }

    // //////////////////////////////////////
    // toString
    // //////////////////////////////////////

    @Override
    public String toString() {
        return ObjectContracts.toString(this, "actionIdentifier,user,timestamp,startedAt,completedAt,duration,transactionId");
    }


    // //////////////////////////////////////
    
    @Inject
    private BookmarkService bookmarkService;

    @javax.inject.Inject
    private DomainObjectContainer container;

}
