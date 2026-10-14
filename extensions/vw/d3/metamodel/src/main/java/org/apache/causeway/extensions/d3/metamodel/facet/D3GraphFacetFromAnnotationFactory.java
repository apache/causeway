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
import org.apache.causeway.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailureUtils;
import org.apache.causeway.extensions.d3.applib.annotations.D3Graph;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;

public class D3GraphFacetFromAnnotationFactory
extends FacetFactoryAbstract {

    @Component
    public static class Register implements MetaModelRefiner {

        @Override
        public void refineProgrammingModel(final ProgrammingModel programmingModel) {
            programmingModel.addFactory(
                    ProgrammingModel.FacetProcessingOrder.Z2_AFTER_FINALLY,
                    new D3GraphFacetFromAnnotationFactory(programmingModel.getMetaModelContext()));
        }
    }

    @Inject
    public D3GraphFacetFromAnnotationFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.PROPERTIES_AND_ACTIONS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        var d3GraphIfAny = processMethodContext
                .synthesizeOnMethodOrMixinType(
                    D3Graph.class,
                    () -> ValidationFailureUtils
                            .raiseAmbiguousMixinAnnotations(processMethodContext.facetHolder(), D3Graph.class));

        d3GraphIfAny.ifPresent(
            d3Graph -> {
                getServiceInjector().injectServicesInto(
                    D3GraphFacetFromAnnotation
                        .create(d3Graph, processMethodContext.facetHolder()));
            }
        );
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        super.processParams(processParameterContext);
    }
}

