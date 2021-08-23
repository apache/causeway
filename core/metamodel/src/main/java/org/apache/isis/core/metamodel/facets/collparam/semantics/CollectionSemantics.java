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

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;

import org.apache.isis.commons.collections.Can;

public enum CollectionSemantics {

    ARRAY(true),

    COLLECTION_INTERFACE(true),

    LIST_IMPLEMENTATION,
    LIST_INTERFACE(true),

    SORTED_SET_IMPLEMENTATION,
    SORTED_SET_INTERFACE(true),

    SET_IMPLEMENTATION,
    SET_INTERFACE(true),

    CAN(true),

    OTHER_IMPLEMENTATION
    ;

    final boolean isSupportedInterfaceForActionParameters;

    private CollectionSemantics() {
        this(false);
    }

    private CollectionSemantics(final boolean isSupportedInterfaceForActionParameters) {
        this.isSupportedInterfaceForActionParameters = isSupportedInterfaceForActionParameters;
    }

    public static CollectionSemantics of(final Class<?> accessorReturnType) {
        if (Can.class.isAssignableFrom(accessorReturnType)) {
            return CAN;
        }
        if (!Collection.class.isAssignableFrom(accessorReturnType)) {
            return ARRAY;
        }
        if (Collection.class.equals(accessorReturnType)) {
            return COLLECTION_INTERFACE;
        }
        if (List.class.isAssignableFrom(accessorReturnType)) {
            return List.class.equals(accessorReturnType) ? LIST_INTERFACE : LIST_IMPLEMENTATION;
        }
        if (SortedSet.class.isAssignableFrom(accessorReturnType)) {
            return SortedSet.class.equals(accessorReturnType) ? SORTED_SET_INTERFACE : SORTED_SET_IMPLEMENTATION;
        }
        if (Set.class.isAssignableFrom(accessorReturnType)) {
            return Set.class.equals(accessorReturnType) ? SET_INTERFACE : SET_IMPLEMENTATION;
        }
        return OTHER_IMPLEMENTATION;
    }

    /**
     * {@link Collection} is not assignable from the corresponding class.
     */
    public boolean isArray() {
        return this == ARRAY;
    }

    public boolean isCan() {
        return this == CAN;
    }

    /**
     * {@link List} is assignable from the corresponding class.
     */
    public boolean isList() {
        return this == LIST_IMPLEMENTATION || this == LIST_INTERFACE;
    }

    /**
     * {@link SortedSet} is assignable from the corresponding class.
     */
    public boolean isSortedSet() {
        return this == SORTED_SET_IMPLEMENTATION || this == SORTED_SET_INTERFACE;
    }

    /**
     * {@link Set} (but not {@link SortedSet}) is assignable from the corresponding class.
     */
    public boolean isUnorderedSet() {
        return this == SET_IMPLEMENTATION || this == SET_INTERFACE;
    }

    /**
     * {@link Set} is assignable from the corresponding class.
     */
    public boolean isAnySet() {
        return isSortedSet() || isUnorderedSet();
    }

    /**
     * For example, {@link Queue}, or some other 3rd party implementation of
     * {@link Collection}.
     */
    public boolean isOther() {
        return this == OTHER_IMPLEMENTATION;
    }

    public boolean isListOrArray() {
        return isList() || isArray();
    }

    public boolean isSupportedInterfaceForActionParameters() {
        return isSupportedInterfaceForActionParameters;
    }

}
