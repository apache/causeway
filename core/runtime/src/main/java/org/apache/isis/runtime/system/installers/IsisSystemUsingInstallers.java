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

package org.apache.isis.runtime.system.installers;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import java.util.List;

import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.metamodel.specloader.FacetDecoratorInstaller;
import org.apache.isis.metamodel.specloader.ObjectReflector;
import org.apache.isis.metamodel.specloader.ObjectReflectorInstaller;
import org.apache.isis.runtime.authentication.AuthenticationManager;
import org.apache.isis.runtime.authentication.AuthenticationManagerInstaller;
import org.apache.isis.runtime.authorization.AuthorizationManager;
import org.apache.isis.runtime.authorization.AuthorizationManagerInstaller;
import org.apache.isis.runtime.fixturesinstaller.FixturesInstaller;
import org.apache.isis.runtime.imageloader.TemplateImageLoader;
import org.apache.isis.runtime.imageloader.TemplateImageLoaderInstaller;
import org.apache.isis.runtime.installers.InstallerLookup;
import org.apache.isis.runtime.persistence.PersistenceMechanismInstaller;
import org.apache.isis.runtime.persistence.PersistenceSessionFactory;
import org.apache.isis.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.runtime.persistence.services.ServicesInstaller;
import org.apache.isis.runtime.remoting.ClientConnectionInstaller;
import org.apache.isis.runtime.session.IsisSessionFactory;
import org.apache.isis.runtime.session.IsisSessionFactoryDefault;
import org.apache.isis.runtime.system.DeploymentType;
import org.apache.isis.runtime.system.IsisSystemAbstract;
import org.apache.isis.runtime.system.IsisSystemException;
import org.apache.isis.runtime.system.SystemConstants;
import org.apache.isis.runtime.transaction.facetdecorator.standard.TransactionFacetDecoratorInstaller;
import org.apache.isis.runtime.userprofile.UserProfileLoader;
import org.apache.isis.runtime.userprofile.UserProfileLoaderDefault;
import org.apache.isis.runtime.userprofile.UserProfileStore;
import org.apache.isis.runtime.userprofile.UserProfileStoreInstaller;
import org.apache.log4j.Logger;

public class IsisSystemUsingInstallers extends IsisSystemAbstract {

    public static final Logger LOG = Logger.getLogger(IsisSystemUsingInstallers.class);

    private final InstallerLookup installerLookup;

    private AuthenticationManagerInstaller authenticationInstaller;
    private AuthorizationManagerInstaller authorizationInstaller;
    private ObjectReflectorInstaller reflectorInstaller;
    private ServicesInstaller servicesInstaller;
    private UserProfileStoreInstaller userProfileStoreInstaller;
    private PersistenceMechanismInstaller persistenceMechanismInstaller;
    private FixturesInstaller fixtureInstaller;

    // ///////////////////////////////////////////
    // Constructors
    // ///////////////////////////////////////////

    public IsisSystemUsingInstallers(final DeploymentType deploymentType, final InstallerLookup installerLookup) {
        super(deploymentType);
        ensureThatArg(installerLookup, is(not(nullValue())));
        this.installerLookup = installerLookup;
    }

    // ///////////////////////////////////////////
    // InstallerLookup
    // ///////////////////////////////////////////

    /**
     * As per {@link #IsisSystemUsingInstallers(DeploymentType, InstallerLookup) constructor}.
     */
    public InstallerLookup getInstallerLookup() {
        return installerLookup;
    }

    // ///////////////////////////////////////////
    // Create context hooks
    // ///////////////////////////////////////////

    @Override
    public IsisSessionFactory doCreateSessionFactory(final DeploymentType deploymentType) throws IsisSystemException {
        final PersistenceSessionFactory persistenceSessionFactory = obtainPersistenceSessionFactory(deploymentType);
        final UserProfileLoader userProfileLoader = new UserProfileLoaderDefault(obtainUserProfileStore());
        return createSessionFactory(deploymentType, userProfileLoader, persistenceSessionFactory);
    }

    /**
     * Overloaded version designed to be called by subclasses that need to explicitly specify different persistence
     * mechanisms.
     * 
     * <p>
     * This is <i>not</i> a hook method, rather it is designed to be called <i>from</i> the
     * {@link #doCreateSessionFactory(DeploymentType) hook method}.
     */
    protected final IsisSessionFactory createSessionFactory(final DeploymentType deploymentType,
        final UserProfileLoader userProfileLoader, final PersistenceSessionFactory persistenceSessionFactory)
        throws IsisSystemException {

        final IsisConfiguration configuration = getConfiguration();
        final AuthenticationManager authenticationManager = obtainAuthenticationManager(deploymentType);
        final AuthorizationManager authorizationManager = obtainAuthorizationManager(deploymentType);
        final TemplateImageLoader templateImageLoader = obtainTemplateImageLoader();
        final ObjectReflector reflector = obtainReflector(deploymentType);

        final List<Object> servicesList = obtainServices();

        // bind metamodel to the (runtime) framework
        // REVIEW: misplaced? seems like a side-effect...
        reflector.setRuntimeContext(new RuntimeContextFromSession());

        return new IsisSessionFactoryDefault(deploymentType, configuration, templateImageLoader, reflector,
            authenticationManager, authorizationManager, userProfileLoader, persistenceSessionFactory, servicesList);

    }

