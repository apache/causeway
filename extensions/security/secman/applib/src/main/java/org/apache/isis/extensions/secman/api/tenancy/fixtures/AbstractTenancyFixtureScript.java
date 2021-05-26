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
package org.apache.isis.extensions.secman.api.tenancy.fixtures;

import javax.inject.Inject;

import org.apache.isis.extensions.secman.api.tenancy.dom.ApplicationTenancy;
import org.apache.isis.extensions.secman.api.tenancy.dom.ApplicationTenancyRepository;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

public abstract class AbstractTenancyFixtureScript extends FixtureScript {


    protected ApplicationTenancy create(
            final String name,
            final String path,
            final String parentPath,
            final ExecutionContext executionContext) {

        final ApplicationTenancy parent = parentPath != null
                ? applicationTenancyRepository.findByPath(parentPath)
                : null;
        applicationTenancy = applicationTenancyRepository.newTenancy(name, path, parent);
        executionContext.addResult(this, name, applicationTenancy);
        return applicationTenancy;
    }

    private ApplicationTenancy applicationTenancy;

    /**
     * The {@link ApplicationTenancy} created by this fixture.
     */
    public ApplicationTenancy getApplicationTenancy() {
        return applicationTenancy;
    }

    @Inject private ApplicationTenancyRepository applicationTenancyRepository;

}
