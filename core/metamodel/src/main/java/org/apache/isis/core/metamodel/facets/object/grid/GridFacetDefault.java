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
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.layout.LayoutFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
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

    private final _Lazy<LayoutFacet> layoutFacetLazy = _Lazy.threadSafe(()->
        getFacetHolder().getFacet(LayoutFacet.class));
    

    private final Map<String, Grid> gridByLayoutName = new HashMap<>();
    
    private GridFacetDefault(
            final FacetHolder facetHolder,
            final GridService gridService) {
        super(GridFacetDefault.type(), facetHolder, Derivation.NOT_DERIVED);
        this.gridService = gridService;
    }

    @Override
    public Grid getGrid(final @Nullable ManagedObject objectAdapter) {
        
        guardAgainstObjectOfDifferentType(objectAdapter);
        
        // gridByLayoutName is used as cache, unless gridService.supportsReloading() returns true
        return gridByLayoutName.compute(layoutNameFor(objectAdapter), 
                (layoutName, cachedLayout)->
                    (cachedLayout==null
                            || gridService.supportsReloading())
                    ? this.load(layoutName)
                    : cachedLayout
        );
        
    }
    
    // -- HELPER
    
    private void guardAgainstObjectOfDifferentType(final @Nullable ManagedObject objectAdapter) {
        
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(objectAdapter)) {
            return; // cannot introspect
        }
        
        if(!getSpecification().equals(objectAdapter.getSpecification())) {
            throw _Exceptions.unrecoverableFormatted(
                    "getGrid(adapter) was called passing an adapter (specId: %s), "
                    + "for which this GridFacet (specId: %s) is not responsible; "
                    + "indicates that some framework internals are wired up in a wrong way",
                    objectAdapter.getSpecification().getSpecId().asString(),
                    getSpecification().getSpecId().asString());
        }
    }

    private String layoutNameFor(final @Nullable ManagedObject objectAdapter) {
        if(!hasLayoutFacet()
                || ManagedObjects.isNullOrUnspecifiedOrEmpty(objectAdapter)) {
            return "";
        }
        return _Strings.nullToEmpty(layoutFacetLazy.get().layout(objectAdapter));
    }
    
    private boolean hasLayoutFacet() {
        return layoutFacetLazy.get()!=null;
    }

    private Grid load(final @Nullable String layoutName) {
        
        val domainClass = getSpecification().getCorrespondingClass();

        val grid = Optional.ofNullable(
                // loads from object's XML if available
                gridService.load(domainClass, layoutName)) 
                // loads from default-XML if available
                .orElseGet(()->gridService.defaultGridFor(domainClass)); 
        return gridService.normalize(grid);
    }

    private ObjectSpecification getSpecification() {
        return (ObjectSpecification) getFacetHolder();
    }

}
