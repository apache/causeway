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

import com.google.common.base.Predicate;
import org.apache.isis.applib.filter.Predicates;

public final class FacetFilters {

    private FacetFilters() {
    }

    /**
     * {@link Predicate <Facet>#accept(Facet) Accepts} everything.
     */
    public static final Predicate<Facet> ANY = Predicates.anyOfType(Facet.class);
    /**
     * {@link Predicate <Facet>#accept(Facet) Accepts} nothing.
     */
    public static final Predicate<Facet> NONE = new Predicate<Facet>() {
        @Override
        public boolean apply(final Facet facet) {
            return false;
        }
    };

    public static Predicate<Facet> isA(final Class<?> superClass) {
        return new Predicate<Facet>() {
            @Override
            public boolean apply(final Facet facet) {
                if (facet instanceof DecoratingFacet) {
                    final DecoratingFacet<?> decoratingFacet = (DecoratingFacet<?>) facet;
                    return apply(decoratingFacet.getDecoratedFacet());
                }
                return superClass.isAssignableFrom(facet.getClass());
            }
        };
    }
}
