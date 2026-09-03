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

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HtmxDeclarativeTemplateTest {

    @Test
    void bindsOnlyTokensPresentInTheOriginalTemplate() {
        assertThat(HtmxDeclarativeTemplate.bind(
                "{{causeway.content}} {{causeway.brand}}",
                Map.of(
                        "content", "<article>{{causeway.brand}}</article>",
                        "brand", "Pet Clinic"),
                "TEST_BINDING_INVALID"))
                .isEqualTo("<article>{{causeway.brand}}</article> Pet Clinic");
    }

    @Test
    void permitsKnownOptionalBindingsToBeAbsent() {
        assertThat(HtmxDeclarativeTemplate.bind(
                "{{causeway.required}}",
                Map.of("required", "present", "optional", "unused"),
                Set.of("required"),
                "TEST_BINDING_INVALID"))
                .isEqualTo("present");

        assertThatThrownBy(() -> HtmxDeclarativeTemplate.bind(
                "{{causeway.optional}}",
                Map.of("required", "secret", "optional", "present"),
                Set.of("required"),
                "TEST_BINDING_INVALID"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TEST_BINDING_INVALID")
                .hasMessageNotContaining("secret");
    }

    @Test
    void acceptsFlexibleShellLayoutAndNestedRouteWrappers() {
        final var shell = """
                <body class="custom">
                  <cw-graphql-client endpoint="{{causeway.graphQlEndpoint}}">
                    <aside><cw-menubars></cw-menubars></aside>
                    {{causeway.authenticationChrome}}
                    <div id="causeway-route-loading"></div>
                    <div id="causeway-route-announcement"></div>
                    <cw-action-results id="causeway-result"></cw-action-results>
                    <div id="causeway-route"><div><span>{{causeway.routeContent}}</span></div></div>
                  </cw-graphql-client>
                </body>
                """;

        HtmxDeclarativeTemplate.validateApplicationShell(shell, "fixture:shell");
    }

    @Test
    void rejectsUnknownAndUnusedBindingsWithoutDisclosingValues() {
        assertThatThrownBy(() -> HtmxDeclarativeTemplate.bind(
                "{{causeway.unknown}}",
                Map.of("secret", "do-not-disclose"),
                "TEST_BINDING_INVALID"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TEST_BINDING_INVALID")
                .hasMessageNotContaining("do-not-disclose");

        assertThatThrownBy(() -> HtmxDeclarativeTemplate.bind(
                "plain",
                Map.of("secret", "do-not-disclose"),
                "TEST_BINDING_INVALID"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TEST_BINDING_INVALID")
                .hasMessageNotContaining("do-not-disclose");
    }
}
