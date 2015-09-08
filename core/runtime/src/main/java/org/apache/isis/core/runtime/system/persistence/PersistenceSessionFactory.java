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

package org.apache.isis.core.runtime.system.persistence;

import java.util.Map;
import java.util.Set;

import org.datanucleus.PropertyNames;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpiAware;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.runtime.persistence.FixturesInstalledFlag;
import org.apache.isis.core.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.objectstore.jdo.datanucleus.DataNucleusApplicationComponents;
import org.apache.isis.objectstore.jdo.datanucleus.DataNucleusObjectStore;
import org.apache.isis.objectstore.jdo.datanucleus.DataNucleusPersistenceMechanismInstaller;
import org.apache.isis.objectstore.jdo.datanucleus.JDOStateManagerForIsis;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.auditable.AuditableAnnotationInJdoApplibFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.auditable.AuditableMarkerInterfaceInJdoApplibFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.datastoreidentity.JdoDatastoreIdentityAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.discriminator.JdoDiscriminatorAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.query.JdoQueryAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.version.JdoVersionAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.prop.column.BigDecimalDerivedFromJdoColumnAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.prop.column.MandatoryFromJdoColumnAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.prop.column.MaxLengthDerivedFromJdoColumnAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.prop.notpersistent.JdoNotPersistentAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.prop.primarykey.JdoPrimaryKeyAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.specloader.validator.JdoMetaModelValidator;
import org.apache.isis.objectstore.jdo.service.RegisterEntities;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

