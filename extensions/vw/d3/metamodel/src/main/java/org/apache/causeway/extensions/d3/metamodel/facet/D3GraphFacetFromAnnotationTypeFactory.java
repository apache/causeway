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
package org.apache.causeway.extensions.d3.metamodel.facet;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.extensions.d3.applib.annotations.D3Graph;

import jakarta.inject.Inject;

/**
 * Processes a {@link D3Graph} annotation placed on a domain <b>type</b> (class),
 * attaching the {@link D3GraphFacet} to the type's facet holder
 * ({@link org.apache.causeway.core.metamodel.spec.ObjectSpecification}).
 * <p>
 * In contrast to {@link D3GraphFacetFromAnnotationFactory} (which handles
 * {@code @D3Graph} on properties/actions), this factory enables a whole domain
 * object to be rendered as a D3 graph. The graph data is then resolved through a
 * {@link org.apache.causeway.extensions.d3.applib.spi.D3GraphAdvisor}, as there is
 * no annotated property to read the data from.
 * </p>
 */
public class D3GraphFacetFromAnnotationTypeFactory
extends FacetFactoryAbstract {

    @Inject
    public D3GraphFacetFromAnnotationTypeFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        processClassContext.synthesizeOnType(D3Graph.class)
            .ifPresent(
                d3Graph -> getServiceInjector().injectServicesInto(
                    D3GraphFacetFromAnnotation
                        .create(d3Graph, processClassContext.facetHolder()))
            );
    }

}
