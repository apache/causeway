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
import java.util.Arrays;
import java.util.LinkedHashMap;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

final class HtmxClasspathShellLoader {

    static final String LOCATION = "classpath*:/META-INF/causeway/webcomponents/shells/htmx.html";
    static final int MAXIMUM_SHELL_BYTES = 256 * 1024;
    private static final String APPLICATION_SOURCE = "resource:htmx.html";
    private static final String DEFAULT_SOURCE = "internal:default-shell.html";

    private final ResourcePatternResolver resolver;
    private final HtmxViewerProperties.ResourcePageMode resourcePageMode;

    HtmxClasspathShellLoader(final ResourcePatternResolver resolver) {
        this(resolver, HtmxViewerProperties.ResourcePageMode.CACHED);
    }

    HtmxClasspathShellLoader(
            final ResourcePatternResolver resolver,
            final HtmxViewerProperties.ResourcePageMode resourcePageMode) {
        this.resolver = java.util.Objects.requireNonNull(resolver);
        this.resourcePageMode = java.util.Objects.requireNonNull(resourcePageMode);
    }

    HtmxShellDefinition load() {
        final Resource[] discovered;
        try {
            discovered = resolver.getResources(LOCATION);
        } catch (IOException ex) {
            throw failure("HTMX_SHELL_DISCOVERY_FAILED", "Private application shell discovery failed.");
        }
        final var unique = new LinkedHashMap<String, Resource>();
        Arrays.stream(discovered)
                .sorted(java.util.Comparator.comparing(Resource::getDescription))
                .forEach(resource -> unique.putIfAbsent(identity(resource), resource));
        if (unique.size() > 1) {
            throw failure("HTMX_SHELL_DUPLICATE", "More than one private application shell was discovered.");
        }
        if (unique.isEmpty()) {
            final var html = HtmxDeclarativeTemplate.load("default-shell.html");
            HtmxDeclarativeTemplate.validateApplicationShell(html, DEFAULT_SOURCE);
            return HtmxShellDefinition.cached(DEFAULT_SOURCE, false, html);
        }
        final var resource = unique.values().iterator().next();
        final var startupHtml = validatedShell(resource);
        return resourcePageMode == HtmxViewerProperties.ResourcePageMode.RELOAD
                ? new HtmxShellDefinition(APPLICATION_SOURCE, true, () -> validatedShell(resource))
                : HtmxShellDefinition.cached(APPLICATION_SOURCE, true, startupHtml);
    }

    private static String validatedShell(final Resource resource) {
        final var html = decode(resource);
        HtmxDeclarativeTemplate.validateApplicationShell(html, APPLICATION_SOURCE);
        return html;
    }

    static String decode(final Resource resource) {
        final byte[] bytes;
        try (InputStream input = resource.getInputStream()) {
            bytes = readBounded(input);
        } catch (IOException ex) {
            throw failure("HTMX_SHELL_UNREADABLE", "Private application shell cannot be read.");
        }
        if (bytes.length == 0) {
            throw failure("HTMX_SHELL_EMPTY", "Private application shell is empty.");
        }
        final String html;
        try {
            html = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException ex) {
            throw failure("HTMX_SHELL_INVALID_UTF8", "Private application shell is not valid UTF-8.");
        }
        if (html.indexOf('\0') >= 0) {
            throw failure("HTMX_SHELL_NUL_CONTENT", "Private application shell contains a NUL character.");
        }
        return html;
    }

    private static byte[] readBounded(final InputStream input) throws IOException {
        final var output = new ByteArrayOutputStream(Math.min(8192, MAXIMUM_SHELL_BYTES));
        final var buffer = new byte[8192];
        int total = 0;
        int read;
        while ((read = input.read(buffer)) >= 0) {
            if (read == 0) {
                continue;
            }
            total += read;
            if (total > MAXIMUM_SHELL_BYTES) {
                throw failure(
                        "HTMX_SHELL_SIZE_EXCEEDED",
                        "Private application shell exceeds " + MAXIMUM_SHELL_BYTES + " UTF-8 bytes.");
            }
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private static String identity(final Resource resource) {
        try {
            return resource.getURL().toExternalForm();
        } catch (IOException ex) {
            return resource.getDescription();
        }
    }

    private static IllegalStateException failure(final String code, final String message) {
        return new IllegalStateException(code + ": " + message);
    }
}
