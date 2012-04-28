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

package org.apache.isis.runtimes.dflt.testsupport;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.runtimes.dflt.objectstores.dflt.InMemoryPersistenceMechanismInstaller;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.PersistenceMechanismInstaller;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerPersist;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.PersistenceSessionObjectStore;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures.Fixtures.Initialization;
import org.apache.isis.security.dflt.authentication.AuthenticationRequestDefault;
import org.apache.isis.tck.dom.eg.ExamplePojoAggregated;
import org.apache.isis.tck.dom.eg.ExamplePojoRepository;
import org.apache.isis.tck.dom.eg.ExamplePojoWithCollections;
import org.apache.isis.tck.dom.eg.ExamplePojoWithReferences;
import org.apache.isis.tck.dom.eg.ExamplePojoWithValues;
import org.apache.isis.tck.dom.eg.TestPojo;
import org.apache.isis.tck.dom.eg.TestPojoRepository;

/**
 * Wraps a plain {@link IsisSystemDefault}, and provides a number of features to assist with testing.
 */
public class IsisSystemWithFixtures implements org.junit.rules.TestRule {

    /**
     * A precanned set of fixtures for use by tests if desired.
     */
    public static class Fixtures {

        public enum Initialization {
            INIT,
            NO_INIT
        }

        public TestPojoRepository testPojoRepository = new TestPojoRepository();
        public ExamplePojoRepository examplePojoRepository = new ExamplePojoRepository();

        public TestPojo testPojo1, testPojo2;
        public ExamplePojoWithValues epv1, epv2, epv3, epv4, epv5, epv6;
        public ExamplePojoWithReferences epr1, epr2, epr3, epr4, epr5, epr6;
        public ExamplePojoWithCollections epc1, epc2, epc3, epc4, epc5, epc6;
        public ExamplePojoAggregated epr1_a1, epr1_a2, epr1_a3, epc1_a1, epc1_a2, epc1_a3;

        private void init(DomainObjectContainer container) {
            testPojo1 = container.newTransientInstance(TestPojo.class);
            testPojo2 = container.newTransientInstance(TestPojo.class);
            epv1 = container.newTransientInstance(ExamplePojoWithValues.class);
            epv2 = container.newTransientInstance(ExamplePojoWithValues.class);
            epv3 = container.newTransientInstance(ExamplePojoWithValues.class);
            epv4 = container.newTransientInstance(ExamplePojoWithValues.class);
            epv5 = container.newTransientInstance(ExamplePojoWithValues.class);
            epv6 = container.newTransientInstance(ExamplePojoWithValues.class);
            epr1 = container.newTransientInstance(ExamplePojoWithReferences.class);
            epr2 = container.newTransientInstance(ExamplePojoWithReferences.class);
            epr3 = container.newTransientInstance(ExamplePojoWithReferences.class);
            epr4 = container.newTransientInstance(ExamplePojoWithReferences.class);
            epr5 = container.newTransientInstance(ExamplePojoWithReferences.class);
            epr6 = container.newTransientInstance(ExamplePojoWithReferences.class);
            epc1 = container.newTransientInstance(ExamplePojoWithCollections.class);
            epc2 = container.newTransientInstance(ExamplePojoWithCollections.class);
            epc3 = container.newTransientInstance(ExamplePojoWithCollections.class);
            epc4 = container.newTransientInstance(ExamplePojoWithCollections.class);
            epc5 = container.newTransientInstance(ExamplePojoWithCollections.class);
            epc6 = container.newTransientInstance(ExamplePojoWithCollections.class);
            epr1_a1 = container.newAggregatedInstance(epr1, ExamplePojoAggregated.class);
            epr1_a2 = container.newAggregatedInstance(epr1, ExamplePojoAggregated.class);
            epr1_a3 = container.newAggregatedInstance(epr1, ExamplePojoAggregated.class);
            epc1_a1 = container.newAggregatedInstance(epc1, ExamplePojoAggregated.class);
            epc1_a2 = container.newAggregatedInstance(epc1, ExamplePojoAggregated.class);
            epc1_a3 = container.newAggregatedInstance(epc1, ExamplePojoAggregated.class);
        }
    }

