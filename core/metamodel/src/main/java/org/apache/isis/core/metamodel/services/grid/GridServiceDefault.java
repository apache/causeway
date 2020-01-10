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
package org.apache.isis.core.metamodel.services.grid;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.services.grid.GridLoaderService;
import org.apache.isis.applib.services.grid.GridService;
import org.apache.isis.applib.services.grid.GridSystemService;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.collections._Lists;

@Service
@Named("isisMetaModel.GridServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class GridServiceDefault implements GridService {

    public static final String COMPONENT_TNS = "http://isis.apache.org/applib/layout/component";
    public static final String COMPONENT_SCHEMA_LOCATION = "http://isis.apache.org/applib/layout/component/component.xsd";

    public static final String LINKS_TNS = "http://isis.apache.org/applib/layout/links";
    public static final String LINKS_SCHEMA_LOCATION = "http://isis.apache.org/applib/layout/links/links.xsd";
    
    @Inject private GridLoaderService gridLoaderService;
    @Inject private List<GridSystemService<?>> gridSystemServices;

    // //////////////////////////////////////

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
        return gridLoaderService.existsFor(domainClass);
    }

    @Override
    public Grid load(final Class<?> domainClass) {
        return gridLoaderService.load(domainClass);
    }

    @Override
    public Grid load(final Class<?> domainClass, final String layout) {
        return gridLoaderService.load(domainClass, layout);
    }

    // //////////////////////////////////////

    @Override
    public Grid defaultGridFor(Class<?> domainClass) {

        for (GridSystemService<?> gridSystemService : gridSystemServices()) {
            Grid grid = gridSystemService.defaultGrid(domainClass);
            if(grid != null) {
                return grid;
            }
        }
        throw new IllegalStateException("No GridSystemService available to create grid for '" + domainClass.getName() + "'");
    }

    @Override
    public Grid normalize(final Grid grid) {

        if(grid.isNormalized()) {
            return grid;
        }

        final Class<?> domainClass = grid.getDomainClass();

        for (GridSystemService<?> gridSystemService : gridSystemServices()) {
            gridSystemService.normalize(_Casts.uncheckedCast(grid), domainClass);
        }

        final String tnsAndSchemaLocation = tnsAndSchemaLocation(grid);
        grid.setTnsAndSchemaLocation(tnsAndSchemaLocation);

        grid.setNormalized(true);

        return grid;
    }

    @Override
    public Grid complete(final Grid grid) {

        final Class<?> domainClass = grid.getDomainClass();
        for (GridSystemService<?> gridSystemService : gridSystemServices()) {
            gridSystemService.complete(_Casts.uncheckedCast(grid), domainClass);
        }

        return grid;
    }

    @Override
    public Grid minimal(final Grid grid) {

        final Class<?> domainClass = grid.getDomainClass();
        for (GridSystemService<?> gridSystemService : gridSystemServices()) {
            gridSystemService.minimal(_Casts.uncheckedCast(grid), domainClass);
        }

        return grid;
    }


    /**
     * Not public API, exposed only for testing.
     */
    public String tnsAndSchemaLocation(final Grid grid) {
        final List<String> parts = _Lists.newArrayList();

        parts.add(COMPONENT_TNS);
        parts.add(COMPONENT_SCHEMA_LOCATION);

        parts.add(LINKS_TNS);
        parts.add(LINKS_SCHEMA_LOCATION);

        for (GridSystemService<?> gridSystemService : getGridSystemServices()) {
            final Class<? extends Grid> gridImpl = gridSystemService.gridImplementation();
            if(gridImpl.isAssignableFrom(grid.getClass())) {
                parts.add(gridSystemService.tns());
                parts.add(gridSystemService.schemaLocation());
            }
        }
        return parts.stream()
                .collect(Collectors.joining(""));
    }

    ////////////////////////////////////////////////////////

    private List<GridSystemService<?>> filteredGridSystemServices;

    /**
     * For all of the available {@link GridSystemService}s available, return only the first one for any that
     * are for the same grid implementation.
     *
     * <p>
     *   This allows default implementations (eg for bootstrap3) to be overridden while also allowing for the more
     *   general idea of multiple implementations.
     * </p>
     */
    protected List<GridSystemService<?>> gridSystemServices() {

        if (filteredGridSystemServices == null) {
            List<GridSystemService<?>> services = _Lists.newArrayList();

            for (GridSystemService<?> gridSystemService : getGridSystemServices()) {
                final Class<?> gridImplementation = gridSystemService.gridImplementation();
                final boolean seenBefore = _NullSafe.stream(services)
                        .anyMatch((GridSystemService<?> systemService) -> 
                        systemService.gridImplementation() == gridImplementation);
                if(!seenBefore) {
                    services.add(gridSystemService);
                }
            }

            filteredGridSystemServices = services;

        }
        return filteredGridSystemServices;
    }

    // -- poor man's testing support

    List<GridSystemService<?>> gridSystemServicesForTest;
    Iterable<GridSystemService<?>> getGridSystemServices() {
        return gridSystemServices!=null 
                ? gridSystemServices
                        : gridSystemServicesForTest;
    }


}
