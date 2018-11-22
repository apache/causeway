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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.config.internal._Config;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.core.metamodel.facets.value.dateutil.JavaUtilDateValueSemanticsProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JavaUtilDateValueSemanticsProviderTest extends ValueSemanticsProviderAbstractTestCase {

    private java.util.Date date;
    private FacetHolder holder;

    @Before
    public void setUpObjects() throws Exception {
        
        _Config.put("isis.value.format.datetime", null);

        TestClock.initialize();
        date = new java.util.Date(0);

        holder = new FacetHolderImpl();
        setValue(new JavaUtilDateValueSemanticsProvider(holder, mockServicesInjector) {
            @Override
            protected String defaultFormat() {
                return "iso";
            }
        });
    }

    @Test
    public void testInvalidParse() throws Exception {
        try {
            getValue().parseTextEntry(null, "invalid entry");
            fail();
        } catch (final TextEntryParseException expected) {
        }
    }

    /**
     * Something rather bizarre here, that the epoch formats as 01:00 rather
     * than 00:00. It's obviously because of some sort of timezone issue, but I
     * don't know where that dependency is coming from.
     */
    @Test
    public void testTitleOf() {
        final String EXPECTED = DateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT).format(new java.util.Date(0));
        assertEquals(EXPECTED, getValue().displayTitleOf(date));
    }

    @Test
    public void testParse() throws Exception {
        final Object newValue = getValue().parseTextEntry(null, "1980-01-01 10:40");

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        calendar.set(1980, 0, 1, 10, 40, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        assertEquals(calendar.getTime(), newValue);
    }

}