    // ///////////////////////////////////////////
    // Configuration
    // ///////////////////////////////////////////

    /**
     * Returns a <i>snapshot</i> of the {@link IsisConfiguration configuration} held by the
     * {@link #getInstallerLookup() installer lookup}.
     * 
     * @see InstallerLookup#getConfiguration()
     */
    @Override
    public IsisConfiguration getConfiguration() {
        return installerLookup.getConfiguration();
    }

    // ///////////////////////////////////////////
    // Authentication & Authorization
    // ///////////////////////////////////////////

    public void lookupAndSetAuthenticatorAndAuthorization(DeploymentType deploymentType) {

        IsisConfiguration configuration = installerLookup.getConfiguration();
        String connection = configuration.getString(SystemConstants.CLIENT_CONNECTION_KEY);

        if (connection != null) {
            lookupAndSetAuthenticatorAndAuthorizationUsingClientConnectionInstaller(connection);
        } else {
            lookupAndSetAuthenticatorAndAuthorizationInstallers(deploymentType);
        }
    }

    private void lookupAndSetAuthenticatorAndAuthorizationUsingClientConnectionInstaller(String connection) {
        ClientConnectionInstaller clientConnectionInstaller = installerLookup.clientConnectionInstaller(connection);
        if (clientConnectionInstaller == null) {
            return;
        }
        setAuthenticationInstaller(clientConnectionInstaller);
        setAuthorizationInstaller(clientConnectionInstaller);
    }

    private void lookupAndSetAuthenticatorAndAuthorizationInstallers(DeploymentType deploymentType) {
        // use the one specified in configuration
        String authenticationManagerKey = getConfiguration().getString(SystemConstants.AUTHENTICATION_INSTALLER_KEY);
        final AuthenticationManagerInstaller authenticationInstaller =
            installerLookup.authenticationManagerInstaller(authenticationManagerKey, deploymentType);
        if (authenticationInstaller != null) {
            setAuthenticationInstaller(authenticationInstaller);
        }

        // use the one specified in configuration
        String authorizationManagerKey = getConfiguration().getString(SystemConstants.AUTHORIZATION_INSTALLER_KEY);
        final AuthorizationManagerInstaller authorizationInstaller =
            installerLookup.authorizationManagerInstaller(authorizationManagerKey, deploymentType);
        if (authorizationInstaller != null) {
            setAuthorizationInstaller(authorizationInstaller);
        }
    }

    /**
     * Set the type of connection to used to access the server.
     * 
     * <p>
     * Note that the {@link IsisSessionFactoryUsingInstallers} also checks the {@link ClientConnectionInstaller} twice
     * over: to see if a <tt>PersistenceSessionProxy</tt> should be used as a persistor, and for any
     * {@link FacetDecoratorInstaller}s.
     */
    public void setAuthenticationInstaller(final AuthenticationManagerInstaller authenticationManagerInstaller) {
        this.authenticationInstaller = authenticationManagerInstaller;
    }

    /**
     * Set the type of connection to used to access the server.
     * 
     * <p>
     * Note that the {@link IsisSessionFactoryUsingInstallers} also checks the {@link ClientConnectionInstaller} twice
     * over: to see if a <tt>PersistenceSessionProxy</tt> should be used as a persistor, and for any
     * {@link FacetDecoratorInstaller}s.
     */
    public void setAuthorizationInstaller(final AuthorizationManagerInstaller authorizationManagerInstaller) {
        this.authorizationInstaller = authorizationManagerInstaller;
    }

    @Override
    protected AuthenticationManager obtainAuthenticationManager(DeploymentType deploymentType) {
        return authenticationInstaller.createAuthenticationManager();
    }

    protected AuthorizationManager obtainAuthorizationManager(DeploymentType deploymentType) {
        return authorizationInstaller.createAuthorizationManager();
    }

    // ///////////////////////////////////////////
    // Fixtures
    // ///////////////////////////////////////////

    public void lookupAndSetFixturesInstaller() {
        IsisConfiguration configuration = installerLookup.getConfiguration();
        String fixture = configuration.getString(SystemConstants.FIXTURES_INSTALLER_KEY);

        final FixturesInstaller fixturesInstaller = installerLookup.fixturesInstaller(fixture);
        if (fixturesInstaller != null) {
            this.fixtureInstaller = fixturesInstaller;
        }
    }

    public void setFixtureInstaller(FixturesInstaller fixtureInstaller) {
        this.fixtureInstaller = fixtureInstaller;
    }

    @Override
    protected FixturesInstaller obtainFixturesInstaller() throws IsisSystemException {
        return fixtureInstaller;
    }

