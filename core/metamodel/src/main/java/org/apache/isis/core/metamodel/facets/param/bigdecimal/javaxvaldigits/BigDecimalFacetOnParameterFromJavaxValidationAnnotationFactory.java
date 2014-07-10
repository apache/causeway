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
package org.apache.isis.core.metamodel.facets.param.bigdecimal.javaxvaldigits;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.math.BigDecimal;

import javax.validation.constraints.Digits;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.value.bigdecimal.BigDecimalValueFacet;

public class BigDecimalFacetOnParameterFromJavaxValidationAnnotationFactory extends FacetFactoryAbstract {

    public BigDecimalFacetOnParameterFromJavaxValidationAnnotationFactory() {
        super(FeatureType.PARAMETERS_ONLY);
    }

    @Override
    public void processParams(ProcessParameterContext processParameterContext) {

        final Method method = processParameterContext.getMethod();
        final int paramNum = processParameterContext.getParamNum();
        
        if(BigDecimal.class != method.getParameterTypes()[paramNum]) {
            return;
        }
        final Annotation[] parameterAnnotations = Annotations.getParameterAnnotations(method)[paramNum];

        for (final Annotation parameterAnnotation : parameterAnnotations) {
            if (parameterAnnotation instanceof Digits) {
                Digits digitsAnnotation = (Digits) parameterAnnotation;
                FacetUtil.addFacet(create(digitsAnnotation, processParameterContext.getFacetHolder()));
                return;
            }
        }
    }

    private BigDecimalValueFacet create(final Digits annotation, final FacetHolder holder) {
        final int length = annotation.integer() + annotation.fraction();
        final int scale = annotation.fraction();
        return annotation == null ? null : 
            new BigDecimalFacetOnParameterFromJavaxValidationDigitsAnnotation(holder, length, scale);
    }
    
}
