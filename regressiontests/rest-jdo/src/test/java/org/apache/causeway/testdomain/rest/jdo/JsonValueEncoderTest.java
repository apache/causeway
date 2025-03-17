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
package org.apache.causeway.testdomain.rest.jdo;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.function.Consumer;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.internal.base._Temporals;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.jaxrsresteasy.CausewayModuleViewerRestfulObjectsJaxrsResteasy;
import org.apache.causeway.viewer.restfulobjects.rendering.service.valuerender.JsonValueConverter.Context;
import org.apache.causeway.viewer.restfulobjects.rendering.service.valuerender.JsonValueEncoderServiceDefault;

@SpringBootTest(classes={
        Configuration_headless.class,
        CausewayModuleViewerRestfulObjectsJaxrsResteasy.class
})
@TestPropertySource(CausewayPresets.UseLog4j2Test)
class JsonValueEncoderTest {

    @Inject MetaModelContext mmc;

    @Test
    public void whenString() {
        var value = "aString";
        var representation = representationFor(value);

        assertThat(representation.isString("value"), is(true));
        assertThat(representation.getString("value"), is("aString"));

        assertThat(representation.getString("format"), is(nullValue()));
        assertThat(representation.getString("extensions.x-causeway-format"), is("string"));
    }

    @Test
    public void whenBooleanWrapper() {
        var value = Boolean.TRUE;
        var representation = representationFor(value);

        assertThat(representation.isBoolean("value"), is(true));
        assertThat(representation.getBoolean("value"), is(Boolean.TRUE));

        assertThat(representation.getString("format"), is(nullValue()));
    }

    @Test
    public void whenBooleanPrimitive() {
        var value = true;
        var representation = representationFor(value);

        assertThat(representation.isBoolean("value"), is(true));
        assertThat(representation.getBoolean("value"), is(true));

        assertThat(representation.getString("format"), is(nullValue()));
    }

    @Test
    public void whenByteWrapper() {
        var value = Byte.valueOf((byte)123);
        var representation = representationFor(value);

        assertThat(representation.isIntegralNumber("value"), is(true));
        assertThat(representation.getByte("value"), is(Byte.valueOf((byte)123)));

        assertThat(representation.getString("format"), is("int"));
        assertThat(representation.getString("extensions.x-causeway-format"), is("byte"));
    }

    @Test
    public void whenBytePrimitive() {
        var value = (byte)123;
        var representation = representationFor(value);

        assertThat(representation.isIntegralNumber("value"), is(true));
        assertThat(representation.getByte("value"), is((byte)123));

        assertThat(representation.getString("format"), is("int"));
        assertThat(representation.getString("extensions.x-causeway-format"), is("byte"));
    }

    @Test
    public void whenShortWrapper() {
        var value = Short.valueOf((short)12345);
        var representation = representationFor(value);

        assertThat(representation.isIntegralNumber("value"), is(true));
        assertThat(representation.getShort("value"), is(Short.valueOf((short)12345)));

        assertThat(representation.getString("format"), is("int"));
        assertThat(representation.getString("extensions.x-causeway-format"), is("short"));
    }

    @Test
    public void whenShortPrimitive() {
        var value = (short)12345;
        var representation = representationFor(value);

        assertThat(representation.isIntegralNumber("value"), is(true));
        assertThat(representation.getShort("value"), is((short)12345));

        assertThat(representation.getString("format"), is("int"));
        assertThat(representation.getString("extensions.x-causeway-format"), is("short"));
    }

    @Test
    public void whenIntWrapper() {
        var value = Integer.valueOf(12345678);
        var representation = representationFor(value);

        assertThat(representation.isIntegralNumber("value"), is(true));
        assertThat(representation.getInt("value"), is(Integer.valueOf(12345678)));

        assertThat(representation.getString("format"), is("int"));
        assertThat(representation.getString("extensions.x-causeway-format"), is("int"));
    }

    @Test
    public void whenIntPrimitive() {
        var value = 12345678;
        var representation = representationFor(value);

        assertThat(representation.isIntegralNumber("value"), is(true));
        assertThat(representation.getInt("value"), is(12345678));

        assertThat(representation.getString("format"), is("int"));
        assertThat(representation.getString("extensions.x-causeway-format"), is("int"));
    }

    @Test
    public void whenLongWrapper() {
        var value = Long.valueOf(12345678901234L);
        var representation = representationFor(value);

        assertThat(representation.isIntegralNumber("value"), is(true));
        assertThat(representation.getLong("value"), is(Long.valueOf(12345678901234L)));

        assertThat(representation.getString("format"), is("int"));
        assertThat(representation.getString("extensions.x-causeway-format"), is("long"));
    }

