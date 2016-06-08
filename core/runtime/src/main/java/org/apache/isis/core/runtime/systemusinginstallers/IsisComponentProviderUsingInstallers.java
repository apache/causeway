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

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.components.Installer;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.factory.InstanceCreationClassException;
import org.apache.isis.core.commons.factory.InstanceCreationException;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.commons.factory.UnavailableClassException;
import org.apache.isis.core.commons.lang.ObjectExtensions;
import org.apache.isis.core.runtime.authentication.AuthenticationManagerInstaller;
import org.apache.isis.core.runtime.authorization.AuthorizationManagerInstaller;
import org.apache.isis.core.runtime.fixtures.FixturesInstallerFromConfiguration;
import org.apache.isis.core.runtime.services.ServicesInstaller;
import org.apache.isis.core.runtime.services.ServicesInstallerFromConfigurationAndAnnotation;
import org.apache.isis.core.runtime.system.IsisSystem;
import org.apache.isis.core.runtime.system.SystemConstants;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

public class IsisComponentProviderUsingInstallers extends IsisComponentProvider {


    public IsisComponentProviderUsingInstallers(
            final IsisConfiguration configuration, final AppManifest appManifestIfAny) {
        this(appManifestIfAny, configuration, new InstallerLookup(configuration));
    }

    private IsisComponentProviderUsingInstallers(
            final AppManifest appManifestIfAny,
            final IsisConfiguration configuration,
            final InstallerLookup installerLookup) {
        super(
                appManifestIfAny(appManifestIfAny, configuration),
                configuration);

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
            final IsisConfiguration configuration) {
        if(appManifestFromConstructor != null) {
            return appManifestFromConstructor;
        }
        final String appManifestFromConfiguration = configuration.getString(SystemConstants.APP_MANIFEST_KEY);
        return appManifestFromConfiguration != null
                ? InstanceUtil.createInstance(appManifestFromConfiguration, AppManifest.class)
                : null;
    }

    //endregion

    /**
     * Looks up authorization and authentication implementations.
     *
     * Previously this class was used to lookup the top-level components of
     * {@link IsisSystem} (persistence, authentication, authorization, reflector and so on),
     * each being looked up from a file called <tt>installer-registry.properties</tt>.
     * Of these responsibilities, only the support for authentication and authorization remains,
     * the remainder have only a single implementation.  The implementation has been
     * substantially simplified.
     *
     * Note that it <i>is</i> possible to use other {@link Installer} implementations;
     * just specify the {@link Installer}'s fully qualified class name.
     *
     * @deprecated - intention is to replace in future using CDI
     */
    @Deprecated
    static class InstallerLookup implements ApplicationScopedComponent {

        private static final Logger LOG = LoggerFactory.getLogger(InstallerLookup.class);

        //region > constructor, fields

        private final IsisConfiguration isisConfiguration;
        private final List<Installer> installerList = Lists.newArrayList();

        public InstallerLookup(final IsisConfiguration isisConfiguration) throws InstanceCreationException {
            this.isisConfiguration = isisConfiguration;

            final List<String> installerClassNames = Lists.newArrayList(
                "org.apache.isis.core.security.authentication.BypassAuthenticationManagerInstaller", // bypass
                "org.apache.isis.security.shiro.authentication.ShiroAuthenticationManagerInstaller", // shiro
                "org.apache.isis.core.security.authorization.BypassAuthorizationManagerInstaller",   // bypass
                "org.apache.isis.security.shiro.authorization.ShiroAuthorizationManagerInstaller"    // shiro
            );

            for (String className : installerClassNames) {
                if (className.length() == 0 || className.startsWith("#")) {
                    continue;
                }
                try {
                    final Installer object = (Installer) InstanceUtil.createInstance(className, isisConfiguration);
                    LOG.debug("created component installer: " + object.getName() + " - " + className);
                    installerList.add(object);
                } catch (final UnavailableClassException e) {
                    LOG.info("component installer not found; it will not be available: " + className);
                } catch (final InstanceCreationClassException e) {
                    LOG.info("instance creation exception: " + e.getMessage());
                }
            }
        }

        //endregion

        //region > framework

        public AuthenticationManagerInstaller authenticationManagerInstaller(final String requested) {

            return getInstaller(
                    AuthenticationManagerInstaller.class,
                    requested,
                    SystemConstants.AUTHENTICATION_INSTALLER_KEY,
                    SystemConstants.AUTHENTICATION_DEFAULT);
        }

        public AuthorizationManagerInstaller authorizationManagerInstaller(final String requested) {
            return getInstaller(
                    AuthorizationManagerInstaller.class, requested,
                    SystemConstants.AUTHORIZATION_INSTALLER_KEY,
                    SystemConstants.AUTHORIZATION_DEFAULT);
        }

        //endregion

        //region > helpers
        @SuppressWarnings("unchecked")
        private <T extends Installer> T getInstaller(final Class<T> cls, final String implName) {
            Assert.assertNotNull("No name specified", implName);
            for (final Installer installer : installerList) {
                if (cls.isAssignableFrom(installer.getClass()) && installer.getName().equals(implName)) {
                    return (T) installer;
                }
            }
            return (T) getInstaller(implName);
        }

        @SuppressWarnings("unchecked")
        public Installer getInstaller(final String implClassName) {
            try {
                return ObjectExtensions.asT(InstanceUtil.createInstance(implClassName));
            } catch (final UnavailableClassException e) {
                return null;
            }
        }

        private <T extends Installer> T getInstaller(
                final Class<T> requiredType,
                String reqImpl,
                final String key,
                final String defaultImpl) {
            if (reqImpl == null) {
                reqImpl = isisConfiguration.getString(key, defaultImpl);
            }
            if (reqImpl == null) {
                return null;
            }
            final T installer = getInstaller(requiredType, reqImpl);
            if (installer == null) {
                throw new InstanceCreationException(
                        "Failed to load installer; named/class:'" + reqImpl + "' (of type " + requiredType.getName() + ")");
            }
            return installer;
        }

        //endregion

    }
}
