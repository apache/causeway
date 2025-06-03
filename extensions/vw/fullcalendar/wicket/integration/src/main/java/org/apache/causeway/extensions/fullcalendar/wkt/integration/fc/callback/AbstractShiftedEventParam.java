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
package org.apache.causeway.extensions.fullcalendar.wkt.integration.fc.callback;

import java.time.ZonedDateTime;

import org.apache.causeway.extensions.fullcalendar.wkt.integration.fc.Event;
import org.apache.causeway.extensions.fullcalendar.wkt.integration.fc.EventSource;

import lombok.Getter;

class AbstractShiftedEventParam extends AbstractEventParam {

    @Getter private final int daysDelta;
    @Getter private final int minutesDelta;

	public AbstractShiftedEventParam(final EventSource source, final Event event, final int hoursDelta, final int minutesDelta) {
		super(source, event);
		this.daysDelta = hoursDelta;
		this.minutesDelta = minutesDelta;
	}

	public ZonedDateTime getNewStartTime() {
		return shift(getEvent().getStart());
	}

	public ZonedDateTime getNewEndTime() {
		return shift(getEvent().getEnd());
	}

	private ZonedDateTime shift(final ZonedDateTime start) {
		return start.plusDays(daysDelta).plusMinutes(minutesDelta);
	}

}
