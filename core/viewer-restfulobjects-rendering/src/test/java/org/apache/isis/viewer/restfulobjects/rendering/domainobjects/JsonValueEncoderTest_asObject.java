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
package org.apache.isis.viewer.restfulobjects.rendering.domainobjects;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(JMock.class)
public class JsonValueEncoderTest_asObject {

    private final Mockery context = new JUnit4Mockery();

    private ObjectAdapter mockObjectAdapter;
    private ObjectSpecification mockObjectSpec;
    private EncodableFacet mockEncodableFacet;
    private ObjectAdapterProvider mockAdapterManager;

    private Object encoded;

    private JsonRepresentation representation;

    @Before
    public void setUp() throws Exception {
        mockObjectAdapter = context.mock(ObjectAdapter.class);
        mockObjectSpec = context.mock(ObjectSpecification.class);

        context.checking(new Expectations() {
            {
                allowing(mockObjectAdapter).getSpecification();
                will(returnValue(mockObjectSpec));
            }
        });
        mockEncodableFacet = context.mock(EncodableFacet.class);
        mockAdapterManager = context.mock(ObjectAdapterProvider.class);

        JsonValueEncoder.testSetAdapterManager(mockAdapterManager::adapterFor);
        encoded = new Object();
    }

    @After
    public void tearDown() throws Exception {
        JsonValueEncoder.testSetAdapterManager(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenAdapterIsNull() throws Exception {
        JsonValueEncoder.asObject(null, null);
    }

    @Test
    public void whenBooleanPrimitive() throws Exception {
        whenBoolean(boolean.class);
    }

    @Test
    public void whenBooleanWrapper() throws Exception {
        whenBoolean(Boolean.class);
    }

    private void whenBoolean(final Class<?> cls) {
        allowingObjectSpecCorrespondingClassIs(cls);
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        never(mockEncodableFacet);
        context.checking(new Expectations() {
            {
            	oneOf(mockObjectAdapter).getPojo();
                will(returnValue(true));
            }
        });
        assertEquals(true, JsonValueEncoder.asObject(mockObjectAdapter, null));
    }

    @Test
    public void whenIntegerPrimitive() throws Exception {
        whenInteger(int.class);
    }

    @Test
    public void whenIntegerWrapper() throws Exception {
        whenInteger(Integer.class);
    }

    private void whenInteger(final Class<?> cls) {
        allowingObjectSpecCorrespondingClassIs(cls);
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        never(mockEncodableFacet);
        context.checking(new Expectations() {
            {
            	oneOf(mockObjectAdapter).getPojo();
                will(returnValue(123));
            }
        });
        assertEquals(123, JsonValueEncoder.asObject(mockObjectAdapter, null));
    }

    @Test
    public void whenLongPrimitive() throws Exception {
        whenLong(long.class);
    }

    @Test
    public void whenLongWrapper() throws Exception {
        whenLong(Long.class);
    }

    private void whenLong(final Class<?> cls) {
        allowingObjectSpecCorrespondingClassIs(cls);
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        never(mockEncodableFacet);
        context.checking(new Expectations() {
            {
            	oneOf(mockObjectAdapter).getPojo();
                will(returnValue(123456789L));
            }
        });
        assertEquals(123456789L, JsonValueEncoder.asObject(mockObjectAdapter, null));
    }

    @Test
    public void whenDoublePrimitive() throws Exception {
        whenDouble(double.class);
    }

    @Test
    public void whenDoubleWrapper() throws Exception {
        whenDouble(Double.class);
    }

    private void whenDouble(final Class<?> cls) {
        allowingObjectSpecCorrespondingClassIs(cls);
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        never(mockEncodableFacet);
        context.checking(new Expectations() {
            {
            	oneOf(mockObjectAdapter).getPojo();
                will(returnValue(12345.6789));
            }
        });
        assertEquals(12345.6789, JsonValueEncoder.asObject(mockObjectAdapter, null));
    }

    @Test
    public void whenBigInteger() throws Exception {
        allowingObjectSpecCorrespondingClassIs(BigInteger.class);
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        never(mockEncodableFacet);
        final BigInteger value = new BigInteger("123456789012345");
        context.checking(new Expectations() {

            {
            	oneOf(mockObjectAdapter).getPojo();
                will(returnValue(value));
            }
        });
        assertEquals(value, JsonValueEncoder.asObject(mockObjectAdapter, null));
    }

    @Test
    public void whenBigDecimal() throws Exception {
        allowingObjectSpecCorrespondingClassIs(BigDecimal.class);
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        never(mockEncodableFacet);
        final BigDecimal value = new BigDecimal("1234567890.1234567890");
        context.checking(new Expectations() {

            {
                oneOf(mockObjectAdapter).getPojo();
                will(returnValue(value));
            }
        });
        assertEquals(value, JsonValueEncoder.asObject(mockObjectAdapter, null));
    }

    @Test
    public void whenString() throws Exception {
        allowingObjectSpecCorrespondingClassIs(String.class);
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        context.checking(new Expectations() {
            {
                oneOf(mockObjectAdapter).getPojo();
                will(returnValue("encodedString"));
            }
        });
        final Object actual = JsonValueEncoder.asObject(mockObjectAdapter, null);
        assertSame("encodedString", actual);
    }

    private void allowingObjectSpecCorrespondingClassIs(final Class<?> result) {
        context.checking(new Expectations() {
            {
                allowing(mockObjectSpec).getCorrespondingClass();
                will(returnValue(result));
            }
        });
        context.checking(new Expectations() {
            {
                allowing(mockObjectSpec).getSpecId();
                will(returnValue(ObjectSpecId.of(result.getName())));
            }
        });
    }

    private <T extends Facet> void allowingObjectSpecHas(final Class<T> facetClass, final T encodableFacet) {
        context.checking(new Expectations() {
            {
                allowing(mockObjectSpec).getFacet(facetClass);
                will(returnValue(encodableFacet));
            }
        });
    }

    private void never(final EncodableFacet encodableFacet2) {
        context.checking(new Expectations() {
            {
                never(encodableFacet2);
            }
        });
    }

}
