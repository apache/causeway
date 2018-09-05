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
import java.util.Objects;
import java.util.Set;

import javax.jdo.PersistenceManagerFactory;

import org.datanucleus.PropertyNames;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.services.ServicesInjector;
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
public class PersistenceSessionFactory4 implements
PersistenceSessionFactory, ApplicationScopedComponent, FixturesInstalledFlag {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceSessionFactory4.class);

    public static final String JDO_OBJECTSTORE_CONFIG_PREFIX = "isis.persistor.datanucleus";  // specific to the JDO objectstore
    public static final String DATANUCLEUS_CONFIG_PREFIX = "isis.persistor.datanucleus.impl"; // reserved for datanucleus' own config props


    private final _Lazy<DataNucleusApplicationComponents4> applicationComponents = 
            _Lazy.threadSafe(this::createDataNucleusApplicationComponents);

    private IsisConfiguration configuration;

    @Programmatic
    @Override
    public void init(final IsisConfigurationDefault configuration) {
        this.configuration = configuration;
    }

    @Programmatic
    @Override
    public boolean isInitialized() {
        return this.configuration != null;
    }

    private DataNucleusApplicationComponents4 createDataNucleusApplicationComponents() {

        final RegisterEntities registerEntities = new RegisterEntities();
        final Set<String> classesToBePersisted = registerEntities.getEntityTypes();
        
            final IsisConfiguration jdoObjectstoreConfig = configuration.createSubset(
                    JDO_OBJECTSTORE_CONFIG_PREFIX);

            final IsisConfiguration dataNucleusConfig = configuration.createSubset(DATANUCLEUS_CONFIG_PREFIX);
            final Map<String, String> datanucleusProps = dataNucleusConfig.asMap();
            addDataNucleusPropertiesIfRequired(datanucleusProps);

        return new DataNucleusApplicationComponents4(jdoObjectstoreConfig,
                            datanucleusProps, classesToBePersisted);
    }
    
    private static void addDataNucleusPropertiesIfRequired(
            final Map<String, String> props) {

        // new feature in DN 3.2.3; enables dependency injection into entities
        putIfNotPresent(props, PropertyNames.PROPERTY_OBJECT_PROVIDER_CLASS_NAME, JDOStateManagerForIsis.class.getName());

        putIfNotPresent(props, "javax.jdo.PersistenceManagerFactoryClass", JDOPersistenceManagerFactory.class.getName());

        // previously we defaulted this property to "true", but that could cause the target database to be modified
        putIfNotPresent(props, PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_SCHEMA, Boolean.FALSE.toString());

        putIfNotPresent(props, PropertyNames.PROPERTY_SCHEMA_VALIDATE_ALL, Boolean.TRUE.toString());
        putIfNotPresent(props, PropertyNames.PROPERTY_CACHE_L2_TYPE, "none");

        putIfNotPresent(props, PropertyNames.PROPERTY_PERSISTENCE_UNIT_LOAD_CLASSES, Boolean.TRUE.toString());

        String connectionFactoryName = props.get(PropertyNames.PROPERTY_CONNECTION_FACTORY_NAME);
        if(connectionFactoryName != null) {
            String connectionFactory2Name = props.get(PropertyNames.PROPERTY_CONNECTION_FACTORY2_NAME);
            String transactionType = props.get("javax.jdo.option.TransactionType");
            // extended logging
            if(transactionType == null) {
                LOG.info("found config properties to use non-JTA JNDI datasource ({})", connectionFactoryName);
                if(connectionFactory2Name != null) {
                    LOG.warn("found config properties to use non-JTA JNDI datasource ({}); second '-nontx' JNDI datasource also configured but will not be used ({})", connectionFactoryName, connectionFactory2Name);
                }
            } else {
                LOG.info("found config properties to use JTA JNDI datasource ({})", connectionFactoryName);
            }
            if(connectionFactory2Name == null) {
                // JDO/DN itself will (probably) throw an exception
                LOG.error("found config properties to use JTA JNDI datasource ({}) but config properties for second '-nontx' JNDI datasource were *not* found", connectionFactoryName);
            } else {
                LOG.info("... and config properties for second '-nontx' JNDI datasource also found; {}", connectionFactory2Name);
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

    @Programmatic
    @Override
    public final void shutdown() {
        if(!isInitialized()) {
            return;
        }
        if(applicationComponents.isMemoized()) {
            applicationComponents.get().shutdown();
            applicationComponents.clear();
        }
        this.configuration = null;
    }

    /**
     * Called by {@link org.apache.isis.core.runtime.system.session.IsisSessionFactory#openSession(AuthenticationSession)}.
     */
    @Programmatic
    @Override
    public PersistenceSession4 createPersistenceSession(
            final ServicesInjector servicesInjector,
            final AuthenticationSession authenticationSession) {

        Objects.requireNonNull(applicationComponents, "PersistenceSession5 requires initialization. "+this.hashCode());
        
        final FixturesInstalledFlag fixturesInstalledFlag = this;
        
        //[ahuber] if stale force recreate
        guardAgainstStaleState();
        
        final PersistenceManagerFactory persistenceManagerFactory =
                applicationComponents.get().getPersistenceManagerFactory();

        return new PersistenceSession4(
                servicesInjector,
                authenticationSession, persistenceManagerFactory,
                fixturesInstalledFlag);
    }

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

    // [ahuber] JRebel support, not tested at all
    private void guardAgainstStaleState() {
        if(applicationComponents.get().isStale()) {
            try {
                applicationComponents.get().shutdown();
            } catch (Exception e) {
                // ignore
            }
            applicationComponents.clear();
        }
    }


}

