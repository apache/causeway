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
package org.apache.causeway.core.metamodel.facets.object.navchild;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Provides a MethodHandle and its associated Dewey order.
 *
 * @since 3.2
 */
public final class NavigableSubtreeSequenceFacet
extends FacetAbstract {

	@Getter @Accessors(fluent = true)
    private final String sequence;
	@Getter @Accessors(fluent = true)
    private final MethodHandle methodHandle;
    private final String origin;

    // -- FACTORY

    public static Optional<NavigableSubtreeSequenceFacet> create(
        /**
         * Informal text, describing the origin of this facet.
         */
        final String origin,
        final Class<?> cls,
        final Optional<ResolvedMethod> resolvedMethod,
        final String sequence,
        final FacetHolder facetHolder) {

        return resolvedMethod
            .map(ResolvedMethod::method)
            .flatMap(method->Try.call(()->MethodHandles
                    .privateLookupIn(cls, MethodHandles.lookup())
                    .unreflect(method))
                .ifFailure(Throwable::printStackTrace)
                .getValue()
                .map(mh->new NavigableSubtreeSequenceFacet(sequence, mh, origin, facetHolder)));
    }

    private NavigableSubtreeSequenceFacet(
    	    final String sequence,
    	    final MethodHandle methodHandle,
    	    final String origin,
    	    final FacetHolder facetHolder) {
    	super(NavigableSubtreeSequenceFacet.class, facetHolder);
    	this.sequence = sequence;
    	this.methodHandle = methodHandle;
    	this.origin = origin;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
    	super.visitAttributes(visitor);
        visitor.accept("origin", origin);
        visitor.accept("sequence", sequence);
        visitor.accept("methodHandle", methodHandle);
    }

}

