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
import static org.junit.Assert.fail;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.adapters.EncodingException;
import org.apache.isis.applib.value.Date;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.core.metamodel.facets.value.date.DateValueSemanticsProvider;

public class DateValueSemanticsProviderTest extends ValueSemanticsProviderAbstractTestCase {

    private DateValueSemanticsProvider adapter;
    private Date date;
    private FacetHolder holder;

    @Before
    public void setUpObjects() throws Exception {
        context.checking(new Expectations() {
            {
                allowing(mockConfiguration).getString("isis.value.format.date");
                will(returnValue(null));
            }
        });

        TestClock.initialize();
        date = new Date(2001, 2, 4);
        holder = new FacetHolderImpl();
        setValue(adapter = new DateValueSemanticsProvider(holder, mockServicesInjector));
    }

    @Test
    public void testDateAsEncodedString() throws Exception {
        assertEquals("20010204", adapter.toEncodedString(date));
    }

    @Test
    public void testParseEntryOfDaysAfterDate() throws Exception {
        final Date parsed = adapter.parseTextEntry(date, "+7");
        assertEquals(new Date(2001, 2, 11), parsed);
    }

    @Test
    public void testParseEntryOfDaysAfterToToday() throws Exception {
        final Date parsed = adapter.parseTextEntry(null, "+5");
        assertEquals(new Date(2003, 8, 22), parsed);
    }

    @Test
    public void testParseEntryOfDaysBeforeDate() throws Exception {
        final Date parsed = adapter.parseTextEntry(date, "-7");
        assertEquals(new Date(2001, 1, 28), parsed);
    }

    @Test
    public void testParseEntryOfDaysBeforeToToday() throws Exception {
        final Date parsed = adapter.parseTextEntry(null, "-5");
        assertEquals(new Date(2003, 8, 12), parsed);
    }

    @Test
    public void testParseEntryOfKeywordToday() throws Exception {
        final Date parsed = adapter.parseTextEntry(date, "today");
        assertEquals(new Date(2003, 8, 17), parsed);
    }

    @Test
    public void testParseEntryOfWeeksAfterDate() throws Exception {
        final Date parsed = adapter.parseTextEntry(date, "+3w");
        assertEquals(new Date(2001, 2, 25), parsed);
    }

    @Test
    public void testParseEntryOfWeeksAfterToToday() throws Exception {
        final Date parsed = adapter.parseTextEntry(null, "+4w");
        assertEquals(new Date(2003, 9, 14), parsed);
    }

    @Test
    public void testParseEntryOfWeeksBeforeDate() throws Exception {
        final Date parsed = adapter.parseTextEntry(date, "-3w");
        assertEquals(new Date(2001, 1, 14), parsed);
    }

    @Test
    public void testParseEntryOfWeeksBeforeToToday() throws Exception {
        final Date parsed = adapter.parseTextEntry(null, "-4w");
        assertEquals(new Date(2003, 7, 20), parsed);
    }

    @Test
    public void testParseEntryOfMonthsAfterDate() throws Exception {
        final Date parsed = adapter.parseTextEntry(date, "+3m");
        assertEquals(new Date(2001, 5, 4), parsed);
    }

    @Test
    public void testParseEntryOfMonthsAfterToToday() throws Exception {
        final Date parsed = adapter.parseTextEntry(null, "+4m");
        assertEquals(new Date(2003, 12, 17), parsed);
    }

    @Test
    public void testParseEntryOfMonthsBeforeDate() throws Exception {
        final Date parsed = adapter.parseTextEntry(date, "-3m");
        assertEquals(new Date(2000, 11, 4), parsed);
    }

    @Test
    public void testParseEntryOfMonthsBeforeToToday() throws Exception {
        final Date parsed = adapter.parseTextEntry(null, "-4m");
        assertEquals(new Date(2003, 4, 17), parsed);
    }

    @Test(expected = TextEntryParseException.class)
    public void illegalEntry() throws Exception {
        adapter.parseTextEntry(null, "xxx");
    }

    @Test
    public void testRestoreOfInvalidDatal() throws Exception {
        try {
            adapter.fromEncodedString("2003may12");
            fail();
        } catch (final EncodingException expected) {
        }
    }

}
