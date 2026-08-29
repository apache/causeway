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

import java.util.Objects;
import java.util.function.Supplier;

final class HtmxPageDefinition {

    enum Source {
        RESOURCE,
        FACTORY;

        String attributeValue() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    private final String logicalTypeName;
    private final Source source;
    private final String safeSourceIdentifier;
    private final Supplier<String> resourceContent;
    private final HtmxPageFragmentFactory factory;

    private HtmxPageDefinition(
            final String logicalTypeName,
            final Source source,
            final String safeSourceIdentifier,
            final Supplier<String> resourceContent,
            final HtmxPageFragmentFactory factory) {
        this.logicalTypeName = logicalTypeName;
        this.source = source;
        this.safeSourceIdentifier = safeSourceIdentifier;
        this.resourceContent = resourceContent;
        this.factory = factory;
    }

    static HtmxPageDefinition resource(
            final String logicalTypeName,
            final String safeSourceIdentifier,
            final String html) {
        final var cachedHtml = Objects.requireNonNull(html);
        return new HtmxPageDefinition(
                Objects.requireNonNull(logicalTypeName),
                Source.RESOURCE,
                Objects.requireNonNull(safeSourceIdentifier),
                () -> cachedHtml,
                null);
    }

    static HtmxPageDefinition reloadingResource(
            final String logicalTypeName,
            final String safeSourceIdentifier,
            final Supplier<String> content) {
        return new HtmxPageDefinition(
                Objects.requireNonNull(logicalTypeName),
                Source.RESOURCE,
                Objects.requireNonNull(safeSourceIdentifier),
                Objects.requireNonNull(content),
                null);
    }

    static HtmxPageDefinition factory(final HtmxPageFragmentFactory factory) {
        final var type = Objects.requireNonNull(factory).getClass().getName();
        return new HtmxPageDefinition(
                factory.logicalTypeName(),
                Source.FACTORY,
                bounded("factory:" + type),
                null,
                factory);
    }

    String logicalTypeName() {
        return logicalTypeName;
    }

    Source source() {
        return source;
    }

    String safeSourceIdentifier() {
        return safeSourceIdentifier;
    }

    String render(final HtmxObjectRoute route) {
        return source == Source.RESOURCE ? resourceContent.get() : factory.render(route);
    }

    private static String bounded(final String value) {
        return value.length() <= 200 ? value : value.substring(0, 197) + "...";
    }
}
