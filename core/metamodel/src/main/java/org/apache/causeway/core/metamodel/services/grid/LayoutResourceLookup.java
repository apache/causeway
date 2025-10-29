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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.layout.resource.LayoutResource;
import org.apache.causeway.applib.layout.resource.LayoutResourceLoader;
import org.apache.causeway.applib.services.grid.GridService.LayoutKey;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.commons.internal.reflection._Reflect.InterfacePolicy;

import lombok.extern.slf4j.Slf4j;

/**
 * Finds {@link LayoutResource}(s) based on domainClass and layout-suffix,
 * by probing possible name candidates against the class-path or other sources (SPI).
 *
 * @since 4.0
 */
@Slf4j
record LayoutResourceLookup(
        Can<LayoutResourceLoader> layoutResourceLoaders,
        /**
         * In effect is used as a Set<LayoutKey>. (there is no concurrent hash set)
         */
        Map<LayoutKey, LayoutKey> knownInvalidKeys) {

    public LayoutResourceLookup(
            final Can<LayoutResourceLoader> layoutResourceLoaders) {
        this(layoutResourceLoaders, new ConcurrentHashMap<>());
    }

    /**
     * if known bad - don't load
     */
    public Optional<LayoutResource> lookupLayoutResource(
            final LayoutKey layoutKey,
            final EnumSet<CommonMimeType> supportedFormats) {

        if(isKnownInvalid(layoutKey)) return Optional.empty();

        var layoutResourceOpt = _Reflect.streamTypeHierarchy(layoutKey.domainClass(), InterfacePolicy.EXCLUDE)
            .flatMap(type->loadContent(type, layoutKey.layoutIfAny(), supportedFormats).stream())
            .findFirst();

        if(layoutResourceOpt.isPresent()) return layoutResourceOpt;

        log.debug(
            "Failed to locate or load layout resource for class {}, "
            + "with layout-suffix (if any) {}, "
            + "using layout-resource-loaders {}.",
            layoutKey.domainClass().getName(), layoutKey.layoutIfAny(),
            layoutResourceLoaders().stream()
                .map(Object::getClass)
                .map(Class::getName)
                .collect(Collectors.joining(", ")));

        return Optional.empty();
    }

    boolean isKnownInvalid(final LayoutKey layoutKey) {
        return knownInvalidKeys.get(layoutKey)!=null;
    }

    public void markInvalid(final LayoutKey layoutKey) {
        knownInvalidKeys.put(layoutKey, layoutKey);
    }

    /**
     * To support metamodel invalidation/rebuilding of spec.
     */
    public void unmarkInvalid(final Class<?> domainClass) {
        knownInvalidKeys.entrySet().removeIf(entry->entry.getKey().domainClass().equals(domainClass));
    }

    // -- HELPER

    private Optional<LayoutResource> loadContent(
            final @NonNull Class<?> domainClass,
            final @Nullable String layoutIfAny,
            final EnumSet<CommonMimeType> supportedFormats) {
        return streamResourceNameCandidatesFor(domainClass, layoutIfAny, supportedFormats)
            .flatMap(candidateResourceName->lookupLayoutResourceUsingLoaders(domainClass, candidateResourceName).stream())
            .findFirst();
    }

    private Stream<String> streamResourceNameCandidatesFor(
            final @NonNull Class<?> domainClass,
            final @Nullable String layoutIfAny,
            final @NonNull  EnumSet<CommonMimeType> supportedFormats) {
        return supportedFormats.stream()
                .flatMap(format->streamResourceNameCandidatesFor(domainClass, layoutIfAny, format));
    }

    private Stream<String> streamResourceNameCandidatesFor(
            final @NonNull Class<?> domainClass,
            final @Nullable String layoutIfAny,
            final @NonNull CommonMimeType format) {
        return format.proposedFileExtensions().stream()
                .flatMap(fileExtension->streamResourceNameCandidatesFor(domainClass, layoutIfAny, fileExtension));
    }

    private Stream<String> streamResourceNameCandidatesFor(
            final @NonNull Class<?> domainClass,
            final @Nullable String layoutIfAny,
            final @NonNull String fileExtension) {

        var typeSimpleName = domainClass.getSimpleName();

        return _Strings.isNotEmpty(layoutIfAny)
            ? Stream.of(
                    String.format("%s-%s.layout.%s", typeSimpleName, layoutIfAny, fileExtension),
                    String.format("%s.layout.%s", typeSimpleName, fileExtension),
                    String.format("%s.layout.fallback.%s", typeSimpleName, fileExtension))
            : Stream.of(
                    String.format("%s.layout.%s", typeSimpleName, fileExtension),
                    String.format("%s.layout.fallback.%s", typeSimpleName,fileExtension));
    }

    private Optional<LayoutResource> lookupLayoutResourceUsingLoaders(
            final @NonNull Class<?> type,
            final @NonNull String candidateResourceName) {

        return layoutResourceLoaders.stream()
            .flatMap(loader->loader.lookupLayoutResource(type, candidateResourceName).stream())
            .findFirst();
    }

}
