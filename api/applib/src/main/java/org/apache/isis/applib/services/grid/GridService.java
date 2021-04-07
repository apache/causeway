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

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.layout.grid.Grid;

/**
 * Provides the ability to load the XML layout (grid) for a domain class.
 *
 * @since 1.x {@index}
 */
public interface GridService {


    /**
     * Whether dynamic reloading of layouts is enabled.
     *
     * <p>
     *     The default implementation just delegates to the configured
     *     {@link GridLoaderService}; the default implementation of <i>that</i>
     *     service enables reloading wihle prototyping, disables in production.
     * </p>
     */
    boolean supportsReloading();

    /**
     * To support metamodel invalidation/rebuilding of spec.
     *
     * <p>
     *     The default implementation just delegates to the configured
     *     {@link GridLoaderService}.
     * </p>
     */
    void remove(Class<?> domainClass);

    /**
     * Whether any persisted layout metadata (eg a <code>.layout.xml</code> file) exists for this domain class.
     *
     * <p>
     *     The default implementation just delegates to the configured
     *     {@link GridLoaderService}.
     * </p>
     */
    boolean existsFor(Class<?> domainClass);

    /**
     * Returns a new instance of a {@link Grid} for the specified domain class,
     * for example as loaded from a <code>layout.xml</code> file.
     *
     * <p>
     *     If non exists, returns <code>null</code>.  (The caller can then
     *     use {@link GridService#defaultGridFor(Class)} to obtain a
     *     default grid if necessary).
     * </p>
     *
     * <p>
     *     The default implementation just delegates to the configured
     *     {@link GridLoaderService}.
     * </p>
     */
    Grid load(final Class<?> domainClass);

    /**
     * Returns an alternative layout for the domain class.
     *
     * <p>
     *     The alternative layout name can for example be returned by the
     *     domain object's <code>layout()</code> method, whereby - based on the
     *     state of the domain object - it requests a different layout be used.
     * </p>
     *
     * <p>
     *     The default implementation just delegates to the configured
     *     {@link GridLoaderService}; the default implementation of <i>that</i>
     *     service uses the layout name to search for a differently
     *      named layout file, <code>[domainClass].layout.[layout].xml</code>.
     * </p>
     */
    Grid load(Class<?> domainClass, String layout);

    /**
     * Returns a default grid; eg where none can be loaded using {@link #load(Class)}.
     *
     * <p>
     * Used when no existing grid layout exists for a domain class.
     * </p>
     *
     * <p>
     *     The default implementation searches through all available
     *     {@link GridSystemService}s and asks each in turn for a
     *     {@link GridSystemService#defaultGrid(Class) default grid}.
     * </p>
     */
    Grid defaultGridFor(Class<?> domainClass);

    /**
     * Returns a normalized grid for the domain class obtained previously using {@link #load(Class)}.
     *
     * <p>
     *     If a &quot;normalized&quot; grid is persisted as the <code>layout.xml</code>, then the expectation is that
     *     the {@link MemberOrder} annotation can be removed from the domain class
     *     because the binding of properties/collections/actions will be within the XML.  However, the layout
     *     annotations ({@link DomainObjectLayout}, {@link ActionLayout}, {@link PropertyLayout} and
     *     {@link CollectionLayout}) (if present) will continue to be used to provide additional layout metadata.  Of
     *     course, there is nothing to prevent the developer from extending the layout XML to also include the
     *     layout XML (in other words moving towards a {@link #complete(Grid) complete} grid.  Metadata within the
     *     <code>layout.xml</code> file takes precedence over any annotations.
     * </p>
     */
    Grid normalize(final Grid grid);

    /**
     * Modifies the provided {@link Grid} with additional metadata, broadly speaking corresponding to the
     * {@link DomainObjectLayout}, {@link ActionLayout}, {@link PropertyLayout} and {@link CollectionLayout}.
     *
     * <p>
     *     If a &quot;completed&quot; grid is persisted as the <code>layout.xml</code>, then there should be no need
     *     for any of the layout annotations, nor the {@link MemberOrder} annotations,
     *     to be required in the domain class itself.
     * </p>
     */
    Grid complete(Grid grid);

    /**
     * Modifies the provided {@link Grid}, removing all metadata except the basic grid structure.
     *
     * <p>
     *     If a &quot;minimal&quot; grid is persisted as the <code>layout.xml</code>, then the expectation is that
     *     most of the layout annotations ({@link DomainObjectLayout}, {@link ActionLayout}, {@link PropertyLayout},
     *     {@link CollectionLayout}, but also {@link MemberOrder}) will still be retained in the domain class code.
     * </p>
     *
     * @param grid
     */
    Grid minimal(Grid grid);

}
