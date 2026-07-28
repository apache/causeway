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

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.causeway.applib.annotation.Domain;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Streams;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

public interface Hierarchical {

    /**
     * Get the set of specifications for all the interfaces that the class
     * represented by this specification implements.
     */
    Can<ObjectSpecification> interfaces();

    /**
     * Whether <code>this</code> specification represents the same specification,
     * or a subclass of the specified <code>other</code> specification.
     * <p>
     * <tt>subSpec.isOfType(superSpec)</tt> is equivalent to
     * {@link Class#isAssignableFrom(Class) Java's}
     * <tt>superType.isAssignableFrom(subType)</tt>.
     * @return whether <code>this</code> is <b>instanceof</b> <code>other</code>
     */
    boolean isOfType(ObjectSpecification other);

    /**
     * Same as {@link #isOfType(ObjectSpecification)}, except treating wrapper/primitive the same.
     */
    boolean isOfTypeResolvePrimitive(ObjectSpecification other);

    /**
     * Get the specification for this specification's class's superclass.
     */
    ObjectSpecification superclass();

    default boolean isTypeHierarchyRoot() {
        return superclass()==null;
    }

	static <T extends Facet> Optional<T> lookupFacet(final Class<T> facetType,
			final FacetHolder facetHolder,
			final Hierarchical hierarchical) {
        // lookup facet holder's facet
		Stream<T> facets1 = facetHolder.lookupFacet(facetType).stream();

        // lookup all interfaces
		Stream<T> facets2 = _NullSafe.stream(hierarchical.interfaces())
                .filter(_NullSafe::isPresent) // just in case
                .flatMap(interfaceSpec->interfaceSpec.lookupFacet(facetType).stream());

        // search up the inheritance hierarchy
		Stream<T> facets3 = _NullSafe.streamNullable(hierarchical.superclass())
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
