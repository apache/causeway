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

import org.joda.time.DateTime;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkHolder;
import org.apache.isis.applib.services.bookmark.BookmarkService;

public interface Interaction {

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
    public abstract Timestamp getStartedAt();
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the Isis PersistenceSession is opened.  Uses the applib {@link Clock}.
     */
    public abstract void setStartedAt(Timestamp startedAt);
    
    // //////////////////////////////////////
    
    /**
     * The unique identifier (a GUID) for this interaction occurred.
     * 
     * <p>
     * Note that this is <i>not</i> the same as the Isis transaction guid, as found in the JDO applib's
     * <tt>PublishedEvent</tt>.  In general there could be several transactions within a single interaction.
     */
    @Disabled
    public abstract String getGuid();

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: set when the Isis transaction is started.
     */
    public abstract void setGuid(String guid);
    
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

}    
