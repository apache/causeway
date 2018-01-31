package org.apache.isis.schema.utils;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.schema.cmd.v1.CommandDto;
import org.apache.isis.schema.cmd.v1.MapDto;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

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

        CommandDtoUtils.setUserData(dto, "someKey", null);
        assertThat(CommandDtoUtils.getUserData(dto, "someKey"), is(nullValue()));
    }
}