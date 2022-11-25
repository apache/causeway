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
package org.apache.causeway.core.runtimeservices.factory;

import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.objectlifecycle.ObjectLifecyclePublisher;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.NonNull;
import lombok.val;

@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".FactoryServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class FactoryServiceDefault implements FactoryService {

    @Inject InteractionService interactionService; // dependsOn
    @Inject private SpecificationLoader specificationLoader;
    @Inject private CausewaySystemEnvironment causewaySystemEnvironment;
    @Inject private Provider<ObjectLifecyclePublisher> objectLifecyclePublisherProvider;
    private ObjectLifecyclePublisher objectLifecyclePublisher() { return objectLifecyclePublisherProvider.get(); }


    @Override
    public <T> T getOrCreate(final @NonNull Class<T> requiredType) {
        val spec = loadSpecElseFail(requiredType);
        if(spec.isInjectable()) {
            return get(requiredType);
        }
        return create(requiredType);
    }

    @Override
    public <T> T get(final @NonNull Class<T> requiredType) {
        return causewaySystemEnvironment.getIocContainer()
                .get(requiredType)
                .orElseThrow(()->_Exceptions.noSuchElement("not an injectable type %s", requiredType));
    }

    @Override
    public <T> T detachedEntity(final @NonNull Class<T> domainClass) {
        val entitySpec = loadSpecElseFail(domainClass);
        if(!entitySpec.isEntity()) {
            throw _Exceptions.illegalArgument("Class '%s' is not an entity", domainClass.getName());
        }
        return createObject(domainClass, entitySpec);
    }

    @Override
    public <T> T detachedEntity(final @NonNull T entityPojo) {
        val entityClass = entityPojo.getClass();
        val spec = loadSpecElseFail(entityClass);
        if(!spec.isEntity()) {
            throw _Exceptions.illegalArgument("Type '%s' is not recognized as an entity type by the framework.",
                    entityClass);
        }        objectLifecyclePublisher().onPostCreate(ManagedObject.entity(spec, entityPojo, Optional.empty()));
        return entityPojo;
    }

    @Override
    public <T> T mixin(final @NonNull Class<T> mixinClass, final @NonNull Object mixee) {
        val mixinSpec = loadSpecElseFail(mixinClass);
        val mixinFacet = mixinSpec.getFacet(MixinFacet.class);
        if(mixinFacet == null) {
            throw _Exceptions.illegalArgument("Class '%s' is not a mixin",
                    mixinClass.getName());
        }
        if(mixinSpec.isAbstract()) {
            throw _Exceptions.illegalArgument("Cannot instantiate abstract type '%s' as a mixin",
                    mixinClass.getName());
        }
        val mixin = mixinFacet.instantiate(mixee);
        return _Casts.uncheckedCast(mixin);
    }

    @Override
    public <T> T viewModel(final @NonNull T viewModelPojo) {
        val viewModelClass = viewModelPojo.getClass();
        val spec = loadSpecElseFail(viewModelClass);
        if(!spec.isViewModel()) {
            throw _Exceptions.illegalArgument("Type '%s' is not recognized as a ViewModel by the framework.",
                    viewModelClass);
        }
        spec.viewmodelFacetElseFail().initialize(viewModelPojo);
        objectLifecyclePublisher().onPostCreate(ManagedObject.viewmodel(spec, viewModelPojo, Optional.empty()));
        return viewModelPojo;
    }

    @Override
    public <T> T viewModel(final @NonNull Class<T> viewModelClass, final @Nullable Bookmark bookmark) {
        val spec = loadSpecElseFail(viewModelClass);
        return createViewModelElseFail(viewModelClass, spec, Optional.ofNullable(bookmark));
    }

    @Override
    public <T> T create(final @NonNull Class<T> domainClass) {
        val spec = loadSpecElseFail(domainClass);
        if(spec.isInjectable()) {
            throw _Exceptions.illegalArgument(
                    "Class '%s' is managed by Spring, use get() instead", domainClass.getName());
        }
        if(spec.isViewModel()) {
            return createViewModelElseFail(domainClass, spec, Optional.empty());
        }
        if(spec.isEntity()) {
            return detachedEntity(domainClass);
        }
        // fallback to generic object creation
        return createObject(domainClass, spec);
    }

    // -- HELPER

    private ObjectSpecification loadSpecElseFail(final @NonNull Class<?> type) {
        return specificationLoader.specForTypeElseFail(type);
    }

    /** handles injection, post-construct and publishing */
    private <T> T createViewModelElseFail(
            final @NonNull Class<T> viewModelClass,
            final @NonNull ObjectSpecification objectSpecification,
            final @NonNull Optional<Bookmark> bookmarkIfAny) {
        return Optional.of(objectSpecification)
        .filter(ObjectSpecification::isViewModel)
        .<T>map(spec->{
            val viewModel = spec.viewmodelFacetElseFail().instantiate(spec, bookmarkIfAny);
            objectLifecyclePublisher().onPostCreate(viewModel);
            return _Casts.uncheckedCast(viewModel.getPojo());
        })
        .orElseThrow(()->_Exceptions.illegalArgument("Type '%s' is not recognized as a ViewModel by the framework.",
                viewModelClass));
    }

    /** handles injection and publishing, but probably not post-construct */
    private <T> T createObject(
            final @NonNull Class<?> type,
            final @NonNull ObjectSpecification spec) {
        val domainObject = spec.createObject();
        return _Casts.uncheckedCast(domainObject.getPojo());
    }

}
