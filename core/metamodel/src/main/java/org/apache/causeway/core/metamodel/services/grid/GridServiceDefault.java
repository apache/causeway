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

import java.util.EnumSet;
import java.util.Optional;

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
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;

/**
 * Default implementation of {@link GridService}.
 */
@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".GridServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public record GridServiceDefault(
    GridLoadingContext context,
    LayoutResourceLookup layoutLookup,
    GridLoader loader,
    FallbackGridProvider fallback,
    ObjectMemberResolverForGrid memberResolver,
    GridCache cache) implements GridService {

    @Inject
    public GridServiceDefault(
            final GridLoadingContext gridLoadingContext) {
        this(gridLoadingContext,
            new LayoutResourceLookup(gridLoadingContext.layoutResourceLoaders()),
            new GridLoader(gridLoadingContext),
            new FallbackGridProvider(gridLoadingContext),
            new ObjectMemberResolverForGrid(gridLoadingContext),
            new GridCache(gridLoadingContext));
    }

    @Override
    public EnumSet<CommonMimeType> supportedFormats() {
        return context.supportedFormats();
    }

    @Override
    public Optional<GridMarshaller> marshaller(final CommonMimeType format) {
        return context.gridMarshaller(format);
    }

    @Override
    public boolean supportsReloading() {
        return context.supportsReloading();
    }

    @Override
    public void invalidate(final Class<?> domainClass) {
        if(supportsReloading()) cache.remove(domainClass);
    }

    @Override
    public BSGrid load(final LayoutKey layoutKey) {
        return cache.computeIfAbsent(layoutKey, this::loadNoCache);
    }

    // -- HELPER

    private BSGrid loadNoCache(final LayoutKey layoutKey) {
        return Try.call(()->layoutLookup.lookupLayoutResource(layoutKey, context.supportedFormats()).orElse(null))
            .flatMapSuccessWhenPresent(layoutResource->loader.tryLoad(layoutKey, layoutResource))
            .mapSuccess(gridOpt->gridOpt.orElseGet(()->fallback.defaultGrid(layoutKey.domainClass())))
            // at this point we have a grid
            .mapSuccessWhenPresent(grid->memberResolver.resolve(grid, layoutKey.domainClass()).orElse(null))
            .valueAsNonNullElseFail();
    }

}
