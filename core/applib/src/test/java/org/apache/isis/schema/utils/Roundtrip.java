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
package org.apache.isis.schema.utils;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Test;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.schema.cmd.v1.ParamDto;
import org.apache.isis.schema.common.v1.OidDto;
import org.apache.isis.schema.common.v1.ValueType;
import org.apache.isis.schema.mim.v1.ActionInvocationDto;
import org.apache.isis.schema.mim.v1.MemberInteractionMementoDto;
import org.apache.isis.schema.mim.v1.ReturnDto;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class Roundtrip {

    @Test
    public void happyCase() throws Exception {

        // given

        final Timestamp startedAt = new Timestamp(new Date().getTime());
        final Timestamp completedAt = new Timestamp(startedAt.getTime() + 1000);

        final ReturnDto returnDto = new ReturnDto();
        returnDto.setReturnType(ValueType.BOOLEAN);
        returnDto.setNull(true);

        final MemberInteractionMementoDto mim = MemberInteractionMementoDtoUtils.newActionDto(
                UUID.randomUUID(),
                1,
                new Bookmark("CUS", "12345"), "John Customer", "com.mycompany.Customer#placeOrder", Arrays.<ParamDto>asList(), returnDto, "freddyUser",
                startedAt, completedAt);

        MemberInteractionMementoDtoUtils.addParamArg(mim, "aString", String.class, "Fred", null);
        MemberInteractionMementoDtoUtils.addParamArg(mim, "nullString", String.class, (String) null, null);

        MemberInteractionMementoDtoUtils.addParamArg(mim, "aByte", Byte.class, (Byte) (byte) 123, null);
        MemberInteractionMementoDtoUtils.addParamArg(mim, "nullByte", Byte.class, (Byte) null, null);

        MemberInteractionMementoDtoUtils.addParamArg(mim, "aShort", Short.class, (Short) (short) 32123, null);
        MemberInteractionMementoDtoUtils.addParamArg(mim, "nullShort", Short.class, (Short) null, null);

        MemberInteractionMementoDtoUtils.addParamArg(mim, "anInt", Integer.class, 123454321, null);
        MemberInteractionMementoDtoUtils.addParamArg(mim, "nullInt", Integer.class, (Integer) null, null);

        MemberInteractionMementoDtoUtils.addParamArg(mim, "aLong", Long.class, 1234567654321L, null);
        MemberInteractionMementoDtoUtils.addParamArg(mim, "nullLong", Long.class, (Long) null, null);

        MemberInteractionMementoDtoUtils.addParamArg(mim, "aFloat", Float.class, 12345.6789F, null);
        MemberInteractionMementoDtoUtils.addParamArg(mim, "nullFloat", Float.class, (Float) null, null);

        MemberInteractionMementoDtoUtils.addParamArg(mim, "aDouble", Double.class, 12345678.90123, null);
        MemberInteractionMementoDtoUtils.addParamArg(mim, "nullDouble", Double.class, (Double) null, null);

        MemberInteractionMementoDtoUtils.addParamArg(mim, "aBoolean", Boolean.class, true, null);
        MemberInteractionMementoDtoUtils.addParamArg(mim, "nullBoolean", Boolean.class, (Boolean) null, null);

        MemberInteractionMementoDtoUtils.addParamArg(mim, "aChar", Character.class, 'x', null);
        MemberInteractionMementoDtoUtils.addParamArg(mim, "nullChar", Character.class, (Character) null, null);

        MemberInteractionMementoDtoUtils.addParamArg(mim, "aBigInteger", java.math.BigInteger.class, new java.math.BigInteger("12345678901234567890"), null);
        MemberInteractionMementoDtoUtils
                .addParamArg(mim, "nullBigInteger", java.math.BigInteger.class, (java.math.BigInteger) null, null);

        MemberInteractionMementoDtoUtils.addParamArg(mim, "aBigDecimal", java.math.BigDecimal.class, new java.math.BigDecimal("12345678901234567890"), null);
        MemberInteractionMementoDtoUtils
                .addParamArg(mim, "nullBigDecimal", java.math.BigDecimal.class, (java.math.BigDecimal) null, null);

        MemberInteractionMementoDtoUtils
                .addParamArg(mim, "aJodaDateTime", org.joda.time.DateTime.class, new org.joda.time.DateTime(2015, 5, 23, 9, 54, 1), null);
        MemberInteractionMementoDtoUtils
                .addParamArg(mim, "nullJodaDateTime", org.joda.time.DateTime.class, (org.joda.time.DateTime) null, null);

        MemberInteractionMementoDtoUtils
                .addParamArg(mim, "aJodaLocalDate", org.joda.time.LocalDate.class, new org.joda.time.LocalDate(2015, 5, 23), null);
        MemberInteractionMementoDtoUtils.addParamArg(mim, "nullJodaLocalDate", org.joda.time.LocalDate.class, (org.joda.time.LocalDate) null, null);

        MemberInteractionMementoDtoUtils.addParamArg(mim, "aJodaLocalDateTime", org.joda.time.LocalDateTime.class, new org.joda.time.LocalDateTime(2015, 5, 23, 9, 54, 1), null);
        MemberInteractionMementoDtoUtils.addParamArg(mim, "nullJodaLocalDateTime", org.joda.time.LocalDateTime.class, (org.joda.time.LocalDateTime) null, null);

        MemberInteractionMementoDtoUtils
                .addParamArg(mim, "aJodaLocalTime", org.joda.time.LocalTime.class, new org.joda.time.LocalTime(9, 54, 1), null);
        MemberInteractionMementoDtoUtils.addParamArg(mim, "nullJodaLocalTime", org.joda.time.LocalTime.class, (org.joda.time.LocalTime) null, null);

        MemberInteractionMementoDtoUtils.addParamArg(mim, "aReference", null, new Bookmark("ORD", "12345"), null);
        MemberInteractionMementoDtoUtils.addParamArg(mim, "nullReference", null, null, null);


        // when
        final CharArrayWriter caw = new CharArrayWriter();
        MemberInteractionMementoDtoUtils.toXml(mim, caw);

        MemberInteractionMementoDtoUtils.dump(mim, System.out);

        final CharArrayReader reader = new CharArrayReader(caw.toCharArray());
        final MemberInteractionMementoDto recreated = MemberInteractionMementoDtoUtils.fromXml(reader);


        // then
        assertThat(recreated.getInteraction().getMemberIdentifier(), Matchers.is(mim.getInteraction().getMemberIdentifier()));
        assertThat(recreated.getInteraction().getTarget().getObjectType(), Matchers.is(mim.getInteraction().getTarget().getObjectType()));
        assertThat(recreated.getInteraction().getTarget().getObjectIdentifier(), Matchers.is(mim.getInteraction().getTarget().getObjectIdentifier()));

        final ActionInvocationDto invocationDto = (ActionInvocationDto) recreated.getInteraction();

        int param = 0;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("aString"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.STRING));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(MemberInteractionMementoDtoUtils.getArg(invocationDto, param, String.class), is("Fred"));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("nullString"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.STRING));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(true));
        assertThat(MemberInteractionMementoDtoUtils.getArg(invocationDto, param, String.class), is(nullValue()));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("aByte"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.BYTE));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(MemberInteractionMementoDtoUtils.getArg(invocationDto, param, Byte.class), is((byte) 123));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.BYTE));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(true));
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("nullByte"));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("aShort"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.SHORT));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(MemberInteractionMementoDtoUtils.getArg(invocationDto, param, Short.class), is((short) 32123));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("nullShort"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.SHORT));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("anInt"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.INT));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(MemberInteractionMementoDtoUtils.getArg(invocationDto, param, int.class), is((int) 123454321));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("nullInt"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.INT));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("aLong"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.LONG));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(MemberInteractionMementoDtoUtils.getArg(invocationDto, param, long.class), is((long) 1234567654321L));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("nullLong"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.LONG));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("aFloat"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.FLOAT));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(MemberInteractionMementoDtoUtils.getArg(invocationDto, param, float.class), is((float) 12345.6789F));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("nullFloat"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.FLOAT));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("aDouble"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.DOUBLE));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(MemberInteractionMementoDtoUtils.getArg(invocationDto, param, double.class), is(12345678.90123));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("nullDouble"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.DOUBLE));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("aBoolean"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.BOOLEAN));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(MemberInteractionMementoDtoUtils.getArg(invocationDto, param, boolean.class), is(true));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("nullBoolean"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.BOOLEAN));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("aChar"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.CHAR));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(MemberInteractionMementoDtoUtils.getArg(invocationDto, param, char.class), is('x'));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("nullChar"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.CHAR));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("aBigInteger"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.BIG_INTEGER));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(MemberInteractionMementoDtoUtils.getArg(invocationDto, param, BigInteger.class), is(new java.math.BigInteger("12345678901234567890")));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("nullBigInteger"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.BIG_INTEGER));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("aBigDecimal"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.BIG_DECIMAL));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(MemberInteractionMementoDtoUtils.getArg(invocationDto, param, BigDecimal.class), is(new java.math.BigDecimal("12345678901234567890")));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("nullBigDecimal"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.BIG_DECIMAL));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("aJodaDateTime"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.JODA_DATE_TIME));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(false));
        // bit hacky... regular comparison fails but toString() works... must be some additional data that differs, not sure what tho'
        assertThat(MemberInteractionMementoDtoUtils.getArg(invocationDto, param, DateTime.class).toString(), is(new DateTime(2015, 5, 23, 9, 54, 1).toString()));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("nullJodaDateTime"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.JODA_DATE_TIME));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(true));;

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("aJodaLocalDate"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.JODA_LOCAL_DATE));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(false));
        final LocalDate actual = MemberInteractionMementoDtoUtils.getArg(invocationDto, param, LocalDate.class);
        final LocalDate expected = new LocalDate(2015, 5, 23);
        assertThat(actual, equalTo(expected));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("nullJodaLocalDate"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.JODA_LOCAL_DATE));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("aJodaLocalDateTime"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.JODA_LOCAL_DATE_TIME));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(MemberInteractionMementoDtoUtils.getArg(invocationDto, param, LocalDateTime.class), is(new org.joda.time.LocalDateTime(2015, 5, 23, 9, 54, 1)));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("nullJodaLocalDateTime"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.JODA_LOCAL_DATE_TIME));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("aJodaLocalTime"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.JODA_LOCAL_TIME));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(MemberInteractionMementoDtoUtils.getArg(invocationDto, param, LocalTime.class), is(new org.joda.time.LocalTime(9, 54, 1)));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("nullJodaLocalTime"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.JODA_LOCAL_TIME));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(true));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("aReference"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.REFERENCE));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(false));
        assertThat(MemberInteractionMementoDtoUtils.getArg(invocationDto, param, OidDto.class).getObjectType(), is("ORD"));
        assertThat(MemberInteractionMementoDtoUtils.getArg(invocationDto, param, OidDto.class).getObjectIdentifier(), is("12345"));

        param++;
        assertThat(MemberInteractionMementoDtoUtils.getParameterName(invocationDto, param), is("nullReference"));
        assertThat(MemberInteractionMementoDtoUtils.getParameterType(invocationDto, param), Matchers.is(ValueType.REFERENCE));
        assertThat(MemberInteractionMementoDtoUtils.isNull(invocationDto, param), is(true));

        param++;
//        final int expected = param;
//        assertThat(recreated.getParameters().getNum(), is(expected);
//        assertThat(recreated.getParameters().getParam().size(), is(expected);
//        assertThat(ActionInvocationMementoDtoUtils.getNumberOfParameters(recreated), is(expected);

    }

}
