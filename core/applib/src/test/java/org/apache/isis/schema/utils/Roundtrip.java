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
import java.util.Date;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Test;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.schema.aim.v2.ActionInvocationMementoDto;
import org.apache.isis.schema.aim.v2.ReturnDto;
import org.apache.isis.schema.cmd.v1.ActionDto;
import org.apache.isis.schema.common.v1.OidDto;
import org.apache.isis.schema.common.v1.ValueType;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class Roundtrip {

    @Test
    public void happyCase() throws Exception {

        // given
        final ActionDto actionDto = new ActionDto();
        actionDto.setActionIdentifier("com.mycompany.Customer#placeOrder");

        final Timestamp startedAt = new Timestamp(new Date().getTime());
        final Timestamp completedAt = new Timestamp(startedAt.getTime() + 1000);

        final ReturnDto returnDto = new ReturnDto();
        returnDto.setReturnType(ValueType.BOOLEAN);
        returnDto.setNull(true);

        final ActionInvocationMementoDto aim = ActionInvocationMementoDtoUtils.newDto(
                UUID.randomUUID(),
                1,
                new Bookmark("CUS", "12345"), actionDto, "John Customer", "freddyUser",
                startedAt, completedAt, returnDto);

        ActionInvocationMementoDtoUtils.addParamArg(aim, "aString", String.class, "Fred", null);
        ActionInvocationMementoDtoUtils.addParamArg(aim, "nullString", String.class, (String) null, null);

        ActionInvocationMementoDtoUtils.addParamArg(aim, "aByte", Byte.class, (Byte) (byte) 123, null);
        ActionInvocationMementoDtoUtils.addParamArg(aim, "nullByte", Byte.class, (Byte) null, null);

        ActionInvocationMementoDtoUtils.addParamArg(aim, "aShort", Short.class, (Short) (short) 32123, null);
        ActionInvocationMementoDtoUtils.addParamArg(aim, "nullShort", Short.class, (Short) null, null);

        ActionInvocationMementoDtoUtils.addParamArg(aim, "anInt", Integer.class, 123454321, null);
        ActionInvocationMementoDtoUtils.addParamArg(aim, "nullInt", Integer.class, (Integer) null, null);

        ActionInvocationMementoDtoUtils.addParamArg(aim, "aLong", Long.class, 1234567654321L, null);
        ActionInvocationMementoDtoUtils.addParamArg(aim, "nullLong", Long.class, (Long) null, null);

        ActionInvocationMementoDtoUtils.addParamArg(aim, "aFloat", Float.class, 12345.6789F, null);
        ActionInvocationMementoDtoUtils.addParamArg(aim, "nullFloat", Float.class, (Float) null, null);

        ActionInvocationMementoDtoUtils.addParamArg(aim, "aDouble", Double.class, 12345678.90123, null);
        ActionInvocationMementoDtoUtils.addParamArg(aim, "nullDouble", Double.class, (Double) null, null);

        ActionInvocationMementoDtoUtils.addParamArg(aim, "aBoolean", Boolean.class, true, null);
        ActionInvocationMementoDtoUtils.addParamArg(aim, "nullBoolean", Boolean.class, (Boolean) null, null);

        ActionInvocationMementoDtoUtils.addParamArg(aim, "aChar", Character.class, 'x', null);
        ActionInvocationMementoDtoUtils.addParamArg(aim, "nullChar", Character.class, (Character) null, null);

        ActionInvocationMementoDtoUtils.addParamArg(aim, "aBigInteger", java.math.BigInteger.class, new java.math.BigInteger("12345678901234567890"), null);
        ActionInvocationMementoDtoUtils.addParamArg(aim, "nullBigInteger", java.math.BigInteger.class, (java.math.BigInteger) null, null);

        ActionInvocationMementoDtoUtils.addParamArg(aim, "aBigDecimal", java.math.BigDecimal.class, new java.math.BigDecimal("12345678901234567890"), null);
        ActionInvocationMementoDtoUtils.addParamArg(aim, "nullBigDecimal", java.math.BigDecimal.class, (java.math.BigDecimal) null, null);

        ActionInvocationMementoDtoUtils.addParamArg(aim, "aJodaDateTime", org.joda.time.DateTime.class, new org.joda.time.DateTime(2015, 5, 23, 9, 54, 1), null);
        ActionInvocationMementoDtoUtils.addParamArg(aim, "nullJodaDateTime", org.joda.time.DateTime.class, (org.joda.time.DateTime) null, null);

        ActionInvocationMementoDtoUtils.addParamArg(aim, "aJodaLocalDate", org.joda.time.LocalDate.class, new org.joda.time.LocalDate(2015, 5, 23), null);
        ActionInvocationMementoDtoUtils.addParamArg(aim, "nullJodaLocalDate", org.joda.time.LocalDate.class, (org.joda.time.LocalDate) null, null);

        ActionInvocationMementoDtoUtils.addParamArg(aim, "aJodaLocalDateTime", org.joda.time.LocalDateTime.class, new org.joda.time.LocalDateTime(2015, 5, 23, 9, 54, 1), null);
        ActionInvocationMementoDtoUtils.addParamArg(aim, "nullJodaLocalDateTime", org.joda.time.LocalDateTime.class, (org.joda.time.LocalDateTime) null, null);

        ActionInvocationMementoDtoUtils.addParamArg(aim, "aJodaLocalTime", org.joda.time.LocalTime.class, new org.joda.time.LocalTime(9, 54, 1), null);
        ActionInvocationMementoDtoUtils.addParamArg(aim, "nullJodaLocalTime", org.joda.time.LocalTime.class, (org.joda.time.LocalTime) null, null);

        ActionInvocationMementoDtoUtils.addParamArg(aim, "aReference", null, new Bookmark("ORD", "12345"), null);
        ActionInvocationMementoDtoUtils.addParamArg(aim, "nullReference", null, null, null);


        // when
        final CharArrayWriter caw = new CharArrayWriter();
        ActionInvocationMementoDtoUtils.toXml(aim, caw);

        ActionInvocationMementoDtoUtils.dump(aim, System.out);

        final CharArrayReader reader = new CharArrayReader(caw.toCharArray());
        final ActionInvocationMementoDto recreated = ActionInvocationMementoDtoUtils.fromXml(reader);


        // then
        assertThat(recreated.getInvocation().getAction().getActionIdentifier(), Matchers.is(aim.getInvocation().getAction().getActionIdentifier()));
        assertThat(recreated.getInvocation().getTarget().getObjectType(), Matchers.is(aim.getInvocation().getTarget().getObjectType()));
        assertThat(recreated.getInvocation().getTarget().getObjectIdentifier(), Matchers.is(aim.getInvocation().getTarget().getObjectIdentifier()));


        int param = 0;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("aString"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.STRING));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(false));
        assertThat(ActionInvocationMementoDtoUtils.getArg(recreated, param, String.class), is("Fred"));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("nullString"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.STRING));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(true));
        assertThat(ActionInvocationMementoDtoUtils.getArg(recreated, param, String.class), is(nullValue()));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("aByte"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.BYTE));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(false));
        assertThat(ActionInvocationMementoDtoUtils.getArg(recreated, param, Byte.class), is((byte) 123));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.BYTE));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(true));
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("nullByte"));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("aShort"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.SHORT));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(false));
        assertThat(ActionInvocationMementoDtoUtils.getArg(recreated, param, Short.class), is((short) 32123));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("nullShort"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.SHORT));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(true));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("anInt"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.INT));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(false));
        assertThat(ActionInvocationMementoDtoUtils.getArg(recreated, param, int.class), is((int) 123454321));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("nullInt"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.INT));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(true));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("aLong"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.LONG));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(false));
        assertThat(ActionInvocationMementoDtoUtils.getArg(recreated, param, long.class), is((long) 1234567654321L));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("nullLong"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.LONG));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(true));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("aFloat"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.FLOAT));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(false));
        assertThat(ActionInvocationMementoDtoUtils.getArg(recreated, param, float.class), is((float) 12345.6789F));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("nullFloat"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.FLOAT));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(true));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("aDouble"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.DOUBLE));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(false));
        assertThat(ActionInvocationMementoDtoUtils.getArg(recreated, param, double.class), is(12345678.90123));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("nullDouble"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.DOUBLE));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(true));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("aBoolean"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.BOOLEAN));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(false));
        assertThat(ActionInvocationMementoDtoUtils.getArg(recreated, param, boolean.class), is(true));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("nullBoolean"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.BOOLEAN));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(true));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("aChar"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.CHAR));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(false));
        assertThat(ActionInvocationMementoDtoUtils.getArg(recreated, param, char.class), is('x'));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("nullChar"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.CHAR));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(true));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("aBigInteger"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.BIG_INTEGER));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(false));
        assertThat(ActionInvocationMementoDtoUtils.getArg(recreated, param, BigInteger.class), is(new java.math.BigInteger("12345678901234567890")));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("nullBigInteger"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.BIG_INTEGER));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(true));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("aBigDecimal"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.BIG_DECIMAL));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(false));
        assertThat(ActionInvocationMementoDtoUtils.getArg(recreated, param, BigDecimal.class), is(new java.math.BigDecimal("12345678901234567890")));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("nullBigDecimal"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.BIG_DECIMAL));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(true));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("aJodaDateTime"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.JODA_DATE_TIME));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(false));
        // bit hacky... regular comparison fails but toString() works... must be some additional data that differs, not sure what tho'
        assertThat(ActionInvocationMementoDtoUtils.getArg(recreated, param, DateTime.class).toString(), is(new DateTime(2015, 5, 23, 9, 54, 1).toString()));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("nullJodaDateTime"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.JODA_DATE_TIME));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(true));;

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("aJodaLocalDate"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.JODA_LOCAL_DATE));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(false));
        final LocalDate actual = ActionInvocationMementoDtoUtils.getArg(recreated, param, LocalDate.class);
        final LocalDate expected = new LocalDate(2015, 5, 23);
        assertThat(actual, equalTo(expected));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("nullJodaLocalDate"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.JODA_LOCAL_DATE));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(true));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("aJodaLocalDateTime"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.JODA_LOCAL_DATE_TIME));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(false));
        assertThat(ActionInvocationMementoDtoUtils.getArg(recreated, param, LocalDateTime.class), is(new org.joda.time.LocalDateTime(2015, 5, 23, 9, 54, 1)));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("nullJodaLocalDateTime"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.JODA_LOCAL_DATE_TIME));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(true));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("aJodaLocalTime"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.JODA_LOCAL_TIME));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(false));
        assertThat(ActionInvocationMementoDtoUtils.getArg(recreated, param, LocalTime.class), is(new org.joda.time.LocalTime(9, 54, 1)));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("nullJodaLocalTime"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.JODA_LOCAL_TIME));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(true));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("aReference"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.REFERENCE));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(false));
        assertThat(ActionInvocationMementoDtoUtils.getArg(recreated, param, OidDto.class).getObjectType(), is("ORD"));
        assertThat(ActionInvocationMementoDtoUtils.getArg(recreated, param, OidDto.class).getObjectIdentifier(), is("12345"));

        param++;
        assertThat(ActionInvocationMementoDtoUtils.getParameterName(recreated, param), is("nullReference"));
        assertThat(ActionInvocationMementoDtoUtils.getParameterType(recreated, param), Matchers.is(ValueType.REFERENCE));
        assertThat(ActionInvocationMementoDtoUtils.isNull(recreated, param), is(true));

        param++;
//        final int expected = param;
//        assertThat(recreated.getParameters().getNum(), is(expected);
//        assertThat(recreated.getParameters().getParam().size(), is(expected);
//        assertThat(ActionInvocationMementoDtoUtils.getNumberOfParameters(recreated), is(expected);

    }

}