    private IsisSystemDefault isisSystem;
    private AuthenticationSession authenticationSession;

    // public visibility just to reduce noise in tests
    public DomainObjectContainer container;
    // public visibility just to reduce noise in tests
    public final Fixtures fixtures;
    
    private Initialization fixturesInitialization;
    private final IsisConfiguration configuration;
    private final PersistenceMechanismInstaller persistenceMechanismInstaller;
    private final AuthenticationRequest authenticationRequest;
    private final List<Object> services;

    
    ////////////////////////////////////////////////////////////
    // constructor
    ////////////////////////////////////////////////////////////

    public static class Builder {

        private AuthenticationRequest authenticationRequest = new AuthenticationRequestDefault("tester");
        
        private Initialization fixturesInitialization = Initialization.INIT;
        private IsisConfiguration configuration;
        private PersistenceMechanismInstaller persistenceMechanismInstaller = new InMemoryPersistenceMechanismInstaller();

        private Object[] services;

        public Builder with(IsisConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }
        
        public Builder with(Initialization initialization) {
            this.fixturesInitialization = initialization;
            return this;
        }
        
        public Builder with(PersistenceMechanismInstaller persistenceMechanismInstaller) {
            this.persistenceMechanismInstaller = persistenceMechanismInstaller;
            return this;
        }
        
        public Builder with(AuthenticationRequest authenticationRequest) {
            this.authenticationRequest = authenticationRequest;
            return this;
        }

        public Builder withServices(Object... services) {
            this.services = services;
            return this;
        }
        
        public IsisSystemWithFixtures build() {
            final List<Object> servicesIfAny = services != null? Arrays.asList(services): null;
            return new IsisSystemWithFixtures(fixturesInitialization, configuration, persistenceMechanismInstaller, authenticationRequest, servicesIfAny);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private IsisSystemWithFixtures(Initialization fixturesInitialization, IsisConfiguration configuration, PersistenceMechanismInstaller persistenceMechanismInstaller, AuthenticationRequest authenticationRequest, List<Object> services) {
        this.fixturesInitialization = fixturesInitialization;
        this.configuration = configuration;
        this.persistenceMechanismInstaller = persistenceMechanismInstaller;
        this.authenticationRequest = authenticationRequest;
        this.fixtures = new Fixtures();
        if(services == null) {
            services = Arrays.asList(fixtures.testPojoRepository, fixtures.examplePojoRepository);
        }
        this.services = services;
    }


    ////////////////////////////////////////////////////////////
    // setup, teardown
    ////////////////////////////////////////////////////////////
    

    /**
     * Intended to be called from a test's {@link Before} method.
     */
    public void setUpSystem() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        boolean firstTime = isisSystem == null;
        if(firstTime) {
            isisSystem = createIsisSystem(services);
            isisSystem.init();
            IsisContext.closeSession();
        }

        final AuthenticationManager authenticationManager = isisSystem.getSessionFactory().getAuthenticationManager();
        authenticationSession = authenticationManager.authenticate(authenticationRequest);

        IsisContext.openSession(authenticationSession);
        container = getContainer();
        if(firstTime && fixturesInitialization == Fixtures.Initialization.INIT) {
            fixtures.init(container);
        }
    }

    private DomainObjectContainer getContainer() {
        return IsisContext.getPersistenceSession().getServicesInjector().getContainer();
    }

    /**
     * Intended to be called from a test's {@link After} method.
     */
    public void tearDownSystem() throws Exception {
        IsisContext.closeSession();
    }

    public void bounceSystem() throws Exception {
        tearDownSystem();
        setUpSystem();
    }


