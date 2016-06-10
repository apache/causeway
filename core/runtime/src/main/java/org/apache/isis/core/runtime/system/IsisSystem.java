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
import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.applib.fixturescripts.FixtureScripts;
import org.apache.isis.applib.services.fixturespec.FixtureScriptsDefault;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.lang.ListExtensions;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryProvider;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.configinternal.ConfigurationServiceInternal;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelInvalidException;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.services.deplcat.DeploymentCategoryProviderDefault;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.internal.IsisLocaleInitializer;
import org.apache.isis.core.runtime.system.internal.IsisTimeZoneInitializer;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactoryMetamodelRefiner;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.systemusinginstallers.IsisComponentProvider;
import org.apache.isis.core.runtime.systemusinginstallers.IsisComponentProviderDefault2;

public class IsisSystem implements ApplicationScopedComponent {

    public static final Logger LOG = LoggerFactory.getLogger(IsisSystem.class);

    public static final String MSG_ARE_YOU_SURE = "Are you sure?";
    public static final String MSG_CONFIRM = "Confirm";
    public static final String MSG_CANCEL = "Cancel";


    private boolean initialized = false;

    private IsisSessionFactory isisSessionFactory;

    //region > constructors, fields

    private final IsisComponentProvider componentProvider;
    private final DeploymentCategory deploymentCategory;

    private final IsisLocaleInitializer localeInitializer;
    private final IsisTimeZoneInitializer timeZoneInitializer;

    public IsisSystem(final AppManifest manifest) {
        this(new IsisComponentProviderDefault2(manifest, null),
                DeploymentCategory.PRODUCTION);
    }

    public IsisSystem(final IsisComponentProvider componentProvider, final DeploymentCategory deploymentCategory) {

        this.componentProvider = componentProvider;
        this.deploymentCategory = deploymentCategory;

        this.localeInitializer = new IsisLocaleInitializer();
        this.timeZoneInitializer = new IsisTimeZoneInitializer();
    }

    public DeploymentCategory getDeploymentCategory() {
        return deploymentCategory;
    }
    //endregion

    //region > sessionFactory

    /**
     * Populated after {@link #buildSessionFactory()}.
     */
    public IsisSessionFactory getSessionFactory() {
        return isisSessionFactory;
    }

    //endregion

    //region > buildSessionFactory


    public IsisSessionFactory buildSessionFactory() {

        if (initialized) {
            throw new IllegalStateException("Already initialized");
        }
        initialized = true;


        LOG.info("initialising Isis System");
        LOG.info("working directory: " + new File(".").getAbsolutePath());

        final IsisConfigurationDefault configuration = componentProvider.getConfiguration();
        LOG.info("resource stream source: " + configuration.getResourceStreamSource());

        localeInitializer.initLocale(configuration);
        timeZoneInitializer.initTimeZone(configuration);

        // a bit of a workaround, but required if anything in the metamodel (for example, a
        // ValueSemanticsProvider for a date value type) needs to use the Clock singleton
        // we do this after loading the services to allow a service to prime a different clock
        // implementation (eg to use an NTP time service).
        if (!getDeploymentCategory().isProduction() && !Clock.isInitialized()) {
            FixtureClock.initialize();
        }

        try {

            //
            // everything added to ServicesInjector will be able to @Inject'ed
            //

            //
            // the IsisSessionFactory will look up each of these components from the ServicesInjector
            //

            final ServicesInjector servicesInjector = componentProvider.provideServiceInjector(configuration);

            // fixtureScripts, deploymentCategory, configuration
            final DeploymentCategoryProviderDefault deploymentCategoryProvider =
                    new DeploymentCategoryProviderDefault(deploymentCategory);
            servicesInjector.addFallbackIfRequired(DeploymentCategoryProvider.class, deploymentCategoryProvider);
            servicesInjector.addFallbackIfRequired(ConfigurationServiceInternal.class, configuration);

            // fixtureScripts
            servicesInjector.addFallbackIfRequired(FixtureScripts.class, new FixtureScriptsDefault());

            // authentication
            final AuthenticationManager authenticationManager = componentProvider.provideAuthenticationManager();
            servicesInjector.addFallbackIfRequired(AuthenticationManager.class, authenticationManager);

            // authorization
            final AuthorizationManager authorizationManager = componentProvider.provideAuthorizationManager();
            servicesInjector.addFallbackIfRequired(AuthorizationManager.class, authorizationManager);

            // specificationLoader
            final Collection<MetaModelRefiner> metaModelRefiners =
                    refiners(authenticationManager, authorizationManager,
                            new PersistenceSessionFactoryMetamodelRefiner());
            final SpecificationLoader specificationLoader =
                    componentProvider.provideSpecificationLoader(servicesInjector, metaModelRefiners);
            servicesInjector.addFallbackIfRequired(SpecificationLoader.class, specificationLoader);

            // persistenceSessionFactory
            final PersistenceSessionFactory persistenceSessionFactory = new PersistenceSessionFactory(configuration);
            servicesInjector.addFallbackIfRequired(PersistenceSessionFactory.class, persistenceSessionFactory);


            servicesInjector.validateServices();

            // instantiate the IsisSessionFactory
            isisSessionFactory = new IsisSessionFactory(deploymentCategory, servicesInjector);


            //
            // now, add the IsisSessionFactory and its subcomponents into ServicesInjector, so that they can be
            // @Inject'd into any internal domain services
            //

            // isisSessionFactory itself
            servicesInjector.addFallbackIfRequired(IsisSessionFactory.class, isisSessionFactory);

            // subcomponents of isisSessionFactory itself
            servicesInjector.addFallbackIfRequired(OidMarshaller.class, isisSessionFactory.getOidMarshaller());



            //
            // finally, wire up components and components into services...
            //
            for (Object service : servicesInjector.getRegisteredServices()) {
                // inject itself into each service (if implements ServiceInjectorAware).
                servicesInjector.injectInto(service);
            }

            // ... and make IsisSessionFactory available via the IsisContext static for those places where we canont
            // yet inject.
            IsisContext.setSessionFactory(isisSessionFactory);



            //
            // time to initialize...
            //
            try {
                // first, initial metamodel (may throw exception if invalid)
                specificationLoader.init();
                specificationLoader.validateAndAssert();


                //
                // remaining functionality only done if metamodel is valid.
                //
                authenticationManager.init();
                authorizationManager.init();

                persistenceSessionFactory.init();

                isisSessionFactory.constructServices();


            } catch (final MetaModelInvalidException ex) {
                // no need to use a higher level, such as error(...); the calling code will expose any metamodel
                // validation errors in their own particular way.
                if(LOG.isDebugEnabled()) {
                    LOG.debug("Meta model invalid", ex);
                }
                IsisContext.setMetaModelInvalidException(ex);
            }


        } catch (final IsisSystemException ex) {
            LOG.error("failed to initialise", ex);
            throw new RuntimeException(ex);
        }

        return isisSessionFactory;
    }

    private static Collection<MetaModelRefiner> refiners(Object... possibleRefiners ) {
        return ListExtensions.filtered(Arrays.asList(possibleRefiners), MetaModelRefiner.class);
    }

    //endregion

    // region > metaModel validity
    public boolean isMetaModelValid() {
        return IsisContext.getMetaModelInvalidExceptionIfAny() == null;
    }
    //endregion


}
