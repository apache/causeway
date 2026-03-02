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
package org.apache.causeway.core.metamodel.facets.properties.propertylayout;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.annotation.Repainting;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.QualifiedFacet;
import org.apache.causeway.core.metamodel.facets.properties.renderunchanged.UnchangingFacet;
import org.apache.causeway.core.metamodel.facets.properties.renderunchanged.UnchangingFacetAbstract;

import lombok.Getter;
import lombok.experimental.Accessors;

public class UnchangingFacetForPropertyLayoutXml
extends UnchangingFacetAbstract
implements QualifiedFacet {

    public static Optional<UnchangingFacet> create(
            final PropertyLayoutData propertyLayout,
            final FacetHolder holder,
            final Precedence precedence,
            final @Nullable String qualifier) {
        if(propertyLayout == null)
            return Optional.empty();
        final Repainting repainting = propertyLayout.getRepainting();
        if(repainting == null
                || repainting == Repainting.NOT_SPECIFIED)
            return Optional.empty();
        return Optional.of(
            new UnchangingFacetForPropertyLayoutXml(repainting == Repainting.NO_REPAINT, holder, precedence, qualifier));
    }

    @Getter(onMethod_ = @Override) @Accessors(fluent = true, makeFinal = true)
    private final @Nullable String qualifier;

    private UnchangingFacetForPropertyLayoutXml(
            final boolean unchanging,
            final FacetHolder holder,
            final Precedence precedence,
            final @Nullable String qualifier) {
        super(unchanging, holder, precedence);
        this.qualifier = qualifier;
    }

    @Override
    public boolean isObjectTypeSpecific() {
        return true;
    }

}
