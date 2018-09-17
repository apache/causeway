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
package org.apache.isis.security.shiro.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class UtilTest_parse {

    @Test
    public void testParse() {
        Map<String, Set<String>> perms = Util.parse("user_role = *:ToDoItemsJdo:*:*,*:ToDoItem:*:*;self-install_role = *:ToDoItemsFixturesService:install:*;admin_role = *");
        assertThat(perms, is(not(nullValue())));
        Set<String> set = perms.get("user_role");
        assertThat(set, is(not(nullValue())));
        assertThat(set.size(), is(2));
        assertThat(set.contains("*:ToDoItemsJdo:*:*"), is(true));
        assertThat(set.contains("*:ToDoItem:*:*"), is(true));

        set = perms.get("self-install_role");
        assertThat(set, is(not(nullValue())));
        assertThat(set.size(), is(1));
        assertThat(set.contains("*:ToDoItemsFixturesService:install:*"), is(true));

        set = perms.get("admin_role");
        assertThat(set, is(not(nullValue())));
        assertThat(set.size(), is(1));
        assertThat(set.contains("*"), is(true));

        set = perms.get("non-existent_role");
        assertThat(set, is(nullValue()));
    }

}
