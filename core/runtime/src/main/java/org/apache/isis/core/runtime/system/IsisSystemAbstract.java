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
import java.util.List;

import org.apache.log4j.Logger;

import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.core.commons.components.Installer;
import org.apache.isis.core.commons.components.Noop;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.metamodel.specloader.ObjectReflector;
import org.apache.isis.core.runtime.about.AboutIsis;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.standard.exploration.ExplorationSession;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.fixturesinstaller.FixturesInstaller;
import org.apache.isis.core.runtime.imageloader.TemplateImageLoader;
import org.apache.isis.core.runtime.imageloader.awt.TemplateImageLoaderAwt;
import org.apache.isis.core.runtime.installers.InstallerLookup;
import org.apache.isis.core.runtime.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.session.IsisSession;
import org.apache.isis.core.runtime.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.core.runtime.system.internal.IsisLocaleInitializer;
import org.apache.isis.core.runtime.system.internal.IsisTimeZoneInitializer;
import org.apache.isis.core.runtime.system.internal.SplashWindow;
import org.apache.isis.core.runtime.userprofile.UserProfileStore;

/**
 * 
 */
public abstract class IsisSystemAbstract implements IsisSystem {

    public static final Logger LOG = Logger.getLogger(IsisSystemAbstract.class);

    private static final int SPLASH_DELAY_DEFAULT = 6;

    private final IsisLocaleInitializer localeInitializer;
    private final IsisTimeZoneInitializer timeZoneInitializer;
    private final DeploymentType deploymentType;

    private SplashWindow splashWindow;

    private FixturesInstaller fixtureInstaller;

    private boolean initialized = false;

    private IsisSessionFactory sessionFactory;

    private LogonFixture logonFixture;

    // ///////////////////////////////////////////
    // Constructors
    // ///////////////////////////////////////////

    public IsisSystemAbstract(final DeploymentType deploymentType) {
        this(deploymentType, new IsisLocaleInitializer(), new IsisTimeZoneInitializer());
    }

    public IsisSystemAbstract(final DeploymentType deploymentType, final IsisLocaleInitializer localeInitializer,
        final IsisTimeZoneInitializer timeZoneInitializer) {
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

        int splashDelay = SPLASH_DELAY_DEFAULT;
        try {
            TemplateImageLoader splashLoader = obtainTemplateImageLoader();
            splashLoader.init();
            showSplash(splashLoader);

            // temporarily make a configuration available
            // REVIEW: would rather inject this, or perhaps even the ConfigurationBuilder
            IsisContext.setConfiguration(getConfiguration());

            sessionFactory = doCreateSessionFactory(deploymentType);

            initContext(sessionFactory);
            sessionFactory.init();

            installFixturesIfRequired();

        } catch (IsisSystemException ex) {
            LOG.error("failed to initialise", ex);
            splashDelay = 0;
            throw new RuntimeException(ex);
        } finally {
            removeSplash(splashDelay);
        }
    }

    private void installFixturesIfRequired() throws IsisSystemException {
        // some deployment types (eg CLIENT) do not support installing fixtures
        // instead, any fixtures should be installed when server boots up.
        if (!deploymentType.canInstallFixtures()) {
            return;
        }

        fixtureInstaller = obtainFixturesInstaller();
        if (isNoop(fixtureInstaller)) {
            return;
        }

        IsisContext.openSession(new InitialisationSession());
        fixtureInstaller.installFixtures();
        try {

            // only allow logon fixtures if not in production mode.
            if (!deploymentType.isProduction()) {
                logonFixture = fixtureInstaller.getLogonFixture();
            }
        } finally {
            IsisContext.closeSession();
        }
    }

    protected boolean isNoop(FixturesInstaller candidate) {
        return candidate == null || (fixtureInstaller instanceof Noop);
    }

    private void initContext(IsisSessionFactory sessionFactory) {
        getDeploymentType().initContext(sessionFactory);
    }

    @Override
    public void shutdown() {
        LOG.info("shutting down system");
        IsisContext.closeAllSessions();
    }

    // ///////////////////////////////////////////
    // Hook:
    // ///////////////////////////////////////////

    /**
     * Hook method; the returned implementation is expected to use the same general approach as the subclass itself.
     * 
     * <p>
     * So, for example, <tt>IsisSystemUsingInstallers</tt> uses the {@link InstallerLookup} mechanism to find its
     * components. The corresponding <tt>ExecutionContextFactoryUsingInstallers</tt> object returned by this method
     * should use {@link InstallerLookup} likewise.
     */
    protected abstract IsisSessionFactory doCreateSessionFactory(final DeploymentType deploymentType)
        throws IsisSystemException;

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
    // TemplateImageLoader
    // ///////////////////////////////////////////

    /**
     * Just returns a {@link TemplateImageLoaderAwt}; subclasses may override if required.
     */
    protected TemplateImageLoader obtainTemplateImageLoader() {
        return new TemplateImageLoaderAwt(getConfiguration());
    }

    // ///////////////////////////////////////////
    // Reflector
    // ///////////////////////////////////////////

    protected abstract ObjectReflector obtainReflector(DeploymentType deploymentType) throws IsisSystemException;

    // ///////////////////////////////////////////
    // PersistenceSessionFactory
    // ///////////////////////////////////////////

    protected abstract PersistenceSessionFactory obtainPersistenceSessionFactory(DeploymentType deploymentType)
        throws IsisSystemException;

