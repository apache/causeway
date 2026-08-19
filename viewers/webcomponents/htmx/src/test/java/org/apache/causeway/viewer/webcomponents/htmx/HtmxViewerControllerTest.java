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

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

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
                .contains("<causeway-object-context logical-type=\"petclinic.PetOwner\" object-id=\"owner-1\">")
                .contains("<causeway-object editable>")
                .contains("<causeway-interaction-controller data-causeway-route-interactions>");
        assertThat(response.getHeaders().getFirst("Content-Security-Policy")).contains("default-src 'self'");
        assertThat(response.getHeaders().getFirst("HX-Push-Url")).isNull();
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
                .contains("data-custom-page")
                .containsOnlyOnce("<causeway-object-context");
        assertThat(response.getHeaders().getFirst("HX-Push-Url"))
                .isEqualTo("/app/htmx/object/petclinic.PetOwner/owner-1");
    }

    @Test
    void rendersBoundedInvalidAndLandingStates() {
        final var controller = controller(List.of());

        final var invalid = controller.route(request("/htmx/object/type/%2F", "", true));
        final var landing = controller.route(request("/htmx", "", false));

        assertThat(invalid.getBody()).contains("data-route-state=\"invalid-route\"")
                .doesNotContain("%2F");
        assertThat(invalid.getHeaders().getFirst("HX-Push-Url")).isEqualTo("/htmx");
        assertThat(landing.getBody()).contains("data-route-state=\"landing\"")
                .contains("data-causeway-home-message");
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
        final var registry = new HtmxPageFragmentRegistry(factories);
        return new HtmxViewerController(codec, new HtmxPageRenderer(codec, properties, registry));
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

    private static HtmxViewerProperties properties() {
        final var properties = new HtmxViewerProperties();
        properties.setBasePath("/htmx");
        properties.setBrand("Pet Clinic");
        properties.setWicketComparisonPath("/wicket/");
        return properties;
    }
}
