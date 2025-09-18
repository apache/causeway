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
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.ObjectSupport.EmbeddedIconResource;
import org.apache.causeway.applib.annotation.ObjectSupport.FontAwesomeIconResource;
import org.apache.causeway.applib.annotation.ObjectSupport.IconWhere;
import org.apache.causeway.applib.annotation.ObjectSupport.ClassPathIconResource;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._StableValue;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.resources._Resources;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIcon;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIconEmbedded;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIconFa;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIconService;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIconUrlBased;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.SneakyThrows;

/**
 * Default implementation of {@link ObjectIconService}.
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".ObjectIconServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
record ObjectIconServiceDefault(
        ResourceLoader resourceLoader,
        Map<String, ObjectIcon> iconByKey,
        _StableValue<ObjectIcon> fallbackIconRef)
implements ObjectIconService {

    private static final String DEFAULT_IMAGE_RESOURCE_PATH = "classpath:images";
    /* this is only a subset of NamedWithMimeType.ImageType */
    private static final Can<CommonMimeType> IMAGE_TYPES = Can.of(
                CommonMimeType.PNG,
                CommonMimeType.GIF,
                CommonMimeType.JPEG,
                CommonMimeType.SVG);

    // non-canonical constructor
    @Inject
    public ObjectIconServiceDefault(ResourceLoader resourceLoader) {
        this(resourceLoader, new ConcurrentHashMap<>(), new _StableValue<>());
    }

    @Override
    public ObjectIcon getObjectIcon(ManagedObject managedObject, IconWhere iconWhere) {

        var spec = managedObject.objSpec();

        return spec.getIcon(managedObject, iconWhere)
            .map(iconResource->{
                if(iconResource instanceof ObjectSupport.EmbeddedIconResource embedded)
                    return embedded(spec, embedded);
                if(iconResource instanceof ObjectSupport.FontAwesomeIconResource fa)
                    return fa(spec, fa);
                if(iconResource instanceof ObjectSupport.ClassPathIconResource suffixed)
                    return suffixed(spec, suffixed);
                throw _Exceptions.unmatchedCase(iconResource);
            })
            // also handle the empty suffix case
            .or(()->Optional.ofNullable(suffixed(spec, ClassPathIconResource.emptySuffix())))
            .orElseGet(this::fallbackIcon);
    }

    // -- HELPER

    private ObjectIcon embedded(ObjectSpecification objSpec, EmbeddedIconResource embeddedIconResource) {
        return new ObjectIconEmbedded(objSpec.getCorrespondingClass().getSimpleName(), embeddedIconResource.dataUri());
    }

    private ObjectIcon fa(ObjectSpecification objSpec, FontAwesomeIconResource faIconResource) {
        return new ObjectIconFa(objSpec.getCorrespondingClass().getSimpleName(), faIconResource.faLayers());
    }

    private ObjectIcon suffixed(ObjectSpecification objSpec, ClassPathIconResource cpIconResource) {
        var domainClass = objSpec.getCorrespondingClass();
        var iconResourceKey = StringUtils.hasLength(cpIconResource.suffix())
            ? domainClass.getName() + "-" + cpIconResource.suffix()
            : domainClass.getName();
        var cachedIcon = iconByKey.get(iconResourceKey);
        if(cachedIcon!=null) return cachedIcon;

        var icon = findIcon(objSpec, _Strings.nonEmpty(cpIconResource.suffix()));
        // also memoize unsuccessful icon lookups (as fallback), so we don't search repeatedly
        iconByKey.put(iconResourceKey, icon!=null
            ? icon
            : fallbackIcon());
        return icon;
    }

    private ObjectIcon fallbackIcon() {
        return fallbackIconRef.orElseSet(()->ObjectIconUrlBased.eager(
                "ObjectIconFallback",
                _Resources.lookupResourceUrl(
                        ObjectIconServiceDefault.class,
                        "ObjectIconFallback.png")
                    .orElse(null),
                CommonMimeType.PNG));
    }

    @Nullable
    private ObjectIcon findIcon(
            final ObjectSpecification spec,
            final Optional<String> iconName) {

        var domainClass = spec.getCorrespondingClass();
        var iconNameSuffixIfAny = iconName.orElse(null);
        var iconResourceNameNoExt = _Strings.isNotEmpty(iconNameSuffixIfAny)
                ? domainClass.getSimpleName() + "-" + iconNameSuffixIfAny
                : domainClass.getSimpleName();

        // search for image in corresponding class'es resource path

        for(var imageType : IMAGE_TYPES) {

            var objectIcon = imageType
                .proposedFileExtensions()
                .stream()
                .map(ext->iconResourceNameNoExt + "." + ext)
                .map(iconResourceName->
                        classPathResource(domainClass, iconResourceName)
                        .map(url->ObjectIconUrlBased.lazy(
                                iconResourceNameNoExt,
                                url,
                                imageType)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

            if(objectIcon.isPresent()) return objectIcon.get(); // short-circuit if found
        }

        // also search the default image resource path

        for(var imageType : IMAGE_TYPES) {

            var objectIcon = imageType
                    .proposedFileExtensions()
                    .stream()
                    .map(suffix->DEFAULT_IMAGE_RESOURCE_PATH + "/" + iconResourceNameNoExt + "." + suffix)
                    .map(iconResourcePath->
                            classPathResource(iconResourcePath)
                            .map(url->ObjectIconUrlBased.lazy(
                                    iconResourceNameNoExt,
                                    url,
                                    imageType)))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();

            if(objectIcon.isPresent()) return objectIcon.get(); // short-circuit if found
        }

        return spec.superclass()!=null
            // continue search in super spec
            ? findIcon(spec.superclass(), iconName) // memoizes as a side-effect
            : _Strings.isNotEmpty(iconNameSuffixIfAny)
                // also do a more generic search, skipping the modifier
                ? findIcon(spec, Optional.empty()) // memoizes as a side-effect
                : null;
    }


    @SneakyThrows
    private Optional<URL> classPathResource(
            final @NonNull String absoluteResourceName) {
        if(!absoluteResourceName.startsWith("classpath:")) {
            throw _Exceptions
                .illegalArgument("invalid absolute resourceName %s", absoluteResourceName);
        }
        var resource = resourceLoader.getResource(absoluteResourceName);
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
        return _Resources.lookupResourceUrl(contextClass, relativeResourceName);
    }

}
