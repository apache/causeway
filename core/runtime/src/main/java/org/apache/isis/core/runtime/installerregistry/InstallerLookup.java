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

package org.apache.isis.core.runtime.installerregistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.components.Injectable;
import org.apache.isis.core.commons.components.Installer;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderAware;
import org.apache.isis.core.commons.config.NotFoundPolicy;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.factory.InstanceCreationClassException;
import org.apache.isis.core.commons.factory.InstanceCreationException;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.commons.factory.UnavailableClassException;
import org.apache.isis.core.commons.lang.ObjectExtensions;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.metamodel.specloader.ObjectReflectorInstaller;
import org.apache.isis.core.runtime.IsisInstallerRegistry;
import org.apache.isis.core.runtime.about.AboutIsis;
import org.apache.isis.core.runtime.about.ComponentDetails;
import org.apache.isis.core.runtime.authentication.AuthenticationManagerInstaller;
import org.apache.isis.core.runtime.authorization.AuthorizationManagerInstaller;
import org.apache.isis.core.runtime.fixtures.FixturesInstaller;
import org.apache.isis.core.runtime.installerregistry.installerapi.PersistenceMechanismInstaller;
import org.apache.isis.core.runtime.services.ServicesInstaller;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.IsisSystem;
import org.apache.isis.core.runtime.system.SystemConstants;
import org.apache.isis.core.runtime.systemdependencyinjector.SystemDependencyInjector;
import org.apache.isis.core.runtime.systemdependencyinjector.SystemDependencyInjectorAware;

import static org.hamcrest.CoreMatchers.*;

/**
 * The installers correspond more-or-less to the configurable top-level
 * components of {@link IsisSystem}.
 * 
 * <p>
 * The methods of {@link InstallerRepository} may be called without
 * {@link #init() initializing} this class, but other methods may not.
 *
 * <p>
 * This class retrieves named {@link Installer}s from those loaded at creation,
 * updating the {@link IsisConfiguration} as it goes.
 * </p>
 *
 * <p>
 * A list of possible classes are read in from the resource file
 * <tt>installer-registry.properties</tt>. Each installer has a unique name
 * (with respect to its type) that will be compared when one of this classes
 * methods are called. These are instantiated when requested.
 *
 * <p>
 * Note that it <i>is</i> possible to use an {@link Installer} implementation
 * even if it has not been registered in <tt>installer-registry.properties</tt>
 * : just specify the {@link Installer}'s fully qualified class name.
 */
public class InstallerLookup implements InstallerRepository, ApplicationScopedComponent, IsisConfigurationBuilderAware, Injectable, SystemDependencyInjector {

    private static final Logger LOG = LoggerFactory.getLogger(InstallerLookup.class);

    private final List<Installer> installerList = Lists.newArrayList();

    /**
     * A mutable representation of the {@link IsisConfiguration configuration},
     * injected prior to {@link #init()}.
     *
     * <p>
     *
     * @see #setConfigurationBuilder(IsisConfigurationBuilder)
     */
    private IsisConfigurationBuilder isisConfigurationBuilder;

    // ////////////////////////////////////////////////////////
    // Constructor
    // ////////////////////////////////////////////////////////

    public InstallerLookup() {
        loadInstallers();
    }

