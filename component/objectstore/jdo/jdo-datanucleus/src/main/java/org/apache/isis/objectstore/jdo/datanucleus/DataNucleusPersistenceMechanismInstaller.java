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
package org.apache.isis.objectstore.jdo.datanucleus;

import java.util.Map;

import org.apache.isis.core.commons.components.Installer;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.progmodel.facets.object.ignore.jdo.RemoveJdoEnhancementTypesFacetFactory;
import org.apache.isis.core.progmodel.facets.object.ignore.jdo.RemoveJdoPrefixedMethodsFacetFactory;
import org.apache.isis.core.runtime.bytecode.identity.ObjectFactoryBasic;
import org.apache.isis.core.runtime.installerregistry.installerapi.PersistenceMechanismInstallerAbstract;
import org.apache.isis.core.runtime.persistence.objectstore.ObjectStoreSpi;
import org.apache.isis.core.runtime.persistence.objectstore.algorithm.PersistAlgorithm;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.AdapterManagerSpi;
import org.apache.isis.core.runtime.system.persistence.IdentifierGenerator;
import org.apache.isis.core.runtime.system.persistence.ObjectFactory;
import org.apache.isis.objectstore.jdo.datanucleus.bytecode.DataNucleusTypesClassSubstitutor;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.adaptermanager.DataNucleusPojoRecreator;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.spi.DataNucleusIdentifierGenerator;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.spi.DataNucleusSimplePersistAlgorithm;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.auditable.AuditableAnnotationInJdoApplibFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.auditable.AuditableMarkerInterfaceInJdoApplibFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.datastoreidentity.JdoDatastoreIdentityAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.discriminator.JdoDiscriminatorAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.embeddedonly.JdoEmbeddedOnlyAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.query.JdoQueryAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.prop.primarykey.JdoPrimaryKeyAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.specloader.validator.JdoMetaModelValidator;

/**
 * Configuration files are read in the usual fashion (as per {@link Installer#getConfigurationResources()}, ie will consult all of:
 * <ul>
 * <li><tt>persistor_datanucleus.properties</tt>
 * <li><tt>persistor.properties</tt>
 * <li><tt>isis.properties</tt>
 * </ul>
 * 
 * <p>
 * With respect to configuration, all properties under {@value #ISIS_CONFIG_PREFIX} prefix are passed 
 * through verbatim to the DataNucleus runtime. For example:
 * <table>
 * <tr><th>Isis Property</th><th>DataNucleus Property</th></tr>
 * <tr><td><tt>isis.persistor.datanucleus.impl.datanucleus.foo.Bar</tt></td><td><tt>datanucleus.foo.Bar</tt></td></tr>
 * </table>
 *
 */
public class DataNucleusPersistenceMechanismInstaller extends PersistenceMechanismInstallerAbstract {

    public static final String NAME = "datanucleus";
    
    private static final String ISIS_CONFIG_PREFIX = "isis.persistor.datanucleus.impl";

    private DataNucleusApplicationComponents applicationComponents = null;

    
    public DataNucleusPersistenceMechanismInstaller() {
        super(NAME);
    }

    
    ////////////////////////////////////////////////////////////////////////
    // createObjectStore
    ////////////////////////////////////////////////////////////////////////
    
    @Override
    protected ObjectStoreSpi createObjectStore(IsisConfiguration configuration, ObjectAdapterFactory adapterFactory, AdapterManagerSpi adapterManager) {
        createDataNucleusApplicationComponentsIfRequired(configuration);
        return new DataNucleusObjectStore(adapterFactory, applicationComponents);
    }

    private void createDataNucleusApplicationComponentsIfRequired(IsisConfiguration configuration) {
        if(applicationComponents != null) {
            return;
        }
        
        final IsisConfiguration dataNucleusConfig = configuration.createSubset(ISIS_CONFIG_PREFIX);
        final Map<String, String> props = dataNucleusConfig.asMap();
        addDataNucleusPropertiesIfRequired(props);
        
        applicationComponents = new DataNucleusApplicationComponents(props, getSpecificationLoader().allSpecifications());
    }


