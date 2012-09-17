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

package org.apache.isis.viewer.wicket.viewer.imagecache;

import images.Images;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;

import org.apache.wicket.request.resource.PackageResource;
import org.apache.wicket.request.resource.PackageResourceReference;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.models.ImageResourceCache;

/**
 * Caches images loaded up the <tt>images</tt> package (using the {@link Images}
 * class).
 * 
 * <p>
 * Searches for a fixed set of suffixes: {@value #IMAGE_SUFFICES}.
 */
@Singleton
public class ImageCacheClassPath implements ImageResourceCache {

    public final static List<String> IMAGE_SUFFICES = Arrays.asList("png", "gif", "jpeg", "jpg");

    private final Map<String, PackageResource> imagesByName = Maps.newHashMap();

    @Override
    public PackageResource findImage(final ObjectSpecification noSpec) {
        final String specFullName = noSpec.getFullIdentifier();
        final PackageResource packageResource = findImageResource(specFullName);
        if (packageResource != null) {
            return packageResource;
        } else {
            return findAndCacheImage(noSpec);
        }
    }

    @Override
    public PackageResource findImageResource(final String imageName) {
        final PackageResource packageResource = imagesByName.get(imageName);
        if (packageResource != null) {
            return packageResource;
        } else {
            return findAndCacheImage(imageName);
        }
    }

    private PackageResource findAndCacheImage(final String imageName) {
        final PackageResource packageResource = findImageSuffixed(imageName);
        if (packageResource != null) {
            imagesByName.put(imageName, packageResource);
        }
        return packageResource;
    }

    private PackageResource findImageSuffixed(final String imageName) {
//        for (final String imageSuffix : IMAGE_SUFFICES) {
//            final String path = buildImagePath(imageName, imageSuffix);
//            if (PackageResource.exists(Images.class, path, null, null, null)) {
//                return PackageResource.get(Images.class, path);
//            }
//        }
        return null;
    }

    private synchronized PackageResource findAndCacheImage(final ObjectSpecification noSpec) {
        final PackageResource packageResource = findImageSearchUpHierarchy(noSpec);
        imagesByName.put(noSpec.getFullIdentifier(), packageResource);
        return packageResource;
    }

    private PackageResource findImageSearchUpHierarchy(final ObjectSpecification noSpec) {
//        for (final String imageSuffix : IMAGE_SUFFICES) {
//            final String fullName = noSpec.getFullIdentifier();
//            final String path = buildImagePath(fullName, imageSuffix);
//            if (PackageResource.exists(Images.class, path, null, null, null)) {
//                return PackageResource.get(Images.class, path);
//            }
//        }
//        for (final String imageSuffix : IMAGE_SUFFICES) {
//            final String shortName = noSpec.getShortIdentifier();
//            final String path = buildImagePath(shortName, imageSuffix);
//            if (PackageResource.exists(Images.class, path, null, null, null)) {
//                return PackageResource.get(Images.class, path);
//            }
//        }
        final ObjectSpecification superSpec = noSpec.superclass();
        if (superSpec != null) {
            return findAndCacheImage(superSpec);
        }
        // fallback
//        return PackageResource.get(Images.class, "Default.png");
        return null;
    }

    private String buildImagePath(final String name, final String imageSuffix) {
        return name + "." + imageSuffix;
    }

}
