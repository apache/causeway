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
package org.apache.isis.applib.services.interaction;

import java.sql.Timestamp;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.interaction.spi.InteractionService;

public interface Interaction extends HasTransactionId {

    /**
     * The user that initiated the interaction.
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

    /**
     * The date/time at which the interaction started.
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
    

    /**
     * {@link Bookmark} of the target object (entity or service) on which this interaction was performed.
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

    /**
     * If this interaction is an action invocation (as opposed to updating an object),
     * then holds a string representation of that action, equivalent to
     * {@link Identifier#toClassAndNameIdentityString()}.
     * 
     * <p>
     * Returns <tt>null</tt> otherwise.
     */
    @Disabled
    public abstract String getActionIdentifier();

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the action is invoked (in the ActionInvocationFacet).
     */
    public abstract void setActionIdentifier(String actionIdentifier);
    
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
     * Implementation notes: set when the action is invoked (in the ActionInvocationFacet).
     */
    public abstract void setTargetClass(String targetClass);

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
     * Implementation notes: set when the action is invoked (in the ActionInvocationFacet).
     */
    public abstract void setTargetAction(String targetAction);
    
    // //////////////////////////////////////
    
    /**
     * A human-friendly description of the arguments passed to this interaction (action invocation).
     */
    public String getArguments();
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the action is invoked (in the ActionInvocationFacet).
     */
    public void setArguments(final String arguments);

    // //////////////////////////////////////
    
    /**
     * The nature of this interaction, for example whether as the result of an 
     * {@link #ACTION_INVOCATION explicit action invocation} on the part of the user, or merely as
     * a side-effect of {@link #RENDERING re-rendering} an entity, eg for a viewer (such as the
     * Wicket viewer) that uses the <a href="http://en.wikipedia.org/wiki/Post/Redirect/Get">post/redirect/get</a>
     * to avoid duplicate submissions.
     * 
     * <p>
     * The Isis implementations uses this field as to a hint as to whether to populate the interaction's
     * {@link Interaction#setActionIdentifier(String) action identifier} and related properties.  The expectation 
     * is that implementations of {@link InteractionService} will only persist interactions that were explicitly started
     * by the user.
     */
    public static enum Nature {
        /**
         * Indicates that the {@link Interaction} has occurred as the result of an explicit action invocation
         * on the part of the user.
         */
        ACTION_INVOCATION,
        RENDERING
    }

    public Nature getNature();
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    public void setNature(final Nature nature);

    
    // //////////////////////////////////////

    @Disabled
    public String getException();

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    public void setException(String stackTrace);
    
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
    public Bookmark getResult();
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    public void setResult(Bookmark resultBookmark);

    
    // //////////////////////////////////////

    
    /**
     * Generates numbers in a named sequence
     * 
     * <p>
     * Used to support <tt>BackgroundTaskServiceJdo</tt> and <tt>PublishingServiceJdo</tt> implementations whose
     * persisted entities are uniquely identified by a ({@link #getTransactionId() transactionId}, <tt>sequence</tt>)
     * tuple.
     */
    public int next(final String sequenceName);
    
}    
