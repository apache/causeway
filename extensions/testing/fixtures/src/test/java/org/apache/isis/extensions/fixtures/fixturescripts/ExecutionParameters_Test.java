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
package org.apache.isis.extensions.fixtures.fixturescripts;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ExecutionParameters_Test {

    private ExecutionParameters executionParameters;

    @Before
    public void setUp() throws Exception {
        executionParameters = new ExecutionParameters("");
    }

    @Test
    public void roundTripByte() throws Exception {

        // given
        final byte value = (byte) 3;

        // when
        executionParameters.setParameter("test", value);
        final byte roundTripped = executionParameters.getParameterAsByte("test");
        final byte roundTrippedAsT = executionParameters.getParameterAsT("test", Byte.class);

        // then
        assertThat(roundTripped, is(value));
        assertThat(roundTrippedAsT, is(value));
    }

    @Test
    public void roundTripShort() throws Exception {

        // given
        final short value = (short) 323;

        // when
        executionParameters.setParameter("test", value);
        final short roundTripped = executionParameters.getParameterAsShort("test");
        final short roundTrippedAsT = executionParameters.getParameterAsT("test", Short.class);

        // then
        assertThat(roundTripped, is(value));
        assertThat(roundTrippedAsT, is(value));
    }

    @Test
    public void roundTripInteger() throws Exception {

        // given
        final int value = 32323;

        // when
        executionParameters.setParameter("test", value);
        final int roundTripped = executionParameters.getParameterAsInteger("test");
        final int roundTrippedAsT = executionParameters.getParameterAsT("test", Integer.class);

        // then
        assertThat(roundTripped, is(value));
        assertThat(roundTrippedAsT, is(value));
    }

    @Test
    public void roundTripLong() throws Exception {

        // given
        final long value = 323232323;

        // when
        executionParameters.setParameter("test", value);
        final long roundTripped = executionParameters.getParameterAsLong("test");
        final long roundTrippedAsT = executionParameters.getParameterAsT("test", Long.class);

        // then
        assertThat(roundTripped, is(value));
        assertThat(roundTrippedAsT, is(value));
    }

    @Test
    public void roundTripFloat() throws Exception {

        // given
        final float value = 323232323.2323f;

        // when
        executionParameters.setParameter("test", value);
        final float roundTripped = executionParameters.getParameterAsFloat("test");
        final float roundTrippedAsT = executionParameters.getParameterAsT("test", Float.class);

        // then
        assertThat(roundTripped, is(value));
        assertThat(roundTrippedAsT, is(value));
    }

    @Test
    public void roundTripDouble() throws Exception {

        // given
        final double value = 323232323123.2323d;

        // when
        executionParameters.setParameter("test", value);
        final double roundTripped = executionParameters.getParameterAsDouble("test");
        final double roundTrippedAsT = executionParameters.getParameterAsT("test", Double.class);

        // then
        assertThat(roundTripped, is(value));
        assertThat(roundTrippedAsT, is(value));
    }

    @Test
    public void roundTripCharacter() throws Exception {

        // given
        final char value = 'x';

        // when
        executionParameters.setParameter("test", value);
        final char roundTripped = executionParameters.getParameterAsCharacter("test");
        final char roundTrippedAsT = executionParameters.getParameterAsT("test", Character.class);

        // then
        assertThat(roundTripped, is(value));
        assertThat(roundTrippedAsT, is(value));
    }

    @Test
    public void roundTripString() throws Exception {

        // given
        final String value = "Hello World!";

        // when
        executionParameters.setParameter("test", value);
        final String roundTripped = executionParameters.getParameter("test");
        final String roundTrippedAsT = executionParameters.getParameterAsT("test", String.class);

        // then
        assertThat(roundTripped, is(value));
        assertThat(roundTrippedAsT, is(value));
    }

    @Test
    public void roundTripBoolean() throws Exception {

        // given
        final boolean value = true;

        // when
        executionParameters.setParameter("test", value);
        final boolean roundTripped = executionParameters.getParameterAsBoolean("test");
        final boolean roundTrippedAsT = executionParameters.getParameterAsT("test", Boolean.class);

        // then
        assertThat(roundTripped, is(value));
        assertThat(roundTrippedAsT, is(value));
    }


    @Test
    public void roundTripBigDecimal() throws Exception {

        // given
        final BigDecimal value = new BigDecimal("123456.789123456789");

        // when
        executionParameters.setParameter("test", value);
        final BigDecimal roundTripped = executionParameters.getParameterAsBigDecimal("test");
        final BigDecimal roundTrippedAsT = executionParameters.getParameterAsT("test", BigDecimal.class);

        // then
        assertThat(roundTripped, is(value));
        assertThat(roundTrippedAsT, is(value));
    }

    @Test
    public void roundTripBigInteger() throws Exception {

        // given
        final BigInteger value = new BigInteger("123456789123456789");

        // when
        executionParameters.setParameter("test", value);
        final BigInteger roundTripped = executionParameters.getParameterAsBigInteger("test");
        final BigInteger roundTrippedAsT = executionParameters.getParameterAsT("test", BigInteger.class);

        // then
        assertThat(roundTripped, is(value));
        assertThat(roundTrippedAsT, is(value));
    }

    @Test
    public void roundTripLocalDate() throws Exception {

        // given
        final LocalDate value = LocalDate.now();

        // when
        executionParameters.setParameter("test", value);
        final LocalDate roundTripped = executionParameters.getParameterAsLocalDate("test");
        final LocalDate roundTrippedAsT = executionParameters.getParameterAsT("test", LocalDate.class);

        // then
        assertThat(roundTripped, is(value));
        assertThat(roundTrippedAsT, is(value));
    }

    @Test
    public void roundTripLocalDateTime() throws Exception {

        // given
        final LocalDateTime value = LocalDateTime.now();

        // when
        executionParameters.setParameter("test", value);
        final LocalDateTime roundTripped = executionParameters.getParameterAsLocalDateTime("test");
        final LocalDateTime roundTrippedAsT = executionParameters.getParameterAsT("test", LocalDateTime.class);

        // then
        assertThat(roundTripped, is(value));
        assertThat(roundTrippedAsT, is(value));
    }

    private static enum EnumForTest {
        hello,
        world
    }

    @Test
    public void roundTripEnum() throws Exception {

        // given
        final EnumForTest value = EnumForTest.hello;

        // when
        executionParameters.setParameter("test", value);
        final EnumForTest roundTripped = executionParameters.getParameterAsEnum("test", EnumForTest.class);
        final EnumForTest roundTrippedAsT = executionParameters.getParameterAsT("test", EnumForTest.class);

        // then
        assertThat(roundTripped, is(value));
        assertThat(roundTrippedAsT, is(value));
    }


}