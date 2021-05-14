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
package org.apache.isis.extensions.secman.model.seed.scripts.secman;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.security.authentication.logout.LogoutMenu;
import org.apache.isis.extensions.secman.api.SecmanConfiguration;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.fixtures.AbstractRoleAndPermissionsFixtureScript;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.menu.MeService;
import org.apache.isis.extensions.secman.model.seed.scripts.other.IsisConfigurationRoleAndPermissions;
import org.apache.isis.testing.fixtures.applib.IsisModuleTestingFixturesApplib;

import lombok.val;

/**
 * Role for regular users of the security module.
 *
 * <p>
 * These permissions are intended to be sufficient for most regular users
 * to have access to the 'safe' features provided by the core framework
 * (in particular, everything under the {@link IsisModuleApplib#NAMESPACE isis.applib}
 * namespace.
 * </p>
 *
 * <p>
 * That said, it does <i>not</i> include the ability to impersonate other users
 * (for this, grant the
 * {@link org.apache.isis.extensions.secman.model.seed.scripts.other.IsisSudoImpersonateRoleAndPermissions}
 * role), and also does <i>not</i> include the ability to access the
 * configuration properties (for this, grant the
 * {@link org.apache.isis.extensions.secman.model.seed.scripts.other.IsisSudoImpersonateRoleAndPermissions}
 * role).
 * </p>
 *
 * <p>
 * The permissions also provide the ability to lookup their user account using
 * the {@link MeService}, and for viewing and maintaining their user details.
 * </p>
 *
 * @see org.apache.isis.extensions.secman.model.seed.scripts.other.IsisSudoImpersonateRoleAndPermissions
 * @see IsisConfigurationRoleAndPermissions
 *
 * @since 2.0 {@index}
 */
public class IsisExtSecmanRegularUserRoleAndPermissions extends AbstractRoleAndPermissionsFixtureScript {

    public IsisExtSecmanRegularUserRoleAndPermissions(SecmanConfiguration configBean) {
        super(configBean.getRegularUserRoleName(), "Regular user of the security module");
    }

    @Override
    protected void execute(ExecutionContext executionContext) {

        val allowChanging = Can.of(
                // everything under "isis.applib" is granted.
                // this includes prototype actions for metamodel and translations
                ApplicationFeatureId.newNamespace(IsisModuleApplib.NAMESPACE),

                // we also provide default access to run fixtures (prototype action only)
                ApplicationFeatureId.newNamespace(IsisModuleTestingFixturesApplib.NAMESPACE),

                // also the ability to logout (!)
                ApplicationFeatureId.newType(LogoutMenu.OBJECT_TYPE),

                // remaining permissions give access to the user to maintain their details
                ApplicationFeatureId.newType(MeService.OBJECT_TYPE),
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "updateName"),
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "updatePassword"),
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "updateEmailAddress"),
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "updatePhoneNumber"),
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "updateFaxNumber")

        );

        val allowViewing = Can.of(
                // we also allow the user to see the roles they are in
                // (but nothing more than that)
                ApplicationFeatureId.newType(ApplicationUser.OBJECT_TYPE),
                ApplicationFeatureId.newMember(ApplicationRole.OBJECT_TYPE, "name"),
                ApplicationFeatureId.newMember(ApplicationRole.OBJECT_TYPE, "description")
                );

        val vetoViewing = Can.of(
                // we explicitly ensure that the user cannot grant themselves
                // additional privileges or see stuff that they shouldn't
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "filterPermissions"),
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "resetPassword"),
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "lock"),
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "unlock"),
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "addRole"),
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "removeRoles")
        );

        newPermissions(
                ApplicationPermissionRule.ALLOW,
                ApplicationPermissionMode.VIEWING,
                allowViewing);

        newPermissions(
                ApplicationPermissionRule.ALLOW,
                ApplicationPermissionMode.CHANGING,
                allowChanging);

        newPermissions(
                ApplicationPermissionRule.VETO,
                ApplicationPermissionMode.VIEWING,
                vetoViewing);

    }

}
