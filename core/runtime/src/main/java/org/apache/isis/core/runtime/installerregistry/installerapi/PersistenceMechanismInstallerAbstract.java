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

package org.apache.isis.core.runtime.installerregistry.installerapi;

import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.commons.config.InstallerAbstract;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.services.ServicesInjectorDefault;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.runtime.installerregistry.InstallerLookup;
import org.apache.isis.core.runtime.installerregistry.InstallerLookupAware;
import org.apache.isis.core.runtime.persistence.PersistenceSessionFactoryDelegating;
import org.apache.isis.core.runtime.persistence.adapter.PojoAdapterFactory;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManagerDefault;
import org.apache.isis.core.runtime.persistence.adaptermanager.PojoRecreator;
import org.apache.isis.core.runtime.persistence.adaptermanager.PojoRecreatorDefault;
import org.apache.isis.core.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.core.runtime.persistence.objectstore.IsisObjectStoreLogger;
import org.apache.isis.core.runtime.persistence.objectstore.ObjectStoreSpi;
import org.apache.isis.core.runtime.persistence.objectstore.algorithm.PersistAlgorithm;
import org.apache.isis.core.runtime.persistence.objectstore.algorithm.PersistAlgorithmDefault;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.TransactionalResource;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.*;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.systemdependencyinjector.SystemDependencyInjector;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.hamcrest.CoreMatchers.*;

/**
 * An abstract implementation of {@link PersistenceMechanismInstaller} that will
 * lookup the {@link ObjectAdapterFactory} and {@link ObjectFactory} from the
 * supplied {@link IsisConfiguration}.
 */
public abstract class PersistenceMechanismInstallerAbstract extends InstallerAbstract implements PersistenceMechanismInstaller, InstallerLookupAware {


    private static final String LOGGING_PROPERTY = org.apache.isis.core.runtime.logging.Log4jLogger.PROPERTY_ROOT + "persistenceSession";
    private static final Logger LOG = LoggerFactory.getLogger(PersistenceMechanismInstallerAbstract.class);

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

    
    //////////////////////////////////////////////////////////////////////
    // createPersistenceSessionFactory
    //////////////////////////////////////////////////////////////////////

    @Override
    public PersistenceSessionFactory createPersistenceSessionFactory(final DeploymentType deploymentType) {
        return new PersistenceSessionFactoryDelegating(deploymentType, getConfiguration(), this);
    }


    //////////////////////////////////////////////////////////////////////
    // createPersistenceSession
    //////////////////////////////////////////////////////////////////////