    @Test
    public void whenLongPrimitive() {
        var value = 12345678901234L;
        var representation = representationFor(value);

        assertThat(representation.isIntegralNumber("value"), is(true));
        assertThat(representation.getLong("value"), is(12345678901234L));

        assertThat(representation.getString("format"), is("int"));
        assertThat(representation.getString("extensions.x-causeway-format"), is("long"));
    }

    @Test
    public void whenFloatWrapper() {
        var value = Float.valueOf((float)123.45);
        var representation = representationFor(value);

        assertThat(representation.isDecimal("value"), is(true));
        assertThat(representation.getFloat("value"), is(Float.valueOf((float)123.45)));

        assertThat(representation.getString("format"), is("decimal"));
        assertThat(representation.getString("extensions.x-causeway-format"), is("float"));
    }

    @Test
    public void whenFloatPrimitive() {
        var value = (float)123.45;
        var representation = representationFor(value);

        assertThat(representation.isDecimal("value"), is(true));
        assertThat(representation.getFloat("value"), is((float)123.45));

        assertThat(representation.getString("format"), is("decimal"));
        assertThat(representation.getString("extensions.x-causeway-format"), is("float"));
    }

    @Test
    public void whenDoubleWrapper() {
        var value = Double.valueOf(12345.6789);
        var representation = representationFor(value);

        assertThat(representation.isDecimal("value"), is(true));
        assertThat(representation.getDouble("value"), is(Double.valueOf(12345.6789)));

        assertThat(representation.getString("format"), is("decimal"));
        assertThat(representation.getString("extensions.x-causeway-format"), is("double"));
    }

    @Test
    public void whenDoublePrimitive() {
        var value = 12345.6789;
        var representation = representationFor(value);

        assertThat(representation.isDecimal("value"), is(true));
        assertThat(representation.getDouble("value"), is(12345.6789));

        assertThat(representation.getString("format"), is("decimal"));
        assertThat(representation.getString("extensions.x-causeway-format"), is("double"));
    }

    @Test
    public void whenCharWrapper() {
        var value = Character.valueOf('a');
        var representation = representationFor(value);

        assertThat(representation.isString("value"), is(true));
        assertThat(representation.getChar("value"), is(Character.valueOf('a')));

        assertThat(representation.getString("format"), is(nullValue()));
        assertThat(representation.getString("extensions.x-causeway-format"), is("char"));
    }

    @Test
    public void whenCharPrimitive() {
        var value = 'a';
        var representation = representationFor(value);

        assertThat(representation.isString("value"), is(true));
        assertThat(representation.getChar("value"), is('a'));

        assertThat(representation.getString("format"), is(nullValue()));
        assertThat(representation.getString("extensions.x-causeway-format"), is("char"));
    }

    @Test
    public void whenJavaSqlTimestamp() {

        var epochMilli = defaultInstant().toEpochMilli();
        var value = new java.sql.Timestamp(epochMilli);
        var representation = representationFor(value);

        assertThat(representation.isLong("value"), is(true));
        assertThat(representation.getLong("value"), is(epochMilli));

        assertThat(representation.getString("format"), is("utc-millisec"));
        assertThat(representation.getString("extensions.x-causeway-format"), is("javasqltimestamp"));
    }

    @Test
    public void whenBigInteger() {
        var value = new BigInteger("12345678901234567890");
        //"big-integer(22)"
        var representation = representationFor(value, Context.forTesting(22, 0));

        assertThat(representation.isString("value"), is(true));
        assertThat(representation.isBigInteger("value"), is(true));
        assertThat(representation.getBigInteger("value"), is(new BigInteger("12345678901234567890")));

        assertThat(representation.getString("format"), is("big-integer(22)"));
        assertThat(representation.getString("extensions.x-causeway-format"), is("javamathbiginteger"));
    }

    @Test
    public void whenBigDecimal() {
        var value = new BigDecimal("12345678901234567890.1234");
        //"big-decimal(27,4)"
        var representation = representationFor(value, Context.forTesting(27, 4));

        assertThat(representation.isString("value"), is(true));
        assertThat(representation.isBigDecimal("value"), is(true));
        assertThat(representation.getBigDecimal("value"), is(new BigDecimal("12345678901234567890.1234")));

        assertThat(representation.getString("format"), is("big-decimal(27,4)"));
        assertThat(representation.getString("extensions.x-causeway-format"), is("javamathbigdecimal"));
    }

    // -- TEMPORALS

    @Test
    public void whenJavaUtilDate() {
        var value = java.util.Date.from(defaultInstant());
        var representation = representationFor(value);

        assertThat(representation.isString("value"), is(true));
        assertThat(representation.getString("value"), is("2014-04-25T12:34:45Z"));

        assertThat(representation.getString("format"), is("date-time"));
        assertThat(representation.getString("extensions.x-causeway-format"), is("javautildate"));
    }

