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

package org.apache.isis.runtime.services.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.ViewModel;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.system.session.IsisSessionFactory;

import static org.apache.isis.commons.internal.base._With.requires;

import lombok.extern.log4j.Log4j2;
import lombok.val;

@Service
@Named("isisRuntimeServices.FactoryServiceDefault")
@Order(OrderPrecedence.DEFAULT)
@Primary
@Log4j2
public class FactoryServiceDefault implements FactoryService {
    
    @Inject IsisSessionFactory isisSessionFactory; // dependsOn
    @Inject private SpecificationLoader specificationLoader;
    @Inject private ServiceInjector serviceInjector;

    @Override
    public <T> T instantiate(final Class<T> domainClass) {
        final ObjectSpecification spec = specificationLoader.loadSpecification(domainClass);
        final ManagedObject adapter = ManagedObject._newTransientInstance(spec); 
        return _Casts.uncheckedCast(adapter.getPojo());
    }

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

    @Override
    public <T> T viewModel(Class<T> viewModelClass, String mementoStr) {
        requires(viewModelClass, "viewModelClass");

        val spec = specificationLoader.loadSpecification(viewModelClass);
        if (!spec.containsFacet(ViewModelFacet.class)) {
            val msg = String.format("Type '%s' must be recogniced as a ViewModel, that is the type's meta-model "
                    + "must have an associated ViewModelFacet: ", viewModelClass.getName());
            throw new IllegalArgumentException(msg);
        }

        if(ViewModel.class.isAssignableFrom(viewModelClass)) {
            //FIXME[2152] is this execution branch required, or does the below code suffice for all cases?
            val viewModel = (ViewModel) instantiate(viewModelClass);
            viewModel.viewModelInit(mementoStr);
            return _Casts.uncheckedCast(viewModel);
        }

        val viewModelFacet = spec.getFacet(ViewModelFacet.class);
        val viewModel = viewModelFacet.createViewModelPojo(spec, mementoStr, __->instantiate(viewModelClass));

        return _Casts.uncheckedCast(viewModel);
    }






}
