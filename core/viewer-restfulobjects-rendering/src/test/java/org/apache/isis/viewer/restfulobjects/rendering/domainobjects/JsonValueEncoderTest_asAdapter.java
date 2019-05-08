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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;

@RunWith(JMock.class)
public class JsonValueEncoderTest_asAdapter {

    private final Mockery context = new JUnit4Mockery();

    private JsonRepresentation representation;
    private ObjectSpecification mockObjectSpec;
    private EncodableFacet mockEncodableFacet;
    private ObjectAdapter mockObjectAdapter;

    private ObjectAdapterProvider mockAdapterManager;

    @Before
    public void setUp() throws Exception {
        mockObjectSpec = context.mock(ObjectSpecification.class);
        mockEncodableFacet = context.mock(EncodableFacet.class);
        mockObjectAdapter = context.mock(ObjectAdapter.class);
        mockAdapterManager = context.mock(ObjectAdapterProvider.class);

        JsonValueEncoder.testSetAdapterManager(mockAdapterManager::adapterFor);

        representation = new JsonRepresentation(TextNode.valueOf("aString"));
    }

    @After
    public void tearDown() throws Exception {
        JsonValueEncoder.testSetAdapterManager(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void whenSpecIsNull() throws Exception {
        JsonValueEncoder.asAdapter(null, representation, null);
    }

    @Test
    public void whenReprIsNull() throws Exception {
        assertThat(JsonValueEncoder.asAdapter(mockObjectSpec, null, null), is(Matchers.nullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenReprIsAnArray() throws Exception {
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        JsonValueEncoder.asAdapter(mockObjectSpec, JsonRepresentation.newArray(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenReprIsAMap() throws Exception {
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        assertNull(JsonValueEncoder.asAdapter(mockObjectSpec, JsonRepresentation.newMap(), null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenSpecDoesNotHaveAnEncodableFacet() throws Exception {
        allowingObjectSpecHas(EncodableFacet.class, null);

        assertNull(JsonValueEncoder.asAdapter(mockObjectSpec, representation, null));
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
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        allowingObjectSpecCorrespondingClassAndSpecIdIs(correspondingClass);
        final boolean value = true;
        representation = new JsonRepresentation(BooleanNode.valueOf(value));
        context.checking(new Expectations() {
            {
                oneOf(mockAdapterManager).adapterFor(value);
                will(returnValue(mockObjectAdapter));
            }
        });

        // when
        final ObjectAdapter adapter = JsonValueEncoder.asAdapter(mockObjectSpec, representation, null);

        // then
        assertSame(mockObjectAdapter, adapter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenObjectSpecIsBooleanButReprIsNot() throws Exception {
        // given
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        allowingObjectSpecCorrespondingClassAndSpecIdIs(boolean.class);

        context.checking(new Expectations() {
            {
            	oneOf(mockEncodableFacet).fromEncodedString("aString");
                will(throwException(new TextEntryParseException("'aString' cannot be parsed as a boolean value")));
            }
        });

        // when
        JsonValueEncoder.asAdapter(mockObjectSpec, representation, null);
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
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        allowingObjectSpecCorrespondingClassAndSpecIdIs(correspondingClass);
        final int value = 123;
        representation = new JsonRepresentation(IntNode.valueOf(value));
        context.checking(new Expectations() {
            {
                oneOf(mockAdapterManager).adapterFor(value);
                will(returnValue(mockObjectAdapter));
            }
        });

        // when
        final ObjectAdapter adapter = JsonValueEncoder.asAdapter(mockObjectSpec, representation, null);

        // then
        assertSame(mockObjectAdapter, adapter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenObjectSpecIsIntegerButReprIsNot() throws Exception {
        // given
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        allowingObjectSpecCorrespondingClassAndSpecIdIs(int.class);

        representation = JsonRepresentation.newMap("foo", "bar");

        // when
        JsonValueEncoder.asAdapter(mockObjectSpec, representation, null);
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
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        allowingObjectSpecCorrespondingClassAndSpecIdIs(correspondingClass);
        final long value = 1234567890L;
        representation = new JsonRepresentation(LongNode.valueOf(value));
        context.checking(new Expectations() {
            {
                oneOf(mockAdapterManager).adapterFor(value);
                will(returnValue(mockObjectAdapter));
            }
        });

        // when
        final ObjectAdapter adapter = JsonValueEncoder.asAdapter(mockObjectSpec, representation, null);

        // then
        assertSame(mockObjectAdapter, adapter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenObjectSpecIsLongButReprIsNot() throws Exception {
        // given
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        allowingObjectSpecCorrespondingClassAndSpecIdIs(long.class);

        context.checking(new Expectations() {
            {
            	oneOf(mockEncodableFacet).fromEncodedString("aString");
                will(throwException(new TextEntryParseException("'aString' cannot be parsed as a long value")));
            }
        });

        // when
        JsonValueEncoder.asAdapter(mockObjectSpec, representation, null);
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
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        allowingObjectSpecCorrespondingClassAndSpecIdIs(correspondingClass);
        final double value = 123.45;
        representation = new JsonRepresentation(DoubleNode.valueOf(value));
        context.checking(new Expectations() {
            {
                oneOf(mockAdapterManager).adapterFor(value);
                will(returnValue(mockObjectAdapter));
            }
        });

        // when
        final ObjectAdapter adapter = JsonValueEncoder.asAdapter(mockObjectSpec, representation, null);

        // then
        assertSame(mockObjectAdapter, adapter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenObjectSpecIsDoubleButReprIsNot() throws Exception {
        // given
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        allowingObjectSpecCorrespondingClassAndSpecIdIs(double.class);

        representation = JsonRepresentation.newMap("foo", "bar");

        // when
        JsonValueEncoder.asAdapter(mockObjectSpec, representation, null);
    }

    @Test
    public void whenReprIsBigInteger() throws Exception {
        // given
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        allowingObjectSpecCorrespondingClassAndSpecIdIs(BigInteger.class);
        final BigInteger value = BigInteger.valueOf(123);
        representation = new JsonRepresentation(BigIntegerNode.valueOf(value));
        context.checking(new Expectations() {
            {
                oneOf(mockAdapterManager).adapterFor(value);
                will(returnValue(mockObjectAdapter));
            }
        });

        // when
        final ObjectAdapter adapter = JsonValueEncoder.asAdapter(mockObjectSpec, representation, null);

        // then
        assertSame(mockObjectAdapter, adapter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenObjectSpecIsBigIntegerButReprIsNot() throws Exception {
        // given
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        allowingObjectSpecCorrespondingClassAndSpecIdIs(BigInteger.class);

        representation = JsonRepresentation.newMap("foo", "bar");

        // when
        JsonValueEncoder.asAdapter(mockObjectSpec, representation, null);
    }

    @Test
    public void whenReprIsBigDecimal() throws Exception {
        // given
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        allowingObjectSpecCorrespondingClassAndSpecIdIs(BigDecimal.class);
        final BigDecimal value = new BigDecimal("123234234.45612312343535");
        representation = new JsonRepresentation(DecimalNode.valueOf(value));
        context.checking(new Expectations() {
            {
                oneOf(mockAdapterManager).adapterFor(value);
                will(returnValue(mockObjectAdapter));
            }
        });

        // when
        final ObjectAdapter adapter = JsonValueEncoder.asAdapter(mockObjectSpec, representation, null);

        // then
        assertSame(mockObjectAdapter, adapter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenObjectSpecIsBigDecimalButReprIsNot() throws Exception {
        // given
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        allowingObjectSpecCorrespondingClassAndSpecIdIs(BigDecimal.class);

        representation = JsonRepresentation.newMap("foo", "bar");

        // when
        JsonValueEncoder.asAdapter(mockObjectSpec, representation, null);
    }

    @Test
    public void whenReprIsString() throws Exception {
        // given
        allowingObjectSpecHas(EncodableFacet.class, mockEncodableFacet);
        allowingObjectSpecCorrespondingClassAndSpecIdIs(String.class);
        representation = new JsonRepresentation(TextNode.valueOf("aString"));

        context.checking(new Expectations() {
            {
                oneOf(mockAdapterManager).adapterFor("aString");
                will(returnValue(mockObjectAdapter));
            }
        });

        // when
        final ObjectAdapter adapter = JsonValueEncoder.asAdapter(mockObjectSpec, representation, null);

        // then
        assertSame(mockObjectAdapter, adapter);
    }

    private <T extends Facet> void allowingObjectSpecHas(final Class<T> facetClass, final T encodableFacet) {
        context.checking(new Expectations() {
            {
                allowing(mockObjectSpec).getFacet(facetClass);
                will(returnValue(encodableFacet));
            }
        });
    }

    private void allowingObjectSpecCorrespondingClassAndSpecIdIs(final Class<?> result) {
        context.checking(new Expectations() {
            {
                allowing(mockObjectSpec).getCorrespondingClass();
                will(returnValue(result));

                allowing(mockObjectSpec).getSpecId();
                will(returnValue(ObjectSpecId.of(result.getName())));

            }
        });
    }

}
