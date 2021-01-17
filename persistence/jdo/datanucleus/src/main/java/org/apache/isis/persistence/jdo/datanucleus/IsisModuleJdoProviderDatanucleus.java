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
package org.apache.isis.persistence.jdo.datanucleus;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Provider;
import javax.jdo.PersistenceManagerFactory;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.transaction.changetracking.EntityChangeTracker;
import org.apache.isis.persistence.jdo.datanucleus.config.DnSettings;
import org.apache.isis.persistence.jdo.datanucleus.config.DnEntityDiscoveryListener;
import org.apache.isis.persistence.jdo.datanucleus.entities.DnEntityStateProvider;
import org.apache.isis.persistence.jdo.datanucleus.exceptions.recognizers.ExceptionRecognizerForJDODataStoreException;
import org.apache.isis.persistence.jdo.datanucleus.exceptions.recognizers.ExceptionRecognizerForJDODataStoreExceptionIntegrityConstraintViolationForeignKeyNoActionException;
import org.apache.isis.persistence.jdo.datanucleus.exceptions.recognizers.ExceptionRecognizerForJDOObjectNotFoundException;
import org.apache.isis.persistence.jdo.datanucleus.exceptions.recognizers.ExceptionRecognizerForSQLIntegrityConstraintViolationUniqueOrIndexException;
import org.apache.isis.persistence.jdo.datanucleus.mixins.Persistable_datanucleusIdLong;
import org.apache.isis.persistence.jdo.datanucleus.mixins.Persistable_datanucleusVersionLong;
import org.apache.isis.persistence.jdo.datanucleus.mixins.Persistable_datanucleusVersionTimestamp;
import org.apache.isis.persistence.jdo.datanucleus.mixins.Persistable_downloadJdoMetadata;
import org.apache.isis.persistence.jdo.integration.changetracking.JdoLifecycleListener;
import org.apache.isis.persistence.jdo.integration.jdosupport.JdoSupportServiceDefault;
import org.apache.isis.persistence.jdo.integration.metamodel.JdoIntegrationProgrammingModel;
import org.apache.isis.persistence.jdo.integration.schema.JdoSchemaService;
import org.apache.isis.persistence.jdo.spring.integration.JdoTransactionManager;
import org.apache.isis.persistence.jdo.spring.integration.LocalPersistenceManagerFactoryBean;
import org.apache.isis.persistence.jdo.spring.integration.TransactionAwarePersistenceManagerFactoryProxy;

import lombok.val;

@Configuration
@Import({
    DnEntityDiscoveryListener.class,
    DnEntityStateProvider.class,

    // @Mixin's
    Persistable_datanucleusIdLong.class,
    Persistable_datanucleusVersionLong.class,
    Persistable_datanucleusVersionTimestamp.class,
    Persistable_downloadJdoMetadata.class,

    // @Component's
    JdoIntegrationProgrammingModel.class,

    // @Service's
    DnSettings.class,
    IsisModuleJdoProviderDatanucleus.class,
    JdoSupportServiceDefault.class,
    JdoSchemaService.class,

    ExceptionRecognizerForSQLIntegrityConstraintViolationUniqueOrIndexException.class,
    ExceptionRecognizerForJDODataStoreExceptionIntegrityConstraintViolationForeignKeyNoActionException.class,
    ExceptionRecognizerForJDOObjectNotFoundException.class,
    ExceptionRecognizerForJDODataStoreException.class,

})
public class IsisModuleJdoProviderDatanucleus {

    // reserved for datanucleus' own config props
    @ConfigurationProperties(prefix = "isis.persistence.jdo-datanucleus.impl")
    @Bean("dn-settings")
    public Map<String, String> getAsMap() {
        return new HashMap<>();
    }

    /**
     * {@link TransactionAwarePersistenceManagerFactoryProxy} was retired by the Spring Framework, recommended usage is still online [1].
     * Sources have been recovered from [2].
     * @see [1] https://docs.spring.io/spring-framework/docs/3.0.0.RC2/reference/html/ch13s04.html
     * @see [2] https://github.com/spring-projects/spring-framework/tree/2b3445df8134e2b0c4e4a4c4136cbaf9d58b7fc4/spring-orm/src/main/java/org/springframework/orm/jdo
     */
    @Bean @Primary @Named("transaction-aware-pmf-proxy")
    public TransactionAwarePersistenceManagerFactoryProxy getTransactionAwarePersistenceManagerFactoryProxy(
            final LocalPersistenceManagerFactoryBean localPmfBean) {

        val pmf = localPmfBean.getObject(); // created once per application lifecycle

        val tapmfProxy = new TransactionAwarePersistenceManagerFactoryProxy();
        tapmfProxy.setTargetPersistenceManagerFactory(pmf);
        tapmfProxy.setAllowCreate(false);
        return tapmfProxy;
    }

    @Bean @Named("local-pmf-proxy")
    public LocalPersistenceManagerFactoryBean getLocalPersistenceManagerFactoryBean(
            final MetaModelContext metaModelContext,
            final EventBusService eventBusService,
            final Provider<EntityChangeTracker> entityChangeTrackerProvider,
            final DnSettings dnSettings) {

        //final IsisBeanTypeRegistry beanTypeRegistry,
        // final DnSettings dnSettings,
        //final List<JdoEntityDiscoveryListener> jdoEntityDiscoveryListeners

//      _NullSafe.stream(jdoEntityDiscoveryListeners)
//      .forEach(listener->{
//          listener.onEntitiesDiscovered(pmf, beanTypeRegistry.getEntityTypesJdo(), dnSettings.getAsMap());
//      });

        val lpmfBean = new LocalPersistenceManagerFactoryBean() {
            @Override
            protected PersistenceManagerFactory newPersistenceManagerFactory(java.util.Map<?,?> props) {
                val pmf = super.newPersistenceManagerFactory(props);
                integrateWithApplicationLayer(metaModelContext, eventBusService, entityChangeTrackerProvider, pmf);
                return pmf;
            }
            @Override
            protected PersistenceManagerFactory newPersistenceManagerFactory(String name) {
                val pmf = super.newPersistenceManagerFactory(name);
                integrateWithApplicationLayer(metaModelContext, eventBusService, entityChangeTrackerProvider, pmf);
                return pmf;
            }
        };
        lpmfBean.setJdoPropertyMap(dnSettings.getAsProperties());
        return lpmfBean;
    }

    @Bean @Primary
    @Named("jdo-platform-transaction-manager")
    public JdoTransactionManager getTransactionManager(
            LocalPersistenceManagerFactoryBean localPmfBean) {

        val pmf = localPmfBean.getObject(); // created once per application lifecycle

        return new JdoTransactionManager(pmf);
    }

    // -- HELPER

    private static void integrateWithApplicationLayer(
            final MetaModelContext metaModelContext,
            final EventBusService eventBusService,
            final Provider<EntityChangeTracker> entityChangeTrackerProvider,
            final PersistenceManagerFactory pmf) {

        // install JDO specific entity change listeners ...

        val jdoLifecycleListener = new JdoLifecycleListener(metaModelContext, eventBusService, entityChangeTrackerProvider);
        pmf.addInstanceLifecycleListener(jdoLifecycleListener, (Class[]) null);

    }

}
