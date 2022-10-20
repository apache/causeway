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
package org.apache.causeway.core.metamodel.spec;

import java.util.Optional;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.bookmark.Bookmark;

import lombok.NonNull;

/**
 * Introduced as a shortcut provider.
 */
public interface HasObjectSpecification {

    ObjectSpecification getSpecification();

    // -- SHORTCUTS

    default Class<?> getCorrespondingClass() {
        return getSpecification().getCorrespondingClass();
    }

    default LogicalType getLogicalType() {
        return getSpecification().getLogicalType();
    }

    default String getLogicalTypeName() {
        return getSpecification().getLogicalTypeName();
    }

    /**
     * As used for the element type of collections.
     */
    default Optional<ObjectSpecification> getElementSpecification() {
        return getSpecification().getElementSpecification();
    }

    default Bookmark createBookmark(final @NonNull String urlSafeIdentifier) {
        return Bookmark.forLogicalTypeAndIdentifier(getLogicalType(), urlSafeIdentifier);
    }

}
