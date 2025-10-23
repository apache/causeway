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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.grid.Grid;
import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.services.grid.GridService;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.layout.LayoutPrefixFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

record GridFacetDefault(
    GridService gridService,
    Map<String, Grid> gridByLayoutPrefix,
    _Lazy<LayoutPrefixFacet> layoutFacetLazy,
    @NonNull FacetHolder facetHolder,
    Facet.@NonNull Precedence precedence)
implements GridFacet {

    // -- FACTORIES

    public static GridFacet create(
            final FacetHolder facetHolder,
            final GridService gridService) {
        return new GridFacetDefault(gridService, new ConcurrentHashMap<>(),
            _Lazy.threadSafe(()->facetHolder.getFacet(LayoutPrefixFacet.class)),
            facetHolder, Precedence.DEFAULT);
    }

    // -- METHODS

    @Override public Class<? extends Facet> facetType() { return GridFacet.class; }
    @Override public Precedence getPrecedence() { return precedence(); }
    @Override public FacetHolder getFacetHolder() { return facetHolder(); }

    @Override
    public Grid getGrid(final @Nullable ManagedObject mo) {
        guardAgainstObjectOfDifferentType(mo);

        // gridByLayoutName is used as cache, unless gridService.supportsReloading() returns true
        var grid = gridByLayoutPrefix.compute(layoutPrefixFor(mo),
            (layoutPrefix, cachedLayout)->
                (cachedLayout==null
                        || gridService.supportsReloading())
                ? this.load(layoutPrefix)
                : cachedLayout);

        _Casts.castTo(BSGrid.class, grid)
            .ifPresent(bsGrid->attachAssociatedActions(bsGrid, mo));

        return grid;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        visitor.accept("precedence", getPrecedence().name());
    }

    // -- HELPER

    private void attachAssociatedActions(BSGrid bsGrid, @Nullable ManagedObject mo) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(mo)) return;

        var primedActions = bsGrid.getAllActionsById();
        final Set<String> actionIdsAlreadyAdded = _Sets.newHashSet(primedActions.keySet());

        mo.objSpec().streamProperties(MixedIn.INCLUDED)
        .forEach(property->{
            Optional.ofNullable(
                bsGrid.getAllPropertiesById().get(property.getId()))
            .ifPresent(pl->{
                ObjectAction.Util.findForAssociation(mo, property)
                    .map(action->action.getId())
                    .filter(id->!actionIdsAlreadyAdded.contains(id))
                    .peek(actionIdsAlreadyAdded::add)
                    .map(ActionLayoutData::new)
                    .forEach(pl.getActions()::add);
            });

        });
    }

    private void guardAgainstObjectOfDifferentType(final @Nullable ManagedObject objectAdapter) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(objectAdapter)) return; // cannot introspect
        if(!getSpecification().equals(objectAdapter.objSpec())) {
            throw _Exceptions.unrecoverable(
                    "getGrid(adapter) was called passing an adapter (type: %s), "
                    + "for which this GridFacet (type: %s) is not responsible; "
                    + "indicates that some framework internals are wired up in a wrong way",
                    objectAdapter.objSpec().getCorrespondingClass().getName(),
                    getSpecification().getCorrespondingClass().getName());
        }
    }

    private String layoutPrefixFor(final @Nullable ManagedObject objectAdapter) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(objectAdapter)
            || !hasLayoutPrefixFacet()) {
            return "";
        }
        var layoutName = _Strings.nullToEmpty(layoutFacetLazy.get().layoutPrefix(objectAdapter));
        return layoutName;
    }

    private boolean hasLayoutPrefixFacet() {
        return layoutFacetLazy.get()!=null;
    }

    private Grid load(final @NonNull String layoutPrefix) {
        var domainClass = getSpecification().getCorrespondingClass();
        var grid = Optional.ofNullable(
                // loads from object's XML if available
                gridService.load(domainClass, _Strings.emptyToNull(layoutPrefix)))
                // loads from default-XML if available
                .orElseGet(()->gridService.defaultGridFor(domainClass));
        return gridService.normalize(grid);
    }

    private ObjectSpecification getSpecification() {
        return (ObjectSpecification) getFacetHolder();
    }

}
