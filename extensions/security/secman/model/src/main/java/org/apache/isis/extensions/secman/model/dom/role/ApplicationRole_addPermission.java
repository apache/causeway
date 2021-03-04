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
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.apache.isis.applib.ViewModel;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.MinLength;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeature;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermission;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRepository;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.ApplicationRole.AddPermissionDomainEvent;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import lombok.experimental.Accessors;

@Action(
        domainEvent = AddPermissionDomainEvent.class, 
        associateWith = "permissions")
@ActionLayout(promptStyle = PromptStyle.DIALOG_MODAL)
@RequiredArgsConstructor
public class ApplicationRole_addPermission {
    
    @Inject private ApplicationFeatureRepository applicationFeatureRepository;
    @Inject private ApplicationPermissionRepository<? extends ApplicationPermission> applicationPermissionRepository;
    
    private final ApplicationRole target;
    
    @Value @Accessors(fluent = true)           
    public static class Parameters {
        ApplicationPermissionRule rule; // ALLOW/VETO
        ApplicationPermissionMode mode; // r/w
        AppFeat feature;
    }

    /**
     * Adds a {@link ApplicationPermission permission} for this role to a
     * {@link ApplicationFeature feature}.
     */
    @MemberOrder(sequence = "0")
    public ApplicationRole act(
            
            @Parameter(optionality = Optionality.MANDATORY)
            @ParameterLayout(named="Rule")
            final ApplicationPermissionRule rule,
            
            @Parameter(optionality = Optionality.MANDATORY)
            @ParameterLayout(named="Mode")
            final ApplicationPermissionMode mode,
            
            @Parameter(optionality = Optionality.MANDATORY)
            @ParameterLayout(named="Feature")
            final AppFeat feature) {
        
        applicationPermissionRepository.newPermission(target, rule, mode, feature.getFeatureId());
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
    public java.util.Collection<AppFeat> autoCompleteFeature(
            Parameters params,
            @MinLength(3) String search) {
        
        val idsByName = applicationFeatureRepository.getFeatureIdentifiersByName();
        
        return idsByName.entrySet().stream()
        .filter(entry->matches(entry.getKey(), entry.getValue(), search))
        .map(Map.Entry::getValue)
        .map(AppFeat::new)
        .collect(Collectors.toCollection(TreeSet::new));
    }

    private boolean matches(String featureName, ApplicationFeatureId featureId, String search) {
        //TODO yet not very smart
        return featureName.contains(search);
    }
    
    // -- FEATURE VIEW MODEL WRAPPING A VALUE TYPE 

    /**
     * Viewmodel wrapper around value type {@link ApplicationFeatureId}. Introduced,
     * because at the time of writing, 
     * autoComplete/choices do not support value types.
     */
    @DomainObject(
            nature = Nature.VIEW_MODEL, 
            objectType = "isis.ext.secman.AppFeat")
    @AllArgsConstructor @NoArgsConstructor @EqualsAndHashCode
    public static class AppFeat 
    implements 
        Comparable<AppFeat>,
        ViewModel {
        
        @Property
        @Getter  
        private ApplicationFeatureId featureId;
        
        public String title() {
            return toString();
        }
        
        @Override
        public int compareTo(AppFeat o) {
            val thisId = this.getFeatureId();
            val otherId = o!=null ? o.getFeatureId() : null;
            if(Objects.equals(thisId, otherId)) {
                return 0;
            }
            if(thisId==null) {
                return -1;
            }
            if(otherId==null) {
                return 1;
            }
            return this.getFeatureId().compareTo(o.getFeatureId());
        }
        
        @Override
        public String toString() {
            return featureId!=null 
                    ? featureId.getSort().name() + ": " + featureId.getFullyQualifiedName()
                    : "<no id>";
        }

        @Override
        public String viewModelMemento() {
            return featureId!=null 
                    ? featureId.asEncodedString() 
                    : "<no id>";
        }

        @Override
        public void viewModelInit(String memento) {
            featureId = ApplicationFeatureId.parseEncoded(memento); // fail by intention if memento is '<no id>'
        }
        
    }
    
    
}
