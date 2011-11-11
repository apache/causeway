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
package org.apache.isis.viewer.json.applib;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.isis.viewer.json.applib.RestfulRequest.RequestParameter;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

public class RestfulRequestQueryParameterTest_valueOf {

    private Map<String,String[]> parameterMap;

    @Before
    public void setUp() throws Exception {
        parameterMap = Maps.newHashMap();
    }
    
    @Test
    public void mapContainsList() {
        final RequestParameter<List<List<String>>> queryParameter = RestfulRequest.RequestParameter.FOLLOW_LINKS;
        parameterMap.put("x-ro-follow-links", new String[]{"a", "b.c"});
        List<List<String>> valueOf = queryParameter.valueOf(parameterMap);
        
        assertThat(valueOf.size(), is(2));
        assertThat(valueOf.get(0).size(), is(1));
        assertThat(valueOf.get(0).get(0), is("a"));
        assertThat(valueOf.get(1).size(), is(2));
        assertThat(valueOf.get(1).get(0), is("b"));
        assertThat(valueOf.get(1).get(1), is("c"));
    }

    @Test
    public void mapHasNoKey() {
        final RequestParameter<List<List<String>>> queryParameter = RestfulRequest.RequestParameter.FOLLOW_LINKS;
        parameterMap.put("something-else", new String[]{"a", "b.c"});
        List<List<String>> valueOf = queryParameter.valueOf(parameterMap);
        
        assertThat(valueOf.size(), is(0));
    }

    @Test
    public void mapIsEmpty() {
        final RequestParameter<List<List<String>>> queryParameter = RestfulRequest.RequestParameter.FOLLOW_LINKS;
        List<List<String>> valueOf = queryParameter.valueOf(parameterMap);
        
        assertThat(valueOf.size(), is(0));
    }

    @Test
    public void mapIsNull() {
        final RequestParameter<List<List<String>>> queryParameter = RestfulRequest.RequestParameter.FOLLOW_LINKS;
        List<List<String>> valueOf = queryParameter.valueOf(null);
        
        assertThat(valueOf.size(), is(0));
    }

    @Test
    public void mapContainsCommaSeparatedList() {
        
        final RequestParameter<List<List<String>>> queryParameter = RestfulRequest.RequestParameter.FOLLOW_LINKS;
        parameterMap.put("x-ro-follow-links", new String[]{"a,b.c"});
        List<List<String>> valueOf = queryParameter.valueOf(parameterMap);
        
        assertThat(valueOf.size(), is(2));
        assertThat(valueOf.get(0).size(), is(1));
        assertThat(valueOf.get(0).get(0), is("a"));
        assertThat(valueOf.get(1).size(), is(2));
        assertThat(valueOf.get(1).get(0), is("b"));
        assertThat(valueOf.get(1).get(1), is("c"));
    }

    @Test
    public void commaSeparatedListUrlEncoded() throws UnsupportedEncodingException {
        
        final RequestParameter<List<List<String>>> queryParameter = RestfulRequest.RequestParameter.FOLLOW_LINKS;
        parameterMap.put("x-ro-follow-links", new String[]{URLEncoder.encode("a,b.c", "UTF-8")});
        List<List<String>> valueOf = queryParameter.valueOf(parameterMap);
        
        assertThat(valueOf.size(), is(2));
        assertThat(valueOf.get(0).size(), is(1));
        assertThat(valueOf.get(0).get(0), is("a"));
        assertThat(valueOf.get(1).size(), is(2));
        assertThat(valueOf.get(1).get(0), is("b"));
        assertThat(valueOf.get(1).get(1), is("c"));
    }



}
