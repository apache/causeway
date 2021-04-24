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
package org.apache.isis.extensions.secman.jdo.seed.scripts;

import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.security.authentication.logout.LogoutMenu;
import org.apache.isis.extensions.secman.api.SecmanConfiguration;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRule;

import lombok.val;

/**
 * Role for regular users of the security module, providing the ability to lookup their user account using the
 * {@link org.apache.isis.extensions.secman.model.dom.user.MeService}, and for viewing and maintaining their user details.
 *
 * @since 2.0 {@index}
 */
public class IsisExtSecmanRegularUserRoleAndPermissions extends AbstractRoleAndPermissionsFixtureScript {

    public IsisExtSecmanRegularUserRoleAndPermissions(SecmanConfiguration configBean) {
        super(configBean.getRegularUserRoleName(), "Regular user of the security module");
    }

    @Override
    protected void execute(ExecutionContext executionContext) {

        val allowViewing = Can.of(
                ApplicationFeatureId.newType("isis.ext.secman.ApplicationUser"),
                ApplicationFeatureId.newMember("isis.ext.secman.ApplicationRole", "name"),
                ApplicationFeatureId.newMember("isis.ext.secman.ApplicationRole", "description")
                );
        
        val allowChanging = Can.of(
                ApplicationFeatureId.newMember(LogoutMenu.OBJECT_TYPE, "logout"),
                ApplicationFeatureId.newMember("isis.ext.secman.MeService", "me"),
                ApplicationFeatureId.newMember("isis.ext.secman.ApplicationUser", "updateName"),
                ApplicationFeatureId.newMember("isis.ext.secman.ApplicationUser", "updatePassword"),
                ApplicationFeatureId.newMember("isis.ext.secman.ApplicationUser", "updateEmailAddress"),
                ApplicationFeatureId.newMember("isis.ext.secman.ApplicationUser", "updatePhoneNumber"),
                ApplicationFeatureId.newMember("isis.ext.secman.ApplicationUser", "updateFaxNumber")
                );
        
        val vetoViewing = Can.of(
                ApplicationFeatureId.newMember("isis.ext.secman.ApplicationUser", "filterPermissions"),
                ApplicationFeatureId.newMember("isis.ext.secman.ApplicationUser", "resetPassword"),
                ApplicationFeatureId.newMember("isis.ext.secman.ApplicationUser", "lock"), // named 'enable' in the UI
                ApplicationFeatureId.newMember("isis.ext.secman.ApplicationUser", "unlock"), // named 'disable' in the UI
                ApplicationFeatureId.newMember("isis.ext.secman.ApplicationUser", "addRole"),
                ApplicationFeatureId.newMember("isis.ext.secman.ApplicationUser", "removeRoles")
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
