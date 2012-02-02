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

package org.apache.isis.runtimes.dflt.runtime.viewer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderAware;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.IsisSystem;
import org.apache.isis.runtimes.dflt.runtime.system.SystemConstants;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.systemdependencyinjector.SystemDependencyInjector;
import org.apache.isis.runtimes.dflt.runtime.systemdependencyinjector.SystemDependencyInjectorAware;
import org.apache.isis.runtimes.dflt.runtime.viewer.web.WebAppSpecification;

public abstract class IsisViewerAbstract implements IsisViewer {

    /**
     * @see {@link #setDeploymentType(DeploymentType)}
     */
    private DeploymentType deploymentType;

    private SystemDependencyInjector systemDependencyInjector;
    private IsisConfigurationBuilder isisConfigurationBuilder;

    /**
     * Optionally set, see
     * {@link #setAuthenticationRequestViaArgs(AuthenticationRequest)}
     */
    private AuthenticationRequest authenticationRequestViaArgs;

    // ////////////////////////////////////////////////////////////////
    // Settings
    // ////////////////////////////////////////////////////////////////

    @Override
    public void init() {

        ensureDependenciesInjected();

        final IsisConfiguration configuration = isisConfigurationBuilder.getConfiguration();
        deploymentType = DeploymentType.lookup(configuration.getString(SystemConstants.DEPLOYMENT_TYPE_KEY));

        final String user = configuration.getString(SystemConstants.USER_KEY);
        final String password = configuration.getString(SystemConstants.PASSWORD_KEY);

        if (user != null) {
            authenticationRequestViaArgs = new AuthenticationRequestPassword(user, password);
        }
    }

    @Override
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
     * Default implementation to return null, indicating that this viewer should
     * not be run in a web container.
     */
    @Override
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
        return null;
    }

    // ////////////////////////////////////////////////////////////////
    // Dependencies (injected)
    // ////////////////////////////////////////////////////////////////

    protected void ensureDependenciesInjected() {
        Ensure.ensureThatState(systemDependencyInjector, is(not(nullValue())));
        Ensure.ensureThatState(isisConfigurationBuilder, is(not(nullValue())));
    }

    /**
     * Injected by virtue of being {@link SystemDependencyInjectorAware}.
     */
    @Override
    public void setSystemDependencyInjector(final SystemDependencyInjector dependencyInjector) {
        this.systemDependencyInjector = dependencyInjector;
    }

    protected IsisConfigurationBuilder getConfigurationBuilder() {
        return isisConfigurationBuilder;
    }

    /**
     * Injected by virtue of being {@link IsisConfigurationBuilderAware}.
     */
    @Override
    public void setConfigurationBuilder(final IsisConfigurationBuilder isisConfigurationBuilder) {
        this.isisConfigurationBuilder = isisConfigurationBuilder;
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
