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

import java.util.List;

import jakarta.annotation.Priority;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.layout.grid.Grid;
import org.apache.causeway.applib.services.grid.GridLoaderService;
import org.apache.causeway.applib.services.grid.GridMarshallerService;
import org.apache.causeway.applib.services.grid.GridService;
import org.apache.causeway.applib.services.grid.GridSystemService;
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
    GridLoaderService gridLoaderService,
    GridMarshallerService<? extends Grid> marshaller,
    List<GridSystemService<? extends Grid>> gridSystemServices) implements GridService {

    @Override
    public boolean supportsReloading() {
        return gridLoaderService.supportsReloading();
    }

    @Override
    public void remove(final Class<?> domainClass) {
        gridLoaderService.remove(domainClass);
    }

    @Override
    public boolean existsFor(final Class<?> domainClass) {
        return gridLoaderService.existsFor(domainClass, marshaller.supportedFormats());
    }

    @Override
    public Grid load(final Class<?> domainClass) {
        return gridLoaderService.load(domainClass, marshaller).orElse(null);
    }

    @Override
    public Grid load(final Class<?> domainClass, final String layout) {
        return gridLoaderService.load(domainClass, layout, marshaller).orElse(null);
    }

    // --

    @Override
    public Grid defaultGridFor(final Class<?> domainClass) {
        for (var gridSystemService : gridSystemServices()) {
            var grid = gridSystemService.defaultGrid(domainClass);
            if(grid != null) return grid;
        }
        throw new IllegalStateException(
                "No GridSystemService available to create grid for '" + domainClass.getName() + "'");
    }

    @Override
    public Grid normalize(final Grid grid) {
        if(grid.isNormalized()) return grid;

        var domainClass = grid.domainClass();
        for (var gridSystemService : gridSystemServices()) {
            gridSystemService.normalize(_Casts.uncheckedCast(grid), domainClass);
        }
        return grid;
    }

    @Override
    public Grid complete(final Grid grid) {
        var domainClass = grid.domainClass();
        for (var gridSystemService : gridSystemServices()) {
            gridSystemService.complete(_Casts.uncheckedCast(grid), domainClass);
        }
        return grid;
    }

    @Override
    public Grid minimal(final Grid grid) {
        var domainClass = grid.domainClass();
        for (var gridSystemService : gridSystemServices()) {
            gridSystemService.minimal(_Casts.uncheckedCast(grid), domainClass);
        }
        return grid;
    }

}
