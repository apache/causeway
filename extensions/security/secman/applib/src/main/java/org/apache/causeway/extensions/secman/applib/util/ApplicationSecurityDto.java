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
package org.apache.causeway.extensions.secman.applib.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.causeway.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.causeway.commons.internal.resources._Yaml;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermission;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionMode;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionRule;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRoleRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;

import lombok.Data;
import lombok.NonNull;
import lombok.val;

/**
 * In-memory model of users, roles and permissions.
 *
 * @since 2.0 {index}
 */
@Data
public class ApplicationSecurityDto {

    @Data
    public static class PermissionDto {

        static PermissionDto from(final ApplicationPermission permission) {
            val permissionDto = new PermissionDto();
            permissionDto.setFeatureFqn(permission.getFeatureFqn());
            permissionDto.setFeatureSort(permission.getFeatureSort());
            permissionDto.setMode(permission.getMode());
            permissionDto.setRule(permission.getRule());
            return permissionDto;
        }

        String featureFqn;
        ApplicationFeatureSort featureSort;
        ApplicationPermissionMode mode;
        ApplicationPermissionRule rule;
    }

    @Data
    public static class RoleDto {

        static RoleDto from(final ApplicationRole role) {
            val roleDto = new RoleDto();
            roleDto.set__name(role.getName());
            roleDto.setDescription(role.getDescription());
            role.getPermissions().stream()
            .map(PermissionDto::from)
            .forEach(roleDto.getPermissions()::add);
            return roleDto;
        }

        String __name; // secondary key - ensure earliest alphabetic order
        String description;
        List<PermissionDto> permissions = new ArrayList<>();
    }

    @Data
    public static class UserDto {

        static UserDto from(final ApplicationUser user) {
            val userDto = new UserDto();
            userDto.set__username(user.getUsername());

            user.getRoles().stream()
            .map(ApplicationRole::getName)
            .forEach(userDto.getRoleNames()::add);

            return userDto;
        }

        String __username; // secondary key - ensure earliest alphabetic order
        List<String> roleNames = new ArrayList<>();
    }

    public static ApplicationSecurityDto create(
            final @NonNull ApplicationRoleRepository applicationRoleRepository,
            final @NonNull ApplicationUserRepository applicationUserRepository) {
        val model = new ApplicationSecurityDto();

        applicationRoleRepository.allRoles().stream()
        .map(RoleDto::from)
        .forEach(model.getRoles()::add);

        applicationUserRepository.allUsers().stream()
        .map(UserDto::from)
        .forEach(model.getUsers()::add);

        return model;
    }

    private List<RoleDto> roles = new ArrayList<>();
    private List<UserDto> users = new ArrayList<>();

    public String toYaml() {
        return _Yaml.toString(this).getValue().orElseThrow();
    }

}
