/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.viewer.webcomponents.sample.vue.petclinicsecured;

import java.util.Locale;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.applib.value.Password;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionMode;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionRepository;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionRule;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRoleRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserStatus;
import org.apache.causeway.viewer.webcomponents.sample.petclinic.domain.PetClinicDomainModule;

@Configuration
public class PetClinicSecmanDataConfiguration {

    public static final String USERNAME = "sven";
    public static final String PASSWORD = "pass";
    public static final String LOCKED_USERNAME = "locked";
    public static final String PASSWORDLESS_USERNAME = "passwordless";
    public static final String ROLE_NAME = "petclinic-user";

    @Bean
    ApplicationRunner loadPetClinicSecurityData(
            final TransactionService transactionService,
            final ApplicationUserRepository userRepository,
            final ApplicationRoleRepository roleRepository,
            final ApplicationPermissionRepository permissionRepository) {
        return args -> transactionService.runTransactional(Propagation.REQUIRED, () -> {
            final var existingRole = roleRepository.findByName(ROLE_NAME);
            final var role = existingRole.orElseGet(() -> roleRepository.newRole(
                    ROLE_NAME, "Use the secured Petclinic acceptance application"));
            if (existingRole.isEmpty()) {
                permissionRepository.newPermissionNoCheck(
                        role,
                        ApplicationPermissionRule.ALLOW,
                        ApplicationPermissionMode.CHANGING,
                        ApplicationFeatureSort.NAMESPACE,
                        PetClinicDomainModule.NAMESPACE);
            }

            final var unlocked = userRepository.upsertLocal(
                    USERNAME, Password.of(PASSWORD), ApplicationUserStatus.UNLOCKED);
            unlocked.setAtPath("/petclinic/europe");
            unlocked.setLanguage(Locale.ENGLISH);
            unlocked.setNumberFormat(Locale.GERMANY);
            unlocked.setTimeFormat(Locale.UK);
            addRoleIfMissing(roleRepository, role, unlocked);

            final var locked = userRepository.upsertLocal(
                    LOCKED_USERNAME, Password.of(PASSWORD), ApplicationUserStatus.LOCKED);
            addRoleIfMissing(roleRepository, role, locked);

            userRepository.upsertLocal(
                    PASSWORDLESS_USERNAME, null, ApplicationUserStatus.UNLOCKED);
        });
    }

    private static void addRoleIfMissing(
            final ApplicationRoleRepository roleRepository,
            final org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole role,
            final ApplicationUser user) {
        if (user.getRoles().stream().noneMatch(candidate -> candidate.getName().equals(role.getName()))) {
            roleRepository.addRoleToUser(role, user);
        }
    }
}
