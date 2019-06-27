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
package org.apache.isis.metamodel.facets.properties.bigdecimal.javaxvaldigits;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.constraints.Digits;

import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.Annotations;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.metamodel.facets.value.bigdecimal.BigDecimalValueFacet;

public class BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotationFactory extends FacetFactoryAbstract {

    public BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotationFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        if (BigDecimal.class != processMethodContext.getMethod().getReturnType()) {
            return;
        }

        final List<Digits> annotations = Annotations.getAnnotations(processMethodContext.getMethod(), Digits.class);
        final Digits annotation = annotations.isEmpty() ? null : annotations.get(0);
        if (annotation == null) {
            return;
        }
        FacetUtil.addFacet(create(processMethodContext, annotation));
    }

    private BigDecimalValueFacet create(final ProcessMethodContext processMethodContext, final Digits annotation) {
        final FacetedMethod holder = processMethodContext.getFacetHolder();
        final int length = annotation.integer() + annotation.fraction();
        final int scale = annotation.fraction();
        return new BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotation(holder, length, scale);
    }

}
