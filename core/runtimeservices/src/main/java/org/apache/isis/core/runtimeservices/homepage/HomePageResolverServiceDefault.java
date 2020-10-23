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
import org.apache.isis.applib.services.homepage.HomePageResolverService;
import org.apache.isis.commons.internal.reflection._Annotations;
import org.apache.isis.core.metamodel.registry.IsisBeanTypeRegistry;

import lombok.val;

@Service
@Order(OrderPrecedence.MIDPOINT)
public class HomePageResolverServiceDefault implements HomePageResolverService {

    private final FactoryService factoryService;
    private final IsisBeanTypeRegistry isisBeanTypeRegistry;

    private Optional<Class<?>> viewModelTypeForHomepage;

    @Inject
    public HomePageResolverServiceDefault(
            final FactoryService factoryService,
            final IsisBeanTypeRegistry isisBeanTypeRegistry) {
        
        this.factoryService = factoryService;
        this.isisBeanTypeRegistry = isisBeanTypeRegistry;
    }

    @PostConstruct
    public void init() {
        val viewModelTypes = isisBeanTypeRegistry.getViewModelTypes();
        viewModelTypeForHomepage = viewModelTypes.stream()
                .filter(viewModelType -> _Annotations.isPresent(viewModelType, HomePage.class)) 
                .findFirst();
    }

    @Override
    public Object getHomePage() {
        return viewModelTypeForHomepage.map(factoryService::viewModel).orElse(null);
    }


}
