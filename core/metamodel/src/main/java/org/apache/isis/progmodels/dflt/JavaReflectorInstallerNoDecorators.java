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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.InstallerAbstract;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecorator;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.layoutmetadata.LayoutMetadataReader;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.specloader.FacetDecoratorInstaller;
import org.apache.isis.core.metamodel.specloader.ObjectReflectorInstaller;
import org.apache.isis.core.metamodel.specloader.ReflectorConstants;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;

/**
 * An implementation of {@link ObjectReflectorInstaller} without support for {@link FacetDecoratorInstaller}
 * being looked up (since this functionality is only available from <tt>runtimes.dflt</tt>).
 */
public class JavaReflectorInstallerNoDecorators extends InstallerAbstract implements ObjectReflectorInstaller {

    private static final Logger LOG = LoggerFactory.getLogger(JavaReflectorInstallerNoDecorators.class);

    public static final String PROPERTY_BASE = ConfigurationConstants.ROOT;

    protected final LinkedHashSet<FacetDecoratorInstaller> decoratorInstallers = Sets.newLinkedHashSet();

    // /////////////////////////////////////////////////////
    // Constructor
    // /////////////////////////////////////////////////////

    public JavaReflectorInstallerNoDecorators() {
        this("java");
    }

    public JavaReflectorInstallerNoDecorators(final String name) {
        super(ObjectReflectorInstaller.TYPE, name);
        
    }

    // /////////////////////////////////////////////////////
    // createReflector, doCreateReflector
    // /////////////////////////////////////////////////////

    @Override
    public SpecificationLoaderSpi createReflector(final Collection<MetaModelRefiner> metaModelRefiners) {

        final ProgrammingModel programmingModel = createProgrammingModel(getConfiguration());
        final Set<FacetDecorator> facetDecorators = createFacetDecorators(getConfiguration());
        final MetaModelValidator mmv = createMetaModelValidator(getConfiguration());
        final List<LayoutMetadataReader> layoutMetadataReaders = createLayoutMetadataReaders(getConfiguration());

        return JavaReflectorHelper.createObjectReflector(programmingModel, metaModelRefiners, facetDecorators, layoutMetadataReaders, mmv, getConfiguration());
    }


    /**
     * Hook method to allow subclasses to specify a different implementations
     * (that is, sets of {@link ProgrammingModel} .
     * 
     * <p>
     * By default, looks up implementation from provided
     * {@link IsisConfiguration} using
     * {@link ReflectorConstants#PROGRAMMING_MODEL_FACETS_CLASS_NAME}. If not
     * specified, then defaults to
     * {@value ReflectorConstants#PROGRAMMING_MODEL_FACETS_CLASS_NAME_DEFAULT}.
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
        includeFacetFactories(configuration, programmingModel);
        excludeFacetFactories(configuration, programmingModel);
        return programmingModel;
    }

    private ProgrammingModel lookupAndCreateProgrammingModelFacets(final IsisConfiguration configuration) {
        final String progModelFacetsClassName = configuration.getString(ReflectorConstants.PROGRAMMING_MODEL_FACETS_CLASS_NAME, ReflectorConstants.PROGRAMMING_MODEL_FACETS_CLASS_NAME_DEFAULT);
        final ProgrammingModel programmingModel = InstanceUtil.createInstance(progModelFacetsClassName, ProgrammingModel.class);
        return programmingModel;
    }

    /**
     * Factored out of {@link #createProgrammingModel(IsisConfiguration)}
     * so that subclasses that choose to override can still support
     * customization of their {@link ProgrammingModel} in a similar way.
     */
    protected void includeFacetFactories(final IsisConfiguration configuration, final ProgrammingModel programmingModel) {
        final String[] facetFactoriesIncludeClassNames = configuration.getList(ReflectorConstants.FACET_FACTORY_INCLUDE_CLASS_NAME_LIST);
        if (facetFactoriesIncludeClassNames != null) {
            for (final String facetFactoryClassName : facetFactoriesIncludeClassNames) {
                final Class<? extends FacetFactory> facetFactory = InstanceUtil.loadClass(facetFactoryClassName, FacetFactory.class);
                programmingModel.addFactory(facetFactory);
            }
        }
    }

    /**
     * Factored out of {@link #createProgrammingModel(IsisConfiguration)}
     * so that subclasses that choose to override can still support
     * customization of their {@link ProgrammingModel} in a similar way.
     */
    protected void excludeFacetFactories(final IsisConfiguration configuration, final ProgrammingModel programmingModel) {
        final String[] facetFactoriesExcludeClassNames = configuration.getList(ReflectorConstants.FACET_FACTORY_EXCLUDE_CLASS_NAME_LIST);
        for (final String facetFactoryClassName : facetFactoriesExcludeClassNames) {
            final Class<? extends FacetFactory> facetFactory = InstanceUtil.loadClass(facetFactoryClassName, FacetFactory.class);
            programmingModel.removeFactory(facetFactory);
        }
    }

    /**
     * Hook method to allow subclasses to specify a different sets of
     * {@link FacetDecorator}s.
     */
    protected Set<FacetDecorator> createFacetDecorators(final IsisConfiguration configuration) {
        return Collections.emptySet();
    }


    /**
     * Hook method to allow subclasses to specify a different implementation of
     * {@link MetaModelValidator}.
     * 
     * <p>
     * By default, looks up implementation from provided
     * {@link IsisConfiguration} using
     * {@link ReflectorConstants#META_MODEL_VALIDATOR_CLASS_NAME}. If not
     * specified, then defaults to
     * {@value ReflectorConstants#META_MODEL_VALIDATOR_CLASS_NAME_DEFAULT}.
     */
    protected MetaModelValidator createMetaModelValidator(final IsisConfiguration configuration) {
        final String metaModelValidatorClassName = configuration.getString(ReflectorConstants.META_MODEL_VALIDATOR_CLASS_NAME, ReflectorConstants.META_MODEL_VALIDATOR_CLASS_NAME_DEFAULT);
        return InstanceUtil.createInstance(metaModelValidatorClassName, MetaModelValidator.class);
    }

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



    // /////////////////////////////////////////////////////
    // Optionally Injected: DecoratorInstallers
    // /////////////////////////////////////////////////////

    /**
     * Adds in {@link FacetDecoratorInstaller}; if <tt>null</tt> or if already
     * added then request will be silently ignored.
     */
    @Override
    public void addFacetDecoratorInstaller(final FacetDecoratorInstaller decoratorInstaller) {
        if (decoratorInstaller == null) {
            return;
        }
        decoratorInstallers.add(decoratorInstaller);
    }

    // /////////////////////////////////////////////////////
    // Guice
    // /////////////////////////////////////////////////////

    @Override
    public List<Class<?>> getTypes() {
        return listOf(SpecificationLoaderSpi.class);
    }
}
