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
package org.apache.isis.viewer.wicket.viewer.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.commons.ClassUtil;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Specification;
import org.apache.isis.viewer.wicket.model.models.ImageResourceCache;

import images.Images;

/**
 * Caches images loaded either from the same package as the specified object, or from the <tt>images</tt> package (using the {@link Images} otherwise.
 * class).
 *
 * <p>
 * Searches for a fixed set of suffixes: <code>png, gif, jpeg, jpg, svg</code>.
 */
@Service
@Named("isis.viewer.wicket.ImageResourceCacheClassPath")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("ClassPath")
public class ImageResourceCacheClassPath implements ImageResourceCache {

    private static final long serialVersionUID = 1L;

    private static final List<String> IMAGE_SUFFICES = Arrays.asList("png", "gif", "jpeg", "jpg", "svg");
    private static final String FALLBACK_IMAGE = "Default.png";

    private final Map<ImageResourceCacheKey, ResourceReference> resourceReferenceByKey = _Maps.newConcurrentHashMap();
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

    static final class ImageResourceCacheKey implements Serializable {

        ImageResourceCacheKey(
                final Class<?> resourceClass,
                final String resourceName) {
            this.resourceName = resourceName;
            this.resourceClass = resourceClass;
            this.toString = calcToString();
        }

        // -- getResourceClass, getResourceName

        private final Class<?> resourceClass;
        private final String resourceName;

        public final Class<?> getResourceClass() {
            return resourceClass;
        }

        public final String getResourceName() {
            return resourceName;
        }


        // -- superKey

        ImageResourceCacheKey superKey() {
            if(resourceClass == null) {
                return null;
            }
            final Class<?> superclass = resourceClass.getSuperclass();
            if(superclass == Object.class) {
                return null;
            }
            return new ImageResourceCacheKey(superclass, resourceName);
        }



        // -- resourcePaths

        List<ImageResourceCacheKey> resourcePaths(final String... suffices) {
            return resourcePaths(Arrays.asList(suffices));
        }

        List<ImageResourceCacheKey> resourcePaths(final List<String> suffices) {

            ImageResourceCacheKey key;
            final List<ImageResourceCacheKey> resourcePaths = _Lists.newArrayList();


            boolean generated = false;

            if(resourceName != null) {

                final Class<?> resourceClass = ClassUtil.forNameElseNull(resourceName);

                if(resourceClass != null) {
                    for (String suffix : suffices) {
                        resourcePaths.add(new ImageResourceCacheKey(resourceClass, resourceClass.getSimpleName() + "." + suffix));
                    }
                    generated = true;
                }
            }

            if(!generated && resourceClass != null) {
                // with the iconName
                if(resourceName != null) {
                    key = this;
                    do {
                        for (String suffix : suffices) {
                            resourcePaths.add(new ImageResourceCacheKey(key.getResourceClass(), key.getResourceClass().getSimpleName() + "-" + resourceName + "." + suffix));
                        }
                        for (String suffix : suffices) {
                            resourcePaths.add(new ImageResourceCacheKey(key.getResourceClass(), resourceName + "." + suffix));
                        }
                    } while ((key = key.superKey()) != null);
                    key = this;
                    do {
                        for (String suffix : suffices) {
                            resourcePaths.add(new ImageResourceCacheKey(null, key.getResourceClass().getSimpleName() + "-" + resourceName + "." + suffix));
                        }
                    } while ((key = key.superKey()) != null);
                    for (String suffix : suffices) {
                        resourcePaths.add(new ImageResourceCacheKey(null, resourceName + "." + suffix));
                    }
                }

                // without the iconName
                key = this;
                do {
                    for (String suffix : suffices) {
                        resourcePaths.add(new ImageResourceCacheKey(key.getResourceClass(), key.getResourceClass().getSimpleName() + "." + suffix));
                    }
                } while ((key = key.superKey()) != null);
                key = this;
                do {
                    for (String suffix : suffices) {
                        resourcePaths.add(new ImageResourceCacheKey(null, key.getResourceClass().getSimpleName() + "." + suffix));
                    }
                } while ((key = key.superKey()) != null);

                generated = true;
            }

            if(!generated && resourceName != null) {

                for (String suffix : suffices) {
                    resourcePaths.add(new ImageResourceCacheKey(null, resourceName + "." + suffix));
                }

            }

            // fallback
            for (String suffix : suffices) {
                resourcePaths.add(new ImageResourceCacheKey(null, "Default" + "." + suffix));
            }


            return Collections.unmodifiableList(resourcePaths);
        }



        // -- equals, hashCode

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ImageResourceCacheKey key = (ImageResourceCacheKey) o;
            if (toString != null ? !toString.equals(key.toString) : key.toString != null) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return toString != null ? toString.hashCode() : 0;
        }



        // -- toString

        private final String toString;

        @Override
        public String toString() {
            return toString;
        }

        private String calcToString() {
            final StringBuilder buf = new StringBuilder();
            if(resourceClass != null) {
                buf.append(resourceClass.getName()).append("/").append(resourceClass.getSimpleName());
            }
            if(buf.length() > 0) {
                buf.append("-");
            }
            if(resourceName != null) {
                buf.append(resourceName);
            }
            return buf.toString();
        }

    }
}
