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
package org.apache.isis.extensions.secman.model.dom.user;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.appfeat.ApplicationFeature;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.model.dom.feature.ApplicationFeatureConstants;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Action(
        semantics = SemanticsOf.SAFE,
        associateWith = "permissions",
        associateWithSequence = "1"
        )
@RequiredArgsConstructor
public class ApplicationUser_filterPermissions {

    @Inject private FactoryService factory;
    @Inject private ApplicationFeatureRepository featureRepository;

    private final ApplicationUser target;

    @Model
    public List<UserPermissionViewModel> act(
            
            @ParameterLayout(named="Namespace", typicalLength=ApplicationFeatureConstants.TYPICAL_LENGTH_NAMESPACE)
            final String namespace,
            
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named="Type", typicalLength=ApplicationFeatureConstants.TYPICAL_LENGTH_TYPE_SIMPLE_NAME)
            final String logicalTypeSimpleName) {
        
        val allMembers = featureRepository.allMembers();
        val filtered = _Lists.filter(allMembers, within(namespace, logicalTypeSimpleName));
        return asViewModels(filtered);
    }

    /**
     * Package names that have classes in them.
     */
    @Model
    public Collection<String> choices0Act() {
        return featureRepository.namespaceNames();
    }


    /**
     * Class names for selected package.
     */
    @Model
    public Collection<String> choices1Act(final String namespace) {
        return featureRepository.classNamesRecursivelyContainedIn(namespace);
    }


    static Predicate<ApplicationFeature> within(final String namespace, final String logicalTypeSimpleName) {
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

    List<UserPermissionViewModel> asViewModels(final Collection<ApplicationFeature> features) {
        return _Lists.map(
                features,
                UserPermissionViewModel.Functions.asViewModel(target, factory));
    }


}
