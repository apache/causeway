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

package org.apache.isis.core.metamodel.facets;

import java.util.Optional;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

import lombok.NonNull;

public interface WhereValueFacet extends Facet {

    public Where where();

    // -- UTILS

    static Optional<Where> where(
            final @NonNull FacetHolder facetHolder,
            final @NonNull Class<? extends WhereValueFacet> facetType) {
        return facetHolder.lookupFacet(facetType)
                .map(WhereValueFacet::where);
    }

    // -- PREDICATES

    static boolean isAlways(final @NonNull FacetHolder facetHolder,
            final @NonNull Class<? extends WhereValueFacet> facetType) {
        return WhereValueFacet.where(facetHolder, facetType)
                .map(Where::isAlways)
                .orElse(false);
    }

}
