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

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import org.apache.causeway.applib.services.iactn.InteractionService;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserStatus;
import org.apache.causeway.viewer.webcomponents.htmx.HtmxViewerProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig(HtmxSecmanHttpSecurityTest.TestConfiguration.class)
class HtmxSecmanHttpSecurityTest {

    @Autowired private WebApplicationContext context;
    @Autowired private ApplicationUserRepository repository;
    @Autowired @Qualifier("Secman") private PasswordEncoder passwordEncoder;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        reset(repository);
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void chainProtectsOnlyViewerAndGraphQlAndKeepsLoginPublic() throws Exception {
        mvc.perform(get("/htmx/login"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("no-store")))
                .andExpect(content().string(containsString("Sign in to Petclinic")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("cw-graphql-client"))));
        mvc.perform(get("/htmx/login.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"));
        mvc.perform(get("/htmx/object/42"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/htmx/login"));
        mvc.perform(post("/graphql").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/unrelated"))
                .andExpect(status().isOk())
                .andExpect(content().string("unrelated"));
    }

    @Test
    void fragmentAuthenticationLossSignalsFullBrowserRedirect() throws Exception {
        mvc.perform(get("/htmx/object/42").header("HX-Request", "true"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("HX-Redirect", "/htmx/login?continue=%2Fhtmx%2Fobject%2F42"));
    }

    @Test
    void loginRejectsMissingCsrfAndInvalidCredentialsGenerically() throws Exception {
        mvc.perform(post("/htmx/login").param("username", "absent").param("password", "wrong"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/htmx/login").with(csrf().useInvalidToken())
                        .param("username", "absent").param("password", "wrong"))
                .andExpect(status().isForbidden());
        when(repository.findByUsername("absent")).thenReturn(Optional.empty());
        mvc.perform(post("/htmx/login").with(csrf())
                        .param("username", "absent").param("password", "wrong"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/htmx/login?error=true"));
        mvc.perform(get("/htmx/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sign-in failed. Check your username and password.")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("absent"))));
    }

    @Test
    void validLoginMigratesSessionAndRestoresSafeDeepLink() throws Exception {
        final var applicationUser = applicationUser("sven", "correct horse");
        when(repository.findByUsername("sven")).thenReturn(Optional.of(applicationUser));
        final var session = new MockHttpSession();
        final var initialId = session.getId();
        mvc.perform(get("/htmx/object/42?tab=visits").session(session))
                .andExpect(status().isFound());

        final var result = mvc.perform(post("/htmx/login").session(session).with(csrf())
                        .param("username", "sven").param("password", "correct horse"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/htmx/object/42?tab=visits"))
                .andReturn();

        assertThat(result.getRequest().getSession(false).getId()).isNotEqualTo(initialId);
    }

    @Test
    void unsafeContinueFallsBackToViewerRoot() throws Exception {
        final var applicationUser = applicationUser("sven", "correct horse");
        when(repository.findByUsername("sven")).thenReturn(Optional.of(applicationUser));

        mvc.perform(post("/htmx/login").with(csrf())
                        .param("username", "sven")
                        .param("password", "correct horse")
                        .param("continue", "https://attacker.example/htmx"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/htmx"));
    }

    @Test
    void csrfProtectsGraphQlAndLogoutAndLogoutRemovesAuthentication() throws Exception {
        final var applicationUser = applicationUser("sven", "correct horse");
        when(repository.findByUsername("sven")).thenReturn(Optional.of(applicationUser));
        mvc.perform(post("/graphql").with(user("sven")).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/graphql").with(user("sven")).with(csrf().useInvalidToken())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/graphql").with(user("sven")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string("graphql"));

        final var login = mvc.perform(post("/htmx/login").with(csrf())
                        .param("username", "sven").param("password", "correct horse"))
                .andReturn();
        final var session = (MockHttpSession) login.getRequest().getSession(false);
        mvc.perform(post("/htmx/logout").session(session))
                .andExpect(status().isForbidden());
        mvc.perform(post("/htmx/logout").session(session).with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/htmx/login?logout=true"))
                .andExpect(header().string("Cache-Control", containsString("no-store")));
        mvc.perform(get("/htmx"))
                .andExpect(status().isFound());
    }

    @Test
    void authenticatedForbiddenOutcomeIsNotReportedAsSessionExpiry() throws Exception {
        mvc.perform(get("/htmx/forbidden").with(user("sven")))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("HX-Redirect"));
    }

    private ApplicationUser applicationUser(final String username, final String password) {
        final var role = mock(ApplicationRole.class);
        when(role.getName()).thenReturn("petclinic-user");
        final var user = mock(ApplicationUser.class);
        when(user.getUsername()).thenReturn(username);
        when(user.getEncryptedPassword()).thenReturn(passwordEncoder.encode(password));
        when(user.getStatus()).thenReturn(ApplicationUserStatus.UNLOCKED);
        when(user.getRoles()).thenReturn(Set.of(role));
        when(user.getLanguage()).thenReturn(Locale.ENGLISH);
        return user;
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @org.springframework.context.annotation.Import(HtmxSecmanSecurityConfiguration.class)
    static class TestConfiguration {

        @Bean
        HtmxViewerProperties htmxViewerProperties() {
            final var properties = new HtmxViewerProperties();
            properties.setBrand("Petclinic");
            return properties;
        }

        @Bean
        HtmxSecmanSecurityProperties htmxSecmanSecurityProperties() {
            return new HtmxSecmanSecurityProperties();
        }

        @Bean
        CausewayConfiguration causewayConfiguration() {
            final var configuration = mock(CausewayConfiguration.class);
            final var security = mock(CausewayConfiguration.Security.class);
            final var spring = mock(CausewayConfiguration.Security.Spring.class);
            when(configuration.security()).thenReturn(security);
            when(security.spring()).thenReturn(spring);
            when(spring.allowCsrfFilters()).thenReturn(true);
            return configuration;
        }

        @Bean
        ApplicationUserRepository applicationUserRepository() {
            return mock(ApplicationUserRepository.class);
        }

        @Bean
        InteractionService interactionService() throws Exception {
            final var service = mock(InteractionService.class);
            when(service.callAnonymous(any())).thenAnswer(invocation ->
                    ((Callable<?>) invocation.getArgument(0)).call());
            return service;
        }

        @Bean
        @Qualifier("Secman")
        PasswordEncoder secmanPasswordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        TestEndpoints testEndpoints() {
            return new TestEndpoints();
        }
    }

    @RestController
    static class TestEndpoints {
        @GetMapping({"/htmx", "/htmx/object/42"})
        String viewer() {
            return "viewer";
        }

        @GetMapping("/htmx/forbidden")
        org.springframework.http.ResponseEntity<Void> forbidden() {
            return org.springframework.http.ResponseEntity.status(403).build();
        }

        @PostMapping("/graphql")
        String graphQl() {
            return "graphql";
        }

        @GetMapping("/unrelated")
        String unrelated() {
            return "unrelated";
        }
    }
}
