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

import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.MinLength;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeature;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermission;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRepository;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.ApplicationRole.AddPermissionDomainEvent;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import lombok.experimental.Accessors;

//TODO[2560] wip
//@Action(
//        domainEvent = AddPermissionDomainEvent.class, 
//        associateWith = "permissions")
//@ActionLayout(promptStyle = PromptStyle.DIALOG_MODAL)
@RequiredArgsConstructor
public class ApplicationRole_addPermission {
    
    @Inject private ApplicationFeatureRepository applicationFeatureRepository;
    @Inject private ApplicationPermissionRepository<? extends ApplicationPermission> applicationPermissionRepository;
    
    private final ApplicationRole target;
    
    @Value @Accessors(fluent = true)           
    public static class Parameters {
        ApplicationPermissionRule rule; // ALLOW/VETO
        ApplicationPermissionMode mode; // r/w
        String feature;
    }

    /**
     * Adds a {@link ApplicationPermission permission} for this role to a
     * {@link ApplicationFeature feature}.
     */
    @MemberOrder(sequence = "3")
    public ApplicationRole act(
            
            @Parameter(optionality = Optionality.MANDATORY)
            @ParameterLayout(named="Rule")
            final ApplicationPermissionRule rule,
            
            @Parameter(optionality = Optionality.MANDATORY)
            @ParameterLayout(named="Mode")
            final ApplicationPermissionMode mode,
            
            @Parameter(optionality = Optionality.MANDATORY)
            @ParameterLayout(named="Feature")
            final String featureName) {
        
        val featureId = ApplicationFeatureId.parse(featureName);
        
        applicationPermissionRepository.newPermission(target, rule, mode, featureId);
        return target;
    }

    @Model
    public ApplicationPermissionRule defaultRule(Parameters params) {
        return ApplicationPermissionRule.ALLOW;
    }

    @Model
    public ApplicationPermissionMode defaultMode(Parameters params) {
        return ApplicationPermissionMode.CHANGING;
    }

    @Model
    public java.util.Collection<String> autoComplete2Act(             
            @MinLength(3) String search) {
        
        val idsByName = applicationFeatureRepository.getFeatureIdentifiersByName();
        
        return idsByName.entrySet().stream()
        .filter(entry->matches(entry.getKey(), entry.getValue(), search))
        .map(Map.Entry::getValue)
        .map(ApplicationFeatureId::asString)
        .collect(Collectors.toCollection(TreeSet::new));
    }

    private boolean matches(String featureName, ApplicationFeatureId featureId, String search) {
        //TODO yet not very smart
        return featureName.contains(search);
    }

    
}
