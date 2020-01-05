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
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.HomePage;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.ioc.ManagedBeanAdapter;
import org.apache.isis.config.beans.IsisBeanTypeRegistryHolder;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facets.actions.homepage.HomePageFacet;
import org.apache.isis.metamodel.services.homepage.HomePageAction;
import org.apache.isis.metamodel.services.homepage.HomePageResolverService;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.specloader.SpecificationLoader;

import lombok.val;

@Service
@Named("isisRuntimeServices.HomePageResolverServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class HomePageResolverServiceDefault implements HomePageResolverService {

    @Inject private FactoryService factoryService;
    @Inject private ServiceRegistry serviceRegistry;
    @Inject private SpecificationLoader specLoader;
    @Inject private IsisBeanTypeRegistryHolder isisBeanTypeRegistryHolder;

    @Override
    public HomePageAction getHomePageAction() {
        return homePage.get();
    }

    private final _Lazy<HomePageAction> homePage = _Lazy.threadSafe(this::lookupHomePageAction);

    private HomePageAction lookupHomePageAction() {

        val viewModelTypes = isisBeanTypeRegistryHolder.getIsisBeanTypeRegistry().getViewModelTypes();

        // -- 1) lookup view-models that are type annotated with @HomePage

        HomePageAction homePageAction = viewModelTypes.stream()
                .map(this::homePageViewModelIfUsable)
                .filter(_NullSafe::isPresent)
                .findFirst()
                .orElse(null);

        if(homePageAction!=null) {
            return homePageAction;
        }

        val specRef = new ObjectSpecification[] {null}; // simple object reference

        // -- 2) lookup managed beans that have actions annotated with @HomePage

        homePageAction = 
                serviceRegistry.streamRegisteredBeans()
                .map(ManagedBeanAdapter::getBeanClass)
                .map(specLoader::loadSpecification)
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
        
        // -- 3) lookup view-models that have actions annotated with @HomePage

        homePageAction = viewModelTypes.stream()
                .map(specLoader::loadSpecification)
                .filter(_NullSafe::isPresent)
                .peek(spec->specRef[0]=spec)
                .flatMap(spec->spec.streamObjectActions(Contributed.EXCLUDED))
                .map(objectAction->homePageActionIfUsable(objectAction, specRef[0]))
                .filter(_NullSafe::isPresent)
                .findAny()
                .orElse(null);

        return homePageAction;
    }

    @Vetoed
    @DomainObject(
            nature = Nature.INMEMORY_ENTITY, 
            objectType = "isisRuntimeServices.HomePageResolverServiceDefault.HomePageActionContainer")
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

        if (!objectAction.containsNonFallbackFacet(HomePageFacet.class)) {
            return null;
        }

        final ManagedObject adapterForHomePageActionDeclaringPojo;

        if(spec.isViewModel()) {
            val viewModelPojo = factoryService.instantiate(spec.getCorrespondingClass());
            adapterForHomePageActionDeclaringPojo = ManagedObject.of(spec, viewModelPojo);
        } else if(spec.isManagedBean()) {

            adapterForHomePageActionDeclaringPojo = 
                    serviceRegistry.streamRegisteredBeansOfType(spec.getCorrespondingClass())
                    .filter(bean->bean.getInstance().getFirst().isPresent())
                    .map(bean->ManagedObject.of(spec, bean.getInstance().getFirst().get()))
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
