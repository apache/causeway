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

package org.apache.isis.core.runtime.systemusinginstallers;

import com.google.inject.Inject;

import org.apache.isis.core.runtime.installerregistry.InstallerLookup;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.IsisSystem;
import org.apache.isis.core.runtime.system.IsisSystemFactory;
import org.apache.isis.core.runtime.systemdependencyinjector.SystemDependencyInjector;

/**
 * Implementation of {@link IsisSystemFactory} that uses {@link InstallerLookup}
 * to convert the names of components into actual component instances.
 */
public class IsisSystemThatUsesInstallersFactory implements IsisSystemFactory {

    private final InstallerLookup installerLookup;

    // //////////////////////////////////////////////////////////
    // constructor
    // //////////////////////////////////////////////////////////

    @Inject
    public IsisSystemThatUsesInstallersFactory(final InstallerLookup installerLookup) {
        this.installerLookup = installerLookup;
    }

    // //////////////////////////////////////////////////////////
    // init, shutdown
    // //////////////////////////////////////////////////////////

    @Override
    public void init() {
        // nothing to do
    }

    @Override
    public void shutdown() {
        // nothing to do
    }

    // //////////////////////////////////////////////////////////
    // main API
    // //////////////////////////////////////////////////////////

    @Override
    public IsisSystem createSystem(final DeploymentType deploymentType) {

        final IsisSystemUsingInstallers system = new IsisSystemUsingInstallers(deploymentType, installerLookup);

        system.lookupAndSetAuthenticatorAndAuthorization(deploymentType);
        system.lookupAndSetFixturesInstaller();
        return system;
    }

    // //////////////////////////////////////////////////////////
    // Dependencies (injected or defaulted in constructor)
    // //////////////////////////////////////////////////////////

    public SystemDependencyInjector getInstallerLookup() {
        return installerLookup;
    }

}
