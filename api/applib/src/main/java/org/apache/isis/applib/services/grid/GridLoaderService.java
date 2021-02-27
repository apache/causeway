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

import javax.annotation.Nullable;

import org.apache.isis.applib.layout.grid.Grid;

/**
 * Provides the ability to load the XML layout (grid) for a domain class.
 *
 * @since 1.x {@index}
 */
public interface GridLoaderService {

    /**
     * Whether dynamic reloading of layouts is enabled.
     *
     * <p>
     *     The default implementation enables reloading for prototyping mode,
     *     disables in production
     * </p>
     */
    boolean supportsReloading();

    /**
     * To support metamodel invalidation/rebuilding of spec.
     *
     * <p>
     *     This is called by the {@link org.apache.isis.applib.mixins.layout.Object_rebuildMetamodel} mixin action.
     * </p>
     */
    void remove(Class<?> domainClass);

    /**
     * Whether any persisted layout metadata (eg a <code>.layout.xml</code> file) exists for this domain class.
     *
     * <p>
     *     If none exists, will return null (and the calling {@link GridService}  will use {@link GridSystemService}
     *     to obtain a default grid for the domain class).
     * </p>
     */
    boolean existsFor(Class<?> domainClass);

    /**
     * Returns a new instance of a {@link Grid} for the specified domain class, eg from a
     * <code>layout.xml</code> file, else <code>null</code>.
     */
    default Grid load(Class<?> domainClass) {
        return load(domainClass, null);
    }

    /**
     * Loads a specic alternative {@link Grid} layout for the specified domain
     * class.
     *
     * <p>
     *     The layout alternative will typically be specified through a
     *     `layout()` method on the domain object, the value of which is used
     *     for the suffix of the layout file (eg "Customer-layout.archived.xml"
     *     to use a different layout for customers that have been archived).
     * </p>
     */
    Grid load(
            Class<?> domainClass,
            @Nullable String layout);

}
