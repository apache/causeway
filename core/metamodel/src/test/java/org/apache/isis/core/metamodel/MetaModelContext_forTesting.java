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
package org.apache.isis.core.metamodel;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

import org.springframework.core.env.AbstractEnvironment;

import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.commons.internal.environment.IsisSystemEnvironment;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistry;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistryHolder;
import org.apache.isis.core.config.unittestsupport.IsisConfigurationLegacy;
import org.apache.isis.core.config.unittestsupport.internal._Config;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.objectmanager.ObjectManagerDefault;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.services.events.MetamodelEventService;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.SpecificationLoaderDefault;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.AuthenticationSessionTracker;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.val;

@Builder @Getter
public final class MetaModelContext_forTesting implements MetaModelContext {
    
    public static MetaModelContext buildDefault() {
        return MetaModelContext_forTesting.builder()
        .build();
    }
    
    private ServiceInjector serviceInjector;
    private ServiceRegistry serviceRegistry; 

    @Builder.Default
    private MetamodelEventService metamodelEventService = 
    MetamodelEventService.builder()
    .build();
    
    @Builder.Default
    private IsisSystemEnvironment systemEnvironment = newIsisSystemEnvironment();
    
    @Builder.Default
    private IsisConfiguration configuration = newIsisConfiguration();
    
    private ObjectManager objectManager;

    private SpecificationLoader specificationLoader;
    
    private ProgrammingModel programmingModel;

    private AuthenticationSessionTracker authenticationSessionTracker;

    private TranslationService translationService;

    private AuthenticationSession authenticationSession;

    private AuthorizationManager authorizationManager;

    private AuthenticationManager authenticationManager;

    private TitleService titleService;

    private RepositoryService repositoryService;

    private FactoryService factoryService;

    private TransactionService transactionService;

    private TransactionState transactionState;
    
    private IsisBeanTypeRegistryHolder isisBeanTypeRegistryHolder;

    private Map<String, ManagedObject> serviceAdaptersById;

    @Singular
    private List<Object> singletons;

    //@Override
    public IsisConfigurationLegacy getConfigurationLegacy() {
        return _Config.getConfiguration();
    }
    
    @Override
    public Stream<ManagedObject> streamServiceAdapters() {

        if(serviceAdaptersById==null) {
            return Stream.empty();
        }
        return serviceAdaptersById.values().stream();
    }

    @Override
    public ManagedObject lookupServiceAdapterById(String serviceId) {
        if(serviceAdaptersById==null) {
            return null;
        }
        return serviceAdaptersById.get(serviceId);
    }
    
    // -- LOOKUP

    @Override
    public <T> T getSingletonElseFail(Class<T> type) {
        return getSystemEnvironment().ioc().getSingletonElseFail(type);
    }
    
    public Stream<Object> streamSingletons() {

        val fields = _Lists.of(
                getConfigurationLegacy(),
                getConfiguration(),
                getObjectManager(),
                getIsisBeanTypeRegistryHolder(),
                systemEnvironment,
                serviceInjector,
                serviceRegistry,
                metamodelEventService,
                specificationLoader,
                authenticationSessionTracker,
                getTranslationService(),
                authenticationSession,
                authorizationManager,
                authenticationManager,
                titleService,
                repositoryService,
                transactionService,
                transactionState,
                this);

        return Stream.concat(fields.stream(), getSingletons().stream())
                .filter(_NullSafe::isPresent);
    }
    
    
    
    private static IsisSystemEnvironment newIsisSystemEnvironment() {
        val env = new IsisSystemEnvironment();
        env.setUnitTesting(true);
        return env;
    }
    
    private static IsisConfiguration newIsisConfiguration() {
        val config = new IsisConfiguration(new AbstractEnvironment() {
            @Override
            public String getProperty(String key) {
                return _Config.getConfiguration().getString(key);
            }
        });
        return config;
    }

    @Override
    public ServiceRegistry getServiceRegistry() {
        if(serviceRegistry==null) {
            serviceRegistry = new ServiceRegistry_forTesting((MetaModelContext)this);
        }
        return serviceRegistry;
    }
    
    @Override
    public ServiceInjector getServiceInjector() {
        if(serviceInjector==null) {
            serviceInjector = new ServiceInjector_forTesting((MetaModelContext)this);
        }
        return serviceInjector;
    }

    @Override
    public FactoryService getFactoryService() {
        if(factoryService==null) {
            factoryService = new FactoryService_forTesting((MetaModelContext)this);
        }
        return factoryService;
    }

    
    @Override
    public TranslationService getTranslationService() {
        if(translationService==null) {
            translationService = new TranslationService_forTesting();
        }
        return translationService;
    }
    
    public IsisBeanTypeRegistryHolder getIsisBeanTypeRegistryHolder() {
        if(isisBeanTypeRegistryHolder==null) {
            
            val typeRegistry = new IsisBeanTypeRegistry();
            
            isisBeanTypeRegistryHolder = new IsisBeanTypeRegistryHolder() {
                @Override
                public IsisBeanTypeRegistry getIsisBeanTypeRegistry() {
                    return typeRegistry;
                }
            };
        }
        return isisBeanTypeRegistryHolder;
    }
    
    @Override
    public SpecificationLoader getSpecificationLoader() {
        if(specificationLoader==null) {
            
            val configuration = requireNonNull(getConfiguration());
            val environment = requireNonNull(getSystemEnvironment());
            val serviceRegistry = requireNonNull(getServiceRegistry());
            @SuppressWarnings("unused")
            val serviceInjector = requireNonNull(getServiceInjector());
            val programmingModel = requireNonNull(getProgrammingModel());
            val isisBeanTypeRegistryHolder = requireNonNull(getIsisBeanTypeRegistryHolder());

            specificationLoader = SpecificationLoaderDefault.getInstance(
                    configuration, 
                    environment, 
                    serviceRegistry,
                    programmingModel,
                    isisBeanTypeRegistryHolder);
            
        }
        return specificationLoader;
    }

    @Override
    public ManagedObject getHomePageAdapter() {
        // not supported
        return null;
    }

    @Override
    public ObjectManager getObjectManager() {
        if(objectManager==null) {
            objectManager = ObjectManagerDefault.forTesting((MetaModelContext)this);
        }
        return objectManager;
    }
    

}