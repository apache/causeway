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
package org.apache.isis.security.shiro.permrolemapper;

import java.util.Map;
import java.util.Set;

import org.apache.shiro.config.Ini;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.commons.internal.collections._Sets;

public class PermissionToRoleMapperFromIniTest {

    @Test
    public void test() {
        final Ini ini = Ini.fromResourcePath("classpath:org/apache/isis/security/shiro/permrolemapper/my.ini");
        final Map<String, Set<String>> permissionsByRole = 
                new PermissionToRoleMapperFromIni(ini).getPermissionsByRole();

        assertThat(permissionsByRole.get("role1"), is(equalTo((Set<String>)_Sets.of("foo","bar"))));
        assertThat(permissionsByRole.get("role2"), is(equalTo((Set<String>)_Sets.of("fiz:x","bip:bop:*"))));
        assertThat(permissionsByRole.get("role3"), is(equalTo((Set<String>)_Sets.of("*"))));
        assertThat(permissionsByRole.size(), is(3));
    }

}
