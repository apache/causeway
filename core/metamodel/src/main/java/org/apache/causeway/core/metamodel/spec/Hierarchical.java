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
package org.apache.causeway.core.metamodel.spec;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.collections._Streams;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

public interface Hierarchical {

    final static Hierarchical EMPTY = new Hierarchical() {
		@Override public Optional<ObjectSpecification> superSpec() { return Optional.empty(); }
		@Override public Can<ObjectSpecification> interfaceSpecs() { return Can.empty(); }
	};

	record HierarchicalRecord(
			Optional<ObjectSpecification> superSpec,
			Can<ObjectSpecification> interfaceSpecs)
	implements Hierarchical {
		public HierarchicalRecord {
			superSpec = superSpec!=null
					? superSpec
					: Optional.empty();
			interfaceSpecs = interfaceSpecs!=null
					? interfaceSpecs
					: Can.empty();
		}
	}

	/**
     * Get the set of specifications for all the interfaces that the class
     * represented by this specification implements.
     */
    Can<ObjectSpecification> interfaceSpecs();

    /**
     * Get the specification for this specification's class's superclass.
     */
    Optional<ObjectSpecification> superSpec();

    default boolean isTypeHierarchyRoot() {
        return superSpec().isEmpty();
    }

    /**
     * Returns {@link Stream} of the class hierarchy upwards starting with superSpec.
     */
    default Stream<ObjectSpecification> streamSuperTypeHierarchy() {
        return superSpec()
        		.map(superSpec->Stream.concat(Stream.of(superSpec), superSpec.streamSuperTypeHierarchy()))
        		.orElseGet(Stream::empty);
    }

	static <T extends Facet> Optional<T> lookupFacet(final Class<T> facetType,
			final FacetHolder facetHolder,
			final Hierarchical hierarchical) {
        // lookup facet holder's facet
		Stream<T> facets1 = facetHolder.lookupFacet(facetType).stream();

        // lookup all interfaces
		Stream<T> facets2 = hierarchical.interfaceSpecs().stream()
                .filter(Objects::nonNull) // just in case
                .flatMap(interfaceSpec->interfaceSpec.lookupFacet(facetType).stream());

        // search up the inheritance hierarchy
		Stream<T> facets3 = hierarchical.superSpec().stream()
                .flatMap(superSpec->superSpec.lookupFacet(facetType).stream());

		Stream<T> facetsCombined = _Streams.<T>concat(facets1, facets2, facets3);

		// local class, declared inside this method body, so it is not publicly exposed via this interface
		// while the test method is called, collects the first occurrence of a fallback facet
		class FallbackFacetFilter<Q extends Facet> implements Predicate<Q> {
	        Q fallback;

	        @Override
	        public boolean test(final Q facet) {
	            if(facet==null)
					return false;
	            if(!facet.precedence().isFallback())
					return true;
	            if(fallback == null) {
	                fallback = facet;
	            }
	            return false;
	        }
	    }

        var filter = new FallbackFacetFilter<T>();

        return Optional.ofNullable(facetsCombined
                .filter(filter)
                .findFirst()
                .orElse(filter.fallback));
	}

}
