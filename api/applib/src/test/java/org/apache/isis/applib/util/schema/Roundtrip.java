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
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.LongAdder;

import org.hamcrest.Matchers;
import org.junit.Test;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.schema.cmd.v2.ParamDto;
import org.apache.isis.schema.common.v2.InteractionType;
import org.apache.isis.schema.common.v2.OidDto;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.schema.common.v2.ValueWithTypeDto;
import org.apache.isis.schema.ixn.v2.ActionInvocationDto;
import org.apache.isis.schema.ixn.v2.InteractionDto;
import org.apache.isis.schema.ixn.v2.MemberExecutionDto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import lombok.val;

public class Roundtrip {

    private static InteractionDto newInteractionDtoWithActionInvocation(
            final String interactionId,
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

        interactionDto.setInteractionId(interactionId);
        interactionDto.setExecution(executionDto);

        executionDto.setInteractionType(InteractionType.ACTION_INVOCATION);

        return interactionDto;
    }

    private static void addArg(final InteractionDto interactionDto, final Object sampleValue) {
        val type = sampleValue.getClass();
        val name = type.getSimpleName();
        InteractionDtoUtils.addParamArg(interactionDto, "a"+name, type, sampleValue, null);
        InteractionDtoUtils.addParamArg(interactionDto, "null"+name, type, type.cast(null), null);
    }

    private static void testArg(
            final ActionInvocationDto invocationDto,
            final LongAdder paramIndex,
            final ValueType valueType,
            final Object expectedValue) {
        testArg(invocationDto, paramIndex, valueType, expectedValue, null);
    }

