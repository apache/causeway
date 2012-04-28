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

package org.apache.isis.core.progmodel.facets.object.encodeable;

import org.apache.isis.applib.annotation.Encodable;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationAware;
import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.adapter.map.AdapterMap;
import org.apache.isis.core.metamodel.adapter.map.AdapterMapAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.runtimecontext.DependencyInjector;
import org.apache.isis.core.metamodel.runtimecontext.DependencyInjectorAware;

public class EncodableAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract implements IsisConfigurationAware, DependencyInjectorAware, AdapterMapAware {

    private IsisConfiguration configuration;

    private AdapterMap adapterManager;
    private DependencyInjector dependencyInjector;

    public EncodableAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContaxt) {
        FacetUtil.addFacet(create(processClassContaxt.getCls(), processClassContaxt.getFacetHolder()));
    }

    /**
     * Returns a {@link EncodableFacet} implementation.
     */
    private EncodableFacet create(final Class<?> cls, final FacetHolder holder) {

        // create from annotation, if present
        final Encodable annotation = getAnnotation(cls, Encodable.class);
        if (annotation != null) {
            final EncodableFacetAnnotation facet = new EncodableFacetAnnotation(cls, getIsisConfiguration(), holder, adapterManager, dependencyInjector);
            if (facet.isValid()) {
                return facet;
            }
        }

        // otherwise, try to create from configuration, if present
        final String encoderDecoderName = EncoderDecoderUtil.encoderDecoderNameFromConfiguration(cls, getIsisConfiguration());
        if (!StringUtils.isNullOrEmpty(encoderDecoderName)) {
            final EncodableFacetFromConfiguration facet = new EncodableFacetFromConfiguration(encoderDecoderName, holder, adapterManager, dependencyInjector);
            if (facet.isValid()) {
                return facet;
            }
        }

        // otherwise, no value semantic
        return null;
    }

    // ////////////////////////////////////////////////////////////////////
    // Injected
    // ////////////////////////////////////////////////////////////////////

    public IsisConfiguration getIsisConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(final IsisConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void setAdapterMap(final AdapterMap adapterManager) {
        this.adapterManager = adapterManager;
    }

    @Override
    public void setDependencyInjector(final DependencyInjector dependencyInjector) {
        this.dependencyInjector = dependencyInjector;
    }

}
