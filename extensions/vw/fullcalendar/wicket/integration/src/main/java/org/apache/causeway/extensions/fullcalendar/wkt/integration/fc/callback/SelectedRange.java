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

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record SelectedRange(
	LocalDateTime start,
	LocalDateTime end,
	boolean allDay) {

    SelectedRange(final long startEpochMillis, final long endEpochMillis, final boolean allDay) {
        this(
            LocalDateTime.ofEpochSecond(startEpochMillis/1000, 0, ZoneOffset.UTC),
            LocalDateTime.ofEpochSecond(endEpochMillis/1000, 0, ZoneOffset.UTC),
            allDay);
    }
}
