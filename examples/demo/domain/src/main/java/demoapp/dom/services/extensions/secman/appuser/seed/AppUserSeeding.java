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
package demoapp.dom.services.extensions.secman.appuser.seed;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.extensions.secman.applib.role.fixtures.AbstractRoleAndPermissionsFixtureScript;
import org.apache.isis.extensions.secman.applib.user.dom.AccountType;
import org.apache.isis.extensions.secman.applib.user.fixtures.AbstractUserAndRolesFixtureScript;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import static org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionMode.CHANGING;
import static org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionRule.ALLOW;

import demoapp.dom._infra.seed.SeedServiceAbstract;

@Service
public class AppUserSeeding
extends SeedServiceAbstract {

    protected AppUserSeeding() {
        super(() -> new FixtureScript() {
            @Override protected void execute(final ExecutionContext executionContext) {
                executionContext.executeChildren(this,
                        newRole("demo", Can.of(ApplicationFeatureId.newNamespace("demo"))),
                        newUser("bob", Can.of("isis-ext-secman-admin", "demo"))
                    );
            }

        });
    }

    private static AbstractRoleAndPermissionsFixtureScript newRole(
            final String roleName,
            final Can<ApplicationFeatureId> permissions) {
        return new AbstractRoleAndPermissionsFixtureScript(roleName, null) {
            @Override protected void execute(final ExecutionContext executionContext) {
                newPermissions(ALLOW, CHANGING, permissions);
            }
        };
    }
    private static AbstractUserAndRolesFixtureScript newUser(
            final String username,
            final Can<String> roleNames) {
        return new AbstractUserAndRolesFixtureScript(
                username, "pass", AccountType.LOCAL,
                roleNames) {
        };
    }

}
