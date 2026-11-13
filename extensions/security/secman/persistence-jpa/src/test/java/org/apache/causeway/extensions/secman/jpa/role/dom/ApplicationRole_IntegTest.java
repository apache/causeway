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

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import org.apache.causeway.applib.value.Password;
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

@SpringBootTest(
        classes = ApplicationRole_IntegTest.AppManifest.class
)
@ActiveProfiles("test")
@Transactional
class ApplicationRole_IntegTest extends CausewayIntegrationTestAbstract {

    @Test
    void roleAssignmentAndRemovalAreConsistentAfterReload() {
        var user = newUser("association-user");
        var role = newRole("association-role");

        roleRepository.addRoleToUser(role, user);

        assertThat(user.getRoles()).contains(role);
        assertThat(role.getUsers()).contains(user);

        flushAndClear();

        var reloadedUser = user("association-user");
        var reloadedRole = role("association-role");
        assertThat(reloadedUser.getRoles()).contains(reloadedRole);
        assertThat(reloadedRole.getUsers()).contains(reloadedUser);

        roleRepository.removeRoleFromUser(reloadedRole, reloadedUser);

        assertThat(reloadedUser.getRoles()).doesNotContain(reloadedRole);
        assertThat(reloadedRole.getUsers()).doesNotContain(reloadedUser);

        flushAndClear();

        var userAfterRemoval = user("association-user");
        var roleAfterRemoval = role("association-role");
        assertThat(userAfterRemoval.getRoles()).doesNotContain(roleAfterRemoval);
        assertThat(roleAfterRemoval.getUsers()).doesNotContain(userAfterRemoval);
    }

    @Test
    void roleSideHelperUpdatesTheOwningSide() {
        var user = (org.apache.causeway.extensions.secman.jpa.user.dom.ApplicationUser)
                newUser("helper-user");
        var role = (org.apache.causeway.extensions.secman.jpa.role.dom.ApplicationRole)
                newRole("helper-role");

        role.addToUsers(user);

        assertThat(user.getRoles()).contains(role);
        assertThat(role.getUsers()).contains(user);

        flushAndClear();

        assertThat(user("helper-user").getRoles()).contains(role("helper-role"));
        assertThat(role("helper-role").getUsers()).contains(user("helper-user"));
    }

    @Test
    void deletingAssignedRoleRemovesOwningSideAssignments() {
        var firstUser = newUser("delete-role-user-1");
        var secondUser = newUser("delete-role-user-2");
        var role = newRole("assigned-role-to-delete");
        roleRepository.addRoleToUser(role, firstUser);
        roleRepository.addRoleToUser(role, secondUser);

        roleRepository.deleteRole(role);
        flushAndClear();

        assertThat(roleRepository.findByName("assigned-role-to-delete")).isEmpty();
        assertThat(user("delete-role-user-1").getRoles())
                .extracting(ApplicationRole::getName)
                .doesNotContain("assigned-role-to-delete");
        assertThat(user("delete-role-user-2").getRoles())
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
        var entityManager = jpaSupport.getEntityManagerElseFail(
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
    public static class AppManifest {
    }
}
