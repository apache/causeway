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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.PackageResource;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
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

    private static final List<String> IMAGE_SUFFICES = Arrays.asList("png", "gif", "jpeg", "jpg");
    private static final String FALLBACK_IMAGE = "Default.png";
    
    private final Map<ObjectSpecification, ResourceReference> resourceReferenceBySpec = Maps.newHashMap();


    @Override
    public ResourceReference resourceReferenceFor(ObjectAdapter adapter) {
        return resourceReferenceFor(adapter.getSpecification());
    }

    private ResourceReference resourceReferenceFor(final ObjectSpecification spec) {
        ResourceReference resourceReference = resourceReferenceBySpec.get(spec);
        if(resourceReference != null) {
            return resourceReference;
        }
        
        resourceReference = lookupResourceReferenceFor(spec);
        
        resourceReferenceBySpec.put(spec, resourceReference);
        return resourceReference;
    }

    private ResourceReference lookupResourceReferenceFor(final ObjectSpecification spec) {
        final Class<?> correspondingClass = spec.getCorrespondingClass();
        final String specName = correspondingClass.getSimpleName();
        
        final ResourceReference resourceReference = resourceReferenceFor(specName);
        if(resourceReference != null) {
            return resourceReference;
        }
        
        // search up hierarchy
        final ObjectSpecification superSpec = spec.superclass();
        if(superSpec != null) {
            return resourceReferenceFor(superSpec);
        } 
        
        // fallback
        return newPackageResourceReference(FALLBACK_IMAGE);
    }

    private static ResourceReference resourceReferenceFor(final String specName) {
        for(String imageSuffix: IMAGE_SUFFICES) {
            final String imageName = specName + "." + imageSuffix;

            InputStream resourceAsStream = null;
            resourceAsStream = Images.class.getResourceAsStream(imageName);
            if(resourceAsStream == null) {
                continue;
            } else {
                closeSafely(resourceAsStream);
            }
            return newPackageResourceReference(imageName);
        }
        return null;
    }

    private static PackageResourceReference newPackageResourceReference(final String imageFile) {
        return new PackageResourceReference(Images.class, imageFile);
    }

    private static void closeSafely(InputStream resourceAsStream) {
        try {
            resourceAsStream.close();
        } catch (IOException e) {
            // ignore
        }
    }
}
