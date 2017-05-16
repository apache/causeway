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

package org.apache.isis.core.metamodel.facets.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.value.DateTime;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facets.value.datetime.DateTimeValueSemanticsProvider;

public class DateTimeValueSemanticsProviderTest extends ValueSemanticsProviderAbstractTestCase {

    private DateTimeValueSemanticsProvider adapter;
    private FacetHolder holder;

    @Before
    public void setUpObjects() throws Exception {
        context.checking(new Expectations() {
            {
                allowing(mockConfiguration).getString("isis.value.format.datetime");
                will(returnValue(null));
            }
        });

        TestClock.initialize();
        holder = new FacetHolderImpl();
        setValue(adapter = new DateTimeValueSemanticsProvider(holder, mockServicesInjector));
    }

    @Test
    public void testNow() {
        assertEntry("now", 2003, 8, 17, 21, 30, 25);
    }

    @Test
    public void testToday() {
        assertEntry("today", 2003, 8, 17, 21, 30, 25);
    }

    @Test
    public void testEntryWithShortFormat() {
        final String entry = "21/5/07 10:30";
        final int year = 2007;
        final int month = 5;
        final int day = 21;
        final int hour = 10;
        final int minute = 30;
        assertEntry(entry, year, month, day, hour, minute, 0);

        // assertEquals("21-May-2007 10:30", adapter.titleString(null));
    }

    private void assertEntry(final String entry, final int year, final int month, final int day, final int hour, final int minute, final int second) {
        final Object object = adapter.parseTextEntry(null, entry);
        assertEquals(new DateTime(year, month, day, hour, minute, second), object);
    }

    @Test
    public void testEntryWithMediumFormat() {
        assertEntry("21-May-2007 10:30", 2007, 5, 21, 10, 30, 0);
    }

    @Test
    public void testEntryWithShortISOFormat() {
        assertEntry("20070521T1030", 2007, 5, 21, 10, 30, 0);
    }

    @Test
    public void testEntryWithLongISOFormat() {
        assertEntry("2007-05-21 10:30", 2007, 5, 21, 10, 30, 0);
    }

    @Test
    public void testEntryWithLongISOFormatAndSeconds() {
        assertEntry("2007-05-21 10:30:40", 2007, 5, 21, 10, 30, 40);
    }

    @Test
    public void testEmptyClears() {
        assertNull(adapter.parseTextEntry(null, ""));
    }

    @Test
    public void testAddDayAndMonth() {
        assertEntry("+1d 1m", 2003, 9, 18, 21, 30, 25);
    }

    @Test
    public void testSubtractDayAndMonth() {
        assertEntry("-1d 1m", 2003, 7, 16, 21, 30, 25);
    }

    @Test
    public void testAddOneDay() {
        assertEntry("+1d", 2003, 8, 18, 21, 30, 25);
    }

    @Test
    public void testSubtractOneDay() {
        assertEntry("-1d", 2003, 8, 16, 21, 30, 25);
    }

    @Test
    public void testAddOneMonth() {
        assertEntry("+1m", 2003, 9, 17, 21, 30, 25);
    }

    @Test
    public void testAddOneYear() {
        assertEntry("+1y", 2004, 8, 17, 21, 30, 25);
    }

    @Test
    public void testAddOneHour() {
        assertEntry("+1H", 2003, 8, 17, 22, 30, 25);
    }

    @Test
    public void testAddOneMinute() {
        assertEntry("+1M", 2003, 8, 17, 21, 31, 25);
    }

}
