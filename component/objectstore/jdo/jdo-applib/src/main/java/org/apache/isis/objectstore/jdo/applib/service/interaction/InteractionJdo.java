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
package org.apache.isis.objectstore.jdo.applib.service.interaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;

import javax.inject.Inject;
import javax.jdo.annotations.IdentityType;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
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
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkHolder;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.interaction.Interaction;
import org.apache.isis.applib.util.ObjectContracts;


@javax.jdo.annotations.PersistenceCapable(
        identityType=IdentityType.APPLICATION, 
        table="IsisInteraction")
@javax.jdo.annotations.Queries( {
    @javax.jdo.annotations.Query(
            name="findByGuid", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.interaction.InteractionJdo "
                    + "WHERE guid == :guid"),
    @javax.jdo.annotations.Query(
            name="findCurrent", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.interaction.InteractionJdo "
                    + "WHERE completedAt == null "
                    + "ORDER BY startedAt DESC"),
    @javax.jdo.annotations.Query(
            name="findCompleted", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.interaction.InteractionJdo "
                    + "WHERE completedAt != null "
                    + "ORDER BY startedAt DESC")
})
@MemberGroupLayout(
        columnSpans={6,0,6}, 
        left={"Target","Notes"}, 
        right={"Identifiers","Timings"})
@Named("Interaction")
public class InteractionJdo implements Interaction {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(InteractionJdo.class);

    
    // //////////////////////////////////////
    // actionIdentifier (property)
    // //////////////////////////////////////

    private String actionIdentifier;
    
    @javax.jdo.annotations.Column(allowsNull="false", length=255)
    @Title(sequence="1")
    @TypicalLength(60)
    @Hidden(where=Where.ALL_TABLES)
    @MemberOrder(name="Identifiers",sequence = "11")
    public String getActionIdentifier() {
        return actionIdentifier;
    }

    public void setActionIdentifier(final String actionIdentifier) {
        this.actionIdentifier = actionIdentifier;
    }

    // //////////////////////////////////////
    // targetClass (property)
    // //////////////////////////////////////

    private String targetClass;

    @javax.jdo.annotations.Column(allowsNull="false", length=50)
    @TypicalLength(30)
    @MemberOrder(name="Target", sequence = "1")
    @Named("Class")
    public String getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(final String targetClass) {
        this.targetClass = targetClass;
    }


    // //////////////////////////////////////
    // targetAction (property)
    // //////////////////////////////////////
    
    private String targetAction;
    
    @javax.jdo.annotations.Column(allowsNull="false", length=50)
    @TypicalLength(30)
    @MemberOrder(name="Target", sequence = "2")
    @Named("Action")
    public String getTargetAction() {
        return targetAction;
    }
    
    public void setTargetAction(final String targetAction) {
        this.targetAction = targetAction;
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
    // arguments (property)
    // //////////////////////////////////////
    
    private String arguments;
    
    @javax.jdo.annotations.Column(allowsNull="false", length=255)
    @MultiLine(numberOfLines=6)
    @Hidden(where=Where.ALL_TABLES)
    @MemberOrder(name="Target",sequence = "4")
    @Disabled
    public String getArguments() {
        return arguments;
    }
    
    public void setArguments(final String arguments) {
        this.arguments = arguments;
    }
    
    
    // //////////////////////////////////////
    // target (property)
    // //////////////////////////////////////

    @Programmatic
    @Override
    public Bookmark getTarget() {
        return new Bookmark(getTargetStr());
    }
    
    @Programmatic
    @Override
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
    // startedAt (property)
    // //////////////////////////////////////

    private Timestamp startedAt;

    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="false")
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
     * The date/time at which this interaction completed.
     * 
     * <p>
     * Not part of the applib API, because the default implementation is not persistent
     * and so cannot be queried "after the fact".  This JDO-specific class is persistent,
     * and so we can gather this information.
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
    // user (property)
    // //////////////////////////////////////

    private String user;

    @javax.jdo.annotations.Column(allowsNull="false", length=50)
    @Title(sequence="2", prepend=", ")
    @MemberOrder(name="Identifiers", sequence = "5")
    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }


    // //////////////////////////////////////
    // notes (property)
    // //////////////////////////////////////

    private String notes;

    /**
     * Provides the ability for the end-user to annotate a (potentially long-running)
     * interaction.
     * 
     * <p>
     * Not part of the applib API, because the default implementation is not persistent
     * and so there's no object that can be accessed to be annotated.
     */
    @javax.jdo.annotations.Column(allowsNull="true", length=255)
    @MultiLine(numberOfLines=10)
    @Hidden(where=Where.ALL_TABLES)
    @MemberOrder(name="Notes", sequence = "6")
    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
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
    @Named("Duration")
    @MemberOrder(name="Timings", sequence = "7")
    public BigDecimal getDuration() {
        if(getCompletedAt() == null) {
            return null;
        }
        long millis = getCompletedAt().getTime() - getStartedAt().getTime();
        return new BigDecimal(millis).divide(new BigDecimal(1000)).setScale(2, RoundingMode.HALF_EVEN);
    }


    // //////////////////////////////////////
    // transactionId (property)
    // //////////////////////////////////////

    private String guid;

    @javax.jdo.annotations.PrimaryKey
    @javax.jdo.annotations.Column(allowsNull="false", length=36)
    @TypicalLength(36)
    @MemberOrder(name="Identifiers",sequence = "20")
    public String getGuid() {
        return guid;
    }

    public void setGuid(final String guid) {
        this.guid = guid;
    }

    // //////////////////////////////////////

    @Override
    public String toString() {
        return ObjectContracts.toString(this, "startedAt,user,actionIdentifier,target,completedAt,duration,guid");
    }

    // //////////////////////////////////////
    
    @Inject
    private BookmarkService bookmarkService;

}
