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

package org.apache.isis.applib.services.publish;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.bookmark.Bookmark;

/**
 * Standard metadata about an event to be published.
 * 
 * <p>
 * This is a class rather than an interface so that it may be easily extended in the future.

 * @see EventPayload
 */
public class EventMetadata {
    
    private final UUID transactionId;
    private final int sequence;
    private final String user;
    private final java.sql.Timestamp javaSqlTimestamp;
    private final String title;
    private final EventType eventType;
    private final String targetClass;
    private final String targetAction;
    private final Bookmark target;
    private final String actionIdentifier;
    private final List<String> actionParameterNames;
    private final List<Class<?>> actionParameterTypes;
    private final Class<?> actionReturnType;

    /**
     * @deprecated - no longer called by the framework.
     */
    @Deprecated
    public EventMetadata(
            final UUID transactionId, 
            final int sequence, 
            final EventType eventType, 
            final String user, 
            final long timestamp, 
            final String title) {
        this(transactionId, sequence, eventType, user, new java.sql.Timestamp(timestamp), title, null, null, null, null);
    }

    /**
     * @deprecated - no longer called by the framework.
     */
    @Deprecated
    public EventMetadata(
            final UUID transactionId, 
            final int sequence, 
            final EventType eventType, 
            final String user, 
            final java.sql.Timestamp javaSqlTimestamp, 
            final String title, 
            final String targetClass, 
            final String targetAction, 
            final Bookmark target, 
            final String actionIdentifier) {
        this(transactionId, sequence, eventType, user, javaSqlTimestamp, title, targetClass, targetAction, target, actionIdentifier, null, null, null );
    }

    public EventMetadata(
            final UUID transactionId,
            final int sequence,
            final EventType eventType,
            final String user,
            final Timestamp javaSqlTimestamp,
            final String title,
            final String targetClass,
            final String targetAction,
            final Bookmark target,
            final String actionIdentifier,
            final List<String> actionParameterNames,
            final List<Class<?>> actionParameterTypes,
            final Class<?> actionReturnType) {
        this.transactionId = transactionId;
        this.sequence = sequence;
        this.user = user;
        this.javaSqlTimestamp = javaSqlTimestamp;
        this.title = title;
        this.eventType = eventType;
        this.targetClass = targetClass;
        this.targetAction = targetAction;
        this.target = target;
        this.actionIdentifier = actionIdentifier;
        this.actionParameterNames = actionParameterNames;
        this.actionParameterTypes = actionParameterTypes;
        this.actionReturnType = actionReturnType;
    }

    /**
     * Isis' identifier of the transaction within which this event
     * originated.
     * 
     * <p>
     * Note that there could be several events all with the same transaction Id, distinguished by {@link #getSequence()}.
     */
    public UUID getTransactionId() {
        return transactionId;
    }

    /**
     * The zero-based sequence number within the sequence name.
     * 
     * <p>
     * The combination of [{@link #getTransactionId() transaction Id}, {@link #getSequence() sequence}]
     * is guaranteed to be unique.
     */
    public int getSequence() {
        return sequence;
    }
    
    /**
     * Represents the user that was responsible for generating the event.  
     */
    public String getUser() {
        return user;
    }
    
    public Timestamp getJavaSqlTimestamp() {
        return javaSqlTimestamp;
    }

    /**
     * The timestamp, in milliseconds, since an epoch time.
     * 
     * @deprecated - use {@link #getJavaSqlTimestamp()}
     */
    @Deprecated
    public long getTimestamp() {
        return getJavaSqlTimestamp().getTime();
    }
    
    /**
     * Returns a string in form <tt>transactionId.sequence</tt>.
     */
    public String getId() {
        return getTransactionId() + "." + getSequence();
    }
    
    /**
     * A title for this event, consisting of the oidStr and (for {@link EventType#ACTION_INVOCATION}) also an
     * identifier of the action.
     */
    public String getTitle() {
        return title;
    }

    public static class TitleType {

        private TitleType() {}

        public static class Meta {

            public static final int MAX_LEN = 255;

            private Meta() {}

        }

    }


    public EventType getEventType() {
        return eventType;
    }

    /**
     * User-friendly class name.
     */
    public String getTargetClass() {
        return targetClass;
    }
    
    public Bookmark getTarget() {
        return target;
    }

    /**
     * User-friendly action name (populated only for {@link EventType#ACTION_INVOCATION}s).
     */
    public String getTargetAction() {
        return targetAction;
    }
    
    /**
     * Formal action identifier, corresponding to {@link Identifier#toClassAndNameIdentityString()}).
     * 
     * <p>
     * Populated only for {@link EventType#ACTION_INVOCATION}s.
     */
    public String getActionIdentifier() {
        return actionIdentifier;
    }

    /**
     * Parameter names of the invoked action.
     *
     * <p>
     * Populated only for {@link EventType#ACTION_INVOCATION}s.
     */
    public List<String> getActionParameterNames() {
        return actionParameterNames;
    }

    /**
     * Parameter types of the invoked action.
     *
     * <p>
     * Populated only for {@link EventType#ACTION_INVOCATION}s.
     */
    public List<Class<?>> getActionParameterTypes() {
        return actionParameterTypes;
    }

    /**
     * Return type of the invoked action.
     *
     * <p>
     * Populated only for {@link EventType#ACTION_INVOCATION}s.
     */
    public Class<?> getActionReturnType() {
        return actionReturnType;
    }

    // //////////////////////////////////////

    
    @Override
    public String toString() {
        return getId();
    }

}