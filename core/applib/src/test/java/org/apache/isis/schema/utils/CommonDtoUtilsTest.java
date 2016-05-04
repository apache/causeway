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
package org.apache.isis.schema.utils;

import org.jmock.auto.Mock;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.schema.common.v1.ValueDto;
import org.apache.isis.schema.common.v1.ValueType;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class CommonDtoUtilsTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock
    private BookmarkService mockBookmarkService;

    @Test
    public void enums() throws Exception {
        test(Vertical.DOWN);
    }

    enum Horizontal {
        LEFT, RIGHT
    }

    @Test
    public void nested_enums() throws Exception {
        test(Horizontal.LEFT);
    }

    private void test(final Enum<?> enumVal) {

        // when
        final ValueType valueType = CommonDtoUtils.asValueType(enumVal.getClass());

        // then
        assertThat(valueType, is(ValueType.ENUM));

        // and when
        final ValueDto valueDto = CommonDtoUtils.newValueDto(valueType, enumVal, mockBookmarkService);

        // then
        Object value = CommonDtoUtils.getValue(valueDto, valueType);
        assertThat(value, is(notNullValue()));

        Assert.assertEquals(value, enumVal);
    }

}