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

import javax.enterprise.inject.Vetoed;
import javax.jdo.PersistenceManagerFactory;

import org.datanucleus.PropertyNames;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.persistence.FixturesInstalledState;
import org.apache.isis.core.runtime.persistence.FixturesInstalledStateHolder;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.objectstore.jdo.datanucleus.JDOStateManagerForIsis;
import org.apache.isis.objectstore.jdo.service.RegisterEntities;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * Factory for {@link PersistenceSession}.
 *
 */
@Vetoed @Slf4j
public class PersistenceSessionFactory5
implements PersistenceSessionFactory, ApplicationScopedComponent, FixturesInstalledStateHolder {

    public static final String JDO_OBJECTSTORE_CONFIG_PREFIX = "isis.persistor.datanucleus";  // specific to the JDO objectstore
    public static final String DATANUCLEUS_CONFIG_PREFIX = "isis.persistor.datanucleus.impl"; // reserved for datanucleus' own config props
    
    private final _Lazy<DataNucleusApplicationComponents5> applicationComponents = 
            _Lazy.threadSafe(this::createDataNucleusApplicationComponents);

    private IsisConfiguration configuration;
    
    @Getter(onMethod=@__({@Override})) 
    @Setter(onMethod=@__({@Override})) 
    FixturesInstalledState fixturesInstalledState;
    
    
    @Override
    public void init() {
        this.configuration = _Config.getConfiguration();
        // need to eagerly build, ... must be completed before catalogNamedQueries().
        // Why? because that method causes entity classes to be loaded which register with DN's EnhancementHelper,
        // which are then cached in DN.  It results in our CreateSchema listener not firing.
        createDataNucleusApplicationComponents();
    }

    
    @Override
    public boolean isInitialized() {
        return this.configuration != null;
    }

    private DataNucleusApplicationComponents5 createDataNucleusApplicationComponents() {

        val jdoObjectstoreConfig = configuration.subsetWithNamesStripped(JDO_OBJECTSTORE_CONFIG_PREFIX);
        val dataNucleusConfig = configuration.subsetWithNamesStripped(DATANUCLEUS_CONFIG_PREFIX);
        val datanucleusProps = dataNucleusConfig.copyToMap();
        
        addDataNucleusPropertiesIfRequired(datanucleusProps);
        
        val classesToBePersisted = new RegisterEntities().getEntityTypes();

        return new DataNucleusApplicationComponents5(
                jdoObjectstoreConfig,
                datanucleusProps, 
                classesToBePersisted);
    }

    @Override
    
    public void catalogNamedQueries(final SpecificationLoader specificationLoader) {
        val classesToBePersisted = new RegisterEntities().getEntityTypes();
        DataNucleusApplicationComponents5.catalogNamedQueries(classesToBePersisted, specificationLoader);
    }

//    private boolean shouldCreate(final DataNucleusApplicationComponents5 applicationComponents) {
//        return applicationComponents == null || applicationComponents.isStale();
//    }

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
                log.info("found config properties to use non-JTA JNDI datasource ({})", connectionFactoryName);
                if(connectionFactory2Name != null) {
                    log.warn("found config properties to use non-JTA JNDI datasource ({}); second '-nontx' JNDI datasource also configured but will not be used ({})", connectionFactoryName, connectionFactory2Name);
                }
            } else {
                log.info("found config properties to use JTA JNDI datasource ({})", connectionFactoryName);
            }
            if(connectionFactory2Name == null) {
                // JDO/DN itself will (probably) throw an exception
                log.error("found config properties to use JTA JNDI datasource ({}) but config properties for second '-nontx' JNDI datasource were *not* found", connectionFactoryName);
            } else {
                log.info("... and config properties for second '-nontx' JNDI datasource also found; {}", connectionFactory2Name);
            }
            // nothing further to do
            return;
        } else {
            // use JDBC connection properties; put if not present

            putIfNotPresent(props, "javax.jdo.option.ConnectionDriverName", "org.hsqldb.jdbcDriver");
            putIfNotPresent(props, "javax.jdo.option.ConnectionURL", "jdbc:hsqldb:mem:test");
            putIfNotPresent(props, "javax.jdo.option.ConnectionUserName", "sa");
            putIfNotPresent(props, "javax.jdo.option.ConnectionPassword", "");
            
            if(log.isInfoEnabled()) {
                log.info("using JDBC connection '{}'", 
                        props.get("javax.jdo.option.ConnectionURL"));
            }
            
            
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
    
    @Override
    public PersistenceSession5 createPersistenceSession(
            final AuthenticationSession authenticationSession) {

        Objects.requireNonNull(applicationComponents.get(),
                () -> "PersistenceSession5 requires initialization. "+this.hashCode());
        
        //[ahuber] if stale force recreate
        guardAgainstStaleState();
        
        final PersistenceManagerFactory persistenceManagerFactory =
                applicationComponents.get().getPersistenceManagerFactory();

        return new PersistenceSession5(
                authenticationSession, 
                persistenceManagerFactory,
                this);
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
