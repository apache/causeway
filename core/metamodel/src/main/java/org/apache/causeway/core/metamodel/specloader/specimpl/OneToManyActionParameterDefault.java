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
package org.apache.causeway.core.metamodel.specloader.specimpl;

import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.TypeOfAnyCardinality;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyActionParameter;
import org.apache.causeway.core.metamodel.util.Facets;

import lombok.Getter;

public class OneToManyActionParameterDefault
extends ObjectActionParameterAbstract
implements OneToManyActionParameter {

    public OneToManyActionParameterDefault(
            final ObjectSpecification paramElementType,
            final int index,
            final ObjectActionDefault actionImpl) {
        super(FeatureType.ACTION_PARAMETER_PLURAL, index, paramElementType, actionImpl);
    }

    // -- UNDERLYING TYPE

    @Getter(onMethod_={@Override}, lazy = true)
    private final TypeOfAnyCardinality typeOfAnyCardinality = resolveTypeOfAnyCardinality();
    private TypeOfAnyCardinality resolveTypeOfAnyCardinality() {
        return Facets.typeOfAnyCardinality(getFacetHolder())
                .orElseThrow(()->_Exceptions.unrecoverable(
                        "framework bug: non-scalar feature must have a TypeOfFacet"));
    }


}
