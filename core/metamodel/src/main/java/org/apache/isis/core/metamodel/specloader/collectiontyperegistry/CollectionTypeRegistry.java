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

package org.apache.isis.core.metamodel.specloader.collectiontyperegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.isis.core.metamodel.spec.feature.OneToManyFeature.CollectionSemantics;

/**
 * Defines the types which are considered to be collections.
 * 
 * <p>
 * In this way there are similarities with the way in which value types are
 * specified using <tt>@Value</tt>. However, we need to maintain a repository of
 * these collection types once nominated so that when we introspect classes we
 * look for collections first, and then properties second.
 */
public class CollectionTypeRegistry {

    private final List<Class<?>> collectionTypes = new ArrayList<>();
    private Class<?>[] collectionTypesAsArray = new Class[0];

    /**
     * Inbuilt support for {@link java.util.Collection} as a collection type.
     *
     * <p>
     * Note that this includes any subclasses.
     */
    public CollectionTypeRegistry() {
        addCollectionType(Collection.class);
    }

    /**
     * Plan is for this to be promoted to API at some stage.
     */
    private void addCollectionType(final Class<?> collectionType) {
        collectionTypes.add(collectionType);
        collectionTypesAsArray = collectionTypes.toArray(new Class[0]);
    }

    public boolean isCollectionType(final Class<?> cls) {
        return java.util.Collection.class.isAssignableFrom(cls);
    }

    public boolean isArrayType(final Class<?> cls) {
        return cls.isArray();
    }

    public Class<?>[] getCollectionType() {
        return collectionTypesAsArray;
    }

    public CollectionSemantics semanticsOf(final Class<?> underlyingClass) {
        if (!Collection.class.isAssignableFrom(underlyingClass)) {
            return CollectionSemantics.ARRAY;
        }
        if (List.class.isAssignableFrom(underlyingClass)) {
            return CollectionSemantics.LIST;
        }
        if (Set.class.isAssignableFrom(underlyingClass)) {
            return CollectionSemantics.SET;
        }
        return CollectionSemantics.OTHER;
    }

}
