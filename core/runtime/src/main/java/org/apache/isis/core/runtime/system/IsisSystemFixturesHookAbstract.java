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

package org.apache.isis.core.runtime.system;

import java.io.File;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.specloader.ServiceInitializer;
import org.apache.isis.core.runtime.about.AboutIsis;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.exploration.ExplorationSession;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.installerregistry.InstallerLookup;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.core.runtime.system.internal.IsisLocaleInitializer;
import org.apache.isis.core.runtime.system.internal.IsisTimeZoneInitializer;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManagerException;

/**
 * An implementation of {@link IsisSystem} that has a hook for installing
 * fixtures but does not install them itself.
 */
public abstract class IsisSystemFixturesHookAbstract implements IsisSystem {

    public static final Logger LOG = LoggerFactory.getLogger(IsisSystemFixturesHookAbstract.class);

    private final IsisLocaleInitializer localeInitializer;
    private final IsisTimeZoneInitializer timeZoneInitializer;
    private final DeploymentType deploymentType;

    private boolean initialized = false;

    private IsisSessionFactory sessionFactory;

    private ServiceInitializer serviceInitializer;

    // ///////////////////////////////////////////
    // Constructors
    // ///////////////////////////////////////////

    public IsisSystemFixturesHookAbstract(final DeploymentType deploymentType) {
        this(deploymentType, new IsisLocaleInitializer(), new IsisTimeZoneInitializer());
    }

    public IsisSystemFixturesHookAbstract(final DeploymentType deploymentType, final IsisLocaleInitializer localeInitializer, final IsisTimeZoneInitializer timeZoneInitializer) {
        this.deploymentType = deploymentType;
        this.localeInitializer = localeInitializer;
        this.timeZoneInitializer = timeZoneInitializer;
    }

    // ///////////////////////////////////////////
    // DeploymentType
    // ///////////////////////////////////////////

    @Override
    public DeploymentType getDeploymentType() {
        return deploymentType;
    }

    // ///////////////////////////////////////////
    // init, shutdown
    // ///////////////////////////////////////////

    @Override
    public void init() {

        if (initialized) {
            throw new IllegalStateException("Already initialized");
        } else {
            initialized = true;
        }

        LOG.info("initialising Isis System");
        LOG.info("working directory: " + new File(".").getAbsolutePath());
        LOG.info("resource stream source: " + getConfiguration().getResourceStreamSource());

        localeInitializer.initLocale(getConfiguration());
        timeZoneInitializer.initTimeZone(getConfiguration());

        try {
            sessionFactory = doCreateSessionFactory(deploymentType);

            // temporarily make a configuration available
            // REVIEW: would rather inject this, or perhaps even the
            // ConfigurationBuilder
            IsisContext.setConfiguration(getConfiguration());

            initContext(sessionFactory);
            sessionFactory.init();

            serviceInitializer = initializeServices();

            installFixturesIfRequired();

        } catch (final IsisSystemException ex) {
            LOG.error("failed to initialise", ex);
            throw new RuntimeException(ex);
        }
    }

    private void initContext(final IsisSessionFactory sessionFactory) {
        getDeploymentType().initContext(sessionFactory);
    }

    /**
     * @see #shutdownServices(ServiceInitializer)
     */
    private ServiceInitializer initializeServices() {

        final List<Object> services = sessionFactory.getServices();
        
//        // autowire
//        final ServicesInjectorDefault servicesInjector = new ServicesInjectorDefault();
//        servicesInjector.setSpecificationLookup(sessionFactory.getSpecificationLoader());
//        servicesInjector.setServices(services);
//        servicesInjector.init();

        // validate 
        final ServiceInitializer serviceInitializer = new ServiceInitializer();
        serviceInitializer.validate(getConfiguration(), services);

        // call @PostConstruct (in a session)
        IsisContext.openSession(new InitialisationSession());
        try {
            getTransactionManager().startTransaction();
            try {
                serviceInitializer.postConstruct();
                
                return serviceInitializer;
            } catch(RuntimeException ex) {
                getTransactionManager().getTransaction().setAbortCause(new IsisTransactionManagerException(ex));
                return serviceInitializer;
            } finally {
                // will commit or abort
                getTransactionManager().endTransaction();
            }
        } finally {
            IsisContext.closeSession();
        }
    }


