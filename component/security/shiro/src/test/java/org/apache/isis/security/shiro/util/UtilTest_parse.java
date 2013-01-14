package org.apache.isis.security.shiro.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.apache.isis.security.shiro.util.Util;
import org.junit.Test;

public class UtilTest_parse {

    @Test
    public void testParse() {
        Map<String, List<String>> perms = Util.parse("user_role = *:ToDoItemsJdo:*:*,*:ToDoItem:*:*;self-install_role = *:ToDoItemsFixturesService:install:*;admin_role = *");
        assertThat(perms, is(not(nullValue())));
        List<String> list = perms.get("user_role");
        assertThat(list, is(not(nullValue())));
        assertThat(list.size(), is(2));
        assertThat(list.get(0), is("*:ToDoItemsJdo:*:*"));
        assertThat(list.get(1), is("*:ToDoItem:*:*"));

        list = perms.get("self-install_role");
        assertThat(list, is(not(nullValue())));
        assertThat(list.size(), is(1));
        assertThat(list.get(0), is("*:ToDoItemsFixturesService:install:*"));

        list = perms.get("admin_role");
        assertThat(list, is(not(nullValue())));
        assertThat(list.size(), is(1));
        assertThat(list.get(0), is("*"));

        list = perms.get("non-existent_role");
        assertThat(list, is(nullValue()));
    }

}
