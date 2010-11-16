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


package org.apache.isis.viewer.junit.internal;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.runtime.installers.InstallerLookup;
import org.apache.isis.core.runtime.userprofile.inmemory.InMemoryUserProfileStoreInstaller;
import org.apache.isis.runtime.authentication.standard.noop.NoopAuthenticationManagerInstaller;
import org.apache.isis.runtime.authorization.standard.noop.NoopAuthorizationManagerInstaller;
import org.apache.isis.runtime.system.DeploymentType;
import org.apache.isis.runtime.system.installers.IsisSystemUsingInstallers;
import org.junit.internal.runners.TestClass;

public class IsisSystemUsingInstallersWithinJunit extends IsisSystemUsingInstallers {

    private final TestClass testClass;

    public IsisSystemUsingInstallersWithinJunit(
            final DeploymentType deploymentType, 
            final InstallerLookup installerLookup, 
            final TestClass testClass) {
        super(deploymentType, installerLookup);
        this.testClass = testClass;
        
        setAuthenticationInstaller(getInstallerLookup().injectDependenciesInto(new NoopAuthenticationManagerInstaller()));
        setAuthorizationInstaller(getInstallerLookup().injectDependenciesInto(new NoopAuthorizationManagerInstaller()));
        setPersistenceMechanismInstaller(getInstallerLookup().injectDependenciesInto(new InMemoryPersistenceMechanismInstallerWithinJunit()));
        setUserProfileStoreInstaller(getInstallerLookup().injectDependenciesInto(new InMemoryUserProfileStoreInstaller()));

        // fixture installer
        FixtureInstallerAnnotatedClass fixtureInstaller = new FixtureInstallerAnnotatedClass();
        try {
            fixtureInstaller.addFixturesAnnotatedOn(this.testClass.getJavaClass());
        } catch (InstantiationException e) {
            throw new IsisException(e);
        } catch (IllegalAccessException e) {
            throw new IsisException(e);
        }
        setFixtureInstaller(fixtureInstaller);

        // services installer
        ServicesInstallerAnnotatedClass servicesInstaller = new ServicesInstallerAnnotatedClass();
        try {
            servicesInstaller.addServicesAnnotatedOn(this.testClass.getJavaClass());
        } catch (InstantiationException e) {
            throw new IsisException(e);
        } catch (IllegalAccessException e) {
            throw new IsisException(e);
        }
		setServicesInstaller(servicesInstaller);
    }

    public TestClass getTestClass() {
		return testClass;
	}
    

}