    /**
     * Creates a {@link PersistenceSession} with internal (thread-safe) components obtained from the provided {@link PersistenceSessionFactory}.
     * 
     * <p>
     * Typically should not be overridden.
     */
    @Override
    public PersistenceSession createPersistenceSession(final PersistenceSessionFactory persistenceSessionFactory) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("installing " + this.getClass().getName());
        }

        ObjectAdapterFactory adapterFactory = persistenceSessionFactory.getAdapterFactory();
        PojoRecreator pojoRecreator = persistenceSessionFactory.getPojoRecreator();
        IdentifierGenerator identifierGenerator = persistenceSessionFactory.getIdentifierGenerator();
        ServicesInjectorSpi servicesInjector = persistenceSessionFactory.getServicesInjector();
        
        final PersistAlgorithm persistAlgorithm = createPersistAlgorithm(getConfiguration());
        final AdapterManagerDefault adapterManager = new AdapterManagerDefault(pojoRecreator);
        
        ObjectStoreSpi objectStore = createObjectStore(getConfiguration(), adapterFactory, adapterManager);
        
        ensureThatArg(persistAlgorithm, is(not(nullValue())));
        ensureThatArg(objectStore, is(not(nullValue())));
        
        if (getConfiguration().getBoolean(LOGGING_PROPERTY, false)) {
            final String level = getConfiguration().getString(LOGGING_PROPERTY + ".level", "debug");
            objectStore = new IsisObjectStoreLogger(objectStore, level);
        }
        
        final PersistenceSession persistenceSession = 
                new PersistenceSession(persistenceSessionFactory, adapterFactory, servicesInjector, identifierGenerator, adapterManager, persistAlgorithm, objectStore);
        
        final IsisTransactionManager transactionManager = createTransactionManager(persistenceSession, objectStore, servicesInjector);
        
        ensureThatArg(persistenceSession, is(not(nullValue())));
        ensureThatArg(transactionManager, is(not(nullValue())));
        
        persistenceSession.setDirtiableSupport(true);
        persistenceSession.setTransactionManager(transactionManager);
        
        return persistenceSession;
    }



    // ///////////////////////////////////////////
    // Mandatory hook methods
    // ///////////////////////////////////////////

    /**
     * Hook method to return {@link ObjectStoreSpi}.
     */
    protected abstract ObjectStoreSpi createObjectStore(IsisConfiguration configuration, ObjectAdapterFactory adapterFactory, AdapterManagerSpi adapterManager);
    

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
    protected IsisTransactionManager createTransactionManager(final PersistenceSession persistenceSession, final TransactionalResource transactionalResource, ServicesInjectorSpi servicesInjectorSpi) {
        return new IsisTransactionManager(persistenceSession, transactionalResource,  servicesInjectorSpi);
    }


    /**
     * Hook method to refine the {@link ProgrammingModel}.
     * 
     * <p>
     * By default, just returns the provided {@link ProgrammingModel}.
     */
    @Override
    public void refineProgrammingModel(ProgrammingModel baseProgrammingModel, IsisConfiguration configuration) {
        // no-op
    }
    
    /**
     * Hook method to refine the {@link MetaModelValidator}.
     * 
     * <p>
     * By default, just returns the provided {@link MetaModelValidatorComposite}.
     * 
     * <p>Note that this methods deals in terms of {@link MetaModelValidatorComposite} (rather than plain {@link MetaModelValidator}}s) 
     * in order to allow new {@link MetaModelValidator}s to be easily {@link MetaModelValidatorComposite#add(MetaModelValidator) added}.
     */
    @Override
    public void refineMetaModelValidator(MetaModelValidatorComposite baseMetaModelValidator, IsisConfiguration configuration) {
        // no-op
    }

    /**
     * Hook method to allow subclasses to specify a different implementation of
     * {@link ObjectAdapterFactory}.
     * 
     * <p>
     * By default, returns {@link PojoAdapterFactory};
     */
    public ObjectAdapterFactory createAdapterFactory(final IsisConfiguration configuration) {
        return new PojoAdapterFactory();
    }
    

    /**
     * Hook method to allow subclasses to specify a different implementation of
     * {@link ServicesInjectorSpi}
     * 
     * <p>
     * By default, returns {@link ServicesInjectorDefault};
     */
    public ServicesInjectorSpi createServicesInjector(final IsisConfiguration configuration) {
        return new ServicesInjectorDefault();
    }

    /**
     * Hook method to allow subclasses to specify a different implementation of
     * {@link IdentifierGenerator}
     * 
     * <p>
     * By default, returns {@link IdentifierGeneratorDefault}.
     */
    public IdentifierGenerator createIdentifierGenerator(final IsisConfiguration configuration) {
        return new IdentifierGeneratorDefault();
    }

    /**
     * Hook method to return {@link PojoRecreator}.
     * 
     * <p>
     * By default, returns {@link PojoRecreatorDefault}.
     */
    public PojoRecreator createPojoRecreator(final IsisConfiguration configuration) {
        return new PojoRecreatorDefault();
    }

    // ///////////////////////////////////////////
    // Non overridable.
    // ///////////////////////////////////////////

    /**
     * Returns a {@link RuntimeContext}, with all application-specific properties
     * from the provided {@link IsisConfiguration} copied over.
     */
    public final RuntimeContext createRuntimeContext(final IsisConfiguration configuration) {
        final RuntimeContextFromSession runtimeContext = new RuntimeContextFromSession();
        final Properties properties = applicationPropertiesFrom(configuration);
        runtimeContext.setProperties(properties);
        return runtimeContext;
    }

    private static Properties applicationPropertiesFrom(final IsisConfiguration configuration) {
        final Properties properties = new Properties();
        final IsisConfiguration applicationConfiguration = configuration.getProperties("application");
        for (final String key : applicationConfiguration) {
            final String value = applicationConfiguration.getString(key);
            final String newKey = key.substring("application.".length());
            properties.setProperty(newKey, value);
        }
        return properties;
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
