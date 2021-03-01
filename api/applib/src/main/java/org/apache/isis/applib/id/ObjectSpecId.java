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
package org.apache.isis.applib.id;

import java.io.Serializable;

import org.apache.isis.commons.internal.base._Refs;

import static org.apache.isis.commons.internal.base._With.requiresNotEmpty;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * Represents an {@link org.apache.isis.core.metamodel.spec.ObjectSpecification}, as determined by
 * an {@link org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet}.
 *
 * <p>
 * Has value semantics.
 * @deprecated use {@link LogicalType} instead
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Value
public final class ObjectSpecId implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull private final String specId;
    @NonNull private final String namespace;

    public static ObjectSpecId of(final @NonNull String specId) {
        requiresNotEmpty(specId, "specId");
        return new ObjectSpecId(
                specId, 
                _Refs.stringRef(specId).cutAtLastIndexOf("."));
    }

    public String asString() {
        return specId;
    }

    @Override
    public String toString() {
        return asString();
    }

}
