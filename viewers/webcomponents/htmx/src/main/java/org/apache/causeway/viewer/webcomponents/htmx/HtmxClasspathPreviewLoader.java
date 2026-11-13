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

final class HtmxClasspathPreviewLoader {

    static final String LOCATION = "classpath*:/META-INF/causeway/webcomponents/previews/*.html";
    static final int MAXIMUM_PRESENTATION_BYTES = 64 * 1024;
    static final int MAXIMUM_PRESENTATION_COUNT = 256;
    private static final Pattern LOGICAL_TYPE_NAME = Pattern.compile(
            "[A-Za-z_][A-Za-z0-9_$-]*(?:\\.[A-Za-z_][A-Za-z0-9_$-]*)*");

    private final ResourcePatternResolver resolver;
    private final HtmxViewerProperties.ResourcePageMode resourcePageMode;

    HtmxClasspathPreviewLoader(
            final ResourcePatternResolver resolver,
            final HtmxViewerProperties.ResourcePageMode resourcePageMode) {
        this.resolver = java.util.Objects.requireNonNull(resolver);
        this.resourcePageMode = java.util.Objects.requireNonNull(resourcePageMode);
    }

    List<HtmxPreviewDefinition> load() {
        final Resource[] discovered;
        try {
            discovered = resolver.getResources(LOCATION);
        } catch (IOException ex) {
            throw failure("HTMX_PREVIEW_DISCOVERY_FAILED", "Preview discovery failed.");
        }
        if (discovered.length > MAXIMUM_PRESENTATION_COUNT) {
            throw failure("HTMX_PREVIEW_COUNT_EXCEEDED",
                    "Preview discovery exceeded " + MAXIMUM_PRESENTATION_COUNT + " resources.");
        }
        Arrays.sort(discovered, Comparator
                .comparing((Resource resource) -> safeFilename(resource.getFilename()))
                .thenComparing(Resource::getDescription));
        final var definitions = new ArrayList<HtmxPreviewDefinition>(discovered.length);
        for (final var resource : discovered) {
            definitions.add(load(resource));
        }
        return List.copyOf(definitions);
    }

    private HtmxPreviewDefinition load(final Resource resource) {
        final var filename = resource.getFilename();
        if (filename == null || !filename.endsWith(".html")) {
            throw failure("HTMX_PREVIEW_INVALID_NAME",
                    "A preview has no valid filename.");
        }
        final var logicalTypeName = filename.substring(0, filename.length() - ".html".length());
        if (logicalTypeName.length() > 255 || !LOGICAL_TYPE_NAME.matcher(logicalTypeName).matches()) {
            throw failure("HTMX_PREVIEW_INVALID_NAME", "Preview '"
                    + bounded(filename) + "' does not have a valid logical-type filename.");
        }
        final var source = "resource:" + bounded(filename);
        final var startupHtml = decode(resource, source);
        return resourcePageMode == HtmxViewerProperties.ResourcePageMode.RELOAD
                ? HtmxPreviewDefinition.reloadingResource(
                        logicalTypeName, source, () -> decode(resource, source))
                : HtmxPreviewDefinition.resource(logicalTypeName, source, startupHtml);
    }

    private static String decode(final Resource resource, final String source) {
        final byte[] bytes;
        try (InputStream input = resource.getInputStream()) {
            bytes = readBounded(input, source);
        } catch (IOException ex) {
            throw failure("HTMX_PREVIEW_UNREADABLE",
                    "Preview '" + bounded(source) + "' cannot be read.");
        }
        if (bytes.length == 0) {
            throw failure("HTMX_PREVIEW_EMPTY",
                    "Preview '" + bounded(source) + "' is empty.");
        }
        final String html;
        try {
            html = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException ex) {
            throw failure("HTMX_PREVIEW_INVALID_UTF8",
                    "Preview '" + bounded(source) + "' is not valid UTF-8.");
        }
        if (html.indexOf('\0') >= 0) {
            throw failure("HTMX_PREVIEW_NUL_CONTENT",
                    "Preview '" + bounded(source) + "' contains a NUL character.");
        }
        return html;
    }

    private static byte[] readBounded(final InputStream input, final String source) throws IOException {
        final var output = new ByteArrayOutputStream(Math.min(8192, MAXIMUM_PRESENTATION_BYTES));
        final var buffer = new byte[8192];
        int total = 0;
        int read;
        while ((read = input.read(buffer)) >= 0) {
            if (read == 0) {
                continue;
            }
            total += read;
            if (total > MAXIMUM_PRESENTATION_BYTES) {
                throw failure("HTMX_PREVIEW_SIZE_EXCEEDED",
                        "Preview '" + source + "' exceeds "
                                + MAXIMUM_PRESENTATION_BYTES + " UTF-8 bytes.");
            }
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private static IllegalStateException failure(final String code, final String message) {
        return new IllegalStateException(code + ": " + message);
    }

    private static String safeFilename(final String value) {
        return value == null ? "" : value;
    }

    private static String bounded(final String value) {
        return value.length() <= 200 ? value : value.substring(0, 197) + "...";
    }
}
