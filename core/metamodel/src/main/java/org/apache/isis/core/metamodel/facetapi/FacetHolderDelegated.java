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
package org.apache.isis.core.metamodel.facetapi;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.FacetedMethod;

import lombok.Getter;
import lombok.NonNull;

/**
 * Acts as a facade to the underlying delegate, but provides its own <i>Member</i>-{@link Identifier}.
 * <p>
 * Any mixed in <i>Member</i> has a {@link FacetedMethod} that acts as the {@link FacetHolder},
 * which is shared among all types that this particular mixed in <i>Member</i> is contributed to.
 * @apiNote
 *      was introduced in response to a couple of bugs originating from having both the <i>Member</i> and
 *      its <i>Peer</i> maintaining their own list of {@link Facet}(s), that may run out of sync
 *      eg. with metamodel post-processing;
 */
class FacetHolderDelegated
implements FacetHolder {

    @Getter(onMethod_ = {@Override})
    private final @NonNull Identifier featureIdentifier;
    private final @NonNull FacetHolder delegate;

    public FacetHolderDelegated(
            final @NonNull Identifier featureIdentifier,
            final @NonNull FacetHolder delegate) {
        this.featureIdentifier = featureIdentifier;
        this.delegate = delegate;
    }

    @Override
    public TranslationContext getTranslationContext() {
        return delegate.getTranslationContext();
    }

    @Override
    public void addFacet(@NonNull final Facet facet) {
        //FIXME[ISIS-3049] if a Facet originates from layout.xml introspection, don't install it on the delegate
        delegate.addFacet(facet);
    }

    @Override
    public MetaModelContext getMetaModelContext() {
        return delegate.getMetaModelContext();
    }

    @Override
    public int getFacetCount() {
        return delegate.getFacetCount();
    }

    @Override
    public <T extends Facet> T getFacet(final Class<T> facetType) {
        return delegate.getFacet(facetType);
    }

    @Override
    public boolean containsFacet(final Class<? extends Facet> facetType) {
        return delegate.containsFacet(facetType);
    }

    @Override
    public Stream<Facet> streamFacets() {
        return delegate.streamFacets();
    }

    @Override
    public Stream<FacetRanking> streamFacetRankings() {
        return delegate.streamFacetRankings();
    }

    @Override
    public Optional<FacetRanking> getFacetRanking(final Class<? extends Facet> facetType) {
        return delegate.getFacetRanking(facetType);
    }

}
