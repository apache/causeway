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
package org.apache.isis.objectstore.jdo.applib.service.command;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.annotation.Command.ExecuteIn;
import org.apache.isis.applib.annotation.Command.Persistence;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.Mandatory;
import org.apache.isis.applib.annotation.MemberGroupLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.MultiLine;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.TypicalLength;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.TitleBuffer;
import org.apache.isis.objectstore.jdo.applib.service.DomainChangeJdoAbstract;
import org.apache.isis.objectstore.jdo.applib.service.JdoColumnLength;
import org.apache.isis.objectstore.jdo.applib.service.Util;
import org.apache.isis.objectstore.jdo.applib.service.DomainChangeJdoAbstract.ChangeType;


@javax.jdo.annotations.PersistenceCapable(
        identityType=IdentityType.APPLICATION, 
        table="IsisCommand")
@javax.jdo.annotations.Queries( {
    @javax.jdo.annotations.Query(
            name="findByTransactionId", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.command.CommandJdo "
                    + "WHERE transactionId == :transactionId "),
    @javax.jdo.annotations.Query(
            name="findBackgroundCommandByTransactionId", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.command.CommandJdo "
                    + "WHERE transactionId == :transactionId "
                    + "&& executeIn == 'BACKGROUND'"),
    @javax.jdo.annotations.Query(
            name="findBackgroundCommandsByParent", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.command.CommandJdo "
                    + "WHERE parent == :parent "
                    + "&& executeIn == 'BACKGROUND'"),
    @javax.jdo.annotations.Query(
            name="findBackgroundCommandsNotYetStarted", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.command.CommandJdo "
                    + "WHERE executeIn == 'BACKGROUND' "
                    + "&& startedAt == null "
                    + "ORDER BY timestamp ASC "
                    ),
    @javax.jdo.annotations.Query(
            name="findCurrent", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.command.CommandJdo "
                    + "WHERE completedAt == null "
                    + "ORDER BY timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findCompleted", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.command.CommandJdo "
                    + "WHERE completedAt != null "
                    + "&& executeIn == 'FOREGROUND' "
                    + "ORDER BY timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTargetAndTimestampBetween", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.audit.AuditEntryJdo "
                    + "WHERE targetStr == :targetStr " 
                    + "&& timestamp >= :from " 
                    + "&& timestamp <= :to "
                    + "ORDER BY timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTargetAndTimestampAfter", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.audit.AuditEntryJdo "
                    + "WHERE targetStr == :targetStr " 
                    + "&& timestamp >= :from "
                    + "ORDER BY timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTargetAndTimestampBefore", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.audit.AuditEntryJdo "
                    + "WHERE targetStr == :targetStr " 
                    + "&& timestamp <= :to "
                    + "ORDER BY timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTarget", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.audit.AuditEntryJdo "
                    + "WHERE targetStr == :targetStr " 
                    + "ORDER BY timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTimestampBetween", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.audit.AuditEntryJdo "
                    + "WHERE timestamp >= :from " 
                    + "&&    timestamp <= :to "
                    + "ORDER BY timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTimestampAfter", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.audit.AuditEntryJdo "
                    + "WHERE timestamp >= :from "
                    + "ORDER BY timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTimestampBefore", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.audit.AuditEntryJdo "
                    + "WHERE timestamp <= :to "
                    + "ORDER BY timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="find", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.audit.AuditEntryJdo "
                    + "ORDER BY timestamp DESC")
})
@ObjectType("IsisCommand")
@MemberGroupLayout(
        columnSpans={6,0,6,12}, 
        left={"Identifiers","Target","Notes"},
        right={"Detail","Timings","Results"})
@Named("Command")
@Immutable
public class CommandJdo extends DomainChangeJdoAbstract implements Command {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(CommandJdo.class);

    public CommandJdo() {
        super(ChangeType.COMMAND);
    }



    // //////////////////////////////////////
    // Identification
    // //////////////////////////////////////

