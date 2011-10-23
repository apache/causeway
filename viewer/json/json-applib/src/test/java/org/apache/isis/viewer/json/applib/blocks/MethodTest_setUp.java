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
package org.apache.isis.viewer.json.applib.blocks;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.jboss.resteasy.client.ClientRequest;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class MethodTest_setUp {

    private Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private ClientRequest request;
    private MultivaluedMap<String,String> requestQueryArgs;

    private JsonRepresentation emptyRepr;

    private JsonRepresentation nonEmptyRepr;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        emptyRepr = JsonRepresentation.newMap();

        nonEmptyRepr = JsonRepresentation.newMap();
        nonEmptyRepr.mapPut("aString", "bar"); 
        nonEmptyRepr.mapPut("anInt", 3); 
        nonEmptyRepr.mapPut("aLong", 31231231L); 
        nonEmptyRepr.mapPut("aBoolean", true); 
        nonEmptyRepr.mapPut("aStringRequiringEncoding", "http://localhost:8080/somewhere");

        request = context.mock(ClientRequest.class);
        requestQueryArgs = context.mock(MultivaluedMap.class);      

        context.checking(new Expectations() {
            {
                allowing(request).getQueryParameters();
                will(returnValue(requestQueryArgs));
            }
        });

    }
    
    @Test
    public void GET_empty() throws Exception {
        emptyQueryArgs(Method.GET);
    }

    @Test
    public void DELETE_empty() throws Exception {
        emptyQueryArgs(Method.DELETE);
    }

    @Test
    public void POST_empty() throws Exception {
        emptyBody(Method.POST);
    }

    @Test
    public void PUT_empty() throws Exception {
        emptyBody(Method.PUT);
    }


    @Test
    public void GET_nonEmpty() throws Exception {
        nonEmptyQueryArgs(Method.GET);
    }

    @Test
    public void DELETE_nonEmpty() throws Exception {
        nonEmptyQueryArgs(Method.DELETE);
    }

    @Test
    public void POST_nonEmpty() throws Exception {
        nonEmptyBody(Method.POST);
    }

    @Test
    public void PUT_nonEmpty() throws Exception {
        nonEmptyBody(Method.PUT);
    }


    private void emptyQueryArgs(final Method method) {
        context.checking(new Expectations() {
            {
                one(request).setHttpMethod(method.name());
                never(requestQueryArgs);
            }
        });
        
        method.setUp(request, emptyRepr);
    }

    private void emptyBody(final Method method) {
        context.checking(new Expectations() {
            {
                one(request).setHttpMethod(method.name());
                one(request).body(MediaType.APPLICATION_JSON_TYPE, emptyRepr.toString());
            }
        });
        
        method.setUp(request, emptyRepr);
    }


    private void nonEmptyQueryArgs(final Method method) throws UnsupportedEncodingException {
        context.checking(new Expectations() {
            {
                one(request).setHttpMethod(method.name());
                one(requestQueryArgs).add("aString", "bar");
                one(requestQueryArgs).add("anInt", "3");
                one(requestQueryArgs).add("aLong", "31231231");
                one(requestQueryArgs).add("aBoolean", "true");
                one(requestQueryArgs).add("aStringRequiringEncoding", URLEncoder.encode("http://localhost:8080/somewhere", "UTF-8"));
            }
        });
        
        method.setUp(request, nonEmptyRepr);
    }

    private void nonEmptyBody(final Method method) throws UnsupportedEncodingException {
        final String expected = nonEmptyRepr.toString();
        context.checking(new Expectations() {
            {
                one(request).setHttpMethod(method.name());
                one(request).body(MediaType.APPLICATION_JSON_TYPE, expected);
            }
        });
        
        method.setUp(request, nonEmptyRepr);
    }


}
