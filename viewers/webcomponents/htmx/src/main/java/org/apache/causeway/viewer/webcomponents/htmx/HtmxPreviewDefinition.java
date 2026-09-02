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

import java.util.Objects;
import java.util.function.Supplier;

final class HtmxPreviewDefinition {

    private final String logicalTypeName;
    private final String safeSourceIdentifier;
    private final Supplier<String> content;

    private HtmxPreviewDefinition(
            final String logicalTypeName,
            final String safeSourceIdentifier,
            final Supplier<String> content) {
        this.logicalTypeName = Objects.requireNonNull(logicalTypeName);
        this.safeSourceIdentifier = Objects.requireNonNull(safeSourceIdentifier);
        this.content = Objects.requireNonNull(content);
    }

    static HtmxPreviewDefinition resource(
            final String logicalTypeName,
            final String safeSourceIdentifier,
            final String html) {
        final var cached = Objects.requireNonNull(html);
        return new HtmxPreviewDefinition(logicalTypeName, safeSourceIdentifier, () -> cached);
    }

    static HtmxPreviewDefinition reloadingResource(
            final String logicalTypeName,
            final String safeSourceIdentifier,
            final Supplier<String> content) {
        return new HtmxPreviewDefinition(logicalTypeName, safeSourceIdentifier, content);
    }

    String logicalTypeName() {
        return logicalTypeName;
    }

    String safeSourceIdentifier() {
        return safeSourceIdentifier;
    }

    String content() {
        return content.get();
    }
}
