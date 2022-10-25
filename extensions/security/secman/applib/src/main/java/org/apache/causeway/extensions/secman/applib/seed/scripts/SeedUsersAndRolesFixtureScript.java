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
package org.apache.causeway.extensions.secman.applib.seed.scripts;

import javax.inject.Inject;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayAppFeatureRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayConfigurationRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayExtAuditTrailRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayExtCommandLogRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayExtExecutionLogRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayExtExecutionOutboxRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayExtH2ConsoleRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayExtSecmanAdminRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayExtSecmanRegularUserRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayExtSessionLogRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayPersistenceJdoMetaModelRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewaySudoImpersonateRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayViewerRestfulObjectsSwaggerRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.seed.SeedSecurityModuleService;
import org.apache.causeway.extensions.secman.applib.tenancy.seed.GlobalTenancy;
import org.apache.causeway.extensions.secman.applib.user.seed.CausewayExtSecmanAdminUser;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;

import lombok.val;

/**
 * Sets up roles and permissions for both Secman itself and also for all other modules that expose UI features
 * for use by end-users.
 *
 * <p>
 * This fixture script is run automatically on start-up by the {@link SeedSecurityModuleService}.
 * </p>
 *
 * @see SeedSecurityModuleService
 *
 * @since 2.0 {@index}
 */
public class SeedUsersAndRolesFixtureScript extends FixtureScript {

    @Inject private CausewayConfiguration config;

    @Override
    protected void execute(final ExecutionContext executionContext) {

        val secmanConfig = config.getExtensions().getSecman();

        // global tenancy
        executionContext.executeChild(this, new GlobalTenancy());

        // modules
        executionContext.executeChildren(this,
                new CausewayAppFeatureRoleAndPermissions(),
                new CausewayPersistenceJdoMetaModelRoleAndPermissions(),
                new CausewayExtAuditTrailRoleAndPermissions(),
                new CausewayExtCommandLogRoleAndPermissions(),
                new CausewayExtExecutionLogRoleAndPermissions(),
                new CausewayExtExecutionOutboxRoleAndPermissions(),
                new CausewayExtSessionLogRoleAndPermissions(),
                new CausewayExtH2ConsoleRoleAndPermissions(),
                new CausewayViewerRestfulObjectsSwaggerRoleAndPermissions(),
                new CausewaySudoImpersonateRoleAndPermissions(),
                new CausewayConfigurationRoleAndPermissions()
                );

        // secman module (admin and regular users role, and secman-admin superuser)
        executionContext.executeChildren(this,
                new CausewayExtSecmanAdminRoleAndPermissions(secmanConfig),
                new CausewayExtSecmanRegularUserRoleAndPermissions(secmanConfig),
                new CausewayExtSecmanAdminUser(secmanConfig,
                        CausewayAppFeatureRoleAndPermissions.ROLE_NAME,
                        CausewayPersistenceJdoMetaModelRoleAndPermissions.ROLE_NAME,
                        CausewayExtAuditTrailRoleAndPermissions.ROLE_NAME,
                        CausewayExtCommandLogRoleAndPermissions.ROLE_NAME,
                        CausewayExtExecutionLogRoleAndPermissions.ROLE_NAME,
                        CausewayExtExecutionOutboxRoleAndPermissions.ROLE_NAME,
                        CausewayExtSessionLogRoleAndPermissions.ROLE_NAME,
                        CausewayExtH2ConsoleRoleAndPermissions.ROLE_NAME,
                        CausewayViewerRestfulObjectsSwaggerRoleAndPermissions.ROLE_NAME,
                        CausewaySudoImpersonateRoleAndPermissions.ROLE_NAME,
                        CausewayConfigurationRoleAndPermissions.ROLE_NAME)
                );

    }

}
