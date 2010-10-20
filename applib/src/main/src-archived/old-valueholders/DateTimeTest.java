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


package org.apache.isis.application.valueholder;

public class DateTimeTest extends ValueTestCase {
    private DateTime actual;

    protected void setUp() throws Exception {
        super.setUp();
        actual = new DateTime(2000, 2, 1, 10, 59, 30);
    }

    public void testGetDay() {
        assertEquals(1, actual.getDay());
    }

    public void testGetMonth() {
        assertEquals(2, actual.getMonth());
    }

    public void testGetYear() {
        assertEquals(2000, actual.getYear());
    }

    public void testGetMinute() {
        assertEquals(59, actual.getMinute());
    }

    public void testGetHour() {
        assertEquals(10, actual.getHour());
    }

    public void testSaveRestore() throws Exception {
        DateTime timeStamp1 = new DateTime();
        timeStamp1.parseUserEntry("2003-1-4 10:45");
        assertFalse(timeStamp1.isEmpty());

        DateTime timeStamp2 = new DateTime();
        timeStamp2.restoreFromEncodedString(timeStamp1.asEncodedString());
        assertEquals(timeStamp1.longValue(), timeStamp2.longValue());
        assertFalse(timeStamp2.isEmpty());
    }

    public void testSaveRestorOfNull() throws Exception {
        DateTime timeStamp1 = new DateTime();
        timeStamp1.clear();
        assertTrue("DateTime isEmpty", timeStamp1.isEmpty());

        DateTime timeStamp2 = new DateTime();
        timeStamp2.restoreFromEncodedString(timeStamp1.asEncodedString());
        // assertEquals(timeStamp1.longValue(), timeStamp2.longValue());
        assertTrue(timeStamp2.isEmpty());
    }

    public void testNew() {
        DateTime expected = new DateTime(2003, 8, 17, 21, 30, 25);
        DateTime actual = new DateTime();
        
// FIX       assertEquals(expected, actual);
    }

    public void testNow() {
        DateTime expected = new DateTime(2003, 8, 17, 21, 30, 25);
        DateTime actual = new DateTime();
        actual.reset();
 // FIX       assertEquals(expected, actual);
    }
}
