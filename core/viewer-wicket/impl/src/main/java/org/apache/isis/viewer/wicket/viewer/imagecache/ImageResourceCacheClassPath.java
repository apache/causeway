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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import com.google.common.collect.Maps;

import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.Specification;
import org.apache.isis.viewer.wicket.model.models.ImageResourceCache;

import images.Images;

/**
 * Caches images loaded either from the same package as the specified object, or from the <tt>images</tt> package (using the {@link Images} otherwise.
 * class).
 *
 * <p>
 * Searches for a fixed set of suffixes: <code>png, gif, jpeg, jpg, svg</code>.
 */
@Singleton
public class ImageResourceCacheClassPath implements ImageResourceCache {

    private static final long serialVersionUID = 1L;

    private static final List<String> IMAGE_SUFFICES = Arrays.asList("png", "gif", "jpeg", "jpg", "svg");
    private static final String FALLBACK_IMAGE = "Default.png";

    private final Map<ImageResourceCacheKey, ResourceReference> resourceReferenceByKey = Maps.newConcurrentMap();
    private final PackageResourceReference fallbackResource;

    public ImageResourceCacheClassPath() {
        fallbackResource = new PackageResourceReference(Images.class, FALLBACK_IMAGE);
        resourceReferenceByKey.put(new ImageResourceCacheKey(null, null), fallbackResource);
    }

    @Override
    public ResourceReference resourceReferenceFor(final ManagedObject adapter) {
        final Specification spec = adapter.getSpecification();
        if(spec!=null && spec instanceof ObjectSpecification) {
            final ObjectSpecification objSpec = (ObjectSpecification) spec;
            return resourceReferenceFor(objSpec, objSpec.getIconName(adapter));
        } else {
            return fallbackResource;    
        }

    }

    @Override
    public ResourceReference resourceReferenceForSpec(final ObjectSpecification spec) {
        return resourceReferenceFor(spec, null);
    }

    private ResourceReference resourceReferenceFor(
            final ObjectSpecification specification,
            final String iconNameIfAny) {

        final Class<?> correspondingClassIfAny = specification != null? specification.getCorrespondingClass(): null;
        final ImageResourceCacheKey key = new ImageResourceCacheKey(correspondingClassIfAny, iconNameIfAny);

        ResourceReference resourceReference = resourceReferenceByKey.get(key);
        if(resourceReference == null) {
            resourceReference = findResourceReferenceFor(key);
            resourceReferenceByKey.put(key, resourceReference);
        }

        return resourceReference;
    }

    protected ResourceReference findResourceReferenceFor(final ImageResourceCacheKey key) {
        final List<ImageResourceCacheKey> keys = key.resourcePaths(IMAGE_SUFFICES);
        for (ImageResourceCacheKey resourceKey : keys) {
            Class<?> resourceClass = resourceKey.getResourceClass();
            if(resourceClass == null) {
                resourceClass = Images.class;
            }
            final String iconName = resourceKey.getResourceName();

            final InputStream resourceAsStream = resourceClass.getResourceAsStream(iconName);
            if (resourceAsStream != null) {
                closeSafely(resourceAsStream);
                return new PackageResourceReference(resourceClass, iconName);
            }
        }
        return fallbackResource;
    }

    private static void closeSafely(InputStream resourceAsStream) {
        try {
            resourceAsStream.close();
        } catch (IOException e) {
            // ignore
        }
    }
}
