/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.util.schema;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.schema.cmd.v1.ParamDto;
import org.apache.isis.schema.common.v1.InteractionType;
import org.apache.isis.schema.common.v1.OidDto;
import org.apache.isis.schema.common.v1.ValueType;
import org.apache.isis.schema.common.v1.ValueWithTypeDto;
import org.apache.isis.schema.ixn.v1.ActionInvocationDto;
import org.apache.isis.schema.ixn.v1.InteractionDto;
import org.apache.isis.schema.ixn.v1.MemberExecutionDto;

public class Roundtrip {

    private static InteractionDto newInteractionDtoWithActionInvocation(
            final String transactionId,
            final int sequence,
            final Bookmark targetBookmark,
            final String targetTitle,
            final String actionIdentifier,
            final List<ParamDto> parameterDtos,
            final String user) {

        final MemberExecutionDto executionDto = InteractionDtoUtils.newActionInvocation(
                sequence, targetBookmark, targetTitle,
                actionIdentifier, parameterDtos,
                user);

        final InteractionDto interactionDto = new InteractionDto();

        interactionDto.setMajorVersion("1");
        interactionDto.setMinorVersion("0");

        interactionDto.setTransactionId(transactionId);
        interactionDto.setExecution(executionDto);

        executionDto.setInteractionType(InteractionType.ACTION_INVOCATION);

        return interactionDto;
    }

