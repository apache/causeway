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
package org.apache.causeway.applib.util.schema;


import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.schema.cmd.v2.MapDto;
import org.apache.causeway.schema.common.v2.TypedTupleDto;
import org.apache.causeway.schema.common.v2.ValueType;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;

class CommonDtoUtils_Test {

    @Test
    void getMapValue() {
        assertThat(CommonDtoUtils.getMapValue(null, "someKey"), is(nullValue()));
        assertThat(CommonDtoUtils.getMapValue(new MapDto(), "someKey"), is(nullValue()));

        // given
        final MapDto mapDto = new MapDto();
        final MapDto.Entry e = new MapDto.Entry();
        e.setKey("someKey");
        e.setValue("someValue");
        mapDto.getEntry().add(e);

        assertThat(CommonDtoUtils.getMapValue(mapDto, "someKey"), is("someValue"));
        assertThat(CommonDtoUtils.getMapValue(mapDto, "someThingElse"), is(nullValue()));
    }

    @Test
    void putMapKeyValue() {

        // is ignored
        CommonDtoUtils.putMapKeyValue(null, "someKey", "someValue");

        // when
        final MapDto mapDto = new MapDto();
        CommonDtoUtils.putMapKeyValue(mapDto, "someKey", "someValue");

        assertThat(CommonDtoUtils.getMapValue(mapDto, "someKey"), is("someValue"));
    }

    @Getter
    @ToString @EqualsAndHashCode
    @AllArgsConstructor
    private static class CompositeSample {
        private final long epochMillis;
        private final @NonNull String calendarName;
    }

    @Test
    void roundtripOnComposites() {

        val compositeSample = new CompositeSample(123456L, "sample");

        TypedTupleDto compositeDto = CommonDtoUtils.typedTupleBuilder(compositeSample)
            .addFundamentalType(ValueType.LONG, "epochMillis", CompositeSample::getEpochMillis)
            .addFundamentalType(ValueType.STRING, "calendarName", CompositeSample::getCalendarName)
            .build();

        val json = CommonDtoUtils.getCompositeValueAsJson(compositeDto);

        //XXX there is no guarantee, that the ordering of keys is exactly as follows - should do for now
        val expectedJson = "{\"elements\":["
                + "{\"long\":123456,\"type\":\"long\",\"name\":\"epochMillis\"},"
                + "{\"string\":\"sample\",\"type\":\"string\",\"name\":\"calendarName\"}],"
                + "\"type\":\"org.apache.causeway.applib.util.schema.CommonDtoUtils_Test$CompositeSample\","
                + "\"cardinality\":2}";

        assertEquals(expectedJson, json);

        val compositeAfterRoundtrip = CommonDtoUtils.getCompositeValueFromJson(json);

        assertEquals(expectedJson, CommonDtoUtils.getCompositeValueAsJson(compositeAfterRoundtrip));

    }

}