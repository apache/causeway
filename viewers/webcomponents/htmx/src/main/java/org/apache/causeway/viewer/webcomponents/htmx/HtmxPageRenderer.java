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
                .map(page -> page.render(route))
                .orElse("<causeway-object editable></causeway-object>");
        final var pageKind = custom.isPresent() ? "custom" : "generic";
        final var pageSource = custom
                .map(page -> page.source().attributeValue())
                .orElse("generic");
        return """
                <section class="causeway-route-page causeway-route-object" data-route-state="loading" data-page-kind="%s" data-page-source="%s" data-testid="causeway-route-page" tabindex="-1" aria-label="Object page">
                  <causeway-object-context logical-type="%s" object-id="%s">
                    %s
                    <causeway-interaction-controller data-causeway-route-interactions></causeway-interaction-controller>
                  </causeway-object-context>
                </section>
                """.formatted(
                        pageKind,
                        pageSource,
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
                  %s
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
                        %s
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
                        widgetAttributes(),
                        authenticationMetadata,
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
                        authenticationChrome,
                        fragment,
                        comparisonLink);
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
                <div class="causeway-shell-user" data-testid="causeway-shell-user">
                  <span class="causeway-shell-username">%s</span>
                  <form method="post" action="%s" data-causeway-logout-form>
                    <input type="hidden" name="%s" value="%s">
                    <button type="submit" class="causeway-shell-logout">Sign out</button>
                  </form>
                </div>
                """.formatted(
                        escape(value.username()),
                        escape(value.logoutPath()),
                        escape(value.csrfParameterName()),
                        escape(value.csrfToken()));
    }

    private String widgetAttributes() {
        final var minimumSearchLength = Math.max(0, properties.getReferenceMinimumSearchLength());
        final var maximumResults = Math.max(1, properties.getReferenceMaximumResults());
        final var referenceWidgets = properties.isEffectiveVaadinReferenceWidgets() ? "vaadin" : "native";
        return " data-causeway-editor-toolkit=\"" + properties.getResolvedEditorToolkit() + "\""
                + " data-causeway-reference-widgets=\"" + referenceWidgets + "\""
                + " data-causeway-reference-minimum-search-length=\"" + minimumSearchLength + "\""
                + " data-causeway-reference-maximum-results=\"" + maximumResults + "\""
                + " data-causeway-field-families=\""
                + escape(properties.getEffectiveVaadinFieldFamilies()) + "\"";
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
