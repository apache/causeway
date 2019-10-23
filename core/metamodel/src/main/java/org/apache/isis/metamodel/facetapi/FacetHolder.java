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

package org.apache.isis.metamodel.facetapi;

import java.util.stream.Stream;

import org.apache.isis.metamodel.MetaModelContext;

/**
 * Anything in the metamodel (which also includes peers in the reflector) that
 * can be extended.
 */
public interface FacetHolder {

    int getFacetCount();

    /**
     * Get the facet of the specified type (as per the type it reports from
     * {@link Facet#facetType()}).
     */
    <T extends Facet> T getFacet(Class<T> cls);

    /**
     * Whether there is a facet registered of the specified type.
     */
    boolean containsFacet(Class<? extends Facet> facetType);

    /**
     * Whether there is a facet registered of the specified type that is not a
     * {@link Facet#isNoop() no-op}.
     *
     * <p>
     * Convenience; saves having to {@link #getFacet(Class)} and then check if
     * <tt>null</tt> and not a no-op.
     */
    boolean containsDoOpFacet(Class<? extends Facet> facetType);

    /**
     * As {@link #containsDoOpFacet(Class)}, which additional requirement that the
     * facet is not {@link Facet#isDerived()}.
     */
    boolean containsDoOpNotDerivedFacet(Class<? extends Facet> facetType);

    Stream<Facet> streamFacets();

    /**
     * Adds the facet, extracting its {@link Facet#facetType() type} as the key.
     *
     * <p>
     * If there are any facet of the same type, they will be overwritten
     * <i>provided</i> that either the {@link Facet} specifies to
     * {@link Facet#alwaysReplace() always replace} or if the existing
     * {@link Facet} is a {@link Facet#isNoop() no-op}.
     */
    void addFacet(Facet facet);

    /**
     * Adds the {@link MultiTypedFacet multi-typed facet}, extracting each of
     * its {@link MultiTypedFacet#facetTypes() types} as keys.
     *
     * <p>
     * If there are any facet of the same type, they will be overwritten
     * <i>provided</i> that either the {@link Facet} specifies to
     * {@link Facet#alwaysReplace() always replace} or if the existing
     * {@link Facet} is a {@link Facet#isNoop() no-op}.
     */
    void addMultiTypedFacet(MultiTypedFacet facet);

    /**
     * Remove the facet whose type is that reported by {@link Facet#facetType()}
     * .
     */
    void removeFacet(Facet facet);

    /**
     * Remove the facet of the specified type.
     */
    void removeFacet(Class<? extends Facet> facetType);


    /**
     * 
     * @since 2.0
     */
    MetaModelContext getMetaModelContext();

}
