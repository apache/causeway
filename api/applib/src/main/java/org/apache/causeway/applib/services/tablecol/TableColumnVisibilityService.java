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

/**
 * Provides the ability to suppress columns in tables.
 *
 * <p>
 *     Only one API is used, applying to both parented- and standalone tables.
 * </p>
 *
 * <p>
 *     There can be multiple implementations of this service registered,
 *     ordered as per the Spring
 *     {@link org.springframework.core.annotation.Order} annotation (or equivalent).
 *     All implementations are consulted.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface TableColumnVisibilityService {

    /**
     * For the specified collectionType, whether the given memberId is visible
     *
     * @param collectionType - the type of the elements of the collection (rows of the table)
     * @param memberId - a propertyId or possibly actionId of the collectionType
     *
     * @return true if visible, false otherwise.
     */
    boolean hides(
            final Class<?> collectionType,
            final String memberId);
}
