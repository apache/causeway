/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.viewer.webcomponents.htmx;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class HtmxPageFragmentRegistry {

    private final Map<String, HtmxPageFragmentFactory> byLogicalType;

    public HtmxPageFragmentRegistry(final List<HtmxPageFragmentFactory> factories) {
        final var collected = new LinkedHashMap<String, HtmxPageFragmentFactory>();
        for (var factory : factories) {
            final var logicalType = factory.logicalTypeName();
            if (logicalType == null || logicalType.isBlank()) {
                throw new IllegalStateException("An HTMX page fragment factory must declare a logical type.");
            }
            if (collected.putIfAbsent(logicalType, factory) != null) {
                throw new IllegalStateException("More than one HTMX page fragment factory is registered for " + logicalType + ".");
            }
        }
        this.byLogicalType = Map.copyOf(collected);
    }

    public Optional<HtmxPageFragmentFactory> find(final String logicalTypeName) {
        return Optional.ofNullable(byLogicalType.get(logicalTypeName));
    }

    public int size() {
        return byLogicalType.size();
    }
}
