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

import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.services.layout.LayoutExportStyle;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

/**
 * Loads the layout (grid) for any domain class.
 * Also supports various formats {@link LayoutExportStyle} for export.
 *
 * @since 1.x revised for 4.0 {@index}
 */
public interface GridService {

    public record LayoutKey(
        @NonNull Class<?> domainClass,
        /** layout suffix */
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
     * Returns a normalized grid for the domain class.
     *
     * <p>The alternative layout name can for example be returned by the
     * domain object's <code>layout()</code> method, whereby - based on the
     * state of the domain object - it requests a different layout be used.
     *
     * <p>The default implementation uses the layout name to search for a differently
     * named layout file, <code>[domainClass].layout.[layout].xml</code>.
     *
     * <p>When no specific grid layout is found returns a generic fallback.
     *
     * <p>If a 'normalized' grid is persisted as the <code>layout.xml</code>, then the expectation is that
     * any ordering metadata from layout annotations can be removed from the domain class
     * because the binding of properties/collections/actions will be within the XML.  However, the layout
     * annotations ({@link DomainObjectLayout}, {@link ActionLayout}, {@link PropertyLayout} and
     * {@link CollectionLayout}) (if present) will continue to be used to provide additional layout metadata.  Of
     * course, there is nothing to prevent the developer from extending the layout XML to also include the
     * layout XML (in other words moving towards a {@link #complete(BSGrid) complete} grid.  Metadata within the
     * <code>layout.xml</code> file takes precedence over any annotations.
     */
    BSGrid loadAndNormalize(LayoutKey layoutKey);

    /**
     * Modifies the provided {@link BSGrid} with additional metadata, broadly speaking corresponding to the
     * {@link DomainObjectLayout}, {@link ActionLayout}, {@link PropertyLayout} and {@link CollectionLayout}.
     *
     * <p>If a 'complete' grid is persisted as the <code>layout.xml</code>, then there should be no need
     * for any of the layout annotations,
     * to be required in the domain class itself.
     */
    BSGrid complete(BSGrid grid);

    /**
     * Modifies the provided {@link BSGrid}, removing all metadata except the basic grid structure.
     *
     * <p>If a 'minimal' grid is persisted as the <code>layout.xml</code>, then the expectation is that
     * most of the layout annotations ({@link DomainObjectLayout}, {@link ActionLayout}, {@link PropertyLayout},
     * {@link CollectionLayout} will still be retained in the domain class code.
     *
     */
    BSGrid minimal(BSGrid grid);

    // -- LAYOUT EXPORT

    EnumSet<CommonMimeType> supportedFormats();
    Optional<GridMarshaller> marshaller(CommonMimeType format);

    default BSGrid toGridForExport(
            final Class<?> domainClass,
            final LayoutExportStyle style) {

        var grid = loadAndNormalize(new LayoutKey(domainClass, null));

        if (style == LayoutExportStyle.COMPLETE) return complete(grid);
        if (style == LayoutExportStyle.MINIMAL) return minimal(grid);

        throw _Exceptions.unmatchedCase(style);
    }

}
