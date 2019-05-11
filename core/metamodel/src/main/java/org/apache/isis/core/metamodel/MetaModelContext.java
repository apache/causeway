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

import java.util.stream.Stream;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.metamodel.MetaModelContext_forTesting.MetaModelContext_forTestingBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;

/**
 * 
 * @since 2.0.0-M2
 *
 */
public interface MetaModelContext {

    // -- INTERFACE
    
    IsisConfiguration getConfiguration();
    
    ObjectAdapterProvider getObjectAdapterProvider();

    ServiceInjector getServiceInjector();

    ServiceRegistry getServiceRegistry();

    SpecificationLoader getSpecificationLoader();

    AuthenticationSessionProvider getAuthenticationSessionProvider();

    TranslationService getTranslationService();

    AuthenticationSession getAuthenticationSession();

    AuthorizationManager getAuthorizationManager();

    AuthenticationManager getAuthenticationManager();

    TitleService getTitleService();

    ObjectSpecification getSpecification(Class<?> type);
    
    RepositoryService getRepositoryService();
    
    TransactionService getTransactionService();
    
    TransactionState getTransactionState();
    
	Stream<ObjectAdapter> streamServiceAdapters();
	
	ObjectAdapter lookupServiceAdapterById(String serviceId);

    // -- PRESET INSTANCES
    
    static MetaModelContext current() {
        return _Context.computeIfAbsent(MetaModelContext.class, 
                __->MetaModelContexts.usingSpring()); // default
    }
    
    static void preset(MetaModelContext metaModelContext) {
        _Context.clear();
        _Context.putSingleton(MetaModelContext.class, metaModelContext);
    }
    
    static MetaModelContext_forTestingBuilder builder() {
        return MetaModelContext_forTesting.builder();
    }
    
    // -- DELEGATION - FOR THOSE THAT IMPLEMENT THROUGH DELEGATION
    
    public static interface Delegating extends MetaModelContext {
        
        public MetaModelContext getMetaModelContext();
        
        @Override
        public default IsisConfiguration getConfiguration() {
            return getMetaModelContext().getConfiguration();
        }
        
        @Override
        public default ObjectAdapterProvider getObjectAdapterProvider() {
            return getMetaModelContext().getObjectAdapterProvider();
        }

        @Override
        public default ServiceInjector getServiceInjector() {
            return getMetaModelContext().getServiceInjector();
        }

        @Override
        public default ServiceRegistry getServiceRegistry() {
            return getMetaModelContext().getServiceRegistry();
        }

        @Override
        public default SpecificationLoader getSpecificationLoader() {
            return getMetaModelContext().getSpecificationLoader();
        }

        @Override
        public default AuthenticationSessionProvider getAuthenticationSessionProvider() {
            return getMetaModelContext().getAuthenticationSessionProvider();
        }

        @Override
        public default TranslationService getTranslationService() {
            return getMetaModelContext().getTranslationService();
        }

        @Override
        public default AuthenticationSession getAuthenticationSession() {
            return getMetaModelContext().getAuthenticationSession();
        }

        @Override
        public default AuthorizationManager getAuthorizationManager() {
            return getMetaModelContext().getAuthorizationManager();
        }

        @Override
        public default AuthenticationManager getAuthenticationManager() {
            return getMetaModelContext().getAuthenticationManager();
        }

        @Override
        public default TitleService getTitleService() {
            return getMetaModelContext().getTitleService();
        }

        @Override
        public default ObjectSpecification getSpecification(Class<?> type) {
            return getMetaModelContext().getSpecification(type);
        }
        
        @Override
        public default RepositoryService getRepositoryService() {
            return getMetaModelContext().getRepositoryService();
        }
        
        @Override
        public default TransactionState getTransactionState() {
            return getMetaModelContext().getTransactionState();
        }
        
        @Override
        public default TransactionService getTransactionService() {
            return getMetaModelContext().getTransactionService();
        }
        
        @Override
        public default Stream<ObjectAdapter> streamServiceAdapters() {
        	return streamServiceAdapters();
        }
        
        @Override
        default ObjectAdapter lookupServiceAdapterById(String serviceId) {
        	return lookupServiceAdapterById(serviceId);
        }
        
    }
    
}
