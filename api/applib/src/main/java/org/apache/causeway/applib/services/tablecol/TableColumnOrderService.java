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
package org.apache.causeway.applib.services.tablecol;

import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * Provides the ability to reorder columns in both parented- and
 * standalone tables.
 *
 * <p>
 *     If a property is excluded from the returned list, then no column will
 *     be rendered, so the API can also be used to suppress columns completely.
 * </p>
 *
 * <p>
 *     There can be multiple implementations of this service registered,
 *     ordered as per the Spring
 *     {@link org.springframework.core.annotation.Order} annotation (or equivalent).
 *     The result of the first service implementation to return a
 *     non-<code>null</code> value will be used.
 * </p>
 *
 * <p>
 *      If all provided implementations return <code>null</code>, then the
 *      framework will fallback to a default implementation.
 * </p>
 *
 * <p>
 *     The similar {@link TableColumnVisibilityService} SPI is the preferred way to
 *     suppress columns.  As noted above, this {@link TableColumnOrderService}
 *     can also be used to suppress columns.  The reason that the
 *     {@link TableColumnVisibilityService} is needed in addition to this SPI is
 *     because of the way that non-null values are handled; as soon as one
 *     implementation has an opinion on the order of columns, no other
 *     services are consulted.  Trying to combine both responsibilities
 *     (reordering and filtering only in a single
 *     {@link TableColumnOrderService} would result in the user needing to take
 *     a lot of care in the relative priority of different implementations.
 *     Separating out the filter responsibility in the
 *     {@link TableColumnVisibilityService} SPIs eliminates these difficulties).
 * </p>
 *
 * @since 1.x {@index}
 */
public interface TableColumnOrderService {

    /**
     * For the parent collection owned by the specified parent and collection Id, return a list of association ids
     * to be rendered as columns, in a particular order; those not included will be hidden.
     *
     * @param parent - the parent object (eg an <i>Order</i>).
     * @param collectionId - the logical member name that identifies the collection within its domain object type (eg <i>items</i>).
     * @param elementType - the class of the elements of the collection (eg <i>OrderItem</i>s in a collection of <i>Order#items</i>).
     * @param associationIds - the associations (properties or collections) of the element type that should be rendered in the parent object's collection
     *
     * @return <code>null</code> if has no opinion/provides no reordering for this parent and collection.
     */
    @Nullable
    List<String> orderParented(
            final Object parent,
            final String collectionId,
            final Class<?> elementType,
            final List<String> associationIds);

    /**
     * For the standalone collection of the specified type,  return a list of association ids to be rendered as columns,
     * in a particular order; those not included will be hidden.
     *
     * @param domainType - the class of the domain object being rendered in the standalone collection
     * @param associationIds - the associations (properties or collections) of the domain type that should be rendered in the parent object's collection
     *
     * @return <code>null</code> if has no opinion/provides no reordering for this type.
     */
    @Nullable
    List<String> orderStandalone(
            final Class<?> domainType,
            final List<String> associationIds);

}
