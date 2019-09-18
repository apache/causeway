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
import java.sql.Timestamp;
import java.util.Date;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class JsonValueEncoderTest_appendValueAndFormat {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock
    private JsonRepresentation representation;
    @Mock
    private ObjectSpecification mockObjectSpec;
    @Mock
    private EncodableFacet mockEncodableFacet;
    @Mock
    private ObjectAdapter mockObjectAdapter;
    @Mock
    private ObjectAdapterProvider mockAdapterManager;

    @Before
    public void setUp() {
        JsonValueEncoder.testSetAdapterManager(mockAdapterManager::adapterFor);

        representation = JsonRepresentation.newMap();
    }

    @After
    public void tearDown() {
        JsonValueEncoder.testSetAdapterManager(null);
    }

    @Test
    public void whenString() {
        allowingObjectSpecToReturnSpecIdFor(String.class);
        allowingObjectAdapterToReturn("aString");

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isString("value"), is(true));
        assertThat(representation.getString("value"), is("aString"));

        assertThat(representation.getString("format"), is(nullValue()));
        assertThat(representation.getString("extensions.x-isis-format"), is("string"));
    }

    @Test
    public void whenBooleanWrapper() {
        allowingObjectSpecToReturnSpecIdFor(Boolean.class);
        allowingObjectAdapterToReturn(Boolean.TRUE);

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isBoolean("value"), is(true));
        assertThat(representation.getBoolean("value"), is(Boolean.TRUE));

        assertThat(representation.getString("format"), is(nullValue()));
    }

    @Test
    public void whenBooleanPrimitive() {
        allowingObjectSpecToReturnSpecIdFor(boolean.class);
        allowingObjectAdapterToReturn(true);

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isBoolean("value"), is(true));
        assertThat(representation.getBoolean("value"), is(true));

        assertThat(representation.getString("format"), is(nullValue()));
    }

    @Test
    public void whenByteWrapper() {
        allowingObjectSpecToReturnSpecIdFor(Byte.class);
        allowingObjectAdapterToReturn(Byte.valueOf((byte)123));

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isIntegralNumber("value"), is(true));
        assertThat(representation.getByte("value"), is(Byte.valueOf((byte)123)));

        assertThat(representation.getString("format"), is("int"));
        assertThat(representation.getString("extensions.x-isis-format"), is("byte"));
    }

    @Test
    public void whenBytePrimitive() {
        allowingObjectSpecToReturnSpecIdFor(byte.class);
        allowingObjectAdapterToReturn((byte)123);

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isIntegralNumber("value"), is(true));
        assertThat(representation.getByte("value"), is((byte)123));

        assertThat(representation.getString("format"), is("int"));
        assertThat(representation.getString("extensions.x-isis-format"), is("byte"));
    }

    @Test
    public void whenShortWrapper() {
        allowingObjectSpecToReturnSpecIdFor(Short.class);
        allowingObjectAdapterToReturn(Short.valueOf((short)12345));

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isIntegralNumber("value"), is(true));
        assertThat(representation.getShort("value"), is(Short.valueOf((short)12345)));

        assertThat(representation.getString("format"), is("int"));
        assertThat(representation.getString("extensions.x-isis-format"), is("short"));
    }

    @Test
    public void whenShortPrimitive() {
        allowingObjectSpecToReturnSpecIdFor(short.class);
        allowingObjectAdapterToReturn((short)12345);

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isIntegralNumber("value"), is(true));
        assertThat(representation.getShort("value"), is((short)12345));

        assertThat(representation.getString("format"), is("int"));
        assertThat(representation.getString("extensions.x-isis-format"), is("short"));
    }

    @Test
    public void whenIntWrapper() {
        allowingObjectSpecToReturnSpecIdFor(Integer.class);
        allowingObjectAdapterToReturn(Integer.valueOf(12345678));

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isIntegralNumber("value"), is(true));
        assertThat(representation.getInt("value"), is(Integer.valueOf(12345678)));

        assertThat(representation.getString("format"), is("int"));
        assertThat(representation.getString("extensions.x-isis-format"), is("int"));
    }

    @Test
    public void whenIntPrimitive() {
        allowingObjectSpecToReturnSpecIdFor(int.class);
        allowingObjectAdapterToReturn(12345678);

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isIntegralNumber("value"), is(true));
        assertThat(representation.getInt("value"), is(12345678));

        assertThat(representation.getString("format"), is("int"));
        assertThat(representation.getString("extensions.x-isis-format"), is("int"));
    }

    @Test
    public void whenLongWrapper() {
        allowingObjectSpecToReturnSpecIdFor(Long.class);
        allowingObjectAdapterToReturn(Long.valueOf(12345678901234L));

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isIntegralNumber("value"), is(true));
        assertThat(representation.getLong("value"), is(Long.valueOf(12345678901234L)));

        assertThat(representation.getString("format"), is("int"));
        assertThat(representation.getString("extensions.x-isis-format"), is("long"));
    }

    @Test
    public void whenLongPrimitive() {
        allowingObjectSpecToReturnSpecIdFor(long.class);
        allowingObjectAdapterToReturn(12345678901234L);

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isIntegralNumber("value"), is(true));
        assertThat(representation.getLong("value"), is(12345678901234L));

        assertThat(representation.getString("format"), is("int"));
        assertThat(representation.getString("extensions.x-isis-format"), is("long"));
    }

    @Test
    public void whenFloatWrapper() {
        allowingObjectSpecToReturnSpecIdFor(Float.class);
        allowingObjectAdapterToReturn(Float.valueOf((float)123.45));

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isDecimal("value"), is(true));
        assertThat(representation.getFloat("value"), is(Float.valueOf((float)123.45)));

        assertThat(representation.getString("format"), is("decimal"));
        assertThat(representation.getString("extensions.x-isis-format"), is("float"));
    }

    @Test
    public void whenFloatPrimitive() {
        allowingObjectSpecToReturnSpecIdFor(Float.class);
        allowingObjectAdapterToReturn((float)123.45);

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isDecimal("value"), is(true));
        assertThat(representation.getFloat("value"), is((float)123.45));

        assertThat(representation.getString("format"), is("decimal"));
        assertThat(representation.getString("extensions.x-isis-format"), is("float"));
    }

    @Test
    public void whenDoubleWrapper() {
        allowingObjectSpecToReturnSpecIdFor(Double.class);
        allowingObjectAdapterToReturn(Double.valueOf(12345.6789));

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isDecimal("value"), is(true));
        assertThat(representation.getDouble("value"), is(Double.valueOf(12345.6789)));

        assertThat(representation.getString("format"), is("decimal"));
        assertThat(representation.getString("extensions.x-isis-format"), is("double"));
    }

    @Test
    public void whenDoublePrimitive() {
        allowingObjectSpecToReturnSpecIdFor(double.class);
        allowingObjectAdapterToReturn(12345.6789);

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isDecimal("value"), is(true));
        assertThat(representation.getDouble("value"), is(12345.6789));

        assertThat(representation.getString("format"), is("decimal"));
        assertThat(representation.getString("extensions.x-isis-format"), is("double"));
    }

    @Test
    public void whenCharWrapper() {
        allowingObjectSpecToReturnSpecIdFor(Character.class);
        allowingObjectAdapterToReturn(Character.valueOf('a'));

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isString("value"), is(true));
        assertThat(representation.getChar("value"), is(Character.valueOf('a')));

        assertThat(representation.getString("format"), is(nullValue()));
        assertThat(representation.getString("extensions.x-isis-format"), is("char"));
    }

    @Test
    public void whenCharPrimitive() {
        allowingObjectSpecToReturnSpecIdFor(char.class);
        allowingObjectAdapterToReturn('a');

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isString("value"), is(true));
        assertThat(representation.getChar("value"), is('a'));

        assertThat(representation.getString("format"), is(nullValue()));
        assertThat(representation.getString("extensions.x-isis-format"), is("char"));
    }

    @Test
    public void whenJavaUtilDate() {
        allowingObjectSpecToReturnSpecIdFor(java.util.Date.class);
        allowingObjectAdapterToReturn(asDateTime("2014-04-25T12:34:45Z"));

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isString("value"), is(true));
        assertThat(representation.getString("value"), is("2014-04-25T12:34:45Z"));

        assertThat(representation.getString("format"), is("date-time"));
        assertThat(representation.getString("extensions.x-isis-format"), is("javautildate"));
    }

    @Test
    public void whenJavaSqlDate() {
        allowingObjectSpecToReturnSpecIdFor(java.sql.Date.class);
        allowingObjectAdapterToReturn(asSqlDate("2014-04-25"));

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isString("value"), is(true));
        assertThat(representation.getString("value"), is("2014-04-25"));

        assertThat(representation.getString("format"), is("date"));
        assertThat(representation.getString("extensions.x-isis-format"), is("javasqldate"));
    }

    @Test
    public void whenJodaDateTime() {
        allowingObjectSpecToReturnSpecIdFor(org.joda.time.DateTime.class);
        allowingObjectAdapterToReturn(new org.joda.time.DateTime(asDateTime("2014-04-25T12:34:45Z")));

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isString("value"), is(true));
        assertThat(representation.getString("value"), is("2014-04-25T12:34:45Z"));

        assertThat(representation.getString("format"), is("date-time"));
        assertThat(representation.getString("extensions.x-isis-format"), is("jodadatetime"));
    }

    @Test
    public void whenJodaLocalDateTime() {
        allowingObjectSpecToReturnSpecIdFor(org.joda.time.LocalDateTime.class);
        allowingObjectAdapterToReturn(new org.joda.time.LocalDateTime(asDateTime("2014-04-25T12:34:45Z")));

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isString("value"), is(true));
        assertThat(representation.getString("value"), is("2014-04-25T12:34:45Z"));

        assertThat(representation.getString("format"), is("date-time"));
        assertThat(representation.getString("extensions.x-isis-format"), is("jodalocaldatetime"));
    }

    @Test
    public void whenJodaLocalDate() {
        allowingObjectSpecToReturnSpecIdFor(org.joda.time.LocalDate.class);
        allowingObjectAdapterToReturn(new org.joda.time.LocalDate(2014,4,25));

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isString("value"), is(true));
        assertThat(representation.getString("value"), is("2014-04-25"));

        assertThat(representation.getString("format"), is("date"));
        assertThat(representation.getString("extensions.x-isis-format"), is("jodalocaldate"));
    }

    @Test
    public void whenJavaSqlTimestamp() {
        allowingObjectSpecToReturnSpecIdFor(java.sql.Timestamp.class);
        final long time = asDateTime("2014-04-25T12:34:45Z").getTime();
        allowingObjectAdapterToReturn(new Timestamp(time));

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, null, false);
        assertThat(representation.isLong("value"), is(true));
        assertThat(representation.getLong("value"), is(time));

        assertThat(representation.getString("format"), is("utc-millisec"));
        assertThat(representation.getString("extensions.x-isis-format"), is("javasqltimestamp"));
    }

    @Test
    public void whenBigInteger() {
        allowingObjectSpecToReturnSpecIdFor(BigInteger.class);
        allowingObjectAdapterToReturn(new BigInteger("12345678901234567890"));

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, "big-integer(22)", false);
        assertThat(representation.isString("value"), is(true));
        assertThat(representation.isBigInteger("value"), is(true));
        assertThat(representation.getBigInteger("value"), is(new BigInteger("12345678901234567890")));

        assertThat(representation.getString("format"), is("big-integer(22)"));
        assertThat(representation.getString("extensions.x-isis-format"), is("javamathbiginteger"));
    }

    @Test
    public void whenBigDecimal() {
        allowingObjectSpecToReturnSpecIdFor(BigDecimal.class);
        allowingObjectAdapterToReturn(new BigDecimal("12345678901234567890.1234"));

        JsonValueEncoder.appendValueAndFormat(mockObjectSpec, mockObjectAdapter, representation, "big-decimal(27,4)", false);
        assertThat(representation.isString("value"), is(true));
        assertThat(representation.isBigDecimal("value"), is(true));
        assertThat(representation.getBigDecimal("value"), is(new BigDecimal("12345678901234567890.1234")));

        assertThat(representation.getString("format"), is("big-decimal(27,4)"));
        assertThat(representation.getString("extensions.x-isis-format"), is("javamathbigdecimal"));
    }


    private void allowingObjectSpecToReturnSpecIdFor(final Class<?> cls) {
        context.checking(new Expectations() {
            {
                oneOf(mockObjectSpec).getSpecId();
                will(returnValue(ObjectSpecId.of(cls.getName())));
            }
        });
    }

    private void allowingObjectAdapterToReturn(final Object pojo) {
        context.checking(new Expectations() {
            {
                oneOf(mockObjectAdapter).getPojo();
                will(returnValue(pojo));
            }
        });
    }

    private static java.sql.Date asSqlDate(final String text) {
        return new java.sql.Date(JsonRepresentation.yyyyMMdd.parseDateTime(text).getMillis());
    }

    private static Date asDateTime(final String text) {
        return new java.util.Date(JsonRepresentation.yyyyMMddTHHmmssZ.parseDateTime(text).getMillis());
    }

}
