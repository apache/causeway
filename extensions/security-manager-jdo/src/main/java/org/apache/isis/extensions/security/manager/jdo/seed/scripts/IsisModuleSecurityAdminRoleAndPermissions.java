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
package org.apache.isis.extensions.security.manager.jdo.seed.scripts;

import java.util.Objects;

import org.apache.isis.extensions.security.manager.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.security.manager.api.permission.ApplicationPermissionRule;

public class IsisModuleSecurityAdminRoleAndPermissions extends AbstractRoleAndPermissionsFixtureScript {

    public static final String ROLE_NAME = "isis-module-security-admin";
    public static final String ORG_ISISADDONS_MODULE_SECURITY_APP = "org.apache.isis.extensions.security.manager.jdo.app";
    public static final String ORG_ISISADDONS_MODULE_SECURITY_DOM = "org.apache.isis.extensions.security.manager.jdo.dom";

    public IsisModuleSecurityAdminRoleAndPermissions() {
        super(ROLE_NAME, "Administer security");
    }


    @Override
    protected void execute(ExecutionContext executionContext) {
        newPackagePermissions(
                ApplicationPermissionRule.ALLOW,
                ApplicationPermissionMode.CHANGING,
                ORG_ISISADDONS_MODULE_SECURITY_APP,
                ORG_ISISADDONS_MODULE_SECURITY_DOM);
    }

    public static boolean oneOf(String featureFqn) {
        return Objects.equals(featureFqn, ORG_ISISADDONS_MODULE_SECURITY_APP) ||
               Objects.equals(featureFqn, ORG_ISISADDONS_MODULE_SECURITY_DOM);
    }
}
