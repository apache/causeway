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
package org.apache.causeway.viewer.webcomponents.htmx.security.secman;

import java.security.Principal;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.viewer.webcomponents.htmx.HtmxViewerProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HtmxSecmanSecurityFoundationTest {

    @Test
    void validatorRequiresExplicitCsrfSetting() {
        final var properties = new HtmxSecmanSecurityProperties();
        final var paths = new HtmxSecmanPaths(new HtmxViewerProperties(), properties);

        assertThatThrownBy(() -> new HtmxSecmanSecurityValidator(configuration(false), properties, paths)
                .afterPropertiesSet())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(HtmxSecmanSecurityValidator.CSRF_PROPERTY);
    }

    @Test
    void validatorAcceptsBoundedDistinctConfiguration() {
        final var properties = new HtmxSecmanSecurityProperties();
        final var paths = new HtmxSecmanPaths(new HtmxViewerProperties(), properties);

        new HtmxSecmanSecurityValidator(configuration(true), properties, paths).afterPropertiesSet();
    }

    @Test
    void validatorRejectsPathAndChainConflicts() {
        final var viewer = new HtmxViewerProperties();
        final var properties = new HtmxSecmanSecurityProperties();
        properties.setLogoutPath(properties.getLoginPath());
        final var paths = new HtmxSecmanPaths(viewer, properties);

        assertThatThrownBy(() -> new HtmxSecmanSecurityValidator(configuration(true), properties, paths)
                .afterPropertiesSet())
                .hasMessageContaining("must be distinct");

        properties.setLogoutPath("/logout");
        properties.setFilterChainOrder(1_001);
        assertThatThrownBy(() -> new HtmxSecmanSecurityValidator(
                configuration(true), properties, new HtmxSecmanPaths(viewer, properties)).afterPropertiesSet())
                .hasMessageContaining("between -1000 and 1000");
    }

    @Test
    void destinationPolicyAllowsOnlyCanonicalViewerRoutes() {
        final var paths = new HtmxSecmanPaths(new HtmxViewerProperties(), new HtmxSecmanSecurityProperties());

        assertThat(paths.isSafeViewerDestination("/htmx/object/petclinic.Pet/1?tab=visits")).isTrue();
        assertThat(paths.isSafeViewerDestination("/htmx")).isTrue();
        assertThat(paths.isSafeViewerDestination("https://attacker.example/htmx")).isFalse();
        assertThat(paths.isSafeViewerDestination("//attacker.example/htmx")).isFalse();
        assertThat(paths.isSafeViewerDestination("/htmx/login")).isFalse();
        assertThat(paths.isSafeViewerDestination("/graphql")).isFalse();
        assertThat(paths.isSafeViewerDestination("/wicket")).isFalse();
    }

    @Test
    void shellSpiPublishesOnlyAuthenticatedIdentityCsrfPathsAndExactExclusion() {
        final var viewer = new HtmxViewerProperties();
        final var properties = new HtmxSecmanSecurityProperties();
        final var shell = new SecmanHtmxAuthenticationShell(viewer, properties);
        final var anonymous = new MockHttpServletRequest();
        assertThat(shell.state(anonymous)).isEmpty();

        final var request = new MockHttpServletRequest();
        request.setContextPath("/app");
        request.setUserPrincipal((Principal) () -> "sven");
        request.setAttribute(DefaultCsrfToken.class.getName(), new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "token"));
        request.setAttribute(org.springframework.security.web.csrf.CsrfToken.class.getName(),
                new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "token"));

        final var state = shell.state(request).orElseThrow();
        assertThat(state.username()).isEqualTo("sven");
        assertThat(state.loginPath()).isEqualTo("/app/htmx/login");
        assertThat(state.logoutPath()).isEqualTo("/app/htmx/logout");
        assertThat(state.csrfHeaderName()).isEqualTo("X-CSRF-TOKEN");
        assertThat(state.csrfParameterName()).isEqualTo("_csrf");
        assertThat(state.csrfToken()).isEqualTo("token");
        assertThat(state.excludedActions()).containsExactly(SecmanHtmxAuthenticationShell.FRAMEWORK_LOGOUT);
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
