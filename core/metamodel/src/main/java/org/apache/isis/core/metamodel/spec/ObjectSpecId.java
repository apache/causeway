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
package org.apache.isis.core.metamodel.spec;

import static org.apache.isis.commons.internal.base._With.requiresNotEmpty;

import java.io.Serializable;
import java.util.Objects;

import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;

/**
 * Represents an {@link ObjectSpecification}, as determined by
 * an {@link ObjectSpecIdFacet}.
 *
 * <p>
 * Has value semantics.
 */
public final class ObjectSpecId implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String specId;

    public static ObjectSpecId of(String specId) {
        requiresNotEmpty(specId, "specId");
        return new ObjectSpecId(specId);
    }

    private ObjectSpecId(String specId) {
        this.specId = specId;
    }

    public String asString() {
        return specId;
    }

    @Override
    public int hashCode() {
        return specId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ObjectSpecId other = (ObjectSpecId) obj;
        return Objects.equals(specId, other.specId);
    }

    @Override
    public String toString() {
        return asString();
    }


}
