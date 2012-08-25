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

package org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.core.commons.config.InstallerAbstract;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.InstallerLookup;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.InstallerLookupAware;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceConstants;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceSessionFactoryDelegating;
import org.apache.isis.runtimes.dflt.runtime.persistence.adapter.PojoAdapterFactory;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerDefault;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.PojoRecreator;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.PojoRecreatorDefault;
import org.apache.isis.runtimes.dflt.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.IsisObjectStoreLogger;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStoreSpi;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm.PersistAlgorithm;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm.PersistAlgorithmDefault;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.TransactionalResource;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManagerSpi;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.IdentifierGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.ObjectFactory;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.EnlistedObjectDirtying;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.runtimes.dflt.runtime.systemdependencyinjector.SystemDependencyInjector;

/**
 * An abstract implementation of {@link PersistenceMechanismInstaller} that will
 * lookup the {@link ObjectAdapterFactory} and {@link ObjectFactory} from the
 * supplied {@link IsisConfiguration}.
 * 
 * <p>
 * If none can be found, then will default to the {@link PojoAdapterFactory} and
 * {@link PersistenceConstants#OBJECT_FACTORY_CLASS_NAME_DEFAULT default}link
 * ObjectFactory} (cglib at time of writing). respectively.
 */
public abstract class PersistenceMechanismInstallerAbstract extends InstallerAbstract implements PersistenceMechanismInstaller, InstallerLookupAware {

    private static final String LOGGING_PROPERTY = org.apache.isis.core.runtime.logging.Logger.PROPERTY_ROOT + "persistenceSession";
    private static final Logger LOG = Logger.getLogger(PersistenceMechanismInstallerAbstract.class);

    private SystemDependencyInjector installerLookup;

    public PersistenceMechanismInstallerAbstract(final String name) {
        super(PersistenceMechanismInstaller.TYPE, name);
    }

    /**
     * For subclasses that need to specify a different type.
     */
    public PersistenceMechanismInstallerAbstract(final String type, final String name) {
        super(type, name);
    }


    @Override
    public PersistenceSessionFactory createPersistenceSessionFactory(final DeploymentType deploymentType) {
        return new PersistenceSessionFactoryDelegating(deploymentType, this);
    }


    /**
     * Will return a {@link PersistenceSessionObjectStore}; subclasses are free
     * to downcast if required.
     */
    protected PersistenceSession createPersistenceSession(
            final PersistenceSessionFactory persistenceSessionFactory, 
            final ObjectAdapterFactory objectAdapterFactory, 
            final ObjectFactory objectFactory, 
            final PojoRecreator pojoRecreator, 
            final IdentifierGenerator identifierGenerator,
            final ServicesInjectorSpi servicesInjector) {

        final PersistAlgorithm persistAlgorithm = createPersistAlgorithm(getConfiguration());
        final AdapterManagerDefault adapterManager = new AdapterManagerDefault(pojoRecreator);
        
        ObjectStoreSpi objectStore = createObjectStore(getConfiguration(), objectAdapterFactory, adapterManager);

        ensureThatArg(persistAlgorithm, is(not(nullValue())));
        ensureThatArg(objectStore, is(not(nullValue())));

        if (getConfiguration().getBoolean(LOGGING_PROPERTY, false)) {
            final String level = getConfiguration().getString(LOGGING_PROPERTY + ".level", "debug");
            objectStore = new IsisObjectStoreLogger(objectStore, level);
        }

        final PersistenceSession persistenceSession = 
                new PersistenceSession(persistenceSessionFactory, objectAdapterFactory, objectFactory, servicesInjector, identifierGenerator, adapterManager, persistAlgorithm, objectStore);

        final IsisTransactionManager transactionManager = createTransactionManager(persistenceSession, objectStore);

        ensureThatArg(persistenceSession, is(not(nullValue())));
        ensureThatArg(transactionManager, is(not(nullValue())));

        persistenceSession.setDirtiableSupport(true);
        persistenceSession.setTransactionManager(transactionManager);

        // ... and finally return
        return persistenceSession;
    }

    
    // ///////////////////////////////////////////
    // Optional hook methods
    // ///////////////////////////////////////////

    /**
     * Hook method to create {@link PersistAlgorithm}.
     * 
     * <p>
     * By default returns a {@link PersistAlgorithmDefault}.
     */
    protected PersistAlgorithm createPersistAlgorithm(final IsisConfiguration configuration) {
        return new PersistAlgorithmDefault();
    }

    /**
     * Hook method to return an {@link IsisTransactionManager}.
     * 
     * <p>
     * By default returns a {@link IsisTransactionManager}.
     */
    protected IsisTransactionManager createTransactionManager(final EnlistedObjectDirtying persistor, final TransactionalResource objectStore) {
        return new IsisTransactionManager(persistor, objectStore);
    }

