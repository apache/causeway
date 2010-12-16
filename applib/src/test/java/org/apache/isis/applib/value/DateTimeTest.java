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

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

// TODO review all date based classes - should we use factory and service to create and work with date type values?
public class DateTimeTest {

    private DateTime actual;

    @Before
    public void setUp() throws Exception {
        TestClock.initialize();
        actual = new DateTime(2000, 3, 14, 10, 45);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetDay() {
        assertEquals(14, actual.getDay());
    }

    @Test
    public void testGetMonth() {
        assertEquals(3, actual.getMonth());
    }

    @Test
    public void testGetYear() {
        assertEquals(2000, actual.getYear());
    }

    @Test
    public void test24Hour() {
        final DateTime dt = new DateTime(2006, 05, 07, 23, 59);
        assertEquals("hour", 23, dt.getHour());
        assertEquals("minute", 59, dt.getMinute());
    }

    @Ignore("TODO: need to reinstate functionality")
    @Test
    public void testAdd() {
        // DateTime resultDateTime = actual.add(1, 2, 3);
        // assertEquals(17, resultDateTime.getDay());
        // assertEquals(5, resultDateTime.getMonth());
        // assertEquals(2001, resultDateTime.getYear());
    }

    @Ignore("TODO: may not work")
    @Test
    public void testEquals() throws Exception {
        assertTrue(actual.equals(actual));
        assertTrue(new DateTime(2003, 8, 17).equals(new DateTime()));
        assertTrue(actual.equals(new DateTime(2000, 3, 14, 10, 45)));
    }

    @Ignore("TODO: may not work")
    @Test
    public void testIsLestThan() throws Exception {
        assertFalse(new DateTime(2003, 8, 17).isLessThan(new DateTime(2003, 8, 17)));
        assertTrue(new DateTime(2003, 8, 16).isLessThan(new DateTime(2003, 8, 17)));
    }

    @Ignore("TODO: need to reinstate functionality")
    @Test
    public void testSameDayOfWeekAs() throws Exception {
        // assertTrue(new DateTime(2000, 2, 17).sameDayOfWeekAs(new DateTime(2003, 8, 7))); // Thursday
        // assertFalse(new DateTime(2000, 2, 15).sameDayOfWeekAs(new DateTime(2003, 8, 17))); // Tues &
    }

    @Ignore("TODO: need to reinstate functionality")
    @Test
    public void testSameDayOfMonthAs() throws Exception {
        // assertTrue(new DateTime(2000, 2, 17).sameDayOfMonthAs(new DateTime(2003, 8, 17)));
        // assertFalse(new DateTime(2000, 2, 15).sameDayOfMonthAs(new DateTime(2003, 8, 17)));
    }

    @Ignore("TODO: need to reinstate functionality")
    @Test
    public void testSameDayOfYearAs() throws Exception {
        // assertTrue(new DateTime(2001, 8, 17).sameDayOfYearAs(new DateTime(2003, 8, 17)));
        // assertTrue(new DateTime(1999, 3, 1).sameDayOfYearAs(new DateTime(2000, 2, 29))); // leap year
        // assertFalse(new DateTime(2001, 3, 1).sameDayOfYearAs(new DateTime(2000, 3, 2)));
    }

    @Ignore("TODO: need to reinstate functionality")
    @Test
    public void testSameWeekAs() throws Exception {
        // assertFalse(new DateTime(2000, 2, 15).sameWeekAs(new DateTime(2000, 2, 12))); // Tue, week 7 and Sat,
        // // week 6
        // assertTrue(new DateTime(2001, 2, 16).sameWeekAs(new DateTime(2002, 2, 11))); // Tue, week 7, and Thu,
        // // week 7
    }

    @Ignore("TODO: need to reinstate functionality")
    @Test
    public void testSameMonthAs() throws Exception {
        // assertTrue(new DateTime(2000, 8, 15).sameMonthAs(new DateTime(2003, 8, 17)));
        // assertFalse(new DateTime(2003, 2, 17).sameMonthAs(new DateTime(2003, 8, 17)));
    }

    @Ignore("TODO: need to reinstate functionality")
    @Test
    public void testSameYearAs() throws Exception {
        // assertTrue(new DateTime(2003, 2, 15).sameYearAs(new DateTime(2003, 8, 17)));
        // assertFalse(new DateTime(2000, 2, 15).sameYearAs(new DateTime(2003, 8, 17)));
    }

    @Ignore("TODO: may not work")
    @Test
    public void testDateTimeValue() {
        final DateTime date = new DateTime(1970, 1, 1);
        assertEquals(1970, date.getYear());
        assertEquals(1, date.getMonth());
        assertEquals(1, date.getDay());
        final long time = date.dateValue().getTime();
        assertEquals(1000 * 60 * 60 * 24 * 0, time);
    }

    @Ignore("TODO: need to reinstate functionality")
    @Test
    public void testStartOfYear() {
        // assertEquals(new DateTime(2000, 1, 1), actual.startOfYear());
    }

    @Ignore("TODO: need to reinstate functionality")
    @Test
    public void testStartOfMonth() {
        // assertEquals(new DateTime(2000, 3, 1), actual.startOfMonth());
    }

    @Ignore("TODO: need to reinstate functionality")
    @Test
    public void testStartOfWeek() {
        // assertEquals(new DateTime(2000, 3, 13), actual.startOfWeek());
        // assertEquals(new DateTime(2000, 2, 28), new DateTime(2000, 3, 2).startOfWeek());
    }

    @Ignore("TODO: may not work")
    @Test
    public void testNewWithTodaysDateTime() {
        final DateTime actual = new DateTime();
        final DateTime expected = new DateTime(2003, 8, 17);
        assertEquals(expected, actual);
    }

    @Ignore("TODO: may not work")
    @Test
    public void testToString() {
        assertEquals("2000-3-14", actual.toString());
    }

}
