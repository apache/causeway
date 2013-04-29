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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;

@RunWith(JMock.class)
public class JsonValueEncoderTest_asObject {

    private final Mockery context = new JUnit4Mockery();

    private JsonRepresentation representation;
    private ObjectAdapter objectAdapter;
    private ObjectSpecification objectSpec;

    private EncodableFacet encodableFacet;
    private Object encoded;

    @Before
    public void setUp() throws Exception {
        objectAdapter = context.mock(ObjectAdapter.class);
        objectSpec = context.mock(ObjectSpecification.class);

        context.checking(new Expectations() {
            {
                allowing(objectAdapter).getSpecification();
                will(returnValue(objectSpec));
            }
        });
        encodableFacet = context.mock(EncodableFacet.class);

        encoded = new Object();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenAdapterIsNull() throws Exception {
        JsonValueEncoder.asObject(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenObjectAdapterIsNotSpecialCaseAndSpecIsNotEncodable() throws Exception {
        allowingObjectSpecCorrespondingClassIs(String.class);
        allowingObjectSpecHas(EncodableFacet.class, null);
        JsonValueEncoder.asObject(objectAdapter);
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
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        never(encodableFacet);
        context.checking(new Expectations() {
            {
                one(objectAdapter).getObject();
                will(returnValue(true));
            }
        });
        assertEquals(true, JsonValueEncoder.asObject(objectAdapter));
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
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        never(encodableFacet);
        context.checking(new Expectations() {
            {
                one(objectAdapter).getObject();
                will(returnValue(123));
            }
        });
        assertEquals(123, JsonValueEncoder.asObject(objectAdapter));
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
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        never(encodableFacet);
        context.checking(new Expectations() {
            {
                one(objectAdapter).getObject();
                will(returnValue(123456789L));
            }
        });
        assertEquals(123456789L, JsonValueEncoder.asObject(objectAdapter));
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
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        never(encodableFacet);
        context.checking(new Expectations() {
            {
                one(objectAdapter).getObject();
                will(returnValue(12345.6789));
            }
        });
        assertEquals(12345.6789, JsonValueEncoder.asObject(objectAdapter));
    }

    @Test
    public void whenBigInteger() throws Exception {
        allowingObjectSpecCorrespondingClassIs(BigInteger.class);
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        never(encodableFacet);
        final BigInteger value = new BigInteger("123456789012345");
        context.checking(new Expectations() {

            {
                one(objectAdapter).getObject();
                will(returnValue(value));
            }
        });
        assertEquals(value, JsonValueEncoder.asObject(objectAdapter));
    }

    @Test
    public void whenBigDecimal() throws Exception {
        allowingObjectSpecCorrespondingClassIs(BigDecimal.class);
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        never(encodableFacet);
        final BigDecimal value = new BigDecimal("1234567890.1234567890");
        context.checking(new Expectations() {

            {
                oneOf(objectAdapter).getObject();
                will(returnValue(value));
            }
        });
        assertEquals(value, JsonValueEncoder.asObject(objectAdapter));
    }

    @Test
    public void whenString() throws Exception {
        allowingObjectSpecCorrespondingClassIs(String.class);
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        context.checking(new Expectations() {
            {
                one(encodableFacet).toEncodedString(objectAdapter);
                will(returnValue("encodedString"));
            }
        });
        assertSame("encodedString", JsonValueEncoder.asObject(objectAdapter));
    }

    private void allowingObjectSpecCorrespondingClassIs(final Class<?> result) {
        context.checking(new Expectations() {
            {
                allowing(objectSpec).getCorrespondingClass();
                will(returnValue(result));
            }
        });
        context.checking(new Expectations() {
            {
                allowing(objectSpec).getSpecId();
                will(returnValue(new ObjectSpecId(result.getName())));
            }
        });
    }

    private <T extends Facet> void allowingObjectSpecHas(final Class<T> facetClass, final T encodableFacet) {
        context.checking(new Expectations() {
            {
                allowing(objectSpec).getFacet(facetClass);
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
