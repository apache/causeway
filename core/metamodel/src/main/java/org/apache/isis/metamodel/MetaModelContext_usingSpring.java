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
package org.apache.isis.metamodel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.ioc.spring._Spring;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.IsisConfigurationLegacy;
import org.apache.isis.config.internal._Config;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.metamodel.services.ServiceUtil;
import org.apache.isis.metamodel.services.homepage.HomePageAction;
import org.apache.isis.metamodel.services.homepage.HomePageResolverService;
import org.apache.isis.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.security.authentication.AuthenticationSession;
import org.apache.isis.security.authentication.AuthenticationSessionProvider;
import org.apache.isis.security.authentication.manager.AuthenticationManager;
import org.apache.isis.security.authorization.manager.AuthorizationManager;

import lombok.Getter;
import lombok.val;

class MetaModelContext_usingSpring implements MetaModelContext {

    @Getter(lazy=true) 
    private final IsisConfigurationLegacy configurationLegacy = 
    _Config.getConfiguration();

    @Getter(lazy=true) 
    private final IsisConfiguration configuration = 
    _Spring.getSingletonElseFail(IsisConfiguration.class);

    @Getter(lazy=true) 
    private final ObjectAdapterProvider objectAdapterProvider =
    _Spring.getSingletonElseFail(PersistenceSessionServiceInternal.class);

    @Getter(lazy=true) 
    private final ServiceInjector serviceInjector =
    _Spring.getSingletonElseFail(ServiceInjector.class);

    @Getter(lazy=true) 
    private final ServiceRegistry serviceRegistry =
    _Spring.getSingletonElseFail(ServiceRegistry.class);

    @Getter(lazy=true) 
    private final SpecificationLoader specificationLoader = 
    _Spring.getSingletonElseFail(SpecificationLoader.class);

    @Getter(lazy=true) 
    private final AuthenticationSessionProvider authenticationSessionProvider =
    _Spring.getSingletonElseFail(AuthenticationSessionProvider.class);

    @Getter(lazy=true) 
    private final TranslationService translationService =
    _Spring.getSingletonElseFail(TranslationService.class);

    @Getter(lazy=true) 
    private final AuthorizationManager authorizationManager =
    _Spring.getSingletonElseFail(AuthorizationManager.class); 

    @Getter(lazy=true) 
    private final AuthenticationManager authenticationManager =
    _Spring.getSingletonElseFail(AuthenticationManager.class);

    @Getter(lazy=true) 
    private final TitleService titleService =
    _Spring.getSingletonElseFail(TitleService.class);

    //        @Getter(lazy=true) 
    //        private final ObjectAdapterService objectAdapterService =
    //        _CDI.getSingletonElseFail(ObjectAdapterService.class);

    @Getter(lazy=true) 
    private final RepositoryService repositoryService =
    _Spring.getSingletonElseFail(RepositoryService.class);

    @Getter(lazy=true) 
    private final TransactionService transactionService =
    _Spring.getSingletonElseFail(TransactionService.class);

    @Getter(lazy=true) 
    private final HomePageResolverService homePageResolverService =
    _Spring.getSingletonElseFail(HomePageResolverService.class);

    @Override
    public final AuthenticationSession getAuthenticationSession() {
        return getAuthenticationSessionProvider().getAuthenticationSession();
    }

    @Override
    public final ObjectSpecification getSpecification(final Class<?> type) {
        return type != null ? getSpecificationLoader().loadSpecification(type) : null;
    }

    @Override
    public final TransactionState getTransactionState() {
        return getTransactionService().currentTransactionState();
    }

    @Override
    public final HomePageAction getHomePageAction() {
        return getHomePageResolverService().getHomePageAction();
    }

    // -- SERVICE SUPPORT

    @Override
    public Stream<ObjectAdapter> streamServiceAdapters() {
        return objectAdaptersForBeansOfKnownSort.get().values().stream();
    }

    @Override
    public ObjectAdapter lookupServiceAdapterById(final String serviceId) {
        return objectAdaptersForBeansOfKnownSort.get().get(serviceId);
    }


    // -- HELPER

    private final _Lazy<Map<String, ObjectAdapter>> objectAdaptersForBeansOfKnownSort = 
            _Lazy.threadSafe(this::collectBeansOfKnownSort);

    private Map<String, ObjectAdapter> collectBeansOfKnownSort() {

        val objectAdapterProvider = getObjectAdapterProvider();

        return getServiceRegistry()
                .streamRegisteredBeans()
                .map(objectAdapterProvider::adapterForBean) 
                .peek(this::guardAgainsTransient)
                .collect(Collectors.toMap(ServiceUtil::idOfAdapter, v->v, (o,n)->n, LinkedHashMap::new));
    }

    private void guardAgainsTransient(ObjectAdapter objectAdapter) {
        val oid = objectAdapter.getOid();
        if(oid.isTransient()) {
            val msg = "ObjectAdapter for 'Bean' is expected not to be 'transient' " + oid;
            throw _Exceptions.unrecoverable(msg);
        }
    }


}