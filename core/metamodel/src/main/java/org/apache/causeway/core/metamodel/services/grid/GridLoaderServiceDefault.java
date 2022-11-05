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

import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.layout.grid.Grid;
import org.apache.causeway.applib.services.grid.GridLoaderService;
import org.apache.causeway.applib.services.grid.GridMarshallerService;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.commons.internal.reflection._Reflect.InterfacePolicy;
import org.apache.causeway.commons.internal.resources._Resources;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".GridLoaderServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor //JUnit Support
@Log4j2
public class GridLoaderServiceDefault implements GridLoaderService {

    private final MessageService messageService;
    @Getter(onMethod_={@Override}) @Accessors(fluent = true)
    private final boolean supportsReloading;

    @Inject
    public GridLoaderServiceDefault(
           final MessageService messageService,
           final CausewaySystemEnvironment causewaySystemEnvironment) {
        this.messageService = messageService;
        this.supportsReloading = causewaySystemEnvironment.isPrototyping();
    }

    @Value
    static class LayoutKey {
        private final @NonNull Class<?> domainClass;
        private final @Nullable String layoutIfAny; // layout suffix
    }

    @Value
    static class LayoutResource {
        private final @NonNull String resourceName;
        private final @NonNull CommonMimeType format;
        private final @NonNull String content;
    }

    // for better logging messages (used only in prototyping mode)
    private final Map<LayoutKey, String> badContentByKey = _Maps.newHashMap();
    // cache (used only in prototyping mode)
    private final Map<LayoutKey, Grid> gridCache = _Maps.newHashMap();

    @Override
    public void remove(final Class<?> domainClass) {
        if(!supportsReloading()) {
            return;
        }
        final String layoutIfAny = null;
        val layoutKey = new LayoutKey(domainClass, layoutIfAny);
        badContentByKey.remove(layoutKey);
        gridCache.remove(layoutKey);
    }

    @Override
    public boolean existsFor(final Class<?> domainClass, final EnumSet<CommonMimeType> supportedFormats) {
        return loadLayoutResource(new LayoutKey(domainClass, null), supportedFormats).isPresent();
    }

    @Override
    public <T extends Grid> Optional<T> load(
            final Class<?> domainClass,
            final String layoutIfAny,
            final @NonNull GridMarshallerService<T> marshaller) {

        val supportedFormats = marshaller.supportedFormats();

        val layoutKey = new LayoutKey(domainClass, layoutIfAny);
        val layoutResource = loadLayoutResource(layoutKey, supportedFormats).orElse(null);
        if(layoutResource == null) {
            return Optional.empty();
        }

        if(supportsReloading()) {
            final String badContent = badContentByKey.get(layoutKey);
            if(badContent != null) {
                if(Objects.equals(layoutResource.getContent(), badContent)) {
                    // seen this before and already logged; just quit
                    return Optional.empty();
                } else {
                    // this different content might be good
                    badContentByKey.remove(layoutKey);
                }
            }
        } else {
            // if cached, serve from cache - otherwise fall through
            final Grid grid = gridCache.get(layoutKey);
            if(grid != null) {
                return Optional.of((T)grid);
            }
        }

        try {
            final T grid = marshaller
                    .unmarshal(layoutResource.getContent(), layoutResource.getFormat())
                    .getValue().orElseThrow();
            grid.setDomainClass(domainClass);
            if(supportsReloading()) {
                gridCache.put(layoutKey, grid);
            }
            return Optional.of(grid);
        } catch(Exception ex) {

            if(supportsReloading()) {
                // save fact that this was bad content, so that we don't log again if called next time
                badContentByKey.put(layoutKey, layoutResource.getContent());
            }

            // note that we don't blacklist if the file exists but couldn't be parsed;
            // the developer might fix so we will want to retry.
            final String resourceName = layoutResource.getResourceName();
            final String message = "Failed to parse " + resourceName + " file (" + ex.getMessage() + ")";
            if(supportsReloading()) {
                messageService.warnUser(message);
            }
            log.warn(message);

            return Optional.empty();
        }
    }

    // -- HELPER

    Optional<LayoutResource> loadLayoutResource(
            final LayoutKey dcal,
            final EnumSet<CommonMimeType> supportedFormats) {
        return _Reflect.streamTypeHierarchy(dcal.getDomainClass(), InterfacePolicy.EXCLUDE)
        .map(type->loadContent(type, dcal.getLayoutIfAny(), supportedFormats))
        .filter(Optional::isPresent)
        .findFirst()
        .map(Optional::get);
    }

    private Optional<LayoutResource> loadContent(
            final @NonNull Class<?> domainClass,
            final @Nullable String layoutIfAny,
            final EnumSet<CommonMimeType> supportedFormats) {
        return streamResourceNameCandidatesFor(domainClass, layoutIfAny, supportedFormats)
        .map(candidateResourceName->tryLoadLayoutResource(domainClass, candidateResourceName))
        .filter(Optional::isPresent)
        .findFirst()
        .map(Optional::get);
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
        return format.getProposedFileExtensions().stream()
                .flatMap(fileExtension->streamResourceNameCandidatesFor(domainClass, layoutIfAny, fileExtension));
    }

    private Stream<String> streamResourceNameCandidatesFor(
            final @NonNull Class<?> domainClass,
            final @Nullable String layoutIfAny,
            final @NonNull String fileExtension) {

        val typeSimpleName = domainClass.getSimpleName();

        return _Strings.isNotEmpty(layoutIfAny)
                ? Stream.of(
                        String.format("%s-%s.layout.%s", typeSimpleName, layoutIfAny, fileExtension),
                        String.format("%s.layout.%s", typeSimpleName, fileExtension),
                        String.format("%s.layout.fallback.%s", typeSimpleName, fileExtension))
                : Stream.of(
                        String.format("%s.layout.%s", typeSimpleName, fileExtension),
                        String.format("%s.layout.fallback.%s", typeSimpleName,fileExtension));
    }

    private Optional<LayoutResource> tryLoadLayoutResource(
            final @NonNull Class<?> type,
            final @NonNull String candidateResourceName) {
        try {
            return Optional.ofNullable(
                    _Resources.loadAsStringUtf8(type, candidateResourceName))
                    .map(fileContent->new LayoutResource(
                            candidateResourceName,
                            CommonMimeType.valueOfFileName(candidateResourceName).orElseThrow(),
                            fileContent));
        } catch (IOException ex) {
            log.error(
                    "Failed to load layout file {} (relative to {}.class)",
                    candidateResourceName, type.getName(), ex);
        }
        return Optional.empty();
    }

}
