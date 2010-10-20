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

import org.apache.isis.application.system.TestClock;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class DateTest extends ValueTestCase {

    private Date actual;

    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
        
        Date.setClock(new TestClock());
        actual = new Date(2000, 2, 1);
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

    public void testNew() {
        Date expected = new Date(2003, 8, 17);
        Date actual = new Date();
        assertEquals(expected, actual);
    }

    public void testReset() {
        Date expected = new Date(2003, 8, 17);
        actual.reset();
        assertEquals(expected, actual);
    }

    public void testSaveRestore() throws Exception {
        Date date1 = new Date();
        date1.parseUserEntry("2003-1-4");
        assertFalse(date1.isEmpty());

        Date date2 = new Date();
        date2.restoreFromEncodedString(date1.asEncodedString());
        assertEquals(date1.dateValue(), date2.dateValue());
        assertFalse(date2.isEmpty());
    }

    public void testSaveRestorOfNull() throws Exception {
        Date date1 = new Date();
        date1.clear();
        assertTrue("Date isEmpty", date1.isEmpty());

        Date date2 = new Date();
        date2.restoreFromEncodedString(date1.asEncodedString());
        assertEquals(date1.dateValue(), date2.dateValue());
        assertTrue(date2.isEmpty());
    }

    public void testToday() {
        Date expected = new Date(2003, 8, 17);
        actual.today();
        assertEquals(expected, actual);
    }
}
