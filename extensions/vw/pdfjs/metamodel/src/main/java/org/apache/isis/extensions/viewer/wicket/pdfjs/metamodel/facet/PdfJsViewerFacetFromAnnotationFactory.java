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
package org.apache.isis.extensions.viewer.wicket.pdfjs.metamodel.facet;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.extensions.viewer.wicket.pdfjs.applib.annotations.PdfJsViewer;

import lombok.val;


public class PdfJsViewerFacetFromAnnotationFactory
extends FacetFactoryAbstract {

    @Component
    public static class Register implements MetaModelRefiner {

        @Override
        public void refineProgrammingModel(final ProgrammingModel programmingModel) {
            programmingModel.addFactory(
                    ProgrammingModel.FacetProcessingOrder.Z2_AFTER_FINALLY,
                    new PdfJsViewerFacetFromAnnotationFactory(programmingModel.getMetaModelContext()));
        }
    }

    @Inject
    public PdfJsViewerFacetFromAnnotationFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.PROPERTIES_AND_ACTIONS);
    }


    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        val facetHoder = processMethodContext.getFacetHolder();

        val pdfjsViewerIfAny = processMethodContext.synthesizeOnMethod(PdfJsViewer.class);

        pdfjsViewerIfAny.ifPresent(
            pdfjsViewer -> {
                val pdfJsViewerFacet = PdfJsViewerFacetFromAnnotation.create(pdfjsViewer, facetHoder);
                getMetaModelContext().getServiceInjector().injectServicesInto(pdfJsViewerFacet);
                FacetUtil.addFacetIfPresent(pdfJsViewerFacet);
            }
        );
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        super.processParams(processParameterContext);
    }
}
