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

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.MapDto;

public class CommandDtoUtils_Test {

    CommandDto dto;
    @Before
    public void setUp() throws Exception {
        dto = new CommandDto();
    }

    @Test
    public void getUserData() {

        // null is just ignored
        assertThat(CommandDtoUtils.getUserData(null, "someKey"), is(nullValue()));

        // empty
        assertThat(CommandDtoUtils.getUserData(dto, "someKey"), is(nullValue()));

        // populated
        final MapDto mapDto = new MapDto();
        CommonDtoUtils.putMapKeyValue(mapDto, "someKey", "someValue");
        dto.setUserData(mapDto);

        assertThat(CommandDtoUtils.getUserData(dto, "someKey"), is("someValue"));
    }

    @Test
    public void setUserData() {

        CommandDtoUtils.setUserData(dto, "someKey", "someValue");
        assertThat(CommandDtoUtils.getUserData(dto, "someKey"), is("someValue"));

        CommandDtoUtils.setUserData(dto, "someKey", "someOtherValue");
        assertThat(CommandDtoUtils.getUserData(dto, "someKey"), is("someOtherValue"));

    }

    @Test
    public void clearUserData() {
        // given
        CommandDtoUtils.setUserData(dto, "someKey", "someOtherValue");
        assertThat(CommandDtoUtils.getUserData(dto, "someKey"), is("someOtherValue"));

        // when
        CommandDtoUtils.clearUserData(dto, "someKey");

        // then
        assertThat(CommandDtoUtils.getUserData(dto, "someKey"), is(nullValue()));
    }
}