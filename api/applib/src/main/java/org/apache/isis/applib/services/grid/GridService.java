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

// tag::refguide[]
public interface GridService {

    // end::refguide[]
    /**
     * Whether dynamic reloading of layouts is enabled.
     */
    // tag::refguide[]
    boolean supportsReloading();

    // end::refguide[]
    /**
     * To support metamodel invalidation/rebuilding of spec.
     */
    // tag::refguide[]
    void remove(Class<?> domainClass);

    // end::refguide[]
    /**
     * Whether any persisted layout metadata (eg a <code>.layout.xml</code> file) exists for this domain class.
     */
    // tag::refguide[]
    boolean existsFor(Class<?> domainClass);

    // end::refguide[]
    /**
     * Returns a new instance of a {@link Grid} for the specified domain class, eg from a
     * <code>layout.xml</code> file, else <code>null</code>.
     */
    // tag::refguide[]
    Grid load(final Class<?> domainClass);

    // end::refguide[]
    /**
     * Returns a default grid; eg where none can be loaded using {@link #load(Class)}.
     */
    // tag::refguide[]
    Grid defaultGridFor(Class<?> domainClass);

    // end::refguide[]
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
    // tag::refguide[]
    Grid normalize(final Grid grid);

    // end::refguide[]
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
    // tag::refguide[]
    Grid complete(Grid grid);

    // end::refguide[]
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
    // tag::refguide[]
    Grid minimal(Grid grid);

    // end::refguide[]
    /**
     * Returns a new instance of a {@link Grid} for the specified domain class, eg from a
     * <code>[domainClass].layout.[layout].xml</code> file, else <code>null</code>.
     */
    // tag::refguide[]
    Grid load(Class<?> domainClass, String layout);

}
// end::refguide[]
