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

import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;

public final class FacetFilters {

    private FacetFilters() {
    }

    /**
     * {@link Filter<Facet>#accept(Facet) Accepts} everything.
     */
    public static final Filter<Facet> ANY = Filters.anyOfType(Facet.class);
    /**
     * {@link Filter<Facet>#accept(Facet) Accepts} nothing.
     */
    public static final Filter<Facet> NONE = new Filter<Facet>() {
        @Override
        public boolean accept(final Facet facet) {
            return false;
        }
    };

    public static Filter<Facet> isA(final Class<?> superClass) {
        return new Filter<Facet>() {
            @Override
            public boolean accept(final Facet facet) {
                if (facet instanceof DecoratingFacet) {
                    final DecoratingFacet<?> decoratingFacet = (DecoratingFacet<?>) facet;
                    return accept(decoratingFacet.getDecoratedFacet());
                }
                return superClass.isAssignableFrom(facet.getClass());
            }
        };
    }
}
