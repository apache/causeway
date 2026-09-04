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
package org.apache.causeway.viewer.webcomponents.sample.htmx.petclinic;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.ColorScheme;
import com.microsoft.playwright.options.WaitUntilState;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import org.apache.causeway.viewer.webcomponents.sample.petclinic.domain.PetOwnerRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = PetClinicHtmxApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PetClinicHtmxPlaywrightTest {

    private static final String ROUTE_PAGE = "[data-testid='causeway-route-page']";
    private static final String PROMPT = "[data-testid='action-prompt']";

    @LocalServerPort
    private int port;

    @Autowired
    private PetOwnerRepository ownerRepository;

    private final List<String> browserFailures = new ArrayList<>();
    private final List<String> graphQLRequests = new ArrayList<>();
    private final List<String> previewRequests = new ArrayList<>();
    private final List<String> toolkitRequests = new ArrayList<>();

    private Playwright playwright;
    private Browser browser;
    private BrowserContext browserContext;
    private Page page;

    @BeforeAll
    void startBrowser() {
        playwright = Playwright.create();
        final var launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(Boolean.parseBoolean(System.getProperty("playwright.headless", "true")));
        final var executable = System.getProperty("playwright.chromium.executable", "").trim();
        final var channel = System.getProperty("playwright.chromium.channel", "").trim();
        if (!executable.isEmpty()) {
            launchOptions.setExecutablePath(Path.of(executable));
        } else if (!channel.isEmpty()) {
            launchOptions.setChannel(channel);
        }
        browser = playwright.chromium().launch(launchOptions);
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

    @BeforeEach
    void openPage() {
        browserFailures.clear();
        graphQLRequests.clear();
        previewRequests.clear();
        toolkitRequests.clear();
        browserContext = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1440, 900)
                .setColorScheme(ColorScheme.LIGHT));
        browserContext.addInitScript("""
                (() => {
                  globalThis.__causewayPlaywrightFailures = [];
                  const record = message => globalThis.__causewayPlaywrightFailures.push(String(message));
                  globalThis.addEventListener('error', event => {
                    if (!String(event.message).startsWith('ResizeObserver loop')) record(`page error: ${event.message}`);
                  });
                  globalThis.addEventListener('unhandledrejection', event => record(`unhandled rejection: ${event.reason}`));
                  const originalFetch = globalThis.fetch.bind(globalThis);
                  globalThis.fetch = async (...args) => {
                    const response = await originalFetch(...args);
                    const url = String(response.url || args[0]);
                    if (!response.ok && !(response.status === 404
                        && (url.includes('/_collection-presentations/') || url.includes('/_previews/')))) {
                      record(`HTTP ${response.status}: ${url}`);
                    }
                    if (/\\/graphql(?:\\?|$)/.test(url)) {
                      try {
                        const payload = await response.clone().json();
                        if (Array.isArray(payload?.errors) && payload.errors.length > 0) {
                          record(`GraphQL errors: ${payload.errors.map(error => error.message).join(' | ')}`);
                        }
                      } catch (error) {
                        if (error?.name !== 'AbortError') {
                          record(`GraphQL response was not JSON: ${error}`);
                        }
                      }
                    }
                    return response;
                  };
                })();
                """);
        page = browserContext.newPage();
        page.onConsoleMessage(message -> {
            if ("error".equals(message.type())
                    && !(message.text().contains("Failed to load resource") && message.text().contains("404"))) {
                browserFailures.add("console: " + message.text());
            }
        });
        page.onPageError(error -> browserFailures.add("page: " + error));
        page.onRequest(request -> {
            if (request.url().contains("/graphql") && "POST".equals(request.method())) {
                graphQLRequests.add(request.postData());
            }
            if (request.url().contains("/_previews/")) {
                previewRequests.add(request.url());
            }
            if (request.url().contains("/causeway-webcomponents/vaadin-reference/")
                    || request.url().contains("/causeway-webcomponents/vaadin-fields/")
                    || request.url().contains("/causeway-webcomponents/vaadin-actions/")
                    || request.url().contains("/causeway-webcomponents/vaadin-grid/")
                    || request.url().contains("/causeway-webcomponents/vaadin-menubar/")) {
                toolkitRequests.add(request.url());
            }
        });
        page.onRequestFailed(request -> {
            final var failure = request.failure();
            final var expectedOptionalPresentationMiss = (request.url().contains("/_collection-presentations/")
                    || request.url().contains("/_previews/"))
                    && failure != null && failure.contains("ERR_ABORTED");
            if (!(request.url().contains("/graphql") && failure != null && failure.contains("ERR_ABORTED"))
                    && !expectedOptionalPresentationMiss) {
                browserFailures.add("request: " + request.method() + " " + request.url() + " " + failure);
            }
        });
    }

    @AfterEach
    void closePage() {
        if (nativeToolkit()) {
            assertThat(toolkitRequests).isEmpty();
        }
        assertNoBrowserFailures();
        if (browserContext != null) {
            browserContext.close();
        }
    }

    @Test
    @Order(1)
    void routesHomeObjectsHistoryCollectionsAndResponsiveLayout() {
        openHome();
        assertApplicationUsesAvailableWidth();

        assertThat(page.locator("html").getAttribute("data-causeway-component-toolkit"))
                .isEqualTo(nativeToolkit() ? "native" : "vaadin");
        assertThat(page.locator("html").getAttribute("data-causeway-presentation"))
                .isEqualTo(nativeToolkit() ? "native" : "vaadin");
        assertThat(page.locator("html").getAttribute("data-causeway-action-buttons"))
                .isEqualTo(nativeToolkit() ? "native" : "vaadin");
        assertThat(page.locator("html").getAttribute("data-causeway-collection-grid"))
                .isEqualTo(nativeToolkit() ? "native" : "vaadin");
        assertThat(page.locator("html").getAttribute("data-causeway-application-menubar"))
                .isEqualTo(nativeToolkit() ? "native" : "vaadin");
        assertThat(toolkitRequests.stream().filter(url -> url.contains("/vaadin-menubar/vaadin-menubar.js")).count())
                .isEqualTo(nativeToolkit() ? 0 : 1);
        assertThat(toolkitRequests.stream().noneMatch(url -> !url.contains("/vaadin-menubar/"))).isTrue();
        assertThat(page.locator("body").getAttribute("data-testid")).isEqualTo("petclinic-application-shell");
        assertThat(page.locator("body cw-graphql-client").count()).isEqualTo(1);
        assertThat(page.locator("html").getAttribute("data-causeway-shell-context-error")).isNull();
        assertThat(page.locator("#causeway-route").getAttribute("data-causeway-route-context-error")).isNull();
        assertThat(page.locator("#causeway-route > [data-testid='causeway-route-page'] > cw-object-context").count())
                .isEqualTo(1);
        assertThat(page.locator("#causeway-route > [data-testid='causeway-route-page'] > cw-object-context > cw-interaction-controller").count())
                .isEqualTo(1);
        assertThat(page.locator(ROUTE_PAGE).getAttribute("data-page-kind")).isEqualTo("custom");
        assertThat(page.locator(ROUTE_PAGE).getAttribute("data-page-source")).isEqualTo("resource");
        assertThat(page.locator("[data-testid='petclinic-custom-home']").isVisible()).isTrue();
        assertFocused(ROUTE_PAGE);
        waitForCollectionRows("petOwners", 5);
        waitForCollectionRows("futureVisits", 10);
        assertObjectLinkIcon(page.locator("cw-collection[id='petOwners'] cw-object-link").first());
        assertThat(page.locator("cw-collection[id='petOwners']").getAttribute("paged")).isEqualTo("5");
        assertThat(page.locator("cw-collection[id='futureVisits']").getAttribute("paged")).isEqualTo("10");
        assertThat(page.locator("cw-collection[id='petOwners'] [data-causeway-grid-next]").isVisible()).isTrue();
        assertThat(page.locator("cw-collection[id='futureVisits'] .causeway-collection-label").innerText())
                .isEqualTo("Next appointments");
        assertThat(page.locator("cw-collection[id='futureVisits'] .causeway-collection-description").innerText())
                .isEqualTo("Scheduled visits that have not yet taken place.");
        assertThat(page.locator("cw-collection[id='petOwners'] .causeway-collection-description").count())
                .isZero();
        assertCollectionPresentation("petOwners", "narrow");
        assertCollectionPresentation("futureVisits", "narrow");
        assertThat(toolkitRequests.stream().filter(url -> url.contains("/vaadin-menubar/vaadin-menubar.js")).count())
                .isEqualTo(nativeToolkit() ? 0 : 1);
        assertThat(toolkitRequests.stream().noneMatch(url -> !url.contains("/vaadin-menubar/"))).isTrue();

        final var visitSearch = page.locator("cw-collection[id='futureVisits'] [data-causeway-collection-search]");
        assertThat(visitSearch.isVisible()).isTrue();
        visitSearch.fill("vaccination");
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='futureVisits']\")?.collectionState?.window?.totalCount === 2");
        waitForCollectionRows("futureVisits", 2);
        assertThat(page.locator("cw-collection[id='futureVisits']").innerText()).contains("Vaccination");
        page.locator("cw-collection[id='futureVisits'] [data-causeway-collection-search-clear]").click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='futureVisits']\")?.collectionState?.window?.totalCount === 13");
        waitForCollectionRows("futureVisits", 10);

        final var ownerSearch = page.locator("cw-collection[id='petOwners'] [data-causeway-collection-search]");
        assertThat(ownerSearch.isVisible()).isTrue();
        assertThat(ownerSearch.getAttribute("maxlength")).isEqualTo("256");
        ownerSearch.fill("Mary");
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='petOwners']\")?.collectionState?.window?.totalCount === 1");
        waitForCollectionRows("petOwners", 1);
        assertThat(page.locator("cw-collection[id='petOwners']").innerText()).contains("Mary Smith");
        page.locator("cw-collection[id='petOwners'] [data-causeway-collection-search-clear]").click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='petOwners']\")?.collectionState?.window?.totalCount === 10");
        waitForCollectionRows("petOwners", 5);
        assertThat(page.locator("cw-collection[id='petOwners'] .causeway-collection-range").textContent())
                .isEqualTo("Items 1–5 of 10");
        page.locator("cw-collection[id='petOwners'] [data-causeway-collection-sort='name']").click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='petOwners']\")?.collectionState?.window?.ordering === 'REQUESTED'");
        assertThat(page.locator("cw-collection[id='petOwners'] [data-causeway-collection-sort='name']").getAttribute("aria-label"))
                .contains("descending");

        page.locator("cw-collection[id='petOwners'] [data-causeway-grid-next]").click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='petOwners']\")?.collectionState?.window?.offset === 5");
        waitForCollectionRows("petOwners", 5);
        assertThat(page.locator("cw-collection[id='petOwners'] .causeway-collection-range").textContent())
                .isEqualTo("Items 6–10 of 10");
        assertThat(page.locator("cw-collection[id='petOwners'] [data-causeway-grid-previous]").isVisible()).isTrue();
        page.locator("cw-collection[id='petOwners'] [data-causeway-grid-previous]").click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='petOwners']\")?.collectionState?.window?.offset === 0");
        waitForCollectionRows("petOwners", 5);
        clickObjectLink("Eduardo Rodriguez");
        waitForRoute("petclinic.PetOwner", "s_owner-eduardo");
        waitForCollectionRows("pets", 5);
        waitForCollectionRows("visits", 8);
        assertThat(page.locator("cw-collection[id='pets'] .causeway-collection-range").textContent())
                .isEqualTo("Items 1–5 of 6");
        assertThat(page.locator("cw-collection[id='visits'] .causeway-collection-range").textContent())
                .isEqualTo("Items 1–8 of 11");
        page.locator("cw-collection[id='pets'] [data-causeway-grid-next]").click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='pets']\")?.collectionState?.window?.offset === 5");
        waitForCollectionRows("pets", 1);
        page.locator("cw-collection[id='visits'] [data-causeway-grid-next]").click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='visits']\")?.collectionState?.window?.offset === 8");
        waitForCollectionRows("visits", 3);

        openHome();
        waitForCollectionRows("petOwners", 5);
        final var marySearch = page.locator("cw-collection[id='petOwners'] [data-causeway-collection-search]");
        marySearch.fill("Mary");
        waitForCollectionRows("petOwners", 1);
        clickObjectLink("Mary Smith");
        waitForRoute("petclinic.PetOwner", "s_owner-mary");
        assertThat(page.locator(ROUTE_PAGE).getAttribute("data-page-kind")).isEqualTo("custom");
        assertThat(page.locator(ROUTE_PAGE).getAttribute("data-page-source")).isEqualTo("resource");
        assertThat(page.locator("[data-testid='petclinic-owner-page']").isVisible()).isTrue();
        waitForObjectTitle("Mary Smith (Mary)");
        assertObjectLinkIcon(page.locator("cw-object-header cw-object-link"));
        waitForNoBreadcrumbs();
        assertThat(page.locator("[data-testid='petclinic-breadcrumbs'] nav").count()).isZero();
        assertThat(page.locator(".petclinic-object-grid").evaluate("""
                element => {
                  const details = element.querySelector('.petclinic-object-details').getBoundingClientRect();
                  const collections = element.querySelector('.petclinic-object-collections').getBoundingClientRect();
                  return collections.left >= details.right && Math.abs(collections.top - details.top) < 1;
                }
                """)).isEqualTo(true);
        assertFocused(ROUTE_PAGE);
        waitForCollectionRows("pets", 2);
        waitForCollectionRows("visits", 2);
        assertObjectLinkIcon(page.locator("cw-collection[id='pets'] cw-object-link").first());
        assertThat(page.locator("cw-collection[id='pets']").getAttribute("paged")).isEqualTo("5");
        assertThat(page.locator("cw-collection[id='pets']").getAttribute("sortable")).isEmpty();
        assertThat(page.locator("cw-collection[id='pets']").getAttribute("filterable")).isEmpty();
        final var petSearch = page.locator("cw-collection[id='pets'] [data-causeway-collection-search]");
        assertThat(petSearch.isVisible()).isTrue();
        petSearch.fill("cat");
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='pets']\")?.collectionState?.window?.totalCount === 1");
        waitForCollectionRows("pets", 1);
        if (!nativeToolkit()) {
            assertCompactCollectionGrid("pets");
        }
        assertThat(page.locator("cw-collection[id='pets']").innerText()).contains("Samantha");
        page.locator("cw-collection[id='pets'] [data-causeway-collection-search-clear]").click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='pets']\")?.collectionState?.window?.totalCount === 2");
        waitForCollectionRows("pets", 2);
        page.locator("cw-collection[id='pets'] [data-causeway-collection-sort='name']").click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='pets']\")?.collectionState?.window?.ordering === 'REQUESTED'");
        assertThat(page.locator("cw-collection[id='pets'] cw-action[id='addPet']").isVisible()).isTrue();
        assertThat(page.locator("cw-collection[id='visits']").getAttribute("paged")).isEqualTo("8");
        final var visitTotal = ((Number) page.locator("cw-collection[id='visits']")
                .evaluate("element => element.collectionState.window.totalCount")).intValue();
        assertThat(visitTotal).isEqualTo(2);
        assertThat(page.locator("cw-collection[id='visits'] .causeway-collection-range").textContent())
                .isEqualTo("Items 1–2 of 2");
        assertThat(page.locator("cw-collection[id='visits'] [data-causeway-grid-next]").isDisabled()).isTrue();
        assertCollectionHeading("pets", "Companion animals", "Pets currently registered to this owner.");
        assertCollectionHeading("visits", "Visit history", "All visits recorded for this owner's pets.");
        final var visitDisabledReason = page.locator("cw-collection[id='visits'] .causeway-visually-hidden");
        assertThat(visitDisabledReason.textContent()).contains("Cannot edit a mixed-in collection.");
        assertThat(visitDisabledReason.getAttribute("class")).contains("causeway-visually-hidden");
        assertThat(page.locator("cw-collection [title*='Cannot edit']").count()).isZero();
        assertCollectionPresentation("pets", "grid");
        assertCollectionPresentation("visits", "ordering-not-deterministic");
        if (!nativeToolkit()) {
            assertCompactCollectionGrid("pets");
        }
        assertIntegratedCollectionActionHeader("pets", "addPet", "removePet", false);
        assertBelowCollectionActionTooltip("pets", "addPet");
        final var addPetFocusTarget = page.locator("cw-action[id='addPet'] button, cw-action[id='addPet'] vaadin-button").first();
        final var removePetFocusTarget = page.locator("cw-action[id='removePet'] button, cw-action[id='removePet'] vaadin-button").first();
        addPetFocusTarget.focus();
        page.keyboard().press("Tab");
        assertThat(removePetFocusTarget.evaluate("element => element.matches(':focus') || element.shadowRoot?.activeElement != null")).isEqualTo(true);
        page.keyboard().press("Tab");
        assertThat(petSearch.evaluate("element => element.matches(':focus')")).isEqualTo(true);
        assertThat(page.locator(".petclinic-page-toolbar cw-action[id='delete']").count()).isEqualTo(1);
        assertThat(page.locator("cw-property[id='name'] > cw-action[id='updateName']").count())
                .isEqualTo(1);
        assertThat(page.locator("cw-collection[id='pets'] > cw-action[id='addPet']").count())
                .isEqualTo(1);
        assertThat(page.locator("cw-collection[id='pets'] > cw-action[id='removePet']").count())
                .isEqualTo(1);
        assertThat(page.locator("cw-collection[id='visits'] > cw-action[id='bookVisit']").count())
                .isEqualTo(1);
        final var deleteAction = page.locator("cw-action[id='delete']");
        assertThat(deleteAction.locator(".causeway-action-label").first().textContent())
                .isEqualTo("Remove this owner");
        assertThat(deleteAction.locator(".causeway-action-icon.fa-trash-can").count()).isGreaterThan(0);
        assertThat(deleteAction.locator(".causeway-action-control-tooltip").getAttribute("data-tooltip"))
                .isEqualTo("Deletes this pet owner and their related pets.\n\n"
                        + "This owner has 2 visits");
        assertThat(objectAction("updateName").textContent()).contains("Change the owner's name");
        assertThat(page.locator("cw-action[id='updateName'] .causeway-action-label + .causeway-action-icon.fa-pen-to-square").count())
                .isGreaterThan(0);
        assertThat(objectAction("addPet").textContent()).contains("Register a pet");
        assertThat(page.locator("cw-action[id='addPet'] .causeway-action-icon.fa-paw + .causeway-action-label").count())
                .isGreaterThan(0);
        assertThat(objectAction("removePet").textContent()).contains("Remove Pet");
        assertThat(objectAction("bookVisit").textContent()).contains("Book Visit");
        assertDefaultOrNativeMemberPresentation("name", "delete");
        assertSingleToolkitFieldBoundary("name");
        assertSingleToolkitFieldBoundary("telephoneNumber");
        assertThat(page.locator("cw-collection[id='pets']")
                .evaluate("element => [...element.children].filter(child => child.localName === 'cw-action').map(child => child.getAttribute('id')).join(',')"))
                .isEqualTo("addPet,removePet");
        assertThat(page.locator("cw-property[id='name']")
                .evaluate("element => getComputedStyle(element).gap")).isNotEqualTo("0px");

        final var readsBeforeResponsiveSwitch = graphQLRequests.size();
        page.setViewportSize(500, 900);
        page.waitForFunction("() => [...document.querySelectorAll(\"cw-collection[id='pets'], cw-collection[id='visits']\")].every(element => element.dataset.causewayGridResponsive === 'narrow' && !element.querySelector('cw-collection-grid'))");
        waitForCollectionRows("pets", 2);
        waitForCollectionRows("visits", 2);
        assertIntegratedCollectionActionHeader("pets", "addPet", "removePet", true);
        assertThat(graphQLRequests.size()).isEqualTo(readsBeforeResponsiveSwitch);
        page.setViewportSize(1800, 900);
        if (!nativeToolkit()) {
            page.waitForFunction("() => document.querySelector(\"cw-collection[id='pets']\")?.dataset.causewayGridPresentation.startsWith('grid-') && document.querySelector(\"cw-collection[id='visits']\")?.dataset.causewayGridFallback === 'ordering-not-deterministic'");
            assertCompactCollectionGrid("pets");
        }
        assertIntegratedCollectionActionHeader("pets", "addPet", "removePet", false);
        assertThat(graphQLRequests.size()).isEqualTo(readsBeforeResponsiveSwitch);

        page.goBack();
        waitForPageKind("custom");
        assertThat(page.url()).contains("/object/petclinic.HomePage/");
        assertFocused(ROUTE_PAGE);

        page.goForward();
        waitForRoute("petclinic.PetOwner", "s_owner-mary");
        assertFocused(ROUTE_PAGE);

        page.navigate(url("/htmx/object/petclinic.Pet/s_pet-basil"),
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        waitForRoute("petclinic.Pet", "s_pet-basil");
        assertThat(page.locator("[data-testid='petclinic-pet-page']").isVisible()).isTrue();
        waitForObjectTitle("Basil · dog");
        waitForBreadcrumbs(1);
        assertObjectLinkIcon(page.locator("cw-object-header cw-object-link"));
        assertObjectLinkIcon(page.locator("cw-property[id='petOwner'] cw-object-link"));
        assertObjectLinkIcon(page.locator("[data-testid='petclinic-breadcrumbs'] cw-object-link"));
        assertThat(page.locator("[data-testid='petclinic-breadcrumbs'] cw-object-link").getAttribute("title"))
                .isEqualTo("Mary Smith (Mary)");
        assertThat(page.locator("[data-testid='petclinic-breadcrumbs'] [aria-current='page']").textContent())
                .isEqualTo("Basil · dog");
        waitForMenus();

        final var petUrl = page.url();
        page.locator(ROUTE_PAGE).evaluate("element => { element.dataset.selfLinkProbe = 'old'; }");
        page.locator("cw-object-header cw-object-link button").click();
        page.waitForFunction("() => document.querySelector('[data-testid=\"causeway-route-page\"]')?.dataset.selfLinkProbe !== 'old'");
        waitForRoute("petclinic.Pet", "s_pet-basil");
        waitForObjectTitle("Basil · dog");
        assertThat(page.url()).isEqualTo(petUrl);
        assertObjectLinkIcon(page.locator("cw-object-header cw-object-link"));

        page.navigate(url("/htmx/object/petclinic.Visit/s_visit-basil-checkup"),
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        waitForRoute("petclinic.Visit", "s_visit-basil-checkup");
        assertThat(page.locator("[data-testid='petclinic-visit-page']").isVisible()).isTrue();
        waitForBreadcrumbs(2);
        assertThat(page.locator("[data-testid='petclinic-breadcrumbs'] cw-object-link .causeway-object-link-icon").count())
                .isEqualTo(2);
        assertThat(page.locator("[data-testid='petclinic-breadcrumbs'] cw-object-link")
                .evaluateAll("elements => elements.map(element => element.getAttribute('title')).join(',')"))
                .isEqualTo("Mary Smith (Mary),Basil · dog");
        page.evaluate("() => window.scrollTo(0, document.documentElement.scrollHeight)");
        page.locator("[data-testid='petclinic-breadcrumbs'] cw-object-link[title='Basil · dog'] button")
                .evaluate("element => element.click()");
        waitForRoute("petclinic.Pet", "s_pet-basil");
        assertThat(((Number) page.evaluate("() => window.scrollY")).doubleValue()).isZero();
        page.goBack();
        waitForRoute("petclinic.Visit", "s_visit-basil-checkup");
        waitForBreadcrumbs(2);
        waitForMenus();

        page.setViewportSize(390, 844);
        final var horizontalOverflow = (Number) page.evaluate(
                "() => document.documentElement.scrollWidth - document.documentElement.clientWidth");
        final var overflowingElements = page.evaluate("() => [...document.querySelectorAll('body *')].map(element => { const rect = element.getBoundingClientRect(); return {tag: element.tagName, className: String(element.className || ''), member: element.getAttribute('id'), left: rect.left, right: rect.right, width: rect.width}; }).filter(value => value.right > document.documentElement.clientWidth + 0.5 || value.left < -0.5).slice(0, 20)");
        assertThat(horizontalOverflow)
                .as("overflowing elements: %s", overflowingElements)
                .isEqualTo(0);
        if (nativeToolkit()) {
            assertThat(page.locator(".causeway-menubar-bar-disclosure").count()).isGreaterThan(0);
        } else {
            assertThat(page.locator("cw-menubar-control vaadin-menu-bar").count()).isGreaterThan(0);
            page.waitForFunction("() => document.querySelector('cw-menubar-primary')?.dataset.causewayMenubarResponsive === 'narrow'");
        }

        final var homeReadsBeforeBrandNavigation = graphQLRequests.stream()
                .filter(payload -> payload.contains("CausewayReadApplicationEntry"))
                .count();
        page.locator(".causeway-shell-brand").click();
        page.waitForFunction("() => location.pathname.includes('/object/petclinic.HomePage/')");
        waitForPageKind("custom");
        assertThat(graphQLRequests.stream()
                .filter(payload -> payload.contains("CausewayReadApplicationEntry"))
                .count() - homeReadsBeforeBrandNavigation).isBetween(0L, 1L);
        assertThat(page.url()).contains("/object/petclinic.HomePage/");
        assertThat(page.locator("[data-testid='petclinic-custom-home']").isVisible()).isTrue();
        assertFocused(ROUTE_PAGE);
    }

    @Test
    @Order(2)
    void serviceActionsCoverValidationCancellationScalarAndCollections() {
        openHome();
        assertOrdinaryTertiaryActions();

        openMenu("Pet Owners");
        if (nativeToolkit()) {
            menuDisclosure("Pet Owners").press("Escape");
        } else {
            page.locator("cw-menubar-control[data-causeway-menubar-tier='primary']").press("Escape");
        }
        assertMenuClosedAndFocused("Pet Owners");
        assertThat(page.locator(PROMPT).count()).isZero();

        openMenu("Pet Owners");
        page.locator(ROUTE_PAGE).dispatchEvent("click");
        assertMenuClosed("Pet Owners");
        openMenu("Pet Owners");
        if (!nativeToolkit()) {
            final var manageHeading = page.locator(".causeway-menubar-section-label")
                    .filter(new Locator.FilterOptions().setHasText("Manage"));
            manageHeading.waitFor();
            assertThat(manageHeading.getAttribute("role")).isEqualTo("separator");
            assertThat(page.locator("vaadin-menu-bar-overlay[opened]").count()).isEqualTo(1);
            assertThat(page.locator("vaadin-menu-bar-item[aria-haspopup='true']").count()).isZero();
        }
        assertThat(petclinicServiceActionIds()).containsExactlyInAnyOrder(
                "create", "findByName", "findByNameLike", "listAll", "count", "listUpcoming");
        if (nativeToolkit()) {
            final var listAllPresentation = serviceActionPresentation("listAll");
            listAllPresentation.waitFor();
            assertThat(listAllPresentation.locator(".causeway-action-icon.fa-users").count()).isGreaterThan(0);
            assertThat(listAllPresentation.getAttribute("data-tooltip"))
                    .isEqualTo("Lists every registered pet owner.");
        } else {
            final var listAllDescriptor = (Map<?, ?>) page.evaluate("""
                    () => Object.values(document.querySelector('cw-menubar-primary')._projection.actions)
                      .find(action => action.actionId === 'listAll')
                    """);
            assertThat(String.valueOf(listAllDescriptor.get("iconHint"))).endsWith("users");
            assertThat(listAllDescriptor.get("iconPosition")).isEqualTo("LEFT");
            assertThat(listAllDescriptor.get("description"))
                    .isEqualTo("Lists every registered pet owner.");
        }

        final var listAllRequestStart = graphQLRequests.size();
        final var shellResultOutlet = page.locator("#causeway-result");
        constrainResultContentHeight(shellResultOutlet);
        activateServiceAction("listAll");
        waitForShellResult("Pet owners", "10 results");
        assertStandaloneCollectionResult("Pet owners", 10);
        assertScrollableResultWithDismissBelow(shellResultOutlet);
        assertThat(page.locator("cw-action-results:not([hidden]) cw-standalone-collection").first().textContent())
                .contains("Owner", "Telephone", "Email")
                .doesNotContain("Known as", "Notes", "Unavailable");
        final var listAllRequest = graphQLRequests.subList(listAllRequestStart, graphQLRequests.size()).stream()
                .filter(body -> body != null && body.contains("listAll") && body.contains("results"))
                .findFirst().orElseThrow();
        assertThat(listAllRequest)
                .contains("name", "telephoneNumber", "emailAddress")
                .doesNotContain("knownAs", "notes");
        assertMenuClosedAndFocused("Pet Owners");

        openMenu("Pet Owners");
        activateServiceAction("findByName");
        waitForPrompt("findByName");
        assertMenuClosed("Pet Owners");
        assertFocused(parameter("name"));
        submitPrompt();
        waitForPromptError("mandatory");
        assertFocused(parameter("name"));
        fillParameter("name", "Mary");
        submitPrompt();
        waitForShellResult("Pet owners", "1 result");
        assertStandaloneCollectionResult("Pet owners", 1);
        assertMenuClosedAndFocused("Pet Owners");
        page.locator("cw-standalone-collection cw-object-link[title='Mary Smith (Mary)'] button").click();
        waitForRoute("petclinic.PetOwner", "s_owner-mary");
        assertThat(page.locator("[data-testid='causeway-shell-result'] cw-standalone-collection").count()).isZero();

        openMenu("Pet Owners");
        activateServiceAction("findByNameLike");
        waitForPrompt("findByNameLike");
        fillParameter("name", "James");
        cancelPrompt();
        assertMenuClosedAndFocused("Pet Owners");
        openMenu("Pet Owners");
        activateServiceAction("findByNameLike");
        waitForPrompt("findByNameLike");
        fillParameter("name", "James");
        submitPrompt();
        waitForShellResult("Pet owners", "1 result");
        assertStandaloneCollectionResult("Pet owners", 1);
        assertMenuClosedAndFocused("Pet Owners");

        openMenu("Pet Owners");
        activateServiceAction("count");
        waitForShellResult("count", "10");
        assertMenuClosedAndFocused("Pet Owners");

        openMenu("Pet Owners");
        activateServiceAction("create");
        waitForPrompt("create");
        assertThat(page.locator(PROMPT + " .causeway-action-prompt-description").textContent())
                .isEqualTo("Registers a new pet owner.");
        cancelPrompt();
        assertMenuClosedAndFocused("Pet Owners");

        openMenu("Visits");
        final var listUpcoming = nativeToolkit()
                ? serviceAction("listUpcoming")
                : page.locator("cw-menubar-control[data-causeway-menubar-tier='primary']");
        listUpcoming.focus();
        listUpcoming.press("Tab");
        page.waitForFunction("() => !document.activeElement?.closest('cw-menubar-primary')");
        assertMenuClosed("Visits");
        assertThat((Boolean) page.evaluate(
                "() => !document.activeElement?.closest('cw-menubar-primary')")).isTrue();

        openMenu("Visits");
        activateServiceAction("listUpcoming");
        waitForShellResult("listUpcoming", "13 results");
        assertMenuClosedAndFocused("Visits");
    }

    @Test
    @Order(3)
    void propertyEditingAndPromptFocusRemainDeterministic() {
        openObject("petclinic.PetOwner", "s_owner-mary");

        final var disabledNameLabel = page.locator("cw-property[id='name'] .causeway-property-label.causeway-property-disabled-tooltip");
        disabledNameLabel.waitFor();
        assertThat(disabledNameLabel.getAttribute("data-tooltip")).isNotBlank();
        assertThat(disabledNameLabel.getAttribute("tabindex")).isEqualTo("0");
        assertThat(page.locator("cw-property[id='name'] .causeway-property-disabled-indicator").count()).isZero();
        assertThat((String) page.locator("cw-property[id='name'] .causeway-property-value-string")
                .evaluate("element => getComputedStyle(element).textAlign"))
                .isIn("start", "left");
        assertThat(disabledNameLabel.textContent()).isEqualTo("Full name");

        final var knownAs = page.locator("cw-property[id='knownAs']");
        assertThat(knownAs.locator(".causeway-property-description").textContent())
                .isEqualTo("The familiar or preferred name used by this owner.");
        final var telephone = page.locator("cw-property[id='telephoneNumber']");
        assertThat(telephone.locator(".causeway-property-description").textContent())
                .isEqualTo("Primary telephone number for appointment contact.");
        final var email = page.locator("cw-property[id='emailAddress'] .causeway-property");
        assertThat(email.getAttribute("data-label-position")).isEqualTo("TOP");
        assertThat(page.locator("cw-property[id='emailAddress'] .causeway-property-description").textContent())
                .isEqualTo("Email address used for appointment reminders.");
        final var lastVisit = page.locator("cw-property[id='lastVisit']");
        assertThat(lastVisit.locator(".causeway-property")
                .getAttribute("data-label-position")).isEqualTo("TOP");
        if (!nativeToolkit()) {
            page.evaluate("() => { document.documentElement.lang = 'en-GB'; }");
        }
        lastVisit.locator("[data-causeway-action='edit']").click();
        final var lastVisitEditorSelector = "cw-property[id='lastVisit'] [data-causeway-editor='lastVisit']";
        final var lastVisitEditor = resolveEditor(lastVisitEditorSelector);
        final var lastVisitIso = (String) lastVisitEditor.evaluate("element => element.value");
        final var todayIso = (String) page.evaluate("""
                () => {
                  const now = new Date();
                  const pad = value => String(value).padStart(2, '0');
                  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
                }
                """);
        assertThat(lastVisit.getAttribute("data-causeway-temporal-range-status")).isEqualTo("valid");
        assertThat(lastVisitEditor.evaluate("element => element.min")).isEqualTo("2000-01-01");
        assertThat(lastVisitEditor.evaluate("element => element.max")).isEqualTo(todayIso);
        if (!nativeToolkit()) {
            final var expectedBritishDate = LocalDate.parse(lastVisitIso)
                    .format(DateTimeFormatter.ofPattern("dd/MM/uuuu"));
            assertThat(lastVisitEditor.evaluate("element => element.inputElement?.value"))
                    .isEqualTo(expectedBritishDate);
            lastVisitEditor.focus();
            page.keyboard().press("Tab");
            assertThat(lastVisitEditor.evaluate(
                    "element => element.shadowRoot?.activeElement?.hasAttribute('data-causeway-calendar-trigger')"))
                    .as("Tab should move focus from the date input to its calendar trigger")
                    .isEqualTo(true);
            final var calendarTrigger = lastVisitEditor.locator("[data-causeway-calendar-trigger]");
            assertThat(calendarTrigger.getAttribute("role")).isEqualTo("button");
            assertThat(calendarTrigger.getAttribute("aria-label")).isEqualTo("Open Last Visit calendar");
            page.keyboard().press("Enter");
            page.waitForFunction("selector => document.querySelector(selector)?.opened === true", lastVisitEditorSelector);
            assertThat(lastVisit.locator("[data-causeway-action='save']").count()).isEqualTo(1);
            lastVisitEditor.evaluate("element => element.close()");
        }

        page.waitForTimeout(750);
        final var requestsBeforeLocalRejection = graphQLRequests.size();
        final var futureDate = LocalDate.parse(todayIso).plusDays(1).toString();
        fillEditor(lastVisitEditor, futureDate);
        page.waitForFunction("expected => document.querySelector(\"cw-property[id='lastVisit']\")?.interactionState?.pendingValue === expected", futureDate);
        assertThat(lastVisit.locator("[data-causeway-action='save']").getAttribute("aria-disabled")).isEqualTo("true");
        assertThat(lastVisit.evaluate("element => element.interactionState?.status")).isEqualTo("failed");
        assertThat(lastVisit.evaluate("element => element.interactionState?.error"))
                .isEqualTo("Enter a value on or before " + todayIso + ".");
        assertThat(lastVisit.evaluate("element => element.interactionState?.temporalRange?.semanticType"))
                .isEqualTo("LocalDate");
        assertThat(lastVisit.innerText()).contains("Enter a value on or before " + todayIso);
        assertThat(lastVisit.evaluate("element => element.interactionState.pendingValue")).isEqualTo(futureDate);
        assertThat(graphQLRequests).hasSize(requestsBeforeLocalRejection);

        fillEditor(lastVisitEditor, lastVisitIso);
        lastVisit.locator("[data-causeway-action='save']").click();
        page.waitForFunction("() => document.querySelector(\"cw-property[id='lastVisit']\")?.interactionState == null");
        assertThat(graphQLRequests.size()).isGreaterThan(requestsBeforeLocalRejection);
        assertThat(lastVisit.getAttribute("data-causeway-temporal-range-status")).isNull();

        lastVisit.locator("[data-causeway-action='edit']").click();
        final var cancellationEditor = resolveEditor(lastVisitEditorSelector);
        page.waitForTimeout(750);
        final var requestsBeforeCancellation = graphQLRequests.size();
        fillEditor(cancellationEditor, futureDate);
        page.waitForFunction("expected => document.querySelector(\"cw-property[id='lastVisit']\")?.interactionState?.pendingValue === expected", futureDate);
        lastVisit.locator("[data-causeway-action='cancel']").click();
        page.waitForTimeout(750);
        assertThat(graphQLRequests).hasSize(requestsBeforeCancellation);
        assertThat(lastVisit.getAttribute("data-causeway-temporal-range-status")).isNull();
        if (!nativeToolkit()) {
            page.evaluate("() => { document.documentElement.lang = 'en'; }");
        }
        final var knownAsEdit = knownAs.locator("[data-causeway-action='edit']");
        knownAsEdit.click();
        final var knownAsEditorSelector = "cw-property[id='knownAs'] [data-causeway-editor='knownAs']";
        final var knownAsEditor = resolveEditor(knownAsEditorSelector);
        assertFocused(knownAsEditorSelector);
        final var knownAsSaveSelector = "cw-property[id='knownAs'] [data-causeway-action='save']";
        final var knownAsCancelSelector = "cw-property[id='knownAs'] [data-causeway-action='cancel']";
        if (!nativeToolkit()) {
            final var knownAsClearSelector = "cw-property[id='knownAs'] .causeway-field-clear";
            knownAsEditor.press("Tab");
            assertFocused(knownAsClearSelector);
            page.locator(knownAsClearSelector).press("Enter");
            assertFocused(knownAsEditorSelector);
            fillEditor(knownAsEditor, "Keyboard focus");
            knownAsEditor.press("Tab");
            assertFocused(knownAsClearSelector);
            page.waitForTimeout(750);
            assertFocused(knownAsClearSelector);
            page.locator(knownAsClearSelector).press("Tab");
        } else {
            fillEditor(knownAsEditor, "Keyboard focus");
            knownAsEditor.press("Tab");
        }
        assertFocused(knownAsSaveSelector);
        page.waitForTimeout(750);
        assertFocused(knownAsSaveSelector);
        page.locator(knownAsSaveSelector).press("Tab");
        assertFocused(knownAsCancelSelector);
        page.locator(knownAsCancelSelector).click();

        final var notes = page.locator("cw-property[id='notes']");
        final var notesEdit = notes.locator("[data-causeway-action='edit']");
        revealContainingTab(notes, notesEdit);
        assertThat(notes.locator(".causeway-property-description").textContent())
                .isEqualTo("Additional notes about this pet owner.");
        assertWideMultilinePropertyLayout(notes);
        notesEdit.click();
        final var notesEditor = resolveEditor("cw-property[id='notes'] [data-causeway-editor='notes']");
        assertThat(notesEditor.evaluate("element => element.localName === 'vaadin-text-area' ? element.maxRows : Number(element.rows)"))
                .isEqualTo(5);
        if (!nativeToolkit()) {
            assertSingleVaadinMultilineFocus(notesEditor);
        }
        fillEditor(notesEditor, "First line\nSecond line");
        notes.locator("[data-causeway-action='cancel']").click();

        page.setViewportSize(390, 844);
        assertNarrowMultilinePropertyLayout(notes);
        page.setViewportSize(1440, 900);

        editProperty("telephoneNumber", "020 7000 1234");
        waitForPropertyValue("telephoneNumber", "020 7000 1234");
        assertFocused("cw-property[id='telephoneNumber'] [data-causeway-action='edit']");
        editProperty("telephoneNumber", "020 7946 0312");
        waitForPropertyValue("telephoneNumber", "020 7946 0312");

        final var updateName = objectAction("updateName");
        updateName.click();
        waitForPrompt("updateName", "Change the owner's name");
        assertThat(page.locator(PROMPT).evaluate("element => element.localName")).isEqualTo("section");
        assertThat(page.locator(PROMPT).getAttribute("data-prompt-style")).isEqualTo("INLINE");
        assertThat(page.locator(PROMPT).evaluate("element => element.closest('cw-property')?.id")).isEqualTo("name");
        assertThat(page.locator("cw-property[id='name'] > .causeway-member-primary").isHidden()).isTrue();
        assertThat(page.locator(PROMPT + " .causeway-action-prompt-description").textContent())
                .isEqualTo("Updates the owner's full name.");
        final var updateNameLabel = page.locator(PROMPT + " [data-parameter='name'] .causeway-action-parameter-label");
        assertThat(updateNameLabel.textContent()).isEqualTo("Owner's full name");
        assertThat(updateNameLabel.getAttribute("data-tooltip"))
                .isEqualTo("The complete name used to identify this pet owner.");
        assertThat(page.locator(PROMPT + " [data-parameter='name'] .causeway-action-parameter-description")
                .getAttribute("class")).contains("causeway-visually-hidden");
        assertFocused(parameter("name"));
        fillParameter("name", "Invalid % name");
        submitPrompt();
        waitForPromptError("cannot contain");
        assertFocused(parameter("name"));
        cancelPrompt();
        assertThat(page.locator("cw-property[id='name'] > .causeway-member-primary").isVisible()).isTrue();
        assertFocused("cw-action[id='updateName'] [data-causeway-action-control]");

        updateName.click();
        waitForPrompt("updateName", "Change the owner's name");
        resolveEditor(parameter("name")).press("Escape");
        page.locator(PROMPT).waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED));
        assertFocused("cw-action[id='updateName'] [data-causeway-action-control]");

        final var bookVisitMutations = graphQLMutationCount("bookVisit");
        objectAction("bookVisit").click();
        waitForPrompt("bookVisit");
        final var visitDate = resolveEditor(parameter("visitDate"));
        final var visitTime = resolveEditor(parameter("visitTime"));
        final var bookingTomorrowIso = (String) page.evaluate("""
                () => {
                  const date = new Date();
                  date.setDate(date.getDate() + 1);
                  const pad = value => String(value).padStart(2, '0');
                  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
                }
                """);
        final var bookingTodayIso = LocalDate.parse(bookingTomorrowIso).minusDays(1).toString();
        assertThat(visitDate.evaluate("element => element.min")).isEqualTo(bookingTomorrowIso);
        assertThat(visitTime.evaluate("element => element.min")).isEqualTo("08:00");
        assertThat(visitTime.evaluate("element => element.max")).isEqualTo("17:00");
        final var rejectedDateRequests = graphQLRequestCount("CausewayPrepareAction", bookingTodayIso);
        fillEditor(visitDate, bookingTodayIso);
        commitEditor(resolveEditor(parameter("visitDate")));
        page.waitForFunction("() => document.querySelector(\"[data-testid='action-prompt'] [data-parameter='visitDate'] .causeway-action-parameter-reason\")");
        assertThat(graphQLRequestCount("CausewayPrepareAction", bookingTodayIso)).isEqualTo(rejectedDateRequests);
        assertThat(graphQLMutationCount("bookVisit") - bookVisitMutations).isZero();
        cancelPrompt();
        assertFocused("cw-action[id='bookVisit'] [data-causeway-action-control]");
    }

    @Test
    @Order(4)
    void disposableOwnerExercisesCreateAndEveryObjectAction() {
        openHome();
        openMenu("Pet Owners");
        activateServiceAction("create");
        waitForPrompt("create");
        assertMenuClosed("Pet Owners");
        assertFocused(parameter("name"));
        fillParameter("name", "Playwright Owner");
        fillParameter("knownAs", "PW");
        fillParameter("telephoneNumber", "020 7000 0000");
        fillParameter("emailAddress", "playwright@example.com");
        submitPromptExpectingNavigation();
        waitForLogicalType("petclinic.PetOwner");
        final var ownerPath = page.url();
        waitForObjectTitle("Playwright Owner");
        waitForPropertyValue("knownAs", "PW");
        assertFocused(ROUTE_PAGE);

        final var updateNameMutations = graphQLMutationCount("updateName");
        objectAction("updateName").click();
        waitForPrompt("updateName", "Change the owner's name");
        fillParameter("name", "Playwright Owner Updated");
        submitPromptExpectingNavigation();
        waitForObjectTitle("Playwright Owner Updated");
        assertThat(graphQLMutationCount("updateName") - updateNameMutations).isEqualTo(1);
        assertThat(page.url()).isEqualTo(ownerPath);
        assertFocused(ROUTE_PAGE);

        final var addPetMutations = graphQLMutationCount("addPet");
        objectAction("addPet").click();
        waitForPrompt("addPet", "Register a pet");
        assertThat(page.locator(PROMPT).evaluate("element => element.localName")).isEqualTo("dialog");
        assertThat(page.locator(PROMPT).getAttribute("data-prompt-style")).isEqualTo("DIALOG_SIDEBAR");
        page.setViewportSize(390, 844);
        assertThat((Number) page.evaluate("() => document.documentElement.scrollWidth - document.documentElement.clientWidth"))
                .isEqualTo(0);
        assertThat(page.locator(PROMPT).evaluate("element => element.getBoundingClientRect().right <= innerWidth + 0.5"))
                .isEqualTo(true);
        page.setViewportSize(1440, 900);
        assertThat(page.locator(PROMPT + " .causeway-action-prompt-description").textContent())
                .isEqualTo("Adds a pet to this owner's household.");
        assertThat(page.locator(PROMPT + " [data-parameter='name'] .causeway-action-parameter-label").textContent())
                .isEqualTo("Pet name");
        assertThat(page.locator(PROMPT + " [data-parameter='name'] .causeway-action-parameter-description").textContent())
                .isEqualTo("The name used for this companion animal.");
        assertThat(page.locator(PROMPT + " [data-parameter='species'] .causeway-action-parameter-label").textContent())
                .isEqualTo("Species");
        assertThat(page.locator(PROMPT + " .causeway-action-parameter-reason").count()).isZero();
        final var preparationsBeforeName = graphQLOperationCount("CausewayPrepareAction");
        tabOutOfParameter("name");
        waitForPromptError("mandatory");
        page.waitForTimeout(100);
        assertThat(graphQLOperationCount("CausewayPrepareAction")).isGreaterThanOrEqualTo(preparationsBeforeName + 1);

        final var nameEditor = resolveEditor(parameter("name"));
        nameEditor.focus();
        page.waitForTimeout(100);
        final var preparationsBeforeTyping = graphQLOperationCount("CausewayPrepareAction");
        fillEditor(nameEditor, "Turing");
        page.waitForTimeout(350);
        assertThat(graphQLOperationCount("CausewayPrepareAction")).isEqualTo(preparationsBeforeTyping);
        assertThat(page.locator(PROMPT + " .causeway-action-parameter-reason").count()).isZero();

        tabOutOfParameter("name");
        page.waitForTimeout(200);
        assertFocused(parameter("species"));
        assertThat(page.locator(PROMPT + " .causeway-action-parameter-reason").count()).isZero();
        selectParameter("species", "DOG");
        submitPromptExpectingNavigation();
        waitForRouteUrl(ownerPath);
        waitForCollectionRows("pets", 1);
        assertThat(graphQLMutationCount("addPet") - addPetMutations).isEqualTo(1);

        clickObjectLinkInCollection("pets", "Turing · dog");
        waitForLogicalType("petclinic.Pet");
        editProperty("notes", "Created and edited through Playwright");
        waitForPropertyValue("notes", "Created and edited through Playwright");
        page.goBack();
        waitForRouteUrl(ownerPath);

        final var bookVisitMutations = graphQLMutationCount("bookVisit");
        objectAction("bookVisit").click();
        waitForPrompt("bookVisit");
        assertThat(page.locator(PROMPT).getAttribute("data-prompt-style")).isEqualTo("DIALOG_MODAL");
        assertMovableModalPrompt();
        final var petReference = page.locator(parameter("pet"));
        try {
            petReference.waitFor();
        } catch (com.microsoft.playwright.TimeoutError cause) {
            throw new AssertionError(String.valueOf(
                    page.locator(PROMPT).evaluate("element => element.outerHTML")), cause);
        }
        if (Boolean.TRUE.equals(petReference.evaluate("element => element.localName === 'select'"))) {
            petReference.evaluate("element => { element.selectedIndex = 0; "
                    + "element.dispatchEvent(new Event('change', {bubbles: true, composed: true})); }");
            assertThat(petReference.inputValue()).isNotBlank();
        } else {
            page.waitForFunction("selector => document.querySelector(selector)?.dataset.widgetState === 'ready'", parameter("pet"));
            assertThat(petReference.evaluate("element => element.value?.id")).isNotNull();
        }
        final var visitDate = resolveEditor(parameter("visitDate"));
        final var visitTime = resolveEditor(parameter("visitTime"));
        final var reason = resolveEditor(parameter("reason"));
        final var tomorrowIso = (String) page.evaluate("""
                () => {
                  const date = new Date();
                  date.setDate(date.getDate() + 1);
                  const pad = value => String(value).padStart(2, '0');
                  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
                }
                """);
        final var todayIso = LocalDate.parse(tomorrowIso).minusDays(1).toString();
        assertThat(page.locator(PROMPT + " [data-parameter='reason'] .causeway-action-parameter-label").textContent())
                .isEqualTo("Reason for visit");
        assertThat(page.locator(PROMPT + " [data-parameter='reason'] .causeway-action-parameter-description").textContent())
                .isEqualTo("Describe the purpose of the appointment.");
        assertThat(reason.evaluate("element => element.localName === 'vaadin-text-area' ? element.maxRows : Number(element.rows)"))
                .isEqualTo(3);
        assertThat(visitDate.count()).isEqualTo(1);
        assertThat(visitTime.count())
                .as(page.locator(PROMPT).evaluate("element => element.outerHTML").toString())
                .isEqualTo(1);
        assertThat(visitDate.evaluate("element => element.min")).isEqualTo(tomorrowIso);
        assertThat(visitTime.evaluate("element => element.min")).isEqualTo("08:00");
        assertThat(visitTime.evaluate("element => element.max")).isEqualTo("17:00");
        assertThat(page.locator(PROMPT + " [data-parameter='visitDate']")
                .getAttribute("data-causeway-temporal-range-status")).isEqualTo("valid");
        assertThat(page.locator(PROMPT + " [data-parameter='visitTime']")
                .getAttribute("data-causeway-temporal-range-status")).isEqualTo("valid");
        assertThat(String.valueOf(visitDate.evaluate("element => element.value"))).isNotBlank();
        assertThat(String.valueOf(visitTime.evaluate("element => element.value"))).isNotBlank();
        assertThat(reason.evaluate("element => element.value")).isEqualTo("Routine check-up");

        visitDate.focus();
        page.waitForTimeout(750);
        final var rejectedDateRequests = graphQLRequestCount("CausewayPrepareAction", todayIso);
        fillEditor(visitDate, todayIso);
        commitEditor(resolveEditor(parameter("visitDate")));
        page.waitForFunction("() => document.querySelector(\"[data-testid='action-prompt'] [data-parameter='visitDate'] .causeway-action-parameter-reason\")");
        assertThat(page.locator(PROMPT + " [data-parameter='visitDate'] .causeway-action-parameter-reason").textContent())
                .contains("Enter a value on or after " + tomorrowIso);
        assertThat(graphQLRequestCount("CausewayPrepareAction", todayIso)).isEqualTo(rejectedDateRequests);
        assertThat(graphQLMutationCount("bookVisit") - bookVisitMutations).isZero();
        fillEditor(resolveEditor(parameter("visitDate")), tomorrowIso);
        commitEditor(resolveEditor(parameter("visitDate")));
        page.waitForFunction("() => !document.querySelector(\"[data-testid='action-prompt'] [data-parameter='visitDate'] .causeway-action-parameter-reason\")");

        resolveEditor(parameter("visitTime")).focus();
        page.waitForTimeout(750);
        final var rejectedTimeRequests = graphQLRequestCount("CausewayPrepareAction", "17:15");
        fillEditor(resolveEditor(parameter("visitTime")), "17:15");
        commitEditor(resolveEditor(parameter("visitTime")));
        page.waitForFunction("() => document.querySelector(\"[data-testid='action-prompt'] [data-parameter='visitTime'] .causeway-action-parameter-reason\")");
        assertThat(page.locator(PROMPT + " [data-parameter='visitTime'] .causeway-action-parameter-reason").textContent())
                .contains("Enter a value on or before 17:00");
        assertThat(graphQLRequestCount("CausewayPrepareAction", "17:15")).isEqualTo(rejectedTimeRequests);
        fillEditor(resolveEditor(parameter("visitTime")), "09:00");
        commitEditor(resolveEditor(parameter("visitTime")));
        page.waitForFunction("() => !document.querySelector(\"[data-testid='action-prompt'] [data-parameter='visitTime'] .causeway-action-parameter-reason\")");

        if (!nativeToolkit()) {
            assertSingleVaadinMultilineFocus(resolveEditor(parameter("reason")));
            final var timePicker = resolveEditor(parameter("visitTime"));
            assertThat(timePicker.evaluate("element => element.step")).isEqualTo(900);
            assertThat(String.valueOf(timePicker.evaluate("element => element.inputElement?.value")))
                    .doesNotMatch(".*:[0-9]{2}:[0-9]{2}.*")
                    .doesNotContain(".");
            final var timeTrigger = timePicker.locator("[data-causeway-time-trigger]");
            assertThat(timeTrigger.getAttribute("role")).isEqualTo("button");
            assertThat(timeTrigger.getAttribute("aria-label")).isEqualTo("Open Visit Time time picker");
            timePicker.evaluate("element => element.inputElement.focus()");
            page.waitForTimeout(750);
            final var mutationsBeforePickerUse = graphQLMutationCount("bookVisit");
            final var visibleTimeOverlay = """
                    selector => {
                      const picker = document.querySelector(selector);
                      const overlay = picker?.shadowRoot?.querySelector('vaadin-time-picker-overlay');
                      const bounds = overlay?.getBoundingClientRect();
                      return picker?.opened === true
                        && overlay?.matches(':popover-open')
                        && bounds?.width > 0
                        && bounds?.height > 0;
                    }
                    """;
            assertThat(timePicker.evaluate("element => element.inputElement.matches(':focus')")).isEqualTo(true);
            page.keyboard().press("Tab");
            assertThat(timeTrigger.evaluate("element => element.matches(':focus')"))
                    .as("Tab should move focus from the time input to its clock trigger")
                    .isEqualTo(true);
            timeTrigger.press("Enter");
            page.waitForFunction(visibleTimeOverlay, parameter("visitTime"));
            timePicker.evaluate("element => element.close()");
            page.waitForTimeout(50);
            timeTrigger.focus();
            timeTrigger.press("Space");
            page.waitForFunction(visibleTimeOverlay, parameter("visitTime"));
            timePicker.evaluate("element => element.close()");
            page.waitForTimeout(50);
            timeTrigger.click();
            page.waitForFunction(visibleTimeOverlay, parameter("visitTime"));
            assertThat(graphQLMutationCount("bookVisit") - mutationsBeforePickerUse).isZero();
            timePicker.evaluate("element => element.close()");
            page.waitForTimeout(50);
            page.locator(PROMPT + " [data-causeway-action='submit']").focus();
            page.waitForTimeout(750);
        }
        submitPromptExpectingNavigation();
        waitForRouteUrl(ownerPath);
        waitForCollectionRows("visits", 1);
        assertThat(graphQLMutationCount("bookVisit") - bookVisitMutations).isEqualTo(1);

        clickObjectLinkInCollection("visits", "Turing ·");
        waitForLogicalType("petclinic.Visit");
        editProperty("reason", "Playwright follow-up");
        waitForPropertyValue("reason", "Playwright follow-up");
        page.goBack();
        waitForRouteUrl(ownerPath);

        final var removePetMutations = graphQLMutationCount("removePet");
        objectAction("removePet").click();
        waitForPrompt("removePet");
        assertThat(page.locator(PROMPT).getAttribute("data-prompt-style")).isEqualTo("DIALOG_MODAL");
        waitForReferenceParameterReady("pet");
        assertFocused(parameter("pet"));
        resolveEditor(parameter("pet")).press("Escape");
        page.locator(PROMPT).waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED));
        assertThat(graphQLMutationCount("removePet") - removePetMutations).isZero();
        assertFocused("cw-action[id='removePet'] [data-causeway-action-control]");

        objectAction("removePet").click();
        waitForPrompt("removePet");
        waitForReferenceParameterReady("pet");
        selectFirstAvailableChoice("pet");
        submitPromptExpectingNavigation();
        waitForRouteUrl(ownerPath);
        waitForCollectionRows("pets", 0);
        waitForCollectionRows("visits", 0);
        assertThat(graphQLMutationCount("removePet") - removePetMutations).isEqualTo(1);

        final var deleteMutations = graphQLMutationCount("delete");
        objectAction("delete").click();
        final var confirmation = page.locator("[data-testid='action-confirmation']");
        confirmation.waitFor();
        assertThat(confirmation.getAttribute("role")).isEqualTo("alertdialog");
        assertThat(confirmation.textContent()).contains("Confirm Remove this owner", "This action cannot be undone");
        assertFocused("[data-testid='action-confirmation-confirm']");
        page.locator("[data-testid='action-confirmation-cancel']").click();
        confirmation.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED));
        assertThat(ownerRepository.findById("owner-11")).isNotNull();
        assertThat(graphQLMutationCount("delete") - deleteMutations).isZero();
        assertFocused("cw-action[id='delete'] [data-causeway-action-control]");

        objectAction("delete").click();
        confirmation.waitFor();
        page.locator("[data-testid='action-confirmation-confirm']").click();
        page.waitForFunction("() => location.pathname.includes('/object/petclinic.HomePage/')");
        waitForLogicalType("petclinic.HomePage");
        assertThat(page.locator("[data-testid='petclinic-custom-home']").isVisible()).isTrue();
        assertThat(ownerRepository.findById("owner-11")).isNull();
        assertThat(graphQLMutationCount("delete") - deleteMutations).isEqualTo(1);
    }

    @Test
    @Order(5)
    void destructiveOwnerDeletionRequiresConfirmation() {
        page.navigate(url("/htmx/object/petclinic.PetOwner/s_owner-peter"),
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        waitForPageKind("custom");
        waitForLogicalType("petclinic.PetOwner");
        waitForObjectTitle("Peter McTavish");

        final var deleteMutations = graphQLMutationCount("delete");
        final var deleteAction = objectAction("delete");
        assertThat(deleteAction.isEnabled()).isTrue();
        deleteAction.click();
        final var confirmation = page.locator("[data-testid='action-confirmation']");
        confirmation.waitFor();
        assertThat(confirmation.getAttribute("role")).isEqualTo("alertdialog");
        assertThat(confirmation.textContent()).contains("Confirm Remove this owner", "This action cannot be undone");
        assertFocused("[data-testid='action-confirmation-confirm']");

        page.locator("[data-testid='action-confirmation-cancel']").click();
        confirmation.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED));
        assertThat(ownerRepository.findById("owner-peter")).isNotNull();
        assertThat(graphQLMutationCount("delete") - deleteMutations).isZero();
        assertFocused("cw-action[id='delete'] [data-causeway-action-control]");

        objectAction("delete").click();
        confirmation.waitFor();
        page.locator("[data-testid='action-confirmation-confirm']").click();
        page.waitForFunction("() => location.pathname.includes('/object/petclinic.HomePage/')");
        waitForLogicalType("petclinic.HomePage");
        assertThat(page.locator("[data-testid='petclinic-custom-home']").isVisible()).isTrue();
        assertThat(ownerRepository.findById("owner-peter")).isNull();
        assertThat(graphQLMutationCount("delete") - deleteMutations).isEqualTo(1);
    }

    @Test
    @Order(6)
    void actionPromptStylesRenderRestoreAndInvoke() {
        openObject("petclinic.PetOwner", "s_owner-mary");

        objectAction("updateName").click();
        waitForPrompt("updateName", "Change the owner's name");
        assertThat(page.locator(PROMPT).evaluate("element => element.localName")).isEqualTo("section");
        assertThat(page.locator(PROMPT).getAttribute("data-prompt-style")).isEqualTo("INLINE");
        assertThat(page.locator(PROMPT).evaluate("element => element.closest('cw-property')?.id")).isEqualTo("name");
        cancelPrompt();
        assertThat(page.locator("cw-property[id='name'] > .causeway-member-primary").isVisible()).isTrue();
        assertFocused("cw-action[id='updateName'] [data-causeway-action-control]");

        objectAction("addPet").click();
        waitForPrompt("addPet", "Register a pet");
        assertThat(page.locator(PROMPT).getAttribute("data-prompt-style")).isEqualTo("DIALOG_SIDEBAR");
        page.setViewportSize(390, 844);
        assertThat((Number) page.evaluate("() => document.documentElement.scrollWidth - document.documentElement.clientWidth"))
                .isEqualTo(0);
        cancelPrompt();
        assertFocused("cw-action[id='addPet'] [data-causeway-action-control]");
        page.setViewportSize(1440, 900);

        objectAction("bookVisit").click();
        waitForPrompt("bookVisit");
        assertThat(page.locator(PROMPT).getAttribute("data-prompt-style")).isEqualTo("DIALOG_MODAL");
        assertMovableModalPrompt();
        cancelPrompt();
        assertFocused("cw-action[id='bookVisit'] [data-causeway-action-control]");

        final var mutations = graphQLMutationCount("updateName");
        objectAction("updateName").click();
        waitForPrompt("updateName", "Change the owner's name");
        fillParameter("name", "Mary Smith");
        submitPromptExpectingNavigation();
        waitForObjectTitle("Mary Smith (Mary)");
        assertThat(graphQLMutationCount("updateName") - mutations).isEqualTo(1);
    }

    @Test
    @Order(7)
    @SuppressWarnings("unchecked")
    void declarativeCollectionActionResultsUseDefaultAndInlineProjectionWithoutHydration() {
        openObject("petclinic.PetOwner", "s_owner-mary");
        assertThat(page.locator("cw-action[id='relatedOwners'] > cw-standalone-collection").count()).isEqualTo(1);
        assertThat(page.locator("cw-action[id='relatedOwners'] > cw-standalone-collection").isHidden()).isTrue();
        final var emptyOutlet = page.locator("cw-action-results[data-testid='petclinic-action-results']");
        assertThat(emptyOutlet.isHidden()).isTrue();
        assertThat((List<Number>) emptyOutlet.evaluate("element => [element.offsetWidth, element.offsetHeight]"))
                .containsExactly(0, 0);
        page.evaluate("""
                () => {
                  globalThis.__causewayResultPlacements = [];
                  new MutationObserver(() => {
                    document.querySelectorAll('cw-action-results > cw-standalone-collection').forEach(result => {
                      const placement = `${result.named}:${result.parentNode.dataset.testid}`;
                      if (!globalThis.__causewayResultPlacements.includes(placement)) {
                        globalThis.__causewayResultPlacements.push(placement);
                      }
                    });
                  }).observe(document.body, {childList: true, subtree: true});
                }
                """);

        final var defaultRequestStart = graphQLRequests.size();
        constrainResultContentHeight(emptyOutlet);
        objectAction("allOwners").click();
        final var liveResult = "cw-action-results:not([hidden]) > cw-standalone-collection";
        page.waitForFunction("selector => document.querySelector(selector)?.resultState?.status === 'ready'", liveResult);
        assertThat(page.locator(liveResult).first().getAttribute("named")).isEqualTo("Pet owners");
        assertThat(page.locator(liveResult + " cw-object-link").count()).isGreaterThan(0);
        assertObjectLinkIcon(page.locator(liveResult + " cw-object-link").first());
        page.waitForFunction("() => { const outlet = document.querySelector(\"cw-action-results[data-testid='petclinic-action-results']\"); const header = document.querySelector('.causeway-shell-header'); const rect = outlet?.getBoundingClientRect(); const headerBottom = header?.getBoundingClientRect().bottom ?? 0; return rect && rect.top >= headerBottom + 8 && rect.top < Math.min(innerHeight / 3, 160); }");
        assertScrollableResultWithDismissBelow(emptyOutlet);
        assertThat((List<String>) page.evaluate("() => globalThis.__causewayResultPlacements"))
                .contains("Pet owners:petclinic-action-results");
        final var defaultRequest = graphQLRequests.subList(defaultRequestStart, graphQLRequests.size()).stream()
                .filter(body -> body != null && body.contains("allOwners") && body.contains("results"))
                .findFirst().orElseThrow();
        assertThat(defaultRequest)
                .contains("name")
                .contains("telephoneNumber")
                .contains("emailAddress")
                .doesNotContain("knownAs");

        final var emptyRequestStart = graphQLRequests.size();
        objectAction("noOwners").click();
        page.waitForFunction("selector => { const result = document.querySelector(selector); return result?.named === 'Pet owners' && result?.resultState?.status === 'empty'; }", liveResult);
        assertThat(page.locator(liveResult).first().textContent()).contains("0 results", "No items");
        assertThat(page.locator("#causeway-route-announcement").textContent()).contains("Pet owners: 0 results");
        assertThat(page.locator(liveResult + " cw-object-link").count()).isZero();
        final var emptyRequest = graphQLRequests.subList(emptyRequestStart, graphQLRequests.size()).stream()
                .filter(body -> body != null && body.contains("noOwners") && body.contains("results"))
                .findFirst().orElseThrow();
        assertThat(emptyRequest).contains("name", "telephoneNumber", "emailAddress");

        final var inlineRequestStart = graphQLRequests.size();
        objectAction("relatedOwners").click();
        page.waitForFunction("selector => { const result = document.querySelector(selector); return result?.named === 'Related owners' && result?.resultState?.status === 'ready'; }", liveResult);
        assertThat(page.locator(liveResult).first().getAttribute("named")).isEqualTo("Related owners");
        final var inlineRequest = graphQLRequests.subList(inlineRequestStart, graphQLRequests.size()).stream()
                .filter(body -> body != null && body.contains("relatedOwners") && body.contains("results"))
                .findFirst().orElseThrow();
        assertThat(inlineRequest)
                .contains("name")
                .contains("knownAs")
                .contains("notes")
                .doesNotContain("telephoneNumber", "emailAddress");
        assertThat(page.locator("cw-action[id='relatedOwners'] > cw-standalone-collection").count()).isEqualTo(1);
        assertThat(page.locator("cw-action[id='relatedOwners'] > cw-standalone-collection").isHidden()).isTrue();
        assertThat((List<String>) page.evaluate("() => globalThis.__causewayResultPlacements"))
                .contains("Related owners:petclinic-action-results");

        page.evaluate("""
                () => {
                  globalThis.__causewayResultPlacements = [];
                  const duplicate = document.createElement('cw-action-results');
                  duplicate.dataset.testid = 'duplicate-action-results';
                  document.querySelector('[data-testid="causeway-route-page"]').append(duplicate);
                }
                """);
        objectAction("allOwners").click();
        page.waitForFunction("() => globalThis.__causewayResultPlacements.includes('Pet owners:causeway-shell-result')");
        assertThat((List<String>) page.evaluate("() => globalThis.__causewayResultPlacements"))
                .contains("Pet owners:causeway-shell-result")
                .doesNotContain("Pet owners:petclinic-action-results", "Pet owners:duplicate-action-results");
        assertThat(page.locator("#causeway-result > cw-standalone-collection").count()).isEqualTo(1);
        page.locator("#causeway-result > [data-causeway-result-dismiss]").click();
        assertThat(page.locator("#causeway-result").isHidden()).isTrue();
        assertFocused("cw-action[id='allOwners'] [data-causeway-action-control]");

        page.route("**/_collection-presentations/petclinic.Malformed", route -> route.fulfill(
                new com.microsoft.playwright.Route.FulfillOptions()
                        .setContentType("text/html")
                        .setBody("<cw-standalone-collection><script>globalThis.__malformedPresentationExecuted = true</script></cw-standalone-collection>")));
        assertThat(page.evaluate("""
                async () => {
                  try {
                    await globalThis.causewayActionResultPresentationResolver({logicalTypeName: 'petclinic.Malformed'});
                    return 'accepted';
                  } catch {
                    return 'rejected';
                  }
                }
                """)).isEqualTo("rejected");
        assertThat(page.evaluate("() => globalThis.__malformedPresentationExecuted === true")).isEqualTo(false);
        assertThat(page.locator("html").getAttribute("data-causeway-collection-presentation-error"))
                .isEqualTo("resolution");
    }

    @Test
    @Order(8)
    @SuppressWarnings("unchecked")
    void actionResultDialogAndSidebarSurfacesRemainAccessibleAndResponsive() {
        openObject("petclinic.Visit", "s_visit-basil-checkup");
        waitForMenus();
        final var dialogOutlet = page.locator("[data-testid='petclinic-dialog-results']");
        assertThat(dialogOutlet.getAttribute("presentation-style")).isEqualTo("DIALOG");
        final var dialogScroll = ((Number) page.evaluate("() => window.scrollY")).doubleValue();
        constrainResultContentHeight(dialogOutlet);
        activateServiceAction("listAll");
        final var dialog = dialogOutlet.locator("dialog[data-causeway-action-results-surface='DIALOG']");
        dialog.waitFor();
        page.waitForFunction("() => document.querySelector(\"[data-testid='petclinic-dialog-results'] cw-standalone-collection\")?.resultState?.status === 'ready'");
        assertThat(dialog.getAttribute("aria-modal")).isEqualTo("true");
        assertThat(dialog.getAttribute("aria-label")).isEqualTo("Visit action results");
        assertScrollableResultWithDismissBelow(dialog);
        page.waitForFunction("() => document.querySelector(\"[data-testid='petclinic-dialog-results'] dialog\")?.contains(document.activeElement)");
        assertThat(((Number) page.evaluate("() => window.scrollY")).doubleValue()).isEqualTo(dialogScroll);
        dialog.locator("[data-causeway-result-dismiss]").focus();
        page.keyboard().press("Shift+Tab");
        page.waitForFunction("() => document.querySelector(\"[data-testid='petclinic-dialog-results'] dialog\")?.contains(document.activeElement)");
        page.keyboard().press("Escape");
        page.waitForFunction("() => document.querySelector(\"[data-testid='petclinic-dialog-results']\")?.hidden === true");
        assertThat(dialogOutlet.locator("dialog").count()).isZero();
        assertServiceResultOriginFocused();

        openObject("petclinic.Pet", "s_pet-basil");
        waitForMenus();
        final var sidebarOutlet = page.locator("[data-testid='petclinic-sidebar-results']");
        assertThat(sidebarOutlet.getAttribute("presentation-style")).isEqualTo("SIDEBAR");
        constrainResultContentHeight(sidebarOutlet);
        activateServiceAction("listAll");
        final var sidebar = sidebarOutlet.locator("aside[data-causeway-action-results-surface='SIDEBAR']");
        sidebar.waitFor();
        page.waitForFunction("() => document.querySelector(\"[data-testid='petclinic-sidebar-results'] cw-standalone-collection\")?.resultState?.status === 'ready'");
        assertThat(sidebar.getAttribute("role")).isEqualTo("complementary");
        assertThat(sidebar.getAttribute("aria-modal")).isNull();
        assertScrollableResultWithDismissBelow(sidebar);
        sidebar.locator("button:not([disabled]), a[href], [tabindex]:not([tabindex='-1'])").last().focus();
        page.keyboard().press("Tab");
        assertThat((Boolean) sidebar.evaluate("element => !element.contains(document.activeElement)")).isTrue();
        page.locator("cw-object-header cw-object-link button").focus();
        assertThat((Boolean) sidebar.evaluate("element => !element.contains(document.activeElement)")).isTrue();
        assertThat(sidebar.isVisible()).isTrue();

        sidebar.evaluate("element => { element.dataset.replacementProbe = 'old'; }");
        activateServiceAction("listAll");
        page.waitForFunction("() => { const surface = document.querySelector(\"[data-testid='petclinic-sidebar-results'] aside\"); return surface && surface.dataset.replacementProbe !== 'old'; }");
        assertThat(sidebarOutlet.locator("aside").count()).isEqualTo(1);
        sidebarOutlet.locator("[data-causeway-result-dismiss]").focus();
        page.keyboard().press("Escape");
        page.waitForFunction("() => document.querySelector(\"[data-testid='petclinic-sidebar-results']\")?.hidden === true");
        assertServiceResultOriginFocused();

        activateServiceAction("listAll");
        sidebarOutlet.locator("aside").waitFor();
        page.setViewportSize(480, 800);
        final var sidebarBounds = (List<Number>) sidebarOutlet.locator("aside").evaluate("element => { const rect = element.getBoundingClientRect(); return [rect.left, rect.right, rect.width, document.documentElement.scrollWidth, innerWidth]; }");
        assertThat(sidebarBounds.get(0).doubleValue()).isGreaterThanOrEqualTo(0);
        assertThat(sidebarBounds.get(1).doubleValue()).isLessThanOrEqualTo(sidebarBounds.get(4).doubleValue());
        assertThat(sidebarBounds.get(2).doubleValue()).isLessThanOrEqualTo(sidebarBounds.get(4).doubleValue());
        assertThat(sidebarBounds.get(3).doubleValue()).isLessThanOrEqualTo(sidebarBounds.get(4).doubleValue());
        sidebarOutlet.locator("[data-causeway-result-dismiss]").click();
        page.waitForFunction("() => document.querySelector(\"[data-testid='petclinic-sidebar-results']\")?.hidden === true");
        page.setViewportSize(1440, 900);
    }

    @Test
    @Order(9)
    void collectionRowPreviewsRemainSingleContextualAndRefreshAfterMutations() {
        openHome();
        waitForCollectionRows("petOwners", 5);
        waitForCollectionRows("futureVisits", 10);
        final var owners = page.locator("cw-collection[id='petOwners']");
        final var ownerToggles = owners.locator("button[data-causeway-preview-toggle]");
        ownerToggles.first().waitFor();
        assertThat(ownerToggles.count()).isEqualTo(5);
        assertPreviewToggleIcon(ownerToggles.first(), false);
        assertThat(page.locator("cw-collection[id='futureVisits'] button[data-causeway-preview-toggle]").count())
                .isEqualTo(10);
        assertThat(previewRequests.stream().filter(url -> url.endsWith("/_previews/petclinic.PetOwner")).count())
                .isEqualTo(1);
        assertThat(previewRequests.stream().filter(url -> url.endsWith("/_previews/petclinic.Visit")).count())
                .isEqualTo(1);

        ownerToggles.first().click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='petOwners']\")?.expandedPreviewKey != null");
        assertPreviewToggleIcon(ownerToggles.first(), true);
        var live = owners.locator("cw-preview[data-causeway-preview-live]");
        assertThat(live.count()).isEqualTo(1);
        assertThat(live.getAttribute("role")).isEqualTo("region");
        assertThat(live.getAttribute("aria-label")).contains("Preview of");
        assertThat(live.locator("section").first().getAttribute("aria-label")).isEqualTo("Owner preview");
        live.locator("cw-collection[id='pets']").waitFor();
        live.locator("cw-collection[id='pets'] .causeway-collection-table, cw-collection[id='pets'] cw-collection-grid")
                .first().waitFor();
        final var firstKey = (String) owners.evaluate("element => element.expandedPreviewKey");

        ownerToggles.nth(1).click();
        page.waitForFunction("key => { const collection = document.querySelector(\"cw-collection[id='petOwners']\"); return collection?.expandedPreviewKey && collection.expandedPreviewKey !== key; }", firstKey);
        assertPreviewToggleIcon(ownerToggles.first(), false);
        assertPreviewToggleIcon(ownerToggles.nth(1), true);
        assertThat(owners.locator("cw-preview[data-causeway-preview-live]").count()).isEqualTo(1);
        live = owners.locator("cw-preview[data-causeway-preview-live]");
        final var escapeOrigin = live.locator("cw-action[id='updateName'] [data-causeway-action-control]").first();
        escapeOrigin.waitFor();
        assertThat((Boolean) escapeOrigin.evaluate("""
                element => {
                  const target = element.matches('button,a,[tabindex]')
                    ? element
                    : element.shadowRoot?.querySelector('button,a,[tabindex]')
                      ?? element.querySelector('button,a,[tabindex]');
                  target?.focus();
                  return Boolean(target && target === target.getRootNode().activeElement);
                }
                """)).isTrue();
        page.keyboard().press("Escape");
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='petOwners']\")?.expandedPreviewKey == null");
        final var restoredToggle = ownerToggles.nth(1);
        assertThat((Boolean) restoredToggle.evaluate("element => element === element.getRootNode().activeElement"))
                .isTrue();

        ownerToggles.first().click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='petOwners']\")?.expandedPreviewKey != null");
        owners.locator("[data-causeway-collection-sort='name']").click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='petOwners']\")?.expandedPreviewKey == null");
        owners.locator("button[data-causeway-preview-toggle]").first().click();
        owners.locator("[data-causeway-grid-next]").click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='petOwners']\")?.expandedPreviewKey == null");
        assertThat(page.evaluate("() => globalThis.causewayCollectionRowPreviewResolver({logicalTypeName: 'petclinic.Missing'})"))
                .isNull();
        assertThat(previewRequests.stream().anyMatch(url -> url.endsWith("/_previews/petclinic.Missing"))).isTrue();

        final var petPreviewRequestsBefore = previewRequests.stream()
                .filter(url -> url.endsWith("/_previews/petclinic.Pet"))
                .count();
        openObject("petclinic.PetOwner", "s_owner-mary");
        waitForCollectionRows("pets", 2);
        final var pets = page.locator("cw-collection[id='pets']");
        final var petToggles = pets.locator("button[data-causeway-preview-toggle]");
        petToggles.first().waitFor();
        pets.evaluate("element => { element.gridResizeObserver?.disconnect(); element.acceptGridResponsiveState(true); }");
        petToggles.first().click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='pets']\")?.expandedPreviewKey != null");
        if (nativeToolkit()) {
            assertThat(pets.locator(".causeway-collection-preview-row cw-preview[data-causeway-preview-live]").count())
                    .isEqualTo(1);
        } else {
            final var grid = pets.locator("cw-collection-grid vaadin-grid");
            grid.waitFor();
            assertThat(((Number) grid.evaluate("element => element.detailsOpenedItems.length")).intValue())
                    .isEqualTo(1);
            assertThat(pets.locator("cw-collection-grid cw-preview[data-causeway-preview-live]").count())
                    .isEqualTo(1);
        }
        pets.evaluate("element => element.acceptGridResponsiveState(false)");
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='pets']\")?.expandedPreviewKey == null");
        petToggles.first().click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='pets']\")?.expandedPreviewKey != null");
        live = pets.locator("cw-preview[data-causeway-preview-live]");
        assertThat(live.locator("section").first().getAttribute("aria-label")).isEqualTo("Pet preview");
        assertThat(live.innerText()).doesNotContain("Pet type-default preview");
        live.locator("cw-collection[id='visits']").waitFor();
        assertThat(previewRequests.stream().filter(url -> url.endsWith("/_previews/petclinic.Pet")).count())
                .isEqualTo(petPreviewRequestsBefore);
        assertThat((Boolean) live.evaluate("element => { const rect = element.getBoundingClientRect(); return rect.left >= 0 && rect.right <= innerWidth; }"))
                .isTrue();

        final var notes = live.locator("cw-property[id='notes']");
        notes.locator("[data-causeway-action='edit']").click();
        final var editorSelector = "cw-collection[id='pets'] cw-preview[data-causeway-preview-live] cw-property[id='notes'] [data-causeway-editor='notes']";
        fillEditor(resolveEditor(editorSelector), "Updated through row preview");
        notes.locator("[data-causeway-action='save']").click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='pets']\")?.expandedPreviewKey == null");
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='pets']\")?.innerText.includes('Updated through row preview')");
        assertFocused("cw-collection[id='pets']");

        pets.locator("button[data-causeway-preview-toggle]").first().click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='pets']\")?.expandedPreviewKey != null");
        pets.locator("cw-preview[data-causeway-preview-live] cw-action[id='clearNotes'] [data-causeway-action-control], cw-preview[data-causeway-preview-live] cw-action[id='clearNotes'] button")
                .first().click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='pets']\")?.expandedPreviewKey == null");
        page.waitForFunction("() => !document.querySelector(\"cw-collection[id='pets']\")?.innerText.includes('Updated through row preview')");
        assertFocused("cw-collection[id='pets']");

        clickObjectLink("Basil");
        waitForRoute("petclinic.Pet", "s_pet-basil");
        assertThat(page.locator("[data-testid='petclinic-pet-page']").isVisible()).isTrue();
    }

    private void assertServiceResultOriginFocused() {
        page.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
        assertThat((String) page.evaluate("() => document.activeElement?.outerHTML ?? 'none'"))
                .contains("Pet Owners");
    }

    @SuppressWarnings("unchecked")
    private void assertMovableModalPrompt() {
        final var prompt = page.locator(PROMPT);
        final var before = (List<Number>) prompt.evaluate("element => { const rect = element.getBoundingClientRect(); return [rect.left, rect.top, rect.right, rect.bottom]; }");
        prompt.evaluate("""
                element => {
                  const handle = element.querySelector('[data-causeway-dialog-drag-handle]');
                  const rect = handle.getBoundingClientRect();
                  const clientX = rect.left + rect.width / 2;
                  const clientY = rect.top + rect.height / 2;
                  handle.dispatchEvent(new PointerEvent('pointerdown', {bubbles: true, cancelable: true, clientX, clientY}));
                  document.dispatchEvent(new PointerEvent('pointermove', {bubbles: true, clientX: clientX + 80, clientY: clientY + 60}));
                  document.dispatchEvent(new PointerEvent('pointerup', {bubbles: true, clientX: clientX + 80, clientY: clientY + 60}));
                }
                """);
        final var after = (List<Number>) prompt.evaluate("element => { const rect = element.getBoundingClientRect(); return [rect.left, rect.top, rect.right, rect.bottom]; }");
        assertThat(after.get(0).doubleValue()).isGreaterThan(before.get(0).doubleValue() + 50);
        assertThat(after.get(1).doubleValue()).isGreaterThan(before.get(1).doubleValue() + 30);
        final var viewport = (List<Number>) page.evaluate("() => [innerWidth, innerHeight]");
        assertThat(after.get(0).doubleValue()).isGreaterThanOrEqualTo(0);
        assertThat(after.get(1).doubleValue()).isGreaterThanOrEqualTo(0);
        assertThat(after.get(2).doubleValue()).isLessThanOrEqualTo(viewport.get(0).doubleValue() + 0.5);
        assertThat(after.get(3).doubleValue()).isLessThanOrEqualTo(viewport.get(1).doubleValue() + 0.5);
    }

    @Test
    @Order(10)
    void frameworkLogoutFailsClosedWhileApplicationActionsAndLocalResourcesRemainAvailable() {
        page.navigate(url("/htmx/object/petclinic.ViewerFallback/s_viewer-fallback"),
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        waitForPageKind("generic");
        waitForMenus();
        page.waitForFunction("() => document.querySelector('cw-object cw-action#openLocalResource')?.componentState?.status === 'ready'");

        assertThat(page.locator("cw-menubars [data-service-logical-type='causeway.security.LogoutMenu'][data-action-id='logout']").count()).isZero();
        final var logoutInvocationsBefore = graphQLRequestCount("CausewayInvokeServiceAction", "LogoutMenu");
        page.locator("#causeway-route").evaluate("element => element.dispatchEvent(new CustomEvent('causeway-action-request', { bubbles: true, composed: true, cancelable: true, detail: { serviceLogicalTypeName: 'causeway.security.LogoutMenu', actionId: 'logout', context: {} } }))");
        page.waitForTimeout(50);
        assertThat(graphQLRequestCount("CausewayInvokeServiceAction", "LogoutMenu")).isEqualTo(logoutInvocationsBefore);
        assertThat(page.locator("#causeway-route-announcement").innerText()).contains("host authentication");

        page.locator("cw-object cw-action#logout [data-causeway-action-control]").click();
        waitForShellResult("logout", "Application action completed");

        final var localResourcePopup = page.waitForPopup(() ->
                page.locator("cw-object cw-action#openLocalResourceInNewWindow [data-causeway-action-control]").click());
        localResourcePopup.locator("h1").waitFor();
        assertThat(localResourcePopup.locator("h1").innerText()).isEqualTo("Pet Clinic local resource");
        localResourcePopup.close();

        page.locator("cw-object cw-action#openLocalResource [data-causeway-action-control]").click();
        page.waitForURL("**/petclinic-local-resource.html");
        assertThat(page.locator("h1").innerText()).isEqualTo("Pet Clinic local resource");
    }

    @Test
    @Order(11)
    void authoredPdfModesProvideCompleteProgressiveReadingAndPersistentLinks() {
        openObject("petclinic.Visit", "s_visit-basil-checkup");
        final var linked = page.locator("cw-property#ownerAgreementDocument");
        assertThat(linked.getAttribute("data-renderer")).isEqualTo("blob");
        assertThat(linked.locator("[data-causeway-pdf-reader]").count()).isZero();
        assertThat(linked.locator(".causeway-value-lob-link").innerText()).isEqualTo("owner-mary-clinic-agreement.pdf");
        final var linkedHref = linked.locator(".causeway-value-lob-link").getAttribute("href");
        assertThat((Boolean) page.evaluate("href => performance.getEntriesByType('resource').some(entry => entry.name === new URL(href, document.baseURI).href)", linkedHref)).isFalse();
        assertThat((Boolean) page.evaluate("() => performance.getEntriesByType('resource').some(entry => entry.name.includes('/pdfjs/'))")).isFalse();

        openObject("petclinic.Pet", "s_pet-basil");
        final var manual = page.locator("cw-property#ownerAgreementPreview [data-causeway-pdf-reader]");
        assertThat(manual.getAttribute("data-causeway-pdf-state")).isEqualTo("inactive");
        assertThat(manual.locator("[data-causeway-pdf-page]").count()).isZero();
        assertThat(manual.locator(".causeway-pdf-toolbar .causeway-value-lob-link").innerText()).isEqualTo("owner-mary-clinic-agreement.pdf");
        assertThat(manual.locator(".causeway-pdf-accessibility-note").count()).isZero();
        final var manualHref = manual.locator(".causeway-value-lob-link").getAttribute("href");
        assertThat((Boolean) page.evaluate("href => performance.getEntriesByType('resource').some(entry => entry.name === new URL(href, document.baseURI).href)", manualHref)).isFalse();
        assertThat((Boolean) page.evaluate("() => performance.getEntriesByType('resource').some(entry => entry.name.includes('/pdfjs/'))")).isFalse();
        manual.locator("[data-causeway-pdf-activate]").click();
        page.waitForFunction("() => document.querySelector('cw-property#ownerAgreementPreview [data-causeway-pdf-reader]')?.dataset.causewayPdfState === 'ready'");
        assertThat(manual.locator("[data-causeway-pdf-page]").count()).isEqualTo(3);
        assertThat(manual.locator("[data-causeway-pdf-status]").innerText()).contains("Page 2 of 3");
        assertThat((Boolean) page.evaluate("href => performance.getEntriesByType('resource').some(entry => entry.name === new URL(href, document.baseURI).href)", manualHref)).isTrue();
        assertThat((Boolean) page.evaluate("() => performance.getEntriesByType('resource').some(entry => entry.name.includes('/pdfjs/pdf.min.mjs'))")).isTrue();

        openObject("petclinic.PetOwner", "s_owner-mary");
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='pets']\")?.collectionState?.rows?.length === 2");
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='visits']\")?.collectionState?.rows?.length === 2");
        final var agreementProperty = page.locator("cw-property#agreement");
        final var automatic = agreementProperty.locator("[data-causeway-pdf-reader]");
        page.waitForFunction("() => document.querySelector('cw-property#agreement [data-causeway-pdf-reader]')?.dataset.causewayPdfState === 'ready'");
        assertThat(agreementProperty.getAttribute("label-position")).isEqualTo("NONE");
        final var agreementCard = page.locator(".petclinic-object-collections .petclinic-agreement-card");
        assertThat(agreementCard.locator("cw-property#agreement").count()).isEqualTo(1);
        assertThat(agreementProperty.locator(".causeway-property-label").isVisible()).isFalse();
        assertThat(((Number) automatic.evaluate("(reader) => reader.getBoundingClientRect().width / reader.closest('.petclinic-agreement-card').getBoundingClientRect().width")).doubleValue()).isGreaterThan(0.9);
        assertThat(automatic.locator("[data-causeway-pdf-page]").count()).isEqualTo(3);
        automatic.locator(".causeway-pdf-page-canvas").first().waitFor();
        final var toolbar = automatic.locator(".causeway-pdf-toolbar");
        final var download = toolbar.locator(".causeway-value-lob-link");
        assertThat(download.getAttribute("href")).isNotBlank();
        assertThat(download.innerText()).isEqualTo("owner-mary-clinic-agreement.pdf");
        assertThat(automatic.locator(".causeway-pdf-accessibility-note").count()).isZero();

        assertContainedPdfNavigation(automatic, 1800);
        assertContainedPdfNavigation(automatic, 720);
        page.setViewportSize(1440, 900);

        automatic.locator("[data-causeway-pdf-next]").click();
        automatic.locator("[data-causeway-pdf-next]").click();
        final var finalPage = automatic.locator("[data-causeway-pdf-page='3']");
        finalPage.locator("canvas").waitFor();
        assertThat(finalPage.getAttribute("aria-label")).isEqualTo("PDF page 3 of 3");
        automatic.locator("[data-causeway-pdf-zoom-in]").click();
        assertThat(automatic.locator("[data-causeway-pdf-zoom-select]").inputValue()).isEqualTo("125");
    }

    private void assertContainedPdfNavigation(final Locator reader, final int width) {
        page.setViewportSize(width, 900);
        final var toolbar = reader.locator(".causeway-pdf-toolbar");
        final var viewport = reader.locator("[data-causeway-pdf-viewport]");
        final var next = reader.locator("[data-causeway-pdf-next]");
        final var previous = reader.locator("[data-causeway-pdf-previous]");
        toolbar.scrollIntoViewIfNeeded();
        final var outerScrollBefore = ((Number) page.evaluate("() => window.scrollY")).doubleValue();
        final var geometryBefore = (String) page.evaluate("() => JSON.stringify({scrollY: window.scrollY, toolbar: document.querySelector('cw-property#agreement .causeway-pdf-toolbar')?.getBoundingClientRect(), viewport: document.querySelector('cw-property#agreement [data-causeway-pdf-viewport]')?.getBoundingClientRect()})");

        next.click();
        page.waitForTimeout(350);
        assertThat(reader.locator("[data-causeway-pdf-status]").innerText()).contains("Page 2 of 3");
        assertThat(((Number) viewport.evaluate("element => element.scrollTop")).doubleValue()).isGreaterThan(0);
        assertThat(Math.abs(((Number) page.evaluate("() => window.scrollY")).doubleValue() - outerScrollBefore)).isLessThan(2.0);
        assertThat((Boolean) next.evaluate("element => document.activeElement === element")).isTrue();
        assertThat(toolbar.isVisible()).isTrue();
        assertThat((Boolean) toolbar.evaluate("element => { const rect = element.getBoundingClientRect(); return rect.top >= 0 && rect.bottom <= window.innerHeight; }")).isTrue();
        assertThat(((Number) page.evaluate("() => document.documentElement.scrollWidth - document.documentElement.clientWidth")).doubleValue()).isLessThanOrEqualTo(1.0);

        previous.click();
        page.waitForTimeout(350);
        assertThat(reader.locator("[data-causeway-pdf-status]").innerText()).contains("Page 1 of 3");
        final var outerScrollAfter = ((Number) page.evaluate("() => window.scrollY")).doubleValue();
        final var geometryAfter = (String) page.evaluate("() => JSON.stringify({scrollY: window.scrollY, toolbar: document.querySelector('cw-property#agreement .causeway-pdf-toolbar')?.getBoundingClientRect(), viewport: document.querySelector('cw-property#agreement [data-causeway-pdf-viewport]')?.getBoundingClientRect()})");
        assertThat(Math.abs(outerScrollAfter - outerScrollBefore))
                .as("PDF navigation moved the outer page; before=%s after=%s", geometryBefore, geometryAfter)
                .isLessThan(2.0);
        assertThat((Boolean) next.evaluate("element => document.activeElement === element")).isTrue();
        assertThat(toolbar.isVisible()).isTrue();
        assertThat((Boolean) toolbar.evaluate("element => { const rect = element.getBoundingClientRect(); return rect.top >= 0 && rect.bottom <= window.innerHeight; }")).isTrue();
        assertSelectablePdfZoom(reader, viewport, outerScrollBefore);
    }

    private void assertPreviewToggleIcon(final Locator toggle, final boolean expanded) {
        assertThat(toggle.getAttribute("aria-expanded")).isEqualTo(Boolean.toString(expanded));
        final var icon = toggle.locator("svg.causeway-collection-preview-icon");
        assertThat(icon.count()).isEqualTo(1);
        assertThat(icon.getAttribute("aria-hidden")).isEqualTo("true");
        assertThat(icon.getAttribute("focusable")).isEqualTo("false");
        assertThat((Boolean) toggle.evaluate("""
                (button, expanded) => {
                  const icon = button.querySelector('.causeway-collection-preview-icon');
                  const iconRect = icon.getBoundingClientRect();
                  const buttonRect = button.getBoundingClientRect();
                  const matrix = new DOMMatrix(getComputedStyle(icon).transform);
                  const directionIsCorrect = expanded
                    ? Math.abs(matrix.a) < 0.01 && Math.abs(matrix.b - 1) < 0.01
                    : Math.abs(matrix.a - 1) < 0.01 && Math.abs(matrix.b) < 0.01;
                  return iconRect.width >= 17 && Math.abs(iconRect.width - iconRect.height) < 1
                    && iconRect.width < buttonRect.width && directionIsCorrect;
                }
                """, expanded)).isTrue();
        assertThat(((Number) page.evaluate("() => document.documentElement.scrollWidth - document.documentElement.clientWidth")).doubleValue())
                .isLessThanOrEqualTo(1.0);
    }

    private void assertSelectablePdfZoom(final Locator reader, final Locator viewport, final double outerScrollBefore) {
        final var select = reader.locator("[data-causeway-pdf-zoom-select]");
        assertThat(select.locator("option").allTextContents())
                .containsSubsequence("Page width", "Page height", "Page fit", "Actual size", "50%", "100%", "200%");
        final var pageStatus = reader.locator("[data-causeway-pdf-status]").innerText();

        select.selectOption("150");
        assertThat(select.inputValue()).isEqualTo("150");
        select.selectOption("page-height");
        reader.locator("[data-causeway-pdf-page='1'] canvas").waitFor();
        assertThat(select.inputValue()).isEqualTo("page-height");
        final var pageHeight = ((Number) reader.locator("[data-causeway-pdf-page='1'] canvas")
                .evaluate("canvas => parseFloat(canvas.style.height)")).doubleValue();
        final var availableHeight = ((Number) viewport.evaluate("element => element.clientHeight - 24")).doubleValue();
        assertThat(Math.abs(pageHeight - availableHeight)).isLessThanOrEqualTo(1.0);

        select.selectOption("page-fit");
        assertThat(select.inputValue()).isEqualTo("page-fit");
        select.selectOption("200");
        reader.locator("[data-causeway-pdf-zoom-in]").click();
        assertThat(select.inputValue()).isEqualTo("225");
        assertThat(select.locator("option[value='225']").innerText()).isEqualTo("225%");
        select.selectOption("page-width");
        reader.locator("[data-causeway-pdf-page='1'] canvas").waitFor();
        assertThat(select.inputValue()).isEqualTo("page-width");
        final var pageWidth = ((Number) reader.locator("[data-causeway-pdf-page='1'] canvas")
                .evaluate("canvas => parseFloat(canvas.style.width)")).doubleValue();
        final var availableWidth = ((Number) viewport.evaluate("element => element.clientWidth - 24")).doubleValue();
        assertThat(Math.abs(pageWidth - availableWidth)).isLessThanOrEqualTo(1.0);

        assertThat(reader.locator("[data-causeway-pdf-status]").innerText()).isEqualTo(pageStatus);
        assertThat(Math.abs(((Number) page.evaluate("() => window.scrollY")).doubleValue() - outerScrollBefore)).isLessThan(2.0);
        assertThat(((Number) page.evaluate("() => document.documentElement.scrollWidth - document.documentElement.clientWidth")).doubleValue()).isLessThanOrEqualTo(1.0);
    }

    @SuppressWarnings("unchecked")
    private void assertApplicationUsesAvailableWidth() {
        page.setViewportSize(1800, 900);
        final var geometry = (List<Number>) page.evaluate("""
                () => {
                  const viewport = document.documentElement.clientWidth;
                  const main = document.querySelector('.causeway-shell-main').getBoundingClientRect();
                  const navbar = document.querySelector('.causeway-shell-navbar').getBoundingClientRect();
                  return [viewport, main.width, main.left, viewport - main.right, navbar.width];
                }
                """);
        final var viewport = geometry.get(0).doubleValue();
        assertThat(page.locator("html").evaluate("element => getComputedStyle(element).getPropertyValue('--causeway-content-width').trim()"))
                .isEqualTo("100%");
        assertThat(page.locator("html").evaluate("element => getComputedStyle(element).getPropertyValue('--causeway-shell-width').trim()"))
                .isEqualTo("100%");
        assertThat(geometry.get(1).doubleValue()).isGreaterThan(viewport * 0.95);
        assertThat(geometry.get(2).doubleValue()).isBetween(8.0, 32.0);
        assertThat(geometry.get(3).doubleValue()).isBetween(8.0, 32.0);
        assertThat(geometry.get(4).doubleValue()).isGreaterThan(viewport * 0.95);
        page.setViewportSize(1440, 900);
    }

    private void openHome() {
        page.navigate(url("/htmx"), new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        waitForPageKind("custom");
        waitForMenus();
    }

    private void waitForMenus() {
        page.waitForFunction("() => ['ready', 'partial-error'].includes(document.querySelector('cw-menubars')?.dataset.menuState)");
        if (nativeToolkit()) {
            assertThat(page.locator("cw-menubar-control").count()).isZero();
            return;
        }
        page.waitForFunction("() => [...document.querySelectorAll('cw-menubar-primary, cw-menubar-secondary, cw-menubar-tertiary')].filter(element => !element.hidden).every(element => element.dataset.causewayMenubarPresentation?.startsWith('vaadin-') && element.querySelector('cw-menubar-control')?.dataset.widgetState === 'ready')");
        assertThat(((Number) page.evaluate("() => performance.getEntriesByType('resource').filter(entry => entry.name.includes('/vaadin-menubar/vaadin-menubar.js')).length")).intValue())
                .isEqualTo(1);
    }

    private void assertOrdinaryTertiaryActions() {
        assertThat(page.locator("cw-menubar-secondary").count()).isZero();
        assertThat(page.locator("cw-menubar-tertiary").evaluate(
                "element => element._projection.menus.map(menu => menu.label).join(',')")).isEqualTo("Account");
        assertThat((Boolean) page.locator("cw-menubar-tertiary").evaluate("""
                element => {
                  const actions = Object.values(element._projection?.actions ?? {});
                  const has = (identity, label) => actions.some(action =>
                    `${action.serviceLogicalTypeName}#${action.actionId}` === identity
                      && action.label === label
                      && action.role === 'tertiary');
                  return has('causeway.applib.UserMenu#me', 'Me')
                    && has('causeway.conf.ConfigurationMenu#configuration', 'Configuration')
                    && !actions.some(action => action.serviceLogicalTypeName === 'causeway.security.LogoutMenu')
                    && !actions.some(action => action.serviceLogicalTypeName === 'causeway.ext.secman.MeService');
                }
                """))
                .as(page.locator("cw-menubar-tertiary").evaluate("element => Object.values(element._projection?.actions ?? {}).map(action => `${action.serviceLogicalTypeName}#${action.actionId}:${action.label}:${action.role}`)").toString())
                .isTrue();
    }

    private void openObject(final String logicalTypeName, final String id) {
        page.navigate(url("/htmx/object/" + logicalTypeName + "/" + id),
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        waitForRoute(logicalTypeName, id);
    }

    private void waitForPageKind(final String pageKind) {
        page.waitForFunction("kind => { const page = document.querySelector('[data-testid=\"causeway-route-page\"]'); return page?.dataset.pageKind === kind && ['ready', 'partial-error'].includes(page.dataset.routeState); }", pageKind);
    }

    private void waitForRoute(final String logicalTypeName, final String id) {
        waitForRouteUrl("/htmx/object/" + logicalTypeName + "/" + id);
        waitForPageKind("custom");
        page.waitForFunction("() => document.querySelector('[data-testid=\"causeway-route-page\"]')?.dataset.pageSource === 'resource'");
    }

    private void waitForLogicalType(final String logicalTypeName) {
        page.waitForFunction("logicalType => { const context = document.querySelector('#causeway-route cw-object-context'); const page = document.querySelector('[data-testid=\"causeway-route-page\"]'); return context?.getAttribute('logical-type') === logicalType && ['ready', 'partial-error'].includes(page?.dataset.routeState); }", logicalTypeName);
    }

    private void waitForRouteUrl(final String pathOrUrl) {
        final var expectedPath = pathOrUrl.startsWith("http") ? java.net.URI.create(pathOrUrl).getPath() : pathOrUrl;
        page.waitForFunction("path => location.pathname === path && ['ready', 'partial-error'].includes(document.querySelector('[data-testid=\"causeway-route-page\"]')?.dataset.routeState)", expectedPath);
    }

    private void clickObjectLink(final String titlePart) {
        clickObjectLink(page.locator("cw-object-link button"), titlePart);
    }

    private void clickObjectLinkInCollection(final String member, final String titlePart) {
        clickObjectLink(page.locator("cw-collection[id='" + member + "'] cw-object-link button"), titlePart);
    }

    private void clickObjectLink(final Locator links, final String titlePart) {
        final var link = links.filter(new Locator.FilterOptions().setHasText(titlePart)).first();
        link.waitFor();
        link.click();
    }

    private void assertObjectLinkIcon(final Locator objectLink) {
        objectLink.waitFor();
        final var icon = objectLink.locator(".causeway-object-link-icon");
        assertThat(icon.count()).isEqualTo(1);
        assertThat(icon.getAttribute("alt")).isEmpty();
        assertThat(icon.getAttribute("aria-hidden")).isEqualTo("true");
        assertThat(icon.getAttribute("src")).contains("/_meta/icon");
        assertThat((Boolean) icon.evaluate(
                "image => image.decode().then(() => image.complete && image.naturalWidth > 0).catch(() => false)"))
                .isTrue();
    }

    private void openMenu(final String name) {
        if (!nativeToolkit()) {
            final var menuButton = page.locator("vaadin-menu-bar-button")
                    .filter(new Locator.FilterOptions().setHasText(name)).first();
            menuButton.waitFor();
            menuButton.click();
            page.locator("vaadin-menu-bar-overlay[opened]").waitFor();
            return;
        }
        final var disclosure = menuDisclosure(name);
        disclosure.waitFor();
        if (!"true".equals(disclosure.getAttribute("aria-expanded"))) {
            disclosure.click();
        }
    }

    private Locator menuDisclosure(final String name) {
        return page.locator("[data-causeway-menu-disclosure]")
                .filter(new Locator.FilterOptions().setHasText(name)).first();
    }

    private void assertMenuClosed(final String name) {
        if (!nativeToolkit()) {
            assertThat(page.locator("cw-menubar-control").count()).isGreaterThan(0);
            assertThat(page.locator("[data-causeway-menu-disclosure]").count()).isZero();
            assertThat(page.locator("vaadin-menu-bar-overlay[opened]").count()).isZero();
            return;
        }
        final var disclosure = menuDisclosure(name);
        page.waitForFunction("id => { const disclosure = document.querySelector(`[data-causeway-menu-disclosure][aria-controls='${id}']`); const panel = document.getElementById(id); return disclosure?.getAttribute('aria-expanded') === 'false' && panel?.hidden === true; }",
                disclosure.getAttribute("aria-controls"));
    }

    private void assertMenuClosedAndFocused(final String name) {
        if (!nativeToolkit()) {
            assertMenuClosed(name);
            page.locator("cw-menubar-control[data-causeway-menubar-tier='primary']").evaluate("element => element.focus()");
            page.waitForFunction("() => document.querySelector('cw-menubar-control[data-causeway-menubar-tier=\"primary\"]')?.contains(document.activeElement)");
            return;
        }
        final var disclosure = menuDisclosure(name);
        final var panelId = disclosure.getAttribute("aria-controls");
        assertMenuClosed(name);
        assertFocused("[data-causeway-menu-disclosure][aria-controls='" + panelId + "']");
    }

    private Locator serviceAction(final String actionId) {
        return page.locator("[data-service-logical-type^='petclinic.'][data-action-id='" + actionId + "']").first();
    }

    private Locator serviceActionPresentation(final String actionId) {
        return serviceAction(actionId).locator("xpath=..");
    }

    private Set<String> petclinicServiceActionIds() {
        if (!nativeToolkit()) {
            final var values = (List<?>) page.evaluate("() => [...document.querySelectorAll('cw-menubar-primary, cw-menubar-secondary, cw-menubar-tertiary')].flatMap(host => Object.values(host._projection?.actions ?? {}).filter(action => action.serviceLogicalTypeName.startsWith('petclinic.')).map(action => action.actionId))");
            final var actionIds = new LinkedHashSet<String>();
            values.forEach(value -> actionIds.add(String.valueOf(value)));
            return actionIds;
        }
        final var actionIds = new LinkedHashSet<String>();
        for (final var action : page.locator("[data-service-logical-type^='petclinic.'][data-action-id]").all()) {
            actionIds.add(action.getAttribute("data-action-id"));
        }
        return actionIds;
    }

    private void activateServiceAction(final String actionId) {
        if (nativeToolkit()) {
            final var action = serviceAction(actionId);
            if (!action.isVisible()) {
                final var panelId = (String) action.evaluate("element => element.closest('[data-causeway-menu-panel]')?.id");
                page.locator("[data-causeway-menu-disclosure][aria-controls='" + panelId + "']").click();
            }
            action.click();
            return;
        }
        final var key = (String) page.evaluate("""
                actionId => {
                  for (const host of document.querySelectorAll('cw-menubar-primary, cw-menubar-secondary, cw-menubar-tertiary')) {
                    const descriptor = Object.values(host._projection?.actions ?? {}).find(action => action.actionId === actionId);
                    if (descriptor && !descriptor.disabled) return descriptor.key;
                  }
                  return null;
                }
                """, actionId);
        assertThat(key).isNotBlank();
        final var menuItem = page.locator("vaadin-menu-bar-item[data-causeway-key='" + key + "']");
        if (!menuItem.isVisible()) {
            final var keyParts = key.split(":");
            final var tier = keyParts[1];
            final var menuIndex = Integer.parseInt(keyParts[2]);
            page.locator("cw-menubar-" + tier + " cw-menubar-control vaadin-menu-bar-button")
                    .nth(menuIndex).click();
        }
        menuItem.waitFor();
        menuItem.click();
    }

    private void assertDefaultOrNativeMemberPresentation(final String propertyMember, final String actionMember) {
        final var propertySelector = "cw-property[id='" + propertyMember + "']";
        page.waitForFunction("selector => document.querySelector(selector)?.dataset.renderer", propertySelector);
        final var actionSelector = "cw-action[id='" + actionMember + "'] [data-causeway-action-control]";
        page.locator(actionSelector).first().waitFor();
        if (nativeToolkit()) {
            assertThat(page.locator(propertySelector).getAttribute("data-renderer")).isNotEqualTo("vaadin-field-view");
            assertThat(page.locator(propertySelector + " cw-field-editor[data-mode='view']").count()).isZero();
            assertThat(page.locator(actionSelector).first().evaluate("element => element.localName")).isEqualTo("button");
            return;
        }
        final var field = page.locator(propertySelector + " cw-field-editor[data-mode='view']");
        field.waitFor();
        page.waitForFunction("selector => document.querySelector(selector)?.dataset.widgetState === 'ready'", propertySelector + " cw-field-editor[data-mode='view']");
        final var fieldControl = field.locator("vaadin-text-field");
        assertThat(fieldControl.getAttribute("readonly")).isNotNull();
        assertThat(page.locator(actionSelector).first().evaluate("element => element.localName")).isEqualTo("cw-action-control");
        page.locator(actionSelector + " vaadin-button").waitFor();
        assertThat(toolkitRequests.stream().anyMatch(url -> url.contains("/vaadin-fields/vaadin-basic.js"))).isTrue();
        assertThat(toolkitRequests.stream().anyMatch(url -> url.contains("/vaadin-actions/vaadin-actions.js"))).isTrue();
    }

    private Locator objectAction(final String member) {
        final var host = page.locator("cw-action[id='" + member + "']").first();
        final var action = host.locator("[data-causeway-action-control]");
        action.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED));
        revealContainingTab(host, action);
        return action;
    }

    private void waitForReferenceParameterReady(final String parameterId) {
        if (nativeToolkit()) {
            final var editor = page.locator(parameter(parameterId));
            editor.waitFor();
            assertThat(editor.evaluate("element => element.localName")).isEqualTo("select");
            return;
        }
        page.waitForFunction(
                "selector => document.querySelector(selector)?.dataset.widgetState === 'ready'",
                parameter(parameterId));
    }

    private void waitForPrompt(final String actionId) {
        waitForPrompt(actionId, humanize(actionId));
    }

    private void waitForPrompt(final String actionId, final String expectedTitle) {
        page.locator(PROMPT).waitFor();
        assertThat(page.locator(PROMPT + " h2").textContent()).isEqualTo(expectedTitle);
    }

    private void fillParameter(final String parameterId, final String value) {
        final var selector = parameter(parameterId);
        fillEditor(resolveEditor(selector), value);
        page.waitForFunction("args => document.querySelector(args.selector)?.value === args.value",
                java.util.Map.of("selector", selector, "value", value));
    }

    private void selectParameter(final String parameterId, final String value) {
        final var selector = parameter(parameterId);
        final var control = resolveEditor(selector);
        if (((String) control.evaluate("element => element.localName")).startsWith("vaadin-")) {
            control.evaluate("(element, value) => { element.value = value; element.dispatchEvent(new Event('change', {bubbles: true, composed: true})); }", value);
        } else {
            control.selectOption(value);
        }
        page.waitForFunction("args => document.querySelector(args.selector)?.value === args.value",
                java.util.Map.of("selector", selector, "value", value));
    }

    private void selectFirstAvailableChoice(final String parameterId) {
        final var control = page.locator(parameter(parameterId));
        if ("cw-reference-editor".equals(control.evaluate("element => element.localName"))) {
            page.waitForFunction("selector => document.querySelector(selector)?.dataset.widgetState === 'ready'", parameter(parameterId));
            final var label = (String) control.evaluate("element => element.querySelector('vaadin-combo-box').items[0].title");
            final var input = control.locator("vaadin-combo-box input");
            input.fill(label);
            page.waitForTimeout(100);
            input.focus();
            page.keyboard().press("ArrowDown");
            page.keyboard().press("Enter");
            page.waitForFunction("selector => document.querySelector(selector)?.value?.id", parameter(parameterId));
            return;
        }
        final var value = (String) control.evaluate("select => [...select.options].find(option => option.value)?.value");
        assertThat(value).isNotBlank();
        selectParameter(parameterId, value);
    }

    private void tabOutOfParameter(final String parameterId) {
        final var selector = parameter(parameterId);
        for (var attempt = 0; attempt < 4; attempt++) {
            page.keyboard().press("Tab");
            final var focusLeft = (Boolean) page.locator(selector).evaluate(
                    "element => element !== document.activeElement && !element.contains(document.activeElement) && !element.shadowRoot?.activeElement");
            if (focusLeft) {
                return;
            }
        }
        throw new AssertionError("Focus remained in action parameter " + parameterId);
    }

    private void submitPrompt() {
        final var submit = page.locator("[data-testid='action-prompt-submit']");
        submit.waitFor();
        submit.click();
    }

    private void submitPromptExpectingNavigation() {
        final var generation = Integer.parseInt(page.locator("[data-testid='causeway-route']")
                .getAttribute("data-navigation-generation"));
        submitPrompt();
        try {
            page.waitForFunction("generation => { const route = document.querySelector('[data-testid=\"causeway-route\"]'); return Number(route?.dataset.navigationGeneration) > generation && route.getAttribute('aria-busy') === 'false'; }", generation);
        } catch (final com.microsoft.playwright.TimeoutError cause) {
            throw new AssertionError("Expected navigation after action at " + page.url()
                    + "; generation=" + page.locator("[data-testid='causeway-route']").getAttribute("data-navigation-generation")
                    + "; busy=" + page.locator("[data-testid='causeway-route']").getAttribute("aria-busy")
                    + "; prompt=" + (page.locator("[data-testid='action-prompt']").count() > 0
                    ? page.locator("[data-testid='action-prompt']").evaluate("element => element.outerHTML") : "none")
                    + "; result=" + page.locator("[data-testid='causeway-shell-result']").textContent(), cause);
        }
    }

    private void cancelPrompt() {
        page.locator("[data-testid='action-prompt-cancel']").click();
        page.locator(PROMPT).waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED));
    }

    private void waitForPromptError(final String messagePart) {
        final var alert = page.locator(PROMPT + " [role='alert']")
                .filter(new Locator.FilterOptions().setHasText(messagePart)).first();
        alert.waitFor();
    }

    @SuppressWarnings("unchecked")
    private void assertSingleToolkitFieldBoundary(final String member) {
        final var property = page.locator("cw-property[id='" + member + "']");
        if (nativeToolkit()) {
            assertThat(property.locator("cw-field-editor").count()).isZero();
            return;
        }
        page.waitForFunction("member => document.querySelector(`cw-property[id='${member}'] cw-field-editor[data-mode='view'] > [data-causeway-field-view]`)?.shadowRoot?.querySelector('[part~=input-field]')", member);
        final var borderWidths = (List<Number>) property.evaluate("""
                element => {
                  const control = element.querySelector('cw-field-editor[data-mode="view"] > [data-causeway-field-view]');
                  const container = control.shadowRoot.querySelector('[part~="input-field"]');
                  const input = control.querySelector('[slot="input"]');
                  const borderWidth = candidate => {
                    const style = getComputedStyle(candidate);
                    return ['Top', 'Right', 'Bottom', 'Left']
                      .reduce((total, side) => total + Number.parseFloat(style[`border${side}Width`]), 0);
                  };
                  return [borderWidth(container), borderWidth(input)];
                }
                """);
        assertThat(borderWidths.get(0).doubleValue())
                .as("toolkit field boundary for %s", member)
                .isGreaterThan(0.0);
        assertThat(borderWidths.get(1).doubleValue())
                .as("nested native input boundary for %s", member)
                .isZero();
    }

    @SuppressWarnings("unchecked")
    private void assertWideMultilinePropertyLayout(final Locator property) {
        final var geometry = (List<Number>) property.evaluate("element => { const label = element.querySelector('.causeway-property-label').getBoundingClientRect(); const description = element.querySelector('.causeway-property-description').getBoundingClientRect(); const value = element.querySelector('.causeway-property-value').getBoundingClientRect(); const edit = element.querySelector('.causeway-property-edit').getBoundingClientRect(); return [label.left, label.right, label.bottom, description.left, description.top, value.left, value.top, edit.left, edit.width]; }");
        assertThat(Math.abs(geometry.get(3).doubleValue() - geometry.get(0).doubleValue())).isLessThanOrEqualTo(1.0);
        assertThat(geometry.get(4).doubleValue()).isGreaterThanOrEqualTo(geometry.get(2).doubleValue() - 1.0);
        assertThat(geometry.get(5).doubleValue()).isGreaterThanOrEqualTo(geometry.get(1).doubleValue());
        assertThat(geometry.get(6).doubleValue()).isLessThanOrEqualTo(geometry.get(4).doubleValue());
        assertThat(geometry.get(7).doubleValue()).isGreaterThanOrEqualTo(geometry.get(5).doubleValue());
        assertThat(geometry.get(8).doubleValue()).isLessThan(160.0);
    }

    @SuppressWarnings("unchecked")
    private void assertNarrowMultilinePropertyLayout(final Locator property) {
        final var geometry = (List<Number>) property.evaluate("element => { const label = element.querySelector('.causeway-property-label').getBoundingClientRect(); const description = element.querySelector('.causeway-property-description').getBoundingClientRect(); const value = element.querySelector('.causeway-property-value').getBoundingClientRect(); const edit = element.querySelector('.causeway-property-edit').getBoundingClientRect(); return [label.bottom, description.top, description.bottom, value.top, edit.top, edit.width, document.documentElement.scrollWidth, document.documentElement.clientWidth]; }");
        assertThat(geometry.get(1).doubleValue()).isGreaterThanOrEqualTo(geometry.get(0).doubleValue() - 1.0);
        assertThat(geometry.get(3).doubleValue()).isGreaterThanOrEqualTo(geometry.get(2).doubleValue() - 1.0);
        assertThat(Math.abs(geometry.get(4).doubleValue() - geometry.get(3).doubleValue()))
                .as("narrow multiline edit and value top alignment: %s", geometry)
                .isLessThanOrEqualTo(1.0);
        assertThat(geometry.get(5).doubleValue()).isLessThan(160.0);
        assertThat(geometry.get(6).doubleValue()).isLessThanOrEqualTo(geometry.get(7).doubleValue() + 1.0);
    }

    @SuppressWarnings("unchecked")
    private void assertSingleVaadinMultilineFocus(final Locator editor) {
        List<String> focusStyles = List.of();
        for (var attempt = 0; attempt < 10; attempt++) {
            editor.evaluate("element => element.inputElement.focus()");
            page.waitForTimeout(50);
            focusStyles = (List<String>) editor.evaluate("""
                    element => {
                      const host = getComputedStyle(element);
                      const field = getComputedStyle(element.shadowRoot.querySelector('[part~="input-field"]'));
                      const input = getComputedStyle(element.inputElement);
                      const inputBorder = ['Top', 'Right', 'Bottom', 'Left']
                        .reduce((total, side) => total + Number.parseFloat(input[`border${side}Width`]), 0);
                      return [host.outlineStyle, host.outlineWidth, field.outlineStyle, field.outlineWidth,
                        String(inputBorder), input.outlineStyle];
                    }
                    """);
            if (focusStyles.get(0).equals("none")
                    && focusStyles.get(2).equals("solid")
                    && Double.parseDouble(focusStyles.get(3).replace("px", "")) > 0.0
                    && Double.parseDouble(focusStyles.get(4)) == 0.0
                    && focusStyles.get(5).equals("none")) {
                break;
            }
        }
        assertThat(focusStyles.get(0)).isEqualTo("none");
        assertThat(focusStyles.get(2)).isEqualTo("solid");
        assertThat(Double.parseDouble(focusStyles.get(3).replace("px", ""))).isGreaterThan(0.0);
        assertThat(Double.parseDouble(focusStyles.get(4))).isZero();
        assertThat(focusStyles.get(5)).isEqualTo("none");
    }

    private void waitForShellResult(final String actionId, final String value) {
        final var result = page.locator("cw-action-results:not([hidden])").first();
        result.waitFor();
        page.waitForFunction("args => { const result = document.querySelector('cw-action-results:not([hidden])'); return result?.textContent.includes(args.action) && result.textContent.includes(args.value); }",
                java.util.Map.of("action", actionId, "value", value));
    }

    private void assertStandaloneCollectionResult(final String expectedName, final int expectedCount) {
        final var result = page.locator("cw-action-results:not([hidden]) cw-standalone-collection").first();
        result.waitFor();
        page.waitForFunction("args => { const element = document.querySelector(\"cw-action-results:not([hidden]) cw-standalone-collection\"); return element?.resultState?.status === (args.count === 0 ? 'empty' : 'ready') && element.resultState.totalCount === args.count; }",
                java.util.Map.of("count", expectedCount));
        assertThat(result.getAttribute("named")).isEqualTo(expectedName);
        assertThat(((Number) result.evaluate("element => element.resultState.totalCount")).intValue())
                .isEqualTo(expectedCount);
        assertThat(page.locator("cw-action-results:not([hidden]) > ul").count()).isZero();
        if (expectedCount > 0) {
            assertObjectLinkIcon(result.locator("cw-object-link").first());
        }
        if (nativeToolkit()) {
            assertThat(result.locator("cw-collection-grid").count()).isZero();
        } else if (expectedCount > 0
                && ((Number) result.evaluate("element => element.columns.length")).intValue() == 0) {
            page.waitForFunction("() => document.querySelector(\"cw-action-results:not([hidden]) cw-standalone-collection\")?.dataset.causewayGridPresentation === 'grid-virtual'");
            assertThat(result.locator("cw-collection-grid").count()).isEqualTo(1);
        }
    }

    private void constrainResultContentHeight(final Locator outlet) {
        outlet.evaluate("element => element.style.setProperty('--causeway-action-results-content-max-block-size', '8rem')");
    }

    @SuppressWarnings("unchecked")
    private void assertScrollableResultWithDismissBelow(final Locator outlet) {
        final var resultLayout = (List<Number>) outlet.evaluate("element => { const dismissElement = element.querySelector('[data-causeway-result-dismiss]'); const scrollable = element.querySelector('cw-standalone-collection'); const dismiss = dismissElement.getBoundingClientRect(); const result = scrollable.getBoundingClientRect(); const bounds = element.getBoundingClientRect(); const dismissFollowsResult = Boolean(scrollable.compareDocumentPosition(dismissElement) & Node.DOCUMENT_POSITION_FOLLOWING); return [dismiss.top - result.bottom, bounds.bottom - dismiss.bottom, dismiss.width, scrollable.scrollHeight - scrollable.clientHeight, dismissFollowsResult ? 1 : 0]; }");
        assertThat(resultLayout.get(0).doubleValue()).isGreaterThanOrEqualTo(-1);
        assertThat(resultLayout.get(1).doubleValue()).isGreaterThanOrEqualTo(-1);
        assertThat(resultLayout.get(2).doubleValue()).isLessThan(160);
        assertThat(resultLayout.get(3).doubleValue()).isGreaterThan(0);
        assertThat(resultLayout.get(4).intValue()).isEqualTo(1);
    }

    private long graphQLOperationCount(final String operationName) {
        return graphQLRequests.stream()
                .filter(body -> body != null && body.contains(operationName))
                .count();
    }

    private long graphQLRequestCount(final String operationName, final String value) {
        return graphQLRequests.stream()
                .filter(body -> body != null && body.contains(operationName) && body.contains(value))
                .count();
    }

    private long graphQLMutationCount(final String actionId) {
        return graphQLRequests.stream()
                .filter(body -> body != null && body.contains("mutation") && body.contains(actionId))
                .count();
    }

    private void editProperty(final String member, final String value) {
        final var property = "cw-property[id='" + member + "']";
        final var propertyLocator = page.locator(property);
        final var edit = propertyLocator.locator("[data-causeway-action='edit']");
        edit.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED));
        revealContainingTab(propertyLocator, edit);
        edit.click();
        final var editorSelector = property + " [data-causeway-editor]";
        final var editor = resolveEditor(editorSelector);
        assertFocused(editorSelector);
        fillEditor(editor, value);
        page.locator(property + " [data-causeway-action='save']").click();
    }

    private void revealContainingTab(final Locator host, final Locator control) {
        if (control.isVisible()) {
            return;
        }
        final var panel = host.locator("xpath=ancestor::*[@role='tabpanel']");
        if (panel.count() == 0) {
            throw new AssertionError("Control is not available at " + page.url()
                    + "; routeType=" + page.locator("#causeway-route cw-object-context").getAttribute("logical-type")
                    + "; host=" + host.evaluate("element => element.outerHTML")
                    + "; ancestors=" + host.evaluate("element => { const values = []; for (let current = element; current; current = current.parentElement) { const style = getComputedStyle(current); const rect = current.getBoundingClientRect(); values.push({tag: current.tagName, className: current.className, hidden: current.hidden, empty: current.dataset?.empty, display: style.display, visibility: style.visibility, width: rect.width, height: rect.height}); } return values; }"));
        }
        final var panelId = panel.getAttribute("id");
        assertThat(panelId).isNotBlank();
        page.locator("[role='tab'][aria-controls='" + panelId + "']").click();
        try {
            control.waitFor();
        } catch (final com.microsoft.playwright.TimeoutError cause) {
            throw new AssertionError("Control did not become visible at " + page.url()
                    + "; routeType=" + page.locator("#causeway-route cw-object-context").getAttribute("logical-type")
                    + "; panel=" + panelId + "; host=" + host.evaluate("element => element.outerHTML"), cause);
        }
    }

    private void waitForNoBreadcrumbs() {
        page.waitForFunction("""
                () => {
                  const breadcrumbs = document.querySelector("[data-testid='petclinic-breadcrumbs']");
                  return breadcrumbs?.hidden === true && breadcrumbs.childElementCount === 0;
                }
                """);
    }

    private void waitForBreadcrumbs(final int ancestorCount) {
        page.waitForFunction(
                "count => document.querySelector(\"[data-testid='petclinic-breadcrumbs'] [aria-current='page']\") && document.querySelectorAll(\"[data-testid='petclinic-breadcrumbs'] cw-object-link\").length === count",
                ancestorCount);
    }

    private void waitForCollectionRows(final String member, final int count) {
        try {
            page.waitForFunction("args => document.querySelector(`cw-collection[id='${args.member}']`)?.collectionState?.rows?.length === args.count",
                    java.util.Map.of("member", member, "count", count));
        } catch (final com.microsoft.playwright.TimeoutError cause) {
            final var collections = page.locator("cw-collection").evaluateAll(
                    "elements => elements.map(element => ({member: element.getAttribute('id'), state: element.collectionState?.status, presentation: element.dataset.causewayGridPresentation, rows: element.collectionState?.rows?.length, text: element.innerText}))");
            throw new AssertionError("Expected " + count + " rows for " + member + " at " + page.url()
                    + "; route=" + page.locator(ROUTE_PAGE).getAttribute("data-route-state")
                    + "; collections=" + collections, cause);
        }
    }

    private void assertCollectionHeading(
            final String member,
            final String expectedName,
            final String expectedDescription) {
        final var collection = page.locator("cw-collection[id='" + member + "']");
        final var heading = collection.locator(":scope > .causeway-collection-label");
        assertThat(heading.count()).isEqualTo(1);
        assertThat(heading.innerText()).isEqualTo(expectedName);
        assertThat(collection.locator(".causeway-collection-description").innerText()).isEqualTo(expectedDescription);
        assertThat(collection.locator(".causeway-collection").getAttribute("aria-labelledby")).isEqualTo(heading.getAttribute("id"));
        assertThat(collection.locator(".causeway-collection").getAttribute("aria-describedby")).isNotBlank();
    }

    @SuppressWarnings("unchecked")
    private void assertCompactCollectionGrid(final String member) {
        final var grid = page.locator("cw-collection[id='" + member + "'] cw-collection-grid vaadin-grid");
        grid.waitFor();
        final var geometry = (List<Number>) grid.evaluate("""
                element => {
                  const bounds = element.getBoundingClientRect();
                  const rows = [...element.shadowRoot.querySelectorAll('[part~="body-row"]')]
                    .filter(row => !row.hidden && row.getBoundingClientRect().height > 0);
                  return [element.allRowsVisible ? 1 : 0, bounds.height, rows.length];
                }
                """);
        assertThat(geometry.get(0).intValue()).isEqualTo(1);
        assertThat(geometry.get(1).doubleValue())
                .as("compact Grid height for %s: %s", member, geometry)
                .isLessThan(260.0);
        assertThat(geometry.get(2).intValue()).isGreaterThan(0);
    }

    @SuppressWarnings("unchecked")
    private void assertIntegratedCollectionActionHeader(
            final String member,
            final String firstAction,
            final String secondAction,
            final boolean wrapped) {
        final var collection = page.locator("cw-collection[id='" + member + "']");
        assertThat(collection.evaluate("element => [...element.children].filter(child => child.localName === 'cw-action').map(child => child.id).join(',')"))
                .isEqualTo(firstAction + "," + secondAction);
        final var geometry = (List<Number>) collection.evaluate("""
                element => {
                  const children = [...element.children];
                  const heading = children.find(child => child.hasAttribute('data-causeway-collection-heading'));
                  const actions = children.filter(child => child.localName === 'cw-action' && !child.hidden);
                  const primary = children.find(child => child.hasAttribute('data-causeway-member-primary'));
                  const panel = primary.querySelector('.causeway-collection');
                  const host = element.getBoundingClientRect();
                  const headingBounds = heading.getBoundingClientRect();
                  const actionBounds = actions.map(action => action.getBoundingClientRect());
                  const controlBounds = actions.map(action => action.querySelector('[data-causeway-action-control], button, vaadin-button').getBoundingClientRect());
                  return [
                    children.indexOf(heading),
                    children.indexOf(actions[0]),
                    children.indexOf(actions[1]),
                    children.indexOf(primary),
                    children.length - 1,
                    headingBounds.top,
                    headingBounds.bottom,
                    Math.min(...actionBounds.map(bounds => bounds.top)),
                    Math.max(...actionBounds.map(bounds => bounds.bottom)),
                    primary.getBoundingClientRect().top,
                    Math.max(...controlBounds.map(bounds => bounds.height)),
                    Math.max(...actionBounds.map(bounds => bounds.right)),
                    host.right,
                    panel.getAttribute('aria-labelledby') === heading.id ? 1 : 0,
                    element.querySelectorAll('#' + CSS.escape(heading.id)).length
                  ];
                }
                """);
        final var diagnostics = collection.evaluate("element => ({host: getComputedStyle(element).display, integrated: element.hasAttribute('data-causeway-collection-heading-actions'), children: [...element.children].map(child => { const control = child.querySelector?.('[data-causeway-action-control], button, vaadin-button'); return {tag: child.localName, id: child.id, heading: child.hasAttribute('data-causeway-collection-heading'), primary: child.hasAttribute('data-causeway-member-primary'), display: getComputedStyle(child).display, top: child.getBoundingClientRect().top, bottom: child.getBoundingClientRect().bottom, height: child.getBoundingClientRect().height, control: control ? {tag: control.localName, height: control.getBoundingClientRect().height, buttonHeight: getComputedStyle(control).getPropertyValue('--vaadin-button-height')} : null}; })})");
        assertThat(collection.getAttribute("data-causeway-collection-heading-actions")).isNotNull();
        assertThat(geometry.get(0).intValue()).as("heading before actions: %s", diagnostics).isLessThan(geometry.get(1).intValue());
        assertThat(geometry.get(1).intValue()).as("first action before second: %s", diagnostics).isLessThan(geometry.get(2).intValue());
        assertThat(geometry.get(2).intValue()).as("actions before body: %s", diagnostics).isLessThan(geometry.get(3).intValue());
        assertThat(geometry.get(3).intValue()).as("collection primary should be final: %s", diagnostics).isEqualTo(geometry.get(4).intValue());
        if (wrapped) {
            assertThat(geometry.get(7).doubleValue())
                    .as("actions should wrap beneath the narrow title: %s", diagnostics)
                    .isGreaterThanOrEqualTo(geometry.get(6).doubleValue() - 1.0);
        } else {
            assertThat(geometry.get(7).doubleValue())
                    .as("actions should share the wide title row: %s", diagnostics)
                    .isLessThan(geometry.get(6).doubleValue() - 1.0);
        }
        assertThat(geometry.get(9).doubleValue())
                .as("collection body should follow the complete header: %s", diagnostics)
                .isGreaterThanOrEqualTo(geometry.get(8).doubleValue() - 1.0);
        assertThat(geometry.get(10).doubleValue())
                .as("collection heading actions should use compact controls: %s", diagnostics)
                .isBetween(32.0, 40.0);
        assertThat(geometry.get(11).doubleValue())
                .as("collection heading actions should remain contained: %s", diagnostics)
                .isLessThanOrEqualTo(geometry.get(12).doubleValue() + 1.0);
        assertThat(geometry.get(13).intValue()).isEqualTo(1);
        assertThat(geometry.get(14).intValue()).isEqualTo(1);
    }

    @SuppressWarnings("unchecked")
    private void assertBelowCollectionActionTooltip(final String member, final String action) {
        final var collection = page.locator("cw-collection[id='" + member + "']");
        final var trigger = collection.locator("cw-action[id='" + action + "'] .causeway-action-control-tooltip").first();
        final var control = trigger.locator("button, vaadin-button").first();
        assertThat(trigger.count()).isEqualTo(1);
        trigger.hover();
        trigger.evaluate("element => new Promise(resolve => setTimeout(resolve, 160))");
        assertBelowCollectionActionTooltipState(trigger, "pointer");
        control.focus();
        trigger.evaluate("element => new Promise(resolve => setTimeout(resolve, 160))");
        assertBelowCollectionActionTooltipState(trigger, "keyboard");
    }

    @SuppressWarnings("unchecked")
    private void assertBelowCollectionActionTooltipState(final Locator trigger, final String activation) {
        final var state = (Map<String, Object>) trigger.evaluate("""
                element => {
                  const style = getComputedStyle(element, '::after');
                  const triggerBounds = element.getBoundingClientRect();
                  const collection = element.closest('cw-collection');
                  const hostBounds = collection.getBoundingClientRect();
                  const bodyBounds = collection.querySelector('[data-causeway-member-primary]').getBoundingClientRect();
                  const tooltipTop = triggerBounds.top + Number.parseFloat(style.top);
                  const tooltipBottom = tooltipTop + Number.parseFloat(style.height);
                  return {
                    visibility: style.visibility,
                    opacity: Number.parseFloat(style.opacity),
                    insetBlockStart: style.insetBlockStart,
                    insetBlockEnd: style.insetBlockEnd,
                    zIndex: Number.parseInt(style.zIndex, 10),
                    belowTrigger: tooltipTop >= triggerBounds.bottom - 1,
                    overlapsBody: tooltipBottom > bodyBounds.top,
                    contained: tooltipTop >= hostBounds.top && tooltipBottom <= hostBounds.bottom + 1
                  };
                }
                """);
        assertThat(state.get("visibility")).as("%s tooltip visibility", activation).isEqualTo("visible");
        assertThat(((Number) state.get("opacity")).doubleValue()).as("%s tooltip opacity", activation).isEqualTo(1.0);
        assertThat(state.get("insetBlockStart")).as("%s tooltip block-start inset", activation).isNotEqualTo("auto");
        assertThat(((Number) state.get("zIndex")).intValue()).isGreaterThan(0);
        assertThat(state.get("belowTrigger")).isEqualTo(true);
        assertThat(state.get("overlapsBody")).isEqualTo(true);
        assertThat(state.get("contained")).isEqualTo(true);
    }

    private void assertCollectionPresentation(final String member, final String expected) {
        final var collection = page.locator("cw-collection[id='" + member + "']");
        if (nativeToolkit() || !"grid".equals(expected)) {
            assertThat(collection.getAttribute("data-causeway-grid-presentation")).isEqualTo("native");
            assertThat(collection.locator("cw-collection-grid").count()).isZero();
            if (!nativeToolkit()) {
                assertThat(collection.getAttribute("data-causeway-grid-fallback")).isEqualTo(expected);
            }
            return;
        }
        try {
            page.waitForFunction("member => { const collection = document.querySelector(`cw-collection[id='${member}']`); return collection?.dataset?.causewayGridPresentation?.startsWith('grid-') && collection.querySelector('cw-collection-grid')?.dataset.widgetState === 'ready'; }", member);
        } catch (final com.microsoft.playwright.TimeoutError cause) {
            final var diagnostics = collection.evaluate("element => ({dataset: {...element.dataset}, width: element.getBoundingClientRect().width, state: element.collectionState?.status, window: element.collectionState?.window, columns: element.columns})");
            throw new AssertionError("Collection " + member + " did not qualify for Grid: " + diagnostics, cause);
        }
        assertThat(collection.locator("cw-collection-grid").count()).isEqualTo(1);
        assertThat(collection.locator("cw-collection-grid").getAttribute("data-widget-state")).isEqualTo("ready");
        assertThat(toolkitRequests.stream().anyMatch(request -> request.contains("/vaadin-grid/vaadin-grid.js"))).isTrue();
    }

    private void waitForObjectTitle(final String value) {
        page.waitForFunction("value => document.querySelector('cw-object-header h1')?.textContent.includes(value)", value);
    }

    private void waitForPropertyValue(final String member, final String value) {
        final var selector = "cw-property[id='" + member + "'] .causeway-property-value";
        try {
            page.waitForFunction("args => { const output = document.querySelector(args.selector); const control = output?.querySelector('cw-field-editor')?.firstElementChild; return output?.textContent.includes(args.value) || String(control?.value ?? '') === args.value; }",
                    java.util.Map.of("selector", selector, "value", value));
        } catch (final com.microsoft.playwright.TimeoutError cause) {
            final var property = page.locator("cw-property[id='" + member + "']");
            final var context = page.locator("#causeway-route cw-object-context");
            throw new AssertionError("Expected property " + member + " to contain '" + value + "' at " + page.url()
                    + "; route=" + page.locator(ROUTE_PAGE).getAttribute("data-route-state")
                    + "; context=" + context.getAttribute("data-context-state")
                    + "; property=" + property.evaluate("element => element.outerHTML")
                    + "; snapshot=" + context.evaluate("element => element.context?.currentState?.snapshot?.data ?? element.context?.state?.snapshot?.data ?? null"), cause);
        }
    }

    private Locator resolveEditor(final String selector) {
        page.locator(selector).first().waitFor();
        page.waitForFunction("selector => { const element = document.querySelector(selector); return element && element.localName !== 'cw-field-editor'; }", selector);
        return page.locator(selector);
    }

    private void fillEditor(final Locator editor, final String value) {
        if (((String) editor.evaluate("element => element.localName")).startsWith("vaadin-")) {
            editor.evaluate("(element, value) => { element.value = value; element.dispatchEvent(new Event('input', {bubbles: true, composed: true})); element.dispatchEvent(new Event('change', {bubbles: true, composed: true})); }", value);
        } else {
            editor.fill(value);
        }
    }

    private void commitEditor(final Locator editor) {
        editor.evaluate("element => element.dispatchEvent(new FocusEvent('focusout', {bubbles: true, composed: true}))");
    }

    private void assertFocused(final String selector) {
        try {
            page.waitForFunction("selector => [...document.querySelectorAll(selector)].some(element => element === document.activeElement || element.contains(document.activeElement) || element.shadowRoot?.activeElement)", selector);
        } catch (RuntimeException ex) {
            final var activeElement = page.evaluate("""
                    () => {
                      const active = document.activeElement;
                      return active ? `${active.localName}${active.id ? `#${active.id}` : ''}${active.className ? `.${String(active.className).replaceAll(' ', '.')}` : ''}` : '<none>';
                    }
                    """);
            throw new AssertionError("Expected focus within " + selector + " but active element was " + activeElement, ex);
        }
        assertThat(page.locator(selector).count()).isGreaterThan(0);
    }

    @SuppressWarnings("unchecked")
    private static boolean nativeToolkit() {
        return "native".equalsIgnoreCase(System.getProperty(
                "causeway.viewer.webcomponents.htmx.component-toolkit",
                System.getProperty("causeway.viewer.webcomponents.htmx.editor-toolkit", "vaadin")));
    }

    private void assertNoBrowserFailures() {
        if (page != null && !page.isClosed()) {
            final var recorded = (List<Object>) page.evaluate("() => globalThis.__causewayPlaywrightFailures ?? []");
            recorded.forEach(failure -> browserFailures.add(String.valueOf(failure)));
        }
        assertThat(browserFailures).isEmpty();
    }

    private String parameter(final String parameterId) {
        return "[data-testid='action-prompt-parameter-" + parameterId + "']";
    }

    private String url(final String path) {
        return "http://localhost:" + port + path;
    }

    private String humanize(final String value) {
        return value.replaceAll("([a-z0-9])([A-Z])", "$1 $2")
                .replaceFirst("^.", value.substring(0, 1).toUpperCase());
    }
}
