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
package org.apache.causeway.core.metamodel.facets.value.semantics;

import java.util.Optional;

import jakarta.validation.constraints.Digits;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MaxTotalDigitsFacet;

//TODO remove
@Deprecated // this is an inferred facet, no longer required since max integer digits and max fractional digits have their own facets which always need to be satisfied
record MaxTotalDigitsFacetFromJavaxValidationDigitsAnnotation(
        int maxTotalDigits,
        FacetHolder facetHolder)
implements MaxTotalDigitsFacet {

    @Deprecated
    static Optional<MaxTotalDigitsFacet> create(
            final Optional<Digits> digitsIfAny,
            final FacetHolder holder) {
        return digitsIfAny
            .filter(digits->digits.integer()>0 && digits.fraction()>0) //TODO does not apply when float/double !!
            .map(digits->new MaxTotalDigitsFacetFromJavaxValidationDigitsAnnotation(
                    digits.integer() + digits.fraction(), holder));
    }

    @Deprecated
    @Override public Class<? extends Facet> facetType() { return MaxTotalDigitsFacet.class; }
    @Deprecated
    @Override public Precedence precedence() { return Precedence.DEFAULT; }

}
