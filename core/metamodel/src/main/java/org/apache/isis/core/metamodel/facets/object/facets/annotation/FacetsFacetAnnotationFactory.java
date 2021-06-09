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

package org.apache.isis.core.metamodel.facets.object.facets.annotation;

import org.apache.isis.applib.annotation.Facets;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.facets.FacetsFacet;

public class FacetsFacetAnnotationFactory extends FacetFactoryAbstract {

    public FacetsFacetAnnotationFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContaxt) {
        final Facets annotation = Annotations.getAnnotation(processClassContaxt.getCls(), Facets.class);
        FacetUtil.addFacetIfPresent(create(annotation, processClassContaxt.getFacetHolder()));
    }

    /**
     * Returns a {@link FacetsFacet} impl provided that at least one valid
     * {@link FacetsFacet#facetFactories() factory} was specified.
     */
    private FacetsFacet create(final Facets annotation, final FacetHolder holder) {
        if (annotation == null) {
            return null;
        }
        final FacetsFacetAnnotation facetsFacetAnnotation = new FacetsFacetAnnotation(annotation, holder);
        return facetsFacetAnnotation.facetFactories().length > 0 ? facetsFacetAnnotation : null;
    }

}
