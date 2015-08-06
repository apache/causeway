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
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import org.apache.isis.applib.GlobSpec;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.lang.ClassUtil;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.fixtures.FixturesInstaller;
import org.apache.isis.core.runtime.services.ServicesInstallerFromAnnotation;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.objectstore.jdo.service.RegisterEntities;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

public abstract class IsisComponentProviderAbstract implements IsisComponentProvider {

    protected final DeploymentType deploymentType;
    /**
     * may be null.
     */
    protected final GlobSpec globSpec;

    /**
     * populated by subclass, in its constructor.
     */
    protected IsisConfiguration configuration;
    /**
     * populated by subclass, in its constructor.
     */
    protected List<Object> services;
    /**
     * populated by subclass, in its constructor.
     */
    protected FixturesInstaller fixturesInstaller;
    /**
     * populated by subclass, in its constructor.
     */
    protected AuthenticationManager authenticationManager;
    /**
     * populated by subclass, in its constructor.
     */
    protected AuthorizationManager authorizationManager;

    public IsisComponentProviderAbstract(
            final DeploymentType deploymentType,
            final GlobSpec globSpec) {

        this.deploymentType = deploymentType;
        this.globSpec = globSpec;
    }

    /**
     * Provided for subclasses to call to ensure that they have correctly populated all fields.
     */
    protected void ensureInitialized() {
        ensureThatState(authenticationManager, is(not(nullValue())));
        ensureThatState(authorizationManager, is(not(nullValue())));
        ensureThatState(services, is(not(nullValue())));
        ensureThatState(fixturesInstaller, is(not(nullValue())));
        ensureThatState(configuration, is(not(nullValue())), "fixtureInstaller could not be looked up");
    }


    //region > globSpec helpers
    protected void specifyServicesAndRegisteredEntitiesUsing(final GlobSpec globSpec) {
        final String packageNamesCsv = modulePackageNamesFrom(globSpec);

        putConfigurationProperty(ServicesInstallerFromAnnotation.PACKAGE_PREFIX_KEY, packageNamesCsv);
        putConfigurationProperty(RegisterEntities.PACKAGE_PREFIX_KEY, packageNamesCsv);
    }

    private String modulePackageNamesFrom(final GlobSpec globSpec) {
        List<Class<?>> modules = globSpec.getModules();
        if (modules == null || modules.isEmpty()) {
            throw new IllegalArgumentException(
                    "If a globSpec is provided then it must return a non-empty set of modules");
        }

        final Iterable<String> iter = Iterables.transform(modules, ClassUtil.Functions.packageNameOf());
        return Joiner.on(',').join(iter);
    }

    protected String fixtureClassNamesFrom(final List<?> fixtures) {
        if (fixtures == null) {
            return null;
        }
        final Iterable<String> fixtureClassNames = Iterables.transform(fixtures, classNameOf());
        return Joiner.on(',').join(fixtureClassNames);
    }

    private Function<Object, String> classNameOf() {
        return new Function<Object, String>() {
                        @Nullable @Override
                        public String apply(final Object input) {
                            Class<?> aClass = input instanceof Class ? (Class<?>)input: input.getClass();
                            return aClass.getName();
                        }
                    };
    }

    protected void overrideConfigurationUsing(final GlobSpec globSpec) {
        final Map<String, String> configurationProperties = globSpec.getConfigurationProperties();
        if (configurationProperties != null) {
            for (Map.Entry<String, String> configProp : configurationProperties.entrySet()) {
                putConfigurationProperty(configProp.getKey(), configProp.getValue());
            }
        }
    }

    protected final void putConfigurationProperty(final String key, final String value) {
        if(value == null) {
            return;
        }
        doPutConfigurationProperty(key, value);
    }


    /**
     * For subclasses to implement, to update their implementation of {@link IsisConfiguration}.
     */
    protected abstract void doPutConfigurationProperty(final String key, final String value);

    //endregion

    //region > API impl.
    
    @Override
    public DeploymentType getDeploymentType() {
        return deploymentType;
    }

    @Override
    public IsisConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public AuthenticationManager provideAuthenticationManager(final DeploymentType deploymentType) {
        return authenticationManager;
    }

    @Override
    public  AuthorizationManager provideAuthorizationManager(final DeploymentType deploymentType) {
        return authorizationManager;
    }

    @Override
    public FixturesInstaller provideFixturesInstaller() {
        return fixturesInstaller;
    }

    @Override
    public List<Object> provideServices() {
        return services;
    }
    //endregion

}