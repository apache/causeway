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
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

public class JsonValueEncoderTest_asObject {

    @Rule public JUnitRuleMockery2 context = 
            JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock private ObjectSpecification mockObjectSpec;
    @Mock private EncodableFacet mockEncodableFacet;
    @Mock private ManagedObject mockObjectAdapter;
    @Mock private SpecificationLoader specLoader;

    private JsonValueEncoder jsonValueEncoder;

    @Before
    public void setUp() throws Exception {

        context.checking(new Expectations() {
            {
                allowing(mockObjectAdapter).getSpecification();
                will(returnValue(mockObjectSpec));
            }
        });

        jsonValueEncoder = JsonValueEncoder.forTesting(specLoader);
        
    }

    @After
    public void tearDown() throws Exception {
        
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenAdapterIsNull() throws Exception {
        jsonValueEncoder.asObject(null, null);
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
        assertEquals(true, jsonValueEncoder.asObject(mockObjectAdapter, null));
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
        assertEquals(123, jsonValueEncoder.asObject(mockObjectAdapter, null));
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
        assertEquals(123456789L, jsonValueEncoder.asObject(mockObjectAdapter, null));
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
        assertEquals(12345.6789, jsonValueEncoder.asObject(mockObjectAdapter, null));
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
        assertEquals(value, jsonValueEncoder.asObject(mockObjectAdapter, null));
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
        assertEquals(value, jsonValueEncoder.asObject(mockObjectAdapter, null));
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
        final Object actual = jsonValueEncoder.asObject(mockObjectAdapter, null);
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
