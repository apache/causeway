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
package org.apache.isis.viewer.restfulobjects.rendering.service.valuerender;

import org.springframework.lang.Nullable;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.rendering.service.valuerender.JsonValueConverter.Context;

import lombok.NonNull;

/**
 * Converts value representing {@link ManagedObject}s to their corresponding JSON representation.
 */
public interface JsonValueEncoderService {

    public ManagedObject asAdapter(
            final ObjectSpecification objectSpec,
            final JsonRepresentation argValueRepr,
            final JsonValueConverter.Context context);

    public void appendValueAndFormat(
            final ManagedObject valueAdapter,
            final JsonRepresentation repr,
            final Context context);

    @Nullable
    public Object asObject(final @NonNull ManagedObject adapter, final JsonValueConverter.Context context);

    static void appendFormats(
            final JsonRepresentation repr,
            final @Nullable String format, final @Nullable String extendedFormat, final boolean suppressExtensions) {
        repr.putFormat(format);
        if(!suppressExtensions) {
            repr.putExtendedFormat(extendedFormat);
        }
    }

}