    private IsisSystemDefault createIsisSystem(List<Object> services) {
        final IsisSystemDefault system = new IsisSystemDefault(DeploymentType.UNIT_TESTING, services) {
            @Override
            public IsisConfiguration getConfiguration() {
                if(IsisSystemWithFixtures.this.configuration != null) {
                    return IsisSystemWithFixtures.this.configuration;
                } else {
                    return super.getConfiguration();
                }
            }
            @Override
            protected PersistenceMechanismInstaller obtainPersistenceMechanismInstaller(IsisConfiguration configuration) {
                final PersistenceMechanismInstaller installer = IsisSystemWithFixtures.this.persistenceMechanismInstaller;
                configuration.injectInto(installer);
                return installer;
            }
        };
        return system;
    }



    
    ////////////////////////////////////////////////////////////
    // properties
    ////////////////////////////////////////////////////////////

    /**
     * The {@link IsisSystemDefault} created during {@link #setUpSystem()}.
     * 
     * <p>
     * Can fine-tune the actual implementation using the hook {@link #createIsisSystem()}.
     */
    public IsisSystemDefault getIsisSystem() {
        return isisSystem;
    }

    /**
     * The {@link AuthenticationSession} created during {@link #setUpSystem()}.
     * 
     * <p>
     * Can fine-tune before hand using {@link #createAuthenticationRequest()}.
     */
    public AuthenticationSession getAuthenticationSession() {
        return authenticationSession;
    }



    ////////////////////////////////////////////////////////////
    // Convenience for tests
    ////////////////////////////////////////////////////////////

    public ObjectSpecification loadSpecification(Class<?> cls) {
        return getIsisSystem().getSessionFactory().getSpecificationLoader().loadSpecification(cls);
    }

    public ObjectAdapter persist(Object domainObject) {
        ensureSessionInProgress();
        ensureObjectIsNotPersistent(domainObject);
        container.persist(domainObject);
        return adapterFor(domainObject);
    }

    public ObjectAdapter destroy(Object domainObject ) {
        ensureSessionInProgress();
        ensureObjectIsPersistent(domainObject);
        container.remove(domainObject);
        return adapterFor(domainObject);
    }

    public ObjectAdapter adapterFor(Object domainObject) {
        ensureSessionInProgress();
        return IsisContext.getPersistenceSession().getAdapterManager().adapterFor(domainObject);
    }

    public ObjectAdapter recreateAdapter(RootOid oid) {
        ensureSessionInProgress();
        return IsisContext.getPersistenceSession().recreateAdapter(oid);
    }

    public ObjectAdapter remapAsPersistent(RootOid oid, Object pojo) {
        ensureSessionInProgress();
        ensureObjectIsNotPersistent(pojo);
        final ObjectAdapter adapter = adapterFor(pojo);
        ((AdapterManagerPersist)IsisContext.getPersistenceSession().getAdapterManager()).remapAsPersistent(adapter);
        return adapter;
    }

    @SuppressWarnings("unchecked")
    public <T extends ObjectStore> T getObjectStore(Class<T> cls) {
        final PersistenceSessionObjectStore persistenceSession = (PersistenceSessionObjectStore) IsisContext.getPersistenceSession();
        return (T) persistenceSession.getObjectStore();
    }

    private static void ensureSessionInProgress() {
        if(!IsisContext.inSession()) {
            throw new IllegalStateException("Session must be in progress");
        }
    }

    private void ensureObjectIsNotPersistent(Object domainObject) {
        if(container.isPersistent(domainObject)) {
            throw new IllegalArgumentException("domain object is already persistent");
        }
    }

    private void ensureObjectIsPersistent(Object domainObject) {
        if(!container.isPersistent(domainObject)) {
            throw new IllegalArgumentException("domain object is not persistent");
        }
    }

    ////////////////////////////////////////////////////////////
    // JUnit @Rule integration
    ////////////////////////////////////////////////////////////

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                setUpSystem();
                try {
                    base.evaluate();
                } finally {
                    tearDownSystem();
                }
            }
        };
    }



    
}
