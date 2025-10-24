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
package org.apache.causeway.core.metamodel.services.grid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.xml.bind.Marshaller;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.layout.grid.Grid;
import org.apache.causeway.applib.services.grid.GridSystemService;

@Service
public record XsiSchemaLocationProviderForGrid(List<GridSystemService<? extends Grid>> gridSystemServices) {

    static final String COMPONENT_TNS = "https://causeway.apache.org/applib/layout/component";
    static final String COMPONENT_SCHEMA_LOCATION = "https://causeway.apache.org/applib/layout/component/component.xsd";

    static final String LINKS_TNS = "https://causeway.apache.org/applib/layout/links";
    static final String LINKS_SCHEMA_LOCATION = "https://causeway.apache.org/applib/layout/links/links.xsd";

    public XsiSchemaLocationProviderForGrid {
        final var gridImplementations = new HashSet<Class<?>>();
        /*
         * For all of the {@link GridSystemService}s available, return only the first one for any that
         * are for the same grid implementation.
         *
         * <p>This allows default implementations (eg for bootstrap3) to be overridden while also allowing for the more
         * general idea of multiple implementations.
         */
        gridSystemServices = gridSystemServices
                .stream()
                // true only if gridImplementations did not already contain the specified element
                .filter(gridService->gridImplementations.add(gridService.gridImplementation()))
                .toList();
    }

    /**
     * @see Marshaller#JAXB_SCHEMA_LOCATION
     */
    public String xsiSchemaLocation(final Class<? extends Grid> gridClass) {
        var parts = new ArrayList<String>();

        parts.add(COMPONENT_TNS);
        parts.add(COMPONENT_SCHEMA_LOCATION);

        parts.add(LINKS_TNS);
        parts.add(LINKS_SCHEMA_LOCATION);

        for (var gridSystemService : gridSystemServices()) {
            var gridImpl = gridSystemService.gridImplementation();
            if(gridImpl.isAssignableFrom(gridClass)) {
                parts.add(gridSystemService.tns());
                parts.add(gridSystemService.schemaLocation());
            }
        }
        return parts.stream()
                .collect(Collectors.joining(" "));
    }
}
