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

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

final class HtmxPageRenderer {

    private static final String DOCUMENT_TEMPLATE = HtmxDeclarativeTemplate.load("document.html");
    private static final String DEFAULT_SHELL_TEMPLATE = HtmxDeclarativeTemplate.load("default-shell.html");
    private static final String GENERIC_OBJECT_PAGE_TEMPLATE = HtmxDeclarativeTemplate.load("generic-object-page.html");
    private static final String LANDING_PAGE_TEMPLATE = HtmxDeclarativeTemplate.load("landing-page.html");
    private static final String INVALID_ROUTE_PAGE_TEMPLATE = HtmxDeclarativeTemplate.load("invalid-route-page.html");

    static {
        HtmxDeclarativeTemplate.validateDocumentTemplate(DOCUMENT_TEMPLATE);
        HtmxDeclarativeTemplate.validateApplicationShell(DEFAULT_SHELL_TEMPLATE, "internal:default-shell.html");
        HtmxDeclarativeTemplate.validateResourcePage(GENERIC_OBJECT_PAGE_TEMPLATE, "generic-object-page.html");
    }

    private final HtmxRouteCodec routeCodec;
    private final HtmxViewerProperties properties;
    private final HtmxPageFragmentRegistry fragmentRegistry;
    private final HtmxShellDefinition shellDefinition;

    HtmxPageRenderer(
            final HtmxRouteCodec routeCodec,
            final HtmxViewerProperties properties,
            final HtmxPageFragmentRegistry fragmentRegistry) {
        this(
                routeCodec,
                properties,
                fragmentRegistry,
                HtmxShellDefinition.cached("internal:default-shell.html", false, DEFAULT_SHELL_TEMPLATE));
    }

    HtmxPageRenderer(
            final HtmxRouteCodec routeCodec,
            final HtmxViewerProperties properties,
            final HtmxPageFragmentRegistry fragmentRegistry,
            final HtmxShellDefinition shellDefinition) {
        this.routeCodec = routeCodec;
        this.properties = properties;
        this.fragmentRegistry = fragmentRegistry;
        this.shellDefinition = shellDefinition;
    }

    String renderObjectFragment(final HtmxObjectRoute route) {
        final var custom = fragmentRegistry.find(route.logicalTypeName());
        if (custom.isEmpty()) {
            return HtmxDeclarativeTemplate.bindObjectPage(
                    GENERIC_OBJECT_PAGE_TEMPLATE,
                    route,
                    "generic-object-page.html",
                    true);
        }
        final var page = custom.orElseThrow();
        return HtmxDeclarativeTemplate.bindObjectPage(
                page.render(route),
                route,
                page.safeSourceIdentifier(),
                page.source() == HtmxPageDefinition.Source.RESOURCE);
    }

    String renderLandingFragment() {
        return LANDING_PAGE_TEMPLATE;
    }

    String renderInvalidRouteFragment() {
        return HtmxDeclarativeTemplate.bind(
                INVALID_ROUTE_PAGE_TEMPLATE,
                java.util.Map.of("homePath", escape(routeCodec.rootPath())),
                "HTMX_INVALID_ROUTE_BINDING_UNRESOLVED");
    }

    String renderShell(final String contextPath, final String fragment, final String canonicalPath) {
        return renderShell(contextPath, fragment, canonicalPath, Optional.empty());
    }

    String renderShell(
            final String contextPath,
            final String fragment,
            final String canonicalPath,
            final Optional<HtmxAuthenticationShell.State> authenticationState) {
        final var context = normalizeContextPath(contextPath);
        final var basePath = context + routeCodec.basePath();
        final var graphQlEndpoint = context + normalizeOriginPath(properties.getGraphQlEndpoint(), "/graphql");
        final var language = safeLanguage(properties.getLanguage());
        final var comparisonLink = comparisonLink(context);
        final var applicationStylesheet = applicationStylesheet(context);
        final var authenticationMetadata = authenticationMetadata(authenticationState);
        final var authenticationChrome = authenticationChrome(authenticationState);
        final var shell = HtmxDeclarativeTemplate.bind(
                shellDefinition.render(),
                java.util.Map.of(
                        "basePath", escape(basePath),
                        "brand", escape(properties.getBrand()),
                        "graphQlEndpoint", escape(graphQlEndpoint),
                        "authenticationChrome", authenticationChrome,
                        "routeContent", fragment,
                        "comparisonLink", comparisonLink),
                Set.of("graphQlEndpoint", "authenticationChrome", "routeContent"),
                "HTMX_SHELL_BINDING_UNRESOLVED");
        return HtmxDeclarativeTemplate.bind(
                DOCUMENT_TEMPLATE,
                java.util.Map.ofEntries(
                        java.util.Map.entry("language", language),
                        java.util.Map.entry("basePath", escape(basePath)),
                        java.util.Map.entry("canonicalPath", escape(context + canonicalPath)),
                        java.util.Map.entry("widgetAttributes", widgetAttributes(context)),
                        java.util.Map.entry("authenticationMetadata", authenticationMetadata),
                        java.util.Map.entry("brand", escape(properties.getBrand())),
                        java.util.Map.entry("contextPath", escape(context)),
                        java.util.Map.entry("applicationStylesheet", applicationStylesheet),
                        java.util.Map.entry("applicationShell", shell)),
                "HTMX_DOCUMENT_BINDING_UNRESOLVED");
    }

