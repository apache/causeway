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

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.util.List;

import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;

/**
 * Implementation that just delegates to a supplied
 * {@link PersistenceSessionFactory}.
 */
public class PersistenceSessionFactoryDelegating implements PersistenceSessionFactory, FixturesInstalledFlag {

    private final DeploymentType deploymentType;
    private final PersistenceSessionFactoryDelegate persistenceSessionFactoryDelegate;
    private SpecificationLoader specificationLoader;
    private List<Object> serviceList;

    private Boolean fixturesInstalled;

    public PersistenceSessionFactoryDelegating(final DeploymentType deploymentType, final PersistenceSessionFactoryDelegate persistenceSessionFactoryDelegate) {
        this.deploymentType = deploymentType;
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
        ensureThatState(specificationLoader, is(notNullValue()));
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

        doInit();
    }

    /**
     * Optional hook method for implementation-specific initialization.
     */
    protected void doInit() {
    }

    @Override
    public final void shutdown() {
        doShutdown();
        // other
    }

    /**
     * Optional hook method for implementation-specific shutdown.
     */
    protected void doShutdown() {
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
    // Dependencies (injected via setters)
    // //////////////////////////////////////////////////////

    @Override
    public List<Object> getServices() {
        return serviceList;
    }

    @Override
    public void setServices(final List<Object> serviceList) {
        this.serviceList = serviceList;
    }

    /**
     * @see #setSpecificationLoader(SpecificationLoader)
     */
    @Override
    public SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    /**
     * Injected prior to {@link #init()}.
     */
    @Override
    public void setSpecificationLoader(final SpecificationLoader specificationLoader) {
        this.specificationLoader = specificationLoader;
    }

}