    private void loadInstallers() {
        final InputStream in = getInstallerRegistryStream(IsisInstallerRegistry.INSTALLER_REGISTRY_FILE);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                final String className = StringExtensions.asFirstWord(line);
                if (className.length() == 0 || className.startsWith("#")) {
                    continue;
                }
                try {
                    final Installer object = (Installer) InstanceUtil.createInstance(className);
                    LOG.debug("created component installer: " + object.getName() + " - " + className);
                    installerList.add(object);
                } catch (final UnavailableClassException e) {
                    LOG.info("component installer not found; it will not be available: " + className);
                } catch (final InstanceCreationClassException e) {
                    LOG.info("instance creation exception: " + e.getMessage());
                } catch (final InstanceCreationException e) {
                    throw e;
                }
            }
        } catch (final IOException e) {
            throw new IsisException(e);
        } finally {
            close(reader);
        }

        final List<ComponentDetails> installerVersionList = new ArrayList<ComponentDetails>();
        for (final Installer installer : installerList) {
            installerVersionList.add(new InstallerVersion(installer));
        }
        AboutIsis.setComponentDetails(installerVersionList);
    }

    // ////////////////////////////////////////////////////////
    // InstallerRepository impl.
    // ////////////////////////////////////////////////////////

    /**
     * This method (and only this method) may be called prior to {@link #init()
     * initialization}.
     */
    @Override
    public Installer[] getInstallers(final Class<?> cls) {
        final List<Installer> list = new ArrayList<Installer>();
        for (final Installer comp : installerList) {
            if (cls.isAssignableFrom(comp.getClass())) {
                list.add(comp);
            }
        }
        return list.toArray(new Installer[list.size()]);
    }

    // ////////////////////////////////////////////////////////
    // init, shutdown
    // ////////////////////////////////////////////////////////

    @Override
    public void init() {
        ensureDependenciesInjected();
    }

    private void ensureDependenciesInjected() {
        Ensure.ensureThatState(isisConfigurationBuilder, is(not(nullValue())));
    }

    @Override
    public void shutdown() {
        // nothing to do.
    }

    // /////////////////////////////////////////////////////////
    // metamodel
    // /////////////////////////////////////////////////////////

    public ObjectReflectorInstaller reflectorInstaller(final String requested) {
        return getInstaller(ObjectReflectorInstaller.class, requested, SystemConstants.REFLECTOR_KEY, SystemConstants.REFLECTOR_DEFAULT);
    }

    // /////////////////////////////////////////////////////////
    // framework
    // /////////////////////////////////////////////////////////

    public AuthenticationManagerInstaller authenticationManagerInstaller(final String requested, final DeploymentType deploymentType) {
        return getInstaller(AuthenticationManagerInstaller.class, requested, SystemConstants.AUTHENTICATION_INSTALLER_KEY, deploymentType.isExploring() ? SystemConstants.AUTHENTICATION_EXPLORATION_DEFAULT : SystemConstants.AUTHENTICATION_DEFAULT);
    }

    public AuthorizationManagerInstaller authorizationManagerInstaller(final String requested, final DeploymentType deploymentType) {
        return getInstaller(AuthorizationManagerInstaller.class, requested, SystemConstants.AUTHORIZATION_INSTALLER_KEY, !deploymentType.isProduction() ? SystemConstants.AUTHORIZATION_NON_PRODUCTION_DEFAULT : SystemConstants.AUTHORIZATION_DEFAULT);
    }

    public FixturesInstaller fixturesInstaller(final String requested) {
        return getInstaller(FixturesInstaller.class, requested, SystemConstants.FIXTURES_INSTALLER_KEY, SystemConstants.FIXTURES_INSTALLER_DEFAULT);
    }

    public PersistenceMechanismInstaller persistenceMechanismInstaller(final String requested, final DeploymentType deploymentType) {
        final String persistorDefault = deploymentType.isExploring() || deploymentType.isPrototyping() ? SystemConstants.OBJECT_PERSISTOR_NON_PRODUCTION_DEFAULT : SystemConstants.OBJECT_PERSISTOR_PRODUCTION_DEFAULT;
        return getInstaller(PersistenceMechanismInstaller.class, requested, SystemConstants.OBJECT_PERSISTOR_KEY, persistorDefault);
    }

    public ServicesInstaller servicesInstaller(final String requestedImplementationName) {
        return getInstaller(ServicesInstaller.class, requestedImplementationName, SystemConstants.SERVICES_INSTALLER_KEY, SystemConstants.SERVICES_INSTALLER_DEFAULT);
    }

    // /////////////////////////////////////////////////////////
    // framework - generic
    // /////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    public <T extends Installer> T getInstaller(final Class<T> cls, final String implName) {
        Assert.assertNotNull("No name specified", implName);
        for (final Installer installer : installerList) {
            if (cls.isAssignableFrom(installer.getClass()) && installer.getName().equals(implName)) {
                mergeConfigurationFor(installer);
                injectDependenciesInto(installer);
                return (T) installer;
            }
        }
        return (T) getInstaller(implName);
    }

    @SuppressWarnings("unchecked")
    public Installer getInstaller(final String implClassName) {
        try {
            final Installer installer = ObjectExtensions.asT(InstanceUtil.createInstance(implClassName));
            if (installer != null) {
                mergeConfigurationFor(installer);
                injectDependenciesInto(installer);
            }
            return installer;
        } catch (final InstanceCreationException e) {
            throw new InstanceCreationException("Specification error in " + IsisInstallerRegistry.INSTALLER_REGISTRY_FILE, e);
        } catch (final UnavailableClassException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Installer> T getInstaller(final Class<T> installerCls) {
        try {
            final T installer = (T) (InstanceUtil.createInstance(installerCls));
            if (installer != null) {
                mergeConfigurationFor(installer);
                injectDependenciesInto(installer);
            }
            return installer;
        } catch (final InstanceCreationException e) {
            throw new InstanceCreationException("Specification error in " + IsisInstallerRegistry.INSTALLER_REGISTRY_FILE, e);
        } catch (final UnavailableClassException e) {
            return null;
        }
    }

    // ////////////////////////////////////////////////////////
    // Helpers
    // ////////////////////////////////////////////////////////

    private <T extends Installer> T getInstaller(final Class<T> requiredType, String reqImpl, final String key, final String defaultImpl) {
        if (reqImpl == null) {
            reqImpl = getConfiguration().getString(key, defaultImpl);
        }
        if (reqImpl == null) {
            return null;
        }
        final T installer = getInstaller(requiredType, reqImpl);
        if (installer == null) {
            throw new InstanceCreationException("Failed to load installer; named/class:'" + reqImpl + "' (of type " + requiredType.getName() + ")");
        }
        return installer;
    }

    private void close(final BufferedReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (final IOException e) {
                throw new IsisException(e);
            }
        }
    }

    private InputStream getInstallerRegistryStream(final String componentFile) {
        final InputStream in = IsisInstallerRegistry.getPropertiesAsStream();
        if (in == null) {
            throw new IsisException("No resource found: " + componentFile);
        }
        return in;
    }

    // ////////////////////////////////////////////////////////
    // Configuration
    // ////////////////////////////////////////////////////////

    /**
     * Returns a <i>snapshot</i> of the current {@link IsisConfiguration}.
     *
     * <p>
     * The {@link IsisConfiguration} could subsequently be appended to if
     * further {@link Installer}s are loaded.
     */
    public IsisConfiguration getConfiguration() {
        return isisConfigurationBuilder.getConfiguration();
    }

    public void mergeConfigurationFor(final Installer installer) {
        for (final String installerConfigResource : installer.getConfigurationResources()) {
            isisConfigurationBuilder.addConfigurationResource(installerConfigResource, NotFoundPolicy.CONTINUE);
        }
    }

    @Override
    public <T> T injectDependenciesInto(final T candidate) {
        injectInto(candidate);
        return candidate;
    }

    // ////////////////////////////////////////////////////////////////////
    // Injectable
    // ////////////////////////////////////////////////////////////////////

    @Override
    public void injectInto(final Object candidate) {
        if (SystemDependencyInjectorAware.class.isAssignableFrom(candidate.getClass())) {
            final SystemDependencyInjectorAware cast = SystemDependencyInjectorAware.class.cast(candidate);
            cast.setSystemDependencyInjector(this);
        }
        if (InstallerLookupAware.class.isAssignableFrom(candidate.getClass())) {
            final InstallerLookupAware cast = InstallerLookupAware.class.cast(candidate);
            cast.setInstallerLookup(this);
        }
        isisConfigurationBuilder.injectInto(candidate);
    }

    // ////////////////////////////////////////////////////////
    // Dependencies (injected)
    // ////////////////////////////////////////////////////////

    public IsisConfigurationBuilder getConfigurationBuilder() {
        return isisConfigurationBuilder;
    }

    @Override
    @Inject
    public void setConfigurationBuilder(final IsisConfigurationBuilder configurationLoader) {
        this.isisConfigurationBuilder = configurationLoader;
    }

}
