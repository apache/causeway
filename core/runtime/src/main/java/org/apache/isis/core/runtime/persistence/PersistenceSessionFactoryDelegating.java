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

package org.apache.isis.core.runtime.persistence;

import java.util.List;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.*;

/**
 * Implementation that just delegates to a supplied
 * {@link PersistenceSessionFactory}.
 */
public class PersistenceSessionFactoryDelegating implements PersistenceSessionFactory, FixturesInstalledFlag {

    private final DeploymentType deploymentType;
    private final IsisConfiguration configuration;
    private final PersistenceSessionFactoryDelegate persistenceSessionFactoryDelegate;

    /**
     * @see #setServices(List)
     */
    private List<Object> serviceList;

    private Boolean fixturesInstalled;

    private ObjectAdapterFactory adapterFactory;
    private ServicesInjectorSpi servicesInjector;
    private RuntimeContext runtimeContext;

    public PersistenceSessionFactoryDelegating(
            final DeploymentType deploymentType, 
            final IsisConfiguration isisConfiguration, 
            final PersistenceSessionFactoryDelegate persistenceSessionFactoryDelegate) {
        this.deploymentType = deploymentType;
        this.configuration = isisConfiguration;
        this.persistenceSessionFactoryDelegate = persistenceSessionFactoryDelegate;
    }

    @Override
    public DeploymentType getDeploymentType() {
        return deploymentType;
    }

    public PersistenceSessionFactoryDelegate getDelegate() {
        return persistenceSessionFactoryDelegate;
    }

    @Override
    public PersistenceSession createPersistenceSession() {
        return persistenceSessionFactoryDelegate.createPersistenceSession(this);
    }

    @Override
    public final void init() {

        // check prereq dependencies injected
        ensureThatState(serviceList, is(notNullValue()));

        // a bit of a workaround, but required if anything in the metamodel (for
        // example, a
        // ValueSemanticsProvider for a date value type) needs to use the Clock
        // singleton
        // we do this after loading the services to allow a service to prime a
        // different clock
        // implementation (eg to use an NTP time service).
        if (!deploymentType.isProduction() && !Clock.isInitialized()) {
            FixtureClock.initialize();
        }

        adapterFactory = persistenceSessionFactoryDelegate.createAdapterFactory(getConfiguration());

        ensureThatState(adapterFactory, is(not(nullValue())));

        servicesInjector = persistenceSessionFactoryDelegate.createServicesInjector(getConfiguration());

        ensureThatState(servicesInjector, is(not(nullValue())));

        runtimeContext = persistenceSessionFactoryDelegate.createRuntimeContext(getConfiguration());
        ensureThatState(runtimeContext, is(not(nullValue())));

        // inject the specification loader etc.
        runtimeContext.injectInto(servicesInjector);
        
        // wire up components

        getSpecificationLoader().injectInto(runtimeContext);
        for (Object service : serviceList) {
            runtimeContext.injectInto(service);
        }

        servicesInjector.setServices(serviceList);
        servicesInjector.init();
    }


    @Override
    public final void shutdown() {
        doShutdown();
    }

    /**
     * Optional hook method for implementation-specific shutdown.
     */
    protected void doShutdown() {
    }

    
    // //////////////////////////////////////////////////////
    // Components (setup during init...)
    // //////////////////////////////////////////////////////

    public ObjectAdapterFactory getAdapterFactory() {
        return adapterFactory;
    }
    
    public ServicesInjectorSpi getServicesInjector() {
        return servicesInjector;
    }

    // //////////////////////////////////////////////////////
    // MetaModelAdjuster impl
    // //////////////////////////////////////////////////////

    @Override
    public void refineMetaModelValidator(MetaModelValidatorComposite metaModelValidator, IsisConfiguration configuration) {
        persistenceSessionFactoryDelegate.refineMetaModelValidator(metaModelValidator, configuration);
    }

    @Override
    public void refineProgrammingModel(ProgrammingModel baseProgrammingModel, IsisConfiguration configuration) {
        persistenceSessionFactoryDelegate.refineProgrammingModel(baseProgrammingModel, configuration);
    }

    // //////////////////////////////////////////////////////
    // FixturesInstalledFlag impl
    // //////////////////////////////////////////////////////

    @Override
    public Boolean isFixturesInstalled() {
        return fixturesInstalled;
    }

    @Override
    public void setFixturesInstalled(final Boolean fixturesInstalled) {
        this.fixturesInstalled = fixturesInstalled;
    }

    // //////////////////////////////////////////////////////
    // Dependencies (injected from constructor)
    // //////////////////////////////////////////////////////

    public IsisConfiguration getConfiguration() {
        return configuration;
    }
    
    // //////////////////////////////////////////////////////
    // Dependencies (injected via setters)
    // //////////////////////////////////////////////////////

    @Override
    public void setServices(final List<Object> serviceList) {
        this.serviceList = serviceList;
    }

    // //////////////////////////////////////////////////////
    // Dependencies (from context)
    // //////////////////////////////////////////////////////

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

}
