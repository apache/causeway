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

package org.apache.isis.core.runtime.runner;

import java.util.List;
import java.util.Map;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.runtime.system.SystemConstants;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.session.IsisSessionFactoryBuilder;
import org.apache.isis.core.runtime.systemusinginstallers.IsisComponentProvider;
import org.apache.isis.core.runtime.systemusinginstallers.IsisComponentProviderUsingInstallers;

public class IsisInjectModule extends AbstractModule {

    /**
     * Placeholder for no {@link AppManifest}.
     *
     * <p>
     *     This is bound in by default in <tt>IsisWicketModule</tt>, but is replaced with null when the system is
     *     {@link #provideIsisSessionFactory(AppManifest)} created} .
     * </p>
     */
    private static final AppManifest APP_MANIFEST_NOOP = new AppManifest() {
        @Override public List<Class<?>> getModules() {
            return null;
        }
        @Override public List<Class<?>> getAdditionalServices() {
            return null;
        }

        @Override public String getAuthenticationMechanism() {
            return null;
        }

        @Override public String getAuthorizationMechanism() {
            return null;
        }

        @Override public List<Class<? extends FixtureScript>> getFixtures() {
            return null;
        }

        @Override public Map<String, String> getConfigurationProperties() {
            return null;
        }
    };

    private final DeploymentCategory deploymentCategory;
    private final IsisConfigurationDefault isisConfiguration;

    public IsisInjectModule(
            final IsisConfigurationDefault isisConfiguration) {
        this.isisConfiguration = isisConfiguration;
        this.deploymentCategory = IsisContext.getEnvironment().getDeploymentCategory();
    }

    /**
     * Allows the {@link AppManifest} to be programmatically bound in.
     *
     * <p>
     *     For example, this can be done in override of
     *     <code>IsisWicketApplication#newIsisWicketModule</code>.
     * </p>
     */
    @Override
    protected void configure() {
        bind(AppManifest.class).toInstance(APP_MANIFEST_NOOP);
    }

    /**
     * Simply as provided in the constructor.
     */
    @Provides
    @Singleton
    protected IsisConfiguration provideConfiguration() {
        return isisConfiguration;
    }

    /**
     * Simply as provided in the constructor.
     */
    @Provides
    @Singleton
    protected DeploymentCategory provideDeploymentCategory() {
        return deploymentCategory;
    }

    @Provides
    @com.google.inject.Inject
    @Singleton
    protected IsisSessionFactory provideIsisSessionFactory(
            final AppManifest appManifestIfExplicitlyBound) {

        final AppManifest appManifestToUse = determineAppManifest(appManifestIfExplicitlyBound);

        final IsisComponentProvider componentProvider =
                new IsisComponentProviderUsingInstallers(appManifestToUse, isisConfiguration);

        final IsisSessionFactoryBuilder builder =
                new IsisSessionFactoryBuilder(componentProvider, deploymentCategory, componentProvider.getAppManifest());

        // as a side-effect, if the metamodel turns out to be invalid, then
        // this will push the MetaModelInvalidException into IsisContext.
        return builder.buildSessionFactory();
    }

    @Provides
    @com.google.inject.Inject
    @Singleton
    protected ServicesInjector provideServicesInjector(final IsisSessionFactory isisSessionFactory) {
        return isisSessionFactory.getServicesInjector();
    }


    private AppManifest determineAppManifest(final AppManifest appManifestIfExplicitlyBound) {
        final AppManifest appManifest =
                appManifestIfExplicitlyBound != APP_MANIFEST_NOOP
                ? appManifestIfExplicitlyBound
                        : null;

        return appManifestFrom(appManifest, isisConfiguration);
    }


    /**
     * If an {@link AppManifest} was explicitly provided (eg from the Guice <tt>IsisWicketModule</tt> when running
     * unde the Wicket viewer) then use that; otherwise read the <tt>isis.properties</tt> config file and look
     * for an <tt>isis.appManifest</tt> entry instead.
     */
    private static AppManifest appManifestFrom(
            final AppManifest appManifestFromConstructor,
            final IsisConfiguration configuration) {
        if(appManifestFromConstructor != null) {
            return appManifestFromConstructor;
        }
        final String appManifestFromConfiguration = configuration.getString(SystemConstants.APP_MANIFEST_KEY);
        return appManifestFromConfiguration != null
                ? InstanceUtil.createInstance(appManifestFromConfiguration, AppManifest.class)
                        : null;
    }


}
