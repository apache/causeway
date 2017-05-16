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
package org.apache.isis.core.metamodel.facets.collparam.semantics;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public enum CollectionSemantics {
    LIST {
        @Override
        public Object emptyCollectionOf(final Class<?> elementClass) {
            return new ArrayList<>();
        }
    }, ARRAY {
        @Override
        public Object emptyCollectionOf(final Class<?> elementClass) {
            return Array.newInstance(elementClass, 0);
        }
    }, SORTED_SET {
        @Override
        public Object emptyCollectionOf(final Class<?> elementClass) {
            return new TreeSet<>();
        }
    }, SET {
        @Override
        public Object emptyCollectionOf(final Class<?> elementClass) {
            return new HashSet<>();
        }
    }, OTHER {
        @Override
        public Object emptyCollectionOf(final Class<?> elementClass) {
            return new ArrayList<>();
        }
    };

    public static CollectionSemantics of(final Class<?> accessorReturnType) {
        if (!Collection.class.isAssignableFrom(accessorReturnType)) {
            return ARRAY;
        }
        if (List.class.isAssignableFrom(accessorReturnType)) {
            return LIST;
        }
        if (SortedSet.class.isAssignableFrom(accessorReturnType)) {
            return SORTED_SET;
        }
        if (Set.class.isAssignableFrom(accessorReturnType)) {
            return SET;
        }
        return OTHER;
    }

    /**
     * The corresponding class is not a subclass of {@link Collection}.
     */
    public boolean isArray() {
        return this == ARRAY;
    }

    public boolean isList() {
        return this == LIST;
    }

    public boolean isSet() {
        return this == SET || this == SORTED_SET;
    }

    /**
     * For example, {@link Queue}, or some other 3rd party implementation of
     * {@link Collection}.
     */
    public boolean isOther() {
        return this == OTHER;
    }

    public boolean isListOrArray() {
        return isList() || isArray();
    }

    public abstract Object emptyCollectionOf(final Class<?> elementClass);

}
