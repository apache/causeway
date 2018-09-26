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

package org.apache.isis.core.metamodel.adapter.version;

import static org.apache.isis.commons.internal.base._With.mapIfPresentElse;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nullable;

import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.commons.encoding.Encodable;
import org.apache.isis.core.commons.lang.DateExtensions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;

/**
 * An instance of this class is held by each {@link ObjectAdapter} and is used
 * to represent a particular version (at a point in time) of domain object
 * wrapped by that adapter.
 *
 * <p>
 * This is normally done using some form of incrementing number or timestamp,
 * which would be held within the implementing class. The numbers, timestamps,
 * etc should change for each changed object, and the different() method should
 * indicate that the two Version objects are different.
 *
 * <p>
 * The user's name and a timestamp should also be kept so that when an message
 * is passed to the user it can be of the form "user has change object at time"
 */
public class Version implements Serializable, Encodable {

    private static final long serialVersionUID = 1L;

    private final static Oid.Marshaller OID_MARSHALLER = Oid.marshaller();

    // -- FACTORIES

    public static Version empty() {
        return Factory.EMPTY_VERSION;
    }
    
    public static Version of(long sequence, @Nullable String user) {
        return of(sequence, user, Factory.EMPTY_TIMESTAMP);
    }
    
    public static Version of(long sequence, @Nullable String user, long utcTimestamp) {
        return new Version(sequence, user, utcTimestamp);
    }
    
    // -- LOGIC FOR EMPTY
    
    public static boolean isEmpty(Version version) {
        return version == Factory.EMPTY_VERSION;
    }
    
    public boolean hasTimestamp() {
        return utcTimestamp!=Factory.EMPTY_TIMESTAMP;
    }

    
    // -- constructor, fields
    private final long sequence;
    private final String user;
    private final long utcTimestamp;

    private Version(long sequence, @Nullable String user, long utcTimestamp) {
        this.sequence = sequence;
        this.user = user;
        this.utcTimestamp = utcTimestamp;
    }

    // -- Encodable

    public Version(final DataInputExtended input) throws IOException {
        this(input.readLong(), input.readUTF(), input.readLong());
    }


    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        output.writeLong(sequence);
        output.writeUTF(user);
        output.writeLong(utcTimestamp);
    }

    // -- getters
    /**
     * The internal, strictly monotonically increasing, version number.
     *
     * <p>
     * This might be the timestamp of the change, or it might be simply a number incrementing 1,2,3...
     */
    public long getSequence() {
        return sequence;
    }

    /**
     * Returns the user who made the last change (used for display/reporting only)
     *
     * <p>
     * May be null.
     */
    public @Nullable String getUser() {
        return user;
    }

    /**
     * The time of the last change, as UTC milliseconds.
     *
     * <p>
     * May be zero.
     *
     * @see #getTime()
     */
    public long getUtcTimestamp() {
        return utcTimestamp;
    }

    /**
     * Returns the time of the last change (used for display/reporting only, not comparison)
     *
     * <p>
     * May be null.
     *
     * @see #getUtcTimestamp()
     */
    public @Nullable Date getTime() {
        return utcTimestamp!=Factory.EMPTY_TIMESTAMP ? new Date(this.utcTimestamp) : null;
    }

    // -- enString

    public String enString() {
        return OID_MARSHALLER.marshal(this);
    }

    // -- equals, hashCode

    @Override
    public int hashCode() {
        return Long.hashCode(sequence);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Version other = (Version) obj;
        return sequence == other.sequence;
    }

    /**
     * Compares this version against the specified version and returns true if
     * they are different versions (by checking {@link #getSequence()}).
     *
     * <p>
     * This is use for optimistic checking, where the existence of a different
     * version will normally cause a concurrency exception.
     */
    public boolean different(Version version) {
        return !equals(version);
    }

    // -- sequence

    @Override
    public String toString() {
        return "#" + sequence + " " + getUser() + " " + DateExtensions.asTimestamp(getTime());
    }

    /**
     * Returns the sequence for printing/display
     */
    public String sequence() {
        return Long.toString(sequence, 16);
    }

    // -- SPECIAL CASES
    
    /** for convenience*/
    public static final class Factory {
        
        private final static Version EMPTY_VERSION = null;
        private final static long EMPTY_TIMESTAMP = 0L;
    
        public static @Nullable Version ifPresent(@Nullable Long sequence, String user, @Nullable Long utcTimestamp) {
            return mapIfPresentElse(sequence, __->
                of(sequence.longValue(), user, timestampOfNullable(utcTimestamp)), EMPTY_VERSION);
        }
        
        public static @Nullable Version ifPresent(@Nullable Long sequence, String user, long utcTimestamp) {
            return mapIfPresentElse(sequence, __->
                of(sequence.longValue(), user, utcTimestamp), EMPTY_VERSION);
        }
        
        public static @Nullable Version ifPresent(@Nullable Long sequence, String user) {
            return mapIfPresentElse(sequence, __->
                of(sequence.longValue(), user, EMPTY_TIMESTAMP), EMPTY_VERSION);
        }
        
        public static @Nullable Version parse(@Nullable String sequence, String user, @Nullable String utcTimestamp) {
            return mapIfPresentElse(sequence, __->
                of(Long.parseLong(sequence), user, parseTimeStamp(utcTimestamp)), EMPTY_VERSION); 
        }
        
        // -- HELPER
        
        private static long parseTimeStamp(@Nullable String utcTimestamp) {
            return utcTimestamp != null ? Long.parseLong(utcTimestamp) : EMPTY_TIMESTAMP;
        }
        
        private static long timestampOfNullable(@Nullable Long utcTimestamp) {
            return utcTimestamp != null ? utcTimestamp.longValue() : EMPTY_TIMESTAMP;
        }
        
    }








}
