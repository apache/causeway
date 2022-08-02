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
package org.apache.isis.commons.internal.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.val;

class TemporalsTest {

    @Test
    void roundtripOffsetTime() {
        for(val temporal: _Temporals.sampleOffsetTime()) {
            assertEquals(temporal, _Temporals.destringAsOffsetTime(_Temporals.enstringOffsetTime(temporal)));
        }
    }

    @Test
    void roundtripOffsetDateTime() {
        for(val temporal: _Temporals.sampleOffsetDateTime()) {
            assertEquals(temporal, _Temporals.destringAsOffsetDateTime(_Temporals.enstringOffsetDateTime(temporal)));
        }
    }

    @Test
    void roundtripZonedDateTime() {
        for(val temporal: _Temporals.sampleZonedDateTime()) {
            assertEquals(temporal, _Temporals.destringAsZonedDateTime(_Temporals.enstringZonedDateTime(temporal)));
        }
    }

}
