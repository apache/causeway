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


package org.apache.isis.extensions.headless.junit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jmock.Mockery;
import org.junit.internal.runners.InitializationError;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.internal.runners.MethodRoadie;
import org.junit.internal.runners.TestMethod;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.extensions.headless.junit.internal.IsisSystemUsingInstallersWithinJunit;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.config.ConfigurationBuilder;
import org.apache.isis.metamodel.config.ConfigurationBuilderDefault;
import org.apache.isis.metamodel.config.ConfigurationBuilderFileSystem;
import org.apache.isis.metamodel.services.ServicesInjector;
import org.apache.isis.runtime.authentication.AuthenticationRequest;
import org.apache.isis.runtime.authentication.standard.SimpleSession;
import org.apache.isis.runtime.authentication.standard.exploration.AuthenticationRequestExploration;
import org.apache.isis.runtime.authentication.standard.exploration.ExplorationSession;
import org.apache.isis.runtime.authentication.standard.fixture.AuthenticationRequestLogonFixture;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.installers.InstallerLookupDefault;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.system.DeploymentType;
import org.apache.isis.runtime.system.Splash;
import org.apache.isis.runtime.system.SystemConstants;
import org.apache.isis.runtime.transaction.IsisTransactionManager;


/**
 * Copied from JMock, and with the same support.
 * 
 */
public class IsisTestRunner extends JUnit4ClassRunner {

    private final Field mockeryField;


    /**
     * Only used during object construction.
     */
    public IsisTestRunner(final Class<?> testClass) throws InitializationError {
        super(testClass);

        // JMock initialization, adapted to allow for no mockery field.
        mockeryField = findFieldAndMakeAccessible(testClass, Mockery.class);
    }

    @Override
    protected void invokeTestMethod(final Method method, final RunNotifier notifier) {

    	final Description description = methodDescription(method);
    	
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilderDefault();
        configurationBuilder.add(SystemConstants.NOSPLASH_KEY, ""+true); // switch off splash
        
        InstallerLookupDefault installerLookup = new InstallerLookupDefault(getClass());
        configurationBuilder.injectInto(installerLookup);
		installerLookup.init();

		IsisSystemUsingInstallersWithinJunit system = null;        
		AuthenticationSession session = null;
        try {
            // init the system; cf similar code in Isis and IsisServletContextInitializer
            final DeploymentType deploymentType = DeploymentType.PROTOTYPE;

            // TODO: replace with regular IsisSystem and remove this subclass.
            system = new IsisSystemUsingInstallersWithinJunit(
                    deploymentType, installerLookup, getTestClass());

            system.init();
            
			// specific to this bootstrap mechanism
            AuthenticationRequest request;
			final LogonFixture logonFixture = system.getFixturesInstaller().getLogonFixture();
			if (logonFixture != null) {
				request = new AuthenticationRequestLogonFixture(logonFixture);
			} else {
				request = new AuthenticationRequestExploration(logonFixture);
			}
			session = IsisContext.getAuthenticationManager().authenticate(request);
			
			IsisContext.openSession(session);
			getTransactionManager().startTransaction();
			
			Object test = createTest();
			getServicesInjector().injectDependencies(test);
			
		    final TestMethod testMethod = wrapMethod(method);
		    new MethodRoadie(test, testMethod, notifier, description).run();
		    
		    getTransactionManager().endTransaction();
            
        } catch (final InvocationTargetException e) {
            notifier.testAborted(description, e.getCause());
            getTransactionManager().abortTransaction();
            return;
        } catch (final Exception e) {
            notifier.testAborted(description, e);
            return;
        } finally {
            if (system != null) {
                if (session != null) {
                    IsisContext.closeSession();
                }
                system.shutdown();
            }
        }
    }


    /**
     * Taken from JMock's runner.
     */
    @Override
    protected TestMethod wrapMethod(final Method method) {
        return new TestMethod(method, getTestClass()) {
            @Override
            public void invoke(final Object testFixture) throws IllegalAccessException, InvocationTargetException {

                super.invoke(testFixture);

                if (mockeryField != null) {
                    mockeryOf(testFixture).assertIsSatisfied();
                }
            }
        };
    }

    /**
     * JMock code.
     * 
     * @param test
     * @return
     */
    protected Mockery mockeryOf(final Object test) {
        if (mockeryField == null) {
            return null;
        }
        try {
            final Mockery mockery = (Mockery) mockeryField.get(test);
            if (mockery == null) {
                throw new IllegalStateException(String.format("Mockery named '%s' is null", mockeryField.getName()));
            }
            return mockery;
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(String.format("cannot get value of field %s", mockeryField.getName()), e);
        }
    }

    /**
     * Adapted from JMock code.
     */
    static Field findFieldAndMakeAccessible(final Class<?> testClass, final Class<?> clazz) throws InitializationError {
        for (Class<?> c = testClass; c != Object.class; c = c.getSuperclass()) {
            for (final Field field : c.getDeclaredFields()) {
                if (clazz.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    return field;
                }
            }
        }
        return null;
    }
    

    
    ///////////////////////////////////////////////////////
    // Dependencies (from context)
    ///////////////////////////////////////////////////////

    private static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }
    
    private static ServicesInjector getServicesInjector() {
        return getPersistenceSession().getServicesInjector();
    }

    private static IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }


}
