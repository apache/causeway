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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.core.commons.components.Installer;
import org.apache.isis.core.commons.components.Noop;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.lang.ListExtensions;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.exploration.ExplorationSession;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.fixtures.FixturesInstaller;
import org.apache.isis.core.runtime.installerregistry.InstallerLookup;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.IsisSystemException;
import org.apache.isis.core.runtime.system.IsisSystemFixturesHookAbstract;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.core.runtime.system.internal.IsisLocaleInitializer;
import org.apache.isis.core.runtime.system.internal.IsisTimeZoneInitializer;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.session.IsisSessionFactoryDefault;

/**
 * 
 */
public abstract class IsisSystemAbstract extends IsisSystemFixturesHookAbstract {

    public static final Logger LOG = LoggerFactory.getLogger(IsisSystemAbstract.class);

    private FixturesInstaller fixtureInstaller;

    private LogonFixture logonFixture;

    // ///////////////////////////////////////////
    // Constructors
    // ///////////////////////////////////////////

    public IsisSystemAbstract(final DeploymentType deploymentType) {
        this(deploymentType, new IsisLocaleInitializer(), new IsisTimeZoneInitializer());
    }

    public IsisSystemAbstract(final DeploymentType deploymentType, final IsisLocaleInitializer localeInitializer, final IsisTimeZoneInitializer timeZoneInitializer) {
        super(deploymentType, localeInitializer, timeZoneInitializer);
    }

    @Override
    protected void installFixturesIfRequired() throws IsisSystemException {

        fixtureInstaller = obtainFixturesInstaller();
        if (isNoop(fixtureInstaller)) {
            return;
        }

        IsisContext.openSession(new InitialisationSession());
        fixtureInstaller.installFixtures();
        try {

            // only allow logon fixtures if not in production mode.
            if (!getDeploymentType().isProduction()) {
                logonFixture = fixtureInstaller.getLogonFixture();
            }
        } finally {
            IsisContext.closeSession();
        }
    }

    private boolean isNoop(final FixturesInstaller candidate) {
        return candidate == null || (fixtureInstaller instanceof Noop);
    }

    // ///////////////////////////////////////////
    // Fixtures
    // ///////////////////////////////////////////

    /**
     * This is the only {@link Installer} that is used by any (all) subclass
     * implementations, because it effectively <i>is</i> the component we need
     * (as opposed to a builder/factory of the component we need).
     * 
     * <p>
     * The fact that the component <i>is</i> an installer (and therefore can be
     * {@link InstallerLookup} looked up} is at this level really just an
     * incidental implementation detail useful for the subclass that uses
     * {@link InstallerLookup} to create the other components.
     */
    protected abstract FixturesInstaller obtainFixturesInstaller() throws IsisSystemException;

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
     * Intended to be used when for {@link DeploymentType#SERVER_EXPLORATION
     * exploration} (instead of an {@link ExplorationSession}) or
     * {@link DeploymentType#SERVER_PROTOTYPE prototype} deployments (saves logging
     * in). Should be <i>ignored</i> in other {@link DeploymentType}s.
     */
    @Override
    public LogonFixture getLogonFixture() {
        return logonFixture;
    }

    @Override
    protected void appendFixturesInstallerDebug(final DebugBuilder debug) {
        debug.appendln("Fixture Installer", fixtureInstaller == null ? "none" : fixtureInstaller.getClass().getName());
    }

    

    // ///////////////////////////////////////////
    // Session Factory
    // ///////////////////////////////////////////

    @Override
    public IsisSessionFactory doCreateSessionFactory(final DeploymentType deploymentType) throws IsisSystemException {
        final PersistenceSessionFactory persistenceSessionFactory = obtainPersistenceSessionFactory(deploymentType);
        return createSessionFactory(deploymentType, persistenceSessionFactory);
    }

    /**
     * Overloaded version designed to be called by subclasses that need to
     * explicitly specify different persistence mechanisms.
     * 
     * <p>
     * This is <i>not</i> a hook method, rather it is designed to be called
     * <i>from</i> the {@link #doCreateSessionFactory(DeploymentType) hook
     * method}.
     */
    protected final IsisSessionFactory createSessionFactory(
            final DeploymentType deploymentType,
            final PersistenceSessionFactory persistenceSessionFactory) throws IsisSystemException {

        final IsisConfiguration configuration = getConfiguration();
        final AuthenticationManager authenticationManager = obtainAuthenticationManager(deploymentType);
        final AuthorizationManager authorizationManager = obtainAuthorizationManager(deploymentType);
        final OidMarshaller oidMarshaller = obtainOidMarshaller();
        
        final Collection<MetaModelRefiner> metaModelRefiners = refiners(authenticationManager, authorizationManager, persistenceSessionFactory);
        final SpecificationLoaderSpi reflector = obtainSpecificationLoaderSpi(deploymentType, metaModelRefiners);

        final List<Object> services = obtainServices();

        // bind metamodel to the (runtime) framework
        final RuntimeContextFromSession runtimeContext = obtainRuntimeContextFromSession();
        runtimeContext.injectInto(reflector);

        return newIsisSessionFactory(
                deploymentType,
                persistenceSessionFactory,
                configuration,
                authenticationManager, authorizationManager,
                oidMarshaller,
                reflector,
                services);
    }

    protected RuntimeContextFromSession obtainRuntimeContextFromSession() {
        return new RuntimeContextFromSession();
    }

    protected IsisSessionFactoryDefault newIsisSessionFactory(
            final DeploymentType deploymentType,
            final PersistenceSessionFactory persistenceSessionFactory,
            final IsisConfiguration configuration,
            final AuthenticationManager authenticationManager, final AuthorizationManager authorizationManager,
            final OidMarshaller oidMarshaller,
            final SpecificationLoaderSpi reflector,
            final List<Object> services) {
        return new IsisSessionFactoryDefault(deploymentType, configuration, reflector, authenticationManager, authorizationManager, persistenceSessionFactory, services, oidMarshaller);
    }

    private static Collection<MetaModelRefiner> refiners(Object... possibleRefiners ) {
        return ListExtensions.filtered(Arrays.asList(possibleRefiners), MetaModelRefiner.class);
    }

    
}
