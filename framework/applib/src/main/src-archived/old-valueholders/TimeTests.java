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

import org.apache.isis.application.value.ValueParseException;


public class TimeTests extends ValueTestCase {
    private Time t;

    public void testTimeConstructors() {
        assertEquals("Two identically created objects", t.dateValue(), new Time(10, 40).dateValue());
        assertEquals("One object created from another", t.dateValue(), new Time(t).dateValue());
    }

    public void testSetTime() {
        Time t2 = new Time();
        t2.setValue(10, 40);
        assertEquals("Set with values", t.dateValue(), t2.dateValue());
        Time t3 = new Time();
        t3.setValue(10, 40);
        assertEquals("Set with values", t.dateValue(), t3.dateValue());
    }

    public void testGetHour() {
        assertEquals(10, t.getHour());
    }

    public void testGetMinute() {
        assertEquals(40, t.getMinute());
    }

    public void testZero() {
        assertEquals("Zero value", 0, Time.getZero());
    }

    public void testGetValue() {
        Time t2 = new Time(0, 0);
        assertEquals("new zero value time", 0, t2.longValue());

        t2 = new Time();
        t2.setValue(0, 0);
        assertEquals("set to zero", 0, t2.longValue());

        Time t3 = new Time(0, 1);
        assertEquals(t2.longValue() + 60, t3.longValue());

        assertEquals(10 * 3600 + 40 * 60, t.longValue());
    }

    public void testClear() {
        assertTrue("After creation should not be empty", !t.isEmpty());
        t.clear();
        assertTrue("After clear should be empty", t.isEmpty());
    }

    public void testDefaultTime() throws InterruptedException {
        Time t1 = new Time();
        assertEquals("temp", t1.dateValue(), new Time().dateValue());
    }

    public void testParseTime() throws ValueParseException {
        t.parseUserEntry("0:00");
        assertEquals("00:00", 0, t.longValue());

        t.parseUserEntry("0:01");
        assertEquals("00:00", 60, t.longValue());

        t.parseUserEntry("11:35 AM");
        assertEquals("11:35", 11 * 3600 + 35 * 60, t.longValue());

        t.parseUserEntry("12:50");
        assertEquals("12:50", 12 * 3600 + 50 * 60, t.longValue());

        t.parseUserEntry("14:45");
        assertEquals("14:45", 14 * 3600 + 45 * 60, t.longValue());

        t.parseUserEntry("22:55");
        assertEquals("22:55", 22 * 3600 + 55 * 60, t.longValue());
        t.parseUserEntry("23:00");
        assertEquals("23:00", 23 * 3600, t.longValue());

        t.parseUserEntry("23:59");
        assertEquals("23:59", 23 * 3600 + 59 * 60, t.longValue());
    }

    public void testIsEqualsTo() {
        Time t2 = new Time(0, 0);
        t.clear();

        assertTrue("When object is empty and is compared with non-empty", !t2.isEqualTo(t));
        assertTrue("When object is non-empty and is compared with empty", !t.isEqualTo(t2));
        t2.clear();
        assertTrue("When both objects are empty", !t.isEqualTo(t2));

        t.setValue(1, 15);
        t2.setValue(1, 00);
        assertTrue("When times are different", !t.isEqualTo(t2));

        t2.setValue(1, 15);
        assertTrue("When times are same", t.isEqualTo(t2));
    }

    public void testParseAdd() throws ValueParseException {
        assertEquals("10:40", 10 * 3600 + 40 * 60, t.longValue());

        t.parseUserEntry("+1");
        assertEquals("11:40", 11 * 3600 + 40 * 60, t.longValue());

        t.parseUserEntry("+1");
        assertEquals("12:40", 12 * 3600 + 40 * 60, t.longValue());

        t.parseUserEntry("+22");
        assertEquals("10:40", 10 * 3600 + 40 * 60, t.longValue());

    }

    public void testSave() throws Exception {
        assertEquals("1040", t.asEncodedString());

        t.setValue(6, 25);
        assertEquals("0625", t.asEncodedString());

        t.setValue(23, 55);
        assertEquals("2355", t.asEncodedString());
    }

    public void testSaveEmpty() throws Exception {
        t.clear();
        assertEquals("NULL", t.asEncodedString());
    }

    public void testRestore() {
        t.restoreFromEncodedString("0805");
        assertEquals(8 * 3600 + 5 * 60, t.longValue());

        t.restoreFromEncodedString("2359");
        assertEquals(23 * 3600 + 59 * 60, t.longValue());
    }

    public void testRestoreEmpty() {
        t.restoreFromEncodedString("NULL");
        assertTrue(t.isEmpty());
    }

    protected void setUp() throws Exception {
        super.setUp();

        t = new Time(10, 40);
    }

}
