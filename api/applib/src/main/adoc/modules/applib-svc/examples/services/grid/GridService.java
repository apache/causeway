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
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.layout.grid.Grid;

public interface GridService {

    /**
     * Whether dynamic reloading of layouts is enabled.
     */
    @Programmatic
    boolean supportsReloading();

    /**
     * To support metamodel invalidation/rebuilding of spec.
     */
    @Programmatic
    void remove(Class<?> domainClass);

    /**
     * Whether any persisted layout metadata (eg a <code>.layout.xml</code> file) exists for this domain class.
     */
    @Programmatic
    boolean existsFor(Class<?> domainClass);

    /**
     * Returns a new instance of a {@link Grid} for the specified domain class, eg from a
     * <code>layout.xml</code> file, else <code>null</code>.
     */
    @Programmatic
    Grid load(final Class<?> domainClass);

    /**
     * Returns a default grid; eg where none can be loaded using {@link #load(Class)}.
     */
    @Programmatic
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
    @Programmatic
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
    @Programmatic
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
     * @return
     */
    @Programmatic
    Grid minimal(Grid grid);

    /**
     * Returns a new instance of a {@link Grid} for the specified domain class, eg from a
     * <code>[domainClass].layout.[layout].xml</code> file, else <code>null</code>.
     */
    @Programmatic
    Grid load(Class<?> domainClass, String layout);


}
