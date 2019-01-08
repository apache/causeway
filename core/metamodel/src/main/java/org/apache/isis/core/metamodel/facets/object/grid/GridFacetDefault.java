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
package org.apache.isis.core.metamodel.facets.object.grid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.services.grid.GridService2;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.layout.LayoutFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class GridFacetDefault
            extends FacetAbstract
            implements GridFacet {

    private static final Logger LOG = LoggerFactory.getLogger(GridFacetDefault.class);


    public static Class<? extends Facet> type() {
        return GridFacet.class;
    }


    public static GridFacet create(
            final FacetHolder facetHolder,
            final GridService2 gridService) {
        return new GridFacetDefault(facetHolder, gridService);
    }

    private final GridService2 gridService;

    private Grid grid;

    private GridFacetDefault(
            final FacetHolder facetHolder,
            final GridService2 gridService) {
        super(GridFacetDefault.type(), facetHolder, Derivation.NOT_DERIVED);
        this.gridService = gridService;
    }

    public Grid getGrid(final ObjectAdapter objectAdapterIfAny) {
        if (!gridService.supportsReloading() && this.grid != null) {
            return this.grid;
        }
        final Class<?> domainClass = getSpecification().getCorrespondingClass();
        final LayoutFacet layoutFacet = getFacetHolder().getFacet(LayoutFacet.class);
        final String layout = layoutFacet != null ? layoutFacet.layout(objectAdapterIfAny) : null;
        this.grid = load(domainClass, layout);

        return this.grid;
    }

    private Grid load(final Class<?> domainClass, final String layout) {
        Grid grid = gridService.load(domainClass, layout);
        if(grid == null) {
            grid = gridService.defaultGridFor(domainClass);
        }
        gridService.normalize(grid);
        return grid;
    }

    private ObjectSpecification getSpecification() {
        return (ObjectSpecification) getFacetHolder();
    }

}
