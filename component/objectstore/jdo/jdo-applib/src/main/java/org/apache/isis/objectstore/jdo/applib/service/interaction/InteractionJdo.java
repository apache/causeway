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
import java.util.UUID;

import javax.jdo.annotations.IdentityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
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
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.interaction.Interaction;
import org.apache.isis.applib.services.interaction.spi.InteractionFactory;
import org.apache.isis.applib.util.ObjectContracts;


@javax.jdo.annotations.PersistenceCapable(
        identityType=IdentityType.APPLICATION, 
        table="IsisInteraction")
@javax.jdo.annotations.Queries( {
    @javax.jdo.annotations.Query(
            name="findByTransactionId", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.interaction.InteractionJdo "
                    + "WHERE transactionId == :transactionId"),
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
        right={"Identifiers","Timings","Results"})
@Named("Interaction")
public class InteractionJdo implements Interaction, HasTransactionId {

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
        this.actionIdentifier = abbreviated(actionIdentifier, 255);
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
        this.targetClass = abbreviated(targetClass, 50);
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
        this.targetAction = abbreviated(targetAction, 50);
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
    @Named("Target Bookmark")
    @Hidden(where=Where.ALL_TABLES)
    @MemberOrder(name="Target", sequence="3")
    public String getTargetStr() {
        return targetStr;
    }

    public void setTargetStr(final String targetStr) {
        this.targetStr = abbreviated(targetStr, 255);
    }

    
    // //////////////////////////////////////
    // openTargetObject (action)
    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @MemberOrder(name="TargetStr", sequence="1")
    @Named("Open")
    public Object openTargetObject() {
        return lookupBookmark(getTarget());
    }
    public boolean hideOpenTargetObject() {
        return getTargetStr() == null;
    }
    

    // //////////////////////////////////////
    // arguments (property)
    // //////////////////////////////////////
    
    private String arguments;
    
    @javax.jdo.annotations.Column(allowsNull="false", length=1024)
    @MultiLine(numberOfLines=6)
    @Hidden(where=Where.ALL_TABLES)
    @MemberOrder(name="Target",sequence = "4")
    @Disabled
    public String getArguments() {
        return arguments;
    }
    
    public void setArguments(final String arguments) {
        this.arguments = abbreviated(arguments, 1024);
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
    @Disabled
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
        this.user = abbreviated(user,50);
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
    @javax.jdo.annotations.Column(allowsNull="true", length=1024)
    @MultiLine(numberOfLines=10)
    @Hidden(where=Where.ALL_TABLES)
    @MemberOrder(name="Notes", sequence = "6")
    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = abbreviated(notes, 1024);
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

        
    private UUID transactionId;

    /**
     * The unique identifier (a GUID) of the transaction in which this interaction occurred.
     */
    @javax.jdo.annotations.PrimaryKey
    @javax.jdo.annotations.Column(allowsNull="false", length=36)
    @TypicalLength(36)
    @MemberOrder(name="Identifiers",sequence = "20")
    @Disabled
    @Override
    public UUID getTransactionId() {
        return transactionId;
    }

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: copied over from the Isis transaction when the interaction is persisted.
     */
    @Override
    public void setTransactionId(final UUID transactionId) {
        this.transactionId = transactionId;
    }

    // //////////////////////////////////////
    // nature (property)
    // //////////////////////////////////////

    private Nature nature;

    @javax.jdo.annotations.NotPersistent
    @Programmatic
    @Override
    public Nature getNature() {
        return nature;
    }
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: populated by the viewer as hint to {@link InteractionFactory} implementation.
     */
    @Override
    public void setNature(Nature nature) {
        this.nature = nature;
    }

    
    // //////////////////////////////////////
    // result (property)
    // //////////////////////////////////////

    @Programmatic
    @Override
    public Bookmark getResult() {
        return getResultStr() != null? new Bookmark(getResultStr()): null;
    }
    
    @Programmatic
    @Override
    public void setResult(Bookmark result) {
        setResultStr(result != null? result.toString(): null);
    }

    private String resultStr;
    @javax.jdo.annotations.Column(allowsNull="true", length=255, name="result")
    @Hidden(where=Where.ALL_TABLES)
    @Named("Result Bookmark")
    @MemberOrder(name="Results", sequence="25")
    public String getResultStr() {
        return resultStr;
    }

    public void setResultStr(final String resultStr) {
        this.resultStr = abbreviated(resultStr,255);
    }

    
    // //////////////////////////////////////
    // openResultObject (action)
    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @MemberOrder(name="ResultStr", sequence="1")
    @Named("Open")
    public Object openResultObject() {
        Bookmark bookmark = getResult();
        return lookupBookmark(bookmark);
    }
    public boolean hideOpenResultObject() {
        return getResultStr() == null;
    }



    // //////////////////////////////////////
    // exception (property)
    // //////////////////////////////////////

    private String exception;

    /**
     * Stack trace of any exception that might have occurred if this interaction/transaction aborted.
     * 
     * <p>
     * Not visible in the UI, but accessible 
     * <p>
     * Not part of the applib API, because the default implementation is not persistent
     * and so there's no object that can be accessed to be annotated.
     */
    @javax.jdo.annotations.Column(allowsNull="true", length=2000)
    @Hidden
    @Override
    public String getException() {
        return exception;
    }

    @Override
    public void setException(final String exception) {
        this.exception = abbreviated(exception, 2000);
    }
    
    
    // //////////////////////////////////////
    // causedException (derived property)
    // showException (associated action)
    // //////////////////////////////////////
    
    @javax.jdo.annotations.NotPersistent
    @MemberOrder(name="Results",sequence = "30")
    @Hidden(where=Where.ALL_TABLES)
    public boolean isCausedException() {
        return getException() != null;
    }

    @ActionSemantics(Of.SAFE)
    @MemberOrder(name="causedException", sequence = "1")
    public String showException() {
        return getException();
    }
    public boolean hideShowException() {
        return !isCausedException();
    }

    // //////////////////////////////////////


    @Override
    public String toString() {
        return ObjectContracts.toString(this, "startedAt,user,actionIdentifier,target,completedAt,duration,transactionId");
    }

    // //////////////////////////////////////

    private Object lookupBookmark(Bookmark bookmark) {
        try {
        return bookmarkService != null
                ? bookmarkService.lookup(bookmark)
                : null;
        } catch(RuntimeException ex) {
            if(ex.getClass().getName().contains("ObjectNotFoundException")) {
                container.warnUser("Object not found - has it since been deleted?");
                return null;
            } 
            throw ex;
        }
    }

    private static String abbreviated(final String str, final int maxLength) {
        return str != null? (str.length() < maxLength ? str : str.substring(0, maxLength - 3) + "..."): null;
    }

    // //////////////////////////////////////
    
    @javax.inject.Inject
    private BookmarkService bookmarkService;
    
    @javax.inject.Inject
    private DomainObjectContainer container;
}