    private static String authenticationMetadata(final Optional<HtmxAuthenticationShell.State> state) {
        if (state.isEmpty()) {
            return "";
        }
        final var value = state.orElseThrow();
        final var exclusions = value.excludedActions().stream()
                .map(HtmxAuthenticationShell.ActionIdentity::externalForm)
                .sorted()
                .collect(java.util.stream.Collectors.joining(","));
        return """
                <meta name="causeway-auth-login" content="%s">
                <meta name="causeway-auth-csrf-header" content="%s">
                <meta name="causeway-auth-csrf-parameter" content="%s">
                <meta name="causeway-auth-csrf-token" content="%s">
                <meta name="causeway-auth-excluded-actions" content="%s">
                """.formatted(
                escape(value.loginPath()),
                escape(value.csrfHeaderName()),
                escape(value.csrfParameterName()),
                escape(value.csrfToken()),
                escape(exclusions));
    }

    private static String authenticationChrome(final Optional<HtmxAuthenticationShell.State> state) {
        if (state.isEmpty()) {
            return "";
        }
        final var value = state.orElseThrow();
        return """
                <form method="post" action="%s" data-causeway-logout-form hidden>
                  <input type="hidden" name="%s" value="%s">
                </form>
                """.formatted(
                escape(value.logoutPath()),
                escape(value.csrfParameterName()),
                escape(value.csrfToken()));
    }

    private String widgetAttributes(final String contextPath) {
        final var minimumSearchLength = Math.max(0, properties.getReferenceMinimumSearchLength());
        final var maximumResults = Math.max(1, properties.getReferenceMaximumResults());
        final var referenceWidgets = properties.isEffectiveVaadinReferenceWidgets() ? "vaadin" : "native";
        return " data-causeway-component-toolkit=\"" + properties.getResolvedComponentToolkit() + "\""
                + " data-causeway-toolkit-source=\"" + properties.getToolkitConfigurationSource() + "\""
                + " data-causeway-editor-toolkit=\"" + properties.getResolvedEditorToolkit() + "\""
                + " data-causeway-presentation=\"" + (properties.isEffectiveVaadinPresentation() ? "vaadin" : "native") + "\""
                + " data-causeway-action-buttons=\"" + (properties.isEffectiveVaadinActionButtons() ? "vaadin" : "native") + "\""
                + " data-causeway-collection-grid=\"" + (properties.isEffectiveVaadinCollectionGrid() ? "vaadin" : "native") + "\""
                + " data-causeway-grid-family=\"" + (properties.isEffectiveVaadinCollectionGrid() ? "healthy" : "native") + "\""
                + " data-causeway-grid-module-url=\"" + escape(contextPath)
                + "/causeway-webcomponents/vaadin-grid/vaadin-grid.js\""
                + " data-causeway-grid-policy-revision=\"0\""
                + " data-causeway-application-menubar=\""
                + (properties.isEffectiveVaadinApplicationMenubar() ? "vaadin" : "native") + "\""
                + " data-causeway-menubar-family=\""
                + (properties.isEffectiveVaadinApplicationMenubar() ? "healthy" : "native") + "\""
                + " data-causeway-application-menubar-url=\"" + escape(contextPath)
                + "/causeway-webcomponents/vaadin-menubar/vaadin-menubar.js\""
                + " data-causeway-menubar-policy-revision=\"0\""
                + " data-causeway-reference-widgets=\"" + referenceWidgets + "\""
                + " data-causeway-reference-minimum-search-length=\"" + minimumSearchLength + "\""
                + " data-causeway-reference-maximum-results=\"" + maximumResults + "\""
                + " data-causeway-field-families=\""
                + escape(properties.getEffectiveVaadinFieldFamilies()) + "\""
                + " data-causeway-resource-page-mode=\""
                + properties.getResourcePageMode().name().toLowerCase(Locale.ROOT) + "\"";
    }

    private String applicationStylesheet(final String contextPath) {
        final var configured = properties.getApplicationStylesheet();
        if (configured == null || configured.isBlank()) {
            return "";
        }
        final var path = contextPath + normalizeOriginPath(configured, "/css/application.css");
        return "<link rel=\"stylesheet\" href=\"" + escape(path) + "\">";
    }

    private String comparisonLink(final String contextPath) {
        final var configured = properties.getWicketComparisonPath();
        if (configured == null || configured.isBlank()) {
            return "";
        }
        final var path = contextPath + normalizeOriginPath(configured, "/wicket/");
        return "<a href=\"" + escape(path) + "\">Compare Wicket viewer</a>";
    }

    private static String normalizeOriginPath(final String configured, final String fallback) {
        final var candidate = configured == null || configured.isBlank() ? fallback : configured.trim();
        if (!candidate.startsWith("/") || candidate.startsWith("//") || candidate.contains("?") || candidate.contains("#")) {
            throw new IllegalStateException("The HTMX viewer GraphQL endpoint must be an origin-relative path.");
        }
        return candidate;
    }

    private static String normalizeContextPath(final String contextPath) {
        if (contextPath == null || contextPath.isBlank() || "/".equals(contextPath)) {
            return "";
        }
        return contextPath.endsWith("/") ? contextPath.substring(0, contextPath.length() - 1) : contextPath;
    }

    private static String safeLanguage(final String configured) {
        final var candidate = configured == null ? "en" : configured.trim().toLowerCase(Locale.ROOT);
        return candidate.matches("[a-z]{2,8}(?:-[a-z0-9]{1,8})*") ? candidate : "en";
    }

    static String escape(final String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
