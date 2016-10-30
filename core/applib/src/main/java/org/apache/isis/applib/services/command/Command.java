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
package org.apache.isis.applib.services.command;

import java.sql.Timestamp;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Command.ExecuteIn;
import org.apache.isis.applib.annotation.Command.Persistence;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.background.BackgroundCommandService;
import org.apache.isis.applib.services.background.BackgroundService;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.eventbus.ActionDomainEvent;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.publish.EventMetadata;
import org.apache.isis.applib.services.publish.EventPayload;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.schema.cmd.v1.CommandDto;

/**
 * Represents the <i>intention to</i> invoke either an action or modify a property.  This intention is reified as a
 * {@link Command#getMemento() memento} by way of the (internal) <tt>CommandDtoServiceInternal</tt> domain service;
 * typically corresponding to the XML equivalent of a {@link CommandDto}.
 *
 * <p>
 *     The {@link Command} interface also captures details of the corresponding action invocation (or property edit),
 *     specifically when that action/edit {@link Command#getStartedAt() started} or
 *     {@link Command#getCompletedAt() completed}, and its result, either a {@link Command#getResult() return value}
 *     or an {@link Command#getException() exception}.  The {@link Command3} sub-interface also captures the stack
 *     of {@link ActionDomainEvent}s.
 * </p>
 *
 * <p>
 *     Note that when invoking an action, other actions may be invoked courtesy of the {@link WrapperFactory}.  These
 *     "sub-actions" do <i>not</i> modify the contents of the command object; in other words think of the command
 *     object as representing the outer-most originating action.
 * </p>
 *
 * <p>
 *     One of the responsibilities of the command is to generate unique sequence numbers for a given transactionId.
 *     This is done by {@link #next(String)}.  There are three possible sequences that might be generated:
 *     the sequence of changed domain objects being published by the {@link org.apache.isis.applib.services.publish.PublishingService#publish(EventMetadata, EventPayload)}; the
 *     sequence of wrapped action invocations (each being published), and finally one or more background commands
 *     that might be scheduled via the {@link BackgroundService}.
 * </p>
 *
 */
public interface Command extends HasTransactionId {

    /**
     * @deprecated - in previous versions this was the value for {@link #getTargetAction()} if this command represents an edit of an object's property (rather than an action); now though the property's name is used (ie same as actions).
     */
    @Deprecated
    String ACTION_IDENTIFIER_FOR_EDIT = "(edit)";


    //region > user (property)
    /**
     * The user that created the command.
     */

    String getUser();
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the Isis PersistenceSession is opened.
     */
    void setUser(String user);
    //endregion

    //region > timestamp (property)

    /**
     * The date/time at which this command was created.
     */
    Timestamp getTimestamp();
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the Isis PersistenceSession is opened.  Uses the applib {@link Clock}.
     */
    void setTimestamp(Timestamp timestamp);

    //endregion

    //region > target (property)

    /**
     * {@link Bookmark} of the target object (entity or service) on which this action was performed.
     * 
     * <p>
     * Will only be populated if a {@link BookmarkService} has been configured.
     */
    Bookmark getTarget();
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the action is invoked (in the ActionInvocationFacet).
     */
    void setTarget(Bookmark target);

    //endregion

    //region > memberIdentifier (property)

    /**
     * Holds a string representation of the invoked action, or the edited property, equivalent to
     * {@link Identifier#toClassAndNameIdentityString()}.
     */
    String getMemberIdentifier();

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the action is invoked (in <tt>ActionInvocationFacet</tt>) or in
     * property edited (in <tt>PropertySetterFacet</tt>).
     */
    void setMemberIdentifier(String memberIdentifier);

    //endregion

    //region > targetClass (property)

    /**
     * A human-friendly description of the class of the target object.
     */
    String getTargetClass();

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the action is invoked (in the <tt>ActionInvocationFacet</t>).
     */
    void setTargetClass(String targetClass);

    //endregion

    //region > targetAction (property)

    /**
     * The human-friendly name of the action invoked/property edited on the target object.
     *
     * <p>
     *     NB: in earlier versions, if the command represented an edit of a property, then it held the special value &quot;{@value ACTION_IDENTIFIER_FOR_EDIT}&quot;.  This is NO LONGER the case; it simply holds the member.
     * </p>
     */
    String getTargetAction();
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the action is invoked (in the <tt>ActionInvocationFacet</tt>) or property edited
     * (in the <tt>PropertySetterOrClearFacet</tt>).
     */
    void setTargetAction(String targetAction);

    //endregion

    //region > arguments (property)

    /**
     * A human-friendly description of the arguments with which the action was invoked.
     */
    String getArguments();
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the action is invoked (in the <tt>ActionInvocationFacet</tt>).
     */
    void setArguments(final String arguments);

    //endregion

    //region > memento (property)

    /**
     * A formal (XML or similar) specification of the action to invoke/being invoked.
     */
    String getMemento();
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the action is invoked (in the <tt>ActionInvocationFacet</tt>).
     */
    void setMemento(final String memento);

    //endregion

    //region > executeIn (property)

    /**
     * The mechanism by which this command is to be executed, either synchronously &quot;in the 
     * {@link ExecuteIn#FOREGROUND foreground}&quot; or is to be executed asynchronously &quot;in the 
     * {@link ExecuteIn#BACKGROUND background}&quot; through the {@link BackgroundCommandService}.
     */
    ExecuteIn getExecuteIn();
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    void setExecuteIn(final ExecuteIn executeIn);

    //endregion

    //region > executor (property)

