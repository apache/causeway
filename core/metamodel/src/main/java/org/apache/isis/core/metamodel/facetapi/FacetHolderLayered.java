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
class FacetHolderLayered
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
        this.local = FacetHolderAbstract.simple(shared.getMetaModelContext(), featureIdentifier);
    }

    @Override
    public TranslationContext getTranslationContext() {
        // don't use the local's TranslationContext - produces less work for human translators
        return shared.getTranslationContext();
    }

    @Override
    public void addFacet(@NonNull final Facet facet) {
        // eg. if a Facet originates from layout.xml introspection, don't install it on the shared FacetHolder
        val facetHolder = facet.isAllowedToBeSharedWhenMixedIn()
                ? shared
                : local;
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
        return (int)streamFacets().count();
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
        return localFacet; // have the local win, this is safe for layout.xml stuff, but not for future use-cases

//        // semantic conflict (unfortunately semanticEquals() is not always implemented yet)
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
        //FIXME[ISIS-3049] concat with local
        return shared.streamFacets();
    }

    @Override
    public Stream<FacetRanking> streamFacetRankings() {
        //FIXME[ISIS-3049] concat with local
        return shared.streamFacetRankings();
    }

    @Override
    public Optional<FacetRanking> getFacetRanking(final Class<? extends Facet> facetType) {
        //FIXME[ISIS-3049] concat with local
        return shared.getFacetRanking(facetType);
    }

}
