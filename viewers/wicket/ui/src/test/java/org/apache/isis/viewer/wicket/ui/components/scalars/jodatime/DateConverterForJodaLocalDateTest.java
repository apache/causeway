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
package org.apache.isis.viewer.wicket.ui.components.scalars.jodatime;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;

public class DateConverterForJodaLocalDateTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private WicketViewerSettings settings;

    @Before
    public void setUp() throws Exception {
        context.checking(new Expectations() {
            {
                allowing(settings).getDatePattern();
                will(returnValue("yyyy-MM-dd"));
            }
        });
    }

    @Test
    public void roundtrip() {
        final DateConverterForJodaLocalDate converter = new DateConverterForJodaLocalDate(settings, 0);
        final LocalDate dt = converter.convertToObject("2013-05-11", null);
        assertThat(dt, is(new LocalDate(2013, 05, 11)));

        final String str = converter.convertToString(dt, null);
        assertThat(str, is("2013-05-11"));
    }

    @Test
    public void roundtripWithAdjustBy() {
        final DateConverterForJodaLocalDate converter = new DateConverterForJodaLocalDate(settings, -1);
        final LocalDate dt = converter.convertToObject("2013-05-11", null);
        assertThat(dt, is(new LocalDate(2013, 05, 12)));

        final String str = converter.convertToString(dt, null);
        assertThat(str, is("2013-05-11"));
    }

}
