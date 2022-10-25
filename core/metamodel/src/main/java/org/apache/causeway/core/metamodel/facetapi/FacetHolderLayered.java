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
package org.apache.causeway.core.metamodel.facetapi;

import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

/**
 * Provides a merged view of the <i>local</i> and the <i>shared</i> list of {@link Facet}s,
 * but has its own <i>Member</i>-{@link Identifier}.
 * <p>
 * Any mixed in <i>Member</i> has a {@link FacetedMethod} that acts as the {@link FacetHolder},
 * which is shared among all types that this particular mixed in <i>Member</i> is contributed to.
 *
 * @apiNote
 *      was introduced in response to a couple of bugs originating from having both the <i>Member</i> and
 *      its <i>Peer</i> maintaining their own list of {@link Facet}(s), that may run out of sync
 *      eg. with metamodel post-processing;
 *
 * @see Facet#isAllowedToBeSharedWhenMixedIn()
 */
final class FacetHolderLayered
implements FacetHolder {

    @Getter(onMethod_ = {@Override})
    private final @NonNull Identifier featureIdentifier;
    private final @NonNull FacetHolder shared;
    private final @NonNull FacetHolder local;

    public FacetHolderLayered(
            final @NonNull Identifier featureIdentifier,
            final @NonNull FacetHolder shared) {
        this.featureIdentifier = featureIdentifier;
        this.shared = shared;
        this.local = FacetHolder.simple(shared.getMetaModelContext(), featureIdentifier);
    }

    @Override
    public TranslationContext getTranslationContext() {
        // don't use the local's TranslationContext - produces less work for human translators
        return shared.getTranslationContext();
    }

    @Override
    public void addFacet(@NonNull final Facet facet) {
        // eg. if a Facet originates from layout.xml introspection, don't install it on the shared FacetHolder
        val facetHolder = facet.isObjectTypeSpecific()
                ? local
                : shared;
        facetHolder.addFacet(facet);
    }

    @Override
    public MetaModelContext getMetaModelContext() {
        return shared.getMetaModelContext();
    }

    @Override
    public int getFacetCount() {
        // optimization, not strictly required
        if(local.getFacetCount()==0) {
            return shared.getFacetCount();
        }
        // cannot simply add up shared and local
        return (int)streamPopulatedFacetTypes().count();
    }

    @Override
    public <T extends Facet> T getFacet(final Class<T> facetType) {
        val localFacet = local.getFacet(facetType);
        val sharedFacet = shared.getFacet(facetType);

        if(localFacet==null) {
            return sharedFacet;
        }
        if(sharedFacet==null) {
            return localFacet;
        }
        if(localFacet.getPrecedence().ordinal() > sharedFacet.getPrecedence().ordinal()) {
            return localFacet;
        }
        if(sharedFacet.getPrecedence().ordinal() > localFacet.getPrecedence().ordinal()) {
            return sharedFacet;
        }
        if(localFacet.semanticEquals(sharedFacet)) {
            return localFacet; // arbitrarily picking one
        }
        // semantic conflict
        // have the local win, this is safe for layout.xml stuff, but probably not for future use-cases
        return localFacet;

//        unfortunately semanticEquals() is not always implemented yet, otherwise we could throw ...
//        throw _Exceptions.illegalState("conflicting facet semantics between shared %s and local %s",
//                FacetUtil.toString(sharedFacet),
//                FacetUtil.toString(localFacet));
    }

    @Override
    public boolean containsFacet(final Class<? extends Facet> facetType) {
        return shared.containsFacet(facetType)
                || local.containsFacet(facetType);
    }

    @Override
    public Stream<Facet> streamFacets() {
        // optimization, not strictly required
        if(local.getFacetCount()==0) {
            return shared.streamFacets();
        }
        return streamPopulatedFacetTypes()
                .<Facet>map(facetType->getFacet(facetType))
                .filter(_NullSafe::isPresent);
    }

    @Override
    public Stream<FacetRanking> streamFacetRankings() {
        // optimization, not strictly required
        if(local.getFacetCount()==0) {
            return shared.streamFacetRankings();
        }
        return streamPopulatedFacetTypes()
                .map(facetType->getFacetRanking(facetType).orElseThrow());
    }

    @Override
    public Optional<FacetRanking> getFacetRanking(final Class<? extends Facet> facetType) {
        val localFacetRanking = local.getFacetRanking(facetType);
        val sharedFacetRanking = shared.getFacetRanking(facetType);

        if(localFacetRanking.isEmpty()) {
            return sharedFacetRanking;
        }
        if(sharedFacetRanking.isEmpty()) {
            return localFacetRanking;
        }

        val combinedFacetRanking = new FacetRanking(facetType);
        // arbitrarily picking order: shared first and local last, such that if in conflict local wins
        combinedFacetRanking.addAll(sharedFacetRanking.get());
        combinedFacetRanking.addAll(localFacetRanking.get());

        return Optional.of(combinedFacetRanking);
    }

    // -- HELPER

    private Stream<Class<? extends Facet>> streamPopulatedFacetTypes() {
        val facetTypes = new HashSet<Class<? extends Facet>>();
        Stream.concat(
                shared.streamFacets(),
                local.streamFacets())
        .map(Facet::facetType)
        .forEach(facetTypes::add);
        return facetTypes.stream();
    }

}
