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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.mixins.metamodel.Object_rebuildMetamodel;
import org.apache.causeway.applib.services.grid.GridMarshaller;
import org.apache.causeway.applib.services.grid.GridService;
import org.apache.causeway.applib.services.grid.GridSystemService;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.services.grid.GridLoader.LayoutKey;
import org.apache.causeway.core.metamodel.services.grid.spi.LayoutResourceLoader;

import lombok.extern.slf4j.Slf4j;

/**
 * Cache for {@link BSGrid} instances,
 * delegating grid loading to the {@link GridLoader}.
 *
 * @since 4.0
 */
@Slf4j
record GridCache(
    GridLoader gridLoader,
    MessageService messageService,
    /**
     * Whether dynamic reloading of layouts is enabled.
     *
     * <p> The default implementation enables reloading for prototyping mode,
     * disables in production
     */
    boolean supportsReloading,
    Map<LayoutKey, BSGrid> gridCache,
    // for better logging messages (used only in prototyping mode)
    Map<LayoutKey, String> badContentByKey) {

    public GridCache(
            final MessageService messageService,
            final boolean supportsReloading,
            final List<LayoutResourceLoader> layoutResourceLoaders) {
        this(new GridLoader(Can.ofCollection(layoutResourceLoaders)),
            messageService,
            supportsReloading,
            new HashMap<>(), new HashMap<>());
    }

    /**
     * To support metamodel invalidation/rebuilding of spec.
     * Acts as a no-op if reloading is not supported.
     *
     * <p>This is called by the {@link Object_rebuildMetamodel} mixin action.
     */
    public void remove(final Class<?> domainClass) {
        if(!supportsReloading()) return;

        final String layoutIfAny = null;
        var layoutKey = new LayoutKey(domainClass, layoutIfAny);
        badContentByKey.remove(layoutKey);
        gridCache.remove(layoutKey);
    }

    /**
     * Whether any persisted layout metadata (eg a <code>.layout.xml</code> file) exists for this domain class.
     *
     * <p>If none exists, will return null (and the calling {@link GridService} will use {@link GridSystemService}
     * to obtain a default grid for the domain class).
     */
    public boolean existsFor(final Class<?> domainClass, final EnumSet<CommonMimeType> supportedFormats) {
        return gridLoader.loadLayoutResource(new LayoutKey(domainClass, null), supportedFormats).isPresent();
    }

    /**
     * Optionally returns a new instance of a {@link BSGrid},
     * based on whether the underlying resource could be found, loaded and parsed.
     *
     * <p>The layout alternative will typically be specified through a
     * `layout()` method on the domain object, the value of which is used
     * for the suffix of the layout file (eg "Customer-layout.archived.xml"
     * to use a different layout for customers that have been archived).
     *
     * @throws UnsupportedOperationException - when format is not supported
     */
    public Optional<BSGrid> load(
            final Class<?> domainClass,
            final String layoutIfAny,
            final @NonNull GridMarshaller marshaller) {

        var supportedFormats = marshaller.supportedFormats();

        var layoutKey = new LayoutKey(domainClass, layoutIfAny);
        var layoutResource = gridLoader.loadLayoutResource(layoutKey, supportedFormats).orElse(null);
        if(layoutResource == null) {
            log.debug(
                    "Failed to locate or load layout resource for class {}, "
                    + "with layout-suffix (if any) {}, "
                    + "using layout-resource-loaders {}.",
                    domainClass.getName(), layoutIfAny,
                    gridLoader().layoutResourceLoaders().stream()
                        .map(Object::getClass)
                        .map(Class::getName)
                        .collect(Collectors.joining(", ")));
            return Optional.empty();
        }

        if(supportsReloading()) {
            final String badContent = badContentByKey.get(layoutKey);
            if(badContent != null) {
                if(Objects.equals(layoutResource.content(), badContent)) {
                    // seen this before and already logged; just quit
                    return Optional.empty();
                } else {
                    // this different content might be good
                    badContentByKey.remove(layoutKey);
                }
            }
        } else {
            // if cached, serve from cache - otherwise fall through
            final BSGrid grid = gridCache.get(layoutKey);
            if(grid != null) return Optional.of(grid);
        }

        try {
            final BSGrid grid = marshaller
                .unmarshal(domainClass, layoutResource.content(), layoutResource.format())
                .getValue().orElseThrow();
            if(supportsReloading()) {
                gridCache.put(layoutKey, grid);
            }
            return Optional.of(grid);
        } catch(Exception ex) {

            if(supportsReloading()) {
                // save fact that this was bad content, so that we don't log again if called next time
                badContentByKey.put(layoutKey, layoutResource.content());
            }

            // note that we don't blacklist if the file exists but couldn't be parsed;
            // the developer might fix so we will want to retry.
            final String resourceName = layoutResource.resourceName();
            final String message = "Failed to parse " + resourceName + " file (" + ex.getMessage() + ")";
            if(supportsReloading()) {
                messageService.warnUser(message);
            }
            log.warn(message);

            return Optional.empty();
        }
    }

    /**
     * Optionally returns a new instance of a {@link BSGrid},
     * based on whether the underlying resource could be found, loaded and parsed.
     *
     * @throws UnsupportedOperationException - when format is not supported
     */
    public Optional<BSGrid> load(
            final Class<?> domainClass,
            final @NonNull GridMarshaller marshaller) {
        return load(domainClass, null, marshaller);
    }

}
