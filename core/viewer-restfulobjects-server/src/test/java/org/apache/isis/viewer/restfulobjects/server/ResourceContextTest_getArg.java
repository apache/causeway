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
package org.apache.isis.viewer.restfulobjects.server;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Maps;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulRequest.RequestParameter;
import org.apache.isis.viewer.restfulobjects.applib.util.UrlEncodingUtils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ResourceContextTest_getArg {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private ResourceContext resourceContext;


    @Test
    public void whenArgExists() throws Exception {
        final String queryString = UrlEncodingUtils.urlEncode(JsonRepresentation.newMap("x-ro-page", "123").asJsonNode());
        givenServletRequestQueryString(queryString);
        givenServletRequestParameterMapEmpty();

        resourceContext = new ResourceContext(null, null, null, null, null, null, null, (String)null, httpServletRequest, null, null,
                null) {
            @Override
            void init(final RepresentationType representationType) {
                //
            }
        };
        final Integer arg = resourceContext.getArg(RequestParameter.PAGE);
        assertThat(arg, equalTo(123));
    }

    @Test
    public void whenArgDoesNotExist() throws Exception {
        final String queryString = UrlEncodingUtils.urlEncode(JsonRepresentation.newMap("xxx", "123").asJsonNode());
        givenServletRequestQueryString(queryString);
        givenServletRequestParameterMapEmpty();

        resourceContext = new ResourceContext(null, null, null, null, null, null, null, (String)null, httpServletRequest, null, null,
                null) {
            @Override
            void init(final RepresentationType representationType) {
                //
            }
        };
        final Integer arg = resourceContext.getArg(RequestParameter.PAGE);
        assertThat(arg, equalTo(RequestParameter.PAGE.getDefault()));
    }

    private void givenServletRequestQueryString(final String queryString) {
        context.checking(new Expectations() {
            {
                one(httpServletRequest).getQueryString();
                will(returnValue(queryString));
            }
        });
    }


    private void givenServletRequestParameterMapEmpty() {
        final HashMap<Object, Object> parameterMap = Maps.newHashMap();
        context.checking(new Expectations() {
            {
                oneOf(httpServletRequest).getParameterMap();
                will(returnValue(parameterMap));
            }
        });
    }

}
