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

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.icon.IconFacetAbstract;
import org.apache.causeway.core.metamodel.facets.object.projection.ProjectionFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import org.jspecify.annotations.NonNull;

public class IconFacetFromProjectionFacet
extends IconFacetAbstract {

    private final @NonNull ProjectionFacet projectionFacet;

    public IconFacetFromProjectionFacet(
            final ProjectionFacet projectionFacet,
            final FacetHolder holder) {
        super(holder);
        this.projectionFacet = projectionFacet;
    }

    @Override
    public Optional<String> iconName(final ManagedObject targetAdapter) {
        var projectedAdapter = projectionFacet.projected(targetAdapter);
        return projectedAdapter.objSpec().getIconName(projectedAdapter);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
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
