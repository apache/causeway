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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.causeway.viewer.webcomponents.htmx;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class HtmxCollectionPresentationRegistry {

    private final Map<String, HtmxCollectionPresentationDefinition> byLogicalType;

    HtmxCollectionPresentationRegistry(final List<HtmxCollectionPresentationDefinition> definitions) {
        final var collected = new LinkedHashMap<String, HtmxCollectionPresentationDefinition>();
        definitions.forEach(definition -> {
            final var previous = collected.putIfAbsent(definition.logicalTypeName(), definition);
            if (previous != null) {
                throw new IllegalStateException("HTMX_COLLECTION_PRESENTATION_DUPLICATE: More than one collection "
                        + "presentation is registered for " + bounded(definition.logicalTypeName()) + " ("
                        + previous.safeSourceIdentifier() + ", " + definition.safeSourceIdentifier() + ").");
            }
        });
        this.byLogicalType = Map.copyOf(collected);
    }

    Optional<HtmxCollectionPresentationDefinition> find(final String logicalTypeName) {
        return Optional.ofNullable(byLogicalType.get(logicalTypeName));
    }

    int size() {
        return byLogicalType.size();
    }

    private static String bounded(final String value) {
        return value.length() <= 200 ? value : value.substring(0, 197) + "...";
    }
}
