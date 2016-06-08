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
import org.apache.isis.applib.fixtures.InstallableFixture;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.resource.ResourceStreamSourceContextLoaderClassPath;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.standard.AuthenticationManagerStandard;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.authorization.standard.AuthorizationManagerStandard;
import org.apache.isis.core.runtime.fixtures.FixturesInstallerFromConfiguration;
import org.apache.isis.core.runtime.services.ServicesInstallerFromConfiguration;
import org.apache.isis.core.runtime.services.ServicesInstallerFromConfigurationAndAnnotation;

public class IsisComponentProviderDefault2 extends IsisComponentProvider  {




    public IsisComponentProviderDefault2(
            final AppManifest appManifestIfAny,
            final List<Object> servicesOverride,
            final List<InstallableFixture> fixturesOverride,
            final IsisConfiguration configurationOverride) {
        super(appManifestIfAny, elseDefault(configurationOverride));

        this.services = appManifestIfAny != null
                ? new ServicesInstallerFromConfigurationAndAnnotation(getConfiguration()).getServices()
                : servicesOverride != null
                    ? servicesOverride
                    : new ServicesInstallerFromConfiguration(getConfiguration()).getServices();

        final String fixtureClassNamesCsv = classNamesFrom(
                        appManifestIfAny != null ? appManifestIfAny.getFixtures() : fixturesOverride);

        putConfigurationProperty(FixturesInstallerFromConfiguration.FIXTURES, fixtureClassNamesCsv);

        // integration tests ignore appManifest for authentication and authorization.
        this.authenticationManager = createAuthenticationManager();
        this.authorizationManager = createAuthorizationManager();

    }

    /**
     * Default will read <tt>isis.properties</tt> (and other optional property files) from the &quot;config&quot;
     * package on the current classpath.
     */
    private static IsisConfigurationDefault elseDefault(final IsisConfiguration configuration) {
        return configuration != null
                ? (IsisConfigurationDefault) configuration
                : new IsisConfigurationDefault(ResourceStreamSourceContextLoaderClassPath.create("config"));
    }

    /**
     * The standard authentication manager, configured with the 'bypass' authenticator (allows all requests through).
     */
    private AuthenticationManager createAuthenticationManager() {
        final AuthenticationManagerStandard authenticationManager =
                new AuthenticationManagerStandard(getConfiguration());
        authenticationManager.addAuthenticator(new AuthenticatorBypass(getConfiguration()));
        return authenticationManager;
    }

    private AuthorizationManager createAuthorizationManager() {
        return new AuthorizationManagerStandard(getConfiguration());
    }


}
