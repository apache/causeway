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
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.InstallerLookup;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.InstallerLookupAware;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceConstants;
import org.apache.isis.runtimes.dflt.runtime.persistence.adapterfactory.pojo.PojoAdapterFactory;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerDefault;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerExtended;
import org.apache.isis.runtimes.dflt.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.IdentifierGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.ObjectFactory;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;
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

    /**
     * Creates a {@link PersistenceSession} that is initialized with the various
     * hook methods.
     * 
     * @see #createPersistenceSession(PersistenceSessionFactory,
     *      AdapterManagerExtended, ObjectAdapterFactory, ObjectFactory,
     *      IdentifierGenerator, ServicesInjector)
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

        final AdapterManagerExtended adapterManager = createAdapterManager(getConfiguration());
        final ObjectAdapterFactory adapterFactory = createAdapterFactory(getConfiguration());
        final ObjectFactory objectFactory = createObjectFactory(getConfiguration());
        final IdentifierGenerator identifierGenerator = createIdentifierGenerator(getConfiguration());

        final RuntimeContext runtimeContext = createRuntimeContext(getConfiguration());
        final DomainObjectContainer container = createContainer(getConfiguration());

        final ServicesInjector servicesInjector = createServicesInjector(getConfiguration());
        final List<Object> serviceList = persistenceSessionFactory.getServices();

        ensureThatArg(adapterManager, is(not(nullValue())));
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

        servicesInjector.setContainer(container);
        servicesInjector.setServices(serviceList);
        persistenceSessionFactory.getSpecificationLoader().injectInto(runtimeContext);

        final PersistenceSession persistenceSession = createPersistenceSession(persistenceSessionFactory, adapterManager, adapterFactory, objectFactory, identifierGenerator, servicesInjector);
        return persistenceSession;
    }

    // ///////////////////////////////////////////
    // Mandatory hook methods
    // ///////////////////////////////////////////

    /**
     * Mandatory hook method called by
     * {@link #createPersistenceSession(PersistenceSessionFactory)}, passing the
     * components created by the other (optional) hooks.
     * 
     * @see #createPersistenceSession(PersistenceSessionFactory)
     */
    protected abstract PersistenceSession createPersistenceSession(final PersistenceSessionFactory persistenceSessionFactory, final AdapterManagerExtended adapterManager, final ObjectAdapterFactory adapterFactory, final ObjectFactory objectFactory, final IdentifierGenerator identifierGenerator,
            final ServicesInjector servicesInjector);

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
     * {@link ServicesInjector}
     * 
     * <p>
     * By default, looks up implementation from provided
     * {@link IsisConfiguration} using
     * {@link PersistenceConstants#SERVICES_INJECTOR_CLASS_NAME}. If no
     * implementation is specified, then defaults to
     * {@value PersistenceConstants#SERVICES_INJECTOR_CLASS_NAME_DEFAULT}.
     */
    protected ServicesInjector createServicesInjector(final IsisConfiguration configuration) {
        final String configuredClassName = configuration.getString(PersistenceConstants.SERVICES_INJECTOR_CLASS_NAME, PersistenceConstants.SERVICES_INJECTOR_CLASS_NAME_DEFAULT);
        return InstanceUtil.createInstance(configuredClassName, ServicesInjector.class);
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
     * Hook method to return {@link AdapterManagerExtended}.
     * 
     * <p>
     * By default returns an {@link AdapterManagerDefault}.
     */
    protected AdapterManagerExtended createAdapterManager(final IsisConfiguration configuration) {
        return new AdapterManagerDefault();
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
    // Guice
    // /////////////////////////////////////////////////////

    @Override
    public List<Class<?>> getTypes() {
        return listOf(PersistenceSessionFactory.class);
    }

}
