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


package org.apache.isis.core.progmodel.facets.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.applib.adapters.EncodingException;
import org.apache.isis.applib.value.Time;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;

@RunWith(JMock.class)
public class TimeValueSemanticsProviderTest extends ValueSemanticsProviderAbstractTestCase {

    private TimeValueSemanticsProvider adapter;
    private Time time;
    private FacetHolder holder;

    @Before
    public void setUpObjects() throws Exception {
        mockery.checking(new Expectations(){{
        	allowing(mockConfiguration).getString("isis.value.format.time");
        	will(returnValue(null));
        }});

        TestClock.initialize();
        setupSpecification(Time.class);
        time = new Time(8, 13);
        holder = new FacetHolderImpl();
        setValue(adapter = new TimeValueSemanticsProvider(holder, mockConfiguration, mockContext));
    }

    @Test
    public void testTimeAsEncodedString() throws Exception {
        assertEquals("081300000", adapter.toEncodedString(time));
    }

    @Test
    public void testParseEntryOfHoursAfterTime() throws Exception {
        // TimeValueSemanticsProvider adapter = new TimeValueSemanticsProvider(new Time(15, 10));
        final Object parsed = adapter.parseTextEntry(time, "+5H");
        assertEquals(new Time(13, 13), parsed);
    }

    @Test
    public void testParseEntryOfHoursAfterNow() throws Exception {
        // TimeValueSemanticsProvider adapter = new TimeValueSemanticsProvider();
        final Object parsed = adapter.parseTextEntry(null, "+5H");
        assertEquals(new Time(2, 30), parsed);
    }

    @Test
    public void testParseEntryOfHoursBeforeTime() throws Exception {
        // TimeValueSemanticsProvider adapter = new TimeValueSemanticsProvider(new Time(12, 4));
        final Object parsed = adapter.parseTextEntry(time, "-7H");
        assertEquals(new Time(1, 13), parsed);
    }

    @Test
    public void testParseEntryOfHoursBeforeToNow() throws Exception {
        // TimeValueSemanticsProvider adapter = new TimeValueSemanticsProvider();
        final Object parsed = adapter.parseTextEntry(null, "-5H");
        assertEquals(new Time(16, 30), parsed);
    }

    @Test
    public void testParseEntryOfKeywordNow() throws Exception {
        // TimeValueSemanticsProvider adapter = new TimeValueSemanticsProvider();
        final Object parsed = adapter.parseTextEntry(time, "now");
        assertEquals(new Time(), parsed);
    }

    @Test
    public void testRestoreTime() throws Exception {
        // TimeValueSemanticsProvider adapter = new TimeValueSemanticsProvider();
        final Object parsed = adapter.fromEncodedString("213000000");
        assertEquals(new Time(21, 30), parsed);
    }

    @Test
    public void testRestoreOfInvalidDatal() throws Exception {
        try {
            adapter.fromEncodedString("two ten");
            fail();
        } catch (final EncodingException expected) {}
    }

}

