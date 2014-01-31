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
import java.util.UUID;

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
    
    /**
     * @deprecated - use {@link #EventMetadata(UUID, int, EventType, String, Timestamp, String)}
     */
    @Deprecated
    public EventMetadata(
            final UUID transactionId, 
            final int sequence, 
            final EventType eventType, 
            final String user, 
            final long timestamp, 
            final String title) {
        this(transactionId, sequence, eventType, user, new java.sql.Timestamp(timestamp), title);
    }
    
    public EventMetadata(
            final UUID transactionId, 
            final int sequence, 
            final EventType eventType, 
            final String user, 
            final java.sql.Timestamp javaSqlTimestamp, 
            final String title) {
        this.transactionId = transactionId;
        this.sequence = sequence;
        this.user = user;
        this.javaSqlTimestamp = javaSqlTimestamp;
        this.title = title;
        this.eventType = eventType;
    }
    
    /**
     * Isis' identifier of the transaction within which this event
     * originated.
     * 
     * <p>
     * Note that there could be several events all with the same transaction Id.
     */
    public UUID getTransactionId() {
        return transactionId;
    }
    
    /**
     * The zero-based sequence number of this event within the transaction.
     * 
     * <p>
     * The combination of {@link #getTransactionId() transaction Id} and {@link #getSequence() sequence}
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
     * Returns a string that concatenates the {@link #getTransactionId()} and the
     * {@link #getSequence()} with a period (<tt>.</tt>).
     */
    public String getId() {
        return getTransactionId() + "." + getSequence();
    }
    
    /**
     * A user-friendly title for this event.
     */
    public String getTitle() {
        return title;
    }

    public EventType getEventType() {
        return eventType;
    }
    
    @Override
    public String toString() {
        return getId();
    }


}