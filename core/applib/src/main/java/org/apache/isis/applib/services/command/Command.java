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
import org.apache.isis.applib.annotation.Command.ExecuteIn;
import org.apache.isis.applib.annotation.Command.Persistence;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.background.BackgroundCommandService;
import org.apache.isis.applib.services.background.BackgroundService;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;

public interface Command extends HasTransactionId {

    
    // //////////////////////////////////////
    // user (property)
    // //////////////////////////////////////

    /**
     * The user that initiated the action.
     */
    @Disabled
    public abstract String getUser();
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the Isis PersistenceSession is opened.
     */
    public abstract void setUser(String user);

    // //////////////////////////////////////
    // timestamp (property)
    // //////////////////////////////////////

    /**
     * The date/time at which this action was created.
     */
    @Disabled
    public abstract Timestamp getTimestamp();
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the Isis PersistenceSession is opened.  Uses the applib {@link Clock}.
     */
    public abstract void setTimestamp(Timestamp startedAt);
    
    
    // //////////////////////////////////////
    // target (property)
    // //////////////////////////////////////

    
    /**
     * {@link Bookmark} of the target object (entity or service) on which this action was performed.
     * 
     * <p>
     * Will only be populated if a {@link BookmarkService} has been configured.
     */
    @Disabled
    public abstract Bookmark getTarget();
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the action is invoked (in the ActionInvocationFacet).
     */
    public abstract void setTarget(Bookmark target);
    
    
    // //////////////////////////////////////
    // memberIdentifier (property)
    // //////////////////////////////////////

    /**
     * Holds a string representation of the invoked action, equivalent to
     * {@link Identifier#toClassAndNameIdentityString()}.
     * 
     * <p>
     * This property is called 'memberIdentifier' rather than 'actionIdentifier' for
     * consistency with other services (such as auditing and publishing) that may act on
     * properties rather than simply just actions.
     */
    @Disabled
    public abstract String getMemberIdentifier();

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the action is invoked (in the <tt>ActionInvocationFacet</tt>).
     */
    public abstract void setMemberIdentifier(String actionIdentifier);
    
    // //////////////////////////////////////
    // targetClass (property)
    // //////////////////////////////////////

    /**
     * A human-friendly description of the class of the target object.
     */
    @Disabled
    public abstract String getTargetClass();

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the action is invoked (in the <tt>ActionInvocationFacet</t>).
     */
    public abstract void setTargetClass(String targetClass);

    // //////////////////////////////////////
    // targetAction (property)
    // //////////////////////////////////////
    
    /**
     * The human-friendly name of the action invoked on the target object.
     */
    @Disabled
    public abstract String getTargetAction();
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the action is invoked (in the <tt>ActionInvocationFacet</tt>).
     */
    public abstract void setTargetAction(String targetAction);
    
    // //////////////////////////////////////
    // arguments (property)
    // //////////////////////////////////////
    
    /**
     * A human-friendly description of the arguments with which the action was invoked.
     */
    @Disabled
    public String getArguments();
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the action is invoked (in the <tt>ActionInvocationFacet</tt>).
     */
    public void setArguments(final String arguments);

    
    // //////////////////////////////////////
    // memento (property)
    // //////////////////////////////////////

    /**
     * A formal (XML or similar) specification of the action to invoke/being invoked.
     */
    @Disabled
    public String getMemento();
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the action is invoked (in the <tt>ActionInvocationFacet</tt>).
     */
    public void setMemento(final String memento);


    // //////////////////////////////////////
    // executeIn (property)
    // //////////////////////////////////////
    
    /**
     * The mechanism by which this command is to be executed, either synchronously &quot;in the 
     * {@link ExecuteIn#FOREGROUND foreground}&quot; or is to be executed asynchronously &quot;in the 
     * {@link ExecuteIn#BACKGROUND background}&quot; through the {@link BackgroundCommandService}.
     */
    @Disabled
    public ExecuteIn getExecuteIn();
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    public void setExecuteIn(final ExecuteIn executeIn);


    // //////////////////////////////////////
    // executor (property)
    // //////////////////////////////////////
    
    
    public static enum Executor {
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
    @Disabled
    public Executor getExecutor();

    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    public void setExecutor(final Executor executor);
    
    


    // //////////////////////////////////////
    // startedAt (property)
    // //////////////////////////////////////

