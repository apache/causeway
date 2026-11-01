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

    private final Map<String, HtmxPageDefinition> byLogicalType;

    public HtmxPageFragmentRegistry(final List<HtmxPageFragmentFactory> factories) {
        this(factories, List.of());
    }

    HtmxPageFragmentRegistry(
            final List<HtmxPageFragmentFactory> factories,
            final List<HtmxPageDefinition> resourcePages) {
        final var collected = new LinkedHashMap<String, HtmxPageDefinition>();
        resourcePages.forEach(page -> register(collected, page));
        factories.stream().map(HtmxPageDefinition::factory).forEach(page -> register(collected, page));
        this.byLogicalType = Map.copyOf(collected);
    }

    Optional<HtmxPageDefinition> find(final String logicalTypeName) {
        return Optional.ofNullable(byLogicalType.get(logicalTypeName));
    }

    public int size() {
        return byLogicalType.size();
    }

    private static void register(
            final Map<String, HtmxPageDefinition> collected,
            final HtmxPageDefinition page) {
        final var logicalType = page.logicalTypeName();
        if (logicalType == null || logicalType.isBlank()) {
            throw new IllegalStateException("HTMX_PAGE_MISSING_LOGICAL_TYPE: An HTMX page must declare a logical type.");
        }
        final var previous = collected.putIfAbsent(logicalType, page);
        if (previous != null) {
            throw new IllegalStateException("HTMX_PAGE_DUPLICATE: More than one HTMX page is registered for "
                    + bounded(logicalType) + " (" + previous.safeSourceIdentifier() + ", "
                    + page.safeSourceIdentifier() + ").");
        }
    }

    private static String bounded(final String value) {
        return value.length() <= 200 ? value : value.substring(0, 197) + "...";
    }
}
