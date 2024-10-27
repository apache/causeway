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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.IndexedConsumer;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedConstructor;
import org.apache.causeway.commons.io.UrlUtils;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.commons.ClassExtensions;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

/**
 * Corresponds to {@link ViewModel} interface.
 */
public class ViewModelFacetForViewModelInterface
extends ViewModelFacetAbstract {

    public static <T> Optional<ViewModelFacet> create(
            final Class<T> cls,
            final FacetHolder holder) {

        if(!ViewModel.class.isAssignableFrom(cls)) {
            return Optional.empty();
        }

        ResolvedConstructor pickedConstructor = null; // not used for abstract types

        if(!cls.isInterface()
                && !ClassExtensions.isAbstract(cls)) {

            var explicitInjectConstructors = ProgrammingModelConstants.ViewmodelConstructor.PUBLIC_WITH_INJECT_SEMANTICS.getAll(cls);
            var publicConstructors = ProgrammingModelConstants.ViewmodelConstructor.PUBLIC_ANY.getAll(cls);

            var violation = explicitInjectConstructors.getCardinality().isMultiple()
                    ? ProgrammingModelConstants.MessageTemplate.VIEWMODEL_MULTIPLE_CONSTRUCTORS_WITH_INJECT_SEMANTICS
                    : explicitInjectConstructors.getCardinality().isZero()
                        && !publicConstructors.getCardinality().isOne()
                            // in absence of a constructor with inject semantics there must be exactly one public to pick instead
                            ? ProgrammingModelConstants.MessageTemplate.VIEWMODEL_MISSING_OR_MULTIPLE_PUBLIC_CONSTRUCTORS
                            : null;

            if(violation!=null) {

                ValidationFailure.raiseFormatted(holder,
                        violation
                            .builder()
                            .addVariable("type", cls.getName())
                            .addVariable("found", explicitInjectConstructors.getCardinality().isMultiple()
                                    ? "{" + explicitInjectConstructors.stream()
                                            .map(ResolvedConstructor::constructor)
                                            .map(Constructor::toString)
                                            .collect(Collectors.joining(", ")) + "}"
                                    : "none")
                            .buildMessage());

                return Optional.empty();

            }

            // -- else happy case

            pickedConstructor = explicitInjectConstructors.getCardinality().isOne()
                    ? explicitInjectConstructors.getSingletonOrFail()
                    : publicConstructors.getSingletonOrFail();

        }

        return Optional.of(new ViewModelFacetForViewModelInterface(holder, pickedConstructor));
    }

    private ResolvedConstructor constructorAnyArgs;

    protected ViewModelFacetForViewModelInterface(
            final FacetHolder holder,
            final @Nullable ResolvedConstructor constructorAnyArgs) {
        super(holder, Precedence.HIGH);
        this.constructorAnyArgs = constructorAnyArgs;
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
        return SpecialMemento.encode(viewModelPojo.viewModelMemento());
    }

    // -- HELPER

    /**
     * In support of (stateless) {@link ViewModel}s that don't use a memento,
     * or if an empty memento String is actually considered a valid use-case.
     * (e.g. a Viewmodel that simply holds a String value for a search say)
     * @apiNote introduced so we can create valid bookmarks,
     *      that must have a non-empty identifier part
     * @implNote the pipe character '|' is regarded unsafe,
     *      hence gets processed by {@link URLEncoder} and {@link URLDecoder},
     *      which makes it safe for us to use with special meaning
     */
    @Getter @Accessors(fluent=true)
    @RequiredArgsConstructor
    enum SpecialMemento {
        EMPTY("||"),
        NULL("|");
        static String encode(final @Nullable String memento) {
            return memento==null
                    ? NULL.representationInUrl()
                    : memento.isEmpty()
                            ? EMPTY.representationInUrl()
                            : UrlUtils.urlEncodeUtf8(memento);
        }
        static String decode(final @Nullable String memento) {
            return NULL.matches(memento)
                    ? null
                    : EMPTY.matches(memento)
                            ? ""
                            : UrlUtils.urlDecodeUtf8(memento);
        }
        final String representationInUrl;
        boolean matches(final String other) {
            return representationInUrl.equals(other);
        }
    }

    @SneakyThrows
    private Object deserialize(
            @NonNull final ObjectSpecification viewmodelSpec,
            @Nullable final String mementoEncoded) {

        _Assert.assertNotNull(constructorAnyArgs, ()->"framework bug: required non-null, "
                + "this can only happen, if we try to deserialize an abstract type");

        var memento = SpecialMemento.decode(mementoEncoded);
        var resolvedArgs = resolveArgsForConstructor(constructorAnyArgs, getServiceRegistry(), memento);
        var viewmodelPojo = constructorAnyArgs.constructor().newInstance(resolvedArgs);
        return viewmodelPojo;
    }

    private static Object[] resolveArgsForConstructor(
            final ResolvedConstructor constructor,
            final ServiceRegistry serviceRegistry,
            final String memento) {

        var paramTypes = Can.ofArray(constructor.paramTypes());
        var args = new Object[constructor.paramCount()];
        paramTypes.forEach(IndexedConsumer.zeroBased((final int i, final Class<?> paramType)->{
            if(paramType.equals(String.class)) {
                args[i] = memento; // its ok to do this never, once, or more than once per constructor, see ViewModel java-doc
                return;
            }
            args[i] = serviceRegistry.lookupServiceElseFail(paramType);
        }));
        return args;
    }

}
