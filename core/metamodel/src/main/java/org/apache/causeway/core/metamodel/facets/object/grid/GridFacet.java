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
package org.apache.causeway.core.metamodel.facets.object.grid;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.layout.grid.Grid;
import org.apache.causeway.applib.services.grid.GridSystemService;
import org.apache.causeway.applib.services.layout.LayoutService;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.object.ManagedObject;

/**
 * Obtain the current grid, derived either from a <code>.layout.xml</code> file, and normalized, or synthesized from
 * existing layout metadata (annotations or <code>layout.json</code>).
 * <p>
 * Most of the heavy lifting is done by delegating to the {@link LayoutService} and {@link GridSystemService} services.
 */
public interface GridFacet extends Facet {

    enum GridVariant {
        /**
         * 'normalized' variant, that can also be exported back to a layout DTO
         */
        NORMALIZED,
        /**
         * Variant, that can NOT be exported back to a layout DTO,
         * as it has presentation specific modifications applied.
         * <p> e.g. removal of empty tabs
         */
        UI;
    }

    Grid getGrid(GridVariant variant, @Nullable ManagedObject mo);
    default Grid getGrid(@Nullable final ManagedObject mo) {
        return getGrid(GridVariant.NORMALIZED, mo);
    }

}
