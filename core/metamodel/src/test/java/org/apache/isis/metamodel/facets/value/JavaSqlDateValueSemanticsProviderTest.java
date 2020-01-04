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

package org.apache.isis.metamodel.facets.value;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.isis.metamodel.context.MetaModelContextAware;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.metamodel.facets.value.datesql.JavaSqlDateValueSemanticsProvider;
import org.apache.isis.unittestsupport.config.internal._Config;

public class JavaSqlDateValueSemanticsProviderTest extends ValueSemanticsProviderAbstractTestCase {

    private JavaSqlDateValueSemanticsProvider adapter;
    private Date date;
    private FacetHolder holder;

    @Before
    public void setUpObjects() throws Exception {

        _Config.put("isis.value.format.date", null);

        TestClock.initialize();
        date = new Date(0);
        holder = new FacetHolderImpl();
        ((MetaModelContextAware)holder).setMetaModelContext(super.metaModelContext);
        
        setValue(adapter = new JavaSqlDateValueSemanticsProvider(holder) {
            @Override
            protected String defaultFormat() {
                return "iso";
            }
        });
    }

    @Test
    public void testInvalidParse() throws Exception {
        try {
            adapter.parseTextEntry(null, "date");
            fail();
        } catch (final TextEntryParseException expected) {
        }
    }

    @Test
    public void testTitleOf() {
        assertEquals(DateFormat.getDateInstance(SimpleDateFormat.MEDIUM).format(new Date(0)), adapter.displayTitleOf(date));
    }

    @Test
    public void testParse() throws Exception {
        final Object newValue = adapter.parseTextEntry(null, "1/1/1980");

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        calendar.set(1980, 0, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        assertEquals(calendar.getTime(), newValue);
    }

}
