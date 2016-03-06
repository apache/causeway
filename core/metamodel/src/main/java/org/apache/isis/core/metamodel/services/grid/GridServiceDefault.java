/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.services.grid;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.component.Grid;
import org.apache.isis.applib.services.grid.GridSystemService;
import org.apache.isis.applib.services.grid.GridLoaderService;
import org.apache.isis.applib.services.grid.GridService;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class GridServiceDefault implements GridService {

    private static final Logger LOG = LoggerFactory.getLogger(GridServiceDefault.class);

    public static final String COMMON_TNS = "http://isis.apache.org/applib/layout/component";
    public static final String COMMON_SCHEMA_LOCATION = "http://isis.apache.org/applib/layout/component/component.xsd";


    // //////////////////////////////////////

    @Override
    @Programmatic
    public boolean existsFor(final Class<?> domainClass) {
        return gridLoaderService.existsFor(domainClass);
    }

    @Override
    @Programmatic
    public Grid load(final Class<?> domainClass) {
        return gridLoaderService.load(domainClass);
    }

    // //////////////////////////////////////

    @Override
    @Programmatic
    public Grid defaultGridFor(Class<?> domainClass) {

        for (GridSystemService gridSystemService : gridNormalizerServices()) {
            Grid grid = gridSystemService.defaultGrid(domainClass);
            if(grid != null) {
                return grid;
            }
        }
        throw new IllegalStateException("No GridNormalizerService available to create grid for '" + domainClass.getName() + "'");
    }

    @Override
    public Grid normalize(final Grid grid) {

        // if have .layout.json and then add a .layout.xml without restarting, then note that the .layout.xml won't
        // be picked up.  To do so would require normalizing repeatedly in order to trample over the .layout.json's
        // original facets
        if(grid.isNormalized()) {
            return grid;
        }

        final Class<?> domainClass = grid.getDomainClass();

        for (GridSystemService gridSystemService : gridNormalizerServices()) {
            gridSystemService.normalize(grid, domainClass);
        }

        final String tnsAndSchemaLocation = tnsAndSchemaLocation(grid);
        grid.setTnsAndSchemaLocation(tnsAndSchemaLocation);

        grid.setNormalized(true);

        return grid;
    }

    @Override
    @Programmatic
    public Grid complete(final Grid grid) {

        final Class<?> domainClass = grid.getDomainClass();
        for (GridSystemService gridSystemService : gridNormalizerServices()) {
            gridSystemService.complete(grid, domainClass);
        }

        return grid;
    }

    @Override
    @Programmatic
    public Grid minimal(final Grid grid) {

        final Class<?> domainClass = grid.getDomainClass();
        for (GridSystemService gridSystemService : gridNormalizerServices()) {
            gridSystemService.minimal(grid, domainClass);
        }

        return grid;
    }


    /**
     * Not public API, exposed only for testing.
     */
    @Programmatic
    public String tnsAndSchemaLocation(final Grid grid) {
        final List<String> parts = Lists.newArrayList();
        parts.add(COMMON_TNS);
        parts.add(COMMON_SCHEMA_LOCATION);
        FluentIterable.from(gridSystemServices)
                .filter(new Predicate<GridSystemService>() {
                    @Override
                    public boolean apply(final GridSystemService gridSystemService) {
                        final Class<? extends Grid> gridImpl = gridSystemService.gridImplementation();
                        return gridImpl.isAssignableFrom(grid.getClass());
                    }
                })
                .transform(new Function<GridSystemService, Void>() {
                    @Nullable @Override
                    public Void apply(final GridSystemService gridSystemService) {
                        parts.add(gridSystemService.tns());
                        parts.add(gridSystemService.schemaLocation());
                        return null;
                    }
                });
        return Joiner.on(" ").join(parts);
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
    @Programmatic
    protected List<GridSystemService<?>> gridNormalizerServices() {

        if (filteredGridSystemServices == null) {
            List<GridSystemService<?>> services = Lists.newArrayList();

            for (GridSystemService gridSystemService : this.gridSystemServices) {
                final Class gridImplementation = gridSystemService.gridImplementation();
                final boolean notSeenBefore = FluentIterable.from(services).filter(new Predicate<GridSystemService<?>>() {
                    @Override public boolean apply(@Nullable final GridSystemService<?> gridNormalizerService) {
                        return gridNormalizerService.gridImplementation() == gridImplementation;
                    }
                }).isEmpty();
                if(notSeenBefore) {
                    services.add(gridSystemService);
                }
            }

            filteredGridSystemServices = services;

        }
        return filteredGridSystemServices;
    }

    ////////////////////////////////////////////////////////



    @Inject
    GridLoaderService gridLoaderService;

    @Inject
    List<GridSystemService> gridSystemServices;

}
