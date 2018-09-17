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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Maps;

public class Util {

    public static Map<String, Set<String>> parse(String permissionsByRoleStr) {
        final Map<String, Set<String>> permsByRole = _Maps.newHashMap();
        
        _Strings.splitThenStream(permissionsByRoleStr, ";")
        .forEach(roleAndPermsStr->{
            
            final List<String> roleAndPerms = _Strings.splitThenStream(roleAndPermsStr, "=")
                    .collect(Collectors.toList());
            
            if(roleAndPerms.size() != 2) {
                return;
            }
            final String role = roleAndPerms.get(0).trim();
            final String permStr = roleAndPerms.get(0).trim();
            
            final Set<String> perms = _Strings.splitThenStream(permStr, ",")
                    .map(String::trim)
                    .collect(Collectors.toSet());
            
            permsByRole.put(role, perms);
        });
        
        return permsByRole;
    }
}
