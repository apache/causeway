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

import java.util.Objects;

import org.apache.isis.extensions.secman.api.SecurityModuleConfig;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRule;

public class IsisModuleSecurityAdminRoleAndPermissions extends AbstractRoleAndPermissionsFixtureScript {

    private String[] adminStickyPackagePermissions;

    public IsisModuleSecurityAdminRoleAndPermissions(SecurityModuleConfig configBean) {
        super(configBean.getAdminRoleName(), "Administer security");
        this.adminStickyPackagePermissions = configBean.getAdminStickyPackagePermissions();
    }

    @Override
    protected void execute(ExecutionContext executionContext) {
        newPackagePermissions(
                ApplicationPermissionRule.ALLOW,
                ApplicationPermissionMode.CHANGING,
                adminStickyPackagePermissions);
    }

    public static boolean oneOf(SecurityModuleConfig configBean, String featureFqn) {
        for(String stickyPackage : configBean.getAdminStickyPackagePermissions()) {
            if(Objects.equals(featureFqn, stickyPackage)) {
                return true;
            }
        }
        return false;
    }
}
