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
package org.apache.isis.webapp.context;

import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.menu.MenuBarsService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.memento.ObjectAdapterMemento;
import org.apache.isis.runtime.memento.ObjectAdapterMementoSupport;
import org.apache.isis.runtime.system.session.IsisSession;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
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
    private final MenuBarsService menuBarsService = lookupServiceElseFail(MenuBarsService.class);
    
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final ObjectAdapterMementoSupport mementoSupport = lookupServiceElseFail(ObjectAdapterMementoSupport.class);
    
    @Getter(lazy = true)
    private final Function<Object, ManagedObject> pojoToAdapter = metaModelContext.getObjectManager()::adapt;
    
    public IsisSession getCurrentSession() {
        return IsisSession.currentOrElseNull();
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
    
    public ObjectAdapterMemento mementoFor(ManagedObject adapter) {
        return ObjectAdapterMemento.ofAdapter(adapter, getMementoSupport());
    }
    
    public ObjectAdapterMemento mementoFor(RootOid rootOid) {
        return ObjectAdapterMemento.ofRootOid(rootOid, getMementoSupport());
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
        
        default ObjectAdapterMementoSupport getMementoSupport() {
            return getCommonContext().getMementoSupport();
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
