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

package org.apache.isis.runtimes.dflt.runtime.persistence;

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
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.runtimes.dflt.runtime.installers.InstallerLookup;
import org.apache.isis.runtimes.dflt.runtime.installers.InstallerLookupAware;
import org.apache.isis.runtimes.dflt.runtime.persistence.adapterfactory.AdapterFactory;
import org.apache.isis.runtimes.dflt.runtime.persistence.adapterfactory.pojo.PojoAdapterFactory;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerDefault;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerExtended;
import org.apache.isis.runtimes.dflt.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectfactory.ObjectFactory;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.OidGenerator;

/**
 * An abstract implementation of {@link PersistenceMechanismInstaller} that will lookup the {@link AdapterFactory} and
 * {@link ObjectFactory} from the supplied {@link IsisConfiguration}.
 * 
 * <p>
 * If none can be found, then will default to the {@link PojoAdapterFactory} and
 * {@link PersistenceConstants#OBJECT_FACTORY_CLASS_NAME_DEFAULT default}link ObjectFactory} (cglib at time of writing).
 * respectively.
 */
public abstract class PersistenceMechanismInstallerAbstract extends InstallerAbstract implements
    PersistenceMechanismInstaller, InstallerLookupAware {

    private static final String LOGGING_PROPERTY = org.apache.isis.core.runtime.logging.Logger.PROPERTY_ROOT
        + "persistenceSession";
    private static final Logger LOG = Logger.getLogger(PersistenceMechanismInstallerAbstract.class);

    private InstallerLookup installerLookup;

    public PersistenceMechanismInstallerAbstract(String name) {
        super(PersistenceMechanismInstaller.TYPE, name);
    }

    /**
     * For subclasses that need to specify a different type.
     */
    public PersistenceMechanismInstallerAbstract(String type, String name) {
        super(type, name);
    }

    /**
     * Creates a {@link PersistenceSession} that is initialized with the various hook methods.
     * 
     * @see #createPersistenceSession(PersistenceSessionFactory, AdapterManagerExtended, AdapterFactory, ObjectFactory,
     *      OidGenerator, ServicesInjector)
     * @see #createAdapterFactory(IsisConfiguration)
     * @see #createAdapterManager(IsisConfiguration)
     * @see #createContainer(IsisConfiguration)
     * @see #createOidGenerator(IsisConfiguration)
     * @see #createRuntimeContext(IsisConfiguration)
     * @see #createServicesInjector(IsisConfiguration)
     */
    @Override
    public PersistenceSession createPersistenceSession(final PersistenceSessionFactory persistenceSessionFactory) {
        if (LOG.isInfoEnabled()) {
            LOG.info("installing " + this.getClass().getName());
        }

        final AdapterManagerExtended adapterManager = createAdapterManager(getConfiguration());
        final AdapterFactory adapterFactory = createAdapterFactory(getConfiguration());
        final ObjectFactory objectFactory = createObjectFactory(getConfiguration());
        final OidGenerator oidGenerator = createOidGenerator(getConfiguration());

        final RuntimeContext runtimeContext = createRuntimeContext(getConfiguration());
        final DomainObjectContainer container = createContainer(getConfiguration());

        final ServicesInjector servicesInjector = createServicesInjector(getConfiguration());
        final List<Object> serviceList = persistenceSessionFactory.getServices();

        ensureThatArg(adapterManager, is(not(nullValue())));
        ensureThatArg(adapterFactory, is(not(nullValue())));
        ensureThatArg(objectFactory, is(not(nullValue())));
        ensureThatArg(oidGenerator, is(not(nullValue())));

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

        PersistenceSession persistenceSession =
            createPersistenceSession(persistenceSessionFactory, adapterManager, adapterFactory, objectFactory,
                oidGenerator, servicesInjector);

        if (getConfiguration().getBoolean(LOGGING_PROPERTY, false)) {
            String level = getConfiguration().getString(LOGGING_PROPERTY + ".level", "info");
            persistenceSession = new PersistenceSessionLogger(persistenceSession, level);
        }

        return persistenceSession;
    }

    // ///////////////////////////////////////////
    // Mandatory hook methods
    // ///////////////////////////////////////////

    /**
     * Mandatory hook method called by {@link #createPersistenceSession(PersistenceSessionFactory)}, passing the
     * components created by the other (optional) hooks.
     * 
     * @see #createPersistenceSession(PersistenceSessionFactory)
     */
    protected abstract PersistenceSession createPersistenceSession(
        final PersistenceSessionFactory persistenceSessionFactory, final AdapterManagerExtended adapterManager,
        final AdapterFactory adapterFactory, final ObjectFactory objectFactory, final OidGenerator oidGenerator,
        final ServicesInjector servicesInjector);

    // ///////////////////////////////////////////
    // Optional hook methods
    // ///////////////////////////////////////////

    /**
     * Hook method to allow subclasses to specify a different implementation of {@link AdapterFactory}.
     * 
     * <p>
     * By default, looks up implementation from provided {@link IsisConfiguration} using
     * {@link PersistenceConstants#ADAPTER_FACTORY_CLASS_NAME}. If no implementation is specified, then defaults to
     * {@value PersistenceConstants#ADAPTER_FACTORY_CLASS_NAME_DEFAULT}.
     */
    protected AdapterFactory createAdapterFactory(final IsisConfiguration configuration) {
        final String configuredClassName =
            configuration.getString(PersistenceConstants.ADAPTER_FACTORY_CLASS_NAME,
                PersistenceConstants.ADAPTER_FACTORY_CLASS_NAME_DEFAULT);
        return InstanceUtil.createInstance(configuredClassName, AdapterFactory.class);
    }

    /**
     * Hook method to allow subclasses to specify a different implementation of {@link ObjectFactory}.
     * 
     * <p>
     * By default, looks up implementation from provided {@link IsisConfiguration} using
     * {@link PersistenceConstants#OBJECT_FACTORY_CLASS_NAME}. If no implementation is specified, then defaults to
     * {@value PersistenceConstants#OBJECT_FACTORY_CLASS_NAME_DEFAULT}.
     */
    protected ObjectFactory createObjectFactory(final IsisConfiguration configuration) {
        final String configuredClassName =
            configuration.getString(PersistenceConstants.OBJECT_FACTORY_CLASS_NAME,
                PersistenceConstants.OBJECT_FACTORY_CLASS_NAME_DEFAULT);
        return InstanceUtil.createInstance(configuredClassName,
            PersistenceConstants.OBJECT_FACTORY_CLASS_NAME_DEFAULT, ObjectFactory.class);
    }

    /**
     * Hook method to allow subclasses to specify a different implementation of {@link ServicesInjector}
     * 
     * <p>
     * By default, looks up implementation from provided {@link IsisConfiguration} using
     * {@link PersistenceConstants#SERVICES_INJECTOR_CLASS_NAME}. If no implementation is specified, then defaults to
     * {@value PersistenceConstants#SERVICES_INJECTOR_CLASS_NAME_DEFAULT}.
     */
    protected ServicesInjector createServicesInjector(IsisConfiguration configuration) {
        final String configuredClassName =
            configuration.getString(PersistenceConstants.SERVICES_INJECTOR_CLASS_NAME,
                PersistenceConstants.SERVICES_INJECTOR_CLASS_NAME_DEFAULT);
        return InstanceUtil.createInstance(configuredClassName, ServicesInjector.class);
    }

    /**
     * Hook method to allow subclasses to specify a different implementation of {@link OidGenerator}
     * 
     * <p>
     * By default, looks up implementation from provided {@link IsisConfiguration} using
     * {@link PersistenceConstants#OID_GENERATOR_CLASS_NAME}. If no implementation is specified, then defaults to
     * {@value PersistenceConstants#OID_GENERATOR_CLASS_NAME_DEFAULT}.
     */
    protected OidGenerator createOidGenerator(final IsisConfiguration configuration) {
        final String oidGeneratorClassName =
            configuration.getString(PersistenceConstants.OID_GENERATOR_CLASS_NAME,
                PersistenceConstants.OID_GENERATOR_CLASS_NAME_DEFAULT);
        return InstanceUtil.createInstance(oidGeneratorClassName, OidGenerator.class);
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
        Properties properties = new Properties();
        IsisConfiguration applicationConfiguration = configuration.getProperties("application");
        for (String key : applicationConfiguration) {
            String value = applicationConfiguration.getString(key);
            String newKey = key.substring("application.".length());
            properties.setProperty(newKey, value);
        }
        RuntimeContextFromSession runtimeContext = new RuntimeContextFromSession();
        runtimeContext.setProperties(properties);
        return runtimeContext;
    }

    /**
     * Hook method to return a {@link DomainObjectContainer}.
     * 
     * <p>
     * By default, looks up implementation from provided {@link IsisConfiguration} using
     * {@link PersistenceConstants#DOMAIN_OBJECT_CONTAINER_CLASS_NAME}. If no implementation is specified, then defaults
     * to {@value PersistenceConstants#DOMAIN_OBJECT_CONTAINER_NAME_DEFAULT}.
     */
    protected DomainObjectContainer createContainer(final IsisConfiguration configuration) {
        final String configuredClassName =
            configuration.getString(PersistenceConstants.DOMAIN_OBJECT_CONTAINER_CLASS_NAME,
                PersistenceConstants.DOMAIN_OBJECT_CONTAINER_NAME_DEFAULT);
        return InstanceUtil.createInstance(configuredClassName,
            PersistenceConstants.DOMAIN_OBJECT_CONTAINER_NAME_DEFAULT, DomainObjectContainer.class);
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
    protected InstallerLookup getInstallerLookup() {
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
