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
package org.apache.isis.core.metamodel.facets.object.viewmodel;

import java.util.Optional;

import org.apache.isis.applib.ViewModel;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.HasPostConstructMethodCache;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

/**
 * Corresponds to {@link ViewModel} interface.
 */
public class ViewModelFacetForViewModelInterface
extends ViewModelFacetAbstract {

    public static Optional<ViewModelFacet> create(
            final Class<?> cls,
            final FacetHolder holder,
            final HasPostConstructMethodCache postConstructMethodCache) {

        if(!ViewModel.class.isAssignableFrom(cls)) {
            return Optional.empty();
        }

        if(!ProgrammingModelConstants.ViewmodelConstructor.SINGLE_STRING_ARG
            .get(cls)
            .isPresent()) {

            ValidationFailure.raiseFormatted(holder,
                    ProgrammingModelConstants.Validation.VIEWMODEL_MISSING_DESERIALIZING_CONSTRUCTOR
                        .getMessageForType(cls.getName()));

            return Optional.empty();
        }

        return Optional.of(new ViewModelFacetForViewModelInterface(holder, postConstructMethodCache));
    }

    protected ViewModelFacetForViewModelInterface(
            final FacetHolder holder,
            final HasPostConstructMethodCache postConstructMethodCache) {
        super(holder, postConstructMethodCache);
    }

    @Override
    protected ManagedObject createViewmodel(
            @NonNull final ObjectSpecification viewmodelSpec) {
        return ManagedObject.of(
                viewmodelSpec,
                deserialize(viewmodelSpec, null));
    }

    @SneakyThrows
    @Override
    protected ManagedObject createViewmodel(
            @NonNull final ObjectSpecification viewmodelSpec,
            @NonNull final Bookmark bookmark) {
        return ManagedObject.bookmarked(
                        viewmodelSpec,
                        deserialize(viewmodelSpec, bookmark.getIdentifier()),
                        bookmark);
    }

    @Override
    public String serialize(final ManagedObject viewModel) {
        final ViewModel viewModelPojo = (ViewModel) viewModel.getPojo();
        return viewModelPojo.viewModelMemento();
    }

    // -- HELPER

    @SneakyThrows
    private Object deserialize(
            @NonNull final ObjectSpecification viewmodelSpec,
            @NonNull final String memento) {
        val constructorTakingMemento = ProgrammingModelConstants.ViewmodelConstructor.SINGLE_STRING_ARG
                .get(viewmodelSpec.getCorrespondingClass())
                .orElseThrow();
        val viewmodelPojo = constructorTakingMemento
                .newInstance(memento);
        return viewmodelPojo;
    }

}
