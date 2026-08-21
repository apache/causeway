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

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.facetapi.Facet.Precedence;
import org.apache.causeway.core.metamodel.facetapi.FacetWithAttributes.DisablingOrEnabling;
import org.apache.causeway.core.metamodel.facetapi.FacetWithAttributes.HidingOrShowing;
import org.apache.causeway.core.metamodel.facetapi.FacetWithAttributes.Validating;
import org.apache.causeway.core.metamodel.util.snapshot.XmlSchema;
import org.jspecify.annotations.NonNull;
import org.springframework.util.ClassUtils;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class FacetUtil {

    public static <T extends Facet> XmlSchema.ExtensionData<T> getFacetsByType(final FacetHolder facetHolder) {
        return new XmlSchema.ExtensionData<>() {
            @Override public int size() {
                return facetHolder.getFacetCount();
            }
            @SuppressWarnings("unchecked")
            @Override public void visit(final BiConsumer<Class<T>, T> elementConsumer) {
                facetHolder.streamFacets()
                	.forEach(facet->elementConsumer.accept((Class<T>)facet.facetType(), (T)facet));
            }
        };
    }

    // -- FACET ATTRIBUTES

    public static String attributesAsString(final Facet facet) {
        return streamAttributes(facet)
            .filter(kv->!kv.key().equals("facet")) // skip superfluous attribute
            .map(_Strings.KeyValuePair::toString)
            .collect(Collectors.joining("; "));
    }

    public static Stream<_Strings.KeyValuePair> streamAttributes(final Facet facet) {
        final var keyValuePairs = new ArrayList<_Strings.KeyValuePair>();
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
        if(facetHolders==null)
            return Optional.empty();
        return Stream.of(facetHolders)
            .filter(Objects::nonNull)
            .map(facetHolder->facetHolder.lookupFacet(facetType).orElse(null))
            .filter(Objects::nonNull)
            .reduce((a, b)->b.precedence().ordinal()>a.precedence().ordinal()
                ? b
                : a);
    }

    /** Looks up specified facetType within given {@link FacetHolder}s, honoring Facet {@link Precedence},
     * while first one found wins over later found if they have the same precedence. */
    public static <F extends Facet> Optional<F> lookupFacetInButExcluding(
            final @NonNull Class<F> facetType,
            final Predicate<Object> excluded,
            final FacetHolder ... facetHolders) {
        if(facetHolders==null)
            return Optional.empty();
        return Stream.of(facetHolders)
	        .filter(Objects::nonNull)
	        .filter(x -> !excluded.test(x))
	        .map(facetHolder->facetHolder.lookupFacet(facetType).orElse(null))
	        .filter(Objects::nonNull)
	        .reduce((a, b)->b.precedence().ordinal()>a.precedence().ordinal()
	                ? b
	                : a);
    }

    /**
     * Looks up the exact facet class and if found returns it,
     * otherwise creates it via the factory and automatically wires it to its holder.
     * <p>
     * Only exception is, when there already exists a facet with higher precedence, in which case
     * an empty optional is returned.
     */
    public static <E extends T, T extends Facet> Optional<E> computeIfAbsentExact(
            final FacetHolder facetHolder,
            final Class<T> facetType,
            final Class<E> facetExactClass,
            final Precedence overrideUpToIncluding,
            final Function<FacetHolder, E> facetFactory) {

        final T winnerFacet = facetHolder.lookupFacet(facetType).orElse(null);
        if(winnerFacet==null)
        	return Optional.of(facetFactory.apply(facetHolder));
        if(winnerFacet.getClass().equals(facetExactClass))
        	return Optional.of(winnerFacet).map(facetExactClass::cast);
        // check if we are allowed to override based on precedence
        if(winnerFacet.precedence().ordinal()<=overrideUpToIncluding.ordinal())
            return Optional.of(facetFactory.apply(facetHolder));
        // not allowed to override
        return Optional.empty();
    }

	public static void visitAttributes(final Facet facet, final BiConsumer<String, Object> visitor) {
        visitor.accept("facet", canonicalFacetShortName(facet));
        visitor.accept("precedence", facet.precedence().name());

        var interactionAdvisors = interactionAdvisors(facet, ", ");

        // suppress 'advisors' if none
        if(!interactionAdvisors.isEmpty()) {
            visitor.accept("interactionAdvisors", interactionAdvisors);
        }
	}

	// for anonymous inner classes use the container class name instead.
	private static final Pattern NUMERIC_ENDING = Pattern.compile("\\.\\d{1,2}$");
	private String canonicalFacetShortName(final Facet facet) {
		var matcher = NUMERIC_ENDING.matcher(ClassUtils.getShortName(facet.getClass()));
		return matcher.replaceAll("");
	}

    private String interactionAdvisors(final Facet facet, final String delimiter) {
        return Stream.of(Validating.class, HidingOrShowing.class, DisablingOrEnabling.class)
	        .filter(marker->marker.isAssignableFrom(facet.getClass()))
	        .map(Class::getSimpleName)
	        .collect(Collectors.joining(delimiter));
    }

}
