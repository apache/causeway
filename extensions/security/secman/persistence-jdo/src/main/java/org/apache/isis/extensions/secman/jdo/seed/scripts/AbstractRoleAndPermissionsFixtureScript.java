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

import javax.inject.Inject;

import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermissionRepository;
import org.apache.isis.extensions.secman.jdo.dom.role.ApplicationRoleRepository;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import lombok.val;

public abstract class AbstractRoleAndPermissionsFixtureScript extends FixtureScript {

    @Inject private ApplicationRoleRepository applicationRoleRepository;
    @Inject private ApplicationPermissionRepository applicationPermissionRepository;

    private final String roleName;
    private final String roleDescription;

    protected AbstractRoleAndPermissionsFixtureScript(
            final String roleName,
            final String roleDescriptionIfAny) {
        this.roleName = roleName;
        this.roleDescription = roleDescriptionIfAny;
    }

    /**
     * Subclasses should override and call
     * {@link #newPermissions(ApplicationPermissionRule, ApplicationPermissionMode, Can)}
     */
    @Override
    protected abstract void execute(ExecutionContext executionContext);

    /**
     * For subclasses to call in {@link #execute(FixtureScript.ExecutionContext)}.
     */
    protected void newPermissions(
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final Can<ApplicationFeatureId> featureIds) {

        if(featureIds == null
                || featureIds.isEmpty()) {
            return;
        }

        val securityRole = applicationRoleRepository.findByName(roleName)
                .orElseGet(() -> applicationRoleRepository.newRole(roleName, roleDescription));

        for(val featureId : featureIds) {
            val featureFqn = featureId.getFullyQualifiedName();

            // can't use role#addPackage because that does a check for existence of the package, which is
            // not guaranteed to exist yet (the SecurityFeatures#init() may not have run).
            ((org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermissionRepository)
                    applicationPermissionRepository)
            .newPermissionNoCheck(
                    (org.apache.isis.extensions.secman.jdo.dom.role.ApplicationRole)securityRole,
                    rule,
                    mode,
                    featureId.getSort(),
                    featureFqn);
        }
    }


}
