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


package org.apache.isis.core.runtime.viewer;

import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.metamodel.config.ConfigurationBuilder;
import org.apache.isis.core.metamodel.config.ConfigurationBuilderAware;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.installers.InstallerLookup;
import org.apache.isis.core.runtime.installers.InstallerLookupAware;
import org.apache.isis.core.runtime.web.WebAppSpecification;
import org.apache.isis.runtime.system.DeploymentType;
import org.apache.isis.runtime.system.IsisSystem;
import org.apache.isis.runtime.system.SystemConstants;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;


public abstract class IsisViewerAbstract implements IsisViewer {

    /**
     * @see {@link #setDeploymentType(DeploymentType)}
     */
    private DeploymentType deploymentType;

    private InstallerLookup installerLookup;
    private ConfigurationBuilder configurationBuilder;
    private IsisSystem system; // never written to!!!

    /**
     * Optionally set, see {@link #setAuthenticationRequestViaArgs(AuthenticationRequest)}
     */
    private AuthenticationRequest authenticationRequestViaArgs;

    // ////////////////////////////////////////////////////////////////
    // Settings
    // ////////////////////////////////////////////////////////////////

    public void init() {

        ensureDependenciesInjected();

        IsisConfiguration configuration = configurationBuilder.getConfiguration();
        deploymentType = DeploymentType.lookup(configuration.getString(SystemConstants.DEPLOYMENT_TYPE_KEY));

        String user = configuration.getString(SystemConstants.USER_KEY);
        String password = configuration.getString(SystemConstants.PASSWORD_KEY);

        if (user != null) {
            authenticationRequestViaArgs = new AuthenticationRequestPassword(user, password);
        }
    }

    public void shutdown() {
    // does nothing
    }

    // ////////////////////////////////////////////////////////////////
    // Settings
    // ////////////////////////////////////////////////////////////////

    public final DeploymentType getDeploymentType() {
        return deploymentType;
    }

    /**
     * Default implementation to return null, indicating that this viewer should not be run in a web
     * container.
     */
    public WebAppSpecification getWebAppSpecification() {
        return null;
    }

    public AuthenticationRequest getAuthenticationRequestViaArgs() {
        return authenticationRequestViaArgs;
    }

    protected void clearAuthenticationRequestViaArgs() {
        authenticationRequestViaArgs = null;
    }

    // ////////////////////////////////////////////////////////////////
    // Post-bootstrapping
    // ////////////////////////////////////////////////////////////////

    public LogonFixture getLogonFixture() {
        // REVIEW: findBugs says this will always return null, since 'system' never written to...
        return system != null ? system.getLogonFixture() : null;
    }

    // ////////////////////////////////////////////////////////////////
    // Dependencies (injected)
    // ////////////////////////////////////////////////////////////////

    protected void ensureDependenciesInjected() {
        Ensure.ensureThatState(installerLookup, is(not(nullValue())));
        Ensure.ensureThatState(configurationBuilder, is(not(nullValue())));
    }

    /**
     * Injected by virtue of being {@link InstallerLookupAware}.
     */
    public void setInstallerLookup(final InstallerLookup installerLookup) {
        this.installerLookup = installerLookup;
    }

    protected ConfigurationBuilder getConfigurationBuilder() {
		return configurationBuilder;
	}

    /**
     * Injected by virtue of being {@link ConfigurationBuilderAware}.
     */
    public void setConfigurationBuilder(final ConfigurationBuilder configurationBuilder) {
        this.configurationBuilder = configurationBuilder;
    }

    // ////////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // ////////////////////////////////////////////////////////////////

    /**
     * Available after {@link IsisSystem} has been bootstrapped.
     */
    protected static IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }

    /**
     * Available after {@link IsisSystem} has been bootstrapped.
     */
    public static AuthenticationManager getAuthenticationManager() {
        return IsisContext.getAuthenticationManager();
    }

}
