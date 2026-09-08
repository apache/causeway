/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.core.config;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.springframework.boot.test.util.TestPropertyValues;

import org.apache.causeway.core.config.CausewayConfiguration.ViewerProfiles;

class CausewayConfiguration_ViewerProfiles_Test {

    @Nested
    @DisplayName("Default Values & Logic")
    class DefaultsUnitTest {

        @Test
        @DisplayName("Should apply default map values when config is null")
        void shouldApplyDefaultMap() {
            ViewerProfiles profiles = new ViewerProfiles(null, null);

            assertThat(profiles.map())
                .hasSize(3)
                .containsEntry("wicket", "web-ui")
                .containsEntry("restful", "api,restful-only")
                .containsEntry("graphql", "api,graphql-only");
        }

        @Test
        @DisplayName("Should apply default namespace when config is null")
        void shouldApplyDefaultNamespace() {
            ViewerProfiles profiles = new ViewerProfiles(null, null);

            assertThat(profiles.namespace())
                .hasSize(1)
                .containsEntry("default", "web-ui,api");
        }

        @Test
        @DisplayName("Should resolve profiles using default namespace for unmatched packages")
        void shouldResolveProfilesUsingDefaultNamespace() {
            ViewerProfiles profiles = new ViewerProfiles(null, null);
            Set<String> resolved = profiles.profilesForNamespace("java.lang");

            assertThat(resolved).containsExactly("web-ui", "api");
        }

        @Test
        @DisplayName("Should use longest prefix match for namespace resolution")
        void shouldUseLongestPrefixMatch() {
            Map<String, String> viewer = Map.of(
                    "viewer1", "api,web-ui,mobile"
                );
            Map<String, String> namespace = Map.of(
                "com.myapp", "api",
                "com.myapp.orders", "web-ui",
                "com.myapp.orders.impl", "mobile"
            );
            ViewerProfiles profiles = new ViewerProfiles(viewer, namespace);

            assertThat(profiles.profilesForNamespace("com.myapp.orders"))
                .containsExactly("web-ui");

            assertThat(profiles.profilesForNamespace("com.myapp.orders.impl"))
                .containsExactly("mobile");

            assertThat(profiles.profilesForNamespace("com.myapp.payments.Payment"))
                .containsExactly("api");
        }

        @Test
        @DisplayName("Should parse comma-separated profiles correctly")
        void shouldParseCommaSeparatedProfiles() {
            ViewerProfiles profiles = new ViewerProfiles(null, null);
            Set<String> parsed = profiles.profilesForViewer("restful");

            assertThat(parsed).containsExactlyInAnyOrder("api", "restful-only");
        }

        @Test
        @DisplayName("Should return empty set for blank or missing viewer id")
        void shouldReturnEmptyForMissingViewer() {
            ViewerProfiles profiles = new ViewerProfiles(null, null);

            assertThat(profiles.profilesForViewer("nonexistent")).isEmpty();
            assertThat(profiles.profilesForViewer("")).isEmpty();
        }
    }

    private final ConfigurationFactory configurationFactory = new ConfigurationFactory();

    @Test
    void customViewerMapping() {
        configurationFactory.test(
            TestPropertyValues.of(
                    "causeway.viewer-profiles.map.viewer1=web-ui",
                    "causeway.viewer-profiles.map.viewer2=test,api"),
                causeway -> {
                    assertThat(causeway.viewerProfiles().profilesForViewer("viewer1")).containsExactly("web-ui");
                    assertThat(causeway.viewerProfiles().profilesForViewer("viewer2")).containsExactly("test", "api");
            });
    }

    @Test
    void throwsWhenUnmapped() {
        assertThrows(Exception.class, ()->
            configurationFactory.test(
                TestPropertyValues.of(
                        "causeway.viewer-profiles.map.viewer1=web-ui"),
                    causeway -> {}));
    }
}
