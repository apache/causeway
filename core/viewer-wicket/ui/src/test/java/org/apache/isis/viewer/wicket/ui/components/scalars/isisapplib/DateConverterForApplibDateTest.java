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
package org.apache.isis.viewer.wicket.ui.components.scalars.isisapplib;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.TimeZone;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.value.Date;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;

public class DateConverterForApplibDateTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private WicketViewerSettings settings;

    private TimeZone timezone;

    @Before
    public void setUp() throws Exception {
        context.checking(new Expectations() {
            {
                allowing(settings).getDatePattern();
                will(returnValue("yyyy-MM-dd"));
            }
        });
    }
    
    @Before
    public void fixTimezone() throws Exception {
        timezone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    
    @Before
    public void restoreTimezone() throws Exception {
        TimeZone.setDefault(timezone);
    }
    
    @Test
    public void roundtripWhenParsingDateFormat() {
        final DateConverterForApplibDate converter = new DateConverterForApplibDate(settings, 0);
        final Date dt = converter.convertToObject("2013-05-11", null);
        assertThat(dt, is(new Date(2013, 05, 11)));
        
        final String str = converter.convertToString(dt, null);
        assertThat(str, is("2013-05-11"));
    }

    @Test
    public void roundtripWhenParsingDateFormatWithAdjustBy() {
        final DateConverterForApplibDate converter = new DateConverterForApplibDate(settings, -1);
        final Date dt = converter.convertToObject("2013-05-11", null);
        assertThat(dt, is(new Date(2013, 05, 12)));
        
        final String str = converter.convertToString(dt, null);
        assertThat(str, is("2013-05-11"));
    }

}
