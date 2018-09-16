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

import java.util.stream.Stream;

/**
 * A Class that provides multiple facet implementations, either directly or
 * through a delegate.
 *
 * <p>
 * The client of this interface should use {@link #getFacet(Class)} to obtain
 * the facet implementation for each of the {@link #facetTypes() facets types}.
 */
public interface MultiTypedFacet extends Facet {

    /**
     * All of the facet types either implemented or available by this facet
     * implementation.
     *
     */
    public Stream<Class<? extends Facet>> facetTypes();

    public <T extends Facet> T getFacet(Class<T> facet);

    boolean containsFacetTypeOf(Class<? extends Facet> facetType);
}
