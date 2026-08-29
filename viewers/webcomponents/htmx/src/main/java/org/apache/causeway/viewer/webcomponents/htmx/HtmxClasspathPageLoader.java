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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

final class HtmxClasspathPageLoader {

    static final String LOCATION = "classpath*:/META-INF/causeway/webcomponents/pages/*.html";
    static final int MAXIMUM_PAGE_BYTES = 256 * 1024;
    static final int MAXIMUM_PAGE_COUNT = 512;
    private static final Pattern LOGICAL_TYPE_NAME = Pattern.compile(
            "[A-Za-z_][A-Za-z0-9_$-]*(?:\\.[A-Za-z_][A-Za-z0-9_$-]*)*");

    private final ResourcePatternResolver resolver;
    private final HtmxViewerProperties.ResourcePageMode resourcePageMode;

    HtmxClasspathPageLoader(final ResourcePatternResolver resolver) {
        this(resolver, HtmxViewerProperties.ResourcePageMode.CACHED);
    }

    HtmxClasspathPageLoader(
            final ResourcePatternResolver resolver,
            final HtmxViewerProperties.ResourcePageMode resourcePageMode) {
        this.resolver = java.util.Objects.requireNonNull(resolver);
        this.resourcePageMode = java.util.Objects.requireNonNull(resourcePageMode);
    }

    List<HtmxPageDefinition> load() {
        final Resource[] discovered;
        try {
            discovered = resolver.getResources(LOCATION);
        } catch (IOException ex) {
            throw failure("HTMX_PAGE_DISCOVERY_FAILED", "Private HTML page discovery failed.");
        }
        if (discovered.length > MAXIMUM_PAGE_COUNT) {
            throw failure(
                    "HTMX_PAGE_COUNT_EXCEEDED",
                    "Private HTML page discovery exceeded " + MAXIMUM_PAGE_COUNT + " resources.");
        }
        Arrays.sort(discovered, Comparator
                .comparing((Resource resource) -> safeFilename(resource.getFilename()))
                .thenComparing(Resource::getDescription));
        final var definitions = new ArrayList<HtmxPageDefinition>(discovered.length);
        for (final var resource : discovered) {
            definitions.add(load(resource));
        }
        return List.copyOf(definitions);
    }

    private HtmxPageDefinition load(final Resource resource) {
        final var filename = resource.getFilename();
        if (filename == null || !filename.endsWith(".html")) {
            throw failure("HTMX_PAGE_INVALID_NAME", "A private HTML page has no valid filename.");
        }
        final var logicalTypeName = filename.substring(0, filename.length() - ".html".length());
        if (logicalTypeName.length() > 255 || !LOGICAL_TYPE_NAME.matcher(logicalTypeName).matches()) {
            throw failure(
                    "HTMX_PAGE_INVALID_NAME",
                    "Private HTML page '" + bounded(filename) + "' does not have a valid logical-type filename.");
        }
        final var source = "resource:" + bounded(filename);
        final var startupHtml = decode(resource, source);
        return resourcePageMode == HtmxViewerProperties.ResourcePageMode.RELOAD
                ? HtmxPageDefinition.reloadingResource(logicalTypeName, source, () -> decode(resource, source))
                : HtmxPageDefinition.resource(logicalTypeName, source, startupHtml);
    }

    static String decode(final Resource resource, final String source) {
        final byte[] bytes;
        try (InputStream input = resource.getInputStream()) {
            bytes = readBounded(input, source);
        } catch (IOException ex) {
            throw failure("HTMX_PAGE_UNREADABLE", "Private HTML page '" + bounded(source) + "' cannot be read.");
        }
        if (bytes.length == 0) {
            throw failure("HTMX_PAGE_EMPTY", "Private HTML page '" + bounded(source) + "' is empty.");
        }
        final String html;
        try {
            html = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException ex) {
            throw failure("HTMX_PAGE_INVALID_UTF8", "Private HTML page '" + bounded(source) + "' is not valid UTF-8.");
        }
        if (html.indexOf('\0') >= 0) {
            throw failure("HTMX_PAGE_NUL_CONTENT", "Private HTML page '" + bounded(source) + "' contains a NUL character.");
        }
        return html;
    }

    private static byte[] readBounded(final InputStream input, final String source) throws IOException {
        final var output = new ByteArrayOutputStream(Math.min(8192, MAXIMUM_PAGE_BYTES));
        final var buffer = new byte[8192];
        int total = 0;
        int read;
        while ((read = input.read(buffer)) >= 0) {
            if (read == 0) {
                continue;
            }
            total += read;
            if (total > MAXIMUM_PAGE_BYTES) {
                throw failure(
                        "HTMX_PAGE_SIZE_EXCEEDED",
                        "Private HTML page '" + source + "' exceeds " + MAXIMUM_PAGE_BYTES + " UTF-8 bytes.");
            }
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private static IllegalStateException failure(
            final String code,
            final String message) {
        final var boundedMessage = code + ": " + bounded(message);
        return new IllegalStateException(boundedMessage);
    }

    private static String safeFilename(final String filename) {
        return filename == null ? "" : filename;
    }

    private static String bounded(final String value) {
        return value.length() <= 240 ? value : value.substring(0, 237) + "...";
    }
}
