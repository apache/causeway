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
package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.codehaus.jackson.node.BigIntegerNode;
import org.codehaus.jackson.node.BooleanNode;
import org.codehaus.jackson.node.DecimalNode;
import org.codehaus.jackson.node.DoubleNode;
import org.codehaus.jackson.node.IntNode;
import org.codehaus.jackson.node.LongNode;
import org.codehaus.jackson.node.TextNode;
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
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.json.applib.JsonRepresentation;

@RunWith(JMock.class)
public class JsonValueEncoderTest_asAdapter {

    private final Mockery context = new JUnit4Mockery();

    private JsonValueEncoder jsonValueEncoder;
    private JsonRepresentation representation;
    private ObjectSpecification objectSpec;

    private EncodableFacet encodableFacet;
    private ObjectAdapter objectAdapter;

    @Before
    public void setUp() throws Exception {
        objectSpec = context.mock(ObjectSpecification.class);
        encodableFacet = context.mock(EncodableFacet.class);
        objectAdapter = context.mock(ObjectAdapter.class);

        representation = new JsonRepresentation(TextNode.valueOf("aString"));
        jsonValueEncoder = new JsonValueEncoder();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenSpecIsNull() throws Exception {
        jsonValueEncoder.asAdapter(null, representation);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenReprIsNull() throws Exception {
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        jsonValueEncoder.asAdapter(objectSpec, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenReprIsAnArray() throws Exception {
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        jsonValueEncoder.asAdapter(objectSpec, JsonRepresentation.newArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenReprIsAMap() throws Exception {
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        assertNull(jsonValueEncoder.asAdapter(objectSpec, JsonRepresentation.newMap()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenSpecDoesNotHaveAnEncodableFacet() throws Exception {
        allowingObjectSpecHas(EncodableFacet.class, null);

        assertNull(jsonValueEncoder.asAdapter(objectSpec, representation));
    }

    @Test
    public void whenReprIsBooleanPrimitive() throws Exception {
        whenReprIsBoolean(boolean.class);
    }

    @Test
    public void whenReprIsBooleanWrapper() throws Exception {
        whenReprIsBoolean(Boolean.class);
    }

    private void whenReprIsBoolean(final Class<?> correspondingClass) {
        // given
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        allowingObjectSpecCorrespondingClassIs(correspondingClass);
        final boolean value = true;
        representation = new JsonRepresentation(BooleanNode.valueOf(value));
        context.checking(new Expectations() {
            {
                one(encodableFacet).fromEncodedString("" + value);
                will(returnValue(objectAdapter));
            }
        });

        // when
        final ObjectAdapter adapter = jsonValueEncoder.asAdapter(objectSpec, representation);

        // then
        assertSame(objectAdapter, adapter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenObjectSpecIsBooleanButReprIsNot() throws Exception {
        // given
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        allowingObjectSpecCorrespondingClassIs(boolean.class);

        // when
        jsonValueEncoder.asAdapter(objectSpec, representation);
    }

    @Test
    public void whenReprIsIntegerPrimitive() throws Exception {
        whenReprIsInteger(int.class);
    }

    @Test
    public void whenReprIsIntegerWrapper() throws Exception {
        whenReprIsInteger(Integer.class);
    }

    private void whenReprIsInteger(final Class<?> correspondingClass) {
        // given
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        allowingObjectSpecCorrespondingClassIs(correspondingClass);
        final int value = 123;
        representation = new JsonRepresentation(IntNode.valueOf(value));
        context.checking(new Expectations() {
            {
                one(encodableFacet).fromEncodedString("" + value);
                will(returnValue(objectAdapter));
            }
        });

        // when
        final ObjectAdapter adapter = jsonValueEncoder.asAdapter(objectSpec, representation);

        // then
        assertSame(objectAdapter, adapter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenObjectSpecIsIntegerButReprIsNot() throws Exception {
        // given
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        allowingObjectSpecCorrespondingClassIs(int.class);

        representation = JsonRepresentation.newMap("foo", "bar");

        // when
        jsonValueEncoder.asAdapter(objectSpec, representation);
    }

    @Test
    public void whenReprIsLongPrimitive() throws Exception {
        whenReprIsLong(long.class);
    }

    @Test
    public void whenReprIsLongWrapper() throws Exception {
        whenReprIsLong(Long.class);
    }

    private void whenReprIsLong(final Class<?> correspondingClass) {
        // given
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        allowingObjectSpecCorrespondingClassIs(correspondingClass);
        final long value = 1234567890L;
        representation = new JsonRepresentation(LongNode.valueOf(value));
        context.checking(new Expectations() {
            {
                one(encodableFacet).fromEncodedString("" + value);
                will(returnValue(objectAdapter));
            }
        });

        // when
        final ObjectAdapter adapter = jsonValueEncoder.asAdapter(objectSpec, representation);

        // then
        assertSame(objectAdapter, adapter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenObjectSpecIsLongButReprIsNot() throws Exception {
        // given
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        allowingObjectSpecCorrespondingClassIs(long.class);

        // when
        jsonValueEncoder.asAdapter(objectSpec, representation);
    }

    @Test
    public void whenReprIsDoublePrimitive() throws Exception {
        whenReprIsDouble(double.class);
    }

    @Test
    public void whenReprIsDoubleWrapper() throws Exception {
        whenReprIsDouble(Double.class);
    }

    private void whenReprIsDouble(final Class<?> correspondingClass) {
        // given
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        allowingObjectSpecCorrespondingClassIs(correspondingClass);
        final double value = 123.45;
        representation = new JsonRepresentation(DoubleNode.valueOf(value));
        context.checking(new Expectations() {
            {
                one(encodableFacet).fromEncodedString("" + value);
                will(returnValue(objectAdapter));
            }
        });

        // when
        final ObjectAdapter adapter = jsonValueEncoder.asAdapter(objectSpec, representation);

        // then
        assertSame(objectAdapter, adapter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenObjectSpecIsDoubleButReprIsNot() throws Exception {
        // given
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        allowingObjectSpecCorrespondingClassIs(double.class);

        representation = JsonRepresentation.newMap("foo", "bar");

        // when
        jsonValueEncoder.asAdapter(objectSpec, representation);
    }

    @Test
    public void whenReprIsBigInteger() throws Exception {
        // given
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        allowingObjectSpecCorrespondingClassIs(BigInteger.class);
        final BigInteger value = BigInteger.valueOf(123);
        representation = new JsonRepresentation(BigIntegerNode.valueOf(value));
        context.checking(new Expectations() {
            {
                one(encodableFacet).fromEncodedString("" + value);
                will(returnValue(objectAdapter));
            }
        });

        // when
        final ObjectAdapter adapter = jsonValueEncoder.asAdapter(objectSpec, representation);

        // then
        assertSame(objectAdapter, adapter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenObjectSpecIsBigIntegerButReprIsNot() throws Exception {
        // given
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        allowingObjectSpecCorrespondingClassIs(BigInteger.class);

        representation = JsonRepresentation.newMap("foo", "bar");

        // when
        jsonValueEncoder.asAdapter(objectSpec, representation);
    }

    @Test
    public void whenReprIsBigDecimal() throws Exception {
        // given
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        allowingObjectSpecCorrespondingClassIs(BigDecimal.class);
        final BigDecimal value = new BigDecimal("123234234.45612312343535");
        representation = new JsonRepresentation(DecimalNode.valueOf(value));
        context.checking(new Expectations() {
            {
                one(encodableFacet).fromEncodedString("" + value);
                will(returnValue(objectAdapter));
            }
        });

        // when
        final ObjectAdapter adapter = jsonValueEncoder.asAdapter(objectSpec, representation);

        // then
        assertSame(objectAdapter, adapter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenObjectSpecIsBigDecimalButReprIsNot() throws Exception {
        // given
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        allowingObjectSpecCorrespondingClassIs(BigDecimal.class);

        representation = JsonRepresentation.newMap("foo", "bar");

        // when
        jsonValueEncoder.asAdapter(objectSpec, representation);
    }

    @Test
    public void whenReprIsString() throws Exception {
        // given
        allowingObjectSpecHas(EncodableFacet.class, encodableFacet);
        allowingObjectSpecCorrespondingClassIs(String.class);
        representation = new JsonRepresentation(TextNode.valueOf("aString"));

        context.checking(new Expectations() {
            {
                one(encodableFacet).fromEncodedString("aString");
                will(returnValue(objectAdapter));
            }
        });

        // when
        final ObjectAdapter adapter = jsonValueEncoder.asAdapter(objectSpec, representation);

        // then
        assertSame(objectAdapter, adapter);
    }

    private <T extends Facet> void allowingObjectSpecHas(final Class<T> facetClass, final T encodableFacet) {
        context.checking(new Expectations() {
            {
                allowing(objectSpec).getFacet(facetClass);
                will(returnValue(encodableFacet));
            }
        });
    }

    private void allowingObjectSpecCorrespondingClassIs(final Class<?> result) {
        context.checking(new Expectations() {
            {
                allowing(objectSpec).getCorrespondingClass();
                will(returnValue(result));
            }
        });
    }

}