    // ///////////////////////////////////////////
    // Template Image Loader
    // ///////////////////////////////////////////

    /**
     * Uses the {@link TemplateImageLoader} configured in {@link InstallerLookup}, if available, else falls back to that
     * of the superclass.
     */
    @Override
    protected TemplateImageLoader obtainTemplateImageLoader() {
        TemplateImageLoaderInstaller templateImageLoaderInstaller = installerLookup.templateImageLoaderInstaller(null);
        if (templateImageLoaderInstaller != null) {
            return templateImageLoaderInstaller.createLoader();
        } else {
            return super.obtainTemplateImageLoader();
        }
    }

    // ///////////////////////////////////////////
    // Reflector
    // ///////////////////////////////////////////

    public void setReflectorInstaller(final ObjectReflectorInstaller reflectorInstaller) {
        this.reflectorInstaller = reflectorInstaller;
    }

    @Override
    protected ObjectReflector obtainReflector(DeploymentType deploymentType) throws IsisSystemException {
        if (reflectorInstaller == null) {
            String fromCmdLine = getConfiguration().getString(SystemConstants.REFLECTOR_KEY);
            reflectorInstaller = installerLookup.reflectorInstaller(fromCmdLine);
        }
        ensureThatState(reflectorInstaller, is(not(nullValue())),
            "reflector installer has not been injected and could not be looked up");

        // add in transaction support (if already in set then will be ignored)
        reflectorInstaller.addFacetDecoratorInstaller(installerLookup
            .getInstaller(TransactionFacetDecoratorInstaller.class));

        // if there is a client connection installer, then add facet decorator installer also
        String connection = getConfiguration().getString(SystemConstants.CLIENT_CONNECTION_KEY);
        if (connection != null) {
            FacetDecoratorInstaller clientConnectionInstaller = installerLookup.clientConnectionInstaller(connection);
            reflectorInstaller.addFacetDecoratorInstaller(clientConnectionInstaller);
        }

        return reflectorInstaller.createReflector();
    }

    // ///////////////////////////////////////////
    // Services
    // ///////////////////////////////////////////

    public void setServicesInstaller(ServicesInstaller servicesInstaller) {
        this.servicesInstaller = servicesInstaller;
    }

    @Override
    protected List<Object> obtainServices() {
        if (servicesInstaller == null) {
            servicesInstaller = installerLookup.servicesInstaller(null);
        }
        ensureThatState(servicesInstaller, is(not(nullValue())),
            "services installer has not been injected and could not be looked up");

        return servicesInstaller.getServices(getDeploymentType());
    }

    // ///////////////////////////////////////////
    // User Profile Loader/Store
    // ///////////////////////////////////////////

    public void lookupAndSetUserProfileFactoryInstaller() {
        IsisConfiguration configuration = installerLookup.getConfiguration();
        String persistor = configuration.getString(SystemConstants.PROFILE_PERSISTOR_INSTALLER_KEY);

        UserProfileStoreInstaller userProfilePersistenceMechanismInstaller =
            installerLookup.userProfilePersistenceMechanismInstaller(persistor, getDeploymentType());
        if (userProfilePersistenceMechanismInstaller != null) {
            setUserProfileStoreInstaller(userProfilePersistenceMechanismInstaller);
        }
    }

    public void setUserProfileStoreInstaller(UserProfileStoreInstaller userProfilestoreInstaller) {
        this.userProfileStoreInstaller = userProfilestoreInstaller;
    }

    @Override
    protected UserProfileStore obtainUserProfileStore() {
        return userProfileStoreInstaller.createUserProfileStore(getConfiguration());
    }

    // ///////////////////////////////////////////
    // PersistenceSessionFactory
    // ///////////////////////////////////////////

    public void setPersistenceMechanismInstaller(final PersistenceMechanismInstaller persistenceMechanismInstaller) {
        this.persistenceMechanismInstaller = persistenceMechanismInstaller;
    }

    @Override
    protected PersistenceSessionFactory obtainPersistenceSessionFactory(DeploymentType deploymentType)
        throws IsisSystemException {

        // attempt to look up connection (that is, a ProxyPersistor)
        String connection = getConfiguration().getString(SystemConstants.CLIENT_CONNECTION_KEY);
        if (connection != null) {
            persistenceMechanismInstaller = installerLookup.clientConnectionInstaller(connection);
        }

        // if nothing, look for a object store persistor
        if (persistenceMechanismInstaller == null) {
            String persistenceMechanism = getConfiguration().getString(SystemConstants.OBJECT_PERSISTOR_INSTALLER_KEY);
            persistenceMechanismInstaller =
                installerLookup.persistenceMechanismInstaller(persistenceMechanism, deploymentType);
        }

        ensureThatState(persistenceMechanismInstaller, is(not(nullValue())),
            "persistor installer has not been injected and could not be looked up");

        return persistenceMechanismInstaller.createPersistenceSessionFactory(deploymentType);
    }

}
