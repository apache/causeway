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
package org.apache.isis.core.metamodel._testing;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

import org.springframework.core.env.AbstractEnvironment;

import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.beans.IsisBeanFactoryPostProcessorForSpring;
import org.apache.isis.core.config.beans.IsisBeanTypeClassifier;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.execution.MemberExecutorService;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.objectmanager.ObjectManagerDefault;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.registry.IsisBeanTypeRegistry;
import org.apache.isis.core.metamodel.registry.IsisBeanTypeRegistryDefault;
import org.apache.isis.core.metamodel.services.events.MetamodelEventService;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.SpecificationLoaderDefault;
import org.apache.isis.core.security.authentication.Authentication;
import org.apache.isis.core.security.authentication.AuthenticationContext;
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
    
    private WrapperFactory wrapperFactory;

    private SpecificationLoader specificationLoader;
    
    private ProgrammingModel programmingModel;

    private AuthenticationContext authenticationContext;

    private TranslationService translationService;

    private Authentication authenticationSession;

    private AuthorizationManager authorizationManager;

    private AuthenticationManager authenticationManager;

    private TitleService titleService;

    private RepositoryService repositoryService;

    private FactoryService factoryService;
    
    private MemberExecutorService memberExecutor;

    private TransactionService transactionService;

    private TransactionState transactionState;
    
    private IsisBeanTypeClassifier isisBeanTypeClassifier;
    
    private IsisBeanTypeRegistry isisBeanTypeRegistry;

    private Map<String, ManagedObject> serviceAdaptersById;

    @Singular
    private List<Object> singletons;
    
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
                getConfiguration(),
                getObjectManager(),
                getWrapperFactory(),
                getIsisBeanTypeClassifier(),
                getIsisBeanTypeRegistry(),
                systemEnvironment,
                serviceInjector,
                serviceRegistry,
                metamodelEventService,
                specificationLoader,
                authenticationContext,
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
        val properties = _Maps.<String, String>newHashMap();
        val config = new IsisConfiguration(new AbstractEnvironment() {
            @Override
            public String getProperty(String key) {
                return properties.get(key);
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
    
    private final IsisBeanFactoryPostProcessorForSpring isisBeanFactoryPostProcessorForSpring = 
            new IsisBeanFactoryPostProcessorForSpring();
    
    public IsisBeanTypeClassifier getIsisBeanTypeClassifier() {
        if(isisBeanTypeClassifier==null) {
            isisBeanTypeClassifier = isisBeanFactoryPostProcessorForSpring.getIsisBeanTypeClassifier();
        }
        return isisBeanTypeClassifier;
    }
    
    public IsisBeanTypeRegistry getIsisBeanTypeRegistry() {
        if(isisBeanTypeRegistry==null) {
            isisBeanTypeRegistry = new IsisBeanTypeRegistryDefault(Can.empty());
        }
        return isisBeanTypeRegistry;
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
            val isisBeanTypeClassifier = requireNonNull(getIsisBeanTypeClassifier());
            val isisBeanTypeRegistry = requireNonNull(getIsisBeanTypeRegistry());

            specificationLoader = SpecificationLoaderDefault.getInstance(
                    configuration, 
                    environment, 
                    serviceRegistry,
                    programmingModel,
                    isisBeanTypeClassifier,
                    isisBeanTypeRegistry);
            
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
    
    @Override
    public WrapperFactory getWrapperFactory() {
        if(wrapperFactory==null) {
            wrapperFactory = new WrapperFactory_forTesting();
        }
        return wrapperFactory;
    }

    public void runWithConfigProperties(Consumer<Map<String, String>> setup, Runnable runnable) {
        val properties = _Maps.<String, String>newHashMap();
        setup.accept(properties);
        
        val currentConfigBackup = this.configuration;
        try {
            
            this.configuration = new IsisConfiguration(new AbstractEnvironment() {
                @Override
                public String getProperty(String key) {
                    return properties.get(key);
                }
            });
            
            runnable.run();
        } finally {
            this.configuration = currentConfigBackup;
        }
        
         
                
    }
    

}