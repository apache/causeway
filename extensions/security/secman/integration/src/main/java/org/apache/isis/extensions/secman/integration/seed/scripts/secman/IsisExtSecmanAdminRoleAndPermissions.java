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
package org.apache.isis.extensions.secman.integration.seed.scripts.secman;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.extensions.secman.api.SecmanConfiguration;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.api.role.fixtures.AbstractRoleAndPermissionsFixtureScript;

/**
 * Sets up the {@link SecmanConfiguration#getAdminRoleName() secman admin role}
 * with its initial set of permissions (the union of
 * {@link SecmanConfiguration#getAdminStickyNamespacePermissions()}
 * and {@link SecmanConfiguration#getAdminAdditionalNamespacePermissions()}).
 *
 * @see SecmanConfiguration
 *
 * @since 2.0 {@index}
 */
public class IsisExtSecmanAdminRoleAndPermissions extends AbstractRoleAndPermissionsFixtureScript {

    private final List<String> adminInitialPackagePermissions;

    public IsisExtSecmanAdminRoleAndPermissions(SecmanConfiguration configBean) {
        super(configBean.getAdminRoleName(), "Administer security");
        this.adminInitialPackagePermissions = configBean.streamAdminNamespacePermissions()
                .collect(Collectors.toList());
    }

    @Override
    protected void execute(ExecutionContext executionContext) {
        newPermissions(
                ApplicationPermissionRule.ALLOW,
                ApplicationPermissionMode.CHANGING,
                Can.ofCollection(adminInitialPackagePermissions)
                        .map(ApplicationFeatureId::newNamespace));
    }

}
