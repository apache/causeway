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

package org.apache.isis.core.metamodel.adapter.oid;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.core.commons.encoding.Encodable;
import org.apache.isis.core.metamodel.adapter.oid.Oid.Version;

import com.google.common.base.Strings;


/**
 * An immutable identifier for either a root object (subtype {@link RootOid}) or 
 * an aggregated object (subtype {@link AggregatedOid}).
 * 
 * <p>
 * Note that value objects (strings, ints, {@link Value}s etc) do not have an {@link Oid}. 
 */
public interface Oid extends Encodable {

    /**
     * A string representation of this {@link Oid}.
     */
    String enString(OidMarshaller oidMarshaller);

    String enStringNoVersion(OidMarshaller oidMarshaller);
    
    Version getVersion();

    /**
     * Flags whether this OID is for a transient (not-yet-persisted) object.
     * 
     * <p>
     * In the case of an {@link AggregatedOid}, is determined by the state 
     * of its {@link AggregatedOid#getParentOid() parent}'s {@link RootOid#isTransient() state}.
     */
    boolean isTransient();

    public static enum State {
        PERSISTENT("P"), TRANSIENT("T");
        
        private final String code;
        private State(final String code) {
            this.code = code;
        }
        
        public boolean isTransient() {
            return this == TRANSIENT;
        }

        public static State valueOf(boolean isTransient) {
            return isTransient? TRANSIENT: PERSISTENT;
        }

        public String getCode() {
            return code;
        }
    }

    
    public static final class Version {
        
        private final Long sequence;
        private final String user;
        private final Long utcTimestamp;

        public static Version create(String sequence, String user, String utcTimestamp) {
            if(sequence == null) { 
                return null;
            }
            return new Version(sequence, user, utcTimestamp);
        }

        public static Version create(Long sequence, String user, Long utcTimestamp) {
            if(sequence == null) { 
                return null;
            }
            return new Version(sequence, user, utcTimestamp);
        }

        private Version(String sequence, String user, String utcTimestamp) {
            this(Long.parseLong(sequence), user, utcTimestamp != null?Long.parseLong(utcTimestamp):null); 
        }

        private Version(Long sequence, String user, Long utcTimestamp) {
            this.sequence = sequence;
            this.user = user;
            this.utcTimestamp = utcTimestamp;
        }

        public Long getSequence() {
            return sequence;
        }
        
        public String getUser() {
            return user;
        }
        
        public Long getUtcTimestamp() {
            return utcTimestamp;
        }

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
        
        public String enString(OidMarshaller oidMarshaller) {
            return oidMarshaller.marshal(this);

        }


    }
    
}
