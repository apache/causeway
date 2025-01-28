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

import java.util.function.BiConsumer;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

import org.jspecify.annotations.NonNull;

public abstract class WhereValueFacetAbstract
extends FacetAbstract
implements WhereValueFacet {

    private final @NonNull Where where;

    public WhereValueFacetAbstract(
            final @NonNull Class<? extends Facet> facetType,
            final @NonNull FacetHolder holder,
            final @NonNull Where where) {

        super(facetType, holder);
        this.where = where;
    }

    public WhereValueFacetAbstract(
            final @NonNull Class<? extends Facet> facetType,
            final @NonNull FacetHolder holder,
            final @NonNull Where where,
            final Facet.@NonNull Precedence precedence) {

        super(facetType, holder, precedence);
        this.where = where;
    }

    @Override
    public Where where() {
        return where;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("where", where);
    }

}
