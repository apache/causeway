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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.commons.ClassUtil;

final class ImageResourceCacheKey implements Serializable {

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
