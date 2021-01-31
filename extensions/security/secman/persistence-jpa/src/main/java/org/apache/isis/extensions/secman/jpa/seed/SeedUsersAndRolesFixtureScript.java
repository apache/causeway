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
package org.apache.isis.extensions.secman.jpa.seed;

import javax.inject.Inject;

import org.apache.isis.extensions.secman.api.SecmanConfiguration;
import org.apache.isis.extensions.secman.jpa.seed.scripts.GlobalTenancy;
import org.apache.isis.extensions.secman.jpa.seed.scripts.IsisExtFixturesFixtureResultsRoleAndPermissions;
import org.apache.isis.extensions.secman.jpa.seed.scripts.IsisExtSecmanAdminRoleAndPermissions;
import org.apache.isis.extensions.secman.jpa.seed.scripts.IsisExtSecmanAdminUser;
import org.apache.isis.extensions.secman.jpa.seed.scripts.IsisExtSecmanFixtureRoleAndPermissions;
import org.apache.isis.extensions.secman.jpa.seed.scripts.IsisExtSecmanRegularUserRoleAndPermissions;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

/**
 * This fixture script will be run automatically on start-up by virtue of the fact that the
 * {@link org.apache.isis.extensions.secman.jpa.seed.SeedSecurityModuleService} is a
 * {@link org.apache.isis.applib.annotation.DomainService} and calls the setup during its
 * {@link org.apache.isis.extensions.secman.jpa.seed.SeedSecurityModuleService#init() init}
 * ({@link javax.annotation.PostConstruct}) method.
 *
 * @since 2.0 {@index}
 */
public class SeedUsersAndRolesFixtureScript extends FixtureScript {

    @Inject private SecmanConfiguration configBean;

    @Override
    protected void execute(ExecutionContext executionContext) {

        // global tenancy
        executionContext.executeChild(this, new GlobalTenancy());

        // security module
        executionContext.executeChild(this, new IsisExtSecmanAdminRoleAndPermissions(configBean));

        executionContext.executeChild(this, new IsisExtSecmanFixtureRoleAndPermissions(configBean));
        executionContext.executeChild(this, new IsisExtSecmanRegularUserRoleAndPermissions(configBean));

        executionContext.executeChild(this, new IsisExtSecmanAdminUser(configBean));

        // isis applib
        executionContext.executeChild(this, new IsisExtFixturesFixtureResultsRoleAndPermissions());
    }



}
