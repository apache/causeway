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

import java.time.ZoneId;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class VirtualClock_Test {

    private VirtualClock virtualClock;

    @BeforeEach
    void setup() {
        virtualClock = VirtualClock.frozenTestClock();
    }

    @Test
    void nowAt() {
        Assertions.assertThat(virtualClock.nowAsInstant()).isEqualTo("2003-07-17T21:30:25Z");
    }

    @Test
    void nowAsEpochMilli() {
        Assertions.assertThat(virtualClock.nowAsEpochMilli()).isEqualTo(1058477425000L);
    }

    @Test
    void nowAsLocalDate() {
        Assertions.assertThat(virtualClock.nowAsLocalDate(ZoneId.of("UTC")).toString()).isEqualTo("2003-07-17");
    }

    @Test
    void nowAsLocalDateTime() {
        Assertions.assertThat(virtualClock.nowAsLocalDateTime(ZoneId.of("UTC")).toString()).isEqualTo("2003-07-17T21:30:25");
    }

    @Test
    void nowAsOffsetDateTime() {
        Assertions.assertThat(virtualClock.nowAsOffsetDateTime(ZoneId.of("UTC")).toString()).isEqualTo("2003-07-17T21:30:25Z");
    }

    @Test
    @Disabled // depends on the timezone
    void nowAsJavaUtilDate() {
        Assertions.assertThat(virtualClock.nowAsJavaUtilDate().toString()).isEqualTo("Thu Jul 17 22:30:25 BST 2003");
    }

    @Test
    @Disabled // depends on the timezone
    void nowAsJavaSqlTimestamp() {
        Assertions.assertThat(virtualClock.nowAsJavaSqlTimestamp().toString()).isEqualTo("2003-07-17 22:30:25.0");
    }

    @Test
    @Disabled // depends on the timezone
    void nowAsXmlGregorianCalendar() {
        Assertions.assertThat(virtualClock.nowAsXmlGregorianCalendar().toString()).isEqualTo("2003-07-17T22:30:25.000+01:00");
    }

    @Test
    void nowAsJodaDateTime() {
        Assertions.assertThat(virtualClock.nowAsJodaDateTime(ZoneId.of("UTC")).toString()).isEqualTo("2003-07-17T21:30:25.000Z");
    }

    @Test
    void nowAsJodaLocalDate() {
        Assertions.assertThat(virtualClock.nowAsJodaLocalDate(ZoneId.of("UTC")).toString()).isEqualTo("2003-07-17");
    }

}