    @Override
    public void shutdown() {
        LOG.info("shutting down system");
        
        shutdownServices(this.serviceInitializer);
        
        IsisContext.closeAllSessions();
    }

    /**
     * @see #initializeServices()
     */
    private void shutdownServices(final ServiceInitializer serviceInitializer) {
        
        // call @PostDestroy (in a session)
        IsisContext.openSession(new InitialisationSession());
        try {
            getTransactionManager().startTransaction();
            try {
                serviceInitializer.preDestroy();
                
            } catch(RuntimeException ex) {
                getTransactionManager().getTransaction().setAbortCause(new IsisTransactionManagerException(ex));
            } finally {
                // will commit or abort
                getTransactionManager().endTransaction();
            }
        } finally {
            IsisContext.closeSession();
        }
    }
    
    // ///////////////////////////////////////////
    // Hook:
    // ///////////////////////////////////////////

    /**
     * Hook method; the returned implementation is expected to use the same
     * general approach as the subclass itself.
     * 
     * <p>
     * So, for example, <tt>IsisSystemUsingInstallers</tt> uses the
     * {@link InstallerLookup} mechanism to find its components. The
     * corresponding <tt>ExecutionContextFactoryUsingInstallers</tt> object
     * returned by this method should use {@link InstallerLookup} likewise.
     */
    protected abstract IsisSessionFactory doCreateSessionFactory(final DeploymentType deploymentType) throws IsisSystemException;

    // ///////////////////////////////////////////
    // Configuration
    // ///////////////////////////////////////////

    /**
     * Populated after {@link #init()}.
     */
    @Override
    public IsisSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    // ///////////////////////////////////////////
    // Configuration
    // ///////////////////////////////////////////

    @Override
    public abstract IsisConfiguration getConfiguration();


    // ///////////////////////////////////////////
    // OidMarshaller
    // ///////////////////////////////////////////

    /**
     * Just returns a {@link OidMarshaller}; subclasses may override if
     * required.
     */
    protected OidMarshaller obtainOidMarshaller() {
        return new OidMarshaller();
    }

    // ///////////////////////////////////////////
    // Reflector
    // ///////////////////////////////////////////

    protected abstract SpecificationLoaderSpi obtainSpecificationLoaderSpi(DeploymentType deploymentType, Collection<MetaModelRefiner> metaModelRefiners) throws IsisSystemException;

    // ///////////////////////////////////////////
    // PersistenceSessionFactory
    // ///////////////////////////////////////////

    protected abstract PersistenceSessionFactory obtainPersistenceSessionFactory(DeploymentType deploymentType) throws IsisSystemException;

    // ///////////////////////////////////////////
    // Fixtures (hooks)
    // ///////////////////////////////////////////

    /**
     * Optional hook for appending debug information pertaining to fixtures
     * installer if required.
     */
    protected void appendFixturesInstallerDebug(final DebugBuilder debug) {
    }

    /**
     * The {@link LogonFixture}, if any, obtained by running fixtures.
     * 
     * <p>
     * Intended to be used when for {@link DeploymentType#SERVER_EXPLORATION
     * exploration} (instead of an {@link ExplorationSession}) or
     * {@link DeploymentType#SERVER_PROTOTYPE prototype} deployments (saves logging
     * in). Should be <i>ignored</i> in other {@link DeploymentType}s.
     * 
     * <p>
     * This implementation always returns <tt>null</tt>.
     */
    @Override
    public LogonFixture getLogonFixture() {
        return null;
    }

    /**
     * Optional hook for installing fixtures.
     * 
     * <p>
     * This implementation does nothing.
     */
    protected void installFixturesIfRequired() throws IsisSystemException {
    }

    // ///////////////////////////////////////////
    // Authentication & Authorization Manager
    // ///////////////////////////////////////////

