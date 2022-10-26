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
package org.apache.causeway.extensions.secman.applib.user.dom.mixins.perms;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionMode;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionRule;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionValue;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionValueSet;
import org.apache.causeway.extensions.secman.applib.permission.spi.PermissionsEvaluationService;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;

import lombok.val;

class UserPermissionViewModelTest {

    @ParameterizedTest
    @ValueSource(strings = {"sven", "!@$5:", ""}) // username candidates
    void roundtrip(final String username) {

        val featureId = ApplicationFeatureId
                .fromIdentifier(Identifier.classIdentifier(LogicalType.fqcn(this.getClass())));

        val permissionsEvaluationService = mock(PermissionsEvaluationService.class);

        val permissions = List.of(new ApplicationPermissionValue(featureId,
                ApplicationPermissionRule.ALLOW,
                ApplicationPermissionMode.CHANGING));

        val user = mock(ApplicationUser.class);
        when(user.getUsername()).thenReturn(username);
        when(user.getPermissionSet()).thenReturn(new ApplicationPermissionValueSet(
                permissions,
                permissionsEvaluationService));

        val factoryService = MetaModelContext_forTesting.buildDefault()
                .getFactoryService();

        val vm = UserPermissionViewModel.asViewModel(user, factoryService)
                .apply(featureId);

        // when
        val memento = vm.viewModelMemento();
        val vmAfterRoundtrip = new UserPermissionViewModel(memento);

        // then
        assertEquals(username, vmAfterRoundtrip.getUsername());
    }

}