    private static void addDataNucleusPropertiesIfRequired(
            final Map<String, String> props) {
        putIfNotPresent(props, "javax.jdo.PersistenceManagerFactoryClass", "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");

        putIfNotPresent(props, "datanucleus.autoCreateSchema", "true");
        putIfNotPresent(props, "datanucleus.validateSchema", "true");
        putIfNotPresent(props, "datanucleus.cache.level2.type", "none");

        if(props.containsKey("datanucleus.ConnectionFactoryName")) {
            // JNDI connection properties present; do nothing
            return;
        } else {
            // use JDBC connection properties; put if not present
            putIfNotPresent(props, "javax.jdo.option.ConnectionDriverName", "org.hsqldb.jdbcDriver");
            putIfNotPresent(props, "javax.jdo.option.ConnectionURL", "jdbc:hsqldb:mem:test;hsqldb.sqllog=3");
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

    ////////////////////////////////////////////////////////////////////////
    // PersistenceSessionFactoryDelegate impl
    ////////////////////////////////////////////////////////////////////////

    @Override
    protected PersistAlgorithm createPersistAlgorithm(IsisConfiguration configuration) {
        return new DataNucleusSimplePersistAlgorithm();
    }
    
    @Override
    public IdentifierGenerator createIdentifierGenerator(IsisConfiguration configuration) {
        return new DataNucleusIdentifierGenerator();
    }

    @Override
    public ClassSubstitutor createClassSubstitutor(IsisConfiguration configuration) {
        return new DataNucleusTypesClassSubstitutor();
    }

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel, IsisConfiguration configuration) {
        addJdoFacetFactoriesTo(programmingModel);
        addDataNucleusFacetFactoriesTo(programmingModel);
    }

    private void addJdoFacetFactoriesTo(ProgrammingModel baseProgrammingModel) {
        baseProgrammingModel.addFactory(JdoPersistenceCapableAnnotationFacetFactory.class);
        baseProgrammingModel.addFactory(JdoDatastoreIdentityAnnotationFacetFactory.class);
        baseProgrammingModel.addFactory(JdoEmbeddedOnlyAnnotationFacetFactory.class);

        baseProgrammingModel.addFactory(JdoPrimaryKeyAnnotationFacetFactory.class);
        baseProgrammingModel.addFactory(JdoDiscriminatorAnnotationFacetFactory.class);

        baseProgrammingModel.addFactory(JdoQueryAnnotationFacetFactory.class);
        
        baseProgrammingModel.addFactory(AuditableAnnotationInJdoApplibFacetFactory.class);
        baseProgrammingModel.addFactory(AuditableMarkerInterfaceInJdoApplibFacetFactory.class);
    }

    private void addDataNucleusFacetFactoriesTo(ProgrammingModel baseProgrammingModel) {
        baseProgrammingModel.addFactory(RemoveJdoEnhancementTypesFacetFactory.class);
        baseProgrammingModel.addFactory(RemoveJdoPrefixedMethodsFacetFactory.class);
    }

    @Override
    public void refineMetaModelValidator(MetaModelValidatorComposite metaModelValidator, IsisConfiguration configuration) {
        metaModelValidator.add(new JdoMetaModelValidator());
    }


    @Override
    public ObjectFactory createObjectFactory(IsisConfiguration configuration) {
        return new ObjectFactoryBasic();
    }

    @Override
    public DataNucleusPojoRecreator createPojoRecreator(IsisConfiguration configuration) {
        return new DataNucleusPojoRecreator();
    }



    
    ////////////////////////////////////////////////////////////////////////
    // Dependencies
    ////////////////////////////////////////////////////////////////////////
    
    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }




}
