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

import java.io.UnsupportedEncodingException;

import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.viewer.json.applib.ClientRequestConfigurer;
import org.apache.isis.viewer.json.applib.HttpMethod2;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class HttpMethodTest_setUp {

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
        setsUpQueryString(HttpMethod2.GET);
    }

    @Test
    public void delete() throws Exception {
        setsUpQueryString(HttpMethod2.DELETE);
    }

    @Test
    public void post() throws Exception {
        setsUpBody(HttpMethod2.POST);
    }

    @Test
    public void put() throws Exception {
        setsUpBody(HttpMethod2.PUT);
    }


    private void setsUpQueryString(final HttpMethod2 httpMethod2) throws UnsupportedEncodingException {
        context.checking(new Expectations() {
            {
                one(requestConfigurer).setHttpMethod(httpMethod2);
                one(requestConfigurer).queryString(repr);
            }
        });
        
        httpMethod2.setUpArgs(requestConfigurer, repr);
    }

    private void setsUpBody(final HttpMethod2 httpMethod2) throws UnsupportedEncodingException {
        context.checking(new Expectations() {
            {
                one(requestConfigurer).setHttpMethod(httpMethod2);
                one(requestConfigurer).body(repr);
            }
        });
        
        httpMethod2.setUpArgs(requestConfigurer, repr);
    }

}
