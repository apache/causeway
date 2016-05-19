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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.components.Installer;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderAware;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
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
import org.apache.isis.core.runtime.system.IsisSystem;
import org.apache.isis.core.runtime.system.SystemConstants;
import org.apache.isis.core.runtime.systemdependencyinjector.SystemDependencyInjector;
import org.apache.isis.core.runtime.systemdependencyinjector.SystemDependencyInjectorAware;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

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
public class InstallerLookup implements InstallerRepository, ApplicationScopedComponent, SystemDependencyInjector {

    private static final Logger LOG = LoggerFactory.getLogger(InstallerLookup.class);

    private final IsisConfigurationDefault isisConfiguration;

    //region > Constructor

    private final List<Installer> installerList = Lists.newArrayList();

    public InstallerLookup(final IsisConfigurationDefault isisConfiguration) {
        this.isisConfiguration = isisConfiguration;

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

    //endregion

    //region > Configuration
    public IsisConfigurationDefault getConfiguration() {
        return isisConfiguration;
    }
    //endregion

    //region > InstallerRepository impl.

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
    //endregion


    //region > metamodel
    public ObjectReflectorInstaller reflectorInstaller(final String requested) {
        return getInstaller(ObjectReflectorInstaller.class, requested, SystemConstants.REFLECTOR_KEY, SystemConstants.REFLECTOR_DEFAULT);
    }
    //endregion

    //region > framework

    public AuthenticationManagerInstaller authenticationManagerInstaller(final String requested) {
        return getInstaller(
                AuthenticationManagerInstaller.class, requested,
                SystemConstants.AUTHENTICATION_INSTALLER_KEY,
                SystemConstants.AUTHENTICATION_DEFAULT);
    }

    public AuthorizationManagerInstaller authorizationManagerInstaller(final String requested) {
        return getInstaller(
                AuthorizationManagerInstaller.class, requested,
                SystemConstants.AUTHORIZATION_INSTALLER_KEY,
                SystemConstants.AUTHORIZATION_DEFAULT);
    }

    public FixturesInstaller fixturesInstaller(final String requested) {
        return getInstaller(
                FixturesInstaller.class, requested,
                SystemConstants.FIXTURES_INSTALLER_KEY,
                SystemConstants.FIXTURES_INSTALLER_DEFAULT);
    }

    public PersistenceMechanismInstaller persistenceMechanismInstaller(final String requested) {
        return getInstaller(
                PersistenceMechanismInstaller.class, requested,
                SystemConstants.OBJECT_PERSISTOR_KEY,
                SystemConstants.OBJECT_PERSISTOR_DEFAULT);
    }

    public ServicesInstaller servicesInstaller(final String requestedImplementationName) {
        return getInstaller(
                ServicesInstaller.class, requestedImplementationName,
                SystemConstants.SERVICES_INSTALLER_KEY,
                SystemConstants.SERVICES_INSTALLER_DEFAULT);
    }
    //endregion

    //region > framework - generic
    @SuppressWarnings("unchecked")
    public <T extends Installer> T getInstaller(final Class<T> cls, final String implName) {
        Assert.assertNotNull("No name specified", implName);
        for (final Installer installer : installerList) {
            if (cls.isAssignableFrom(installer.getClass()) && installer.getName().equals(implName)) {
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
                injectDependenciesInto(installer);
            }
            return installer;
        } catch (final InstanceCreationException e) {
            throw new InstanceCreationException("Specification error in " + IsisInstallerRegistry.INSTALLER_REGISTRY_FILE, e);
        } catch (final UnavailableClassException e) {
            return null;
        }
    }
    //endregion

    //region > Helpers

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
    //endregion


    //region > SystemDependencyInjector, Injectable
    @Override
    public <T> T injectDependenciesInto(final T candidate) {
        injectInto(candidate);
        return candidate;
    }

    public void injectInto(final Object candidate) {
        if (SystemDependencyInjectorAware.class.isAssignableFrom(candidate.getClass())) {
            final SystemDependencyInjectorAware cast = SystemDependencyInjectorAware.class.cast(candidate);
            cast.setSystemDependencyInjector(this);
        }
        if (InstallerLookupAware.class.isAssignableFrom(candidate.getClass())) {
            final InstallerLookupAware cast = InstallerLookupAware.class.cast(candidate);
            cast.setInstallerLookup(this);
        }
    }
    //endregion


}
