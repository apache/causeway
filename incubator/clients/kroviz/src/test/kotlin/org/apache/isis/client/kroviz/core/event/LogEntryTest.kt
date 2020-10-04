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
package org.apache.isis.client.kroviz.core.event

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogEntryTest {

    @Test
    fun testTitle() {
        // given
        val url = "https://kroki.io"

        // when
        val le = LogEntry(url)

        // then
        assertFalse(le.title.startsWith("/"))
    }

    @Test
    fun testCalculate() {
        // given
        val le = LogEntry("http://test/url")

        // when
        le.setSuccess()

        // then
        assertTrue(0 <= le.duration)

        if (le.duration < 0 && le.cacheHits == 0) {
            //TODO add assert
        }
    }

}