    private static void testArg(
            final ActionInvocationDto invocationDto,
            final LongAdder paramIndex,
            final ValueType valueType,
            final Object expectedValue,
            final String nameOverride) {

        paramIndex.increment();
        int param = paramIndex.intValue();

        val type = expectedValue.getClass();
        val name = nameOverride!=null ? nameOverride : type.getSimpleName();
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("a"+name));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(valueType));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(false));

        val actualValue = InteractionDtoUtils.getParameterArgValue(invocationDto, param);

        // equals test, some types need special checks ...
        if(expectedValue instanceof OidDto) {

            assertThat( ((OidDto)actualValue).getId(), is(((OidDto)expectedValue).getId()) );
            assertThat( ((OidDto)actualValue).getType(), is(((OidDto)expectedValue).getType()) );

        } else if(expectedValue instanceof org.joda.time.DateTime) {

            assertThat( actualValue.toString(), is(expectedValue.toString()) );

        } else if(expectedValue instanceof Iterable
                || expectedValue.getClass().isArray()) {

            val actualAsCan = Can.ofStream(_NullSafe.streamAutodetect(actualValue));
            val expectedAsCan = Can.ofStream(_NullSafe.streamAutodetect(expectedValue));

            assertThat(actualAsCan, is(expectedAsCan));

        } else {
            assertThat(actualValue, is(expectedValue));
        }

        paramIndex.increment();
        param = paramIndex.intValue();
        assertThat(InteractionDtoUtils.getParameterName(invocationDto, param), is("null"+name));
        assertThat(InteractionDtoUtils.getParameterType(invocationDto, param), Matchers.is(valueType));
        assertThat(InteractionDtoUtils.isNull(invocationDto, param), is(true));
    }

    private static class SampleValues {

        final Bookmark bookmark = Bookmark.forLogicalTypeNameAndIdentifier("ORD", "12345");
        final OidDto reference = new OidDto();
        {
            reference.setId("12345");
            reference.setType("ORD");
        }

        final String string = "Fred";
        final byte primitiveByte = (byte)123;
        final short primitiveShort= (short) 32123;
        final int primitiveInteger = 123454321;
        final long primitiveLong = 1234567654321L;
        final float primitiveFloat = 12345.6789F;
        final double primitiveDouble = 12345678.90123d;
        final boolean primitiveBoolean = true;
        final char primitiveCharacter = 'x';

        final BigInteger bigInteger = new java.math.BigInteger("12345678901234567890");
        final BigDecimal bigDecimal = new java.math.BigDecimal("12345678901234567890");

        // java.time
        final LocalTime localTime = LocalTime.of(9, 54, 1);
        final OffsetTime offsetTime = OffsetTime.of(9, 54, 1, 123_000_000, ZoneOffset.ofTotalSeconds(-120));
        final LocalDate localDate = LocalDate.of(2015, 5, 23);
        final LocalDateTime localDateTime = LocalDateTime.of(2015, 5, 23, 9, 54, 1);
        final OffsetDateTime offsetDateTime = OffsetDateTime.of(2015, 5, 23, 9, 54, 1, 0, ZoneOffset.UTC);
        final ZonedDateTime zonedDateTime = ZonedDateTime.of(2015, 5, 23, 9, 54, 1, 0, ZoneOffset.UTC);

        // joda.time
        final org.joda.time.DateTime jodaDateTime = new org.joda.time.DateTime(2015, 5, 23, 9, 54, 1);
        final org.joda.time.LocalDate jodaLocalDate = new org.joda.time.LocalDate(2015, 5, 23);
        final org.joda.time.LocalDateTime jodaLocalDateTime = new org.joda.time.LocalDateTime(2015, 5, 23, 9, 54, 1);
        final org.joda.time.LocalTime jodaLocalTime = new org.joda.time.LocalTime(9, 54, 1);

        // iterables
        final List<Long> list = _Lists.of(1L, 2L, 3L);
        final Set<Long> set = _Sets.of(1L, 2L, 3L);
        final Can<Long> can = Can.of(1L, 2L, 3L);
        final long[] array = {1L, 2L, 3L};

    }

    private final SampleValues sampleValues = new SampleValues();


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
                Bookmark.forLogicalTypeNameAndIdentifier("CUS", "12345"), "John Customer", "com.mycompany.Customer#placeOrder", Arrays.<ParamDto>asList(),
                "freddyUser"
                );

        addArg(interactionDto, sampleValues.bookmark);
        addArg(interactionDto, sampleValues.string);
        addArg(interactionDto, sampleValues.primitiveByte);
        addArg(interactionDto, sampleValues.primitiveShort);
        addArg(interactionDto, sampleValues.primitiveInteger);
        addArg(interactionDto, sampleValues.primitiveLong);
        addArg(interactionDto, sampleValues.primitiveFloat);
        addArg(interactionDto, sampleValues.primitiveDouble);
        addArg(interactionDto, sampleValues.primitiveBoolean);
        addArg(interactionDto, sampleValues.primitiveCharacter);

        addArg(interactionDto, sampleValues.bigInteger);
        addArg(interactionDto, sampleValues.bigDecimal);

        // java.time
        addArg(interactionDto, sampleValues.localTime);
        addArg(interactionDto, sampleValues.localDate);
        addArg(interactionDto, sampleValues.localDateTime);
        addArg(interactionDto, sampleValues.offsetTime);
        addArg(interactionDto, sampleValues.offsetDateTime);
        addArg(interactionDto, sampleValues.zonedDateTime);

        // joda.time
        addArg(interactionDto, sampleValues.jodaDateTime);
        addArg(interactionDto, sampleValues.jodaLocalDate);
        addArg(interactionDto, sampleValues.jodaLocalDateTime);
        addArg(interactionDto, sampleValues.jodaLocalTime);

        // iterables
        addArg(interactionDto, sampleValues.list);
        addArg(interactionDto, sampleValues.set);
        addArg(interactionDto, sampleValues.can);
        addArg(interactionDto, sampleValues.array);

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
        val paramIndex = new LongAdder();
        paramIndex.decrement();

        testArg(invocationDto, paramIndex, ValueType.REFERENCE, sampleValues.reference, "Bookmark");
        testArg(invocationDto, paramIndex, ValueType.STRING, sampleValues.string);
        testArg(invocationDto, paramIndex, ValueType.BYTE, sampleValues.primitiveByte);
        testArg(invocationDto, paramIndex, ValueType.SHORT, sampleValues.primitiveShort);
        testArg(invocationDto, paramIndex, ValueType.INT, sampleValues.primitiveInteger);
        testArg(invocationDto, paramIndex, ValueType.LONG, sampleValues.primitiveLong);
        testArg(invocationDto, paramIndex, ValueType.FLOAT, sampleValues.primitiveFloat);
        testArg(invocationDto, paramIndex, ValueType.DOUBLE, sampleValues.primitiveDouble);
        testArg(invocationDto, paramIndex, ValueType.BOOLEAN, sampleValues.primitiveBoolean);
        testArg(invocationDto, paramIndex, ValueType.CHAR, sampleValues.primitiveCharacter);

        testArg(invocationDto, paramIndex, ValueType.BIG_INTEGER, sampleValues.bigInteger);
        testArg(invocationDto, paramIndex, ValueType.BIG_DECIMAL, sampleValues.bigDecimal);

        // java.time
        testArg(invocationDto, paramIndex, ValueType.LOCAL_TIME, sampleValues.localTime);
        testArg(invocationDto, paramIndex, ValueType.LOCAL_DATE, sampleValues.localDate);
        testArg(invocationDto, paramIndex, ValueType.LOCAL_DATE_TIME, sampleValues.localDateTime);
        testArg(invocationDto, paramIndex, ValueType.OFFSET_TIME, sampleValues.offsetTime);
        testArg(invocationDto, paramIndex, ValueType.OFFSET_DATE_TIME, sampleValues.offsetDateTime);
        testArg(invocationDto, paramIndex, ValueType.ZONED_DATE_TIME, sampleValues.zonedDateTime);

        // joda.time
        testArg(invocationDto, paramIndex, ValueType.JODA_DATE_TIME, sampleValues.jodaDateTime);
        testArg(invocationDto, paramIndex, ValueType.JODA_LOCAL_DATE, sampleValues.jodaLocalDate);
        testArg(invocationDto, paramIndex, ValueType.JODA_LOCAL_DATE_TIME, sampleValues.jodaLocalDateTime);
        testArg(invocationDto, paramIndex, ValueType.JODA_LOCAL_TIME, sampleValues.jodaLocalTime);

        // iterables
        testArg(invocationDto, paramIndex, ValueType.COLLECTION, sampleValues.list);
        testArg(invocationDto, paramIndex, ValueType.COLLECTION, sampleValues.set);
        testArg(invocationDto, paramIndex, ValueType.COLLECTION, sampleValues.can);
        testArg(invocationDto, paramIndex, ValueType.COLLECTION, sampleValues.array);
    }


}
