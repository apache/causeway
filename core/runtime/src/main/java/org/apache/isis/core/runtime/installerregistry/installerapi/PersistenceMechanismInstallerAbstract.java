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

package org.apache.isis.core.runtime.installerregistry.installerapi;

import java.util.List;
import org.apache.isis.core.commons.config.InstallerAbstract;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.ObjectFactory;

/**
 * An abstract implementation of {@link PersistenceMechanismInstaller} that will
 * lookup the {@link ObjectAdapterFactory} and {@link ObjectFactory} from the
 * supplied {@link IsisConfiguration}.
 */
public abstract class PersistenceMechanismInstallerAbstract extends InstallerAbstract implements PersistenceMechanismInstaller {

    public PersistenceMechanismInstallerAbstract(final String name) {
        super(PersistenceMechanismInstaller.TYPE, name);
    }

    //////////////////////////////////////////////////////////////////////
    // createPersistenceSessionFactory
    //////////////////////////////////////////////////////////////////////

    //region > createPersistenceSessionFactory 
    
    @Override
    public PersistenceSessionFactory createPersistenceSessionFactory(final DeploymentType deploymentType) {
        return new PersistenceSessionFactory(deploymentType, getConfiguration(), this);
    }

    //endregion

    // ///////////////////////////////////////////
    // Optional hook methods
    // ///////////////////////////////////////////


    /**
     * Hook method to refine the {@link ProgrammingModel}.
     * 
     * <p>
     * By default, just returns the provided {@link ProgrammingModel}.
     */
    @Override
    public void refineProgrammingModel(ProgrammingModel baseProgrammingModel, IsisConfiguration configuration) {
        // no-op
    }
    
    /**
     * Hook method to refine the {@link MetaModelValidator}.
     * 
     * <p>
     * By default, just returns the provided {@link MetaModelValidatorComposite}.
     * 
     * <p>Note that this methods deals in terms of {@link MetaModelValidatorComposite} (rather than plain {@link MetaModelValidator}}s) 
     * in order to allow new {@link MetaModelValidator}s to be easily {@link MetaModelValidatorComposite#add(MetaModelValidator) added}.
     */
    @Override
    public void refineMetaModelValidator(MetaModelValidatorComposite baseMetaModelValidator, IsisConfiguration configuration) {
        // no-op
    }


    // /////////////////////////////////////////////////////
    // Dependencies (from context)
    // /////////////////////////////////////////////////////

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    // /////////////////////////////////////////////////////
    // Guice
    // /////////////////////////////////////////////////////

    @Override
    public List<Class<?>> getTypes() {
        return listOf(PersistenceSessionFactory.class);
    }
}
