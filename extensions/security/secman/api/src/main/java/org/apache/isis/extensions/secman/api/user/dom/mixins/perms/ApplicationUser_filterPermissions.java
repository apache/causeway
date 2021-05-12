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
package org.apache.isis.extensions.secman.api.user.dom.mixins.perms;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

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
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.feature.dom.ApplicationFeatureChoices;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUser;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Action(
        associateWith = "permissions",
        domainEvent = ApplicationUser_filterPermissions.DomainEvent.class,
        semantics = SemanticsOf.SAFE
)
@ActionLayout(sequence = "1", promptStyle = PromptStyle.DIALOG_MODAL)
@RequiredArgsConstructor
public class ApplicationUser_filterPermissions {

    public static class DomainEvent
            extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationUser_filterPermissions> {}

    @Inject private FactoryService factory;
    @Inject private ApplicationFeatureRepository featureRepository;
    @Inject private ApplicationFeatureChoices applicationFeatureChoices;

    private final ApplicationUser target;

    @MemberSupport
    public List<UserPermissionViewModel> act(

            @Parameter(optionality = Optionality.MANDATORY)
            @ParameterLayout(
                    named = "Feature",
                    describedAs = ApplicationFeatureChoices.DESCRIBED_AS)
            final ApplicationFeatureChoices.AppFeat feature) {

        val featureId = feature.getFeatureId();

        final String namespace = featureId.getNamespace();
        final String typeSimpleName = featureId.getTypeSimpleName();

        val allMembers = featureRepository.allMembers();
        val filtered = _Lists.filter(allMembers, within(namespace, typeSimpleName));
        return asViewModels(filtered);
    }

    @MemberSupport
    public java.util.Collection<ApplicationFeatureChoices.AppFeat> autoComplete0Act(
            final @MinLength(3) String search) {
        return applicationFeatureChoices.autoCompleteFeature(search);
    }

    // -- HELPER XXX left over from refactoring, could be simplified ..

    private static Predicate<ApplicationFeature> within(final String namespace, final String logicalTypeSimpleName) {
        return (ApplicationFeature input) -> {
            final ApplicationFeatureId inputFeatureId = input.getFeatureId();

            // recursive match on package
            val namespaceId = ApplicationFeatureId.newNamespace(namespace);
            if(!inputFeatureId.getPathIds().contains(namespaceId)) {
                return false;
            }

            // match on class (if specified)
            return logicalTypeSimpleName == null
                    || Objects.equals(inputFeatureId.getTypeSimpleName(), logicalTypeSimpleName);
        };
    }

    private List<UserPermissionViewModel> asViewModels(final Collection<ApplicationFeature> features) {
        return _Lists.map(
                features,
                UserPermissionViewModel.asViewModel(target, factory));
    }


}
