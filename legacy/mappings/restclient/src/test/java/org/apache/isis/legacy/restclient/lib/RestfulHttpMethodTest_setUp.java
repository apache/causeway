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
package org.apache.isis.legacy.restclient.lib;

import java.io.UnsupportedEncodingException;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.legacy.restclient.lib.ClientRequestConfigurer;
import org.apache.isis.legacy.restclient.lib.RestfulHttpMethodHelper;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;

public class RestfulHttpMethodTest_setUp {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private ClientRequestConfigurer requestConfigurer;

    private JsonRepresentation repr;

    @Before
    public void setUp() throws Exception {
        repr = JsonRepresentation.newMap();
        repr.mapPut("aString", "bar");
        repr.mapPut("anInt", 3);
        repr.mapPut("aLong", 31231231L);
        repr.mapPut("aBoolean", true);
        repr.mapPut("aStringRequiringEncoding", "http://localhost:8080/somewhere");
    }

    @Test
    public void get() throws Exception {
        setsUpQueryString(RestfulHttpMethodHelper.GET);
    }

    @Test
    public void delete() throws Exception {
        setsUpQueryString(RestfulHttpMethodHelper.DELETE);
    }

    @Test
    public void post() throws Exception {
        setsUpBody(RestfulHttpMethodHelper.POST);
    }

    @Test
    public void put() throws Exception {
        setsUpBody(RestfulHttpMethodHelper.PUT);
    }

    private void setsUpQueryString(final RestfulHttpMethodHelper httpMethod) throws UnsupportedEncodingException {
        context.checking(new Expectations() {
            {
                oneOf(requestConfigurer).setHttpMethod(httpMethod);
                oneOf(requestConfigurer).queryString(repr);
            }
        });

        httpMethod.setUpArgs(requestConfigurer, repr);
    }

    private void setsUpBody(final RestfulHttpMethodHelper httpMethod) throws UnsupportedEncodingException {
        context.checking(new Expectations() {
            {
                oneOf(requestConfigurer).setHttpMethod(httpMethod);
                oneOf(requestConfigurer).body(repr);
            }
        });

        httpMethod.setUpArgs(requestConfigurer, repr);
    }

}
