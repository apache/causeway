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
package org.apache.isis.extensions.fullcalendar.applib.spi;

import org.apache.isis.extensions.fullcalendar.applib.CalendarEventable;
import org.apache.isis.extensions.fullcalendar.applib.Calendarable;

/**
 * Optional SPI service that allows a {@link Calendarable} or {@link CalendarEventable} object to be translated/dereferenced to some other object, typically its owner.  The markers on the calendar then open up the dereferenced object, rather than the original
 * {@link Calendarable} or {@link CalendarEventable} object.
 *
 * <p>
 *     For example, the <tt>incode-module-commchannel</tt>'s <tt>Note</tt> implements <tt>CalendarEventable</tt>, but this service allows the <i>owner</i> of the
 *     <tt>Note</tt> (ie the <tt>Notable</tt>) sto be shown instead.
 * </p>
 *
 * @since 2.0 {@index}
 */
public interface CalendarableDereferencingService {
	Object dereference(final Object calendarableOrCalendarEventable);
}
