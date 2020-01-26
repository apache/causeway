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
package org.apache.isis.core.runtimeservices.homepage;

import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.HomePage;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistryHolder;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.services.homepage.HomePageResolverService;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.val;

@Service
@Order(OrderPrecedence.MIDPOINT)
public class HomePageResolverServiceDefault  implements HomePageResolverService {

    private final SpecificationLoader specificationLoader;
    private final FactoryService factoryService;
    private final IsisBeanTypeRegistryHolder isisBeanTypeRegistryHolder;
    private final ObjectManager objectManager;

    private Optional<Class> viewModelType;

    @Inject
    public HomePageResolverServiceDefault(
            final SpecificationLoader specificationLoader,
            final FactoryService factoryService,
            final IsisBeanTypeRegistryHolder isisBeanTypeRegistryHolder, ObjectManager objectManager) {
        this.specificationLoader = specificationLoader;
        this.factoryService = factoryService;
        this.isisBeanTypeRegistryHolder = isisBeanTypeRegistryHolder;
        this.objectManager = objectManager;
    }

    @PostConstruct
    public void init() {
        val viewModelTypes = isisBeanTypeRegistryHolder.getIsisBeanTypeRegistry().getViewModelTypes();
        viewModelType = viewModelTypes.stream()
                .filter(type -> type.isAnnotationPresent(HomePage.class))
                .map(x -> (Class)x)
                .findFirst();
    }

    @Override
    public Object getHomePage() {
        return viewModelType.map(factoryService::viewModel).orElse(null);
    }


}
