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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HtmxViewerControllerTest {

    private final HtmxViewerProperties properties = properties();
    private final HtmxRouteCodec codec = new HtmxRouteCodec(properties.getBasePath());

    @Test
    void returnsCompleteStableShellForOrdinaryObjectRequest() {
        final var controller = controller(List.of());
        final var request = request("/app/htmx/object/petclinic.PetOwner/owner-1", "/app", false);

        final var response = controller.route(request);

        assertThat(response.getBody())
                .contains("<!doctype html>")
                .contains("<causeway-menubars>")
                .contains("<main id=\"causeway-route\"")
                .contains("data-page-kind=\"generic\"")
                .contains("data-page-source=\"generic\"")
                .contains("<causeway-object-context logical-type=\"petclinic.PetOwner\" object-id=\"owner-1\">")
                .contains("<causeway-object editable>")
                .contains("<causeway-interaction-controller data-causeway-route-interactions>");
        assertThat(response.getHeaders().getFirst("Content-Security-Policy"))
                .contains("default-src 'self'")
                .contains("sha256-0wLqlhzs6Y30XLr3aVbYP1PYgStuEbKPfSQ0hPe+kY4=")
                .contains("sha256-8YLhGMhYZnbpzrpjhu2GmLRimv2CABlByy++wN9OR0w=")
                .contains("sha256-3QT3eM+q9TclSqSU3m57G/bQwWnIhIFfAxgKI5k9zxs=")
                .contains("style-src-attr 'none'")
                .doesNotContain("'unsafe-inline'");
        assertThat(response.getBody())
                .contains("data-causeway-editor-toolkit=\"vaadin\"")
                .contains("data-causeway-reference-widgets=\"vaadin\"")
                .contains("data-causeway-field-families=\"basic,numeric,local-temporal\"");
        assertThat(response.getHeaders().getFirst("HX-Push-Url")).isNull();
    }

    @Test
    void preservesLongOpaqueIdentityAcrossFullFragmentAndHistoryResponses() {
        final var controller = controller(List.of());
        final var identifier = "memento-" + "a".repeat(3000);
        final var route = new HtmxObjectRoute("demo.CompositeValuesPage", identifier);
        final var canonicalPath = codec.objectPath(route);

        final var full = controller.route(request("/app" + canonicalPath, "/app", false));
        final var fragment = controller.route(request("/app" + canonicalPath, "/app", true));
        final var historyRequest = request("/app" + canonicalPath, "/app", true);
        historyRequest.addHeader("HX-History-Restore-Request", "true");
        final var history = controller.route(historyRequest);

        assertThat(full.getBody()).contains("object-id=\"" + identifier + "\"");
        assertThat(fragment.getBody()).contains("object-id=\"" + identifier + "\"");
        assertThat(fragment.getHeaders().getFirst("HX-Push-Url")).isEqualTo("/app" + canonicalPath);
        assertThat(history.getBody()).contains("object-id=\"" + identifier + "\"");
        assertThat(history.getHeaders().getFirst("HX-Push-Url")).isNull();
    }

    @Test
    void explicitNativePolicyRemovesVaadinAdaptersAndHashes() {
        properties.setEditorToolkit(HtmxViewerProperties.EditorToolkit.NATIVE);

        final var response = controller(List.of()).route(request("/htmx", "", false));

        assertThat(response.getHeaders().getFirst("Content-Security-Policy"))
                .doesNotContain("sha256-")
                .contains("style-src-attr 'none'")
                .doesNotContain("'unsafe-inline'");
        assertThat(response.getBody())
                .contains("data-causeway-editor-toolkit=\"native\"")
                .contains("data-causeway-reference-widgets=\"native\"")
                .contains("data-causeway-field-families=\"\"");
    }

    @Test
    @SuppressWarnings("deprecation")
    void addsOnlyPinnedReferenceWidgetStyleHashesInDeprecatedCompatibilityMode() {
        properties.setVaadinReferenceWidgets(true);
        properties.setReferenceMinimumSearchLength(3);
        properties.setReferenceMaximumResults(40);
        final var response = controller(List.of()).route(request("/htmx", "", false));

        assertThat(response.getHeaders().getFirst("Content-Security-Policy"))
                .contains("style-src-elem 'self' 'sha256-xGEkK13KcZJdGhZfeIjuH6IWVGTHtjs/IqUVa8T0XXw='")
                .contains("style-src-attr 'none'")
                .doesNotContain("'unsafe-inline'");
        assertThat(response.getBody())
                .contains("data-causeway-editor-toolkit=\"compatibility\"")
                .contains("data-causeway-reference-widgets=\"vaadin\"")
                .contains("data-causeway-reference-minimum-search-length=\"3\"")
                .contains("data-causeway-reference-maximum-results=\"40\"")
                .contains("data-causeway-field-families=\"\"");
    }

    @Test
    @SuppressWarnings("deprecation")
    void addsOnlyEnabledFieldFamilyHashesInDeprecatedCompatibilityMode() {
        properties.setVaadinFieldFamilies("numeric, basic");

        final var response = controller(List.of()).route(request("/htmx", "", false));
        final var policy = response.getHeaders().getFirst("Content-Security-Policy");

        assertThat(response.getBody())
                .contains("data-causeway-editor-toolkit=\"compatibility\"")
                .contains("data-causeway-reference-widgets=\"native\"")
                .contains("data-causeway-field-families=\"basic,numeric\"");
        assertThat(policy)
                .contains("sha256-0wLqlhzs6Y30XLr3aVbYP1PYgStuEbKPfSQ0hPe+kY4=")
                .contains("sha256-8YLhGMhYZnbpzrpjhu2GmLRimv2CABlByy++wN9OR0w=")
                .doesNotContain("sha256-3QT3eM+q9TclSqSU3m57G/bQwWnIhIFfAxgKI5k9zxs=")
                .contains("style-src-attr 'none'")
                .doesNotContain("'unsafe-inline'");
        assertThat(occurrences(policy, "'sha256-xGEkK13KcZJdGhZfeIjuH6IWVGTHtjs/IqUVa8T0XXw='"))
                .isEqualTo(2);
    }

    @Test
    @SuppressWarnings("deprecation")
    void deduplicatesDeprecatedReferenceAndFieldHashesAndRejectsInvalidFamilies() {
        properties.setVaadinReferenceWidgets(true);
        properties.setVaadinFieldFamilies("local-temporal");

        final var response = controller(List.of()).route(request("/htmx", "", false));
        final var policy = response.getHeaders().getFirst("Content-Security-Policy");

        assertThat(policy).contains("sha256-3QT3eM+q9TclSqSU3m57G/bQwWnIhIFfAxgKI5k9zxs=");
        assertThat(occurrences(policy, "'sha256-xGEkK13KcZJdGhZfeIjuH6IWVGTHtjs/IqUVa8T0XXw='"))
                .isEqualTo(2);
        assertThatThrownBy(() -> properties.setVaadinFieldFamilies("basic,unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("basic, numeric, and local-temporal");
        assertThatThrownBy(() -> properties.setVaadinFieldFamilies("basic,basic"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("deprecation")
    void explicitCommonPolicyPreventsDeprecatedValuesFromMixingShellPolicy() {
        properties.setVaadinReferenceWidgets(false);
        properties.setVaadinFieldFamilies("");
        properties.setEditorToolkit(HtmxViewerProperties.EditorToolkit.VAADIN);

        final var response = controller(List.of()).route(request("/htmx", "", false));
        final var policy = response.getHeaders().getFirst("Content-Security-Policy");

        assertThat(response.getBody())
                .contains("data-causeway-editor-toolkit=\"vaadin\"")
                .contains("data-causeway-reference-widgets=\"vaadin\"")
                .contains("data-causeway-field-families=\"basic,numeric,local-temporal\"");
        assertThat(policy)
                .contains("sha256-0wLqlhzs6Y30XLr3aVbYP1PYgStuEbKPfSQ0hPe+kY4=")
                .contains("sha256-8YLhGMhYZnbpzrpjhu2GmLRimv2CABlByy++wN9OR0w=")
                .contains("sha256-3QT3eM+q9TclSqSU3m57G/bQwWnIhIFfAxgKI5k9zxs=");
    }

    @Test
    void returnsOnlyCustomFragmentAndCanonicalHistoryForHtmxRequest() {
        final var custom = new HtmxPageFragmentFactory() {
            @Override
            public String logicalTypeName() {
                return "petclinic.PetOwner";
            }

            @Override
            public String render(final HtmxObjectRoute route) {
                return "<article data-custom-page><causeway-property member=\"name\"></causeway-property></article>";
            }
        };
        final var controller = controller(List.of(custom));
        final var request = request("/app/htmx/object/petclinic.PetOwner/owner-1", "/app", true);

        final var response = controller.route(request);

        assertThat(response.getBody())
                .doesNotContain("<!doctype html>")
                .contains("data-page-kind=\"custom\"")
                .contains("data-page-source=\"factory\"")
                .contains("data-custom-page")
                .containsOnlyOnce("<causeway-object-context");
        assertThat(response.getHeaders().getFirst("HX-Push-Url"))
                .isEqualTo("/app/htmx/object/petclinic.PetOwner/owner-1");
    }

    @Test
    void rendersLiteralResourcePageInsideEscapedRouteContext() {
        final var resource = HtmxPageDefinition.resource(
                "petclinic.PetOwner",
                "resource:petclinic.PetOwner.html",
                "<article data-resource-page>{{objectId}}<causeway-property member=\"name\"></causeway-property></article>");
        final var controller = controller(List.of(), List.of(resource));

        final var response = controller.route(request(
                "/htmx/object/petclinic.PetOwner/owner%26one",
                "",
                true));

        assertThat(response.getBody())
                .contains("data-page-kind=\"custom\"")
                .contains("data-page-source=\"resource\"")
                .contains("object-id=\"owner&amp;one\"")
                .contains("{{objectId}}")
                .contains("data-resource-page")
                .containsOnlyOnce("<causeway-object-context")
                .containsOnlyOnce("<causeway-interaction-controller");
    }

    @Test
    void rendersBoundedInvalidAndLandingStates() {
        final var controller = controller(List.of());

        final var invalid = controller.route(request("/htmx/object/type/%2F", "", true));
        final var overlong = controller.route(request("/htmx/object/type/" + "x".repeat(4097), "", true));
        final var landing = controller.route(request("/htmx", "", false));

        assertThat(invalid.getBody()).contains("data-route-state=\"invalid-route\"")
                .doesNotContain("%2F");
        assertThat(invalid.getHeaders().getFirst("HX-Push-Url")).isEqualTo("/htmx");
        assertThat(overlong.getBody()).contains("data-route-state=\"invalid-route\"")
                .doesNotContain("x".repeat(256));
        assertThat(overlong.getHeaders().getFirst("HX-Push-Url")).isEqualTo("/htmx");
        assertThat(landing.getBody()).contains("data-route-state=\"landing\"")
                .contains("data-causeway-home-message");
    }

    @Test
    void authenticatedFullShellRendersBoundedCsrfIdentityLogoutAndExactPolicyMetadata() {
        final var controller = controller(List.of());
        controller.setAuthenticationShell(request -> Optional.of(new HtmxAuthenticationShell.State(
                "Sven & Co",
                "/htmx/login",
                "/htmx/logout",
                "X-CSRF-TOKEN",
                "_csrf",
                "token<bounded>",
                Set.of(new HtmxAuthenticationShell.ActionIdentity(
                        "causeway.security.LogoutMenu", "logout")))));

        final var response = controller.route(request("/htmx", "", false));

        assertThat(response.getBody())
                .contains("<meta name=\"causeway-auth-login\" content=\"/htmx/login\">")
                .contains("<meta name=\"causeway-auth-csrf-header\" content=\"X-CSRF-TOKEN\">")
                .contains("<meta name=\"causeway-auth-csrf-token\" content=\"token&lt;bounded&gt;\">")
                .contains("causeway.security.LogoutMenu#logout")
                .contains("<span class=\"causeway-shell-username\">Sven &amp; Co</span>")
                .contains("<form method=\"post\" action=\"/htmx/logout\" data-causeway-logout-form>")
                .contains("<input type=\"hidden\" name=\"_csrf\" value=\"token&lt;bounded&gt;\">");
    }

    @Test
    void historyRestoreReceivesOnlyTheRouteFragmentWithoutChangingHistory() {
        final var controller = controller(List.of());
        final var request = request("/htmx/object/petclinic.PetOwner/owner-1", "", true);
        request.addHeader("HX-History-Restore-Request", "true");

        final var response = controller.route(request);

        assertThat(response.getBody())
                .doesNotContain("<!doctype html>")
                .contains("data-page-kind=\"generic\"")
                .containsOnlyOnce("<causeway-object-context");
        assertThat(response.getHeaders().getFirst("HX-Push-Url")).isNull();
    }

    private HtmxViewerController controller(final List<HtmxPageFragmentFactory> factories) {
        return controller(factories, List.of());
    }

    private HtmxViewerController controller(
            final List<HtmxPageFragmentFactory> factories,
            final List<HtmxPageDefinition> resourcePages) {
        final var registry = new HtmxPageFragmentRegistry(factories, resourcePages);
        return new HtmxViewerController(codec, new HtmxPageRenderer(codec, properties, registry), properties);
    }

    private static MockHttpServletRequest request(
            final String requestUri,
            final String contextPath,
            final boolean htmx) {
        final var request = new MockHttpServletRequest("GET", requestUri);
        request.setRequestURI(requestUri);
        request.setContextPath(contextPath);
        if (htmx) {
            request.addHeader("HX-Request", "true");
        }
        return request;
    }

    private static int occurrences(final String value, final String candidate) {
        return value.split(java.util.regex.Pattern.quote(candidate), -1).length - 1;
    }

    private static HtmxViewerProperties properties() {
        final var properties = new HtmxViewerProperties();
        properties.setBasePath("/htmx");
        properties.setBrand("Pet Clinic");
        properties.setWicketComparisonPath("/wicket/");
        return properties;
    }
}
