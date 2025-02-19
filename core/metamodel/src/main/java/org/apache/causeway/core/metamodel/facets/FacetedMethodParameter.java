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
package org.apache.causeway.core.metamodel.facets;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedType;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;

public record FacetedMethodParameter(
    int paramIndex,
    FacetHolder facetHolder,
    FeatureType featureType,
    ResolvedType resolvedType
    ) implements TypedFacetHolder {

    public FacetedMethodParameter(
            final MetaModelContext mmc,
            final FeatureType featureType,
            final Class<?> declaringType,
            final MethodFacade methodFacade,
            final int paramIndex) {
        this(
            paramIndex,
            FacetHolder.simple(mmc, FeatureType.ACTION.identifierFor(
                LogicalType.infer(declaringType),
                methodFacade)),
            featureType,
            methodFacade.resolveParameter(paramIndex));
    }

    /**
     * Returns an instance with {@code resolvedType} replaced by given {@code newType}.
     */
    public FacetedMethodParameter withResolvedType(final ResolvedType newType) {
        return new FacetedMethodParameter(paramIndex, facetHolder, featureType, newType);
    }

    @Override // as used for logging, not strictly required
    public String toString() {
        return resolvedType.toString();
    }

}