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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.services.grid.GridService;
import org.apache.causeway.applib.services.grid.GridService.LayoutKey;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.object.layout.LayoutPrefixFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Getter @Accessors(fluent = true)
final class BSGridFacet implements GridFacet {

    private final GridService gridService;
    private final Map<String, BSGrid> normalizedGridByLayoutPrefix;
    private final _Lazy<LayoutPrefixFacet> layoutFacetLazy;
    private final @NonNull FacetHolder facetHolder;
    private final @NonNull Facet.Precedence precedence;
	
    // -- FACTORIES

    public static GridFacet create(
            final FacetHolder facetHolder,
            final GridService gridService) {
        return new BSGridFacet(gridService, new ConcurrentHashMap<>(),
            _Lazy.threadSafe(()->facetHolder.getFacet(LayoutPrefixFacet.class)),
            facetHolder, Precedence.DEFAULT);
    }

    // -- METHODS

    @Override public Class<? extends Facet> facetType() { return GridFacet.class; }

    @Override
    public BSGrid getGrid(final @Nullable ManagedObject mo) {
        guardAgainstObjectOfDifferentType(mo);
        return normalized(mo);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        visitor.accept("precedence", precedence().name());
    }

    @Override
    public void clearCache() {
    	normalizedGridByLayoutPrefix.clear();
    }
    
    // -- HELPER

    /**
     * normalizedGridByLayoutPrefix is used as cache, unless gridService.supportsReloading() returns true
     */
    private BSGrid normalized(final @Nullable ManagedObject mo) {
        return normalizedGridByLayoutPrefix.compute(layoutPrefixFor(mo),
            (layoutPrefix, cachedLayout)->
                (cachedLayout==null
                        || gridService.supportsReloading())
                    ? this.load(layoutPrefix)
                    : cachedLayout);
    }

    private void guardAgainstObjectOfDifferentType(final @Nullable ManagedObject objectAdapter) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(objectAdapter)) return; // cannot introspect
        if(!objSpec().equals(objectAdapter.objSpec()))
            throw _Exceptions.unrecoverable(
                    "getGrid(adapter) was called passing an adapter (type: %s), "
                    + "for which this GridFacet (type: %s) is not responsible; "
                    + "indicates that some framework internals are wired up in a wrong way",
                    objectAdapter.objSpec().getCorrespondingClass().getName(),
                    objSpec().getCorrespondingClass().getName());
    }

    private String layoutPrefixFor(final @Nullable ManagedObject objectAdapter) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(objectAdapter)
            || !hasLayoutPrefixFacet())
            return "";
        var layoutName = _Strings.nullToEmpty(layoutFacetLazy.get().layoutPrefix(objectAdapter));
        return layoutName;
    }

    private boolean hasLayoutPrefixFacet() {
        return layoutFacetLazy.get()!=null;
    }

    private BSGrid load(final String layoutPrefix) {
        var domainClass = objSpec().getCorrespondingClass();
        var bsGrid = gridService.load(new LayoutKey(domainClass, layoutPrefix));
        return bsGrid;
    }

    private ObjectSpecification objSpec() {
        return (ObjectSpecification) facetHolder();
    }

    @AllArgsConstructor
    @Getter @Accessors(fluent = true)
    final static class NoLayout implements GridFacet {
    	
        private final @NonNull FacetHolder facetHolder;
        private final @NonNull Facet.Precedence precedence; 
    	
        @Override public Class<? extends Facet> facetType() { return GridFacet.class; }

        @Override public void visitAttributes(final BiConsumer<String, Object> visitor) {
        	FacetUtil.visitAttributes(this, visitor);
        }
        @Override public BSGrid getGrid(@Nullable final ManagedObject mo) {
            return null;
        }
        @Override public void clearCache() {}
    }

}