    // ///////////////////////////////////////////
    // Fixtures
    // ///////////////////////////////////////////

    /**
     * This is the only {@link Installer} that is used by any (all) subclass implementations, because it effectively
     * <i>is</i> the component we need (as opposed to a builder/factory of the component we need).
     * 
     * <p>
     * The fact that the component <i>is</i> an installer (and therefore can be {@link InstallerLookup} looked up} is at
     * this level really just an incidental implementation detail useful for the subclass that uses
     * {@link InstallerLookup} to create the other components.
     */
    protected abstract FixturesInstaller obtainFixturesInstaller() throws IsisSystemException;

    // ///////////////////////////////////////////
    // Authentication Manager
    // ///////////////////////////////////////////

    protected abstract AuthenticationManager obtainAuthenticationManager(DeploymentType deploymentType)
        throws IsisSystemException;

    // ///////////////////////////////////////////
    // UserProfileLoader
    // ///////////////////////////////////////////

    protected abstract UserProfileStore obtainUserProfileStore();

    // ///////////////////////////////////////////
    // Services
    // ///////////////////////////////////////////

    protected abstract List<Object> obtainServices();

    // ///////////////////////////////////////////
    // Fixtures Installer
    // ///////////////////////////////////////////

    public FixturesInstaller getFixturesInstaller() {
        return fixtureInstaller;
    }

    /**
     * The {@link LogonFixture}, if any, obtained by running fixtures.
     * 
     * <p>
     * Intended to be used when for {@link DeploymentType#EXPLORATION exploration} (instead of an
     * {@link ExplorationSession}) or {@link DeploymentType#PROTOTYPE prototype} deployments (saves logging in). Should
     * be <i>ignored</i> in other {@link DeploymentType}s.
     */
    @Override
    public LogonFixture getLogonFixture() {
        return logonFixture;
    }

    // ///////////////////////////////////////////
    // Splash
    // ///////////////////////////////////////////

    private void showSplash(TemplateImageLoader imageLoader) {

        boolean vetoSplashFromConfig =
            getConfiguration().getBoolean(SystemConstants.NOSPLASH_KEY, SystemConstants.NOSPLASH_DEFAULT);
        if (!vetoSplashFromConfig && getDeploymentType().shouldShowSplash()) {
            splashWindow = new SplashWindow(imageLoader);
        }
    }

    private void removeSplash(final int delay) {
        if (splashWindow != null) {
            if (delay == 0) {
                splashWindow.removeImmediately();
            } else {
                splashWindow.toFront();
                splashWindow.removeAfterDelay(delay);
            }
        }
    }

    // ///////////////////////////////////////////
    // debugging
    // ///////////////////////////////////////////

    private void debug(final DebugString debug, final Object object) {
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
    public DebuggableWithTitle debugSection(String selectionName) {
        // DebugInfo deb;
        if (selectionName.equals("Configuration")) {
            return getConfiguration();
        } /*
           * else if (selectionName.equals("Overview")) { debugOverview(debug); } else if
           * (selectionName.equals("Authenticator")) { deb = IsisContext.getAuthenticationManager(); } else if
           * (selectionName.equals("Reflector")) { deb = IsisContext.getSpecificationLoader(); } else if
           * (selectionName.equals("Contexts")) { deb = debugListContexts(debug); } else { deb =
           * debugDisplayContext(selectionName, debug); }
           */
        return null;
    }

    private void debugDisplayContext(final String selector, final DebugString debug) {
        final IsisSession d = IsisContext.getSession(selector);
        if (d != null) {
            d.debugAll(debug);
        } else {
            debug.appendln("No context: " + selector);
        }
    }

    private void debugListContexts(final DebugString debug) {
        final String[] contextIds = IsisContext.getInstance().allSessionIds();
        for (int i = 0; i < contextIds.length; i++) {
            debug.appendln(contextIds[i]);
            debug.appendln("-----");
            final IsisSession d = IsisContext.getSession(contextIds[i]);
            d.debug(debug);
            debug.appendln();
        }
    }

    @Override
    public String[] debugSectionNames() {
        final String[] general =
            new String[] { "Overview", "Authenticator", "Configuration", "Reflector", "Requests", "Contexts" };
        final String[] contextIds = IsisContext.getInstance().allSessionIds();
        final String[] combined = new String[general.length + contextIds.length];
        System.arraycopy(general, 0, combined, 0, general.length);
        System.arraycopy(contextIds, 0, combined, general.length, contextIds.length);
        return combined;
    }

    private void debugOverview(final DebugString debug) {
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
            final String system =
                System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") "
                    + System.getProperty("os.version");
            final String java = System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version");
            debug.appendln("user: " + user);
            debug.appendln("os: " + system);
            debug.appendln("java: " + java);
            debug.appendln("working directory: " + new File(".").getAbsolutePath());

            debug.appendTitle("System Installer");
            debug.appendln("Fixture Installer", fixtureInstaller == null ? "none" : fixtureInstaller.getClass()
                .getName());

            debug.appendTitle("System Components");
            debug.appendln("Authentication manager", IsisContext.getAuthenticationManager().getClass().getName());
            debug.appendln("Configuration", getConfiguration().getClass().getName());

            final DebuggableWithTitle[] inf = IsisContext.debugSystem();
            for (int i = 0; i < inf.length; i++) {
                if (inf[i] != null) {
                    inf[i].debugData(debug);
                }
            }
        } catch (RuntimeException e) {
            debug.appendException(e);
        }
    }

}
