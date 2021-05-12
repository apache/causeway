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
package org.apache.isis.extensions.secman.api.role.dom.mixins;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.MinLength;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.appfeat.ApplicationFeature;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionRepository;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.dom.mixins.ApplicationRole_addPermission.DomainEvent;
import org.apache.isis.extensions.secman.api.feature.dom.ApplicationFeatureChoices;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

@Action(
        associateWith = "permissions",
        domainEvent = DomainEvent.class,
        semantics = SemanticsOf.NON_IDEMPOTENT
)
@ActionLayout(
		named = "Add",
		promptStyle = PromptStyle.DIALOG_MODAL,
		sequence = "1"
)
@RequiredArgsConstructor
public class ApplicationRole_addPermission {

    public static class DomainEvent
            extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationRole_addPermission> {}

    @Inject private ApplicationPermissionRepository applicationPermissionRepository;
    @Inject private ApplicationFeatureChoices applicationFeatureChoices;

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
        return applicationFeatureChoices.autoCompleteFeature(search);
    }

}
