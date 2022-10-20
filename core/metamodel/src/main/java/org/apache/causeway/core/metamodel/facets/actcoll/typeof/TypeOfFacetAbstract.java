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
package org.apache.causeway.core.metamodel.facets.actcoll.typeof;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.CollectionSemantics;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.TypeOfAnyCardinality;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

public abstract class TypeOfFacetAbstract
extends FacetAbstract
implements TypeOfFacet {

    private static final Class<? extends Facet> type() {
        return TypeOfFacet.class;
    }

    protected TypeOfFacetAbstract(
            final TypeOfAnyCardinality value,
            final FacetHolder holder) {
        this(value, holder, Precedence.DEFAULT);
    }

    protected TypeOfFacetAbstract(
            final TypeOfAnyCardinality type,
            final FacetHolder holder,
            final Precedence precedence) {
        super(type(), holder, precedence);
        this.value = type;
    }

    @Getter(onMethod_={@Override}) @Accessors(fluent = true)
    private final @NonNull TypeOfAnyCardinality value;

    @Override
    public final ObjectSpecification elementSpec() {
        return getSpecificationLoader().specForTypeElseFail(value().getElementType());
    }

    @Override
    public final Optional<CollectionSemantics> getCollectionSemantics() {
        return value().getCollectionSemantics();
    }

    @Override
    public final void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("element-type", value().getElementType());
        getCollectionSemantics()
            .ifPresent(sem->visitor.accept("collection-semantics", sem.name()));
        value().getContainerType()
            .ifPresent(containerType->visitor.accept("container-type", containerType.getName()));
    }

    @Override
    public final boolean semanticEquals(final @NonNull Facet other) {
        return other instanceof TypeOfFacet
                ? this.value() == ((TypeOfFacet)other).value()
                : false;
    }

}
