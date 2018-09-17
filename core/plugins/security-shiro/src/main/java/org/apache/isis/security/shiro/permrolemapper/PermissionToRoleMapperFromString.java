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

import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.security.shiro.util.Util;

public class PermissionToRoleMapperFromString implements PermissionToRoleMapper {

    private final Map<String, Set<String>> permissionsByRole = _Maps.newLinkedHashMap();

    public PermissionToRoleMapperFromString(String permissionsByRoleStr) {
        permissionsByRole.putAll(Util.parse(permissionsByRoleStr));
    }

    @Override
    public Map<String, Set<String>> getPermissionsByRole() {
        return permissionsByRole;
    }

}
