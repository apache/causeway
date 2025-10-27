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
package org.apache.causeway.applib.services.grid;

import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;

/**
 * Encapsulates a single layout grid system which can be used to customize the layout
 * of domain objects.
 *
 * <p>In particular this means being able to return a "normalized" form
 * (validating and associating domain object members into the various regions
 * of the grid) and in providing a default grid if there is no other metadata
 * available.
 *
 * @since 1.x revised for 4.0 {@index}
 */
public interface GridSystemService {

    /**
     * A default grid, used when no grid layout can be found for the domain
     * class.
     *
     * <p>For example, this layout could define two columns in ratio 4:8.
     *
     * @param domainClass
     */
    BSGrid defaultGrid(Class<?> domainClass);

    /**
     * Validates and normalizes a grid, modifying the grid so that all of the
     * domain object's members (properties, collections, actions) are bound to
     * regions of the grid.
     *
     * <p>E.g. for properties (and similar for collections and actions) the annotation attributes
     * {@link org.apache.causeway.applib.annotation.PropertyLayout#sequence()}
     * and
     * {@link org.apache.causeway.applib.annotation.PropertyLayout#fieldSetId()}
     * or
     * {@link org.apache.causeway.applib.annotation.PropertyLayout#fieldSetName()}
     * are used.
     * Such a grid, if persisted as the layout XML file for the domain class,
     * allows the various layout annotation attributes to be unspecified or removed from the source code
     * of the domain class.
     */
    void normalize(BSGrid grid, Class<?> domainClass);

    /**
     * Takes a normalized grid and enriches it with all the available metadata
     * (taken from Apache Causeway' internal metadata) that can be represented in
     * the layout XML.
     *
     * <p>Such a grid, if persisted as the layout XML file for the domain class,
     * allows all layout annotations
     * ({@link org.apache.causeway.applib.annotation.ActionLayout},
     * {@link org.apache.causeway.applib.annotation.PropertyLayout},
     * {@link org.apache.causeway.applib.annotation.CollectionLayout}) to be
     * removed from the source code of the domain class.
     * @param grid
     * @param domainClass
     */
    void complete(BSGrid grid, Class<?> domainClass);

    /**
     * Takes a normalized grid and strips out removes all members, leaving only
     * the grid structure.
     *
     * <p>Such a grid, if persisted as the layout XML file for the domain class,
     * requires that e.g. for properties (and similar for collections and actions) the annotation attributes
     * {@link org.apache.causeway.applib.annotation.PropertyLayout#sequence()}
     * and
     * {@link org.apache.causeway.applib.annotation.PropertyLayout#fieldSetId()}
     * or
     * {@link org.apache.causeway.applib.annotation.PropertyLayout#fieldSetName()}
     * are retained in the source code of said class in order to bind
     * members to the regions of the grid.
     *
     * @param grid
     * @param domainClass
     */
    void minimal(BSGrid grid, Class<?> domainClass);

}
