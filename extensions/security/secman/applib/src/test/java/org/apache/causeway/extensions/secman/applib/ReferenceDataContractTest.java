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

import java.util.List;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.domain.RefData;
import org.apache.causeway.extensions.secman.applib.feature.api.ApplicationFeatureChoices;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermission;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancy;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceDataContractTest {

    @Test
    void designatedSecmanAbstractionsDeclareStableReferenceData() {
        assertThat(List.of(
                ApplicationUser.class,
                ApplicationRole.class,
                ApplicationTenancy.class,
                ApplicationPermission.class))
                .allMatch(RefData.class::isAssignableFrom);
    }

    @Test
    void permissionFeatureReferenceViewModelIsStableReferenceData() {
        // AppFeat is the reference view-model for permission-feature choices; commands whose target or
        // reference parameter is an AppFeat bookmark must be recognised as export participants.
        assertThat(RefData.class.isAssignableFrom(ApplicationFeatureChoices.AppFeat.class)).isTrue();
    }

    @Test
    void markerAddsNoPersistentStructure() {
        assertThat(RefData.class.getDeclaredMethods()).isEmpty();
        assertThat(RefData.class.getDeclaredFields()).isEmpty();
        assertThat(RefData.class.getDeclaredAnnotations()).isEmpty();
    }
}
