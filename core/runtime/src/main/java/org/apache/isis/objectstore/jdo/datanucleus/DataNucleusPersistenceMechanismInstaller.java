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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.components.Installer;
import org.apache.isis.core.commons.config.InstallerAbstract;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.installerregistry.installerapi.PersistenceMechanismInstaller;
import org.apache.isis.core.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;

/**
 * Configuration files are read in the usual fashion (as per {@link Installer#getConfigurationResources()}, ie will consult all of:
 * <ul>
 * <li><tt>persistor_datanucleus.properties</tt>
 * <li><tt>persistor.properties</tt>
 * <li><tt>isis.properties</tt>
 * </ul>
 * 
 * <p>
 * With respect to configuration, all properties under {@value #DATANUCLEUS_CONFIG_PREFIX} prefix are passed
 * through verbatim to the DataNucleus runtime. For example:
 * <table>
 * <tr><th>Isis Property</th><th>DataNucleus Property</th></tr>
 * <tr><td><tt>isis.persistor.datanucleus.impl.datanucleus.foo.Bar</tt></td><td><tt>datanucleus.foo.Bar</tt></td></tr>
 * </table>
 *
 */
public class DataNucleusPersistenceMechanismInstaller extends InstallerAbstract implements PersistenceMechanismInstaller {

    private static final Logger LOG = LoggerFactory.getLogger(DataNucleusPersistenceMechanismInstaller.class);

    public static final String NAME = "datanucleus";

    public static final String CLASS_METADATA_LOADED_LISTENER_KEY = "classMetadataLoadedListener";
    static final String CLASS_METADATA_LOADED_LISTENER_DEFAULT = CreateSchemaObjectFromClassMetadata.class.getName();

    public static final String JDO_OBJECTSTORE_CONFIG_PREFIX = "isis.persistor.datanucleus";  // specific to the JDO objectstore
    public static final String DATANUCLEUS_CONFIG_PREFIX = "isis.persistor.datanucleus.impl"; // reserved for datanucleus' own config props

    public DataNucleusPersistenceMechanismInstaller() {
        super(PersistenceMechanismInstaller.TYPE, NAME);
    }

    //region > createPersistenceSessionFactory
    @Override
    public PersistenceSessionFactory createPersistenceSessionFactory(
            final DeploymentType deploymentType,
            final ServicesInjectorSpi servicesInjector,
            final IsisConfiguration configuration,
            final RuntimeContextFromSession runtimeContext) {

        return new PersistenceSessionFactory(deploymentType, servicesInjector, configuration, runtimeContext);
    }
    //endregion

    //region > dependencies

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    //endregion

    @Override
    public List<Class<?>> getTypes() {
        return listOf(PersistenceSessionFactory.class);
    }

}
