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
package org.apache.causeway.viewer.webcomponents.sample.vue.petclinicsecured;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.transaction.annotation.Transactional;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayConfigurationRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = PetClinicVueSecuredApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PetClinicVueSecuredApplication_IntegTest {

    private static final Pattern CSRF_FIELD = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"");
    private static final Pattern CSRF_CONTEXT = Pattern.compile("\\\"csrfToken\\\":\\\"([^\\\"]+)\\\"");

    @LocalServerPort
    private int port;

    @Autowired
    private CausewayConfiguration causewayConfiguration;

    @Autowired
    private ApplicationUserRepository userRepository;

    @Test
    @Transactional
    void deterministicUserHasApplicationAndFrameworkRoles() {
        final var roleNames = userRepository.findByUsername(PetClinicSecmanDataConfiguration.USERNAME).orElseThrow()
                .getRoles().stream()
                .map(ApplicationRole::getName)
                .toList();
        assertThat(roleNames).contains(
                PetClinicSecmanDataConfiguration.ROLE_NAME,
                causewayConfiguration.extensions().secman().seed().regularUser().roleName(),
                CausewayConfigurationRoleAndPermissions.ROLE_NAME);
    }

    @Test
    void realSecmanLoginCsrfGraphQlAndLogoutJourney() throws Exception {
        final var cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        final var client = HttpClient.newBuilder()
                .cookieHandler(cookies)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        final var protectedRoute = get(client, "/vue/object/petclinic.PetOwner/s_owner-mary");
        assertThat(protectedRoute.statusCode()).isEqualTo(302);
        assertThat(protectedRoute.headers().firstValue("location").orElseThrow()).contains("/vue/login");

        final var login = get(client, "/vue/login");
        assertThat(login.statusCode()).isEqualTo(200);
        assertThat(login.body()).contains("Sign in to Pet Clinic").doesNotContain("cw-graphql-client");
        final var anonymousSessionId = sessionId(cookies);
        final var loginCsrf = token(CSRF_FIELD, login.body());

        final var authenticated = login(client, loginCsrf,
                PetClinicSecmanDataConfiguration.USERNAME,
                PetClinicSecmanDataConfiguration.PASSWORD,
                "/vue/object/petclinic.PetOwner/s_owner-mary");
        assertThat(authenticated.statusCode()).isEqualTo(302);
        assertThat(authenticated.headers().firstValue("location").orElseThrow())
                .contains("/vue/object/petclinic.PetOwner/s_owner-mary");
        assertThat(sessionId(cookies)).isNotEqualTo(anonymousSessionId);

        final var shell = get(client, "/vue/object/petclinic.PetOwner/s_owner-mary");
        assertThat(shell.statusCode()).isEqualTo(200);
        assertThat(shell.headers().firstValue("cache-control").orElseThrow()).contains("no-store");
        assertThat(shell.body())
                .contains("name=\"causeway-authentication-context\" content=\"/vue/authentication\"")
                .contains("/vue/assets/")
                .doesNotContain("csrfToken")
                .doesNotContain("sven");

        final var context = get(client, "/vue/authentication");
        assertThat(context.statusCode()).isEqualTo(200);
        assertThat(context.headers().firstValue("cache-control").orElseThrow()).contains("no-store");
        assertThat(context.body())
                .contains("\"username\":\"sven\"")
                .contains("\"csrfHeaderName\":\"X-CSRF-TOKEN\"")
                .contains("\"csrfParameterName\":\"_csrf\"")
                .contains("\"loginPath\":\"/vue/login\"")
                .contains("\"logoutPath\":\"/vue/logout\"")
                .doesNotContain("password")
                .doesNotContain("roles");
        final var currentCsrf = token(CSRF_CONTEXT, context.body());
        assertThat(get(client, "/causeway-webcomponents/context-events.mjs").statusCode()).isEqualTo(200);
        assertThat(get(client, "/causeway-webcomponents/register.mjs").statusCode()).isEqualTo(200);

        final var graphQlWithoutCsrf = graphQl(client, null);
        assertThat(graphQlWithoutCsrf.statusCode()).isEqualTo(403);
        final var graphQl = graphQl(client, currentCsrf);
        assertThat(graphQl.statusCode()).isEqualTo(200);
        assertThat(graphQl.body()).contains("\"data\"").doesNotContain("Access is denied");

        final var logoutWithoutCsrf = postForm(client, "/vue/logout", "");
        assertThat(logoutWithoutCsrf.statusCode()).isEqualTo(403);
        final var logout = postForm(client, "/vue/logout", "_csrf=" + encode(currentCsrf));
        assertThat(logout.statusCode()).isEqualTo(302);
        assertThat(logout.headers().firstValue("location").orElseThrow()).contains("/vue/login?logout=true");
        assertThat(get(client, "/vue").statusCode()).isEqualTo(302);
    }

    @Test
    void absentLockedPasswordlessAndWrongPasswordShareGenericFailure() throws Exception {
        for (final var credentials : new String[][]{
                {"absent", "pass"},
                {PetClinicSecmanDataConfiguration.LOCKED_USERNAME, PetClinicSecmanDataConfiguration.PASSWORD},
                {PetClinicSecmanDataConfiguration.PASSWORDLESS_USERNAME, "pass"},
                {PetClinicSecmanDataConfiguration.USERNAME, "wrong"}}) {
            final var cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
            final var client = HttpClient.newBuilder()
                    .cookieHandler(cookies)
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .build();
            final var csrf = token(CSRF_FIELD, get(client, "/vue/login").body());
            final var failure = login(client, csrf, credentials[0], credentials[1], null);
            assertThat(failure.statusCode()).isEqualTo(302);
            assertThat(failure.headers().firstValue("location").orElseThrow()).contains("/vue/login?error=true");
            final var outcome = get(client, failure.headers().firstValue("location").orElseThrow());
            assertThat(outcome.body())
                    .contains("Sign-in failed. Check your username and password.")
                    .doesNotContain(credentials[0]);
        }
    }

    private HttpResponse<String> login(
            final HttpClient client,
            final String csrf,
            final String username,
            final String password,
            final String destination) throws Exception {
        var form = "_csrf=" + encode(csrf)
                + "&username=" + encode(username)
                + "&password=" + encode(password);
        if (destination != null) {
            form += "&continue=" + encode(destination);
        }
        return postForm(client, "/vue/login", form);
    }

    private HttpResponse<String> graphQl(final HttpClient client, final String csrf) throws Exception {
        final var builder = HttpRequest.newBuilder(uri("/graphql"))
                .header("Content-Type", "application/json");
        if (csrf != null) {
            builder.header("X-CSRF-TOKEN", csrf);
        }
        return client.send(builder.POST(HttpRequest.BodyPublishers.ofString(
                        "{\"query\":\"query SecuredHome { rich { application { home { kind logicalTypeName } } } }\"}"))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(final HttpClient client, final String path) throws Exception {
        final var destination = path.startsWith("http://") || path.startsWith("https://")
                ? URI.create(path)
                : uri(path);
        return client.send(HttpRequest.newBuilder(destination).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postForm(
            final HttpClient client,
            final String path,
            final String form) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(final String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private static String token(final Pattern pattern, final String html) {
        final var matcher = pattern.matcher(html);
        assertThat(matcher.find()).as(html).isTrue();
        return matcher.group(1).replace("&amp;", "&");
    }

    private static String sessionId(final CookieManager cookies) {
        return cookies.getCookieStore().getCookies().stream()
                .filter(cookie -> cookie.getName().equals("JSESSIONID"))
                .findFirst().orElseThrow().getValue();
    }

    private static String encode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
