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

package org.apache.isis.core.metamodel.progmodel;

import java.util.List;

import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.configbuilder.IsisConfigurationBuilder;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.specloader.ReflectorConstants;

public interface FacetFactorySet {

    List<FacetFactory> getList();

    enum Position {
        BEGINNING,END
    }
    void addFactory(Class<? extends FacetFactory> facetFactoryClass);
    void addFactory(FacetFactory facetFactory);
    void addFactory(Class<? extends FacetFactory> facetFactoryClass, Position position);
    void addFactory(FacetFactory facetFactory, Position position);

    void removeFactory(Class<? extends FacetFactory> facetFactoryClass);

    /**
     * Key used to lookup comma-separated list of {@link FacetFactory}s to
     * include.
     *
     * @see #FACET_FACTORY_EXCLUDE_CLASS_NAME_LIST
     */
    String FACET_FACTORY_INCLUDE_CLASS_NAME_LIST = ConfigurationConstants.ROOT + "reflector.facets.include";

    /**
     * Key used to lookup comma-separated list of {@link FacetFactory}s to
     * exclude.
     *
     * @see #FACET_FACTORY_INCLUDE_CLASS_NAME_LIST
     */
    String FACET_FACTORY_EXCLUDE_CLASS_NAME_LIST = ConfigurationConstants.ROOT + "reflector.facets.exclude";

    /**
     * This is a bit nasty, but currently the bootstrapping of the metamodel for integration tests vs the webapp differs;
     * the intent of this class is to centralize some logic that should be applied in both cases.
     */
    class Util {
        private Util(){}

        public static void includeFacetFactories(
                final IsisConfigurationBuilder configurationBuilder, 
                final FacetFactorySet programmingModel) {
            
            final String[] facetFactoriesIncludeClassNames = configurationBuilder.peekAtList(ReflectorConstants.FACET_FACTORY_INCLUDE_CLASS_NAME_LIST);
            if (facetFactoriesIncludeClassNames != null) {
                for (final String facetFactoryClassName : facetFactoriesIncludeClassNames) {
                    final Class<? extends FacetFactory> facetFactory = InstanceUtil.loadClass(facetFactoryClassName, FacetFactory.class);
                    programmingModel.addFactory(facetFactory);
                }
            }
        }

        public static void excludeFacetFactories(
                final IsisConfigurationBuilder configurationBuilder, 
                final FacetFactorySet programmingModel) {
            
            final String[] facetFactoriesExcludeClassNames = configurationBuilder.peekAtList(ReflectorConstants.FACET_FACTORY_EXCLUDE_CLASS_NAME_LIST);
            for (final String facetFactoryClassName : facetFactoriesExcludeClassNames) {
                final Class<? extends FacetFactory> facetFactory = InstanceUtil.loadClass(facetFactoryClassName, FacetFactory.class);
                programmingModel.removeFactory(facetFactory);
            }
        }
        
//        public static void includeFacetFactories(final IsisConfiguration configuration, final FacetFactorySet programmingModel) {
//            final String[] facetFactoriesIncludeClassNames = configuration.getList(ReflectorConstants.FACET_FACTORY_INCLUDE_CLASS_NAME_LIST);
//            if (facetFactoriesIncludeClassNames != null) {
//                for (final String facetFactoryClassName : facetFactoriesIncludeClassNames) {
//                    final Class<? extends FacetFactory> facetFactory = InstanceUtil.loadClass(facetFactoryClassName, FacetFactory.class);
//                    programmingModel.addFactory(facetFactory);
//                }
//            }
//        }
//
//        public static void excludeFacetFactories(final IsisConfiguration configuration, final FacetFactorySet programmingModel) {
//            final String[] facetFactoriesExcludeClassNames = configuration.getList(ReflectorConstants.FACET_FACTORY_EXCLUDE_CLASS_NAME_LIST);
//            for (final String facetFactoryClassName : facetFactoriesExcludeClassNames) {
//                final Class<? extends FacetFactory> facetFactory = InstanceUtil.loadClass(facetFactoryClassName, FacetFactory.class);
//                programmingModel.removeFactory(facetFactory);
//            }
//        }
        
    }
}
