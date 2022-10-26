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
package org.apache.causeway.extensions.secman.applib;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.value.Password;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermission;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionRepository;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRoleRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserStatus;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import static org.apache.causeway.applib.services.appfeat.ApplicationFeatureId.newType;
import static org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionMode.CHANGING;
import static org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionRule.ALLOW;

import lombok.val;

public abstract class ApplicationPermissionRepositoryIntegTestAbstract extends CausewayIntegrationTestAbstract {

    protected ApplicationUser maryUser, mungoUser, midgeUser;
    protected ApplicationRole girlRole, dogRole, mouseRole, littleRole, flatDwellerRole;
    protected ApplicationPermission rideLiftForGirlRolePerm, rideLiftForDogRolePerm, rideLiftForMouseRolePerm;
    protected ApplicationPermission liftButtonForGirlRolePerm;
    protected ApplicationPermission chaseCatsForDogRolePerm;
    protected ApplicationPermission eatCheeseForMouseRolePerm;

    @BeforeEach
    void setUp() {
        maryUser = userRepository
                .upsertLocal("mary", Password.of("marypass"), ApplicationUserStatus.UNLOCKED);
        mungoUser = userRepository
                .upsertLocal("mungo", Password.of("mungopass"), ApplicationUserStatus.UNLOCKED);
        midgeUser = userRepository.upsertLocal("midge", Password.of("midgepass"), ApplicationUserStatus.UNLOCKED);

        girlRole = roleRepository.upsert("girl", "Girl");
        dogRole = roleRepository.upsert("dog", "Dog");
        mouseRole = roleRepository.upsert("mouse", "Mouse");

        littleRole = roleRepository.upsert("little", "Little");
        flatDwellerRole = roleRepository.upsert("flat-dweller", "Flat Dweller");

        roleRepository.addRoleToUser(girlRole, maryUser);
        roleRepository.addRoleToUser(littleRole, maryUser);
        roleRepository.addRoleToUser(flatDwellerRole, maryUser);

        roleRepository.addRoleToUser(dogRole, mungoUser);
        roleRepository.addRoleToUser(flatDwellerRole, mungoUser);

        roleRepository.addRoleToUser(mouseRole, midgeUser);
        roleRepository.addRoleToUser(littleRole, midgeUser);
        roleRepository.addRoleToUser(flatDwellerRole, midgeUser);

        ApplicationFeatureId rideLiftFeature = newType("mmm.RideLiftService");

        liftButtonForGirlRolePerm = permissionRepository.newPermission(girlRole, ALLOW, CHANGING, newType("mmm.PressLiftButtonService"));
        rideLiftForGirlRolePerm = permissionRepository.newPermission(girlRole, ALLOW, CHANGING, rideLiftFeature);
        rideLiftForDogRolePerm = permissionRepository.newPermission(dogRole, ALLOW, CHANGING, rideLiftFeature);
        chaseCatsForDogRolePerm = permissionRepository.newPermission(dogRole, ALLOW, CHANGING, newType("mmm.ChaseCatsService"));
        rideLiftForMouseRolePerm = permissionRepository.newPermission(mouseRole, ALLOW, CHANGING, rideLiftFeature);
        eatCheeseForMouseRolePerm = permissionRepository.newPermission(mouseRole, ALLOW, CHANGING, newType("mmm.EatsCheeseService"));
    }

    @Test
    void end_to_end() {

        // when
        val maryPermissions = permissionRepository.findByUser(maryUser);

        // then
        Assertions.assertThat(maryPermissions).containsExactlyInAnyOrder(liftButtonForGirlRolePerm, rideLiftForGirlRolePerm);

        // when
        final List<ApplicationPermission> adhocPermissions = permissionRepository.findByRoleNames(Arrays.asList(girlRole.getName(), mouseRole.getName()));

        // then
        Assertions.assertThat(adhocPermissions).containsExactlyInAnyOrder(liftButtonForGirlRolePerm, rideLiftForGirlRolePerm, rideLiftForMouseRolePerm, eatCheeseForMouseRolePerm);

    }

    @Inject ApplicationPermissionRepository permissionRepository;
    @Inject ApplicationRoleRepository roleRepository;
    @Inject ApplicationUserRepository userRepository;

}