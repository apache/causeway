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
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.commons.io.YamlUtils;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermission;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionMode;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionRule;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRoleRepository;
import org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancy;
import org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancyRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.AccountType;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserStatus;

import lombok.Data;
import lombok.NonNull;

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
            var permissionDto = new PermissionDto();
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
            var roleDto = new RoleDto();
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

        static UserDto from(final ApplicationUser user, Function<Locale, String> localeStringifier) {
            var userDto = new UserDto();
            userDto.set__username(user.getUsername());
            userDto.setEncryptedPassword(user.getEncryptedPassword());
            userDto.setAccountType(user.getAccountType());
            
            userDto.setAtPath(user.getAtPath());
            userDto.setFamilyName(user.getFamilyName());
            userDto.setGivenName(user.getGivenName());
            userDto.setKnownAs(user.getKnownAs());
            userDto.setEmailAddress(user.getEmailAddress());
            userDto.setPhoneNumber(user.getPhoneNumber());
            userDto.setFaxNumber(user.getFaxNumber());
            userDto.setLanguage(localeStringifier.apply(user.getLanguage()));
            userDto.setNumberFormat(localeStringifier.apply(user.getNumberFormat()));
            userDto.setTimeFormat(localeStringifier.apply(user.getTimeFormat()));
            userDto.setStatus(user.getStatus());

            user.getRoles().stream()
            .map(ApplicationRole::getName)
            .forEach(userDto.getRoleNames()::add);

            return userDto;
        }

        String __username; // secondary key - ensure earliest alphabetic order
        String encryptedPassword;
        AccountType accountType;
        
        String familyName;
        String givenName;
        String knownAs;
        String emailAddress;
        String phoneNumber;
        String faxNumber;
        String language;
        String numberFormat;
        String timeFormat;
        String atPath;
        ApplicationUserStatus status;
        
        List<String> roleNames = new ArrayList<>();
    }

    @Data
    public static class TenancyDto {

        static TenancyDto from(final ApplicationTenancy tenancy) {
            var tenancyDto = new TenancyDto();
            tenancyDto.set__name(tenancy.getName());
            tenancyDto.setPath(tenancy.getPath());

            Optional.ofNullable(tenancy.getParent())
                .ifPresent(parent->
                    tenancyDto.setParentPath(parent.getPath()));
            return tenancyDto;
        }

        String __name; // secondary key - ensure earliest alphabetic order
        String path;
        @Nullable String parentPath;
    }

    public static ApplicationSecurityDto create(
            final @NonNull ApplicationRoleRepository applicationRoleRepository,
            final @NonNull ApplicationUserRepository applicationUserRepository,
            final @NonNull ApplicationTenancyRepository applicationTenancyRepository,
            final @NonNull ValueSemanticsProvider<Locale> localeSemantics) {
        var model = new ApplicationSecurityDto();

        applicationRoleRepository.allRoles().stream()
        .map(RoleDto::from)
        .forEach(model.getRoles()::add);

        applicationUserRepository.allUsers().stream()
        .map(user->UserDto.from(user, 
                locale->localeSemantics.getParser().parseableTextRepresentation(null, locale)))
        .forEach(model.getUsers()::add);

        applicationTenancyRepository.allTenancies().stream()
        .map(TenancyDto::from)
        .forEach(model.getTenancies()::add);

        return model;
    }

    public static Try<ApplicationSecurityDto> tryRead(final @Nullable DataSource dataSource) {
        if(dataSource==null) {
            return Try.success(null);
        }
        return YamlUtils.tryRead(ApplicationSecurityDto.class, dataSource);
    }

    private List<RoleDto> roles = new ArrayList<>();
    private List<UserDto> users = new ArrayList<>();
    private List<TenancyDto> tenancies = new ArrayList<>();

    public String toYaml() {
        return YamlUtils.toStringUtf8(this);
    }

}
