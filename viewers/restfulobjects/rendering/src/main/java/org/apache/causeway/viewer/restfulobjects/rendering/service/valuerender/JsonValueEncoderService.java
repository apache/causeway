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
package org.apache.causeway.viewer.restfulobjects.rendering.service.valuerender;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import org.springframework.lang.Nullable;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;

import lombok.NonNull;
import lombok.val;

/**
 * Converts value representing {@link ManagedObject}s to their corresponding JSON representation
 * and back.
 *
 * @since 1.x refined for 2.0 {@index}
 */
public interface JsonValueEncoderService {

    /**
     * The value recovered from {@link JsonRepresentation}
     * as {@link ManagedObject} honoring {@link Context},
     * otherwise <tt>null</tt>.
     */
    public ManagedObject asAdapter(
            final ObjectSpecification valueSpec,
            final JsonRepresentation valueRepr,
            final JsonValueConverter.Context context);

    /**
     * Appends given value type representing {@link ManagedObject} to given
     * {@link JsonRepresentation} honoring {@link Context}.
     */
    public void appendValueAndFormat(
            final ManagedObject valueAdapter,
            final JsonRepresentation repr,
            final JsonValueConverter.Context context);

    /**
     * A {@link JsonNode} or otherwise natively supported simple type from given {@link ManagedObject},
     * honoring {@link Context}.
     */
    @Nullable
    public Object asObject(final @NonNull ManagedObject adapter, final JsonValueConverter.Context context);

    // -- UTILITY

    default NullNode appendNullAndFormat(final JsonRepresentation repr, final boolean suppressExtensions) {
        val value = NullNode.getInstance();
        repr.mapPutJsonNode("value", value);
        appendFormats(repr, "string", "string", suppressExtensions);
        return value;
    }

    default void appendFormats(
            final JsonRepresentation repr,
            final @Nullable String format, final @Nullable String extendedFormat, final boolean suppressExtensions) {
        repr.putFormat(format);
        if(!suppressExtensions) {
            repr.putExtendedFormat(extendedFormat);
        }
    }

}
