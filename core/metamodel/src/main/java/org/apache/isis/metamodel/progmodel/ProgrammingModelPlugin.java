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
package org.apache.isis.metamodel.progmodel;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import org.apache.isis.commons.internal.collections._Multimaps;
import org.apache.isis.commons.internal.collections._Multimaps.SetMultimap;
import org.apache.isis.metamodel.facets.FacetFactory;

public interface ProgrammingModelPlugin {

    // -- CONTRACT

    /**
     * Guides the priority at which facet factories are registered.
     * There is no other use.
     */
    public static enum FacetFactoryCategory {
        // extend as needed ...
        VALUE
        ;
    }

    public static interface FactoryCollector {

        /**
         *
         * @param factoryClass
         * @param category
         */
        public void addFactory(FacetFactory facetFactory, final FacetFactoryCategory category);

        /**
         *
         * @param category
         * @return
         */
        public Set<FacetFactory> getFactories(FacetFactoryCategory category);

    }

    public static FactoryCollector collector() {
        return new FactoryCollector() {

            final SetMultimap<FacetFactoryCategory, FacetFactory> factoriesByCategory =
                    _Multimaps.newSetMultimap();

            @Override
            public void addFactory(FacetFactory factory, FacetFactoryCategory category) {
                Objects.requireNonNull(factory);
                Objects.requireNonNull(category);
                factoriesByCategory.putElement(category, factory);
            }

            @Override
            public Set<FacetFactory> getFactories(final FacetFactoryCategory category) {
                if(category==null) {
                    return Collections.emptySet();
                }
                return Collections.unmodifiableSet(
                        factoriesByCategory.getOrDefault(category, Collections.emptySet())	);

            }
        };
    }

    // -- INTERFACE

    public void plugin(FactoryCollector collector);

    // --

}
