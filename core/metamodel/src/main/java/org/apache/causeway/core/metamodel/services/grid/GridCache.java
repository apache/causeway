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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.layout.resource.LayoutResource;
import org.apache.causeway.applib.mixins.metamodel.Object_rebuildMetamodel;
import org.apache.causeway.applib.services.grid.GridService.LayoutKey;

import lombok.extern.slf4j.Slf4j;

/**
 * Cache for {@link BSGrid} instances,.
 *
 * @since 4.0
 */
@Slf4j
record GridCache(
    Map<LayoutKey, BSGrid> gridsByKey,
    // for better logging messages (used only in prototyping mode)
    Map<LayoutKey, LayoutResource> badLayoutResourceByKey) {

    public GridCache(
            final GridLoadingContext gridLoadingContext) {
        this(
            new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
    }

    /**
     * To support metamodel invalidation/rebuilding of spec.
     * Acts as a no-op if reloading is not supported.
     *
     * <p>This is called by the {@link Object_rebuildMetamodel} mixin action.
     */
    public void remove(final Class<?> domainClass) {
        badLayoutResourceByKey.entrySet().removeIf(entry->entry.getKey().domainClass().equals(domainClass));
        gridsByKey.entrySet().removeIf(entry->entry.getKey().domainClass().equals(domainClass));
    }

    public BSGrid computeIfAbsent(final LayoutKey layoutKey, final Function<LayoutKey, BSGrid> factory) {
        return gridsByKey.computeIfAbsent(layoutKey, factory);
    }

    /**
     * Stores a normalized, validated grid.
     */
    public void putValid(final LayoutKey layoutKey, final BSGrid bsGrid) {
        gridsByKey.put(layoutKey, bsGrid);
    }

    /**
     * Stores a bad {@link LayoutResource}.
     */
    public void putInvalid(final LayoutKey layoutKey, final LayoutResource layoutResource) {
        badLayoutResourceByKey.put(layoutKey, layoutResource);
    }


//    /**
//     * Optionally returns a new instance of a {@link BSGrid},
//     * based on whether the underlying resource could be found, loaded and parsed.
//     *
//     * <p>The layout alternative will typically be specified through a
//     * `layout()` method on the domain object, the value of which is used
//     * for the suffix of the layout file (eg "Customer-layout.archived.xml"
//     * to use a different layout for customers that have been archived).
//     *
//     * @throws UnsupportedOperationException - when format is not supported
//     */
//    public Optional<BSGrid> load(
//            final LayoutKey layoutKey,
//            final @NonNull GridMarshaller marshaller) {
//
//        var supportedFormats = marshaller.supportedFormats();
//
//        var layoutResourceOpt = gridLoader.lookupLayoutResource(layoutKey, supportedFormats);
//        if(layoutResourceOpt.isEmpty()) return Optional.empty();
//
//        var layoutResource = layoutResourceOpt.get();
//
//        if(supportsReloading()) {
//            final String badContent = badContentByKey.get(layoutKey);
//            if(badContent != null) {
//                if(Objects.equals(layoutResource.content(), badContent)) {
//                    // seen this before and already logged; just quit
//                    return Optional.empty();
//                } else {
//                    // this different content might be good
//                    badContentByKey.remove(layoutKey);
//                }
//            }
//        } else {
//            // if cached, serve from cache - otherwise fall through
//            final BSGrid grid = gridsByKey.get(layoutKey);
//            if(grid != null) return Optional.of(grid);
//        }
//
//        try {
//            final BSGrid grid = marshaller
//                .unmarshal(domainClass, layoutResource.content(), layoutResource.format())
//                .getValue().orElseThrow();
//            if(supportsReloading()) {
//                gridsByKey.put(layoutKey, grid);
//            }
//            return Optional.of(grid);
//        } catch(Exception ex) {
//
//            if(supportsReloading()) {
//                // save fact that this was bad content, so that we don't log again if called next time
//                badContentByKey.put(layoutKey, layoutResource.content());
//            }
//
//            // note that we don't blacklist if the file exists but couldn't be parsed;
//            // the developer might fix so we will want to retry.
//            final String resourceName = layoutResource.resourceName();
//            final String message = "Failed to parse " + resourceName + " file (" + ex.getMessage() + ")";
//            if(supportsReloading()) {
//                messageService.warnUser(message);
//            }
//            log.warn(message);
//
//            return Optional.empty();
//        }
//    }

}
