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

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.PublishedAction;
import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.applib.annotation.PublishedObject.EventCanonicalizer;
import org.apache.isis.applib.services.audit.AuditingService;
import org.apache.isis.applib.services.publish.CanonicalEvent;
import org.apache.isis.applib.services.publish.PublishingService;
import org.apache.isis.core.commons.config.InstallerAbstract;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.services.ServicesInjectorDefault;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.runtime.installerregistry.InstallerLookup;
import org.apache.isis.core.runtime.installerregistry.InstallerLookupAware;
import org.apache.isis.core.runtime.persistence.PersistenceConstants;
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
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PublishingServiceWithCanonicalizers;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.TransactionalResource;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.AdapterManagerSpi;
import org.apache.isis.core.runtime.system.persistence.IdentifierGenerator;
import org.apache.isis.core.runtime.system.persistence.IdentifierGeneratorDefault;
import org.apache.isis.core.runtime.system.persistence.ObjectFactory;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.system.transaction.EnlistedObjectDirtying;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.systemdependencyinjector.SystemDependencyInjector;
import org.apache.log4j.Logger;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

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
        ObjectFactory objectFactory = persistenceSessionFactory.getObjectFactory();
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
                new PersistenceSession(persistenceSessionFactory, adapterFactory, objectFactory, servicesInjector, identifierGenerator, adapterManager, persistAlgorithm, objectStore);
        
        final IsisTransactionManager transactionManager = createTransactionManager(servicesInjector, persistenceSession, objectStore);
        
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
    protected IsisTransactionManager createTransactionManager(ServicesInjectorSpi servicesInjectorSpi, final EnlistedObjectDirtying enlistedObjectDirtying, final TransactionalResource transactionalResource) {
        return new IsisTransactionManager(enlistedObjectDirtying, transactionalResource, servicesInjectorSpi);
    }


    @Override
    public ClassSubstitutor createClassSubstitutor(IsisConfiguration configuration) {
        return InstanceUtil.createInstance(PersistenceConstants.CLASS_SUBSTITUTOR_CLASS_NAME_DEFAULT, ClassSubstitutor.class);
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
     * {@link ObjectFactory}.
     * 
     * <p>
     * By default, returns <tt>org.apache.isis.runtimes.dflt.bytecode.dflt.objectfactory.CglibObjectFactory</tt>.  Note that this requires that
     * the <tt>org.apache.isis.runtimes.dflt.bytecode:dflt</tt> module is added to the classpath. 
     */
    public ObjectFactory createObjectFactory(final IsisConfiguration configuration) {
        return InstanceUtil.createInstance(PersistenceConstants.OBJECT_FACTORY_CLASS_NAME_DEFAULT, ObjectFactory.class);
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
    public DomainObjectContainer createContainer(final IsisConfiguration configuration) {
        final String configuredClassName = configuration.getString(PersistenceConstants.DOMAIN_OBJECT_CONTAINER_CLASS_NAME, PersistenceConstants.DOMAIN_OBJECT_CONTAINER_NAME_DEFAULT);
        return InstanceUtil.createInstance(configuredClassName, PersistenceConstants.DOMAIN_OBJECT_CONTAINER_NAME_DEFAULT, DomainObjectContainer.class);
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
