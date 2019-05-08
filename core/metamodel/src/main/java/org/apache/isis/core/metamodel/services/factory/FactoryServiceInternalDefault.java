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

package org.apache.isis.core.metamodel.services.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        )
public class FactoryServiceInternalDefault implements FactoryService {


    @Programmatic
    @Override
    @SuppressWarnings("unchecked")
    public <T> T instantiate(final Class<T> domainClass) {
        final ObjectSpecification spec = specificationLoader.loadSpecification(domainClass);
        final ObjectAdapter adapter = doCreateTransientInstance(spec);
        return (T) adapter.getPojo();
    }

    /**
     * Factored out as a potential hook method for subclasses.
     */
    protected ObjectAdapter doCreateTransientInstance(final ObjectSpecification spec) {
        return persistenceSessionServiceInternal.createTransientInstance(spec);
    }


    @Programmatic
    @Override
    public <T> T m(final Class<T> mixinClass, final Object mixedIn) {
        return mixin(mixinClass, mixedIn);
    }


    @Programmatic
    @Override
    public <T> T mixin(final Class<T> mixinClass, final Object mixedIn) {
        final ObjectSpecification objectSpec = specificationLoader.loadSpecification(mixinClass);
        final MixinFacet mixinFacet = objectSpec.getFacet(MixinFacet.class);
        if(mixinFacet == null) {
            throw new NonRecoverableException("Class '" + mixinClass.getName() + " is not a mixin");
        }
        if(!mixinFacet.isMixinFor(mixedIn.getClass())) {
            throw new NonRecoverableException("Mixin class '" + mixinClass.getName() + " is not a mixin for supplied object '" + mixedIn + "'");
        }
        final Constructor<?>[] constructors = mixinClass.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if(constructor.getParameterTypes().length == 1 &&
                    constructor.getParameterTypes()[0].isAssignableFrom(mixedIn.getClass())) {
                final Object mixin;
                try {
                    mixin = constructor.newInstance(mixedIn);
                    return _Casts.uncheckedCast(serviceInjector.injectServicesInto(mixin));
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new NonRecoverableException(e);
                }
            }
        }
        // should never get here because of previous guards
        throw new NonRecoverableException( String.format(
                "Failed to locate constructor in %s to instantiate using %s", mixinClass.getName(), mixedIn));
    }

    @javax.inject.Inject
    SpecificationLoader specificationLoader;

    @javax.inject.Inject
    ServiceRegistry serviceRegistry;

    @javax.inject.Inject
    ServiceInjector serviceInjector;
    
    @javax.inject.Inject
    PersistenceSessionServiceInternal persistenceSessionServiceInternal;

}
