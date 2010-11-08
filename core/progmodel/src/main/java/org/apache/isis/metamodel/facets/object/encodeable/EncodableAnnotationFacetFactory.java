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


package org.apache.isis.metamodel.facets.object.encodeable;

import org.apache.isis.applib.annotation.Encodable;
import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.metamodel.config.IsisConfiguration;
import org.apache.isis.metamodel.config.IsisConfigurationAware;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.facets.FacetUtil;
import org.apache.isis.metamodel.facets.MethodRemover;
import org.apache.isis.metamodel.java5.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.runtimecontext.RuntimeContextAware;
import org.apache.isis.metamodel.spec.feature.ObjectFeatureType;


public class EncodableAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract implements IsisConfigurationAware, RuntimeContextAware {

    private IsisConfiguration configuration;
	private RuntimeContext runtimeContext;

    public EncodableAnnotationFacetFactory() {
        super(ObjectFeatureType.OBJECTS_ONLY);
    }

    @Override
    public boolean process(final Class<?> cls, final MethodRemover methodRemover, final FacetHolder holder) {
        return FacetUtil.addFacet(create(cls, holder));
    }

    /**
     * Returns a {@link EncodableFacet} implementation.
     */
    private EncodableFacet create(final Class<?> cls, final FacetHolder holder) {

        // create from annotation, if present
        final Encodable annotation = getAnnotation(cls, Encodable.class);
        if (annotation != null) {
            final EncodableFacetAnnotation facet = new EncodableFacetAnnotation(cls, getIsisConfiguration(), holder, getRuntimeContext());
            if (facet.isValid()) {
                return facet;
            }
        }

        // otherwise, try to create from configuration, if present
        final String encoderDecoderName = EncoderDecoderUtil.encoderDecoderNameFromConfiguration(cls,
                getIsisConfiguration());
        if (!StringUtils.isEmpty(encoderDecoderName)) {
            final EncodableFacetFromConfiguration facet = new EncodableFacetFromConfiguration(encoderDecoderName, holder, getRuntimeContext());
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
    public void setIsisConfiguration(final IsisConfiguration configuration) {
        this.configuration = configuration;
    }

    
    private RuntimeContext getRuntimeContext() {
		return runtimeContext;
	}
	public void setRuntimeContext(final RuntimeContext runtimeContext) {
		this.runtimeContext = runtimeContext;
	}

}
