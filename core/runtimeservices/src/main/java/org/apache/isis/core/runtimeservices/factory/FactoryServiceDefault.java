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
package org.apache.isis.core.runtimeservices.factory;

import java.lang.reflect.InvocationTargetException;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.services.objectlifecycle.ObjectLifecyclePublisher;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import static org.apache.isis.commons.internal.reflection._Reflect.Filter.paramAssignableFrom;
import static org.apache.isis.commons.internal.reflection._Reflect.Filter.paramCount;

import lombok.NonNull;
import lombok.val;

@Service
@Named("isis.runtimeservices.FactoryServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class FactoryServiceDefault implements FactoryService {

    @Inject InteractionService interactionService; // dependsOn
    @Inject private SpecificationLoader specificationLoader;
    @Inject private ServiceInjector serviceInjector;
    @Inject private IsisSystemEnvironment isisSystemEnvironment;
    @Inject private ObjectLifecyclePublisher objectLifecyclePublisher;

    @Override
    public <T> T getOrCreate(final @NonNull Class<T> requiredType) {
        val spec = loadSpec(requiredType);
        if(spec.isManagedBean()) {
            return get(requiredType);
        }
        return create(requiredType);
    }

    @Override
    public <T> T get(final @NonNull Class<T> requiredType) {
        return isisSystemEnvironment.getIocContainer()
                .get(requiredType)
                .orElseThrow(_Exceptions::noSuchElement);
    }

    @Override
    public <T> T detachedEntity(final @NonNull Class<T> domainClass) {
        val entitySpec = loadSpec(domainClass);
        if(!entitySpec.isEntity()) {
            throw _Exceptions.illegalArgument("Class '%s' is not an entity", domainClass.getName());
        }
        return _Casts.uncheckedCast(createObject(entitySpec));
    }

    @Override
    public <T> T detachedEntity(final @NonNull T entityPojo) {
        val entityClass = entityPojo.getClass();
        val spec = loadSpec(entityClass);
        if(!spec.isEntity()) {
            throw _Exceptions.illegalArgument("Type '%s' is not recogniced as an entity type by the framework.",
                    entityClass);
        }
        serviceInjector.injectServicesInto(entityPojo);
        objectLifecyclePublisher.onPostCreate(ManagedObject.of(spec, entityPojo));
        return entityPojo;
    }

    @Override
    public <T> T mixin(final @NonNull Class<T> mixinClass, final @NonNull Object mixedIn) {
        val mixinSpec = loadSpec(mixinClass);
        val mixinFacet = mixinSpec.getFacet(MixinFacet.class);
        if(mixinFacet == null) {
            throw _Exceptions.illegalArgument("Class '%s' is not a mixin", mixinClass.getName());
        }
        if(mixinSpec.isAbstract()) {
            throw _Exceptions.illegalArgument("Cannot instantiate abstract type '%s' as a mixin", mixinClass.getName());
        }
        if(!mixinFacet.isMixinFor(mixedIn.getClass())) {
            throw _Exceptions.illegalArgument("Mixin class '%s' is not a mixin for supplied object '%s'",
                    mixinClass.getName(), mixedIn);
        }
        val mixinConstructor = _Reflect
                .getPublicConstructors(mixinClass)
                        .filter(paramCount(1).and(paramAssignableFrom(0, mixedIn.getClass())))
                .getSingleton()
                .orElseThrow(()->_Exceptions.illegalArgument(
                        "Failed to locate constructor in '%s' to instantiate using '%s'",
                        mixinClass.getName(), mixedIn));

        try {
            val mixin = mixinConstructor.newInstance(mixedIn);
            return _Casts.uncheckedCast(serviceInjector.injectServicesInto(mixin));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw _Exceptions.illegalArgument(
                    "Failed to invoke constructor of '%s' using single argument '%s'",
                    mixinClass.getName(), mixedIn);
        }
    }

    @Override
    public <T> T viewModel(final @NonNull T viewModelPojo) {
        val viewModelClass = viewModelPojo.getClass();
        val spec = loadSpec(viewModelClass);
        if(!spec.isViewModel()) {
            throw _Exceptions.illegalArgument("Type '%s' is not recogniced as a ViewModel by the framework.",
                    viewModelClass);
        }
        serviceInjector.injectServicesInto(viewModelPojo);
        objectLifecyclePublisher.onPostCreate(ManagedObject.of(spec, viewModelPojo));
        return viewModelPojo;
    }

    @Override
    public <T> T viewModel(final @NonNull Class<T> viewModelClass, final @Nullable Bookmark bookmark) {

        val spec = loadSpec(viewModelClass);
        val viewModelFacet = getViewModelFacet(spec);
        val viewModel = viewModelFacet.createViewModelPojo(spec, bookmark, __->createObject(spec));
        return _Casts.uncheckedCast(viewModel);
    }

    @Override
    public <T> T create(final @NonNull Class<T> domainClass) {
        val spec = loadSpec(domainClass);

        if(spec.isManagedBean()) {
            throw _Exceptions.illegalArgument(
                    "Class '%s' is managed by IoC container, use get() instead", domainClass.getName());
        }
        return _Casts.uncheckedCast(createObject(spec));
    }

    // -- HELPER

    private ObjectSpecification loadSpec(final @NonNull Class<?> type) {
        return specificationLoader.specForTypeElseFail(type);
    }

    private ViewModelFacet getViewModelFacet(final @NonNull ObjectSpecification spec) {
        val viewModelFacet = spec.getFacet(ViewModelFacet.class);
        if(viewModelFacet==null) {
            throw _Exceptions.illegalArgument("Type '%s' must be recogniced as a ViewModel, "
                    + "that is the type's meta-model "
                    + "must have an associated ViewModelFacet: ", spec.getCorrespondingClass());
        }
        return viewModelFacet;
    }

    private Object createObject(final ObjectSpecification spec) {
        // already handles injection and publishing
        val domainObject = spec.createObject();
        return domainObject.getPojo();
    }

}
