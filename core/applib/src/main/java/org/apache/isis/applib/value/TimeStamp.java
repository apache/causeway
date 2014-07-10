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

package org.apache.isis.applib.value;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.clock.Clock;

/**
 * Value object representing a date/time value marking a point in time This is a
 * user facing date/time value, more a marker used to indicate the temporal
 * relationship between two objects.
 * 
 * @see DateTime
 */
@Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.value.timestamp.TimeStampValueSemanticsProvider")
public class TimeStamp extends Magnitude<TimeStamp> {

    private static final long serialVersionUID = 1L;
    private final long time;

    /**
     * Create a TimeStamp object for storing a timeStamp set to the current
     * time.
     */
    public TimeStamp() {
        time = Clock.getTime();
    }

    public TimeStamp(final long time) {
        this.time = time;
    }

    /**
     * returns true if the time stamp of this object has the same value as the
     * specified timeStamp
     */
    @Override
    public boolean isEqualTo(final TimeStamp timeStamp) {
        return this.time == (timeStamp).time;
    }

    /**
     * returns true if the timeStamp of this object is earlier than the
     * specified timeStamp
     */
    @Override
    public boolean isLessThan(final TimeStamp timeStamp) {
        return time < (timeStamp).time;
    }

    public long longValue() {
        return time;
    }

    @Override
    public String toString() {
        return "Time Stamp " + longValue();
    }
}
