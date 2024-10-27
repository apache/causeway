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
package org.apache.causeway.viewer.restfulobjects.rendering.domainobjects;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;

class JsonValueEncoderTest
extends JsonValueEncoderTestAbstract {

    @BeforeEach
    void setup() {
        super.setUp();
    }

    @Test
    void whenManagedObjectIsNull() {
        assertThrows(RuntimeException.class, ()->{
            jsonValueEncoder.asObject(null, null);
        });
    }

    @Test
    void whenSpecIsNull() throws Exception {
        var representation = new JsonRepresentation(TextNode.valueOf("aString"));
        assertThrows(IllegalArgumentException.class, ()->{
            jsonValueEncoder.asAdapter(null, representation, null);
        });
    }

    @Test
    void whenReprIsNull() throws Exception {
        var spec = specFor(Object.class);
        assertThat(jsonValueEncoder.asAdapter(spec, null, null), is(Matchers.nullValue()));
    }

    @Test
    void whenReprIsAnArray() throws Exception {
        var spec = specFor(Integer.class); // arbitrary value class, pick any
        assertThrows(IllegalArgumentException.class, ()->{
            jsonValueEncoder.asAdapter(spec, JsonRepresentation.newArray(), null);
        });
    }

    @Test
    void whenReprIsAMap() throws Exception {
        var spec = specFor(Integer.class); // arbitrary value class, pick any
        assertThrows(IllegalArgumentException.class, ()->{
            assertNull(jsonValueEncoder.asAdapter(spec, JsonRepresentation.newMap(), null));
        });
    }

    @Test
    void whenSpecDoesNotHaveAnEncodableFacet() throws Exception {
        var representation = new JsonRepresentation(TextNode.valueOf("aString"));
        var spec = specFor(Object.class); // arbitrary non-value class, pick any
        assertThrows(IllegalArgumentException.class, ()->{
            assertNull(jsonValueEncoder.asAdapter(spec, representation, null));
        });
    }

    @ParameterizedTest
    @ValueSource(classes = {
            boolean.class, Boolean.class,
            long.class, Long.class,
            int.class, Integer.class,
            short.class, Short.class,
            byte.class, Byte.class,
            char.class, Character.class,
            double.class, Double.class,
            float.class, Float.class,
            BigInteger.class, BigDecimal.class,})
    void whenObjectSpecIsIncompatibleWithRepr(final Class<?> correspondingClass) {
        // given
        var spec = specFor(correspondingClass);

        // when repr is 'string'
        assertThrows(IllegalArgumentException.class, ()->{
            var representation = new JsonRepresentation(TextNode.valueOf("aString"));
            jsonValueEncoder.asAdapter(spec, representation, null);
        });

        // when repr is 'map'
        assertThrows(IllegalArgumentException.class, ()->{
            var representation = JsonRepresentation.newMap("foo", "bar");
            jsonValueEncoder.asAdapter(spec, representation, null);
        });
    }

    @ParameterizedTest
    @ValueSource(classes = {boolean.class, Boolean.class})
    void whenReprIsBoolean(final Class<?> correspondingClass) {
        // given
        final boolean value = true;
        var representation = new JsonRepresentation(BooleanNode.valueOf(value));
        var spec = specFor(boolean.class);

        // when
        var adapter = jsonValueEncoder.asAdapter(spec, representation, null);

        // then
        assertSame(spec, adapter.getSpecification());
    }

    @ParameterizedTest
    @ValueSource(classes = {int.class, Integer.class})
    void whenReprIsInteger(final Class<?> correspondingClass) {
        // given
        var spec = specFor(correspondingClass);
        final int value = 123;
        var representation = new JsonRepresentation(IntNode.valueOf(value));

        // when
        var adapter = jsonValueEncoder.asAdapter(spec, representation, null);

        // then
        assertSame(spec, adapter.getSpecification());
    }

    @ParameterizedTest
    @ValueSource(classes = {long.class, Long.class})
    void whenReprIsLong(final Class<?> correspondingClass) {
        // given
        var spec = specFor(correspondingClass);
        final long value = 1234567890L;
        var representation = new JsonRepresentation(LongNode.valueOf(value));

        // when
        var adapter = jsonValueEncoder.asAdapter(spec, representation, null);

        // then
        assertSame(spec, adapter.getSpecification());
    }

    @ParameterizedTest
    @ValueSource(classes = {double.class, Double.class})
    void whenReprIsDouble(final Class<?> correspondingClass) {
        // given
        var spec = specFor(correspondingClass);
        final double value = 123.45;
        var representation = new JsonRepresentation(DoubleNode.valueOf(value));

        // when
        var adapter = jsonValueEncoder.asAdapter(spec, representation, null);

        // then
        assertSame(spec, adapter.getSpecification());
    }

    @Test
    void whenReprIsBigInteger() throws Exception {
        // given
        var spec = specFor(BigInteger.class);
        final BigInteger value = BigInteger.valueOf(123);
        var representation = new JsonRepresentation(BigIntegerNode.valueOf(value));

        // when
        var adapter = jsonValueEncoder.asAdapter(spec, representation, null);

        // then
        assertSame(spec, adapter.getSpecification());
    }

    @Test
    void whenReprIsBigDecimal() throws Exception {
        // given
        var spec = specFor(BigDecimal.class);
        final BigDecimal value = new BigDecimal("123234234.45612312343535");
        var representation = new JsonRepresentation(DecimalNode.valueOf(value));

        // when
        var adapter = jsonValueEncoder.asAdapter(spec, representation, null);

        // then
        assertSame(spec, adapter.getSpecification());
    }

    @Test
    void whenReprIsString() throws Exception {
        // given
        var spec = specFor(String.class);
        var representation = new JsonRepresentation(TextNode.valueOf("aString"));

        // when
        var adapter = jsonValueEncoder.asAdapter(spec, representation, null);

        // then
        assertSame(spec, adapter.getSpecification());
    }

}
