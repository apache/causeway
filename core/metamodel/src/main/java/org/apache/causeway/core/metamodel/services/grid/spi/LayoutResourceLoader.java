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
package org.apache.causeway.core.metamodel.services.grid.spi;

import java.util.Optional;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.commons.functional.Try;

/**
 * SPI for grid loading.
 *
 * @since 2.0 {@index}
 */
public interface LayoutResourceLoader {

    /**
     * Try to locate and load a {@link LayoutResource} by type and name.
     */
    Try<LayoutResource> tryLoadLayoutResource(
            final @NonNull Class<?> type,
            final @NonNull String candidateResourceName);

    /**
     * Optionally returns a {@link LayoutResource} based
     * on whether it could be resolved by type and name
     * and successfully read.
     *
     * <p>Silently ignores exceptions underneath, if any.
     */
    default Optional<LayoutResource> lookupLayoutResource(
            final @NonNull Class<?> type,
            final @NonNull String candidateResourceName) {
        return tryLoadLayoutResource(type, candidateResourceName)
                .getValue();
    }

}
