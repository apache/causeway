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
package org.apache.causeway.core.metamodel.facets.object.viewmodel;

import java.lang.reflect.Constructor;
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.IndexedConsumer;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.commons.ClassExtensions;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.HasPostConstructMethodCache;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;
import org.apache.causeway.core.privileged._Privileged;

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

        if(!cls.isInterface()
                && !ClassExtensions.isAbstract(cls)
                && !ProgrammingModelConstants.ViewmodelConstructor.PUBLIC_ANY_ARGS.get(cls).isPresent()) {
            ValidationFailure.raiseFormatted(holder,
                    ProgrammingModelConstants.Validation.VIEWMODEL_MISSING_OR_MULTIPLE_PUBLIC_CONSTRUCTORS
                        .getMessageForType(cls.getName()));

            return Optional.empty();
        }

        return Optional.of(new ViewModelFacetForViewModelInterface(holder, postConstructMethodCache));
    }

    protected ViewModelFacetForViewModelInterface(
            final FacetHolder holder,
            final HasPostConstructMethodCache postConstructMethodCache) {
        super(holder, postConstructMethodCache,  Precedence.HIGH);
    }

    @Override
    protected ManagedObject createViewmodel(
            @NonNull final ObjectSpecification viewmodelSpec) {
        return ManagedObject.viewmodel(
                viewmodelSpec,
                deserialize(viewmodelSpec, null),
                Optional.empty());
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
            @Nullable final String memento) {
        val constructorAnyArgs = ProgrammingModelConstants.ViewmodelConstructor.PUBLIC_ANY_ARGS
                .get(viewmodelSpec.getCorrespondingClass())
                .orElseThrow();

        val resolvedArgs = resolveArgsForConstructor(constructorAnyArgs, getServiceRegistry(), memento);

        val viewmodelPojo = _Privileged
                .newInstance(constructorAnyArgs, resolvedArgs);
        return viewmodelPojo;
    }

    private static Object[] resolveArgsForConstructor(
            final Constructor<?> constructor,
            final ServiceRegistry serviceRegistry,
            final String memento) {
        val params = Can.ofArray(constructor.getParameters());
        val args = new Object[params.size()];
        params.forEach(IndexedConsumer.zeroBased((i, param)->{
            if(param.getType().equals(String.class)) {
                args[i] = memento; // its ok to do this never, once, or more than once per constructor, see ViewModel java-doc
                return;
            }
            args[i] = serviceRegistry.lookupServiceElseFail(param.getType());
        }));
        return args;
    }

}
