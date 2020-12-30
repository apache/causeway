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
package org.apache.isis.persistence.jdo.lightweight;

import java.util.List;

import javax.inject.Named;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistry;
import org.apache.isis.core.runtime.IsisModuleCoreRuntime;
import org.apache.isis.persistence.jdo.applib.IsisModulePersistenceJdoApplib;
import org.apache.isis.persistence.jdo.datanucleus.IsisModuleJdoProviderDatanucleus;
import org.apache.isis.persistence.jdo.datanucleus.config.DnSettings;
import org.apache.isis.persistence.jdo.lightweight.metamodel.JdoLightweightProgrammingModel;
import org.apache.isis.persistence.jdo.lightweight.services.LightweightJdoSupport;
import org.apache.isis.persistence.jdo.metamodel.IsisModuleJdoMetamodel;
import org.apache.isis.persistence.jdo.provider.config.JdoEntityDiscoveryListener;
import org.apache.isis.persistence.jdo.spring.IsisModuleJdoSpring;
import org.apache.isis.persistence.jdo.spring.integration.JdoTransactionManager;
import org.apache.isis.persistence.jdo.spring.integration.LocalPersistenceManagerFactoryBean;
import org.apache.isis.persistence.jdo.spring.integration.TransactionAwarePersistenceManagerFactoryProxy;

import lombok.val;

@Configuration
@Import({
    // modules
    IsisModuleCoreRuntime.class,
    IsisModulePersistenceJdoApplib.class,
    IsisModuleJdoMetamodel.class,
    IsisModuleJdoSpring.class,
    IsisModuleJdoProviderDatanucleus.class,

    // @Component's
    JdoLightweightProgrammingModel.class,
    
    // @Service's
    LightweightJdoSupport.class

})
public class IsisModuleJdoLightweight {
    
    /**
     * {@link TransactionAwarePersistenceManagerFactoryProxy} was retired by the Spring Framework, recommended usage is still online [1].
     * Sources have been recovered from [2].
     * @see [1] https://docs.spring.io/spring-framework/docs/3.0.0.RC2/reference/html/ch13s04.html
     * @see [2] https://github.com/spring-projects/spring-framework/tree/2b3445df8134e2b0c4e4a4c4136cbaf9d58b7fc4/spring-orm/src/main/java/org/springframework/orm/jdo
     */
    @Bean @Named("transaction-aware-pmf-proxy")
    public TransactionAwarePersistenceManagerFactoryProxy getTransactionAwarePersistenceManagerFactoryProxy(
            final LocalPersistenceManagerFactoryBean lpmfBean,
            final IsisBeanTypeRegistry beanTypeRegistry,
            final DnSettings dnSettings,
            final List<JdoEntityDiscoveryListener> jdoEntityDiscoveryListeners) {
        
        val pmf = lpmfBean.getObject();
        
        _NullSafe.stream(jdoEntityDiscoveryListeners)
        .forEach(listener->{
            listener.onEntitiesDiscovered(pmf, beanTypeRegistry.getEntityTypesJdo(), dnSettings.getAsMap());    
        });
        
        val tapmfProxy = new TransactionAwarePersistenceManagerFactoryProxy();
        tapmfProxy.setTargetPersistenceManagerFactory(pmf);
        tapmfProxy.setAllowCreate(false);
        return tapmfProxy;
    }
    
    @Bean 
    public LocalPersistenceManagerFactoryBean getLocalPersistenceManagerFactoryBean(
            final DnSettings dnSettings) {
        
        val lpmfBean = new LocalPersistenceManagerFactoryBean();
        lpmfBean.setJdoPropertyMap(dnSettings.getAsProperties());
        return lpmfBean; 
    }

    @Bean @Primary
    public JdoTransactionManager getJdoTransactionManager(LocalPersistenceManagerFactoryBean localPmf) {
        return new JdoTransactionManager(localPmf.getObject());
    }
    
}
