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
package org.apache.isis.applib.clock;

import java.time.Instant;
import java.util.Objects;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class VirtualClock_withOffset implements VirtualClock {

    private static final long serialVersionUID = -2589204298085221985L;
    
    /**
     * Amount of time (milli seconds) this clock is offset into the future 
     * with respect to the actual (system) time.
     */
    private final long millisOffset;
    
    @Override
    public Instant nowAsInstant() {
        return Instant.now().plusMillis(millisOffset);
    }
    
    // -- TO STRING, EQUALS, HASHCODE
    
    @Override
    public String toString() {
        return String.format("%s: %s", this.getClass().getSimpleName(), nowAsXmlGregorianCalendar());
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj==null) {
            return false;
        }
        // equal if same class and same millisOffset
        if(!Objects.equals(this.getClass(), obj.getClass())) {
            return false;
        }
        if(!Objects.equals(this.millisOffset, ((VirtualClock_withOffset)obj).millisOffset)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        // equal if same class and same millisOffset
        return Objects.hash(this.getClass(), millisOffset);
    }

}
