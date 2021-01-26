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
package org.apache.isis.extensions.secman.model.app.user;

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
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeature;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureId;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureRepositoryDefault;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;

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
    @Inject private ApplicationFeatureRepositoryDefault applicationFeatureRepository;

    private final ApplicationUser holder;

    @Model
    public List<UserPermissionViewModel> act(
            @ParameterLayout(named="Package", typicalLength=ApplicationFeature.TYPICAL_LENGTH_PKG_FQN)
            final String packageFqn,
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named="Class",  typicalLength=ApplicationFeature.TYPICAL_LENGTH_CLS_NAME)
            final String className) {
        val allMembers = applicationFeatureRepository.allMembers();
        val filtered = _Lists.filter(allMembers, within(packageFqn, className));
        return asViewModels(filtered);
    }

    /**
     * Package names that have classes in them.
     */
    @Model
    public Collection<String> choices0Act() {
        return applicationFeatureRepository.packageNames();
    }


    /**
     * Class names for selected package.
     */
    @Model
    public Collection<String> choices1Act(final String packageFqn) {
        return applicationFeatureRepository.classNamesRecursivelyContainedIn(packageFqn);
    }


    static Predicate<ApplicationFeature> within(final String packageFqn, final String className) {
        return (ApplicationFeature input) -> {
            final ApplicationFeatureId inputFeatureId = input.getFeatureId();

            // recursive match on package
            final ApplicationFeatureId packageId = ApplicationFeatureId.newPackage(packageFqn);
            final List<ApplicationFeatureId> pathIds = inputFeatureId.getPathIds();
            if(!pathIds.contains(packageId)) {
                return false;
            }

            // match on class (if specified)
            return className == null || Objects.equals(inputFeatureId.getTypeSimpleName(), className);
        };
    }

    List<UserPermissionViewModel> asViewModels(final Collection<ApplicationFeature> features) {
        return _Lists.map(
                features,
                UserPermissionViewModel.Functions.asViewModel(holder, factory));
    }


}
