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

import java.util.List;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.runtime.authentication.AuthenticationManagerInstaller;
import org.apache.isis.core.runtime.authorization.AuthorizationManagerInstaller;
import org.apache.isis.core.runtime.fixtures.FixturesInstallerFromConfiguration;
import org.apache.isis.core.runtime.installerregistry.InstallerLookup;
import org.apache.isis.core.runtime.services.ServicesInstaller;
import org.apache.isis.core.runtime.services.ServicesInstallerFromConfigurationAndAnnotation;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.SystemConstants;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

public class IsisComponentProviderUsingInstallers extends IsisComponentProvider {


    public IsisComponentProviderUsingInstallers(
            final DeploymentType deploymentType,
            final AppManifest appManifestIfAny,
            final InstallerLookup installerLookup) {
        super(deploymentType,
                appManifestIfAny(appManifestIfAny, installerLookup),
                configurationFrom(installerLookup));

        if(getAppManifestIfAny() != null) {

            final String authenticationMechanism = getAppManifestIfAny().getAuthenticationMechanism();
            putConfigurationProperty(SystemConstants.AUTHENTICATION_INSTALLER_KEY, authenticationMechanism);

            final String authorizationMechanism = getAppManifestIfAny().getAuthorizationMechanism();
            putConfigurationProperty(SystemConstants.AUTHORIZATION_INSTALLER_KEY, authorizationMechanism);
        }

        if(getAppManifestIfAny() != null) {
            List<Class<? extends FixtureScript>> fixtureClasses = getAppManifestIfAny().getFixtures();
            final String fixtureClassNamesCsv = classNamesFrom(fixtureClasses);
            putConfigurationProperty(FixturesInstallerFromConfiguration.FIXTURES, fixtureClassNamesCsv);
        }

        // loading installers causes the configuration to be appended to successively
        final String requestedAuthencn = getConfiguration().getString(SystemConstants.AUTHENTICATION_INSTALLER_KEY);
        AuthenticationManagerInstaller authenticationInstaller =
                installerLookup.authenticationManagerInstaller(requestedAuthencn);

        final String requestedAuthorzn = getConfiguration().getString(SystemConstants.AUTHORIZATION_INSTALLER_KEY);
        AuthorizationManagerInstaller authorizationInstaller =
                installerLookup.authorizationManagerInstaller(requestedAuthorzn);

        ServicesInstaller servicesInstaller = new ServicesInstallerFromConfigurationAndAnnotation(getConfiguration());

        // ensure we were able to load all components via InstallerLookup (fail fast)
        ensureThatState(authenticationInstaller, is(not(nullValue())),
                "authenticationInstaller could not be looked up");
        ensureThatState(authorizationInstaller, is(not(nullValue())),
                "authorizationInstaller could not be looked up");

        // eagerly calculate
        authenticationManager = authenticationInstaller.createAuthenticationManager();
        authorizationManager = authorizationInstaller.createAuthorizationManager();

        services = servicesInstaller.getServices();

        ensureInitialized();
    }

    /**
     * If an {@link AppManifest} was explicitly provided (eg from the Guice <tt>IsisWicketModule</tt> when running
     * unde the Wicket viewer) then use that; otherwise read the <tt>isis.properties</tt> config file and look
     * for an <tt>isis.appManifest</tt> entry instead.
     */
    private static AppManifest appManifestIfAny(
            final AppManifest appManifestFromConstructor,
            final InstallerLookup installerLookup) {
        if(appManifestFromConstructor != null) {
            return appManifestFromConstructor;
        }
        final IsisConfigurationDefault configuration = configurationFrom(installerLookup);
        final String appManifestFromConfiguration = configuration.getString(SystemConstants.APP_MANIFEST_KEY);
        return appManifestFromConfiguration != null
                ? InstanceUtil.createInstance(appManifestFromConfiguration, AppManifest.class)
                : null;
    }


    //endregion


    //region > helpers

    private static IsisConfigurationDefault configurationFrom(final InstallerLookup installerLookup) {
        return installerLookup.getConfiguration();
    }

    //endregion


}
