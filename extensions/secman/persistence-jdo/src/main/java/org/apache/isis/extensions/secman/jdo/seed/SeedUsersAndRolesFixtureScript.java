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
package org.apache.isis.extensions.secman.jdo.seed;

import javax.inject.Inject;

import org.apache.isis.extensions.fixtures.legacy.fixturescripts.FixtureScript;
import org.apache.isis.extensions.secman.api.SecurityModuleConfig;
import org.apache.isis.extensions.secman.jdo.dom.role.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUserRepository;
import org.apache.isis.extensions.secman.jdo.seed.scripts.GlobalTenancy;
import org.apache.isis.extensions.secman.jdo.seed.scripts.IsisApplibFixtureResultsRoleAndPermissions;
import org.apache.isis.extensions.secman.jdo.seed.scripts.IsisModuleSecurityAdminRoleAndPermissions;
import org.apache.isis.extensions.secman.jdo.seed.scripts.IsisModuleSecurityAdminUser;
import org.apache.isis.extensions.secman.jdo.seed.scripts.IsisModuleSecurityFixtureRoleAndPermissions;
import org.apache.isis.extensions.secman.jdo.seed.scripts.IsisModuleSecurityRegularUserRoleAndPermissions;

/**
 * This fixture script will be run automatically on start-up by virtue of the fact that the
 * {@link org.apache.isis.extensions.secman.jdo.seed.SeedSecurityModuleService} is a
 * {@link org.apache.isis.applib.annotation.DomainService} and calls the setup during its
 * {@link org.apache.isis.extensions.secman.jdo.seed.SeedSecurityModuleService#init() init} 
 * ({@link javax.annotation.PostConstruct}) method.
 */
public class SeedUsersAndRolesFixtureScript extends FixtureScript {

	@Inject SecurityModuleConfig configBean;
    @Inject ApplicationRoleRepository applicationRoleRepository;
    @Inject ApplicationUserRepository applicationUserRepository;
    
    //@Override
    protected void execute(ExecutionContext executionContext) {

        // global tenancy
        executionContext.executeChild(this, new GlobalTenancy());

        // security module
        executionContext.executeChild(this, new IsisModuleSecurityAdminRoleAndPermissions());

        executionContext.executeChild(this, new IsisModuleSecurityFixtureRoleAndPermissions());
        executionContext.executeChild(this, new IsisModuleSecurityRegularUserRoleAndPermissions(configBean));

        executionContext.executeChild(this, new IsisModuleSecurityAdminUser());

        // isis applib
        executionContext.executeChild(this, new IsisApplibFixtureResultsRoleAndPermissions());
    }

    
    
}
