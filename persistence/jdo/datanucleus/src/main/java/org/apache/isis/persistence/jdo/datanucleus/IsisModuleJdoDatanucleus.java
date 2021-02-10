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

import javax.inject.Provider;
import javax.jdo.JDOException;
import javax.jdo.PersistenceManagerFactory;
import javax.sql.DataSource;

import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.api.jdo.NucleusJDOHelper;
import org.datanucleus.exceptions.NucleusException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.commons.functional.Result;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.beans.aoppatch.TransactionInterceptorFactory;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.transaction.changetracking.EntityChangeTracker;
import org.apache.isis.persistence.jdo.datanucleus.changetracking.JdoLifecycleListener;
import org.apache.isis.persistence.jdo.datanucleus.config.DnEntityDiscoveryListener;
import org.apache.isis.persistence.jdo.datanucleus.config.DnSettings;
import org.apache.isis.persistence.jdo.datanucleus.dialect.DnJdoDialect;
import org.apache.isis.persistence.jdo.datanucleus.entities.DnEntityStateProvider;
import org.apache.isis.persistence.jdo.datanucleus.jdosupport.JdoSupportServiceDefault;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.JdoDataNucleusProgrammingModel;
import org.apache.isis.persistence.jdo.datanucleus.mixins.Persistable_datanucleusIdLong;
import org.apache.isis.persistence.jdo.datanucleus.mixins.Persistable_datanucleusVersionLong;
import org.apache.isis.persistence.jdo.datanucleus.mixins.Persistable_datanucleusVersionTimestamp;
import org.apache.isis.persistence.jdo.datanucleus.mixins.Persistable_downloadJdoMetadata;
import org.apache.isis.persistence.jdo.integration.IsisModuleJdoIntegration;
import org.apache.isis.persistence.jdo.spring.integration.JdoDialect;
import org.apache.isis.persistence.jdo.spring.integration.JdoTransactionManager;
import org.apache.isis.persistence.jdo.spring.integration.LocalPersistenceManagerFactoryBean;
import org.apache.isis.persistence.jdo.spring.integration.TransactionAwarePersistenceManagerFactoryProxy;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0 {@index}
 */
@Configuration
@Import({
    // modules
    IsisModuleJdoIntegration.class,

    // @Component's
    DnEntityDiscoveryListener.class,
    DnEntityStateProvider.class,
    JdoDataNucleusProgrammingModel.class,

    // @Mixin's
    Persistable_datanucleusIdLong.class,
    Persistable_datanucleusVersionLong.class,
    Persistable_datanucleusVersionTimestamp.class,
    Persistable_downloadJdoMetadata.class,

    // @Service's
    JdoSupportServiceDefault.class,

})
@EnableConfigurationProperties(DnSettings.class)
@Log4j2
public class IsisModuleJdoDatanucleus {
    
    /**
     * Conveniently registers this dialect as a {@link PersistenceExceptionTranslator} with <i>Spring</i>.
     * @see PersistenceExceptionTranslator
     * @see JdoDialect
     */
    @Qualifier("jdo-dialect")
    @Bean
    public DnJdoDialect getDnJdoDialect(final DataSource dataSource) {
        return new DnJdoDialect(dataSource);
    }
    
    @Qualifier("local-pmf-proxy")
    @Bean 
    public LocalPersistenceManagerFactoryBean getLocalPersistenceManagerFactoryBean(
            final IsisConfiguration isisConfiguration,
            final DataSource dataSource,
            final MetaModelContext metaModelContext,
            final EventBusService eventBusService,
            final Provider<EntityChangeTracker> entityChangeTrackerProvider,
            final DnSettings dnSettings) {
        
        _Assert.assertNotNull(dataSource, "a datasource is required");
        
        autoCreateSchemas(dataSource, isisConfiguration);

        val lpmfBean = new LocalPersistenceManagerFactoryBean() {
            @Override
            protected PersistenceManagerFactory newPersistenceManagerFactory(java.util.Map<?,?> props) {
                val pmf = new JDOPersistenceManagerFactory(props);
                pmf.setConnectionFactory(dataSource);
                integrateWithApplicationLayer(metaModelContext, eventBusService, entityChangeTrackerProvider, pmf);
                return pmf;
            }
            @Override
            protected PersistenceManagerFactory newPersistenceManagerFactory(String name) {
                val pmf = super.newPersistenceManagerFactory(name);
                pmf.setConnectionFactory(dataSource); //might be too late, anyway, not sure if this is ever called
                integrateWithApplicationLayer(metaModelContext, eventBusService, entityChangeTrackerProvider, pmf);
                return pmf;
            }
        };
        lpmfBean.setJdoPropertyMap(dnSettings.getAsProperties());
        return lpmfBean;
    }
    
