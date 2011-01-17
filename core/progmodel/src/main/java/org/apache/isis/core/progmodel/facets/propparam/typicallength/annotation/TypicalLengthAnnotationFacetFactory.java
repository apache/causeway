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


package org.apache.isis.core.progmodel.facets.propparam.typicallength.annotation;

import java.lang.annotation.Annotation;

import org.apache.isis.applib.annotation.TypicalLength;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.propparam.typicallength.TypicalLengthFacet;


public class TypicalLengthAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public TypicalLengthAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_PROPERTIES_AND_PARAMETERS);
    }

    @Override
    public void process(ProcessClassContext processClassContaxt) {
        final TypicalLength annotation = getAnnotation(processClassContaxt.getCls(), TypicalLength.class);
        FacetUtil.addFacet(create(annotation, processClassContaxt.getFacetHolder()));
    }

    @Override
    public void process(ProcessMethodContext processMethodContext) {
        final TypicalLength annotation = getAnnotation(processMethodContext.getMethod(), TypicalLength.class);
        final TypicalLengthFacet facet = create(annotation, processMethodContext.getFacetHolder());

        FacetUtil.addFacet(facet);
    }

    @Override
    public void processParams(ProcessParameterContext processParameterContext) {
        final Annotation[] parameterAnnotations = getParameterAnnotations(processParameterContext.getMethod())[processParameterContext.getParamNum()];
        for (int j = 0; j < parameterAnnotations.length; j++) {
            if (parameterAnnotations[j] instanceof TypicalLength) {
                final TypicalLength annotation = (TypicalLength) parameterAnnotations[j];
                FacetUtil.addFacet(create(annotation, processParameterContext.getFacetHolder()));
                return;
            }
        }
    }

    private TypicalLengthFacet create(final TypicalLength annotation, final FacetHolder holder) {
        return annotation != null ? new TypicalLengthFacetAnnotation(annotation.value(), holder) : null;
    }

}
