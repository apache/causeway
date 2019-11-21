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

package org.apache.isis.jdo.persistence;

import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jdo.listener.StoreLifecycleListener;

import org.datanucleus.PropertyNames;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.springframework.stereotype.Service;

import org.apache.isis.commons.internal.base._Blackhole;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.beans.IsisBeanTypeRegistryHolder;
import org.apache.isis.jdo.datanucleus.DataNucleusSettings;
import org.apache.isis.jdo.datanucleus.JDOStateManagerForIsis;
import org.apache.isis.jdo.entities.JdoEntityTypeRegistry;
import org.apache.isis.jdo.lifecycles.JdoStoreLifecycleListenerForIsis;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.runtime.persistence.FixturesInstalledState;
import org.apache.isis.runtime.persistence.FixturesInstalledStateHolder;
import org.apache.isis.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.security.authentication.AuthenticationSession;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 *
 * Factory for {@link PersistenceSession}.
 *
 */
@Service @Singleton @Log4j2
public class PersistenceSessionFactory5
implements PersistenceSessionFactory, FixturesInstalledStateHolder {
    
    @Inject private IsisBeanTypeRegistryHolder isisBeanTypeRegistryHolder;

    private final _Lazy<DataNucleusApplicationComponents5> applicationComponents = 
            _Lazy.threadSafe(this::createDataNucleusApplicationComponents);
    
    private StoreLifecycleListener storeLifecycleListener;
    private MetaModelContext metaModelContext;
    private IsisConfiguration configuration;
    private final JdoEntityTypeRegistry jdoEntityTypeRegistry = new JdoEntityTypeRegistry();

    @Getter(onMethod=@__({@Override})) 
    @Setter(onMethod=@__({@Override})) 
    FixturesInstalledState fixturesInstalledState;

    @Override
    public void init(MetaModelContext metaModelContext) {
        this.metaModelContext = metaModelContext;
        this.configuration = metaModelContext.getConfiguration();
        // need to eagerly build, ... must be completed before catalogNamedQueries().
        // Why? because that method causes entity classes to be loaded which register with DN's EnhancementHelper,
        // which are then cached in DN.  It results in our CreateSchema listener not firing.
        _Blackhole.consume(applicationComponents.get());
        
        this.storeLifecycleListener = new JdoStoreLifecycleListenerForIsis();
        metaModelContext.getServiceInjector().injectServicesInto(storeLifecycleListener);
    }


    @Override
    public boolean isInitialized() {
        return this.configuration != null;
    }

    private DataNucleusApplicationComponents5 createDataNucleusApplicationComponents() {

        val dnSettings = metaModelContext.getServiceRegistry().lookupServiceElseFail(DataNucleusSettings.class);
        val datanucleusProps = dnSettings.getAsMap(); 
        
        addDataNucleusPropertiesIfRequired(datanucleusProps);
        
        val typeRegistry = isisBeanTypeRegistryHolder.getIsisBeanTypeRegistry();
        val classesToBePersisted = jdoEntityTypeRegistry.getEntityTypes(typeRegistry);

        return new DataNucleusApplicationComponents5(
                configuration,
                datanucleusProps, 
                classesToBePersisted);
    }

    @Override
    public void catalogNamedQueries() {
        val typeRegistry = isisBeanTypeRegistryHolder.getIsisBeanTypeRegistry();
        val classesToBePersisted = jdoEntityTypeRegistry.getEntityTypes(typeRegistry);
        DataNucleusApplicationComponents5.catalogNamedQueries(classesToBePersisted, 
                metaModelContext.getSpecificationLoader());
    }

    private static void addDataNucleusPropertiesIfRequired(Map<String, String> props) {

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


    @PreDestroy
    public final void shutdown() {
        if(!isInitialized()) {
            return;
        }
        if(applicationComponents.isMemoized()) {
            applicationComponents.get().shutdown();
            applicationComponents.clear();
        }
        this.configuration = null;
        this.storeLifecycleListener = null;
    }

    /**
     * Called by {@link org.apache.isis.runtime.system.session.IsisSessionFactory#openSession(AuthenticationSession)}.
     */

    @Override
    public PersistenceSession5 createPersistenceSession(
            final AuthenticationSession authenticationSession) {

        Objects.requireNonNull(applicationComponents.get(),
                () -> "PersistenceSession5 requires initialization. "+this.hashCode());

        val persistenceManagerFactory =
                applicationComponents.get().getPersistenceManagerFactory();

        return new PersistenceSession5(
                metaModelContext, 
                authenticationSession, 
                persistenceManagerFactory,
                storeLifecycleListener,
                this);
    }


}
