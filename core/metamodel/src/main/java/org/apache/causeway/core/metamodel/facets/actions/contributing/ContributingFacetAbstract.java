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
package org.apache.causeway.core.metamodel.facets.actions.contributing;

import java.util.function.BiConsumer;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet.Contributing;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

public abstract class ContributingFacetAbstract
extends FacetAbstract
implements ContributingFacet {

    private static final Class<? extends Facet> type() {
        return ContributingFacet.class;
    }

    // -- FACTORIES

    public static ContributingFacetAbstract createAsAction(final FacetHolder holder) {
        return new ContributingFacetAbstract(Contributing.AS_ACTION, holder) {};
    }

    public static ContributingFacetAbstract createAsProperty(final FacetHolder holder) {
        return new ContributingFacetAbstract(Contributing.AS_PROPERTY, holder) {};
    }

    public static ContributingFacetAbstract createAsCollection(final FacetHolder holder) {
        return new ContributingFacetAbstract(Contributing.AS_COLLECTION, holder) {};
    }

    // -- CONSTRUCTION

    @Getter(onMethod_={@Override}) @Accessors(fluent=true)
    private final @NonNull Contributing contributed;

    private ContributingFacetAbstract(
            final Contributing contributed,
            final FacetHolder holder) {
        super(type(), holder);
        this.contributed = contributed;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("contributing", contributed());
    }

}
