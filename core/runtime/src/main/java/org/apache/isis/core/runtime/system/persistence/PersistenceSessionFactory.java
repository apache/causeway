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

package org.apache.isis.core.runtime.system.persistence;

import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManagerFactory;

import org.datanucleus.PropertyNames;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.persistence.FixturesInstalledFlag;
import org.apache.isis.objectstore.jdo.datanucleus.JDOStateManagerForIsis;
import org.apache.isis.objectstore.jdo.service.RegisterEntities;

/**
 *
 * Factory for {@link PersistenceSession}.
 *
 * <p>
 * Implementing class is added to {@link ServicesInjector} as an (internal) domain service; all public methods
 * must be annotated using {@link Programmatic}.
 * </p>
 */
public class PersistenceSessionFactory implements ApplicationScopedComponent, FixturesInstalledFlag {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceSessionFactory.class);

    //region > constructor

    private final IsisConfigurationDefault configuration;

    public PersistenceSessionFactory(final IsisConfigurationDefault isisConfiguration) {
        this.configuration = isisConfiguration;
    }

    //endregion

    //region > init, createDataNucleusApplicationComponents

    public static final String JDO_OBJECTSTORE_CONFIG_PREFIX = "isis.persistor.datanucleus";  // specific to the JDO objectstore
    public static final String DATANUCLEUS_CONFIG_PREFIX = "isis.persistor.datanucleus.impl"; // reserved for datanucleus' own config props


    private DataNucleusApplicationComponents applicationComponents;

    @Programmatic
    public void init(final SpecificationLoader specificationLoader) {
        this.applicationComponents = createDataNucleusApplicationComponents(configuration, specificationLoader);
    }

    @Programmatic
    public boolean isInitialized() {
        return this.applicationComponents != null;
    }

    private DataNucleusApplicationComponents createDataNucleusApplicationComponents(
            final IsisConfiguration configuration, final SpecificationLoader specificationLoader) {

        if (applicationComponents == null || applicationComponents.isStale()) {

            final IsisConfiguration jdoObjectstoreConfig = configuration.createSubset(
                    JDO_OBJECTSTORE_CONFIG_PREFIX);

            final IsisConfiguration dataNucleusConfig = configuration.createSubset(DATANUCLEUS_CONFIG_PREFIX);
            final Map<String, String> datanucleusProps = dataNucleusConfig.asMap();
            addDataNucleusPropertiesIfRequired(datanucleusProps);

            final RegisterEntities registerEntities = new RegisterEntities(specificationLoader);
            final Set<String> classesToBePersisted = registerEntities.getEntityTypes();

            applicationComponents = new DataNucleusApplicationComponents(jdoObjectstoreConfig, specificationLoader,
                    datanucleusProps, classesToBePersisted);
        }

        return applicationComponents;
    }

    private static void addDataNucleusPropertiesIfRequired(
            final Map<String, String> props) {

        // new feature in DN 3.2.3; enables dependency injection into entities
        putIfNotPresent(props, PropertyNames.PROPERTY_OBJECT_PROVIDER_CLASS_NAME, JDOStateManagerForIsis.class.getName());

        putIfNotPresent(props, "javax.jdo.PersistenceManagerFactoryClass", JDOPersistenceManagerFactory.class.getName());

        // previously we defaulted this property to "true", but that could cause the target database to be modified
        putIfNotPresent(props, PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_DATABASE, Boolean.FALSE.toString());

        putIfNotPresent(props, PropertyNames.PROPERTY_SCHEMA_VALIDATE_ALL, Boolean.TRUE.toString());
        putIfNotPresent(props, PropertyNames.PROPERTY_CACHE_L2_TYPE, "none");

        putIfNotPresent(props, PropertyNames.PROPERTY_PERSISTENCE_UNIT_LOAD_CLASSES, Boolean.TRUE.toString());

        String connectionFactoryName = props.get(PropertyNames.PROPERTY_CONNECTION_FACTORY_NAME);
        if(connectionFactoryName != null) {
            String connectionFactory2Name = props.get(PropertyNames.PROPERTY_CONNECTION_FACTORY2_NAME);
            String transactionType = props.get("javax.jdo.option.TransactionType");
            // extended logging
            if(transactionType == null) {
                LOG.info("found config properties to use non-JTA JNDI datasource (" + connectionFactoryName + ")");
                if(connectionFactory2Name != null) {
                    LOG.warn("found config properties to use non-JTA JNDI datasource (" + connectionFactoryName + "); second '-nontx' JNDI datasource also configured but will not be used (" + connectionFactory2Name +")");
                }
            } else {
                LOG.info("found config properties to use JTA JNDI datasource (" + connectionFactoryName + ")");
            }
            if(connectionFactory2Name == null) {
                // JDO/DN itself will (probably) throw an exception
                LOG.error("found config properties to use JTA JNDI datasource (" + connectionFactoryName + ") but config properties for second '-nontx' JNDI datasource were *not* found");
            } else {
                LOG.info("... and config properties for second '-nontx' JNDI datasource also found; " + connectionFactory2Name);
            }
            // nothing further to do
            return;
        } else {
            // use JDBC connection properties; put if not present
            LOG.info("did *not* find config properties to use JNDI datasource; will use JDBC");

            putIfNotPresent(props, "javax.jdo.option.ConnectionDriverName", "org.hsqldb.jdbcDriver");
            putIfNotPresent(props, "javax.jdo.option.ConnectionURL", "jdbc:hsqldb:mem:test");
            putIfNotPresent(props, "javax.jdo.option.ConnectionUserName", "sa");
            putIfNotPresent(props, "javax.jdo.option.ConnectionPassword", "");
        }
    }

    private static void putIfNotPresent(
            final Map<String, String> props,
            String key,
            String value) {
        if(!props.containsKey(key)) {
            props.put(key, value);
        }
    }
    //endregion

    //region > shutdown
    @Programmatic
    public final void shutdown() {
        // no-op
    }

    //endregion


    //region > createPersistenceSession

    /**
     * Called by {@link org.apache.isis.core.runtime.system.session.IsisSessionFactory#openSession(AuthenticationSession)}.
     */
    @Programmatic
    public PersistenceSession createPersistenceSession(
            final ServicesInjector servicesInjector,
            final AuthenticationSession authenticationSession) {

        final FixturesInstalledFlag fixturesInstalledFlag = this;
        final PersistenceManagerFactory persistenceManagerFactory =
                applicationComponents.getPersistenceManagerFactory();

        return new PersistenceSession(
                servicesInjector,
                authenticationSession, persistenceManagerFactory,
                fixturesInstalledFlag);
    }



    //endregion

    //region > FixturesInstalledFlag impl

    private Boolean fixturesInstalled;

    @Programmatic
    @Override
    public Boolean isFixturesInstalled() {
        return fixturesInstalled;
    }

    @Programmatic
    @Override
    public void setFixturesInstalled(final Boolean fixturesInstalled) {
        this.fixturesInstalled = fixturesInstalled;
    }

    //endregion


}