    // ///////////////////////////////////////////
    // Mandatory hook methods
    // ///////////////////////////////////////////

    /**
     * Hook method to return {@link ObjectStoreSpi}.
     */
    protected abstract ObjectStoreSpi createObjectStore(IsisConfiguration configuration, ObjectAdapterFactory adapterFactory, AdapterManagerSpi adapterManager);

    
    /**
     * Creates a {@link PersistenceSession} that is initialized with the various
     * hook methods.
     * 
     * @see #createPersistenceSession(PersistenceSessionFactory,
     *      ObjectAdapterFactory, ObjectFactory, AdapterManagerSpi,
     *      IdentifierGenerator, ServicesInjectorSpi)
     * @see #createAdapterFactory(IsisConfiguration)
     * @see #createAdapterManager(IsisConfiguration)
     * @see #createContainer(IsisConfiguration)
     * @see #createIdentifierGenerator(IsisConfiguration)
     * @see #createRuntimeContext(IsisConfiguration)
     * @see #createServicesInjector(IsisConfiguration)
     */
    @Override
    public PersistenceSession createPersistenceSession(final PersistenceSessionFactory persistenceSessionFactory) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("installing " + this.getClass().getName());
        }

        final PojoRecreator pojoRecreator = createPojoRecreator(getConfiguration());
        final ObjectAdapterFactory adapterFactory = createAdapterFactory(getConfiguration());
        final ObjectFactory objectFactory = createObjectFactory(getConfiguration());
        final IdentifierGenerator identifierGenerator = createIdentifierGenerator(getConfiguration());

        final RuntimeContext runtimeContext = createRuntimeContext(getConfiguration());
        final DomainObjectContainer container = createContainer(getConfiguration());

        final ServicesInjectorSpi servicesInjector = createServicesInjector(getConfiguration());
        final List<Object> serviceList = persistenceSessionFactory.getServices();

        ensureThatArg(pojoRecreator, is(not(nullValue())));
        ensureThatArg(adapterFactory, is(not(nullValue())));
        ensureThatArg(objectFactory, is(not(nullValue())));
        ensureThatArg(identifierGenerator, is(not(nullValue())));

        ensureThatArg(runtimeContext, is(not(nullValue())));
        ensureThatArg(container, is(not(nullValue())));
        ensureThatArg(serviceList, is(not(nullValue())));
        ensureThatArg(servicesInjector, is(not(nullValue())));

        // wire up components
        runtimeContext.injectInto(container);
        runtimeContext.setContainer(container);
        for (Object service : serviceList) {
            runtimeContext.injectInto(service);
        }

        servicesInjector.setContainer(container);
        servicesInjector.setServices(serviceList);
        getSpecificationLoader().injectInto(runtimeContext);

        return createPersistenceSession(persistenceSessionFactory, adapterFactory, objectFactory, pojoRecreator, identifierGenerator, servicesInjector);
    }

    

    // ///////////////////////////////////////////
    // Optional hook methods
    // ///////////////////////////////////////////


    /**
     * Hook method to allow subclasses to specify a different implementation of
     * {@link ObjectAdapterFactory}.
     * 
     * <p>
     * By default, looks up implementation from provided
     * {@link IsisConfiguration} using
     * {@link PersistenceConstants#ADAPTER_FACTORY_CLASS_NAME}. If no
     * implementation is specified, then defaults to
     * {@value PersistenceConstants#ADAPTER_FACTORY_CLASS_NAME_DEFAULT}.
     */
    protected ObjectAdapterFactory createAdapterFactory(final IsisConfiguration configuration) {
        final String configuredClassName = configuration.getString(PersistenceConstants.ADAPTER_FACTORY_CLASS_NAME, PersistenceConstants.ADAPTER_FACTORY_CLASS_NAME_DEFAULT);
        return InstanceUtil.createInstance(configuredClassName, ObjectAdapterFactory.class);
    }

    /**
     * Hook method to allow subclasses to specify a different implementation of
     * {@link ObjectFactory}.
     * 
     * <p>
     * By default, looks up implementation from provided
     * {@link IsisConfiguration} using
     * {@link PersistenceConstants#OBJECT_FACTORY_CLASS_NAME}. If no
     * implementation is specified, then defaults to
     * {@value PersistenceConstants#OBJECT_FACTORY_CLASS_NAME_DEFAULT}.
     */
    protected ObjectFactory createObjectFactory(final IsisConfiguration configuration) {
        final String configuredClassName = configuration.getString(PersistenceConstants.OBJECT_FACTORY_CLASS_NAME, PersistenceConstants.OBJECT_FACTORY_CLASS_NAME_DEFAULT);
        return InstanceUtil.createInstance(configuredClassName, PersistenceConstants.OBJECT_FACTORY_CLASS_NAME_DEFAULT, ObjectFactory.class);
    }

    /**
     * Hook method to allow subclasses to specify a different implementation of
     * {@link ServicesInjectorSpi}
     * 
     * <p>
     * By default, looks up implementation from provided
     * {@link IsisConfiguration} using
     * {@link PersistenceConstants#SERVICES_INJECTOR_CLASS_NAME}. If no
     * implementation is specified, then defaults to
     * {@value PersistenceConstants#SERVICES_INJECTOR_CLASS_NAME_DEFAULT}.
     */
    protected ServicesInjectorSpi createServicesInjector(final IsisConfiguration configuration) {
        final String configuredClassName = configuration.getString(PersistenceConstants.SERVICES_INJECTOR_CLASS_NAME, PersistenceConstants.SERVICES_INJECTOR_CLASS_NAME_DEFAULT);
        return InstanceUtil.createInstance(configuredClassName, ServicesInjectorSpi.class);
    }

    /**
     * Hook method to allow subclasses to specify a different implementation of
     * {@link OidGenerator}
     * 
     * <p>
     * By default, looks up implementation from provided
     * {@link IsisConfiguration} using
     * {@link PersistenceConstants#IDENTIFIER_GENERATOR_CLASS_NAME}. If no
     * implementation is specified, then defaults to
     * {@value PersistenceConstants#IDENTIFIER_GENERATOR_CLASS_NAME_DEFAULT}.
     */
    protected IdentifierGenerator createIdentifierGenerator(final IsisConfiguration configuration) {
        final String identifierGeneratorClassName = configuration.getString(PersistenceConstants.IDENTIFIER_GENERATOR_CLASS_NAME, PersistenceConstants.IDENTIFIER_GENERATOR_CLASS_NAME_DEFAULT);
        return InstanceUtil.createInstance(identifierGeneratorClassName, IdentifierGenerator.class);
    }


    /**
     * Hook method to return {@link PojoRecreator}.
     * 
     * <p>
     * By default returns an {@link PojoRecreatorDefault}.
     */
    protected PojoRecreator createPojoRecreator(final IsisConfiguration configuration) {
        return new PojoRecreatorDefault();
    }

    /**
     * Hook method to return a {@link RuntimeContext}.
     * 
     * <p>
     * By default, returns a {@link RuntimeContextFromSession}.
     */
    protected RuntimeContext createRuntimeContext(final IsisConfiguration configuration) {
        final Properties properties = new Properties();
        final IsisConfiguration applicationConfiguration = configuration.getProperties("application");
        for (final String key : applicationConfiguration) {
            final String value = applicationConfiguration.getString(key);
            final String newKey = key.substring("application.".length());
            properties.setProperty(newKey, value);
        }
        final RuntimeContextFromSession runtimeContext = new RuntimeContextFromSession();
        runtimeContext.setProperties(properties);
        return runtimeContext;
    }

    /**
     * Hook method to return a {@link DomainObjectContainer}.
     * 
     * <p>
     * By default, looks up implementation from provided
     * {@link IsisConfiguration} using
     * {@link PersistenceConstants#DOMAIN_OBJECT_CONTAINER_CLASS_NAME}. If no
     * implementation is specified, then defaults to
     * {@value PersistenceConstants#DOMAIN_OBJECT_CONTAINER_NAME_DEFAULT}.
     */
    protected DomainObjectContainer createContainer(final IsisConfiguration configuration) {
        final String configuredClassName = configuration.getString(PersistenceConstants.DOMAIN_OBJECT_CONTAINER_CLASS_NAME, PersistenceConstants.DOMAIN_OBJECT_CONTAINER_NAME_DEFAULT);
        return InstanceUtil.createInstance(configuredClassName, PersistenceConstants.DOMAIN_OBJECT_CONTAINER_NAME_DEFAULT, DomainObjectContainer.class);
    }

    // /////////////////////////////////////////////////////
    // Dependencies (from setters)
    // /////////////////////////////////////////////////////

    /**
     * By virtue of being {@link InstallerLookupAware}.
     */
    @Override
    public void setInstallerLookup(final InstallerLookup installerLookup) {
        this.installerLookup = installerLookup;
    }

    /**
     * @see #setInstallerLookup(InstallerLookup)
     */
    protected SystemDependencyInjector getInstallerLookup() {
        return installerLookup;
    }

    
    // /////////////////////////////////////////////////////
    // Dependencies (from context)
    // /////////////////////////////////////////////////////

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }
    
    
    // /////////////////////////////////////////////////////
    // Guice
    // /////////////////////////////////////////////////////

    @Override
    public List<Class<?>> getTypes() {
        return listOf(PersistenceSessionFactory.class);
    }


    
    
    
}
