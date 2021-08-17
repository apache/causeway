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
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.springframework.lang.Nullable;

import org.apache.isis.core.metamodel.util.snapshot.XmlSchema;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class FacetUtil {

    /**
     * Attaches the {@link Facet} to its {@link Facet#getFacetHolder()}.
     * @param facet - non-null
     * @return the argument as is
     */
    public static <F extends Facet> F addFacet(final @NonNull F facet) {
        facet.getFacetHolder().addFacet(facet);
        return facet;
    }

    /**
     * Attaches the {@link Facet} to its {@link Facet#getFacetHolder() facet
     * holder} based on precedence. Acts as a no-op if facet is <tt>null</tt>.
     * @param facetIfAny - null-able (for fail-safety)
     * @return the argument as is - or just in case if null converted to an Optional.empty()
     */
    public static <F extends Facet> Optional<F> addFacetIfPresent(final @Nullable Optional<F> facetIfAny) {
        if (facetIfAny == null) {
            return Optional.empty();
        }
        facetIfAny
            .ifPresent(facet->facet.getFacetHolder().addFacet(facet));
        return facetIfAny;
    }

    /**
     * Attaches each {@link Facet} to its {@link Facet#getFacetHolder() facet
     * holder} based on precedence.
     *
     * @return whether given {@code facetList} contains any non-<tt>null</tt> facets
     */
    public static boolean addFacets(final @NonNull Iterable<Facet> facetList) {
        boolean addedFacets = false;
        for (val facet : facetList) {
            addedFacets = addFacetIfPresent(Optional.ofNullable(facet)).isPresent()
                    | addedFacets;
        }
        return addedFacets;
    }

    public static <T extends Facet> XmlSchema.ExtensionData<T> getFacetsByType(final FacetHolder facetHolder) {

        return new XmlSchema.ExtensionData<T>() {

            @Override
            public int size() {
                return facetHolder.getFacetCount();
            }

            @SuppressWarnings("unchecked")
            @Override
            public void visit(final BiConsumer<Class<T>, T> elementConsumer) {
                facetHolder.streamFacets()
                .forEach(facet->elementConsumer.accept((Class<T>)facet.facetType(), (T)facet));
            }

        };
    }

    public static void copyFacetsTo(final FacetHolder source, final FacetHolder target) {
        source.streamFacets()
        .forEach(target::addFacet);
    }

    // -- DYNAMIC UPDATE SUPPORT

    /**
     * Removes any facet that matches the facet's java class from its FacetHolder,
     * then adds the facet to the facetHolder.
     */
    public static <F extends Facet> void updateFacet(
            final @NonNull F facet) {

        purgeIf(facet.facetType(), facet.getClass()::isInstance, facet.getFacetHolder());
        addFacet(facet);
    }

    /**
     * Removes any facet of facet-type from facetHolder if it passes the given filter,
     * then if present adds facetIfAny to the facetHolder.
     */
    public static <F extends Facet> void updateFacet(
            final @NonNull Class<F> facetType,
            final @NonNull Predicate<? super F> filter,
            final @NonNull Optional<? extends F> facetIfAny,
            final @NonNull FacetHolder facetHolder) {

        purgeIf(facetType, filter, facetHolder);
        addFacetIfPresent(facetIfAny);
    }

    /**
     * Removes any facet of facet-type from facetHolder if it passes the given filter.
     */
    public static <F extends Facet> void purgeIf(
            final Class<F> facetType,
            final Predicate<? super F> filter,
            final FacetHolder facetHolder) {

        facetHolder.getFacetRanking(facetType)
        .ifPresent(ranking->ranking.purgeIf(facetType, filter));
    }


}
