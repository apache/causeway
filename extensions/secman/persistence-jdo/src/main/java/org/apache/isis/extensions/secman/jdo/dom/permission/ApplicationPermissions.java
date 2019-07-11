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
package org.apache.isis.extensions.secman.jdo.dom.permission;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.extensions.secman.api.SecurityModule;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValue;
import org.apache.isis.extensions.secman.jdo.dom.role.ApplicationRole;
import org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeatureId;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeatureType;

/**
 * @deprecated - use {@link ApplicationPermissionRepository} or {@link ApplicationPermissionMenu}.
 */
@Deprecated
@DomainService(nature = NatureOfService.DOMAIN)
public class ApplicationPermissions {

    // -- domain event classes

    public static abstract class PropertyDomainEvent<T> extends SecurityModule.PropertyDomainEvent<ApplicationPermissions, T> {}

    public static abstract class CollectionDomainEvent<T> extends SecurityModule.CollectionDomainEvent<ApplicationPermissions, T> {}

    public static abstract class ActionDomainEvent extends SecurityModule.ActionDomainEvent<ApplicationPermissions> {}
    

    // -- iconName

    public String iconName() {
        return "applicationPermission";
    }

    

    // -- findByRole (programmatic)

    /**
     * @deprecated - use {@link ApplicationPermissionRepository#findByRoleCached(ApplicationRole)} or {@link ApplicationPermissionRepository#findByRole(ApplicationRole)} instead.
     */
    @Deprecated
    @Programmatic
    public List<ApplicationPermission> findByRole(final ApplicationRole role) {
        return applicationPermissionRepository.findByRole(role);
    }
    

    // -- findByUser (programmatic)

    /**
     * @deprecated - use {@link ApplicationPermissionRepository#findByUserCached(ApplicationUser)} or {@link ApplicationPermissionRepository#findByUser(ApplicationUser)} instead.
     */
    @Deprecated
    @Programmatic
    public List<ApplicationPermission> findByUser(final ApplicationUser user) {
        return applicationPermissionRepository.findByUser(user);
    }

    

    // -- findByUserAndPermissionValue (programmatic)
    /**
     * @deprecated - use {@link ApplicationPermissionRepository#findByUserAndPermissionValue(String, ApplicationPermissionValue)} instead.
     */
    @Deprecated
    @Programmatic
    public ApplicationPermission findByUserAndPermissionValue(final String username, final ApplicationPermissionValue permissionValue) {
        return applicationPermissionRepository.findByUserAndPermissionValue(username, permissionValue);
    }
    

    // -- findByRoleAndRuleAndFeatureType (programmatic)

    /**
     * @deprecated - use {@link ApplicationPermissionRepository#findByRoleAndRuleAndFeatureTypeCached(ApplicationRole, ApplicationPermissionRule, ApplicationFeatureType)} or {@link ApplicationPermissionRepository#findByRoleAndRuleAndFeatureType(ApplicationRole, ApplicationPermissionRule, ApplicationFeatureType)} instead.
     */
    @Deprecated
    @Programmatic
    public List<ApplicationPermission> findByRoleAndRuleAndFeatureType(
            final ApplicationRole role, final ApplicationPermissionRule rule,
            final ApplicationFeatureType type) {
        return applicationPermissionRepository.findByRoleAndRuleAndFeatureType(role, rule, type);
    }
    

    // -- findByRoleAndRuleAndFeature (programmatic)

    /**
     * @deprecated - use {@link ApplicationPermissionRepository#findByRoleAndRuleAndFeatureCached(ApplicationRole, ApplicationPermissionRule, ApplicationFeatureType, String)} or {@link ApplicationPermissionRepository#findByRoleAndRuleAndFeature(ApplicationRole, ApplicationPermissionRule, ApplicationFeatureType, String)} instead.
     */
    @Deprecated
    @Programmatic
    public ApplicationPermission findByRoleAndRuleAndFeature(final ApplicationRole role, final ApplicationPermissionRule rule, final ApplicationFeatureType type, final String featureFqn) {
        return applicationPermissionRepository.findByRoleAndRuleAndFeature(role, rule, type, featureFqn);
    }
    

    // -- findByFeature (programmatic)

    /**
     * @deprecated - use {@link ApplicationPermissionRepository#findByFeatureCached(ApplicationFeatureId)} or {@link ApplicationPermissionRepository#findByFeature(ApplicationFeatureId)} instead.
     */
    @Deprecated
    @Programmatic
    public List<ApplicationPermission> findByFeature(final ApplicationFeatureId featureId) {
        return applicationPermissionRepository.findByFeature(featureId);
    }
    

    // -- newPermission (programmatic)

    /**
     * @deprecated - use {@link ApplicationPermissionRepository#newPermission(ApplicationRole, ApplicationPermissionRule, ApplicationPermissionMode, ApplicationFeatureType, String)} instead.
     */
    @Deprecated
    @Programmatic
    public ApplicationPermission newPermission(
            final ApplicationRole role,
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final ApplicationFeatureType featureType,
            final String featureFqn) {
        return applicationPermissionRepository.newPermission(role, rule, mode, featureType, featureFqn);
    }

    /**
     * @deprecated - use {@link ApplicationPermissionRepository#newPermissionNoCheck(ApplicationRole, ApplicationPermissionRule, ApplicationPermissionMode, ApplicationFeatureType, String)} instead.
     */
    @Deprecated
    @Programmatic
    public ApplicationPermission newPermissionNoCheck(
            final ApplicationRole role,
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final ApplicationFeatureType featureType,
            final String featureFqn) {
        return applicationPermissionRepository.newPermissionNoCheck(role, rule, mode, featureType, featureFqn);
    }

    /**
     * @deprecated - use {@link ApplicationPermissionRepository#newPermission(ApplicationRole, ApplicationPermissionRule, ApplicationPermissionMode, String, String, String)} instead.
     */
    @Deprecated
    @Programmatic
    public ApplicationPermission newPermission(
            final ApplicationRole role,
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final String featurePackage,
            final String featureClassName,
            final String featureMemberName) {
        return applicationPermissionRepository.newPermission(role, rule, mode, featurePackage, featureClassName, featureMemberName);
    }
    

    // -- allPermission (action)
    public static class AllPermissionsDomainEvent extends ActionDomainEvent {}

    /**
     * @deprecated - use {@link ApplicationPermissionMenu#allPermissions()}
     */
    @Deprecated
    @Action(
            domainEvent=AllPermissionsDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
    )
    public List<ApplicationPermission> allPermissions() {
        return applicationPermissionRepository.allPermissions();
    }
    

    //region  > injected
    @Inject
    ApplicationPermissionRepository applicationPermissionRepository;
    
}