    /**
     * The date/time at  which this action started.
     * 
     * <p>
     * For {@link ExecuteIn#FOREGROUND user-initiated} actions, this will always be
     * populated and have the same value as the {@link #getTimestamp() timestamp}; for
     * {@link ExecuteIn#BACKGROUND background} actions, this will be populated only when the
     * action is executed by a background execution process.
     */
    @Disabled
    public abstract Timestamp getStartedAt();
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the Isis PersistenceSession is opened.  Uses the applib {@link Clock}.
     */
    public abstract void setStartedAt(Timestamp startedAt);
    
    
    // //////////////////////////////////////
    // completedAt (property)
    // //////////////////////////////////////

    
    /**
     * The date/time at which this action completed.
     */
    @Disabled
    public abstract Timestamp getCompletedAt();
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the Isis PersistenceSession is opened.  Uses the applib {@link Clock}.
     */
    public abstract void setCompletedAt(Timestamp completedAt);


    // //////////////////////////////////////
    // parent (property)
    // //////////////////////////////////////


    /**
     * For actions created through the {@link BackgroundService} and {@link BackgroundCommandService},
     * captures the parent action.
     */
    @Optional
    @Disabled
    public Command getParent();

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    public void setParent(final Command parent);

    
    // //////////////////////////////////////
    // exception (property)
    // //////////////////////////////////////

    @Disabled
    @Optional
    public String getException();

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    public void setException(String stackTrace);
    
    // //////////////////////////////////////
    // result (property)
    // //////////////////////////////////////

    
    /**
     * A {@link Bookmark} to the object returned by the action.
     * 
     * <p>
     * If the action returned either a domain entity or a simple value (and did not throw an
     * exception) then this object is provided here.  
     * 
     * <p>
     * For <tt>void</tt> methods and for actions returning collections, the value
     * will be <tt>null</tt>.
     */
    @Disabled
    @Optional
    public Bookmark getResult();
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    public void setResult(Bookmark resultBookmark);

    
    // //////////////////////////////////////
    // persistence (programmatic)
    // //////////////////////////////////////
    
    /**
     * Whether this command should ultimately be persisted (if the configured {@link BackgroundCommandService} supports
     * it) or not.
     * 
     * <p>
     * If the action being executed has been annotated with the {@link org.apache.isis.applib.annotation.Command} 
     * annotation, then (unless its {@link org.apache.isis.applib.annotation.Command#persistence() persistence} 
     * attribute has been set to a different value than its default of {@link Persistence#PERSISTED}), the 
     * {@link Command} object will be persisted.
     * 
     * <p>
     * However, it is possible to prevent the {@link Command} object from ever being persisted by setting the
     * {@link org.apache.isis.applib.annotation.Command#persistence() persistence} attribute to 
     * {@link Persistence#NOT_PERSISTED}, or it can be set to {@link Persistence#IF_HINTED}, meaning it is dependent
     * on whether {@link #setPersistHint(boolean) a hint has been set} by some other means.  
     *
     * <p>
     * For example, a {@link BackgroundCommandService} implementation that creates persisted background commands ought 
     * associate them (via its {@link Command#getParent() parent}) to an original persisted
     * {@link Command}.  The hinting mechanism allows the service to suggest that the parent command be persisted so
     * that the app can then provide a mechanism to find all child background commands for that original parent command.
     */
    @Disabled
    public Persistence getPersistence();
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    public void setPersistence(final Persistence persistence);
    

    // //////////////////////////////////////
    // persistHint (programmatic)
    // //////////////////////////////////////

    
    /**
     * Whether that this {@link Command} should be persisted, if possible.
     */
    @Programmatic
    public boolean isPersistHint();
    
    /**
     * Hint that this {@link Command} should be persisted, if possible.
     * 
     * <p>
     * <b>NOT API</b>: intended to be called only by the framework.  
     * 
     * @see #getPersistence()
     */
    @Programmatic
    public void setPersistHint(boolean persistHint);
    
    // //////////////////////////////////////
    // next (programmatic)
    // //////////////////////////////////////

    /**
     * Generates numbers in a named sequence
     * 
     * <p>
     * Used to support the <tt>PublishingServiceJdo</tt> implementation whose
     * persisted entities are uniquely identified by a ({@link #getTransactionId() transactionId}, <tt>sequence</tt>)
     * tuple.
     */
    @Programmatic
    public int next(final String sequenceName);
}    
