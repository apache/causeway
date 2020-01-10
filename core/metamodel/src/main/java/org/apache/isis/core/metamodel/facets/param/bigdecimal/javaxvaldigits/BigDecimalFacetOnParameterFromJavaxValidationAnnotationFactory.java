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

import java.math.BigDecimal;

import javax.validation.constraints.Digits;

import org.apache.isis.core.metamodel.facets.value.bigdecimal.BigDecimalValueFacet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;

public class BigDecimalFacetOnParameterFromJavaxValidationAnnotationFactory extends FacetFactoryAbstract {

    public BigDecimalFacetOnParameterFromJavaxValidationAnnotationFactory() {
        super(FeatureType.PARAMETERS_ONLY);
    }

    @Override
    public void processParams(ProcessParameterContext processParameterContext) {

        if(BigDecimal.class != processParameterContext.getParameterType()) {
            return;
        }

        processParameterContext.synthesizeOnParameter(Digits.class)
        .ifPresent(digits->{
            super.addFacet(create(digits, processParameterContext.getFacetHolder()));
        });
        
    }

    private BigDecimalValueFacet create(final Digits annotation, final FacetHolder holder) {
        final int length = annotation.integer() + annotation.fraction();
        final int scale = annotation.fraction();
        return new BigDecimalFacetOnParameterFromJavaxValidationDigitsAnnotation(holder, length, scale);
    }

}
