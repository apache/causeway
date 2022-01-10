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
 * Encapsulates a single layout grid system which can be used to customize the layout
 * of domain objects.
 *
 * <p>
 * In particular this means being able to return a "normalized" form
 * (validating and associating domain object members into the various regions
 * of the grid) and in providing a default grid if there is no other metadata
 * available.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface GridSystemService<G extends Grid> {

    /**
     * The concrete subclass of {@link Grid} supported by this implementation.
     *
     * <p>
     *     There can be multiple implementations of this service, this indicates
     *     the base class used by the implementation.
     * </p>
     */
    Class<G> gridImplementation();

    /**
     * The target namespace for this grid system.
     *
     * <p>
     *     This is used when generating the XML.  The Bootstrap3 grid system
     *     provided by the framework returns the value
     *     `http://isis.apache.org/applib/layout/grid/bootstrap3`.
     * </p>
     */
    String tns();

    /**
     * The schema location for the XSD.
     *
     * <p>
     *     Every grid system is expected to provide a schema XSD in order to
     *     provide code completion in an IDE. The Bootstrap3 grid system
     *     provided by the framework returns the value
     *     `http://isis.apache.org/applib/layout/grid/bootstrap3/bootstrap3.xsd`.
     * </p>
     */
    String schemaLocation();

    /**
     * A default grid, used when no grid layout can be found for the domain
     * class.
     *
     * <p>
     *     For example, this layout could define two columns in ratio 4:8.
     * </p>
     *
     * @param domainClass
     */
    G defaultGrid(Class<?> domainClass);

    /**
     * Validates and normalizes a grid, modifying the grid so that all of the
     * domain object's members (properties, collections, actions) are bound to
     * regions of the grid.
     *
     * <p>
     * This is done using existing metadata, most notably that of the
     * {@link org.apache.isis.applib.annotation.MemberOrder} annotation.
     * Such a grid, if persisted as the layout XML file for the domain class,
     * allows the {@link org.apache.isis.applib.annotation.MemberOrder}
     * annotation to be removed from the source code of the domain class
     * (but other annotations must be retained).
     * </p>
     */
    void normalize(G grid, Class<?> domainClass);

    /**
     * Takes a normalized grid and enriches it with all the available metadata
     * (taken from Apache Isis' internal metadata) that can be represented in
     * the layout XML.
     *
     * <p>
     * Such a grid, if persisted as the layout XML file for the domain class,
     * allows all layout annotations
     * ({@link org.apache.isis.applib.annotation.ActionLayout},
     * {@link org.apache.isis.applib.annotation.PropertyLayout},
     * {@link org.apache.isis.applib.annotation.CollectionLayout}) to be
     * removed from the source code of the domain class.
     *
     * </p>
     * @param grid
     * @param domainClass
     */
    void complete(G grid, Class<?> domainClass);

    /**
     * Takes a normalized grid and strips out removes all members, leaving only
     * the grid structure.
     *
     * <p>
     *     Such a grid, if persisted as the layout XML file for the domain
     *     class, requires that the
     *     {@link org.apache.isis.applib.annotation.MemberOrder} annotation
     *     is retained in the source code of said class in order to bind
     *     members to the regions of the grid.
     * </p>
     *
     * @param grid
     * @param domainClass
     */
    void minimal(G grid, Class<?> domainClass);

}
