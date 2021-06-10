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
package org.apache.isis.applib.services.tablecol;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Convenience implementation of {@link TableColumnOrderService} that ignores
 * requests for the order of any types other than that
 * {@link #getCollectionType() type specified in the constructor}.
 *
 * @since 1.x {@index}
 */
@RequiredArgsConstructor
public abstract class TableColumnOrderForCollectionTypeAbstract<T>
        implements TableColumnOrderService {

    @Getter
    private final Class<T> collectionType;

    /**
     * Ignores any request for collections not of the
     * {@link #getCollectionType() type specified in the constructor},
     * otherwise delegates to {@link #orderParented(Object, String, List)}.
     *
     * @see #orderParented(Object, String, List)
     */
    @Override
    public final List<String> orderParented(
            final Object parent,
            final String collectionId,
            final Class<?> collectionType,
            final List<String> propertyIds) {
        if (! this.collectionType.isAssignableFrom(collectionType)) {
            return null;
        }
        return orderParented(parent, collectionId, propertyIds);
    }

    /**
     * Default implementation just returns the provided <code>propertyIds</code>
     * unchanged, but subclasses can override as necessary.
     *
     * @param propertyIds - to reorder
     * @return - the reordered propertyIds (or <code>null</code> if no opinion)
     */
    protected List<String> orderParented(
            final Object parent,
            final String collectionId,
            final List<String> propertyIds) {
        return propertyIds;
    }

    /**
     * Ignores any request for collections not of the
     * {@link #getCollectionType() type specified in the constructor},
     * otherwise delegates to {@link #orderStandalone(List)}.
     *
     * @see #orderStandalone(List)
     */
    @Override
    public final List<String> orderStandalone(
            final Class<?> collectionType,
            final List<String> propertyIds) {
        if (! this.collectionType.isAssignableFrom(collectionType)) {
            return null;
        }
        return orderStandalone(propertyIds);
    }

    /**
     * Default implementation just returns the provided <code>propertyIds</code>
     * unchanged, but subclasses can override as necessary.
     *
     * @param propertyIds - to reorder
     * @return - the reordered propertyIds (or <code>null</code> if no opinion)
     */
    protected List<String> orderStandalone(
            final List<String> propertyIds) {
        return propertyIds;
    }

}
