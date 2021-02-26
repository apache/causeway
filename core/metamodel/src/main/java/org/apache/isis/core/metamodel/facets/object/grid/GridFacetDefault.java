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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.services.grid.GridService;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.layout.LayoutFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.val;

public class GridFacetDefault
extends FacetAbstract
implements GridFacet {

    public static Class<? extends Facet> type() {
        return GridFacet.class;
    }


    public static GridFacet create(
            final FacetHolder facetHolder,
            final GridService gridService) {
        return new GridFacetDefault(facetHolder, gridService);
    }

    private final GridService gridService;

    private final Map<String, Grid> grid = new HashMap<>();
    
    private boolean hasLayoutFacet = false;

    private GridFacetDefault(
            final FacetHolder facetHolder,
            final GridService gridService) {
        super(GridFacetDefault.type(), facetHolder, Derivation.NOT_DERIVED);
        this.gridService = gridService;
    }

    @Override
    public Grid getGrid(final ManagedObject objectAdapterIfAny) {
        if (this.hasLayoutFacet || grid.isEmpty() || gridService.supportsReloading()) {
            val domainClass = getSpecification().getCorrespondingClass();
            final String layout = layout(objectAdapterIfAny);
            if(! grid.containsKey(layout) || gridService.supportsReloading()) {
            	grid.put(layout, load(domainClass, layout));
            }
            return grid.get(layout);
        }
        return grid.get(null);
    }
    
    // -- HELPER
    
    @Nullable
    private String layout(@Nullable final ManagedObject objectAdapterIfAny) {
        if(objectAdapterIfAny == null) {
            return null;
        }
        val facetHolder = getFacetHolder();
        val layoutFacet = facetHolder.getFacet(LayoutFacet.class);
        if(layoutFacet == null) {
            return null;
        }
        this.hasLayoutFacet = true;
        return layoutFacet.layout(objectAdapterIfAny);
    }

    private Grid load(final Class<?> domainClass, final String layout) {
        val grid = Optional.ofNullable(gridService.load(domainClass, layout)) //loads from object's XML if available
                .orElseGet(()->gridService.defaultGridFor(domainClass)); //loads from default-XML if available
        
        gridService.normalize(grid);
        return grid;
    }

    private ObjectSpecification getSpecification() {
        return (ObjectSpecification) getFacetHolder();
    }

}
