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
package org.apache.causeway.core.metamodel.facets.collections.layout;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.QualifiedFacet;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.causeway.core.metamodel.facets.members.hidden.HiddenFacetAbstract;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.Getter;
import lombok.experimental.Accessors;

public class HiddenFacetForCollectionLayoutXml
extends HiddenFacetAbstract
implements QualifiedFacet {

    public static Optional<HiddenFacet> create(
            final CollectionLayoutData collectionLayout,
            final FacetHolder holder,
            final Precedence precedence,
            final @Nullable String qualifier) {
        if (collectionLayout == null)
            return Optional.empty();
        final Where where = collectionLayout.getHidden();
        return where != null
                && where != Where.NOT_SPECIFIED
            ? Optional.of(new HiddenFacetForCollectionLayoutXml(where, holder, precedence, qualifier))
            : Optional.empty();
    }

    @Getter(onMethod_ = @Override) @Accessors(fluent = true, makeFinal = true)
    private final @Nullable String qualifier;

    private HiddenFacetForCollectionLayoutXml(
            final Where where,
            final FacetHolder holder,
            final Precedence precedence,
            final @Nullable String qualifier) {
        super(where, holder, precedence);
        this.qualifier = qualifier;
    }

    @Override
    public boolean isObjectTypeSpecific() {
        return true;
    }

    @Override
    public String hiddenReason(final ManagedObject targetAdapter, final Where whereContext) {
        if(!where().includes(whereContext))
            return null;
        return "Hidden on " + where().getFriendlyName();
    }

}
