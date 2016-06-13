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

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.commons.encoding.Encodable;
import org.apache.isis.core.commons.lang.DateExtensions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;

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
 * The user's name and a timestamp should alos be kept so that when an message
 * is passed to the user it can be of the form "user has change object at time"
 */
public class Version implements Serializable, Encodable {

    private static final long serialVersionUID = 1L;

    private final static OidMarshaller OID_MARSHALLER = OidMarshaller.INSTANCE;

    //region > factory methods

    public static Version create(final Long sequence) {
        return create(sequence, null, (Long)null);
    }

    public static Version create(String sequence, String user, String utcTimestamp) {
        if(sequence == null) { 
            return null;
        }
        return create(Long.parseLong(sequence), user, utcTimestamp != null?Long.parseLong(utcTimestamp):null);
    }

    public static Version create(final Long sequence, final String user, final Date time) {
        return create(sequence, user, time !=null? time.getTime(): null);
    }

    public static Version create(Long sequence, String user, Long utcTimestamp) {
        if(sequence == null) { 
            return null;
        }
        return new Version(sequence, user, utcTimestamp);
    }

    //endregion

    //region > constructor, fields
    private final Long sequence;
    private final String user;
    private final Long utcTimestamp;

    private Version(Long sequence, String user, Long utcTimestamp) {
        this.sequence = sequence;
        this.user = user;
        this.utcTimestamp = utcTimestamp;
    }

    //endregion

    //region > encodable

    public Version(final DataInputExtended input) throws IOException {
        this(input.readLong(), input.readUTF(), input.readLong());
    }


    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        output.writeLong(sequence);
        output.writeUTF(user);
        output.writeLong(utcTimestamp);
    }

    //endregion

    //region > getters
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
    public String getUser() {
        return user;
    }
    
    /**
     * The time of the last change, as UTC milliseconds.
     * 
     * <p>
     * May be null.
     * 
     * @see #getTime()
     */
    public Long getUtcTimestamp() {
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
    public Date getTime() {
        return utcTimestamp != null? new Date(this.utcTimestamp): null;
    }

    //endregion

    //region > enString

    public String enString() {
        return OID_MARSHALLER.marshal(this);
    }

    //endregion

    //region > equals, hashCode

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sequence == null) ? 0 : sequence.hashCode());
        return result;
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
        if (sequence == null) {
            if (other.sequence != null)
                return false;
        } else if (!sequence.equals(other.sequence))
            return false;
        return true;
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

    //endregion

    //region > sequence

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

    //endregion

}