    @Qualifier("transaction-aware-pmf-proxy")
    @Bean @Primary 
    public TransactionAwarePersistenceManagerFactoryProxy getTransactionAwarePersistenceManagerFactoryProxy(
            final @Qualifier("local-pmf-proxy") LocalPersistenceManagerFactoryBean localPmfBean) {

        val pmf = localPmfBean.getObject(); // created once per application lifecycle

        val tapmfProxy = new TransactionAwarePersistenceManagerFactoryProxy();
        tapmfProxy.setTargetPersistenceManagerFactory(pmf);
        tapmfProxy.setAllowCreate(false);
        return tapmfProxy;
    }

    @Qualifier("jdo-platform-transaction-manager")
    @Bean @Primary
    public JdoTransactionManager getTransactionManager(
            final @Qualifier("jdo-dialect") JdoDialect jdoDialect,
            final @Qualifier("local-pmf-proxy") LocalPersistenceManagerFactoryBean localPmfBean) {

        val pmf = localPmfBean.getObject(); // created once per application lifecycle
        val txManager = new JdoTransactionManager(pmf);
        txManager.setJdoDialect(jdoDialect);
        return txManager;
    }

    /**
     * AOP PATCH
     * @implNote works only with patch package 'org.apache.isis.core.config.beans.aoppatch'
     */
    @Bean @Primary
    @SuppressWarnings("serial")
    public TransactionInterceptorFactory getTransactionInterceptorFactory() {
        return ()->new TransactionInterceptor() {
            @Override @SneakyThrows
            protected void completeTransactionAfterThrowing(TransactionInfo txInfo, Throwable cause) {
                super.completeTransactionAfterThrowing(txInfo, cause);

                val txManager = txInfo.getTransactionManager();

                Result.failure(cause)

                //XXX seems like a bug in DN, why do we need to unwrap this?
                .mapFailure(ex->ex instanceof IllegalArgumentException
                        ? ((IllegalArgumentException)ex).getCause()
                        : ex)

                // converts to JDOException
                .mapFailure(ex->ex instanceof NucleusException
                        ? NucleusJDOHelper
                                .getJDOExceptionForNucleusException(((NucleusException)ex))
                        : ex)

                // converts to Spring's DataAccessException
                .mapFailure(ex->ex instanceof JDOException
                        ? (txManager instanceof JdoTransactionManager)
                                ? ((JdoTransactionManager)txManager).getJdoDialect().translateException((JDOException)ex)
                                : ex
                        : ex)

                .optionalElseFail();

            }
        };
    }
    
    // -- HELPER

    /**
     * integrates with settings from isis.persistence.schema.*
     */
    @SneakyThrows
    private static DataSource autoCreateSchemas(
            final DataSource dataSource,
            final IsisConfiguration isisConfiguration) {

        val persistenceSchemaConf = isisConfiguration.getPersistence().getSchema();

        if(!persistenceSchemaConf.getAutoCreateSchemas().isEmpty()) {

            log.info("about to create db schema(s) {}", persistenceSchemaConf.getAutoCreateSchemas());

            try(val con = dataSource.getConnection()){

                val s = con.createStatement();

                for(val schema : persistenceSchemaConf.getAutoCreateSchemas()) {
                    s.execute(String.format(persistenceSchemaConf.getCreateSchemaSqlTemplate(), schema));
                }

            }
        }

        return dataSource;
    }
    
    private static void integrateWithApplicationLayer(
            final MetaModelContext metaModelContext,
            final EventBusService eventBusService,
            final Provider<EntityChangeTracker> entityChangeTrackerProvider,
            final PersistenceManagerFactory pmf) {

        // install JDO specific entity change listeners ...

        val jdoLifecycleListener = 
                new JdoLifecycleListener(metaModelContext, eventBusService, entityChangeTrackerProvider);
        pmf.addInstanceLifecycleListener(jdoLifecycleListener, (Class[]) null);

    }

}
