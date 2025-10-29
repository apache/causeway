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
import org.apache.causeway.applib.layout.resource.LayoutResource;
import org.apache.causeway.applib.services.grid.GridMarshaller;
import org.apache.causeway.applib.services.grid.GridService;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;

import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of {@link GridService}.
 */
@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".GridServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Slf4j
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

    // e.g. triggered by {@link Object_rebuildMetamodel} mixin action
    @Override
    public void invalidate(final Class<?> domainClass) {
        if(supportsReloading()) {
            layoutLookup.unmarkInvalid(domainClass);
            cache.remove(domainClass);
        }
    }

    @Override
    public BSGrid load(final LayoutKey layoutKey) {
        return cache.computeIfAbsent(layoutKey, this::tryLoadNoCache)
            .valueAsNonNullElseFail(); // at least we should have a fallback, otherwise there is some serious issue
    }

    // -- HELPER

    private Try<BSGrid> tryLoadNoCache(final LayoutKey layoutKey) {
        return Try.call(()->layoutLookup.lookupLayoutResource(layoutKey, context.supportedFormats()).orElse(null))
            // on the success rail we optionally have a LayoutResource
            .flatMapSuccessWhenPresent(layoutResource->loader.tryLoad(layoutKey, layoutResource)
                .ifFailure(ex->onFailureWhileLoadingResource(layoutKey, layoutResource, ex)))
            // on the success rail we optionally have a raw BSGrid (not yet validated), if present validate
            .mapSuccessWhenPresent(grid->memberResolver.resolve(grid, layoutKey.domainClass())
                .<BSGrid>fold(a->a, b->{ onValidationFailure(layoutKey, b); return null; }))
            // on the success rail we optionally have a valid BSGrid, if absent use fallback and validate
            .mapSuccess(gridOpt->gridOpt.orElseGet(()->memberResolver.resolve(
                fallback.defaultGrid(layoutKey.domainClass()), layoutKey.domainClass())
                    .<BSGrid>fold(a->a, b->{ onFallbackValidationFailure(b); return null; })));
    }

    private void onFailureWhileLoadingResource(
            final LayoutKey layoutKey,
            final LayoutResource layoutResource,
            final Throwable ex) {
        layoutLookup.markInvalid(layoutKey);
        final String message = "Failed to parse %s, cause (%s)"
            .formatted(layoutResource.resourceName(), ex.getMessage());
        if(context.causewaySystemEnvironment().isPrototyping()) {
            context.messageService().warnUser(message);
        }
        log.warn(message);
    }

    private void onValidationFailure(final LayoutKey layoutKey, final BSGrid bsGrid) {
        layoutLookup.markInvalid(layoutKey);
        if(context.causewaySystemEnvironment().isPrototyping()) {
            context.messageService().warnUser("Grid metadata errors for " + bsGrid.domainClass().getName() + "; check the error log");
        }
        log.error("Grid metadata errors in {}:\n\n{}\n\n", bsGrid.domainClass().getName(), toXml(bsGrid));
    }

    private void onFallbackValidationFailure(final BSGrid bsGrid) {
        if(context.causewaySystemEnvironment().isPrototyping()) {
            context.messageService().warnUser("Grid metadata errors for " + bsGrid.domainClass().getName() + "; check the error log");
        }
        log.error("Grid metadata errors in {}:\n\n{}\n\n", bsGrid.domainClass().getName(), toXml(bsGrid));
    }

    private String toXml(final BSGrid grid) {
        return context().gridMarshaller(CommonMimeType.XML).orElseThrow()
            .marshal(grid, CommonMimeType.XML);
    }

}
