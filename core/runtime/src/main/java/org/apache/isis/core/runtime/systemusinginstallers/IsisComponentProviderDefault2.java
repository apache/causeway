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

import org.apache.isis.applib.AppManifest;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.resource.ResourceStreamSourceContextLoaderClassPath;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.standard.AuthenticationManagerStandard;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.authorization.standard.AuthorizationManagerStandard;

public class IsisComponentProviderDefault2 extends IsisComponentProvider  {

    //region > constructors

    public IsisComponentProviderDefault2(
            final AppManifest appManifest,
            final IsisConfiguration configurationOverride) {
        this(elseDefault(configurationOverride), appManifest);

    }

    private IsisComponentProviderDefault2(
            final IsisConfigurationDefault configuration,
            final AppManifest appManifest
    ) {
        this(configuration, appManifest,
                // integration tests ignore appManifest for authentication and authorization.
                authenticationManagerWithBypass(configuration),
                new AuthorizationManagerStandard(configuration));
    }

    private IsisComponentProviderDefault2(
            final IsisConfigurationDefault configuration,
            final AppManifest appManifest,
            final AuthenticationManager authenticationManager,
            final AuthorizationManager authorizationManager
    ) {
        super(appManifest, configuration, authenticationManager, authorizationManager);
    }

    //endregion

    //region > constructor helpers (factories)
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
    private static AuthenticationManager authenticationManagerWithBypass(final IsisConfiguration configuration ) {
        final AuthenticationManagerStandard authenticationManager = new AuthenticationManagerStandard(configuration);
        authenticationManager.addAuthenticator(new AuthenticatorBypass(configuration));
        return authenticationManager;
    }
    //endregion


}
