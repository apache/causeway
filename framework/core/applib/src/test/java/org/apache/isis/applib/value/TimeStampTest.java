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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class TimeStampTest {

    private TimeStamp timeStamp;

    @Before
    public void setUp() throws Exception {
        TestClock.initialize();
        timeStamp = new TimeStamp();
    }

    @Test
    public void testCreatesToClocksTime() {
        assertEquals(1061155825000L, timeStamp.longValue());
    }

    @Test
    public void testEqualsTo() {
        final TimeStamp timeStamp2 = new TimeStamp();
        assertFalse(timeStamp2 == timeStamp);

        assertTrue(timeStamp.isEqualTo(timeStamp2));
        assertTrue(timeStamp2.isEqualTo(timeStamp));
    }

    @Test
    public void testLessThan() {
        final TimeStamp timeStamp2 = new TimeStamp(1061155825050L);

        assertTrue(timeStamp.isLessThan(timeStamp2));
        assertFalse(timeStamp2.isLessThan(timeStamp));
    }

}
