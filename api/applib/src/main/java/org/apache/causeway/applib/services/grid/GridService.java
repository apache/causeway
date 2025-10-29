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

import java.util.EnumSet;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.services.layout.LayoutExportStyle;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;

/**
 * Loads the layout (grid) for any domain class.
 * Also supports various formats {@link LayoutExportStyle} for export.
 *
 * <p> Using DTOs based on <a href="https://getbootstrap.com>Bootstrap</a> design system.
 *
 * @since 1.x revised for 4.0 {@index}
 */
public interface GridService {

    public record LayoutKey(
        @NonNull Class<?> domainClass,
        /**
         *<p>The optional layout name can for example be returned by the
         * domain object's <code>layout()</code> method, whereby - based on the
         * state of the domain object - it requests a different layout be used.
         */
        @Nullable String layoutIfAny) {
    }

    /**
     * Whether dynamic reloading of layouts is enabled.
     *
     * <p> The default implementation enables reloading while prototyping, disables in production.
     */
    boolean supportsReloading();

    /**
     * To support metamodel invalidation/rebuilding of spec.
     *
     * <p> Acts as a no-op if not {@link #supportsReloading()}.
     */
    void invalidate(Class<?> domainClass);

    /**
     * Returns a {@link BSGrid} for given {@link LayoutKey}.
     *
     * <p>The default implementation uses the layout name to search for a differently
     * named layout file, <code>[domainClass].layout.[layout].xml</code>.
     *
     * <p>When no specific grid layout is found returns a generic fallback.
     */
    BSGrid load(LayoutKey layoutKey);

    EnumSet<CommonMimeType> supportedFormats();
    Optional<GridMarshaller> marshaller(CommonMimeType format);

}
