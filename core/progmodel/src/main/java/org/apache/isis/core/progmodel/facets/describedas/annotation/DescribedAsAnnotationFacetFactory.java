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


package org.apache.isis.core.progmodel.facets.describedas.annotation;

import java.lang.annotation.Annotation;

import org.apache.isis.applib.annotation.DescribedAs;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.naming.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;


public class DescribedAsAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public DescribedAsAnnotationFacetFactory() {
        super(FeatureType.EVERYTHING);
    }

    @Override
    public void process(ProcessClassContext processClassContaxt) {
        final DescribedAs annotation = getAnnotation(processClassContaxt.getCls(), DescribedAs.class);
        FacetUtil.addFacet(create(annotation, processClassContaxt.getFacetHolder()));
    }

    @Override
    public void process(ProcessMethodContext processMethodContext) {

        // look for annotation on the property
        final DescribedAs annotation = getAnnotation(processMethodContext.getMethod(), DescribedAs.class);
        DescribedAsFacet facet = create(annotation, processMethodContext.getFacetHolder());
        if (facet != null) {
            FacetUtil.addFacet(facet);
            return;
        }

        // otherwise, look for annotation on the type
        final Class<?> returnType = processMethodContext.getMethod().getReturnType();
        final DescribedAsFacet returnTypeDescribedAsFacet = getDescribedAsFacet(returnType);
        if (returnTypeDescribedAsFacet != null) {
            facet = new DescribedAsFacetDerivedFromType(returnTypeDescribedAsFacet, processMethodContext.getFacetHolder());
            FacetUtil.addFacet(facet);
        }
    }

    @Override
    public void processParams(ProcessParameterContext processParameterContext) {

        final int paramNum = processParameterContext.getParamNum();
        final Class<?> parameterType = processParameterContext.getMethod().getParameterTypes()[paramNum];
        final Annotation[] parameterAnnotations = getParameterAnnotations(processParameterContext.getMethod())[paramNum];
        for (int j = 0; j < parameterAnnotations.length; j++) {
            if (parameterAnnotations[j] instanceof DescribedAs) {
                FacetUtil.addFacet(create((DescribedAs) parameterAnnotations[j], processParameterContext.getFacetHolder()));
                return;
            }
        }

        // otherwise, fall back to a description on the parameter's type, if available
        final DescribedAsFacet parameterTypeDescribedAsFacet = getDescribedAsFacet(parameterType);
        if (parameterTypeDescribedAsFacet != null) {
            FacetUtil.addFacet(new DescribedAsFacetDerivedFromType(parameterTypeDescribedAsFacet, processParameterContext.getFacetHolder()));
            return;
        }

    }

    private DescribedAsFacet create(final DescribedAs annotation, final FacetHolder holder) {
        return annotation == null ? null : new DescribedAsFacetAnnotation(annotation.value(), holder);
    }

    private DescribedAsFacet getDescribedAsFacet(final Class<?> type) {
        final ObjectSpecification paramTypeSpec = getSpecificationLookup().loadSpecification(type);
        return paramTypeSpec.getFacet(DescribedAsFacet.class);
    }

}
