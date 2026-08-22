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

final class HtmxPageRenderer {

    private static final String HTMX_VERSION = "2.0.6";

    private final HtmxRouteCodec routeCodec;
    private final HtmxViewerProperties properties;
    private final HtmxPageFragmentRegistry fragmentRegistry;

    HtmxPageRenderer(
            final HtmxRouteCodec routeCodec,
            final HtmxViewerProperties properties,
            final HtmxPageFragmentRegistry fragmentRegistry) {
        this.routeCodec = routeCodec;
        this.properties = properties;
        this.fragmentRegistry = fragmentRegistry;
    }

    String renderObjectFragment(final HtmxObjectRoute route) {
        final var custom = fragmentRegistry.find(route.logicalTypeName());
        final var content = custom
                .map(factory -> factory.render(route))
                .orElse("<causeway-object editable></causeway-object>");
        final var pageKind = custom.isPresent() ? "custom" : "generic";
        return """
                <section class="causeway-route-page causeway-route-object" data-route-state="loading" data-page-kind="%s" data-testid="causeway-route-page" tabindex="-1" aria-label="Object page">
                  <causeway-object-context logical-type="%s" object-id="%s">
                    %s
                    <causeway-interaction-controller data-causeway-route-interactions></causeway-interaction-controller>
                  </causeway-object-context>
                </section>
                """.formatted(
                        pageKind,
                        escape(route.logicalTypeName()),
                        escape(route.objectId()),
                        content);
    }

    String renderLandingFragment() {
        return """
                <section class="causeway-route-page causeway-route-landing" data-route-state="landing" data-testid="causeway-route-page" tabindex="-1" aria-labelledby="causeway-route-heading">
                  <div class="causeway-landing-card">
                    <p class="causeway-eyebrow">Apache Causeway</p>
                    <h1 id="causeway-route-heading">Welcome</h1>
                    <p data-causeway-home-message>Select an application action or wait while the configured home page is resolved.</p>
                  </div>
                </section>
                """;
    }

    String renderInvalidRouteFragment() {
        return """
                <section class="causeway-route-page causeway-route-error" data-route-state="invalid-route" data-testid="causeway-route-page" tabindex="-1" aria-labelledby="causeway-route-heading">
                  <div class="causeway-status-card causeway-status-danger" role="alert">
                    <p class="causeway-eyebrow">Invalid route</p>
                    <h1 id="causeway-route-heading">This application route is not valid</h1>
                    <p>Use the application menus or return to the home page.</p>
                    <a class="causeway-button causeway-button-primary" href="%s" data-causeway-route-link>Return home</a>
                  </div>
                </section>
                """.formatted(escape(routeCodec.rootPath()));
    }

    String renderShell(final String contextPath, final String fragment, final String canonicalPath) {
        final var context = normalizeContextPath(contextPath);
        final var basePath = context + routeCodec.basePath();
        final var graphQlEndpoint = context + normalizeOriginPath(properties.getGraphQlEndpoint(), "/graphql");
        final var language = safeLanguage(properties.getLanguage());
        final var comparisonLink = comparisonLink(context);
        final var applicationStylesheet = applicationStylesheet(context);
        return """
                <!doctype html>
                <html lang="%s" data-causeway-htmx-base="%s" data-causeway-canonical-path="%s"%s>
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <meta name="description" content="Generic Apache Causeway application viewer using semantic web components and HTMX routing.">
                  <meta name="color-scheme" content="light dark">
                  <link rel="icon" href="data:,">
                  <meta name="htmx-config" content='{"historyCacheSize":0,"historyRestoreAsHxRequest":false,"includeIndicatorStyles":false}'>
                  <title>%s</title>
                  <link rel="stylesheet" href="%s/causeway-webcomponents/component-styles.css">
                  <link rel="stylesheet" href="%s/causeway-webcomponents/theme.css">
                  <link rel="stylesheet" href="%s/causeway-htmx/causeway-htmx.css">
                  %s
                  <script defer src="%s/webjars/htmx.org/%s/dist/htmx.min.js"></script>
                  <script type="module" src="%s/causeway-htmx/causeway-htmx.mjs"></script>
                </head>
                <body class="causeway-app-shell">
                  <a class="causeway-skip-link" href="#causeway-route">Skip to main content</a>
                  <causeway-graphql-client endpoint="%s">
                    <header class="causeway-shell-header">
                      <div class="causeway-shell-navbar">
                        <a class="causeway-shell-brand" href="%s" data-causeway-route-link aria-label="%s home">
                          <span class="causeway-shell-mark" aria-hidden="true">C</span>
                          <span>%s</span>
                        </a>
                        <causeway-menubars></causeway-menubars>
                      </div>
                    </header>
                    <div id="causeway-route-loading" class="causeway-route-loading htmx-indicator" role="status" aria-live="polite">Loading page…</div>
                    <div id="causeway-route-announcement" class="causeway-visually-hidden" aria-live="polite" aria-atomic="true"></div>
                    <aside id="causeway-result" class="causeway-shell-result" data-testid="causeway-shell-result" aria-live="polite" hidden></aside>
                    <main id="causeway-route" class="causeway-shell-main" data-testid="causeway-route" data-navigation-generation="0" hx-history-elt hx-history="false" aria-busy="false">
                      %s
                    </main>
                    <footer class="causeway-shell-footer">
                      <span>Powered by Apache Causeway</span>
                      %s
                    </footer>
                  </causeway-graphql-client>
                </body>
                </html>
                """.formatted(
                        language,
                        escape(basePath),
                        escape(context + canonicalPath),
                        referenceWidgetAttributes(),
                        escape(properties.getBrand()),
                        escape(context),
                        escape(context),
                        escape(context),
                        applicationStylesheet,
                        escape(context),
                        HTMX_VERSION,
                        escape(context),
                        escape(graphQlEndpoint),
                        escape(basePath),
                        escape(properties.getBrand()),
                        escape(properties.getBrand()),
                        fragment,
                        comparisonLink);
    }

    private String referenceWidgetAttributes() {
        if (!properties.isVaadinReferenceWidgets()) {
            return "";
        }
        final var minimumSearchLength = Math.max(0, properties.getReferenceMinimumSearchLength());
        final var maximumResults = Math.max(1, properties.getReferenceMaximumResults());
        return " data-causeway-reference-widgets=\"vaadin\""
                + " data-causeway-reference-minimum-search-length=\"" + minimumSearchLength + "\""
                + " data-causeway-reference-maximum-results=\"" + maximumResults + "\"";
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
