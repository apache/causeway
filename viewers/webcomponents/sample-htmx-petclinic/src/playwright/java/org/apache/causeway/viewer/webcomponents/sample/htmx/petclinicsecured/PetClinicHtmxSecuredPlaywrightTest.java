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
package org.apache.causeway.viewer.webcomponents.sample.htmx.petclinicsecured;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = PetClinicHtmxSecuredApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PetClinicHtmxSecuredPlaywrightTest {

    @LocalServerPort
    private int port;

    private final List<String> browserFailures = new ArrayList<>();
    private final List<String> graphQlCsrfHeaders = new ArrayList<>();
    private Playwright playwright;
    private Browser browser;

    @BeforeAll
    void startBrowser() {
        playwright = Playwright.create();
        final var options = new BrowserType.LaunchOptions()
                .setHeadless(Boolean.parseBoolean(System.getProperty("playwright.headless", "true")));
        final var executable = System.getProperty("playwright.chromium.executable", "").trim();
        final var channel = System.getProperty("playwright.chromium.channel", "").trim();
        if (!executable.isEmpty()) {
            options.setExecutablePath(Path.of(executable));
        } else if (!channel.isEmpty()) {
            options.setChannel(channel);
        }
        browser = playwright.chromium().launch(options);
    }

    @AfterAll
    void stopBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @Test
    void anonymousLoginDeepLinkMutationCsrfLogoutHistoryAndExpiryJourney() {
        try (var context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 900))) {
            final var page = context.newPage();
            final var menuBarRequests = new ArrayList<String>();
            page.onPageError(error -> browserFailures.add("page: " + error));
            page.onConsoleMessage(message -> {
                if ("error".equals(message.type())) {
                    browserFailures.add("console: " + message.text());
                }
            });
            page.onRequest(request -> {
                if (request.url().contains("/graphql") && "POST".equals(request.method())) {
                    graphQlCsrfHeaders.add(request.headers().get("x-csrf-token"));
                }
                if (request.url().contains("/causeway-webcomponents/vaadin-menubar/")) {
                    menuBarRequests.add(request.url());
                }
            });

            final var deepLink = "/htmx/object/petclinic.PetOwner/s_owner-mary";
            page.navigate(origin() + deepLink, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            page.waitForURL("**/htmx/login**");
            assertThat(page.locator("cw-graphql-client").count()).isZero();
            assertThat(menuBarRequests).isEmpty();
            page.waitForFunction("() => document.activeElement?.id === 'username'");

            page.locator("#username").fill(PetClinicSecmanDataConfiguration.USERNAME);
            page.locator("#password").fill("wrong");
            page.locator("button[type='submit']").click();
            page.waitForURL("**/htmx/login?error=true**");
            assertThat(page.locator("[role='alert']").textContent()).contains("Sign-in failed");
            assertThat(menuBarRequests).isEmpty();

            page.locator("#username").fill(PetClinicSecmanDataConfiguration.USERNAME);
            page.locator("#password").fill(PetClinicSecmanDataConfiguration.PASSWORD);
            page.locator("button[type='submit']").click();
            page.waitForURL("**" + deepLink);
            assertThat(page.locator("meta[name='causeway-auth-csrf-token']").getAttribute("content")).isNotBlank();
            try {
                page.waitForFunction("() => typeof document.querySelector('cw-graphql-client')?.executor === 'function'");
            } catch (com.microsoft.playwright.TimeoutError cause) {
                throw new AssertionError("Authenticated HTMX module did not initialize; failures=" + browserFailures
                        + "; resources=" + page.evaluate("() => performance.getEntriesByType('resource').map(entry => entry.name).filter(name => name.includes('causeway'))")
                        + "; element=" + page.locator("cw-graphql-client").evaluate("element => element.outerHTML"), cause);
            }
            waitForReadyObject(page);
            assertThat(page.locator("[data-testid='causeway-shell-user']").textContent()).contains("sven");
            page.waitForFunction("() => ['ready','partial-error'].includes(document.querySelector('cw-menubars')?.dataset.menuState)");
            page.waitForFunction("() => [...document.querySelectorAll('cw-menubar-primary, cw-menubar-secondary, cw-menubar-tertiary')].filter(element => !element.hidden).every(element => element.dataset.causewayMenubarPresentation?.startsWith('vaadin-') && element.querySelector('cw-menubar-control')?.dataset.widgetState === 'ready')");
            assertThat(menuBarRequests).hasSize(1);
            assertThat(page.locator("[data-causeway-service-action][data-service-logical-type='causeway.security.LogoutMenu'][data-action-id='logout']").count())
                    .isZero();
            assertThat(page.locator("cw-menubar-primary, cw-menubar-secondary, cw-menubar-tertiary")
                    .evaluateAll("elements => elements.some(element => Object.values(element._projection?.actions ?? {}).some(action => action.serviceLogicalTypeName === 'causeway.security.LogoutMenu' && action.actionId === 'logout'))"))
                    .isEqualTo(false);
            assertThat(graphQlCsrfHeaders).isNotEmpty().allSatisfy(value -> assertThat(value).isNotBlank());

            final var property = page.locator("cw-property[id='knownAs']");
            property.locator("[data-causeway-action='edit']").click();
            final var editor = property.locator("[data-causeway-editor]");
            editor.waitFor();
            editor.evaluate("(element, value) => { element.value = value; element.dispatchEvent(new Event('input', {bubbles:true, composed:true})); element.dispatchEvent(new Event('change', {bubbles:true, composed:true})); }", "Mary Secured");
            property.locator("[data-causeway-action='save']").click();
            try {
                page.waitForFunction("() => { const property = document.querySelector(\"cw-property[id='knownAs']\"); return property?.querySelector('cw-field-editor[data-mode=\"view\"]')?.dataset.value === 'Mary Secured' || property?.querySelector('.causeway-property-value')?.textContent.includes('Mary Secured'); }");
            } catch (com.microsoft.playwright.TimeoutError cause) {
                throw new AssertionError("Secured property update did not settle; property=" + property.evaluate("element => element.outerHTML")
                        + "; failures=" + browserFailures + "; csrfHeaders=" + graphQlCsrfHeaders, cause);
            }

            final var rejectedStatus = (Number) page.evaluate("""
                    async () => (await fetch('/graphql', {
                      method: 'POST',
                      headers: {'Content-Type': 'application/json'},
                      body: JSON.stringify({query: 'query Rejected { rich { application { home { kind } } } }'})
                    })).status
                    """);
            assertThat(rejectedStatus.intValue()).isEqualTo(403);
            browserFailures.removeIf(message -> message.contains("status of 403"));

            context.clearCookies();
            page.locator(".causeway-shell-brand").click();
            page.waitForURL("**/htmx/login**");
            assertThat(page.locator("#username").isVisible()).isTrue();
            assertThat(menuBarRequests).hasSize(1);
            browserFailures.removeIf(message -> message.contains("status of 401"));

            page.locator("#username").fill(PetClinicSecmanDataConfiguration.USERNAME);
            page.locator("#password").fill(PetClinicSecmanDataConfiguration.PASSWORD);
            page.locator("button[type='submit']").click();
            page.waitForURL("**/htmx**");
            page.waitForFunction("() => ['ready','partial-error'].includes(document.querySelector('cw-menubars')?.dataset.menuState) && document.querySelector('cw-menubar-control')?.dataset.widgetState === 'ready'");
            assertThat(menuBarRequests).hasSize(2);
            page.locator("[data-causeway-logout-form] button[type='submit']").click();
            page.waitForURL("**/htmx/login?logout=true");
            assertThat(page.locator("[role='status']").textContent()).contains("signed out");
            assertThat(menuBarRequests).hasSize(2);

            page.goBack();
            page.waitForURL("**/htmx/login**");
            assertThat(page.locator("[data-testid='causeway-shell-user']").count()).isZero();
            browserFailures.removeIf(message -> message.contains("status of 401"));
            assertThat(browserFailures).isEmpty();
        }
    }

    private void waitForReadyObject(final Page page) {
        try {
            page.waitForFunction("() => ['ready','partial-error'].includes(document.querySelector('[data-testid=\"causeway-route-page\"]')?.dataset.routeState)");
        } catch (com.microsoft.playwright.TimeoutError cause) {
            throw new AssertionError("Secured route did not become ready; url=" + page.url()
                    + "; route=" + page.locator("#causeway-route").evaluate("element => element.outerHTML")
                    + "; failures=" + browserFailures
                    + "; csrfHeaders=" + graphQlCsrfHeaders, cause);
        }
    }

    private String origin() {
        return "http://localhost:" + port;
    }
}
