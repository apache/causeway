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
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
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
 * Searches for a fixed set of suffixes: {@link #IMAGE_SUFFICES}.
 */
@Singleton
public class ImageResourceCacheClassPath implements ImageResourceCache {

    private static final long serialVersionUID = 1L;
    
    private static final List<String> IMAGE_SUFFICES = Arrays.asList("png", "gif", "jpeg", "jpg");
    private static final String FALLBACK_IMAGE = "Default.png";

    private final Map<Key, ResourceReference> resourceReferenceByKey = Maps.newConcurrentMap();

    static final class Key implements Serializable {

        private final Class<?> resourceClass;
        private final String iconName;
        private final String toString;

        public Key(
                final Class<?> correspondingClass,
                final String iconName) {
            this.iconName = iconName;
            this.resourceClass = correspondingClass;
            this.toString = calcToString();
        }

        //region > getResourceClass, getIconName

        public final Class<?> getResourceClass() {
            return resourceClass;
        }

        public final String getResourceName() {
            return iconName;
        }
        //endregion

        //region > superKey

        Key superKey() {
            if(resourceClass == null) {
                return null;
            }
            final Class<?> superclass = resourceClass.getSuperclass();
            if(superclass == Object.class) {
                return null;
            }
            return new Key(superclass, iconName);
        }

        //endregion

        //region > resourcePaths

        List<Key> resourcePaths(final String... suffices) {
            return resourcePaths(Arrays.asList(suffices));
        }

        List<Key> resourcePaths(final List<String> suffices) {

            Key key;
            final List<Key> resourcePaths = Lists.newArrayList();

            if(resourceClass != null) {
                // with the iconName
                if(iconName != null) {
                    key = this;
                    do {
                        for (String suffix : suffices) {
                            resourcePaths.add(new Key(key.getResourceClass(), key.getResourceClass().getSimpleName() + "-" + iconName + "." + suffix));
                        }
                        for (String suffix : suffices) {
                            resourcePaths.add(new Key(key.getResourceClass(), iconName + "." + suffix));
                        }
                    } while ((key = key.superKey()) != null);
                    key = this;
                    do {
                        for (String suffix : suffices) {
                            resourcePaths.add(new Key(null, key.getResourceClass().getSimpleName() + "-" + iconName + "." + suffix));
                        }
                    } while ((key = key.superKey()) != null);
                    for (String suffix : suffices) {
                        resourcePaths.add(new Key(null, iconName + "." + suffix));
                    }
                }

                // without the iconName
                key = this;
                do {
                    for (String suffix : suffices) {
                        resourcePaths.add(new Key(key.getResourceClass(), key.getResourceClass().getSimpleName() + "." + suffix));
                    }
                } while ((key = key.superKey()) != null);
                key = this;
                do {
                    for (String suffix : suffices) {
                        resourcePaths.add(new Key(null, key.getResourceClass().getSimpleName() + "." + suffix));
                    }
                } while ((key = key.superKey()) != null);


            } else {

                if(iconName != null) {
                    for (String suffix : suffices) {
                        resourcePaths.add(new Key(null, iconName + "." + suffix));
                    }
                }
            }

            // fallback
            for (String suffix : suffices) {
                resourcePaths.add(new Key(null, "Default" + "." + suffix));
            }


            return Collections.unmodifiableList(resourcePaths);
        }

        //endregion

        //region > equals, hashCode, toString

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            if (toString != null ? !toString.equals(key.toString) : key.toString != null) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return toString != null ? toString.hashCode() : 0;
        }

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
            if(iconName != null) {
                buf.append(iconName);
            }
            return buf.toString();
        }
        //endregion
    }

    public ImageResourceCacheClassPath() {
        resourceReferenceByKey.put(new Key(null, null), new PackageResourceReference(Images.class, FALLBACK_IMAGE));
    }

    @Override
    public ResourceReference resourceReferenceFor(final ObjectAdapter adapter) {
        return resourceReferenceFor(adapter.getSpecification(), adapter.getIconName());
    }

    @Override
    public ResourceReference resourceReferenceForSpec(final ObjectSpecification spec) {
        return resourceReferenceFor(spec, null);
    }

    private ResourceReference resourceReferenceFor(
            final ObjectSpecification specification,
            final String iconNameIfAny) {

        final Class<?> correspondingClassIfAny = specification != null? specification.getCorrespondingClass(): null;
        final Key key = new Key(correspondingClassIfAny, iconNameIfAny);

        ResourceReference resourceReference = resourceReferenceByKey.get(key);
        if(resourceReference == null) {
            resourceReference = findResourceReferenceFor(key);
            resourceReferenceByKey.put(key, resourceReference);
        }

        return resourceReference;
    }

    private ResourceReference findResourceReferenceFor(final Key key) {

        final List<Key> keys = key.resourcePaths(IMAGE_SUFFICES);
        for (Key resourceKey : keys) {
            Class<?> resourceClass = resourceKey.getResourceClass();
            if(resourceClass == null) {
                resourceClass = Images.class;
            }
            final String iconName = resourceKey.iconName;

            final InputStream resourceAsStream = resourceClass.getResourceAsStream(iconName);
            if (resourceAsStream != null) {
                closeSafely(resourceAsStream);
                return new PackageResourceReference(resourceClass, iconName);
            }
        }
        return null;
    }

    private static void closeSafely(InputStream resourceAsStream) {
        try {
            resourceAsStream.close();
        } catch (IOException e) {
            // ignore
        }
    }
}
