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
package org.apache.isis.extensions.secman.applib.permission.dom;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser;

/**
 * @since 2.0 {@index}
 */
public interface ApplicationPermissionRepository {

    Optional<ApplicationPermission> findByUserAndPermissionValue(String username, ApplicationPermissionValue changingPermissionValue);

    Optional<ApplicationPermission> findByRoleAndRuleAndFeature(
            ApplicationRole holder,
            ApplicationPermissionRule rule,
            ApplicationFeatureSort type,
            String featureFqn);

    Collection<ApplicationPermission> allPermissions();

    Collection<ApplicationPermission> findOrphaned();
    Collection<ApplicationPermission> findByFeatureCached(ApplicationFeatureId featureId);
    Collection<ApplicationPermission> findByRoleAndRuleAndFeatureTypeCached(
            ApplicationRole holder,
            ApplicationPermissionRule rule,
            ApplicationFeatureSort type);


    /**
     * @return detached entity
     */
    ApplicationPermission newApplicationPermission();

    ApplicationPermission newPermission(
            ApplicationRole role,
            ApplicationPermissionRule rule,
            ApplicationPermissionMode mode,
            String packageFqn,
            String className,
            String memberName);

    ApplicationPermission newPermission(
            ApplicationRole role,
            ApplicationPermissionRule rule,
            ApplicationPermissionMode mode,
            ApplicationFeatureSort featureSort,
            String featureFqn);

    ApplicationPermission newPermission(
            ApplicationRole role,
            ApplicationPermissionRule rule,
            ApplicationPermissionMode mode,
            ApplicationFeatureId featureId);

    /**
     * Intended for use by fixture scripts.
     */
    ApplicationPermission newPermissionNoCheck(
            ApplicationRole role,
            ApplicationPermissionRule rule,
            ApplicationPermissionMode mode,
            ApplicationFeatureSort sort,
            String featureFqn);

    /**
     * Uses the {@link ApplicationRole}s associated with the {@link ApplicationUser}.
     */
    List<ApplicationPermission> findByUser(ApplicationUser applicationUser);

    List<ApplicationPermission> findByRole(ApplicationRole applicationRole);

    /**
     * Uses the {@link UserMemento#getRoles() roles} held within the provided {@link UserMemento}.
     *
     * <p>
     * Added to support {@link org.apache.isis.applib.services.user.ImpersonateMenu.impersonateWithRoles#act(String, List, String) impersonation by role}.
     * </p>
     *
     * @see #findByRoleNames(List)
     */
    List<ApplicationPermission> findByUserMemento(UserMemento user);

    /**
     * Returns the set of permissions associated with the provided list of roles (identified by
     * their role name).
     *
     * @see #findByUserMemento(UserMemento)
     */
    List<ApplicationPermission> findByRoleNames(List<String> roleNames);

}
