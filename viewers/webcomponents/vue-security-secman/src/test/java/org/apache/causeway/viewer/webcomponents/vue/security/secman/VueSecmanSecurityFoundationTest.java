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
package org.apache.causeway.viewer.webcomponents.vue.security.secman;

import org.junit.jupiter.api.Test;

import org.apache.causeway.core.config.CausewayConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VueSecmanSecurityFoundationTest {

    @Test
    void validatorRequiresExplicitCsrfSetting() {
        final var properties = new VueSecmanSecurityProperties();
        final var paths = new VueSecmanPaths(properties);

        assertThatThrownBy(() -> new VueSecmanSecurityValidator(configuration(false), properties, paths)
                .afterPropertiesSet())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(VueSecmanSecurityValidator.CSRF_PROPERTY);
    }

    @Test
    void validatorAcceptsBoundedDistinctConfiguration() {
        final var properties = new VueSecmanSecurityProperties();
        final var paths = new VueSecmanPaths(properties);

        new VueSecmanSecurityValidator(configuration(true), properties, paths).afterPropertiesSet();
    }

    @Test
    void validatorRejectsPathAndChainConflicts() {
        final var properties = new VueSecmanSecurityProperties();
        properties.setLogoutPath(properties.getLoginPath());

        assertThatThrownBy(() -> new VueSecmanSecurityValidator(
                configuration(true), properties, new VueSecmanPaths(properties)).afterPropertiesSet())
                .hasMessageContaining("must be distinct");

        properties.setLogoutPath("/logout");
        properties.setFilterChainOrder(1_001);
        assertThatThrownBy(() -> new VueSecmanSecurityValidator(
                configuration(true), properties, new VueSecmanPaths(properties)).afterPropertiesSet())
                .hasMessageContaining("between -1000 and 1000");
    }

    @Test
    void destinationPolicyAllowsOnlyCanonicalVueRoutes() {
        final var paths = new VueSecmanPaths(new VueSecmanSecurityProperties());

        assertThat(paths.isSafeViewerDestination("/vue/object/petclinic.Pet/1?tab=visits")).isTrue();
        assertThat(paths.isSafeViewerDestination("/vue")).isTrue();
        assertThat(paths.isSafeViewerDestination("https://attacker.example/vue")).isFalse();
        assertThat(paths.isSafeViewerDestination("//attacker.example/vue")).isFalse();
        assertThat(paths.isSafeViewerDestination("/vue/login")).isFalse();
        assertThat(paths.isSafeViewerDestination("/vue/logout")).isFalse();
        assertThat(paths.isSafeViewerDestination("/vue/authentication")).isFalse();
        assertThat(paths.isSafeViewerDestination("/vue/assets/app.js")).isFalse();
        assertThat(paths.isSafeViewerDestination("/graphql")).isFalse();
        assertThat(paths.isSafeViewerDestination("/wicket")).isFalse();
    }

    @Test
    void propertiesRejectMalformedPaths() {
        final var properties = new VueSecmanSecurityProperties();
        assertThatThrownBy(() -> properties.setBasePath("//attacker.example"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> properties.setAuthenticationContextPath("/../authentication"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static CausewayConfiguration configuration(final boolean allowCsrfFilters) {
        final var configuration = mock(CausewayConfiguration.class);
        final var security = mock(CausewayConfiguration.Security.class);
        final var spring = mock(CausewayConfiguration.Security.Spring.class);
        when(configuration.security()).thenReturn(security);
        when(security.spring()).thenReturn(spring);
        when(spring.allowCsrfFilters()).thenReturn(allowCsrfFilters);
        return configuration;
    }
}
