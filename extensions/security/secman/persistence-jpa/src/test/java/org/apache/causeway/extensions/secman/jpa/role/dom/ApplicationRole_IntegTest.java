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
package org.apache.causeway.extensions.secman.jpa.role.dom;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import org.apache.causeway.applib.value.Password;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.extensions.secman.applib.mmm.MmmModule;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRoleRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserStatus;
import org.apache.causeway.extensions.secman.jpa.CausewayModuleExtSecmanPersistenceJpa;
import org.apache.causeway.persistence.jpa.applib.services.JpaSupportService;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.val;

@SpringBootTest(
        classes = ApplicationRole_IntegTest.AppManifest.class
)
@ActiveProfiles("test")
@Transactional
class ApplicationRole_IntegTest extends CausewayIntegrationTestAbstract {

    @Test
    void roleAssignmentAndRemovalAreConsistentAfterReload() {
        val user = newUser("association-user");
        val role = newRole("association-role");

        roleRepository.addRoleToUser(role, user);

        Assertions.assertThat(user.getRoles()).contains(role);
        Assertions.assertThat(role.getUsers()).contains(user);

        flushAndClear();

        val reloadedUser = user("association-user");
        val reloadedRole = role("association-role");
        Assertions.assertThat(reloadedUser.getRoles()).contains(reloadedRole);
        Assertions.assertThat(reloadedRole.getUsers()).contains(reloadedUser);

        roleRepository.removeRoleFromUser(reloadedRole, reloadedUser);

        Assertions.assertThat(reloadedUser.getRoles()).doesNotContain(reloadedRole);
        Assertions.assertThat(reloadedRole.getUsers()).doesNotContain(reloadedUser);

        flushAndClear();

        val userAfterRemoval = user("association-user");
        val roleAfterRemoval = role("association-role");
        Assertions.assertThat(userAfterRemoval.getRoles()).doesNotContain(roleAfterRemoval);
        Assertions.assertThat(roleAfterRemoval.getUsers()).doesNotContain(userAfterRemoval);
    }

    @Test
    void roleSideHelperUpdatesTheOwningSide() {
        val user = (org.apache.causeway.extensions.secman.jpa.user.dom.ApplicationUser)
                newUser("helper-user");
        val role = (org.apache.causeway.extensions.secman.jpa.role.dom.ApplicationRole)
                newRole("helper-role");

        role.addToUsers(user);

        Assertions.assertThat(user.getRoles()).contains(role);
        Assertions.assertThat(role.getUsers()).contains(user);

        flushAndClear();

        Assertions.assertThat(user("helper-user").getRoles()).contains(role("helper-role"));
        Assertions.assertThat(role("helper-role").getUsers()).contains(user("helper-user"));
    }

    @Test
    void deletingAssignedRoleRemovesOwningSideAssignments() {
        val firstUser = newUser("delete-role-user-1");
        val secondUser = newUser("delete-role-user-2");
        val role = newRole("assigned-role-to-delete");
        roleRepository.addRoleToUser(role, firstUser);
        roleRepository.addRoleToUser(role, secondUser);

        roleRepository.deleteRole(role);
        flushAndClear();

        Assertions.assertThat(roleRepository.findByName("assigned-role-to-delete")).isEmpty();
        Assertions.assertThat(user("delete-role-user-1").getRoles())
                .extracting(ApplicationRole::getName)
                .doesNotContain("assigned-role-to-delete");
        Assertions.assertThat(user("delete-role-user-2").getRoles())
                .extracting(ApplicationRole::getName)
                .doesNotContain("assigned-role-to-delete");
    }

    private ApplicationUser newUser(final String username) {
        return userRepository.upsertLocal(
                username,
                Password.of("secret"),
                ApplicationUserStatus.UNLOCKED);
    }

    private ApplicationRole newRole(final String name) {
        return roleRepository.upsert(name, name);
    }

    private ApplicationUser user(final String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }

    private ApplicationRole role(final String name) {
        return roleRepository.findByName(name).orElseThrow();
    }

    private void flushAndClear() {
        val entityManager = jpaSupport.getEntityManagerElseFail(
                org.apache.causeway.extensions.secman.jpa.user.dom.ApplicationUser.class);
        entityManager.flush();
        entityManager.clear();
    }

    @Inject private JpaSupportService jpaSupport;
    @Inject private ApplicationRoleRepository roleRepository;
    @Inject private ApplicationUserRepository userRepository;

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            CausewayModuleCoreRuntimeServices.class,
            CausewayModuleSecurityBypass.class,
            CausewayModuleExtSecmanPersistenceJpa.class,
            MmmModule.class,
    })
    @PropertySources({
            @PropertySource(CausewayPresets.UseLog4j2Test),
    })
    static class AppManifest {
    }
}