    @Test
    public void whenJavaSqlDate() {
        var value = new java.sql.Date(defaultInstant().toEpochMilli());
        var representation = representationFor(value);

        assertThat(representation.isString("value"), is(true));
        assertThat(representation.getString("value"), is("2014-04-25"));

        assertThat(representation.getString("format"), is("date"));
        assertThat(representation.getString("extensions.x-causeway-format"), is("javasqldate"));
    }

    @Test
    public void whenJodaDateTime() {
        var value = new org.joda.time.DateTime(defaultInstant().toEpochMilli());
        var representation = representationFor(value);

        assertThat(representation.isString("value"), is(true));
        assertThat(representation.getString("value"), is("2014-04-25T12:34:45Z"));

        assertThat(representation.getString("format"), is("date-time"));
        assertThat(representation.getString("extensions.x-causeway-format"), is("jodadatetime"));
    }

    @Test
    public void whenJodaLocalDateTime() {
        var value = new org.joda.time.LocalDateTime(defaultInstant().toEpochMilli());
        var representation = representationFor(value);

        assertThat(representation.isString("value"), is(true));
        assertThat(representation.getString("value"), is("2014-04-25T12:34:45Z"));

        assertThat(representation.getString("format"), is("date-time"));
        assertThat(representation.getString("extensions.x-causeway-format"), is("jodalocaldatetime"));
    }

    @Test
    public void whenJodaLocalDate() {
        var value = new org.joda.time.LocalDate(2014,4,25);
        var representation = representationFor(value);

        assertThat(representation.isString("value"), is(true));
        assertThat(representation.getString("value"), is("2014-04-25"));

        assertThat(representation.getString("format"), is("date"));
        assertThat(representation.getString("extensions.x-causeway-format"), is("jodalocaldate"));
    }

    @Test
    void whenBlob() {
        var value = Blob.of("a Blob", CommonMimeType.BIN, new byte[] {1, 2, 3});
        var representation = representationFor(value, osObj->assertEquals(
                    JsonRepresentation.newMap("name", "a Blob.bin",
                            "mimeType", "application/octet-stream",
                            "bytes", "AQID").toString(),
                    osObj.toString()));

        assertThat(representation.getString("extensions.x-causeway-format"), is("blob"));
    }

    @Test
    void whenClob() {
        var value = Clob.of("a Clob", CommonMimeType.TXT, "abc");
        var representation = representationFor(value, osObj->assertEquals(
                    JsonRepresentation.newMap("name", "a Clob.txt",
                            "mimeType", "text/plain",
                            "chars", "abc").toString(),
                    osObj.toString()));

        assertThat(representation.getString("extensions.x-causeway-format"), is("clob"));
    }

    static enum SampleEnum {
        HALLO,
        WORLD;
        public String title() {
            return name().toLowerCase();
        }
    }

    @Test
    void whenEnum() {
        var value = SampleEnum.HALLO;
        var representation = representationFor(value, osObj->assertEquals(
                    JsonRepresentation.newMap(
                            "enumType", SampleEnum.class.getName(),
                            "enumName", value.name()).toString(),
                    osObj.toString()));

        // assert emum is amended with "enumTitle"
        assertThat(representation.getString("value.enumTitle"), is(value.title()));

        assertThat(representation.getString("extensions.x-causeway-format"), is("enum"));
    }

    private JsonRepresentation representationFor(final Object value) {
        return representationFor(value, defaultContext(), osObj->assertEquals(value, osObj));
    }

    private JsonRepresentation representationFor(final Object value, final Consumer<Object> valueAsObjectVerifier) {
        return representationFor(value, defaultContext(), valueAsObjectVerifier);
    }

    private JsonRepresentation representationFor(final Object value, final Context context) {
        return representationFor(value, context, osObj->assertEquals(value, osObj));
    }

    private JsonRepresentation representationFor(
            final Object value, final Context context, final Consumer<Object> valueAsObjectVerifier) {
        var valueAdapter = mmc.getObjectManager().adapt(value);
        var jsonValueEncoder = new JsonValueEncoderServiceDefault(mmc.getSpecificationLoader());

        valueAsObjectVerifier.accept(jsonValueEncoder.asObject(valueAdapter, context));

        var representation = JsonRepresentation.newMap();
        jsonValueEncoder.appendValueAndFormat(valueAdapter, representation, context);

        //debug
        //System.err.printf("value %s-> %s %n", valueAdapter.getSpecification().getCorrespondingClass(), representation);
        return representation;
    }

    private Context defaultContext() {
        return Context.forTesting(null, null);
    }

    private Instant defaultInstant() {
        return _Temporals.parseIsoDateTime("2014-04-25T12:34:45Z")
                .toInstant();
    }

}