public class PersistenceSessionFactory implements MetaModelRefiner,
        SpecificationLoaderSpiAware, ApplicationScopedComponent, FixturesInstalledFlag {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceSessionFactory.class);

    private final DeploymentType deploymentType;
    private final IsisConfiguration configuration;

    private final ServicesInjectorSpi servicesInjector;
    private final RuntimeContextFromSession runtimeContext;

    private DataNucleusApplicationComponents applicationComponents;
    private Boolean fixturesInstalled;
    private SpecificationLoaderSpi specificationLoader;

    public PersistenceSessionFactory(
            final DeploymentType deploymentType,
            final ServicesInjectorSpi servicesInjector,
            final IsisConfiguration isisConfiguration,
            final RuntimeContextFromSession runtimeContext) {

        ensureThatState(deploymentType, is(notNullValue()));
        ensureThatState(servicesInjector, is(notNullValue()));
        ensureThatState(isisConfiguration, is(not(nullValue())));
        ensureThatState(runtimeContext, is(not(nullValue())));

        this.deploymentType = deploymentType;
        this.configuration = isisConfiguration;
        this.servicesInjector = servicesInjector;
        this.runtimeContext = runtimeContext;
    }

    public DeploymentType getDeploymentType() {
        return deploymentType;
    }


    //region > init, createDataNucleusApplicationComponents

    public final void init() {

        // a bit of a workaround, but required if anything in the metamodel (for
        // example, a
        // ValueSemanticsProvider for a date value type) needs to use the Clock
        // singleton
        // we do this after loading the services to allow a service to prime a
        // different clock
        // implementation (eg to use an NTP time service).
        if (!deploymentType.isProduction() && !Clock.isInitialized()) {
            FixtureClock.initialize();
        }

        runtimeContext.injectInto(servicesInjector);

        // wire up components
        getSpecificationLoader().injectInto(runtimeContext);

        for (Object service : servicesInjector.getRegisteredServices()) {
            runtimeContext.injectInto(service);
        }

        servicesInjector.init();

        this.applicationComponents = createDataNucleusApplicationComponents(configuration);
    }


    private DataNucleusApplicationComponents createDataNucleusApplicationComponents(
            final IsisConfiguration configuration) {

        if (applicationComponents == null || applicationComponents.isStale()) {

            final IsisConfiguration jdoObjectstoreConfig = configuration.createSubset(
                    DataNucleusPersistenceMechanismInstaller. JDO_OBJECTSTORE_CONFIG_PREFIX);

            final IsisConfiguration dataNucleusConfig = configuration.createSubset(DataNucleusPersistenceMechanismInstaller.DATANUCLEUS_CONFIG_PREFIX);
            final Map<String, String> datanucleusProps = dataNucleusConfig.asMap();
            addDataNucleusPropertiesIfRequired(datanucleusProps);

            final RegisterEntities registerEntities = new RegisterEntities(configuration.asMap());
            final Set<String> classesToBePersisted = registerEntities.getEntityTypes();

            applicationComponents = new DataNucleusApplicationComponents(jdoObjectstoreConfig, datanucleusProps, classesToBePersisted);
        }

        return applicationComponents;
    }

    private static void addDataNucleusPropertiesIfRequired(
            final Map<String, String> props) {

        // new feature in DN 3.2.3; enables dependency injection into entities
        putIfNotPresent(props, PropertyNames.PROPERTY_OBJECT_PROVIDER_CLASS_NAME, JDOStateManagerForIsis.class.getName());

        putIfNotPresent(props, "javax.jdo.PersistenceManagerFactoryClass", JDOPersistenceManagerFactory.class.getName());

        // previously we defaulted this property to "true", but that could cause the target database to be modified
        putIfNotPresent(props, PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_SCHEMA, Boolean.FALSE.toString());

        putIfNotPresent(props, PropertyNames.PROPERTY_SCHEMA_VALIDATE_ALL, Boolean.TRUE.toString());
        putIfNotPresent(props, PropertyNames.PROPERTY_CACHE_L2_TYPE, "none");

        putIfNotPresent(props, PropertyNames.PROPERTY_PERSISTENCE_UNIT_LOAD_CLASSES, Boolean.TRUE.toString());

        String connectionFactoryName = props.get(PropertyNames.PROPERTY_CONNECTION_FACTORY_NAME);
        if(connectionFactoryName != null) {
            String connectionFactory2Name = props.get(PropertyNames.PROPERTY_CONNECTION_FACTORY2_NAME);
            String transactionType = props.get("javax.jdo.option.TransactionType");
            if(transactionType == null) {
                LOG.info("found non-JTA JNDI datasource (" + connectionFactoryName + ")");
                if(connectionFactory2Name != null) {
                    LOG.warn("found non-JTA JNDI datasource (" + connectionFactoryName + "); second '-nontx' JNDI datasource configured but will not be used (" + connectionFactory2Name +")");
                }
            } else
                LOG.info("found JTA JNDI datasource (" + connectionFactoryName + ")");
            if(connectionFactory2Name == null) {
                // JDO/DN itself will (probably) throw an exception
                LOG.error("found JTA JNDI datasource (" + connectionFactoryName + ") but second '-nontx' JNDI datasource *not* found");
            } else {
                LOG.info("... and second '-nontx' JNDI datasource found; " + connectionFactory2Name);
            }
            // nothing further to do
            return;
        } else {
            // use JDBC connection properties; put if not present
            LOG.info("did *not* find JNDI datasource; will use JDBC");

            putIfNotPresent(props, "javax.jdo.option.ConnectionDriverName", "org.hsqldb.jdbcDriver");
            putIfNotPresent(props, "javax.jdo.option.ConnectionURL", "jdbc:hsqldb:mem:test");
            putIfNotPresent(props, "javax.jdo.option.ConnectionUserName", "sa");
            putIfNotPresent(props, "javax.jdo.option.ConnectionPassword", "");
        }
    }

    private static void putIfNotPresent(
            final Map<String, String> props,
            String key,
            String value) {
        if(!props.containsKey(key)) {
            props.put(key, value);
        }
    }
    //endregion

    //region > shutdown
    public final void shutdown() {
        // no-op
    }

    //endregion


    public PersistenceSession createPersistenceSession() {
        final DataNucleusObjectStore objectStore = new DataNucleusObjectStore(applicationComponents);
        final PersistenceSession persistenceSession = new PersistenceSession(this, objectStore, getConfiguration());
        return persistenceSession;
    }


    // //////////////////////////////////////////////////////
    // MetaModelAdjuster impl
    // //////////////////////////////////////////////////////


    //region > PersistenceSessionFactoryDelegate impl

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel, IsisConfiguration configuration) {
        programmingModel.addFactory(
                JdoPersistenceCapableAnnotationFacetFactory.class, ProgrammingModel.Position.BEGINNING);
        programmingModel.addFactory(JdoDatastoreIdentityAnnotationFacetFactory.class);

        programmingModel.addFactory(JdoPrimaryKeyAnnotationFacetFactory.class);
        programmingModel.addFactory(JdoNotPersistentAnnotationFacetFactory.class);
        programmingModel.addFactory(JdoDiscriminatorAnnotationFacetFactory.class);
        programmingModel.addFactory(JdoVersionAnnotationFacetFactory.class);

        programmingModel.addFactory(JdoQueryAnnotationFacetFactory.class);

        programmingModel.addFactory(BigDecimalDerivedFromJdoColumnAnnotationFacetFactory.class);
        programmingModel.addFactory(MaxLengthDerivedFromJdoColumnAnnotationFacetFactory.class);
        // must appear after JdoPrimaryKeyAnnotationFacetFactory (above)
        // and also MandatoryFacetOnPropertyMandatoryAnnotationFactory
        // and also PropertyAnnotationFactory
        programmingModel.addFactory(MandatoryFromJdoColumnAnnotationFacetFactory.class);

        programmingModel.addFactory(AuditableAnnotationInJdoApplibFacetFactory.class);
        programmingModel.addFactory(AuditableMarkerInterfaceInJdoApplibFacetFactory.class);
    }

    @Override
    public void refineMetaModelValidator(MetaModelValidatorComposite metaModelValidator, IsisConfiguration configuration) {
        metaModelValidator.add(new JdoMetaModelValidator());
    }

    //endregion


    // //////////////////////////////////////////////////////
    // FixturesInstalledFlag impl
    // //////////////////////////////////////////////////////

    @Override
    public Boolean isFixturesInstalled() {
        return fixturesInstalled;
    }

    @Override
    public void setFixturesInstalled(final Boolean fixturesInstalled) {
        this.fixturesInstalled = fixturesInstalled;
    }

    // //////////////////////////////////////////////////////
    // Dependencies (injected from constructor)
    // //////////////////////////////////////////////////////

    public IsisConfiguration getConfiguration() {
        return configuration;
    }

    public ServicesInjectorSpi getServicesInjector() {
        return servicesInjector;
    }

    // //////////////////////////////////////////////////////
    // Dependencies (from init)
    // //////////////////////////////////////////////////////


    protected SpecificationLoaderSpi getSpecificationLoader() {
        return specificationLoader;
    }

    @Override
    public void setSpecificationLoaderSpi(final SpecificationLoaderSpi specificationLoader) {
        this.specificationLoader = specificationLoader;
    }
}
