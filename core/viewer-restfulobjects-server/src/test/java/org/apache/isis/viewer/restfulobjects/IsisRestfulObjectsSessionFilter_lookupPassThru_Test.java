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
package org.apache.isis.viewer.restfulobjects;

import static org.hamcrest.CoreMatchers.is;

import java.util.List;

import javax.servlet.FilterConfig;

import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.viewer.restfulobjects.IsisRestfulObjectsSessionFilter;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class IsisRestfulObjectsSessionFilter_lookupPassThru_Test {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    IsisRestfulObjectsSessionFilter isisSessionFilter;

    @Mock
    FilterConfig mockFilterConfig;

    @Before
    public void setUp() throws Exception {
        isisSessionFilter = new IsisRestfulObjectsSessionFilter();
    }

    @Test
    public void when_null() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockFilterConfig).getInitParameter(IsisRestfulObjectsSessionFilter.PASS_THRU_KEY);
            will(returnValue(null));
        }});

        final List<String> x = isisSessionFilter.lookupAndParsePassThru(mockFilterConfig);
        Assert.assertThat(x.size(), is(0));
    }

    @Test
    public void when_none() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockFilterConfig).getInitParameter(IsisRestfulObjectsSessionFilter.PASS_THRU_KEY);
            will(returnValue(""));
        }});

        final List<String> x = isisSessionFilter.lookupAndParsePassThru(mockFilterConfig);
        Assert.assertThat(x.size(), is(0));
    }

    @Test
    public void when_one() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockFilterConfig).getInitParameter(IsisRestfulObjectsSessionFilter.PASS_THRU_KEY);
            will(returnValue("/abc"));
        }});

        final List<String> x = isisSessionFilter.lookupAndParsePassThru(mockFilterConfig);
        Assert.assertThat(x.size(), is(1));
        Assert.assertThat(x.get(0), is("/abc"));
    }

    @Test
    public void when_several() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockFilterConfig).getInitParameter(IsisRestfulObjectsSessionFilter.PASS_THRU_KEY);
            will(returnValue("/abc,/def"));
        }});

        final List<String> x = isisSessionFilter.lookupAndParsePassThru(mockFilterConfig);
        Assert.assertThat(x.size(), is(2));
        Assert.assertThat(x.get(0), is("/abc"));
        Assert.assertThat(x.get(1), is("/def"));
    }

}