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
package org.apache.causeway.core.metamodel.facets.object.projection.ident;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.jspecify.annotations.NonNull;

import org.springframework.util.ClassUtils;

import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.icon.IconFacet;
import org.apache.causeway.core.metamodel.facets.object.projection.ProjectionFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;

public record IconFacetFromProjectionFacet(
    ProjectionFacet projectionFacet,
    FacetHolder facetHolder)
implements IconFacet {

    @Override public FacetHolder getFacetHolder() { return facetHolder; }
    @Override public Class<? extends Facet> facetType() { return IconFacet.class; }
    @Override public Precedence getPrecedence() { return Precedence.DEFAULT; }

    @Override
    public Optional<ObjectSupport.IconResource> icon(final ManagedObject targetAdapter, final ObjectSupport.IconWhere iconWhere) {
        var projectedAdapter = projectionFacet.projected(targetAdapter);
        return projectedAdapter.objSpec().getIcon(projectedAdapter, iconWhere);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        visitor.accept("facet", ClassUtils.getShortName(getClass()));
        visitor.accept("precedence", getPrecedence().name());
        visitor.accept("projectionFacet", projectionFacet.getClass().getName());
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {
        return other instanceof IconFacetFromProjectionFacet
            ? this.projectionFacet
                    .semanticEquals(((IconFacetFromProjectionFacet)other).projectionFacet)
            : false;
    }

}
