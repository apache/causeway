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
package org.apache.isis.applib.services.grid;

import org.apache.isis.applib.layout.grid.Grid;

/**
 * Provides an implementation of {@link Grid}.
 *
 * @since 1.x {@index}
 */
public interface GridSystemService<G extends Grid> {

    /**
     * Which grid (implementation) is defined by this service.
     */
    Class<G> gridImplementation();

    String tns();

    String schemaLocation();

    G defaultGrid(Class<?> domainClass);

    /**
     * Validate the grid, derive any missing object members, and overwrite any facets in the metamodel based on the
     * layout.
     */
    void normalize(G grid, Class<?> domainClass);

    void complete(G grid, Class<?> domainClass);

    void minimal(G grid, Class<?> domainClass);

}