    @Test
    public void happyCase() throws Exception {

        // given

        final Timestamp startedAt = new Timestamp(new Date().getTime());
        @SuppressWarnings("unused")
        final Timestamp completedAt = new Timestamp(startedAt.getTime() + 1000);

        final ValueWithTypeDto returnDto = new ValueWithTypeDto();
        returnDto.setType(ValueType.BOOLEAN);
        returnDto.setNull(true);

        final InteractionDto interactionDto = newInteractionDtoWithActionInvocation(
                UUID.randomUUID().toString(),
                1,
                new Bookmark("CUS", "12345"), "John Customer", "com.mycompany.Customer#placeOrder", Arrays.<ParamDto>asList(),
                "freddyUser"
                );

        InteractionDtoUtils.addParamArg(interactionDto, "aString", String.class, "Fred", null);
        InteractionDtoUtils.addParamArg(interactionDto, "nullString", String.class, (String) null, null);

        InteractionDtoUtils.addParamArg(interactionDto, "aByte", Byte.class, (Byte) (byte) 123, null);
        InteractionDtoUtils.addParamArg(interactionDto, "nullByte", Byte.class, (Byte) null, null);

        InteractionDtoUtils.addParamArg(interactionDto, "aShort", Short.class, (Short) (short) 32123, null);
        InteractionDtoUtils.addParamArg(interactionDto, "nullShort", Short.class, (Short) null, null);

        InteractionDtoUtils.addParamArg(interactionDto, "anInt", Integer.class, 123454321, null);
        InteractionDtoUtils.addParamArg(interactionDto, "nullInt", Integer.class, (Integer) null, null);

        InteractionDtoUtils.addParamArg(interactionDto, "aLong", Long.class, 1234567654321L, null);
        InteractionDtoUtils.addParamArg(interactionDto, "nullLong", Long.class, (Long) null, null);

        InteractionDtoUtils.addParamArg(interactionDto, "aFloat", Float.class, 12345.6789F, null);
        InteractionDtoUtils.addParamArg(interactionDto, "nullFloat", Float.class, (Float) null, null);

        InteractionDtoUtils.addParamArg(interactionDto, "aDouble", Double.class, 12345678.90123, null);
        InteractionDtoUtils.addParamArg(interactionDto, "nullDouble", Double.class, (Double) null, null);

        InteractionDtoUtils.addParamArg(interactionDto, "aBoolean", Boolean.class, true, null);
        InteractionDtoUtils.addParamArg(interactionDto, "nullBoolean", Boolean.class, (Boolean) null, null);

        InteractionDtoUtils.addParamArg(interactionDto, "aChar", Character.class, 'x', null);
        InteractionDtoUtils.addParamArg(interactionDto, "nullChar", Character.class, (Character) null, null);

        InteractionDtoUtils.addParamArg(interactionDto, "aBigInteger", java.math.BigInteger.class, new java.math.BigInteger("12345678901234567890"), null);
        InteractionDtoUtils
        .addParamArg(interactionDto, "nullBigInteger", java.math.BigInteger.class, (java.math.BigInteger) null, null);

        InteractionDtoUtils.addParamArg(interactionDto, "aBigDecimal", java.math.BigDecimal.class, new java.math.BigDecimal("12345678901234567890"), null);
        InteractionDtoUtils
        .addParamArg(interactionDto, "nullBigDecimal", java.math.BigDecimal.class, (java.math.BigDecimal) null, null);

        InteractionDtoUtils
        .addParamArg(interactionDto, "aOffsetDateTime", java.time.OffsetDateTime.class, OffsetDateTime.of(2015, 5, 23, 9, 54, 1, 0, null), null);
        InteractionDtoUtils
        .addParamArg(interactionDto, "nullOffsetDateTime", java.time.OffsetDateTime.class, (OffsetDateTime) null, null);

        InteractionDtoUtils
        .addParamArg(interactionDto, "aLocalDate", java.time.LocalDate.class, LocalDate.of(2015, 5, 23), null);
        InteractionDtoUtils.addParamArg(interactionDto, "nullLocalDate", java.time.LocalDate.class, (LocalDate) null, null);

        InteractionDtoUtils.addParamArg(interactionDto, "aLocalDateTime", java.time.LocalDateTime.class, LocalDateTime.of(2015, 5, 23, 9, 54, 1), null);
        InteractionDtoUtils.addParamArg(interactionDto, "nullLocalDateTime", java.time.LocalDateTime.class, (LocalDateTime) null, null);

        InteractionDtoUtils
        .addParamArg(interactionDto, "aLocalTime", java.time.LocalTime.class, LocalTime.of(9, 54, 1), null);
        InteractionDtoUtils.addParamArg(interactionDto, "nullLocalTime", java.time.LocalTime.class, (LocalTime) null, null);

        InteractionDtoUtils.addParamArg(interactionDto, "aReference", null, new Bookmark("ORD", "12345"), null);
        InteractionDtoUtils.addParamArg(interactionDto, "nullReference", null, null, null);


        // when
        final CharArrayWriter caw = new CharArrayWriter();
        InteractionDtoUtils.toXml(interactionDto, caw);

        //InteractionDtoUtils.dump(interactionDto, System.out);

        final CharArrayReader reader = new CharArrayReader(caw.toCharArray());
        final InteractionDto recreated = InteractionDtoUtils.fromXml(reader);


        // then
        assertThat(recreated.getExecution().getMemberIdentifier(), Matchers.is(interactionDto.getExecution().getMemberIdentifier()));
        assertThat(recreated.getExecution().getTarget().getType(), Matchers.is(interactionDto.getExecution().getTarget().getType()));
        assertThat(recreated.getExecution().getTarget().getId(), Matchers.is(interactionDto.getExecution().getTarget().getId()));

        final ActionInvocationDto invocationDto = (ActionInvocationDto) recreated.getExecution();

        int param = 0;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("aString"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.STRING));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(InteractionDtoUtils.getParameterArgValue(invocationDto, param, String.class), is("Fred"));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("nullString"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.STRING));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(true));
        assertThat(InteractionDtoUtils.getParameterArgValue(invocationDto, param, String.class), is(nullValue()));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("aByte"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.BYTE));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(InteractionDtoUtils.getParameterArgValue(invocationDto, param, Byte.class), is((byte) 123));

        param++;
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.BYTE));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(true));
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("nullByte"));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("aShort"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.SHORT));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(InteractionDtoUtils.getParameterArgValue(invocationDto, param, Short.class), is((short) 32123));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("nullShort"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.SHORT));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("anInt"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.INT));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(InteractionDtoUtils.getParameterArgValue(invocationDto, param, int.class), is((int) 123454321));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("nullInt"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.INT));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("aLong"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.LONG));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(InteractionDtoUtils.getParameterArgValue(invocationDto, param, long.class), is((long) 1234567654321L));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("nullLong"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.LONG));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("aFloat"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.FLOAT));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(InteractionDtoUtils.getParameterArgValue(invocationDto, param, float.class), is((float) 12345.6789F));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("nullFloat"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.FLOAT));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("aDouble"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.DOUBLE));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(InteractionDtoUtils.getParameterArgValue(invocationDto, param, double.class), is(12345678.90123));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("nullDouble"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.DOUBLE));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("aBoolean"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.BOOLEAN));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(InteractionDtoUtils.getParameterArgValue(invocationDto, param, boolean.class), is(true));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("nullBoolean"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.BOOLEAN));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("aChar"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.CHAR));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(InteractionDtoUtils.getParameterArgValue(invocationDto, param, char.class), is('x'));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("nullChar"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.CHAR));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("aBigInteger"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.BIG_INTEGER));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(InteractionDtoUtils.getParameterArgValue(invocationDto, param, BigInteger.class), is(new java.math.BigInteger("12345678901234567890")));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("nullBigInteger"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.BIG_INTEGER));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("aBigDecimal"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.BIG_DECIMAL));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(InteractionDtoUtils.getParameterArgValue(invocationDto, param, BigDecimal.class), is(new java.math.BigDecimal("12345678901234567890")));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("nullBigDecimal"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.BIG_DECIMAL));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("aOffsetDateTime"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.OFFSET_DATE_TIME));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(false));
        // bit hacky... regular comparison fails but toString() works... must be some additional data that differs, not sure what tho'
        assertThat(
                InteractionDtoUtils.getParameterArgValue(invocationDto, param, OffsetDateTime.class).toString(), is(OffsetDateTime.of(2015, 5, 23, 9, 54, 1, 0, null).toString()));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("nullOffsetDateTime"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.OFFSET_DATE_TIME));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("aLocalDate"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.LOCAL_DATE));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(false));
        final LocalDate actual = InteractionDtoUtils.getParameterArgValue(invocationDto, param, LocalDate.class);
        final LocalDate expected = LocalDate.of(2015, 5, 23);
        assertThat(actual, equalTo(expected));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("nullLocalDate"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.LOCAL_DATE));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("aLocalDateTime"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.LOCAL_DATE_TIME));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(InteractionDtoUtils.getParameterArgValue(invocationDto, param, LocalDateTime.class), is(LocalDateTime.of(2015, 5, 23, 9, 54, 1)));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("nullLocalDateTime"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.LOCAL_DATE_TIME));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("aLocalTime"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.LOCAL_TIME));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(
                InteractionDtoUtils.getParameterArgValue(invocationDto, param, LocalTime.class), is(LocalTime.of(9, 54, 1)));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("nullLocalTime"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.LOCAL_TIME));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("aReference"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.REFERENCE));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(InteractionDtoUtils.getParameterArgValue(invocationDto, param, OidDto.class).getType(), is("ORD"));
        assertThat(InteractionDtoUtils.getParameterArgValue(invocationDto, param, OidDto.class).getId(), is("12345"));

        param++;
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("nullReference"));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.REFERENCE));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        //        final int expected = param;
        //        assertThat(recreated.getParameters().getNum(), is(expected);
        //        assertThat(recreated.getParameters().getParam().size(), is(expected);
        //        assertThat(ActionInvocationMementoDtoUtils.getNumberOfParameters(recreated), is(expected);

    }

}
