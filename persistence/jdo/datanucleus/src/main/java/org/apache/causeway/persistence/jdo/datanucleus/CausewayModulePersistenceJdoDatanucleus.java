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
package org.apache.causeway.persistence.jdo.datanucleus;

import java.util.Collections;
import java.util.List;

import javax.jdo.JDOException;
import javax.jdo.PersistenceManagerFactory;
import javax.sql.DataSource;

import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.metadata.PersistenceUnitMetaData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.config.beans.aoppatch.TransactionInterceptorFactory;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.services.objectlifecycle.ObjectLifecyclePublisher;
import org.apache.causeway.persistence.jdo.datanucleus.changetracking.JdoLifecycleListener;
import org.apache.causeway.persistence.jdo.datanucleus.config.DatanucleusSettings;
import org.apache.causeway.persistence.jdo.datanucleus.dialect.DnJdoDialect;
import org.apache.causeway.persistence.jdo.datanucleus.entities.DnEntityStateProvider;
import org.apache.causeway.persistence.jdo.datanucleus.jdosupport.JdoSupportServiceDefault;
import org.apache.causeway.persistence.jdo.datanucleus.valuetypes.DnByteIdValueSemantics;
import org.apache.causeway.persistence.jdo.datanucleus.valuetypes.DnCharIdValueSemantics;
import org.apache.causeway.persistence.jdo.datanucleus.valuetypes.DnDatastoreIdImplValueSemantics;
import org.apache.causeway.persistence.jdo.datanucleus.valuetypes.DnDatastoreUniqueLongIdValueSemantics;
import org.apache.causeway.persistence.jdo.datanucleus.valuetypes.DnIntIdValueSemantics;
import org.apache.causeway.persistence.jdo.datanucleus.valuetypes.DnLongIdValueSemantics;
import org.apache.causeway.persistence.jdo.datanucleus.valuetypes.DnObjectIdValueSemantics;
import org.apache.causeway.persistence.jdo.datanucleus.valuetypes.DnShortIdValueSemantics;
import org.apache.causeway.persistence.jdo.datanucleus.valuetypes.DnStringIdValueSemantics;
import org.apache.causeway.persistence.jdo.datanucleus.valuetypes.JdoByteIdentityValueSemantics;
import org.apache.causeway.persistence.jdo.datanucleus.valuetypes.JdoCharIdentityValueSemantics;
import org.apache.causeway.persistence.jdo.datanucleus.valuetypes.JdoDatastoreIdValueSemantics;
import org.apache.causeway.persistence.jdo.datanucleus.valuetypes.JdoIntIdentityValueSemantics;
import org.apache.causeway.persistence.jdo.datanucleus.valuetypes.JdoLongIdentityValueSemantics;
import org.apache.causeway.persistence.jdo.datanucleus.valuetypes.JdoObjectIdentityValueSemantics;
import org.apache.causeway.persistence.jdo.datanucleus.valuetypes.JdoShortIdentityValueSemantics;
import org.apache.causeway.persistence.jdo.datanucleus.valuetypes.JdoStringIdentityValueSemantics;
import org.apache.causeway.persistence.jdo.integration.CausewayModulePersistenceJdoIntegration;
import org.apache.causeway.persistence.jdo.provider.config.JdoEntityDiscoveryListener;
import org.apache.causeway.persistence.jdo.spring.integration.JdoDialect;
import org.apache.causeway.persistence.jdo.spring.integration.JdoTransactionManager;
import org.apache.causeway.persistence.jdo.spring.integration.LocalPersistenceManagerFactoryBean;
import org.apache.causeway.persistence.jdo.spring.integration.TransactionAwarePersistenceManagerFactoryProxy;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0 {@index}
 */
