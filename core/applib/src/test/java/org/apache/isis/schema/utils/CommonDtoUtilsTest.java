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

import org.junit.Test;

import org.apache.isis.schema.common.v1.ValueDto;
import org.apache.isis.schema.common.v1.ValueType;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class CommonDtoUtilsTest {

    @Test
    public void enums() throws Exception {

        ValueDto valueDto = new ValueDto();
        boolean b = CommonDtoUtils.setValue(valueDto, Vertical.class, Vertical.DOWN);

        assertThat(b, is(true));

        Object value = CommonDtoUtils.getValue(valueDto, ValueType.ENUM);
        assertThat(value, is(notNullValue()));

        assertThat((Vertical)value, is(equalTo(Vertical.DOWN)));
    }

    enum Horizontal {
        LEFT, RIGHT
    }

    @Test
    public void nested_enums() throws Exception {

        ValueDto valueDto = new ValueDto();
        boolean b = CommonDtoUtils.setValue(valueDto, Horizontal.class, Horizontal.LEFT);

        assertThat(b, is(true));

        Object value = CommonDtoUtils.getValue(valueDto, ValueType.ENUM);
        assertThat(value, is(notNullValue()));

        assertThat((Horizontal)value, is(equalTo(Horizontal.LEFT)));
    }


}