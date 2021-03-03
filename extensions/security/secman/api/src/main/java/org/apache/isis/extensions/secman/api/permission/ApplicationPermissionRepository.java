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
package org.apache.isis.extensions.secman.api.permission;

import java.util.Collection;
import java.util.Optional;

import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureType;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;

/**
 * @since 2.0 {@index}
 */
public interface ApplicationPermissionRepository<P extends ApplicationPermission> {

    Optional<P> findByUserAndPermissionValue(String username, ApplicationPermissionValue changingPermissionValue);

    Optional<P> findByRoleAndRuleAndFeature(
            ApplicationRole holder,
            ApplicationPermissionRule rule,
            ApplicationFeatureType type,
            String featureFqn);

    Collection<P> allPermissions();

    Collection<P> findOrphaned();
    Collection<P> findByFeatureCached(ApplicationFeatureId featureId);
    Collection<P> findByRoleAndRuleAndFeatureTypeCached(
            ApplicationRole holder,
            ApplicationPermissionRule rule,
            ApplicationFeatureType type);


    /**
     * @return detached entity
     */
    P newApplicationPermission();

    P newPermission(
            ApplicationRole role,
            ApplicationPermissionRule rule,
            ApplicationPermissionMode mode,
            String packageFqn,
            String className,
            String memberName);

    P newPermission(
            ApplicationRole role,
            ApplicationPermissionRule rule,
            ApplicationPermissionMode mode,
            ApplicationFeatureType featureType,
            String featureFqn);

    P newPermission(
            ApplicationRole role,
            ApplicationPermissionRule rule,
            ApplicationPermissionMode mode,
            ApplicationFeatureId featureId);
    
    
}
