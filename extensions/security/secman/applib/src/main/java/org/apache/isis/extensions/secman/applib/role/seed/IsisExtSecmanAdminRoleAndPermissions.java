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
package org.apache.isis.extensions.secman.applib.role.seed;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.config.IsisConfiguration.Extensions.Secman;
import org.apache.isis.core.config.IsisConfiguration.Extensions.Secman.Seed.Admin;
import org.apache.isis.core.config.IsisConfiguration.Extensions.Secman.Seed.Admin.NamespacePermissions;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.applib.role.fixtures.AbstractRoleAndPermissionsFixtureScript;

import lombok.val;

/**
 * Sets up the {@link Admin#getRoleName() secman admin role}
 * with its initial set of permissions (the union of
 * {@link NamespacePermissions#getSticky()}
 * and {@link NamespacePermissions#getAdditional()}).
 *
 * @see Secman
 *
 * @since 2.0 {@index}
 */
public class IsisExtSecmanAdminRoleAndPermissions extends AbstractRoleAndPermissionsFixtureScript {

    private final Set<String> adminInitialPackagePermissions;

    public IsisExtSecmanAdminRoleAndPermissions(final Secman config) {
        super(config.getSeed().getAdmin().getRoleName(), "Administer security");
        this.adminInitialPackagePermissions = streamAdminNamespacePermissions(config)
                .collect(Collectors.toCollection(LinkedHashSet::new)); // preserve order, discard duplicates
    }

    @Override
    protected void execute(final ExecutionContext executionContext) {
        newPermissions(
                ApplicationPermissionRule.ALLOW,
                ApplicationPermissionMode.CHANGING,
                Can.ofCollection(adminInitialPackagePermissions)
                        .map(ApplicationFeatureId::newNamespace));
    }

    // -- HELPER

    private static Stream<String> streamAdminNamespacePermissions(final Secman secman) {
        val adminNamespacePermissions = secman.getSeed().getAdmin().getNamespacePermissions();
        return Stream.concat(
                _NullSafe.stream(adminNamespacePermissions.getSticky()),
                _NullSafe.stream(adminNamespacePermissions.getAdditional()));
    }

}
