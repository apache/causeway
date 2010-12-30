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


package org.apache.isis.core.progmodel.facets.propparam.typicallength;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.TypicalLength;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.propparam.typicallength.TypicalLengthFacet;


public class TypicalLengthAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public TypicalLengthAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_PROPERTIES_AND_PARAMETERS);
    }

    @Override
    public boolean process(final Class<?> cls, final MethodRemover methodRemover, final FacetHolder holder) {
        final TypicalLength annotation = getAnnotation(cls, TypicalLength.class);
        return FacetUtil.addFacet(create(annotation, holder));
    }

    @Override
    public boolean process(Class<?> cls, final Method method, final MethodRemover methodRemover, final FacetHolder holder) {
        final TypicalLength annotation = getAnnotation(method, TypicalLength.class);
        final TypicalLengthFacet facet = create(annotation, holder);

        return FacetUtil.addFacet(facet);
    }

    @Override
    public boolean processParams(final Method method, final int paramNum, final FacetHolder holder) {
        final Annotation[] parameterAnnotations = getParameterAnnotations(method)[paramNum];
        for (int j = 0; j < parameterAnnotations.length; j++) {
            if (parameterAnnotations[j] instanceof TypicalLength) {
                final TypicalLength annotation = (TypicalLength) parameterAnnotations[j];
                return FacetUtil.addFacet(create(annotation, holder));
            }
        }
        return false;
    }

    private TypicalLengthFacet create(final TypicalLength annotation, final FacetHolder holder) {
        return annotation != null ? new TypicalLengthFacetAnnotation(annotation.value(), holder) : null;
    }

}
