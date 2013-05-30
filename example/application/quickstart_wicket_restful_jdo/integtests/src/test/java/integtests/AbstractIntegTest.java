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
package integtests;

import dom.todo.ToDoItems;
import fixture.todo.ToDoItemsFixture;
import objstore.jdo.todo.ToDoItemsJdo;

import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.integtestsupport.IsisSystemForTest;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.core.wrapper.WrapperFactoryDefault;
import org.apache.isis.objectstore.jdo.datanucleus.DataNucleusObjectStore;
import org.apache.isis.objectstore.jdo.datanucleus.DataNucleusPersistenceMechanismInstaller;
import org.apache.isis.objectstore.jdo.service.RegisterEntities;

public abstract class AbstractIntegTest {

    protected ToDoItems toDoItems;
    protected WrapperFactory wrapperFactory;
    protected DomainObjectContainer container;

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);
    
    @Rule
    public IsisSystemForTestRule bootstrapIsis = new IsisSystemForTestRule();

    @Rule
    public ExpectedException expectedExceptions = ExpectedException.none();

    /**
     * Same running system returned for all tests, set up with {@link ToDoItemsFixture}.
     * 
     * <p>
     * The database is NOT reset between tests.
     */
    public IsisSystemForTest getIsft() {
        return bootstrapIsis.getIsisSystemForTest();
    }

    @Before
    public void init() {
        toDoItems = getIsft().getService(ToDoItemsJdo.class);
        wrapperFactory = getIsft().getService(WrapperFactoryDefault.class);
        container = getIsft().container;
    }

    protected <T> T wrap(T obj) {
        return wrapperFactory.wrap(obj);
    }

    protected <T> T unwrap(T obj) {
        return wrapperFactory.unwrap(obj);
    }


    ////////////////////////////////////////////////////////////////
    // Boilerplate
    ////////////////////////////////////////////////////////////////
    
    @BeforeClass
    public static void initClass() {
        PropertyConfigurator.configure("logging.properties");
    }
    
    private static class ToDoIntegTestBuilder extends IsisSystemForTest.Builder {

        public ToDoIntegTestBuilder() {
            withFixtures(new ToDoItemsFixture());
            withLoggingAt(Level.INFO);
            with(testConfiguration());
            with(new DataNucleusPersistenceMechanismInstaller());
            
            withServices(
                    new ToDoItemsJdo(),
                    new WrapperFactoryDefault(),
                    new RegisterEntities()
                    );
        }

        private IsisConfiguration testConfiguration() {
            final IsisConfigurationDefault testConfiguration = new IsisConfigurationDefault();

            testConfiguration.add("isis.persistor.datanucleus.RegisterEntities.packagePrefix", "dom");
            testConfiguration.add("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionURL", "jdbc:hsqldb:mem:test");
            
            testConfiguration.add("isis.persistor.datanucleus.impl.datanucleus.defaultInheritanceStrategy", "TABLE_PER_CLASS");
            testConfiguration.add(DataNucleusObjectStore.INSTALL_FIXTURES_KEY , "true");
            
            testConfiguration.add("isis.persistor.datanucleus.impl.datanucleus.cache.level2.type","none");

            return testConfiguration;
        }
    }

    private static class IsisSystemForTestRule implements MethodRule {
        private static ThreadLocal<IsisSystemForTest> ISFT = new ThreadLocal<IsisSystemForTest>() {
            @Override
            protected IsisSystemForTest initialValue() {
                return new ToDoIntegTestBuilder().build().setUpSystem();
            };
        };

        public IsisSystemForTest getIsisSystemForTest() {
            // reuse same system for all calls.
            return ISFT.get();
        }
        
        @Override
        public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
            final IsisSystemForTest isft = getIsisSystemForTest(); // creates and starts running if required
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    isft.beginTran();
                    base.evaluate();
                    // if an exception is thrown by any test, then we don't attempt to cleanup (eg by calling bounceSystem)#
                    // because - in any case - we only ever install the fixtures once for ALL of the tests.
                    // therefore, just fix the first test that fails and don't worry about any other test failures beyond that
                    // (fix them up one by one)
                    isft.commitTran();
                }
            };
        }
    }
    
}