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
package org.apache.causeway.extensions.secman.jdo;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.extensions.secman.integration.CausewayModuleExtSecmanIntegration;
import org.apache.causeway.extensions.secman.jdo.permission.dom.ApplicationPermission;
import org.apache.causeway.extensions.secman.jdo.permission.dom.ApplicationPermissionRepository;
import org.apache.causeway.extensions.secman.jdo.role.dom.ApplicationRole;
import org.apache.causeway.extensions.secman.jdo.role.dom.ApplicationRoleRepository;
import org.apache.causeway.extensions.secman.jdo.tenancy.dom.ApplicationTenancy;
import org.apache.causeway.extensions.secman.jdo.tenancy.dom.ApplicationTenancyRepository;
import org.apache.causeway.extensions.secman.jdo.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.jdo.user.dom.ApplicationUserRepository;
import org.apache.causeway.extensions.secman.jdo.util.RegexReplacer;
import org.apache.causeway.persistence.jdo.datanucleus.CausewayModulePersistenceJdoDatanucleus;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.causeway.testing.fixtures.applib.modules.ModuleWithFixtures;
import org.apache.causeway.testing.fixtures.applib.teardown.jdo.TeardownFixtureJdoAbstract;

/**
 * @since 2.0 {@index}
 */
@Configuration
@Import({
        // Modules
        CausewayModuleExtSecmanIntegration.class,
        CausewayModulePersistenceJdoDatanucleus.class,

        // services
        ApplicationPermissionRepository.class,
        ApplicationRoleRepository.class,
        ApplicationTenancyRepository.class,
        ApplicationUserRepository.class,
        RegexReplacer.class,

        // entities, eager meta-model inspection
        ApplicationPermission.class,
        ApplicationRole.class,
        ApplicationTenancy.class,
        ApplicationUser.class,

})
public class CausewayModuleExtSecmanPersistenceJdo {

    /**
     * Note that this is <i>NOT</i> an implementation of the {@link ModuleWithFixtures#getTeardownFixture()} API;
     * but is provided to allow manual teardown if required.
     */
    public FixtureScript teardownFixture() {
        return new TeardownFixtureJdoAbstract() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                deleteFrom(ApplicationPermission.class);
                deleteFrom(ApplicationUser.class);
                deleteFrom(ApplicationRole.class);
                deleteFrom(ApplicationTenancy.class);
            }
        };
    }

}
