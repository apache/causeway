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
package org.apache.isis.core.webapp.context;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.menu.MenuBarsService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.viewer.wicket.WebAppContextPath;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.session.IsisSessionTracker;
import org.apache.isis.core.security.authentication.MessageBroker;
import org.apache.isis.core.webapp.context.memento.ObjectMemento;
import org.apache.isis.core.webapp.context.memento.ObjectMementoService;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * 
 * @since 2.0
 *
 */
@Log4j2
public class IsisWebAppCommonContext implements MetaModelContext.Delegating {

    /**
     * Can be bootstrapped from a {@link MetaModelContext}
     */
    public static IsisWebAppCommonContext of(MetaModelContext metaModelContext) {
        val webAppCommonContext = new IsisWebAppCommonContext();
        webAppCommonContext.metaModelContext = metaModelContext;
        return webAppCommonContext;
    }
    
    @Getter(onMethod = @__(@Override))
    private MetaModelContext metaModelContext;
    
    @Getter(lazy = true)
    private final WebAppContextPath webAppContextPath = lookupServiceElseFail(WebAppContextPath.class);
    
    @Getter(lazy = true)
    private final MenuBarsService menuBarsService = lookupServiceElseFail(MenuBarsService.class);
    
    @Getter(lazy = true)
    private final IsisSessionTracker isisSessionTracker = lookupServiceElseFail(IsisSessionTracker.class);
    
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final ObjectMementoService mementoService = lookupServiceElseFail(ObjectMementoService.class);
    
    @Getter(lazy = true)
    private final Function<Object, ManagedObject> pojoToAdapter = metaModelContext.getObjectManager()::adapt;
    
    public Optional<MessageBroker> getMessageBroker() {
        val messageBroker = getAuthenticationSessionTracker().currentMessageBroker();
        if(!messageBroker.isPresent()) {
            log.warn("failed to locate a MessageBroker on current AuthenticationSession");
        }
        return messageBroker;
    }
    
    // -- SHORTCUTS
    
    public <T> T lookupServiceElseFail(Class<T> serviceClass) {
        return getMetaModelContext().getServiceRegistry().lookupServiceElseFail(serviceClass);
    }
    
    public <T> T lookupServiceElseFallback(Class<T> serviceClass, Supplier<T> fallback) {
        return getMetaModelContext().getServiceRegistry().lookupService(serviceClass)
                .orElseGet(fallback);
    }
    
    public <T> T injectServicesInto(T pojo) {
        return getMetaModelContext().getServiceInjector().injectServicesInto(pojo);
    }
    
    public TransactionTemplate createTransactionTemplate() {
        val txMan = lookupServiceElseFail(PlatformTransactionManager.class);
        return new TransactionTemplate(txMan);
    }
    
    public ObjectMemento mementoFor(ManagedObject adapter) {
        return getMementoService().mementoForObject(adapter);
    }
    
    public ObjectMemento mementoFor(RootOid rootOid) {
        return getMementoService().mementoForRootOid(rootOid);
    }
    
    public ManagedObject reconstructObject(ObjectMemento memento) {
        return getMementoService().reconstructObject(memento);
    }
    
    // -- COMMON CONTEXT PROVIDER INTERFACE
    
    public static interface Provider {
        IsisWebAppCommonContext getCommonContext();
    }
    
    // -- FOR THOSE THAT IMPLEMENT BY DELEGATION
    
    public static interface Delegating {
        
        IsisWebAppCommonContext getCommonContext();
        
        default IsisConfiguration getConfiguration() {
            return getCommonContext().getConfiguration();
        }
        
        default ServiceRegistry getServiceRegistry() {
            return getCommonContext().getServiceRegistry();
        }
        
        default SpecificationLoader getSpecificationLoader() {
            return getCommonContext().getSpecificationLoader();
        }
        
        default ObjectMementoService getMementoService() {
            return getCommonContext().getMementoService();
        }
        
        default Function<Object, ManagedObject> getPojoToAdapter() {
            return pojo->ManagedObject.of(
                    getCommonContext().getSpecificationLoader()::loadSpecification, pojo);
        }
        
        default ServiceInjector getServiceInjector() {
            return getCommonContext().getServiceInjector();
        }
        
    }
    
    
}
