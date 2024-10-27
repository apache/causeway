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
package org.apache.causeway.extensions.secman.applib.role.fixtures;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionMode;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionRepository;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionRule;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRoleRepository;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScriptWithExecutionStrategy;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScripts;

/**
 * Convenience fixture script intended to be easily subclassed in order to set up an
 * {@link org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole} with associated permissions.
 *
 * @since 2.x {@index}
 */
public abstract class AbstractRoleAndPermissionsFixtureScript
extends FixtureScript
implements FixtureScriptWithExecutionStrategy {

    @Inject private ApplicationRoleRepository applicationRoleRepository;
    @Inject private ApplicationPermissionRepository applicationPermissionRepository;
    @Inject private ApplicationFeatureRepository applicationFeatureRepository;
    @Inject private CausewayConfiguration causewayConfiguration;

    private final Supplier<String> roleNameSupplier;
    private final Supplier<String> roleDescriptionSupplier;

    protected AbstractRoleAndPermissionsFixtureScript(
            final String roleName,
            final String roleDescriptionIfAny) {
        this(() -> roleName, () -> roleDescriptionIfAny);
    }

    protected AbstractRoleAndPermissionsFixtureScript(
            final Supplier<String> roleNameSupplier,
            final Supplier<String> roleDescriptionSupplier) {
        this.roleNameSupplier = nullSafe(roleNameSupplier);
        this.roleDescriptionSupplier = nullSafe(roleDescriptionSupplier);
    }

    @Override
    public FixtureScripts.MultipleExecutionStrategy getMultipleExecutionStrategy() {
        return null;
    }

    protected final String getRoleName() {
        return roleNameSupplier.get();
    }

    protected final String getRoleDescription() {
        return roleDescriptionSupplier.get();
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

        if(featureIds == null || featureIds.isEmpty()) {
            return;
        }

        final List<ApplicationFeatureId> missingFeatureIds = new ArrayList<>();

        for(var featureId : featureIds) {
            var feature = applicationFeatureRepository.findFeature(featureId);
            if (feature == null) {
                missingFeatureIds.add(featureId);
            }
        }

        if (causewayConfiguration.getExtensions().getSecman().getFixtureScripts().getAbstractRoleAndPermissionsFixtureScript().getUnknownFeatureIdCheckingPolicy().isFailFast()) {
            // ensure all featureIds specified actually exist.
            var buf = new StringBuilder();
            for(var featureId : missingFeatureIds) {
                buf.append("- ").append(featureId.getFullyQualifiedName()).append("\n");
            }
            throw new IllegalArgumentException(String.format("No such feature(s):\n%s", buf));
        }

        var roleName = getRoleName();
        var securityRole = applicationRoleRepository.findByName(roleName)
                .orElseGet(() -> applicationRoleRepository.newRole(roleName, getRoleDescription()));

        for(var featureId : featureIds) {
            var featureFqn = featureId.getFullyQualifiedName();

            // can't use role#addPackage because that does a check for existence of the package, which is
            // not guaranteed to exist yet (the SecurityFeatures#init() may not have run).
            applicationPermissionRepository
            .newPermissionNoCheck(
                    securityRole,
                    rule,
                    mode,
                    featureId.getSort(),
                    featureFqn);
        }
    }

    private static <T> Supplier<T> nullSafe(final Supplier<T> supplier) {
        return supplier != null ? supplier : () -> null;
    }

}
