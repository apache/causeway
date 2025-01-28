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
package org.apache.causeway.core.metamodel.facets.object.logicaltype;

import java.util.function.BiConsumer;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

import lombok.Getter;
import org.jspecify.annotations.NonNull;

public abstract class AliasedFacetAbstract
extends FacetAbstract
implements AliasedFacet {

    private static final Class<? extends Facet> type() {
        return AliasedFacet.class;
    }

    @Getter(onMethod_ = {@Override})
    private final @NonNull Can<LogicalType> aliases;

    protected AliasedFacetAbstract(
            final Can<LogicalType> aliases,
            final FacetHolder holder) {
        super(AliasedFacetAbstract.type(), holder);
        this.aliases = aliases;
    }

    protected AliasedFacetAbstract(
            final Can<LogicalType> aliases,
            final FacetHolder holder,
            final Facet.Precedence precedence) {
        super(AliasedFacetAbstract.type(), holder, precedence);
        this.aliases = aliases;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("aliases", aliases);
    }
}
