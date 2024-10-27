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

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.metamodel.facetapi.Facet.Precedence;
import org.apache.causeway.core.metamodel.util.snapshot.XmlSchema;

import lombok.NonNull;
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
        for (var facet : facetList) {
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

    // -- DYNAMIC UPDATE SUPPORT

    /**
     * Removes any facet from its FacetHolder, that matches the facet's java class
     * and has no higher precedence than the given one,
     * then adds given facet to its facetHolder, honoring precedence.
     */
    public static void updateFacet(final @Nullable Facet facet) {
        if(facet==null) {
            return;
        }
        final boolean skip = facet.getFacetHolder().lookupFacet(facet.facetType())
                .map(Facet::getPrecedence)
                .map(Facet.Precedence::ordinal)
                .map(ordinal -> ordinal>facet.getPrecedence().ordinal())
                .orElse(false);
        if(skip) {
            return;
        }

        purgeIf(facet.facetType(), facet.getClass()::isInstance, facet.getFacetHolder());
        addFacet(facet);
    }

    /**
     * If facetIfAny is present,
     * calls {@link #updateFacet(Facet)}, that is,
     * adds it to its facetHolder, replacing any pre-existing, honoring precedence.
     * Otherwise acts as a no-op.
     */
    public static <F extends Facet> void updateFacetIfPresent(
            final @NonNull Optional<? extends F> facetIfAny) {
        updateFacet(facetIfAny.orElse(null));
    }

    /**
     * Removes any facet of facet-type from facetHolder if it passes the given filter.
     */
    private static <F extends Facet> void purgeIf(
            final Class<F> facetType,
            final Predicate<? super F> filter,
            final FacetHolder facetHolder) {

        facetHolder.getFacetRanking(facetType)
        .ifPresent(ranking->ranking.purgeIf(facetType, filter));
    }

    // -- FACET ATTRIBUTES

    public static String attributesAsString(final Facet facet) {
        return streamAttributes(facet)
                .filter(kv->!kv.getKey().equals("facet")) // skip superfluous attribute
                .map(_Strings.KeyValuePair::toString)
                .collect(Collectors.joining("; "));
    }

    public static Stream<_Strings.KeyValuePair> streamAttributes(final Facet facet) {
        final var keyValuePairs = _Lists.<_Strings.KeyValuePair>newArrayList();
        facet.visitAttributes((k, v)->keyValuePairs.add(_Strings.pair(k, ""+v)));
        return keyValuePairs.stream();
    }

    // -- FACET TO STRING

    public static String toString(final Facet facet) {
        var className = ClassUtils.getShortName(facet.getClass());
        var attributesAsString = attributesAsString(facet);
        return facet.getClass() == facet.facetType()
                ? String.format("%s[%s]", className, attributesAsString)
                : String.format("%s[type=%s; %s]", className, ClassUtils.getShortName(facet.facetType()), attributesAsString);
    }

    // -- FACET LOOKUP

    /** Looks up specified facetType within given {@link FacetHolder}s, honoring Facet {@link Precedence},
     * while first one found wins over later found if they have the same precedence. */
    public static <F extends Facet> Optional<F> lookupFacetIn(final @NonNull Class<F> facetType, final FacetHolder ... facetHolders) {
        if(facetHolders==null) {
            return Optional.empty();
        }
        return Stream.of(facetHolders)
        .filter(_NullSafe::isPresent)
        .map(facetHolder->facetHolder.getFacet(facetType))
        .filter(_NullSafe::isPresent)
        .reduce((a, b)->b.getPrecedence().ordinal()>a.getPrecedence().ordinal()
                ? b
                : a);
    }

    /** Looks up specified facetType within given {@link FacetHolder}s, honoring Facet {@link Precedence},
     * while first one found wins over later found if they have the same precedence. */
    public static <F extends Facet> Optional<F> lookupFacetInButExcluding(
            final @NonNull Class<F> facetType,
            final Predicate<Object> excluded,
            final FacetHolder ... facetHolders) {
        if(facetHolders==null) {
            return Optional.empty();
        }
        return Stream.of(facetHolders)
        .filter(_NullSafe::isPresent)
        .filter(x -> !excluded.test(x))
        .map(facetHolder->facetHolder.getFacet(facetType))
        .filter(_NullSafe::isPresent)
        .reduce((a, b)->b.getPrecedence().ordinal()>a.getPrecedence().ordinal()
                ? b
                : a);
    }

}
