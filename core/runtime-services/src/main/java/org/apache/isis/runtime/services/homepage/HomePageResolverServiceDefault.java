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
package org.apache.isis.runtime.services.homepage;

import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.HomePage;
import org.apache.isis.applib.annotation.ViewModel;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.ioc.BeanSort;
import org.apache.isis.config.registry.IsisBeanTypeRegistry;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facets.actions.homepage.HomePageFacet;
import org.apache.isis.metamodel.services.homepage.HomePageAction;
import org.apache.isis.metamodel.services.homepage.HomePageResolverService;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAction;

import lombok.val;

@Service
public class HomePageResolverServiceDefault implements HomePageResolverService {

    @Inject FactoryService factoryService;
    @Inject ServiceRegistry serviceRegistry;

    @Override
    public HomePageAction getHomePageAction() {
        return homePage.get();
    }

    private final _Lazy<HomePageAction> homePage = _Lazy.threadSafe(this::lookupHomePageAction);

    private HomePageAction lookupHomePageAction() {

        val metaModelContext = MetaModelContext.current();
        val specLoader = metaModelContext.getSpecificationLoader();

        val viewModelTypes = IsisBeanTypeRegistry.current().getViewModelTypes();

        // -- 1) lookup view-models that are type annotated with @HomePage

        HomePageAction homePageAction = viewModelTypes.stream()
                .map(viewModelType->homePageViewModelIfUsable(viewModelType))
                .filter(_NullSafe::isPresent)
                .findFirst()
                .orElse(null);

        if(homePageAction!=null) {
            return homePageAction;
        }

        val specRef = new ObjectSpecification[] {null}; // simple object reference

        // -- 2) lookup managed beans that have actions annotated with @HomePage

        homePageAction = 
                serviceRegistry.streamRegisteredBeansOfSort(BeanSort.MANAGED_BEAN)
                .map(bean->bean.getBeanClass())
                .map(managedBeanType->specLoader.loadSpecification(managedBeanType))
                .filter(_NullSafe::isPresent)
                .peek(spec->specRef[0]=spec)
                .flatMap(spec->spec.streamObjectActions(Contributed.EXCLUDED))
                .map(objectAction->homePageActionIfUsable(objectAction, specRef[0]))
                .filter(_NullSafe::isPresent)
                .findAny()
                .orElse(null);

        // -- 3) lookup view-models that have actions annotated with @HomePage

        homePageAction = viewModelTypes.stream()
                .map(viewModelType->specLoader.loadSpecification(viewModelType))
                .filter(_NullSafe::isPresent)
                .peek(spec->specRef[0]=spec)
                .flatMap(spec->spec.streamObjectActions(Contributed.EXCLUDED))
                .map(objectAction->homePageActionIfUsable(objectAction, specRef[0]))
                .filter(_NullSafe::isPresent)
                .findAny()
                .orElse(null);

        if(homePageAction!=null) {
            return homePageAction;
        }

        return homePageAction;
    }

    @Vetoed @ViewModel
    public static class HomePageActionContainer {

        @Inject private FactoryService factoryService;
        private static Class<?> viewModelType;

        @Action @HomePage
        public Object homePage() {
            val viewModelPojo = factoryService.instantiate(viewModelType);
            return viewModelPojo;
        }
    }


    protected HomePageAction homePageViewModelIfUsable(Class<?> type) {
        if(!type.isAnnotationPresent(HomePage.class)) {
            return null; 
        }

        val metaModelContext = MetaModelContext.current();
        val specLoader = metaModelContext.getSpecificationLoader();
        val spec = specLoader.loadSpecification(type);
        if(!spec.isViewModel()) {
            return null;
        }

        HomePageActionContainer.viewModelType = type;

        val containerSpec = specLoader.loadSpecification(HomePageActionContainer.class);

        val homePageAction = containerSpec.streamObjectActions(Contributed.EXCLUDED)
                .map(objectAction->homePageActionIfUsable(objectAction, containerSpec))
                .filter(_NullSafe::isPresent)
                .findAny()
                .orElse(null);

        return homePageAction;
    }

    protected HomePageAction homePageActionIfUsable(ObjectAction objectAction, ObjectSpecification spec) {

        if (!objectAction.containsDoOpFacet(HomePageFacet.class)) {
            return null;
        }

        val metaModelContext = MetaModelContext.current();
        val objectAdapterProvider = metaModelContext.getObjectAdapterProvider();

        final ObjectAdapter adapterForHomePageActionDeclaringPojo;

        if(spec.isViewModel()) {
            val viewModelPojo = factoryService.instantiate(spec.getCorrespondingClass());
            adapterForHomePageActionDeclaringPojo = objectAdapterProvider.adapterFor(viewModelPojo);
        } else if(spec.isManagedBean()) {

            adapterForHomePageActionDeclaringPojo = 
                    serviceRegistry.streamRegisteredBeansOfType(spec.getCorrespondingClass())
                    .map(objectAdapterProvider::adapterForBean)
                    .findFirst()
                    .orElseThrow(_Exceptions::unexpectedCodeReach);

        } else {
            throw _Exceptions.unexpectedCodeReach();
        }

        final Consent visibility =
                objectAction.isVisible(
                        adapterForHomePageActionDeclaringPojo,
                        InteractionInitiatedBy.USER,
                        Where.ANYWHERE);
        if (visibility.isVetoed()) {
            return null;
        }

        final Consent usability =
                objectAction.isUsable(
                        adapterForHomePageActionDeclaringPojo,
                        InteractionInitiatedBy.USER,
                        Where.ANYWHERE
                        );
        if (usability.isVetoed()) {
            return null;
        }

        return HomePageAction.of(adapterForHomePageActionDeclaringPojo, objectAction);
    }



}
