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

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.services.grid.GridMarshaller;
import org.apache.causeway.applib.services.grid.GridService;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;

/**
 * Default implementation of {@link GridService}.
 *
 * @since 1.x revised for 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".GridServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public record GridServiceDefault(
    GridLoadingContext gridLoadingContext,
    GridObjectMemberResolver gridSystemService,
    GridCache gridCache) implements GridService {

    @Inject
    public GridServiceDefault(
            final GridLoadingContext gridLoadingContext) {
        this(gridLoadingContext, new GridObjectMemberResolver(gridLoadingContext), new GridCache(gridLoadingContext));
    }

    @Override
    public boolean supportsReloading() {
        return gridCache.supportsReloading();
    }

    @Override
    public void remove(final Class<?> domainClass) {
        gridCache.remove(domainClass);
    }

    @Override
    public boolean existsFor(final Class<?> domainClass) {
        return gridCache.existsFor(domainClass, gridLoadingContext.supportedFormats());
    }

    @Deprecated //FIXME bad API
    @Override
    public GridMarshaller marshaller() {
        return gridLoadingContext().marshallersByMime().get(CommonMimeType.XML);
    }

    @Override
    public BSGrid load(final Class<?> domainClass) {
        return gridCache.load(domainClass, marshaller()).orElse(null);
    }

    @Override
    public BSGrid load(final Class<?> domainClass, final String layout) {
        return gridCache.load(domainClass, layout, marshaller()).orElse(null);
    }

    // --

    @Override
    public BSGrid defaultGridFor(final Class<?> domainClass) {

        var grid = gridSystemService.defaultGrid(domainClass);
        if(grid != null) return grid;

        throw new IllegalStateException(
                "No GridSystemService available to create grid for '" + domainClass.getName() + "'");
    }

    @Override
    public BSGrid normalize(final BSGrid grid) {
        if(grid.isNormalized()) return grid;

        var domainClass = grid.domainClass();
        gridSystemService().normalize(_Casts.uncheckedCast(grid), domainClass);

        return grid;
    }

    @Override
    public BSGrid complete(final BSGrid grid) {
        var domainClass = grid.domainClass();
        var gridSystemService = gridSystemService();
        gridSystemService.complete(_Casts.uncheckedCast(grid), domainClass);

        return grid;
    }

    @Override
    public BSGrid minimal(final BSGrid grid) {
        var domainClass = grid.domainClass();
        var gridSystemService = gridSystemService();
        gridSystemService.minimal(_Casts.uncheckedCast(grid), domainClass);

        return grid;
    }

}
