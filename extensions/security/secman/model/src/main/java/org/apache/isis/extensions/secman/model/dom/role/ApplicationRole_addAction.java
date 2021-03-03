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

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureType;
import org.apache.isis.applib.services.appfeat.ApplicationMemberType;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeature;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermission;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRepository;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.ApplicationRole.AddPermissionDomainEvent;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = AddPermissionDomainEvent.class, 
        associateWith = "permissions")
@RequiredArgsConstructor
public class ApplicationRole_addAction {
    
    @Inject private ApplicationFeatureRepository applicationFeatureRepository;
    @Inject private ApplicationPermissionRepository<? extends ApplicationPermission> applicationPermissionRepository;
    
    private final ApplicationRole holder;

    /**
     * Adds a {@link ApplicationPermission permission} for this role to a
     * {@link ApplicationMemberType#ACTION action}
     * {@link ApplicationFeatureType#MEMBER member}
     * {@link ApplicationFeature feature}.
     */
    @MemberOrder(sequence = "3")
    public ApplicationRole act(
            @ParameterLayout(named="Rule")
            final ApplicationPermissionRule rule,
            @ParameterLayout(named="Mode")
            final ApplicationPermissionMode mode,
            @ParameterLayout(named="Package", typicalLength=ApplicationFeature.TYPICAL_LENGTH_PKG_FQN)
            final String packageFqn,
            @ParameterLayout(named="Class", typicalLength=ApplicationFeature.TYPICAL_LENGTH_CLS_NAME)
            final String className,
            @ParameterLayout(named="Action", typicalLength = ApplicationFeature.TYPICAL_LENGTH_MEMBER_NAME)
            final String memberName) {
        
        applicationPermissionRepository.newPermission(holder, rule, mode, packageFqn, className, memberName);
        return holder;
    }

    @Model
    public ApplicationPermissionRule default0Act() {
        return ApplicationPermissionRule.ALLOW;
    }

    @Model
    public ApplicationPermissionMode default1Act() {
        return ApplicationPermissionMode.CHANGING;
    }

    @Model
    public java.util.Collection<String> choices2Act() {
        return applicationFeatureRepository.packageNamesContainingClasses(ApplicationMemberType.ACTION);
    }

    @Model
    public java.util.Collection<String> choices3Act(
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final String packageFqn) {
        return applicationFeatureRepository.classNamesContainedIn(packageFqn, ApplicationMemberType.ACTION);
    }

    @Model
    public java.util.Collection<String> choices4Act(
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final String packageFqn,
            final String className) {
        return applicationFeatureRepository.memberNamesOf(packageFqn, className, ApplicationMemberType.ACTION);
    }
    
}
