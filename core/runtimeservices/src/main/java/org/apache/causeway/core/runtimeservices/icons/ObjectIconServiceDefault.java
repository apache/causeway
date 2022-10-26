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
package org.apache.causeway.core.runtimeservices.icons;

import java.net.URL;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.resources._Resources;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIcon;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIconService;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".ObjectIconServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ObjectIconServiceDefault
implements ObjectIconService {

    private static final String DEFAULT_IMAGE_RESOURCE_PATH = "classpath:images";
    private static final Can<CommonMimeType> IMAGE_TYPES =
            Can.of(
                CommonMimeType.PNG,
                CommonMimeType.GIF,
                CommonMimeType.JPEG,
                CommonMimeType.SVG);


    private final ResourceLoader resourceLoader;

    private final Map<String, ObjectIcon> iconByKey = _Maps.newConcurrentHashMap();

    private final ObjectIcon fallbackIcon =
            ObjectIcon.eager(
                    "ObjectIconFallback",
                    _Resources.getResourceUrl(
                            ObjectIconServiceDefault.class,
                            "ObjectIconFallback.png"),
                    CommonMimeType.PNG);


    @Override
    public ObjectIcon getObjectIcon(
            final @NonNull ObjectSpecification spec,
            final @Nullable String iconNameModifier) {

        val domainClass = spec.getCorrespondingClass();
        val iconResourceKey = _Strings.isNotEmpty(iconNameModifier)
                ? domainClass.getName() + "-" + iconNameModifier
                : domainClass.getName();

        // also memoize unsuccessful icon lookups (as fallback), so we don't search repeatedly

        val cachedIcon = iconByKey.get(iconResourceKey);
        if(cachedIcon!=null) {
            return cachedIcon;
        }

        val icon = findIcon(spec, iconNameModifier);

        iconByKey.put(iconResourceKey, icon);

        return icon;

        //XXX cannot use computeIfAbsent, as it does not support recursive update
        // return iconByKey.computeIfAbsent(iconResourceKey, key->
        //     findIcon(spec, iconNameModifier));
    }

    //@Override
    private ObjectIcon getObjectFallbackIcon() {
        return fallbackIcon;
    }

    // -- HELPER

    private ObjectIcon findIcon(
            final @NonNull ObjectSpecification spec,
            final @Nullable String iconNameModifier) {

        val domainClass = spec.getCorrespondingClass();
        val iconResourceNameNoExt = _Strings.isNotEmpty(iconNameModifier)
                ? domainClass.getSimpleName() + "-" + iconNameModifier
                : domainClass.getSimpleName();

        // search for image in corresponding class'es resource path

        for(val imageType : IMAGE_TYPES) {

            val objectIcon = imageType
                .getProposedFileExtensions()
                .stream()
                .map(suffix->iconResourceNameNoExt + "." + suffix)
                .map(iconResourceName->
                        classPathResource(domainClass, iconResourceName)
                        .map(url->ObjectIcon.lazy(
                                iconResourceNameNoExt,
                                url,
                                imageType)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

            if(objectIcon.isPresent()) {
                return objectIcon.get(); // short-circuit if found
            }

        }

        // also search the default image resource path

        for(val imageType : IMAGE_TYPES) {

            val objectIcon = imageType
                    .getProposedFileExtensions()
                    .stream()
                    .map(suffix->DEFAULT_IMAGE_RESOURCE_PATH + "/" + iconResourceNameNoExt + "." + suffix)
                    .map(iconResourcePath->
                            classPathResource(iconResourcePath)
                            .map(url->ObjectIcon.lazy(
                                    iconResourceNameNoExt,
                                    url,
                                    imageType)))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();

            if(objectIcon.isPresent()) {
                return objectIcon.get(); // short-circuit if found
            }

        }

        return spec.superclass()!=null
                // continue search in super spec
                ? getObjectIcon(spec.superclass(), iconNameModifier) // memoizes as a side-effect
                : _Strings.isNotEmpty(iconNameModifier)
                        // also do a more generic search, skipping the modifier
                        ? getObjectIcon(spec, null) // memoizes as a side-effect
                        : getObjectFallbackIcon();
    }

    // -- HELPER

    @SneakyThrows
    private Optional<URL> classPathResource(
            final @NonNull String absoluteResourceName) {
        if(!absoluteResourceName.startsWith("classpath:")) {
            throw _Exceptions
            .illegalArgument("invalid absolute resourceName %s", absoluteResourceName);
        }
        val resource = resourceLoader.getResource(absoluteResourceName);
        return resource.exists()
                ? Optional.ofNullable(resource.getURL())
                : Optional.empty();
    }

    private static Optional<URL> classPathResource(
            final @NonNull Class<?> contextClass,
            final @NonNull String relativeResourceName) {
        if(relativeResourceName.startsWith("/")) {
            throw _Exceptions
            .illegalArgument("invalid relative resourceName %s", relativeResourceName);
        }
        val resourceUrl = _Resources.getResourceUrl(contextClass, relativeResourceName);
        return Optional.ofNullable(resourceUrl);
    }

}
