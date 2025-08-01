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
package org.apache.causeway.extensions.secman.applib.role.seed;

import jakarta.inject.Inject;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.Secman;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.security.CausewayModuleCoreSecurity;
import org.apache.causeway.core.security.authentication.logout.LogoutMenu;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionMode;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionRule;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.causeway.extensions.secman.applib.role.fixtures.AbstractRoleAndPermissionsFixtureScript;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.user.menu.MeService;
import org.apache.causeway.testing.fixtures.applib.CausewayModuleTestingFixturesApplib;

/**
 * Sets up a role for regular users of the security module.
 *
 * <p>These permissions are intended to be sufficient for most regular users
 * to have access to the 'safe' features provided by the core framework
 * (in particular, everything under the {@link CausewayModuleApplib#NAMESPACE causeway.applib}
 * namespace.
 *
 * <p>That said, it does <i>not</i> include the ability to impersonate other users
 * (for this, grant the
 * {@link CausewaySudoImpersonateRoleAndPermissions}
 * role), and also does <i>not</i> include the ability to access the
 * configuration properties (for this, grant the
 * {@link CausewaySudoImpersonateRoleAndPermissions}
 * role).
 *
 * <p>The permissions also provide the ability to lookup their user account using
 * the {@link MeService}, and for viewing and maintaining their user details.
 *
 * @see CausewaySudoImpersonateRoleAndPermissions
 * @see CausewayConfigurationRoleAndPermissions
 *
 * @since 2.0 {@index}
 */
public class CausewayExtSecmanRegularUserRoleAndPermissions extends AbstractRoleAndPermissionsFixtureScript {

    @Inject private CausewaySystemEnvironment env;

    public CausewayExtSecmanRegularUserRoleAndPermissions(final Secman config) {
        super(config.seed().regularUser().roleName(), "Regular user of the security module");
    }

    @Override
    protected void execute(final ExecutionContext executionContext) {

        var allowChanging = Can.of(
                // everything under "causeway.applib" is granted.
                // this includes prototype actions for metamodel and translations
                ApplicationFeatureId.newNamespace(CausewayModuleApplib.NAMESPACE),

                // we also provide default access to run fixtures (prototype action only)
                ApplicationFeatureId.newNamespace(CausewayModuleTestingFixturesApplib.NAMESPACE),

                // we also provide default access to logout action (!)
                ApplicationFeatureId.newNamespace(CausewayModuleCoreSecurity.NAMESPACE),

                // also the ability to logout (!)
                ApplicationFeatureId.newType(LogoutMenu.LOGICAL_TYPE_NAME),

                // remaining permissions give access to the user to maintain their details
                ApplicationFeatureId.newType(MeService.LOGICAL_TYPE_NAME),
                ApplicationFeatureId.newMember(ApplicationUser.LOGICAL_TYPE_NAME, "updateName"),
                ApplicationFeatureId.newMember(ApplicationUser.LOGICAL_TYPE_NAME, "updatePassword"),
                ApplicationFeatureId.newMember(ApplicationUser.LOGICAL_TYPE_NAME, "updateEmailAddress"),
                ApplicationFeatureId.newMember(ApplicationUser.LOGICAL_TYPE_NAME, "updatePhoneNumber"),
                ApplicationFeatureId.newMember(ApplicationUser.LOGICAL_TYPE_NAME, "updateFaxNumber"),

                // optionally allow access to documentation menu and pages, based on module presence
                env.springContextHolder().containsBean("org.apache.causeway.extensions.docgen.help.CausewayModuleExtDocgenHelp")
                    ? ApplicationFeatureId.newNamespace(CausewayExtDocgenRoleAndPermissions.NAMESPACE)
                    : null,

                // optionally allow access to layout menu and pages, based on module presence
                env.springContextHolder().containsBean("org.apache.causeway.extensions.layoutloaders.github.CausewayModuleExtLayoutLoadersGithub")
                    ? ApplicationFeatureId.newNamespace(CausewayExtLayoutLoadersRoleAndPermissions.NAMESPACE)
                    : null
        );

        var allowViewing = Can.of(
                // we also allow the user to see the roles they are in
                // (but nothing more than that)
                ApplicationFeatureId.newType(ApplicationUser.LOGICAL_TYPE_NAME),
                ApplicationFeatureId.newMember(ApplicationRole.LOGICAL_TYPE_NAME, "name"),
                ApplicationFeatureId.newMember(ApplicationRole.LOGICAL_TYPE_NAME, "description")
                );

        newPermissions(
                ApplicationPermissionRule.ALLOW,
                ApplicationPermissionMode.VIEWING,
                allowViewing);

        newPermissions(
                ApplicationPermissionRule.ALLOW,
                ApplicationPermissionMode.CHANGING,
                allowChanging);

    }

}
