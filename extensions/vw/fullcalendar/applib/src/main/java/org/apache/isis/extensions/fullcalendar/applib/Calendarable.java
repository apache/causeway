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
package org.apache.isis.extensions.fullcalendar.applib;

import java.util.Set;

import com.google.common.collect.ImmutableMap;


public interface Calendarable {

    /**
     * The names of unique &quot;calendar&quot;s provided by this object.
     * 
     * <p>
     * The &quot;calendar&quot; is a string identifier that indicates the nature of this event.
     * 
     * <p>
     * For example, an event of a lease's <code>FixedBreakOption</code> has three dates: the <i>break date</i>,
     * the <i>exercise date</i> and the <i>reminder date</i>.  These therefore correspond to three different 
     * calendar names, respectively <i>Fixed break</i>, <i>Fixed break exercise</i> and 
     * <i>Fixed break exercise reminder</i>.
     */
    Set<String> getCalendarNames();
    
    /**
     * The events associated with this object, keyed by their corresponding {@link #getCalendarNames() calendar name}.
     */
	ImmutableMap<String, CalendarEventable> getCalendarEvents();
	
}
