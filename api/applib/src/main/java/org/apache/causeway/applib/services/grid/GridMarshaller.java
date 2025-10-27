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
package org.apache.causeway.applib.services.grid;

import java.util.EnumSet;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.services.marshal.Marshaller;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.functional.Try;

/**
 * Supports {@link BSGrid} marshaling and unmarshaling.
 *
 * @apiNote almost a copy of {@link Marshaller}
 *
 * @since 2.0 revised for 4.0 {@index}
 */
public interface GridMarshaller {

    Class<BSGrid> supportedClass();

    /**
     * Supported format(s) for {@link #unmarshal(Class, String, CommonMimeType)}
     * and {@link #marshal(BSGrid, CommonMimeType)}.
     */
    EnumSet<CommonMimeType> supportedFormats();

    /**
     * @throws UnsupportedOperationException when format is not supported
     */
    String marshal(@NonNull BSGrid value, @NonNull CommonMimeType format);

    /**
     * Returns a new de-serialized instance wrapped in a {@link Try}.
     * @throws UnsupportedOperationException when format is not supported (not wrapped)
     */
    Try<BSGrid> unmarshal(Class<?> domainClass, @Nullable String content, @NonNull CommonMimeType format);

}
