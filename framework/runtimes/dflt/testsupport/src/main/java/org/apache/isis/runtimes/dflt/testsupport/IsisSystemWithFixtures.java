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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.testsupport.domain.ExamplePojoRepository;
import org.apache.isis.runtimes.dflt.testsupport.domain.ExamplePojoWithCollections;
import org.apache.isis.runtimes.dflt.testsupport.domain.ExamplePojoWithReferences;
import org.apache.isis.runtimes.dflt.testsupport.domain.ExamplePojoWithValues;
import org.apache.isis.runtimes.dflt.testsupport.domain.TestPojo;
import org.apache.isis.runtimes.dflt.testsupport.domain.TestPojoRepository;
import org.apache.isis.security.dflt.authentication.AuthenticationRequestDefault;

/**
 * Wraps a plain {@link IsisSystemDefault}, and provides a number of features to assist with testing.
 */
public class IsisSystemWithFixtures implements org.junit.rules.TestRule {

    /**
     * A precanned set of fixtures for use by tests if desired.
     */
    public static class Fixtures {
        public TestPojoRepository testPojoRepository = new TestPojoRepository();
        public ExamplePojoRepository examplePojoRepository = new ExamplePojoRepository();

        public TestPojo testPojo1, testPojo2;
        public ExamplePojoWithValues epv1, epv2, epv3;
        public ExamplePojoWithReferences epr1, epr2, epr3;
        public ExamplePojoWithCollections epc1, epc2, epc3;

        private void init(DomainObjectContainer container) {
            testPojo1 = container.newTransientInstance(TestPojo.class);
            testPojo2 = container.newTransientInstance(TestPojo.class);
            epv1 = container.newTransientInstance(ExamplePojoWithValues.class);
            epv2 = container.newTransientInstance(ExamplePojoWithValues.class);
            epv3 = container.newTransientInstance(ExamplePojoWithValues.class);
            epr1 = container.newTransientInstance(ExamplePojoWithReferences.class);
            epr2 = container.newTransientInstance(ExamplePojoWithReferences.class);
            epr3 = container.newTransientInstance(ExamplePojoWithReferences.class);
            epc1 = container.newTransientInstance(ExamplePojoWithCollections.class);
            epc2 = container.newTransientInstance(ExamplePojoWithCollections.class);
            epc3 = container.newTransientInstance(ExamplePojoWithCollections.class);
        }
    }

    private IsisSystemDefault isisSystem;
    private AuthenticationSession authenticationSession;

    // public visibility just to reduce noise in tests
    public DomainObjectContainer container;
    // public visibility just to reduce noise in tests
    public Fixtures fixtures;

    
    ////////////////////////////////////////////////////////////
    // setup, teardown
    ////////////////////////////////////////////////////////////
    
    /**
     * Intended to be called from a test's {@link Before} method.
     */
    public void setUpSystem() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        fixtures = new Fixtures();
        
        isisSystem = createIsisSystem();
        isisSystem.init();

        final AuthenticationManager authenticationManager = isisSystem.getSessionFactory().getAuthenticationManager();
        authenticationSession = authenticationManager.authenticate(createAuthenticationRequest());

        IsisContext.openSession(authenticationSession);
        
        container = IsisContext.getPersistenceSession().getServicesInjector().getContainer();
        
        fixtures.init(container);
    }

    /**
     * Intended to be called from a test's {@link After} method.
     */
    public void tearDownSystem() throws Exception {
        IsisContext.closeSession();
    }


    ////////////////////////////////////////////////////////////
    // hooks
    ////////////////////////////////////////////////////////////

    /**
     * Optional hook for tests that want to fine-tune the creation of the underlying
     * {@link IsisSystemDefault}.
     */
    protected IsisSystemDefault createIsisSystem() {
        return new IsisSystemDefault(fixtures.testPojoRepository, fixtures.examplePojoRepository);
    }

    /**
     * Optional hook for tests that want to fine-tune the request.
     */
    protected AuthenticationRequest createAuthenticationRequest() {
        return new AuthenticationRequestDefault("tester");
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
