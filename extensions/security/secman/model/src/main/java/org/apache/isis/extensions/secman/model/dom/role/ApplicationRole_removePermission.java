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
package org.apache.isis.extensions.secman.model.dom.role;

import java.util.Collection;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeature;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureType;
import org.apache.isis.extensions.secman.api.SecmanConfiguration;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermission;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRepository;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.ApplicationRoleRepository;

import lombok.RequiredArgsConstructor;

//@Action(
//        domainEvent = RemovePermissionDomainEvent.class, 
//        associateWith = "permissions",
//        associateWithSequence = "9"
//        )
@Deprecated
@RequiredArgsConstructor
public class ApplicationRole_removePermission {

    @Inject private SecmanConfiguration configBean;
    @Inject private RepositoryService repository;
    @Inject private ApplicationRoleRepository<? extends ApplicationRole> applicationRoleRepository;
    @Inject private ApplicationPermissionRepository<? extends ApplicationPermission> applicationPermissionRepository;
    
    private final ApplicationRole holder;

    @Model
    public ApplicationRole act(
            @ParameterLayout(named="Rule")
            final ApplicationPermissionRule rule,
            @ParameterLayout(named="Type")
            final ApplicationFeatureType type,
            @ParameterLayout(named="Feature", typicalLength=ApplicationFeature.TYPICAL_LENGTH_MEMBER_NAME)
            final String featureFqn) {
        
        final ApplicationPermission permission = applicationPermissionRepository
                .findByRoleAndRuleAndFeature(holder, rule, type, featureFqn)
                .orElse(null);
        if(permission != null) {
            repository.remove(permission);
        }
        return holder;
    }

    @Model
    public String validateAct(
            @ParameterLayout(named="Rule")
            final ApplicationPermissionRule rule,
            @ParameterLayout(named="Type")
            final ApplicationFeatureType type,
            @ParameterLayout(named="Feature", typicalLength=ApplicationFeature.TYPICAL_LENGTH_MEMBER_NAME)
            final String featureFqn) {
        if(applicationRoleRepository.isAdminRole(holder) 
                && configBean.isStickyAdminNamespace(featureFqn)) {
            return "Cannot remove top-level namespace permissions for the admin role.";
        }
        return null;
    }
    
    @Model
    public ApplicationPermissionRule default0Act() {
        return ApplicationPermissionRule.ALLOW;
    }
    
    @Model
    public ApplicationFeatureType default1Act() {
        return ApplicationFeatureType.PACKAGE;
    }

    @Model
    public Collection<String> choices2Act(
            final ApplicationPermissionRule rule,
            final ApplicationFeatureType type) {
        
        final Collection<? extends ApplicationPermission> permissions = applicationPermissionRepository
                .findByRoleAndRuleAndFeatureTypeCached(holder, rule, type);
        return _Lists.map(
                permissions,
                ApplicationPermission::getFeatureFqn);
    }

}
