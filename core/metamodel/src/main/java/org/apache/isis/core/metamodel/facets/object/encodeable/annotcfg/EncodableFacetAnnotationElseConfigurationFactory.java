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

package org.apache.isis.core.metamodel.facets.object.encodeable.annotcfg;

import com.google.common.base.Strings;

import org.apache.isis.applib.annotation.Encodable;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncoderDecoderUtil;
import org.apache.isis.core.metamodel.progmodel.DeprecatedMarker;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;

/**
 * @deprecated
 */
@Deprecated
public class EncodableFacetAnnotationElseConfigurationFactory extends FacetFactoryAbstract implements DeprecatedMarker {

    public EncodableFacetAnnotationElseConfigurationFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContaxt) {
        FacetUtil.addFacet(create(processClassContaxt.getCls(), processClassContaxt.getFacetHolder()));
    }

    /**
     * Returns a {@link org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet} implementation.
     */
    private EncodableFacet create(final Class<?> cls, final FacetHolder holder) {

        // create from annotation, if present
        final Encodable annotation = Annotations.getAnnotation(cls, Encodable.class);
        if (annotation != null) {
            final EncodableFacetAnnotation facet = new EncodableFacetAnnotation(cls, holder, servicesInjector);
            if (facet.isValid()) {
                return facet;
            }
        }

        // otherwise, try to create from configuration, if present
        final String encoderDecoderName = EncoderDecoderUtil.encoderDecoderNameFromConfiguration(cls, getConfiguration());
        if (!Strings.isNullOrEmpty(encoderDecoderName)) {
            final EncodableFacetFromConfiguration facet = new EncodableFacetFromConfiguration(encoderDecoderName, holder, servicesInjector);
            if (facet.isValid()) {
                return facet;
            }
        }

        // otherwise, no value semantic
        return null;
    }




}
