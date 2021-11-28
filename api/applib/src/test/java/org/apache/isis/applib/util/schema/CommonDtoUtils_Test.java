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
package org.apache.isis.applib.util.schema;

import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.applib.services.schema.SchemaValueMarshaller;
import org.apache.isis.schema.cmd.v2.MapDto;

public class CommonDtoUtils_Test {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Mock
    private SchemaValueMarshaller mockDtoContext;

    @Test
    public void enums() {
        test(Vertical.DOWN);
    }

    enum Horizontal {
        LEFT, RIGHT
    }

    @Test
    public void nested_enums() {
        test(Horizontal.LEFT);
    }

    private void test(final Enum<?> enumVal) {

        fail("not implemented");

//        // when
//        val valueTypeAndSemantics = CommonDtoUtils.asValueType(enumVal.getClass(), mockDtoContext);
//        final ValueType valueType = valueTypeAndSemantics.getValueType();
//
//        // then
//        assertThat(valueType, is(ValueType.ENUM));
//
//        // and when
//        final ValueDto valueDto = CommonDtoUtils.newValueDto(valueTypeAndSemantics, enumVal, mockDtoContext);
//
//        // then
//        Object value = CommonDtoUtils.getValue(valueDto, valueTypeAndSemantics);
//        assertThat(value, is(notNullValue()));
//
//        Assert.assertEquals(value, enumVal);
    }

    @Test
    public void getMapValue() {
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
    public void putMapKeyValue() {

        // is ignored
        CommonDtoUtils.putMapKeyValue(null, "someKey", "someValue");

        // when
        final MapDto mapDto = new MapDto();
        CommonDtoUtils.putMapKeyValue(mapDto, "someKey", "someValue");

        assertThat(CommonDtoUtils.getMapValue(mapDto, "someKey"), is("someValue"));
    }

}