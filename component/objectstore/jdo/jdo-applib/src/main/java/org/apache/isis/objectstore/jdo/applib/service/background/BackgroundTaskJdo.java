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
import java.math.RoundingMode;
import java.sql.Timestamp;

import javax.inject.Inject;
import javax.jdo.annotations.IdentityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberGroupLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.MultiLine;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.TypicalLength;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.interaction.Interaction;
import org.apache.isis.applib.services.interaction.spi.InteractionFactory;
import org.apache.isis.applib.util.ObjectContracts;


@javax.jdo.annotations.PersistenceCapable(
        identityType=IdentityType.DATASTORE, 
        table="IsisBackgroundTask")
@javax.jdo.annotations.DatastoreIdentity(
        strategy=javax.jdo.annotations.IdGeneratorStrategy.IDENTITY,
         column="id")
@Named("Background Task")
@MemberGroupLayout(
        columnSpans={6,0,6}, 
        left={"Target"},
        right={"Identifiers","Timings"})
public class BackgroundTaskJdo implements HasTransactionId {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(BackgroundTaskJdo.class);

    
    // //////////////////////////////////////

    private String actionIdentifier;
    
    /**
     * The identifier of the action that this background task will/has run.
     * 
     * <p>
     * This information is also available within the {@link #getMemento()}, but is redundantly stored here also
     * to enable analytics.
     */
    @javax.jdo.annotations.Column(allowsNull="false", length=255)
    @Title(sequence="1")
    @TypicalLength(60)
    @Hidden(where=Where.ALL_TABLES)
    @MemberOrder(name="Identifiers",sequence = "11")
    @Disabled
    public String getActionIdentifier() {
        return actionIdentifier;
    }

    public void setActionIdentifier(final String actionIdentifier) {
        this.actionIdentifier = actionIdentifier;
    }

    
    
    // //////////////////////////////////////
    // user (property)
    // //////////////////////////////////////

    private String user;

    /**
     * The user that invoked the initial interaction that gave rise to this background task, and also the credentials
     * with which the task will/has run.
     */
    @javax.jdo.annotations.Column(allowsNull="false", length=50)
    @Title(sequence="2", prepend=", ")
    @MemberOrder(name="Identifiers", sequence = "5")
    @Disabled
    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }




    // //////////////////////////////////////
    // memento (property)
    // //////////////////////////////////////
    
    private String memento;
    
    @javax.jdo.annotations.Column(allowsNull="false", length=1024)
    @MultiLine(numberOfLines=20)
    @Hidden(where=Where.ALL_TABLES)
    @MemberOrder(name="Target",sequence = "4")
    @Disabled
    public String getMemento() {
        return memento;
    }
    
    public void setMemento(final String memento) {
        this.memento = memento;
    }
    
    
    
    // //////////////////////////////////////
    // target (property)
    // //////////////////////////////////////

    @Programmatic
    public Bookmark getTarget() {
        return new Bookmark(getTargetStr());
    }
    
    @Programmatic
    public void setTarget(Bookmark target) {
        setTargetStr(target.toString());
    }

    private String targetStr;
    @javax.jdo.annotations.Column(allowsNull="false", length=255, name="target")
    @Hidden
    public String getTargetStr() {
        return targetStr;
    }

    public void setTargetStr(final String targetStr) {
        this.targetStr = targetStr;
    }


    
    // //////////////////////////////////////
    // targetObject (derived property)
    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @MemberOrder(name="Target", sequence="3")
    @Named("Object")
    public Object getTargetObject() {
        return bookmarkService.lookup(getTarget());
    }


    // //////////////////////////////////////
    // createdAt (property)
    // //////////////////////////////////////

    private Timestamp createdAt;

    /**
     * The date/time at which this background task was created.
     */
    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="false")
    @MemberOrder(name="Timings", sequence = "3")
    @Disabled
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Timestamp createdAt) {
        this.createdAt = createdAt;
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
    @Disabled
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
    @Disabled
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
        if(getCompletedAt() == null) {
            return null;
        }
        long millis = getCompletedAt().getTime() - getStartedAt().getTime();
        return new BigDecimal(millis).divide(new BigDecimal(1000)).setScale(3, RoundingMode.HALF_EVEN);
    }


    // //////////////////////////////////////
    // transactionId (property)
    // //////////////////////////////////////

    
    private String transactionId;

    /**
     * The unique identifier (a GUID) of the transaction of the {@link Interaction} that gave rise to this
     * background task (if known, and if the interaction is itself persisted by way of a suitable implementation of
     * {@link InteractionFactory}).
     */
    @javax.jdo.annotations.Column(allowsNull="true", length=36)
    @TypicalLength(36)
    @MemberOrder(name="Identifiers",sequence = "20")
    @Disabled
    @Override
    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public void setTransactionId(final String transactionId) {
        this.transactionId = transactionId;
    }

    // //////////////////////////////////////

    @Override
    public String toString() {
        return ObjectContracts.toString(this, "actionIdentifier,user,createdAt,startedAt,completedAt,duration,transactionId");
    }


    // //////////////////////////////////////
    
    @Inject
    private BookmarkService bookmarkService;

}
