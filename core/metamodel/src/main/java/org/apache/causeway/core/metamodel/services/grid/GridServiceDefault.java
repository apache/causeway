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
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.services.grid.GridMarshaller;
import org.apache.causeway.applib.services.grid.GridService;
import org.apache.causeway.applib.services.grid.GridSystemService;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.services.grid.spi.LayoutResourceLoader;

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
    GridMarshaller marshaller,
    List<GridSystemService> gridSystemServices,
    GridCache gridCache) implements GridService {

    @Inject
    public GridServiceDefault(
            final CausewaySystemEnvironment causewaySystemEnvironment,
            final GridMarshaller marshaller,
            final MessageService messageService,
            final List<GridSystemService> gridSystemServices,
            final List<LayoutResourceLoader> layoutResourceLoaders) {
        this(marshaller, gridSystemServices, new GridCache(messageService, causewaySystemEnvironment.isPrototyping(), layoutResourceLoaders));
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
        return gridCache.existsFor(domainClass, marshaller.supportedFormats());
    }

    @Override
    public BSGrid load(final Class<?> domainClass) {
        return gridCache.load(domainClass, marshaller).orElse(null);
    }

    @Override
    public BSGrid load(final Class<?> domainClass, final String layout) {
        return gridCache.load(domainClass, layout, marshaller).orElse(null);
    }

    // --

    @Override
    public BSGrid defaultGridFor(final Class<?> domainClass) {
        for (var gridSystemService : gridSystemServices()) {
            var grid = gridSystemService.defaultGrid(domainClass);
            if(grid != null) return grid;
        }
        throw new IllegalStateException(
                "No GridSystemService available to create grid for '" + domainClass.getName() + "'");
    }

    @Override
    public BSGrid normalize(final BSGrid grid) {
        if(grid.isNormalized()) return grid;

        var domainClass = grid.domainClass();
        for (var gridSystemService : gridSystemServices()) {
            gridSystemService.normalize(_Casts.uncheckedCast(grid), domainClass);
        }
        return grid;
    }

    @Override
    public BSGrid complete(final BSGrid grid) {
        var domainClass = grid.domainClass();
        for (var gridSystemService : gridSystemServices()) {
            gridSystemService.complete(_Casts.uncheckedCast(grid), domainClass);
        }
        return grid;
    }

    @Override
    public BSGrid minimal(final BSGrid grid) {
        var domainClass = grid.domainClass();
        for (var gridSystemService : gridSystemServices()) {
            gridSystemService.minimal(_Casts.uncheckedCast(grid), domainClass);
        }
        return grid;
    }

}
