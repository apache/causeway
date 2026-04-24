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
package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.experimental.UtilityClass;

import java.sql.Timestamp;
import java.time.Instant;

@UtilityClass
class TimestampMarshallUtil {
    private static final java.time.format.DateTimeFormatter VM_MEMENTO_FORMATTER =
            java.time.format.DateTimeFormatter
                    .ofPattern("uuuu-MM-dd'T'HH-mm-ss.SSSX")
                    .withZone(java.time.ZoneOffset.UTC);

    static String toString(Timestamp ts) {
        // Human-readable and URL-friendly (no ':' or '~').
        return VM_MEMENTO_FORMATTER.format(ts.toInstant()); // e.g. 2026-04-22T02-00-00.000Z
    }

    static Timestamp fromString(String s, Timestamp fallback) {
        if (s == null || s.isBlank()) {
            return fallback;
        }
        try {
            return Timestamp.from(Instant.from(VM_MEMENTO_FORMATTER.parse(s)));
        } catch (Exception e) {
            return fallback;
        }
    }
}
