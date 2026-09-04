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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = PetClinicVueSecuredApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PetClinicVueSecuredPlaywrightAcceptance {

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

            final var deepLink = "/vue/object/petclinic.PetOwner/s_owner-mary?toolkit=native";
            page.navigate(origin() + deepLink, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            page.waitForURL("**/vue/login**");
            assertThat(page.locator("cw-graphql-client").count()).isZero();
            assertThat(menuBarRequests).isEmpty();
            page.waitForFunction("() => document.activeElement?.id === 'username'");

            page.locator("#username").fill(PetClinicSecmanDataConfiguration.USERNAME);
            page.locator("#password").fill("wrong");
            page.locator("button[type='submit']").click();
            page.waitForURL("**/vue/login?error=true**");
            assertThat(page.locator("[role='alert']").textContent()).contains("Sign-in failed");
            assertThat(menuBarRequests).isEmpty();

            page.locator("#username").fill(PetClinicSecmanDataConfiguration.USERNAME);
            page.locator("#password").fill(PetClinicSecmanDataConfiguration.PASSWORD);
            page.locator("button[type='submit']").click();
            page.waitForURL("**" + deepLink);
            try {
                page.waitForFunction("() => typeof document.querySelector('cw-graphql-client')?.executor === 'function'");
            } catch (com.microsoft.playwright.TimeoutError cause) {
                throw new AssertionError("Authenticated Vue module did not initialize; failures=" + browserFailures
                        + "; resources=" + page.evaluate("() => performance.getEntriesByType('resource').map(entry => entry.name).filter(name => name.includes('causeway'))")
                        + "; element=" + page.locator("cw-graphql-client").evaluate("element => element.outerHTML"), cause);
            }
            waitForReadyObject(page);
            assertThat(page.locator("[data-testid='vue-authentication-shell']").count()).isZero();
            assertThat(page.locator("[data-causeway-authentication-logout]").getAttribute("hidden")).isNotNull();
            page.waitForFunction("() => ['ready','partial-error'].includes(document.querySelector('cw-menubars')?.dataset.menuState)");
            page.waitForFunction("() => [...document.querySelectorAll('cw-menubar-primary, cw-menubar-secondary, cw-menubar-tertiary')].filter(element => !element.hidden).every(element => element.dataset.causewayMenubarPresentation === 'native' && !element.querySelector('cw-menubar-control'))");
            assertThat(menuBarRequests).isEmpty();
            assertThat(page.locator("cw-menubar-secondary").count()).isZero();
            assertThat(page.locator("cw-menubar-tertiary").isVisible()).isTrue();
            assertThat((Boolean) page.locator("cw-menubar-tertiary").evaluate("""
                    element => {
                      const actions = Object.values(element._projection?.actions ?? {});
                      const expected = new Map([
                        ['causeway.applib.UserMenu#me', 'Me'],
                        ['causeway.conf.ConfigurationMenu#configuration', 'Configuration'],
                        ['causeway.security.LogoutMenu#logout', 'Sign out']
                      ]);
                      const menuLabels = element._projection?.menus?.map(menu => menu.label) ?? [];
                      return menuLabels.length === 1 && menuLabels[0] === 'sven'
                        && [...expected].every(([identity, label]) => {
                          const action = actions.find(candidate => `${candidate.serviceLogicalTypeName}#${candidate.actionId}` === identity);
                          return action?.label === label
                            && action.role === 'tertiary'
                            && (identity !== 'causeway.security.LogoutMenu#logout' || action.appearance === 'sign-out');
                        });
                    }
                    """))
                    .as(page.locator("cw-menubar-tertiary").evaluate("element => Object.values(element._projection?.actions ?? {}).map(action => `${action.serviceLogicalTypeName}#${action.actionId}:${action.label}:${action.role}`)").toString())
                    .isTrue();
            assertThat(graphQlCsrfHeaders).isNotEmpty().allSatisfy(value -> assertThat(value).isNotBlank());
            final var userDisclosure = page.locator("[data-causeway-menu-disclosure]")
                    .filter(new com.microsoft.playwright.Locator.FilterOptions().setHasText("sven")).first();
            userDisclosure.click();
            final var signOutControl = page.locator(
                    "[data-service-logical-type='causeway.security.LogoutMenu'][data-action-id='logout']");
            assertThat(signOutControl.getAttribute("data-action-appearance")).isEqualTo("sign-out");
            assertThat(signOutControl.evaluate("element => getComputedStyle(element).borderTopStyle")).isEqualTo("solid");
            assertThat(page.locator("[data-service-logical-type='causeway.applib.UserMenu'][data-action-id='me']")
                    .getAttribute("data-action-appearance")).isNull();
            page.keyboard().press("Escape");

            userDisclosure.click();
            page.locator("[data-service-logical-type='causeway.applib.UserMenu'][data-action-id='me']").click();
            page.waitForURL("**/vue/object/causeway.applib.UserMemento/**");
            page.waitForFunction("() => { const route = document.querySelector('[data-causeway-route-page]'); return ['ready','partial-error'].includes(route?.dataset.routeState) && route.textContent.includes('Identity'); }");
            assertThat(page.locator("body").innerText()).contains("Identity", "Name", "sven");
            assertThat(page.locator("[data-testid='action-result']").count()).isZero();
            assertThat(page.locator("cw-action-results[data-causeway-shell-result]").getAttribute("hidden")).isNotNull();

            page.locator("[data-causeway-menu-disclosure]")
                    .filter(new com.microsoft.playwright.Locator.FilterOptions().setHasText("sven")).first().click();
            page.locator("[data-service-logical-type='causeway.conf.ConfigurationMenu'][data-action-id='configuration']").click();
            page.waitForURL("**/vue/object/causeway.conf.ConfigurationViewmodel/**");
            page.waitForFunction("() => { const route = document.querySelector('[data-causeway-route-page]'); return ['ready','partial-error'].includes(route?.dataset.routeState) && route.textContent.includes('Configuration'); }");
            assertThat(page.locator("body").innerText()).contains("Configuration").doesNotContain("me result");
            assertThat(page.locator("[data-testid='action-result']").count()).isZero();
            page.navigate(origin() + deepLink, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            waitForReadyObject(page);
            page.waitForLoadState(LoadState.NETWORKIDLE);

            final var graphQlCountBeforeLogoutPolicy = graphQlCsrfHeaders.size();
            final var frameworkLogoutClaimed = (Boolean) page.locator("[data-testid='petclinic-vue-application-shell']")
                    .evaluate("""
                            shell => {
                              const form = shell.querySelector('[data-causeway-authentication-logout]');
                              form.addEventListener('submit', event => event.preventDefault(), {once: true});
                              const event = new CustomEvent('causeway-action-request', {
                                bubbles: true,
                                composed: true,
                                cancelable: true,
                                detail: {serviceLogicalTypeName: 'causeway.security.LogoutMenu', actionId: 'logout'}
                              });
                              shell.dispatchEvent(event);
                              return event.defaultPrevented;
                            }
                            """);
            assertThat(frameworkLogoutClaimed).isTrue();
            assertThat(graphQlCsrfHeaders).hasSize(graphQlCountBeforeLogoutPolicy);

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
            page.waitForURL("**/vue/login**");
            assertThat(page.locator("#username").isVisible()).isTrue();
            assertThat(menuBarRequests).isEmpty();
            browserFailures.removeIf(message -> message.contains("status of 401"));

            page.locator("#username").fill(PetClinicSecmanDataConfiguration.USERNAME);
            page.locator("#password").fill(PetClinicSecmanDataConfiguration.PASSWORD);
            page.locator("button[type='submit']").click();
            page.waitForURL("**/vue**");
            page.waitForFunction("() => ['ready','partial-error'].includes(document.querySelector('cw-menubars')?.dataset.menuState) && [...document.querySelectorAll('cw-menubar-primary, cw-menubar-secondary, cw-menubar-tertiary')].filter(element => !element.hidden).every(element => element.querySelector('cw-menubar-control')?.dataset.widgetState === 'ready')");
            assertThat(menuBarRequests).hasSize(1);
            final var userMenu = firstVisible(page.locator("vaadin-menu-bar-button")
                    .filter(new Locator.FilterOptions().setHasText("sven")));
            userMenu.click();
            final var signOutItem = page.locator("vaadin-menu-bar-item")
                    .filter(new com.microsoft.playwright.Locator.FilterOptions().setHasText("Sign out")).last();
            signOutItem.waitFor();
            signOutItem.click();
            page.waitForURL("**/vue/login?logout=true");
            assertThat(page.locator("[role='status']").textContent()).contains("signed out");
            assertThat(menuBarRequests).hasSize(1);

            page.goBack();
            page.waitForURL("**/vue/login**");
            assertThat(page.locator("[data-testid='vue-authentication-shell']").count()).isZero();
            browserFailures.removeIf(message -> message.contains("status of 401"));
            assertThat(browserFailures).isEmpty();
        }
    }

    private static Locator firstVisible(final Locator candidates) {
        for (var index = 0; index < candidates.count(); index++) {
            var candidate = candidates.nth(index);
            if (candidate.isVisible()) {
                return candidate;
            }
        }
        throw new AssertionError("No visible locator among " + candidates.count() + " candidates");
    }

    private void waitForReadyObject(final Page page) {
        try {
            page.waitForFunction("() => ['ready','partial-error'].includes(document.querySelector('[data-causeway-route-page]')?.dataset.routeState)");
        } catch (com.microsoft.playwright.TimeoutError cause) {
            throw new AssertionError("Secured route did not become ready; url=" + page.url()
                    + "; route=" + page.locator("#causeway-vue-route").evaluate("element => element.outerHTML")
                    + "; failures=" + browserFailures
                    + "; csrfHeaders=" + graphQlCsrfHeaders, cause);
        }
    }

    private String origin() {
        return "http://localhost:" + port;
    }
}
