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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.shiro.config.Ini;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.util.PermissionUtils;

public class PermissionToRoleMapperFromIni implements PermissionToRoleMapper {

    private final Map<String, Set<String>> permissionsByRole;

    /**
     * Using the same logic as in {@link IniRealm}.
     */
    public PermissionToRoleMapperFromIni(Ini ini) {
        final Map<String, String> section = ini.getSection(IniRealm.ROLES_SECTION_NAME);
        this.permissionsByRole = Collections.unmodifiableMap(

                section.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey, 
                        entry->PermissionUtils.toPermissionStrings(entry.getValue())))

                );
    }

    @Override
    public Map<String, Set<String>> getPermissionsByRole() {
        return permissionsByRole;
    }
}
