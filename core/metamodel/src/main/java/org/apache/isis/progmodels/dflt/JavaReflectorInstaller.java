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

package org.apache.isis.progmodels.dflt;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.components.InstallerAbstract;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.layoutmetadata.LayoutMetadataReader;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.ReflectorConstants;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.SpecificationLoaderInstaller;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;

public class JavaReflectorInstaller extends InstallerAbstract implements SpecificationLoaderInstaller {

    //region > constants

    private static final Logger LOG = LoggerFactory.getLogger(JavaReflectorInstaller.class);

    public static final String PROPERTY_BASE = ConfigurationConstants.ROOT;

    //endregion

    //region > constructor

    public JavaReflectorInstaller(final IsisConfigurationDefault isisConfiguration) {
        this("java", isisConfiguration);
    }

    public JavaReflectorInstaller(final String name, final IsisConfigurationDefault isisConfiguration) {
        super(SpecificationLoaderInstaller.TYPE, name, isisConfiguration);

    }
    //endregion

    //region > createReflector, doCreateReflector

    @Override
    public SpecificationLoader createReflector(
            final DeploymentCategory deploymentCategory,
            final Collection<MetaModelRefiner> metaModelRefiners,
            final ServicesInjector servicesInjector) {

        final ProgrammingModel programmingModel = createProgrammingModel(getConfiguration());
        final MetaModelValidator mmv = createMetaModelValidator(getConfiguration());
        final List<LayoutMetadataReader> layoutMetadataReaders = createLayoutMetadataReaders(getConfiguration());

        return JavaReflectorHelper.createObjectReflector(deploymentCategory, getConfiguration(), programmingModel, metaModelRefiners,
                layoutMetadataReaders, mmv,
                servicesInjector);
    }

    //endregion

    //region > createProgrammingModel

    /**
     * Hook method to allow subclasses to specify a different implementations
     * (that is, sets of {@link ProgrammingModel} .
     *
     * <p>
     * By default, looks up implementation from provided
     * {@link IsisConfiguration} using
     * {@link ReflectorConstants#PROGRAMMING_MODEL_FACETS_CLASS_NAME}. If not
     * specified, then defaults to
     * {@link ReflectorConstants#PROGRAMMING_MODEL_FACETS_CLASS_NAME_DEFAULT}.
     *
     * <p>
     * The list of facets can be adjusted using
     * {@link ReflectorConstants#FACET_FACTORY_INCLUDE_CLASS_NAME_LIST} to
     * specify additional {@link FacetFactory factories} to include, and
     * {@link ReflectorConstants#FACET_FACTORY_EXCLUDE_CLASS_NAME_LIST} to
     * exclude.
     */
    protected ProgrammingModel createProgrammingModel(final IsisConfiguration configuration) {
        final ProgrammingModel programmingModel = lookupAndCreateProgrammingModelFacets(configuration);
        includeAndExcludeFacetFactories(configuration, programmingModel);
        return programmingModel;
    }

    private ProgrammingModel lookupAndCreateProgrammingModelFacets(final IsisConfiguration configuration) {
        final String progModelFacetsClassName = configuration.getString(ReflectorConstants.PROGRAMMING_MODEL_FACETS_CLASS_NAME, ReflectorConstants.PROGRAMMING_MODEL_FACETS_CLASS_NAME_DEFAULT);
        final ProgrammingModel programmingModel = InstanceUtil.createInstance(progModelFacetsClassName, ProgrammingModel.class);
        return programmingModel;
    }

    private void includeAndExcludeFacetFactories(final IsisConfiguration configuration, final ProgrammingModel programmingModel) {
        includeFacetFactories(configuration, programmingModel);
        excludeFacetFactories(configuration, programmingModel);
    }

    //endregion

    //region > includeFacetFactories, excludeFacetFactories

    /**
     * Factored out of {@link #createProgrammingModel(IsisConfiguration)}
     * so that subclasses that choose to override can still support
     * customization of their {@link ProgrammingModel} in a similar way.
     */
    protected void includeFacetFactories(final IsisConfiguration configuration, final ProgrammingModel programmingModel) {
        ProgrammingModel.Util.includeFacetFactories(configuration, programmingModel);
    }

    /**
     * Factored out of {@link #createProgrammingModel(IsisConfiguration)}
     * so that subclasses that choose to override can still support
     * customization of their {@link ProgrammingModel} in a similar way.
     */
    protected void excludeFacetFactories(final IsisConfiguration configuration, final ProgrammingModel programmingModel) {
        ProgrammingModel.Util.excludeFacetFactories(configuration, programmingModel);
    }

    //endregion

    //region > createMetaModelValidator

    /**
     * Hook method to allow subclasses to specify a different implementation of
     * {@link MetaModelValidator}.
     *
     * <p>
     * By default, looks up implementation from provided
     * {@link IsisConfiguration} using
     * {@link ReflectorConstants#META_MODEL_VALIDATOR_CLASS_NAME}.
     */
    protected MetaModelValidator createMetaModelValidator(final IsisConfiguration configuration) {
        final String metaModelValidatorClassName = configuration.getString(ReflectorConstants.META_MODEL_VALIDATOR_CLASS_NAME, ReflectorConstants.META_MODEL_VALIDATOR_CLASS_NAME_DEFAULT);
        return InstanceUtil.createInstance(metaModelValidatorClassName, MetaModelValidator.class);
    }

    //endregion

    //region > createLayoutMetadataReaders

    protected List<LayoutMetadataReader> createLayoutMetadataReaders(final IsisConfiguration configuration) {
        final List<LayoutMetadataReader> layoutMetadataReaders = Lists.newArrayList();
        final String[] layoutMetadataReaderClassNames = configuration.getList(ReflectorConstants.LAYOUT_METADATA_READER_LIST, ReflectorConstants.LAYOUT_METADATA_READER_LIST_DEFAULT);
        if (layoutMetadataReaderClassNames != null) {
            for (final String layoutMetadataReaderClassName : layoutMetadataReaderClassNames) {
                final LayoutMetadataReader layoutMetadataReader = InstanceUtil.createInstance(layoutMetadataReaderClassName, LayoutMetadataReader.class);
                layoutMetadataReaders.add(layoutMetadataReader);
            }
        }
        return layoutMetadataReaders;
    }

    //endregion

    //region > getTypes

    @Override
    public List<Class<?>> getTypes() {
        return listOf(SpecificationLoader.class);
    }

    //endregion

}
