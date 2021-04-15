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

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.MinLength;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.services.appfeat.ApplicationFeature;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermission;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRepository;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.ApplicationRole.AddPermissionDomainEvent;
import org.apache.isis.extensions.secman.model.dom.feature.ApplicationFeatureChoices;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

@Action(
        domainEvent = AddPermissionDomainEvent.class, 
        associateWith = "permissions")
@ActionLayout(
		named="Add",
		sequence = "0",
		promptStyle = PromptStyle.DIALOG_MODAL)
@RequiredArgsConstructor
public class ApplicationRole_addPermission {
    
    @Inject private ApplicationFeatureRepository featureRepository;
    @Inject private ApplicationPermissionRepository<? extends ApplicationPermission> applicationPermissionRepository;
    
    private final ApplicationRole target;
    
    @Value @Accessors(fluent = true)           
    public static class Parameters {
        ApplicationPermissionRule rule; // ALLOW/VETO
        ApplicationPermissionMode mode; // r/w
        ApplicationFeatureChoices.AppFeat feature;
    }

    /**
     * Adds a {@link ApplicationPermission permission} for this role to a
     * {@link ApplicationFeature feature}.
     */
    public ApplicationRole act(
            
            @Parameter(optionality = Optionality.MANDATORY)
            @ParameterLayout(named="Rule")
            final ApplicationPermissionRule rule,
            
            @Parameter(optionality = Optionality.MANDATORY)
            @ParameterLayout(named="Mode")
            final ApplicationPermissionMode mode,
            
            @Parameter(optionality = Optionality.MANDATORY)
            @ParameterLayout(
                    named = "Feature",
                    describedAs = ApplicationFeatureChoices.DESCRIBED_AS)
            final ApplicationFeatureChoices.AppFeat feature) {
        
        applicationPermissionRepository.newPermission(target, rule, mode, feature.getFeatureId());
        return target;
    }

    @MemberSupport
    public ApplicationPermissionRule defaultRule(Parameters params) {
        return ApplicationPermissionRule.ALLOW;
    }

    @MemberSupport
    public ApplicationPermissionMode defaultMode(Parameters params) {
        return ApplicationPermissionMode.CHANGING;
    }

    @MemberSupport
    public java.util.Collection<ApplicationFeatureChoices.AppFeat> autoCompleteFeature(
            final Parameters params,
            final @MinLength(3) String search) {
        return ApplicationFeatureChoices.autoCompleteFeature(featureRepository, search);
    }
    
}
