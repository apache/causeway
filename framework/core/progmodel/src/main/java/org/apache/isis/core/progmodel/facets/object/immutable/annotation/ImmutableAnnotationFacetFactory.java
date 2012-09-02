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

package org.apache.isis.core.progmodel.facets.object.immutable.annotation;

import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class ImmutableAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public ImmutableAnnotationFacetFactory() {
        super(FeatureType.EVERYTHING_BUT_PARAMETERS);
    }

    @Override
    public void process(final ProcessClassContext processClassContaxt) {
        final Immutable annotation = getAnnotation(processClassContaxt.getCls(), Immutable.class);
        FacetUtil.addFacet(create(annotation, processClassContaxt.getFacetHolder()));
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final FacetedMethod member = processMethodContext.getFacetHolder();
        final Class<?> owningClass = processMethodContext.getCls();
        final ObjectSpecification owningSpec = getSpecificationLookup().loadSpecification(owningClass);
        final ImmutableFacet facet = owningSpec.getFacet(ImmutableFacet.class);
        if (facet != null) {
            facet.copyOnto(member);
        }
    }

    private ImmutableFacet create(final Immutable annotation, final FacetHolder holder) {
        return annotation == null ? null : new ImmutableFacetAnnotation(annotation.value(), holder);
    }

}