@Configuration
@Import({
    // modules
    CausewayModulePersistenceJdoIntegration.class,

    // @Component's
    DnEntityStateProvider.class,

    DnDatastoreIdImplValueSemantics.class, // datastore identity
    DnDatastoreUniqueLongIdValueSemantics.class,
    JdoDatastoreIdValueSemantics.class,
    JdoShortIdentityValueSemantics.class, // application-defined PK, javax.jdo.identity
    JdoLongIdentityValueSemantics.class,
    JdoIntIdentityValueSemantics.class,
    JdoByteIdentityValueSemantics.class,
    JdoCharIdentityValueSemantics.class,
    JdoStringIdentityValueSemantics.class,
    JdoObjectIdentityValueSemantics.class,
    DnShortIdValueSemantics.class,  // application-defined PK, org.datanucleus.identity
    DnLongIdValueSemantics.class,
    DnIntIdValueSemantics.class,
    DnByteIdValueSemantics.class,
    DnCharIdValueSemantics.class,
    DnStringIdValueSemantics.class,
    DnObjectIdValueSemantics.class,

    // @Service's
    JdoSupportServiceDefault.class,

})
@EnableConfigurationProperties(DatanucleusSettings.class)
@Log4j2
public class CausewayModulePersistenceJdoDatanucleus {

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
            final CausewayConfiguration causewayConfiguration,
            final DataSource dataSource,
            final MetaModelContext metaModelContext,
            final ObjectLifecyclePublisher objectLifecyclePublisher,
            final CausewayBeanTypeRegistry beanTypeRegistry,
            final DatanucleusSettings dnSettings) {

        _Assert.assertNotNull(dataSource, "a datasource is required");

        autoCreateSchemas(dataSource, causewayConfiguration);

        val lpmfBean = new LocalPersistenceManagerFactoryBean() {
            @Override
            protected PersistenceManagerFactory newPersistenceManagerFactory(final java.util.Map<?,?> props) {
                val pu = createDefaultPersistenceUnit(beanTypeRegistry);
                val pmf = new JDOPersistenceManagerFactory(pu, props);
                pmf.setConnectionFactory(dataSource);
                integrateWithApplicationLayer(metaModelContext, objectLifecyclePublisher, pmf);
                return pmf;
            }
            @Override
            protected PersistenceManagerFactory newPersistenceManagerFactory(final String name) {
                val pmf = super.newPersistenceManagerFactory(name);
                pmf.setConnectionFactory(dataSource); //might be too late, anyway, not sure if this is ever called
                integrateWithApplicationLayer(metaModelContext, objectLifecyclePublisher, pmf);
                return pmf;
            }
        };
        lpmfBean.setJdoPropertyMap(dnSettings.getAsProperties());
        return lpmfBean;
    }

    @Qualifier("transaction-aware-pmf-proxy")
    @Bean @Primary
    public TransactionAwarePersistenceManagerFactoryProxy getTransactionAwarePersistenceManagerFactoryProxy(
            final MetaModelContext metaModelContext,
            final @Qualifier("local-pmf-proxy") LocalPersistenceManagerFactoryBean localPmfBean,
            final CausewayBeanTypeRegistry beanTypeRegistry,
            final List<JdoEntityDiscoveryListener> jdoEntityDiscoveryListeners,
            final DatanucleusSettings dnSettings) {

        val pmf = localPmfBean.getObject(); // created once per application lifecycle

        notifyJdoEntityDiscoveryListeners(pmf, beanTypeRegistry, jdoEntityDiscoveryListeners, dnSettings);

        val tapmfProxy = new TransactionAwarePersistenceManagerFactoryProxy(metaModelContext);
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
     * @implNote works only with patch package 'org.apache.causeway.core.config.beans.aoppatch'
     */
    @Bean @Primary
    @SuppressWarnings("serial")
    public TransactionInterceptorFactory getTransactionInterceptorFactory() {
        return ()->new TransactionInterceptor() {
            @Override @SneakyThrows
            protected void completeTransactionAfterThrowing(final TransactionInfo txInfo, final Throwable ex) {
                super.completeTransactionAfterThrowing(txInfo, ex);

                if(ex instanceof RuntimeException) {
                    val txManager = txInfo.getTransactionManager();
                    if(txManager instanceof JdoTransactionManager) {
                        val jdoDialect = ((JdoTransactionManager)txManager).getJdoDialect();
                        if(jdoDialect instanceof PersistenceExceptionTranslator) {
                            val translatedEx = ((PersistenceExceptionTranslator)jdoDialect)
                                    .translateExceptionIfPossible((RuntimeException)ex);

                            if(translatedEx!=null) {
                                throw translatedEx;
                            }

                        }

                        if(ex instanceof JDOException) {
                            val translatedEx = jdoDialect.translateException((JDOException)ex);

                            if(translatedEx!=null) {
                                throw translatedEx;
                            }
                        }
                    }
                }
            }
        };
    }

    // -- HELPER

    private static void notifyJdoEntityDiscoveryListeners(
            final PersistenceManagerFactory pmf,
            final CausewayBeanTypeRegistry beanTypeRegistry,
            final List<JdoEntityDiscoveryListener> jdoEntityDiscoveryListeners,
            final DatanucleusSettings dnSettings) {

        if(_NullSafe.isEmpty(jdoEntityDiscoveryListeners)) {
            return;
        }
        // assuming, as we instantiate a DN PMF, all entities discovered are JDO entities
        val jdoEntityTypes = beanTypeRegistry.getEntityTypes();
        if(_NullSafe.isEmpty(jdoEntityTypes)) {
            return;
        }
        val jdoEntityTypesView = Collections.unmodifiableSet(jdoEntityTypes.keySet());
        val dnProps = Collections.unmodifiableMap(dnSettings.getAsProperties());
        jdoEntityDiscoveryListeners
            .forEach(listener->
                    listener.onEntitiesDiscovered(pmf, jdoEntityTypesView, dnProps));
    }

    /**
     * integrates with settings from causeway.persistence.schema.*
     */
    @SneakyThrows
    private static DataSource autoCreateSchemas(
            final DataSource dataSource,
            final CausewayConfiguration causewayConfiguration) {

        val persistenceSchemaConf = causewayConfiguration.getPersistence().getSchema();

        if(!persistenceSchemaConf.getAutoCreateSchemas().isEmpty()) {

            val createSchemaSqlTemplate = persistenceSchemaConf.getCreateSchemaSqlTemplate();

            log.info("About to create db schema(s) {} with template '{}'",
                    persistenceSchemaConf.getAutoCreateSchemas(),
                    createSchemaSqlTemplate);

            try(val con = dataSource.getConnection()){

                val s = con.createStatement();

                for(val schema : persistenceSchemaConf.getAutoCreateSchemas()) {
                    // in case there are multiple placeholders, we specify the string multiple times.
                    val sql = String.format(createSchemaSqlTemplate, schema, schema, schema, schema, schema, schema, schema);
                    log.info("SQL '{}'", sql);
                    s.execute(sql);
                }

            }
        }

        return dataSource;
    }

    private static PersistenceUnitMetaData createDefaultPersistenceUnit (
            final CausewayBeanTypeRegistry beanTypeRegistry) {
        val pumd = new PersistenceUnitMetaData(
                "dynamic-unit", "RESOURCE_LOCAL", null);
        pumd.setExcludeUnlistedClasses(false);
        beanTypeRegistry.getEntityTypes().keySet().stream()
        .map(Class::getName)
        .forEach(pumd::addClassName);
        return pumd;
    }

    private static void integrateWithApplicationLayer(
            final MetaModelContext metaModelContext,
            final ObjectLifecyclePublisher objectLifecyclePublisher,
            final PersistenceManagerFactory pmf) {

        // install JDO specific entity change listeners ...

        val jdoLifecycleListener =
                new JdoLifecycleListener(metaModelContext, objectLifecyclePublisher);
        pmf.addInstanceLifecycleListener(jdoLifecycleListener, (Class[]) null);

    }

}