    enum Executor {
        /**
         * Command being executed by the end-user.
         */
        USER,
        /**
         * Command being executed by a background execution service.
         */
        BACKGROUND,
        /**
         * Command being executed for some other reason, eg as result of redirect-after-post, or the homePage action.
         */
        OTHER
    }

    /**
     * The (current) executor of this command.
     * 
     * <p>
     * Note that (even for implementations of {@link BackgroundCommandService} that persist {@link Command}s), this
     * property is never (likely to be) persisted, because it is always updated to indicate how the command is
     * currently being executed.
     * 
     * <p>
     * If the {@link #getExecutor() executor} matches the required {@link #getExecuteIn() execution policy}, then the
     * command actually is executed.  The combinations are:
     * <ul>
     * <li>executor = USER, executeIn = FOREGROUND, then execute</li>
     * <li>executor = USER, executeIn = BACKGROUND, then persist and return persisted command as a placeholder for the result</li>
     * <li>executor = BACKGROUND, executeIn = FOREGROUND, then ignore</li>
     * <li>executor = BACKGROUND, executeIn = BACKGROUND, then execute, update the command with result</li>
     * </ul>
     * 
     */
    Executor getExecutor();

    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    void setExecutor(final Executor executor);

    //endregion

    //region > startedAt (property, deprecated)

    /**
     * For an command that has actually been executed, holds the date/time at which the {@link Interaction} that
     * executed the command started.
     *
     * @deprecated - see {@link Interaction#getStartedAt()}.
     */
    @Deprecated
    Timestamp getStartedAt();
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     *
     * @deprecated - see {@link Interaction#getCurrentExecution()} and {@link org.apache.isis.applib.services.iactn.Interaction.Execution#setStartedAt(Timestamp)}.
     */
    @Deprecated
    void setStartedAt(Timestamp startedAt);

    //endregion

    //region > completedAt (property, deprecated)

    /**
     * For an command that has actually been executed, holds the date/time at which the {@link Interaction} that
     * executed the command completed.
     *
     * @deprecated - see {@link Interaction#getCurrentExecution()} and  {@link org.apache.isis.applib.services.iactn.Interaction.Execution#getCompletedAt()}.
     */
    @Deprecated
    Timestamp getCompletedAt();
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     *
     * @deprecated - see {@link org.apache.isis.applib.services.iactn.Interaction.Execution#setCompletedAt(Timestamp)}.
     */
    @Deprecated
    void setCompletedAt(Timestamp completedAt);

    //endregion

    //region > parent (property)

    /**
     * For actions created through the {@link BackgroundService} and {@link BackgroundCommandService},
     * captures the parent action.
     */
    Command getParent();

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    void setParent(final Command parent);

    //endregion

    //region > exception (property, deprecated)

    /**
     * For an command that has actually been executed, holds the exception stack
     * trace if the action invocation/property modification threw an exception.
     *
     * @deprecated - see {@link Interaction#getCurrentExecution()} and  {@link org.apache.isis.applib.services.iactn.Interaction.Execution#getThrew()} instead.
     */
    @Deprecated
    @Optional
    String getException();

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    void setException(String stackTrace);

    //endregion

    //region > result (property, deprecated)


    /**
     * For an command that has actually been executed, holds a {@link Bookmark} to the object returned by the corresponding action/property modification.
     * 
     * @deprecated - see {@link Interaction#getCurrentExecution()} and  {@link org.apache.isis.applib.services.iactn.Interaction.Execution#getReturned()} instead.
     */
    @Deprecated
    Bookmark getResult();
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    void setResult(Bookmark resultBookmark);

    //endregion

    //region > persistence (property)

    /**
     * Whether this command should ultimately be persisted (if the configured {@link BackgroundCommandService} supports
     * it) or not.
     * 
     * <p>
     * If the action to be executed has been annotated with the {@link Action#command()} attribute
     * then (unless its {@link Action#commandPersistence()} persistence} attribute has been set to a different value
     * than its default of {@link org.apache.isis.applib.annotation.CommandPersistence#PERSISTED persisted}), the
     * {@link Command} object will be persisted.
     * 
     * <p>
     * However, it is possible to prevent the {@link Command} object from ever being persisted by setting the
     * {@link org.apache.isis.applib.annotation.Action#commandPersistence() persistence} attribute to
     * {@link org.apache.isis.applib.annotation.CommandPersistence#NOT_PERSISTED}, or it can be set to
     * {@link org.apache.isis.applib.annotation.CommandPersistence#IF_HINTED}, meaning it is dependent
     * on whether {@link #setPersistHint(boolean) a hint has been set} by some other means.  
     *
     * <p>
     * For example, a {@link BackgroundCommandService} implementation that creates persisted background commands ought 
     * associate them (via its {@link Command#getParent() parent}) to an original persisted
     * {@link Command}.  The hinting mechanism allows the service to suggest that the parent command be persisted so
     * that the app can then provide a mechanism to find all child background commands for that original parent command.
     */
    Persistence getPersistence();
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    void setPersistence(final Persistence persistence);
    //endregion

    //region > persistHint (programmatic)


    /**
     * Whether that this {@link Command} should be persisted, if possible.
     */
    @Programmatic
    boolean isPersistHint();
    
    /**
     * Hint that this {@link Command} should be persisted, if possible.
     * 
     * <p>
     * <b>NOT API</b>: intended to be called only by the framework.  
     * 
     * @see #getPersistence()
     */
    @Programmatic
    void setPersistHint(boolean persistHint);
    //endregion

    //region > next (programmatic, deprecated)

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     *
     * @deprecated - no longer used (the framework uses {@link Interaction#next(String)} instead).
     */
    @Deprecated
    @Programmatic
    int next(final String sequenceAbbr);

    //endregion

}
