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
package org.apache.isis.core.progmodel.facets.param.decimal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Decimal;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.progmodel.facets.value.bigdecimal.BigDecimalValueFacet;
import org.apache.isis.core.progmodel.facets.value.bigdecimal.BigDecimalValueSemanticsProvider;

public class BigDecimalForParameterDerivedFromDecimalAnnotationFacetFactory extends FacetFactoryAbstract {

    private static final int DEFAULT_LENGTH = BigDecimalValueSemanticsProvider.DEFAULT_LENGTH;
    private static final int DEFAULT_SCALE = BigDecimalValueSemanticsProvider.DEFAULT_SCALE;

    public BigDecimalForParameterDerivedFromDecimalAnnotationFacetFactory() {
        super(FeatureType.PARAMETERS_ONLY);
    }

    @Override
    public void processParams(ProcessParameterContext processParameterContext) {

        final Method method = processParameterContext.getMethod();
        final int paramNum = processParameterContext.getParamNum();
        
        final Annotation[] parameterAnnotations = Annotations.getParameterAnnotations(method)[paramNum];

        for (final Annotation parameterAnnotation : parameterAnnotations) {
            if (parameterAnnotation instanceof Decimal) {
                FacetUtil.addFacet(create((Decimal) parameterAnnotation, processParameterContext.getFacetHolder()));
                return;
            }
        }
    }

    private BigDecimalValueFacet create(final Decimal annotation, final FacetHolder holder) {
        return annotation == null ? null : 
            new BigDecimalFacetForParameterFromDecimalAnnotation(
                    holder, 
                    valueElseDefault(annotation.length(), DEFAULT_LENGTH), 
                    valueElseDefault(annotation.scale(), DEFAULT_SCALE));
    }

    private static Integer valueElseDefault(final int value, final int defaultValue) {
        return value != -1? value: defaultValue;
    }
    
}
