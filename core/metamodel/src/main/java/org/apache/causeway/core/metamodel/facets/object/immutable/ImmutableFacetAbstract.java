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
package org.apache.causeway.core.metamodel.facets.object.immutable;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.causeway.core.metamodel.consent.Consent.VetoReason;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.interactions.UsabilityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.NonNull;

public abstract class ImmutableFacetAbstract
extends FacetAbstract
implements ImmutableFacet {

    private static final Class<? extends Facet> type() {
        return ImmutableFacet.class;
    }

    protected final @NonNull VetoReason reason;

    protected ImmutableFacetAbstract(
            final VetoReason reason,
            final FacetHolder holder) {
        this(reason, holder, Facet.Precedence.DEFAULT);
    }

    protected ImmutableFacetAbstract(
            final VetoReason reason,
            final FacetHolder holder,
            final Facet.Precedence precedence) {
        super(type(), holder, precedence);
        this.reason = reason;
    }

    @Override
    public final Optional<VetoReason> disabledReason(final ManagedObject targetAdapter) {
        return Optional.of(reason);
    }

    @Override
    public final void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("reason", reason.string());
    }

    /**
     * Immutable facet only prevents changes to a property or a collection.
     */
    @Override
    public Optional<VetoReason> disables(final UsabilityContext ic) {
        final ManagedObject target = ic.getTarget();
        switch (ic.getInteractionType()) {
        case PROPERTY_MODIFY:
        case COLLECTION_ADD_TO:
        case COLLECTION_REMOVE_FROM:
            return disabledReason(target);
        default:
            return Optional.empty();
        }
    }

}
