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
package org.apache.isis.core.metamodel.facets.properties.bigdecimal.javaxvaldigits;

import java.math.BigDecimal;

import javax.inject.Inject;
import javax.validation.constraints.Digits;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;

import lombok.val;

public class BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotationFactory
extends FacetFactoryAbstract {

    @Inject
    public BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotationFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        if (BigDecimal.class != processMethodContext.getMethod().getReturnType()) {
            return;
        }

        val digitsIfAny = processMethodContext.synthesizeOnMethod(Digits.class);

        addFacetIfPresent(
                MaxTotalDigitsFacetOnPropertyFromJavaxValidationDigitsAnnotation
                .create(digitsIfAny, processMethodContext.getFacetHolder()));

        addFacetIfPresent(
                MaxFractionDigitsFacetOnPropertyFromJavaxValidationDigitsAnnotation
                .create(digitsIfAny, processMethodContext.getFacetHolder()));
    }

}
