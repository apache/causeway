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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class HtmxDeclarativeTemplate {

    static final String LOGICAL_TYPE_TOKEN = "{{causeway.logicalType}}";
    static final String OBJECT_ID_TOKEN = "{{causeway.objectId}}";

    private static final int MAXIMUM_TEMPLATE_BYTES = 512 * 1024;
    private static final Pattern UNRESOLVED_TOKEN = Pattern.compile("\\{\\{causeway\\.[A-Za-z][A-Za-z0-9]*}}", Pattern.MULTILINE);
    private static final Pattern COMMENTS = Pattern.compile("<!--.*?-->", Pattern.DOTALL);
    private static final String ROOT = "/META-INF/causeway/webcomponents/htmx/";

    private HtmxDeclarativeTemplate() {
    }

    static String load(final String filename) {
        final var resource = ROOT + filename;
        try (var input = HtmxDeclarativeTemplate.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw failure("HTMX_TEMPLATE_MISSING", "Required declarative template is unavailable.");
            }
            final var bytes = input.readNBytes(MAXIMUM_TEMPLATE_BYTES + 1);
            if (bytes.length > MAXIMUM_TEMPLATE_BYTES) {
                throw failure("HTMX_TEMPLATE_SIZE_EXCEEDED", "Required declarative template exceeds its byte bound.");
            }
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw failure("HTMX_TEMPLATE_UNREADABLE", "Required declarative template cannot be read.");
        }
    }

    static void validateShell(final String html) {
        requireSingleTag(html, "cw-graphql-client", "HTMX_SHELL_GRAPHQL_CLIENT_INVALID");
        requireSingleTag(html, "cw-menubars", "HTMX_SHELL_MENUBARS_INVALID");
        requireSingleOccurrence(html, "<main id=\"causeway-route\"", "HTMX_SHELL_ROUTE_INVALID");
        requireSingleOccurrence(html, "<cw-action-results id=\"causeway-result\"", "HTMX_SHELL_RESULT_INVALID");
        requireSingleOccurrence(html, "{{causeway.routeContent}}", "HTMX_SHELL_ROUTE_SLOT_INVALID");
    }

    static void validateResourcePage(final String html, final String source) {
        final var validatedHtml = COMMENTS.matcher(html).replaceAll("");
        validateObjectPageStructure(validatedHtml, source);
        requireSingleOccurrence(validatedHtml, LOGICAL_TYPE_TOKEN, "HTMX_PAGE_LOGICAL_TYPE_BINDING_INVALID");
        requireSingleOccurrence(validatedHtml, OBJECT_ID_TOKEN, "HTMX_PAGE_OBJECT_ID_BINDING_INVALID");
        final var routeContexts = openingTags(validatedHtml, "cw-object-context").stream()
                .filter(tag -> tag.contains("logical-type=\"" + LOGICAL_TYPE_TOKEN + "\"")
                        && tag.contains("object-id=\"" + OBJECT_ID_TOKEN + "\""))
                .count();
        if (routeContexts != 1) {
            throw failure("HTMX_PAGE_ROUTE_BINDING_INVALID", "Object page must declare one token-bound route context.");
        }
    }

    static String bindObjectPage(
            final String html,
            final HtmxObjectRoute route,
            final String source,
            final boolean requireTokens) {
        if (requireTokens) {
            validateResourcePage(html, source);
        }
        final var logicalType = HtmxPageRenderer.escape(route.logicalTypeName());
        final var objectId = HtmxPageRenderer.escape(route.objectId());
        final var bound = html
                .replace(LOGICAL_TYPE_TOKEN, logicalType)
                .replace(OBJECT_ID_TOKEN, objectId);
        validateObjectPageStructure(bound, source);
        final var routeContexts = openingTags(bound, "cw-object-context").stream()
                .filter(tag -> tag.contains("logical-type=\"" + logicalType + "\"")
                        && tag.contains("object-id=\"" + objectId + "\""))
                .count();
        if (routeContexts != 1) {
            throw failure("HTMX_PAGE_ROUTE_BINDING_INVALID", "Object page does not bind exactly one route context to canonical identity.");
        }
        rejectUnresolvedPageTokens(bound);
        return bound;
    }

    static String bind(final String template, final Map<String, String> bindings, final String unresolvedCode) {
        final var matcher = UNRESOLVED_TOKEN.matcher(template);
        final var resolved = new StringBuilder(template.length());
        final var used = new HashSet<String>();
        while (matcher.find()) {
            final var token = matcher.group();
            final var key = token.substring("{{causeway.".length(), token.length() - 2);
            final var value = bindings.get(key);
            if (value == null) {
                throw failure(unresolvedCode, "Declarative template contains an unresolved reserved binding.");
            }
            used.add(key);
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(resolved);
        if (!used.equals(bindings.keySet())) {
            throw failure(unresolvedCode, "Declarative template is missing a required binding.");
        }
        return resolved.toString();
    }

    private static void validateObjectPageStructure(final String html, final String source) {
        if (html == null || html.isBlank()) {
            throw failure("HTMX_PAGE_EMPTY", "Object page is empty.");
        }
        if (openingTags(html, "cw-object-context").isEmpty()) {
            throw failure("HTMX_PAGE_OBJECT_CONTEXT_INVALID", "Object page has no declared object context.");
        }
        requireSingleTag(html, "cw-interaction-controller", "HTMX_PAGE_INTERACTION_CONTROLLER_INVALID");
        requireSingleOccurrence(html, "data-route-state=", "HTMX_PAGE_ROUTE_BOUNDARY_INVALID");
        if (!html.contains("data-testid=\"causeway-route-page\"")) {
            throw failure("HTMX_PAGE_ROUTE_BOUNDARY_INVALID", "Object page has no declared route boundary: " + bounded(source));
        }
    }

    private static List<String> openingTags(final String html, final String tagName) {
        final var withoutComments = COMMENTS.matcher(html).replaceAll("");
        final var matcher = Pattern.compile(
                "<\\s*" + Pattern.quote(tagName) + "\\b[^>]*>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(withoutComments);
        final var tags = new java.util.ArrayList<String>();
        while (matcher.find()) {
            tags.add(matcher.group());
        }
        return List.copyOf(tags);
    }

    private static void requireSingleTag(
            final String html,
            final String tagName,
            final String code) {
        final var count = openingTags(html, tagName).size();
        if (count != 1) {
            throw failure(code, "Declarative template contract requires exactly one <" + tagName + "> element.");
        }
    }

    private static void rejectUnresolvedPageTokens(final String html) {
        if (UNRESOLVED_TOKEN.matcher(html).find()) {
            throw failure("HTMX_PAGE_BINDING_UNRESOLVED", "Declarative template contains an unresolved reserved binding.");
        }
    }

    private static void requireSingleOccurrence(
            final String value,
            final String needle,
            final String code) {
        var count = 0;
        var offset = 0;
        while ((offset = value.indexOf(needle, offset)) >= 0) {
            count++;
            offset += needle.length();
        }
        if (count != 1) {
            throw failure(code, "Declarative template contract requires exactly one occurrence of " + bounded(needle) + ".");
        }
    }

    private static IllegalStateException failure(final String code, final String message) {
        return new IllegalStateException(code + ": " + bounded(message));
    }

    private static String bounded(final String value) {
        return value.length() <= 240 ? value : value.substring(0, 237) + "...";
    }
}