    public String title() {
        final TitleBuffer buf = new TitleBuffer();
        buf.append(getTargetStr());
        buf.append(" ").append(getMemberIdentifier());
        return buf.toString();
    }



    // //////////////////////////////////////
    // user (property)
    // //////////////////////////////////////

    private String user;

    @javax.jdo.annotations.Column(allowsNull="false", length=JdoColumnLength.USER_NAME)
    @MemberOrder(name="Identifiers", sequence = "10")
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
     * The date/time at which this action was created.
     */
    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="false")
    @MemberOrder(name="Identifiers", sequence = "20")
    public Timestamp getTimestamp() {
        return timestamp;
    }
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    public void setTimestamp(final Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    
    // //////////////////////////////////////
    // executor (property)
    // //////////////////////////////////////
    
    private Executor executor;
    
    @Programmatic
    @javax.jdo.annotations.NotPersistent
    @Override
    public Executor getExecutor() {
        return executor;
    }

    @Override
    public void setExecutor(Executor nature) {
        this.executor = nature;
    }

    // //////////////////////////////////////
    // executeIn (property)
    // //////////////////////////////////////

    private ExecuteIn executeIn;

    /**
     * Whether the action was invoked explicitly by the user, or scheduled as a background
     * task, or as for some other reason, eg a side-effect of rendering an object due to 
     * get-after-post).
     */
    @javax.jdo.annotations.Column(allowsNull="false", length=JdoColumnLength.Command.EXECUTE_IN)
    @MemberOrder(name="Identifiers", sequence = "32")
    @Override
    public ExecuteIn getExecuteIn() {
        return executeIn;
    }
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    @Override
    public void setExecuteIn(ExecuteIn nature) {
        this.executeIn = nature;
    }


    // //////////////////////////////////////
    // parent (property)
    // //////////////////////////////////////

    private Command parent;
    
    @Override
    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(name="parentTransactionId", allowsNull="true")
    @Hidden(where=Where.PARENTED_TABLES)
    @MemberOrder(name="Identifiers",sequence = "40")
    public Command getParent() {
        return parent;
    }

    @Override
    public void setParent(Command parent) {
        this.parent = parent;
    }

    
    // //////////////////////////////////////
    // transactionId (property)
    // //////////////////////////////////////

        
    private UUID transactionId;

    /**
     * The unique identifier (a GUID) of the transaction in which this command occurred.
     */
    @javax.jdo.annotations.PrimaryKey
    @javax.jdo.annotations.Column(allowsNull="false", length=JdoColumnLength.TRANSACTION_ID)
    @TypicalLength(JdoColumnLength.TRANSACTION_ID)
    @MemberOrder(name="Identifiers",sequence = "50")
    @Override
    public UUID getTransactionId() {
        return transactionId;
    }

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: copied over from the Isis transaction when the command is persisted.
     */
    @Override
    public void setTransactionId(final UUID transactionId) {
        this.transactionId = transactionId;
    }

    
    // //////////////////////////////////////
    // targetClass (property)
    // //////////////////////////////////////

    private String targetClass;

    @javax.jdo.annotations.Column(allowsNull="false", length=JdoColumnLength.TARGET_CLASS)
    @TypicalLength(30)
    @MemberOrder(name="Target", sequence = "10")
    @Named("Class")
    public String getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(final String targetClass) {
        this.targetClass = Util.abbreviated(targetClass, JdoColumnLength.TARGET_CLASS);
    }


    // //////////////////////////////////////
    // targetAction (property)
    // //////////////////////////////////////
    
    private String targetAction;
    
    @javax.jdo.annotations.Column(allowsNull="false", length=JdoColumnLength.TARGET_ACTION)
    @Mandatory
    @Hidden(where=Where.NOWHERE)
    @TypicalLength(30)
    @MemberOrder(name="Target", sequence = "20")
    @Named("Action")
    public String getTargetAction() {
        return targetAction;
    }
    
    public void setTargetAction(final String targetAction) {
        this.targetAction = Util.abbreviated(targetAction, JdoColumnLength.TARGET_ACTION);
    }
    

    // //////////////////////////////////////
    // target (property)
    // openTargetObject (action)
    // //////////////////////////////////////

    private String targetStr;
    @javax.jdo.annotations.Column(allowsNull="false", length=JdoColumnLength.BOOKMARK, name="target")
    @Hidden(where=Where.ALL_TABLES)
    @MemberOrder(name="Target", sequence="30")
    @Named("Object")
    public String getTargetStr() {
        return targetStr;
    }

    public void setTargetStr(final String targetStr) {
        this.targetStr = targetStr;
    }

    // //////////////////////////////////////
    // arguments (property)
    // //////////////////////////////////////
    
    private String arguments;
    
    @javax.jdo.annotations.Column(allowsNull="true", jdbcType="CLOB")
    @MultiLine(numberOfLines=7)
    @Hidden(where=Where.ALL_TABLES)
    @MemberOrder(name="Target",sequence = "40")
    public String getArguments() {
        return arguments;
    }
    
    public void setArguments(final String arguments) {
        this.arguments = arguments;
    }

    

    // //////////////////////////////////////
    // memberIdentifier (property)
    // //////////////////////////////////////

    private String memberIdentifier;
    
    @javax.jdo.annotations.Column(allowsNull="false", length=JdoColumnLength.MEMBER_IDENTIFIER)
    @TypicalLength(60)
    @Hidden(where=Where.ALL_TABLES)
    @MemberOrder(name="Detail",sequence = "1")
    public String getMemberIdentifier() {
        return memberIdentifier;
    }

    public void setMemberIdentifier(final String memberIdentifier) {
        this.memberIdentifier = Util.abbreviated(memberIdentifier, JdoColumnLength.MEMBER_IDENTIFIER);
    }



    // //////////////////////////////////////
    // memento (property)
    // //////////////////////////////////////
    
    private String memento;
    
    @javax.jdo.annotations.Column(allowsNull="true", jdbcType="CLOB")
    @MultiLine(numberOfLines=9)
    @Hidden(where=Where.ALL_TABLES)
    @MemberOrder(name="Detail",sequence = "30")
    public String getMemento() {
        return memento;
    }
    
    public void setMemento(final String memento) {
        this.memento = memento;
    }



    // //////////////////////////////////////
    // startedAt (derived property)
    // //////////////////////////////////////
    
    private Timestamp startedAt;

    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true")
    @MemberOrder(name="Timings", sequence = "3")
    public Timestamp getStartedAt() {
        return startedAt;
    }

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    public void setStartedAt(final Timestamp startedAt) {
        this.startedAt = startedAt;
    }
    
    
    
    // //////////////////////////////////////
    // completedAt (property)
    // //////////////////////////////////////

    private Timestamp completedAt;

    /**
     * The date/time at which this interaction completed.
     */
    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true")
    @MemberOrder(name="Timings", sequence = "4")
    @Override
    public Timestamp getCompletedAt() {
        return completedAt;
    }

    @Override
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
    @Hidden(where=Where.OBJECT_FORMS)
    public boolean isComplete() {
        return getCompletedAt() != null;
    }

    
    
    // //////////////////////////////////////
    // state (derived property)
    // //////////////////////////////////////

    @javax.jdo.annotations.NotPersistent
    @MemberOrder(name="Results",sequence = "10")
    @Hidden(where=Where.OBJECT_FORMS)
    @Named("Result")
    public String getResultSummary() {
        if(getCompletedAt() == null) {
            return "";
        }
        if(getException() != null) {
            return "EXCEPTION";
        } 
        if(getResultStr() != null) {
            return "OK";
        } else {
            return "OK (VOID)";
        }
    }

    
    // //////////////////////////////////////
    // result (property)
    // openResultObject (action)
    // //////////////////////////////////////
    
    @Programmatic
    @Override
    public Bookmark getResult() {
        return Util.bookmarkFor(getResultStr());
    }

    @Programmatic
    @Override
    public void setResult(Bookmark result) {
        setResultStr(Util.asString(result));
    }

    // //////////////////////////////////////
    
    private String resultStr;

    @javax.jdo.annotations.Column(allowsNull="true", length=JdoColumnLength.BOOKMARK, name="result")
    @Hidden(where=Where.ALL_TABLES)
    @Named("Result Bookmark")
    @MemberOrder(name="Results", sequence="25")
    public String getResultStr() {
        return resultStr;
    }

    public void setResultStr(final String resultStr) {
        this.resultStr = resultStr;
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @MemberOrder(name="ResultStr", sequence="1")
    @Named("Open")
    public Object openResultObject() {
        return Util.lookupBookmark(getResult(), bookmarkService, container);
    }
    public boolean hideOpenResultObject() {
        return getResult() == null;
    }


    // //////////////////////////////////////
    // exception (property)
    // causedException (derived property)
    // showException (associated action)
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
    @javax.jdo.annotations.Column(allowsNull="true", jdbcType="CLOB")
    @Hidden
    @Override
    public String getException() {
        return exception;
    }

    @Override
    public void setException(final String exception) {
        this.exception = exception;
    }
    
    
    // //////////////////////////////////////
    
    @javax.jdo.annotations.NotPersistent
    @MemberOrder(name="Results",sequence = "30")
    @Hidden(where=Where.ALL_TABLES)
    public boolean isCausedException() {
        return getException() != null;
    }

    
    // //////////////////////////////////////
    
    @ActionSemantics(Of.SAFE)
    @MemberOrder(name="causedException", sequence = "1")
    public String showException() {
        return getException();
    }
    public boolean hideShowException() {
        return !isCausedException();
    }


    // //////////////////////////////////////
    // next(...) impl
    // //////////////////////////////////////

    private final Map<String, AtomicInteger> sequenceByName = Maps.newHashMap();



    @Programmatic
    @Override
    public int next(String sequenceName) {
        AtomicInteger next = sequenceByName.get(sequenceName);
        if(next == null) {
            next = new AtomicInteger(0);
            sequenceByName.put(sequenceName, next);
        } else {
            next.incrementAndGet();
        }
        return next.get();
    }

    
    // //////////////////////////////////////
    // persistence (programmatic)
    // //////////////////////////////////////

    private Persistence persistence;
    
    @javax.jdo.annotations.NotPersistent
    @Programmatic
    @Override
    public Persistence getPersistence() {
        return persistence;
    }

    @Override
    public void setPersistence(Persistence persistence) {
        this.persistence = persistence;
    }


    // //////////////////////////////////////
    // setPersistHint (SPI impl)
    // //////////////////////////////////////
    
    private boolean persistHint;

    @NotPersistent
    @Programmatic
    public boolean isPersistHint() {
        return persistHint;
    }
    
    @Programmatic
    @Override
    public void setPersistHint(boolean persistHint) {
        this.persistHint = persistHint;
    }

    
    // //////////////////////////////////////
    
    @Programmatic
    boolean shouldPersist() {
        if(Persistence.PERSISTED == getPersistence()) {
            return true;
        }
        if(Persistence.IF_HINTED == getPersistence()) {
            return isPersistHint();
        }
        return false;
    }



    // //////////////////////////////////////
    // toString
    // //////////////////////////////////////

    @Override
    public String toString() {
        return ObjectContracts.toString(this, "targetStr,memberIdentifier,user,startedAt,completedAt,duration,transactionId");
    }

    
    
    // //////////////////////////////////////
    // dependencies
    // //////////////////////////////////////
    

    @javax.inject.Inject
    private BookmarkService bookmarkService;
    
    @javax.inject.Inject
    private DomainObjectContainer container;

}
