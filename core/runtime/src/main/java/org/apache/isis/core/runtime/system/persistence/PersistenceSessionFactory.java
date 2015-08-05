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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.runtime.persistence.FixturesInstalledFlag;
import org.apache.isis.core.runtime.persistence.ObjectStoreFactory;
import org.apache.isis.core.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContext;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

/**
 * Implementation that just delegates to a supplied
 * {@link org.apache.isis.core.runtime.persistence.ObjectStoreFactory}.
 */
public class PersistenceSessionFactory implements MetaModelRefiner, ApplicationScopedComponent, FixturesInstalledFlag {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceSessionFactory.class);

    private final DeploymentType deploymentType;
    private final IsisConfiguration configuration;
    private final ObjectStoreFactory objectStoreFactory;

    private Boolean fixturesInstalled;

    private final ServicesInjectorSpi servicesInjector;
    private RuntimeContextFromSession runtimeContext;

    public PersistenceSessionFactory(
            final DeploymentType deploymentType,
            final ServicesInjectorSpi servicesInjector,
            final IsisConfiguration isisConfiguration,
            final ObjectStoreFactory objectStoreFactory,
            final RuntimeContextFromSession runtimeContext) {

        ensureThatState(deploymentType, is(notNullValue()));
        ensureThatState(servicesInjector, is(notNullValue()));
        ensureThatState(isisConfiguration, is(not(nullValue())));
        ensureThatState(objectStoreFactory, is(not(nullValue())));
        ensureThatState(runtimeContext, is(not(nullValue())));

        this.deploymentType = deploymentType;
        this.configuration = isisConfiguration;
        this.objectStoreFactory = objectStoreFactory;
        this.servicesInjector = servicesInjector;
        this.runtimeContext = runtimeContext;

    }

    public DeploymentType getDeploymentType() {
        return deploymentType;
    }

    public ObjectStoreFactory getDelegate() {
        return objectStoreFactory;
    }

    public PersistenceSession createPersistenceSession() {

        final ObjectStore objectStore = objectStoreFactory.createObjectStore(getConfiguration());
        ensureThatArg(objectStore, is(not(nullValue())));

        return new PersistenceSession(this, objectStore, getConfiguration());
    }

    public final void init() {

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

        runtimeContext.injectInto(servicesInjector);

        // wire up components
        getSpecificationLoader().injectInto(runtimeContext);

        for (Object service : servicesInjector.getRegisteredServices()) {
            runtimeContext.injectInto(service);
        }

        servicesInjector.init();
    }




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

    public ServicesInjectorSpi getServicesInjector() {
        return servicesInjector;
    }

    // //////////////////////////////////////////////////////
    // MetaModelAdjuster impl
    // //////////////////////////////////////////////////////

    public void refineMetaModelValidator(MetaModelValidatorComposite metaModelValidator, IsisConfiguration configuration) {
        objectStoreFactory.refineMetaModelValidator(metaModelValidator, configuration);
    }

    public void refineProgrammingModel(ProgrammingModel baseProgrammingModel, IsisConfiguration configuration) {
        objectStoreFactory.refineProgrammingModel(baseProgrammingModel, configuration);
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
    // Dependencies (from context)
    // //////////////////////////////////////////////////////

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

}