    protected abstract AuthenticationManager obtainAuthenticationManager(DeploymentType deploymentType) throws IsisSystemException;

    protected abstract AuthorizationManager obtainAuthorizationManager(final DeploymentType deploymentType);

    // ///////////////////////////////////////////
    // Services
    // ///////////////////////////////////////////

    protected abstract List<Object> obtainServices();


    // ///////////////////////////////////////////
    // debugging
    // ///////////////////////////////////////////

    private void debug(final DebugBuilder debug, final Object object) {
        if (object instanceof DebuggableWithTitle) {
            final DebuggableWithTitle d = (DebuggableWithTitle) object;
            debug.appendTitle(d.debugTitle());
            d.debugData(debug);
        } else {
            debug.appendln(object.toString());
            debug.appendln("... no further debug information");
        }
    }

    @Override
    public DebuggableWithTitle debugSection(final String selectionName) {
        // DebugInfo deb;
        if (selectionName.equals("Configuration")) {
            return getConfiguration();
        } /*
           * else if (selectionName.equals("Overview")) { debugOverview(debug);
           * } else if (selectionName.equals("Authenticator")) { deb =
           * IsisContext.getAuthenticationManager(); } else if
           * (selectionName.equals("Reflector")) { deb =
           * IsisContext.getSpecificationLoader(); } else if
           * (selectionName.equals("Contexts")) { deb =
           * debugListContexts(debug); } else { deb =
           * debugDisplayContext(selectionName, debug); }
           */
        return null;
    }

    private void debugDisplayContext(final String selector, final DebugBuilder debug) {
        final IsisSession d = IsisContext.getSession(selector);
        if (d != null) {
            d.debugAll(debug);
        } else {
            debug.appendln("No context: " + selector);
        }
    }

    private void debugListContexts(final DebugBuilder debug) {
        final String[] contextIds = IsisContext.getInstance().allSessionIds();
        for (final String contextId : contextIds) {
            debug.appendln(contextId);
            debug.appendln("-----");
            final IsisSession d = IsisContext.getSession(contextId);
            d.debug(debug);
            debug.appendln();
        }
    }

    @Override
    public String[] debugSectionNames() {
        final String[] general = new String[] { "Overview", "Authenticator", "Configuration", "Reflector", "Requests", "Contexts" };
        final String[] contextIds = IsisContext.getInstance().allSessionIds();
        final String[] combined = new String[general.length + contextIds.length];
        System.arraycopy(general, 0, combined, 0, general.length);
        System.arraycopy(contextIds, 0, combined, general.length, contextIds.length);
        return combined;
    }

    private void debugOverview(final DebugBuilder debug) {
        try {
            debug.appendln(AboutIsis.getFrameworkName());
            debug.appendln(AboutIsis.getFrameworkVersion());
            if (AboutIsis.getApplicationName() != null) {
                debug.appendln("application: " + AboutIsis.getApplicationName());
            }
            if (AboutIsis.getApplicationVersion() != null) {
                debug.appendln("version" + AboutIsis.getApplicationVersion());
            }

            final String user = System.getProperty("user.name");
            final String system = System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") " + System.getProperty("os.version");
            final String java = System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version");
            debug.appendln("user: " + user);
            debug.appendln("os: " + system);
            debug.appendln("java: " + java);
            debug.appendln("working directory: " + new File(".").getAbsolutePath());

            debug.appendTitle("System Installer");
            appendFixturesInstallerDebug(debug);

            debug.appendTitle("System Components");
            debug.appendln("Authentication manager", IsisContext.getAuthenticationManager().getClass().getName());
            debug.appendln("Configuration", getConfiguration().getClass().getName());

            final DebuggableWithTitle[] inf = IsisContext.debugSystem();
            for (final DebuggableWithTitle element : inf) {
                if (element != null) {
                    element.debugData(debug);
                }
            }
        } catch (final RuntimeException e) {
            debug.appendException(e);
        }
    }

    IsisTransactionManager getTransactionManager() {
        return IsisContext.getTransactionManager();
    }


}
