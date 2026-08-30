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

import org.apache.causeway.viewer.webcomponents.sample.htmx.petclinic.domain.PetOwnerRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = PetClinicHtmxApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PetClinicHtmxPlaywrightTest {

    private static final String ROUTE_PAGE = "[data-testid='causeway-route-page']";
    private static final String PROMPT = "dialog[data-testid='action-prompt']";

    @LocalServerPort
    private int port;

    @Autowired
    private PetOwnerRepository ownerRepository;

    private final List<String> browserFailures = new ArrayList<>();
    private final List<String> graphQLRequests = new ArrayList<>();
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
        toolkitRequests.clear();
        browserContext = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1440, 900)
                .setColorScheme(ColorScheme.LIGHT));
        browserContext.addInitScript("""
                (() => {
                  globalThis.__causewayPlaywrightFailures = [];
                  const record = message => globalThis.__causewayPlaywrightFailures.push(String(message));
                  globalThis.addEventListener('error', event => record(`page error: ${event.message}`));
                  globalThis.addEventListener('unhandledrejection', event => record(`unhandled rejection: ${event.reason}`));
                  const originalFetch = globalThis.fetch.bind(globalThis);
                  globalThis.fetch = async (...args) => {
                    const response = await originalFetch(...args);
                    const url = String(response.url || args[0]);
                    if (!response.ok) {
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
            if ("error".equals(message.type())) {
                browserFailures.add("console: " + message.text());
            }
        });
        page.onPageError(error -> browserFailures.add("page: " + error));
        page.onRequest(request -> {
            if (request.url().contains("/graphql") && "POST".equals(request.method())) {
                graphQLRequests.add(request.postData());
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
            if (!(request.url().contains("/graphql") && failure != null && failure.contains("ERR_ABORTED"))) {
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
        assertThat(page.locator(ROUTE_PAGE).getAttribute("data-page-kind")).isEqualTo("custom");
        assertThat(page.locator(ROUTE_PAGE).getAttribute("data-page-source")).isEqualTo("resource");
        assertThat(page.locator("[data-testid='petclinic-custom-home']").isVisible()).isTrue();
        assertFocused(ROUTE_PAGE);
        waitForCollectionRows("petOwners", 2);
        waitForCollectionRows("futureVisits", 3);
        assertThat(page.locator("cw-collection[id='petOwners']").getAttribute("paged")).isEqualTo("2");
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
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='futureVisits']\")?.collectionState?.window?.totalCount === 1");
        waitForCollectionRows("futureVisits", 1);
        assertThat(page.locator("cw-collection[id='futureVisits']").innerText()).contains("Vaccination");
        page.locator("cw-collection[id='futureVisits'] [data-causeway-collection-search-clear]").click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='futureVisits']\")?.collectionState?.window?.totalCount === 3");
        waitForCollectionRows("futureVisits", 3);

        final var ownerSearch = page.locator("cw-collection[id='petOwners'] [data-causeway-collection-search]");
        assertThat(ownerSearch.isVisible()).isTrue();
        assertThat(ownerSearch.getAttribute("maxlength")).isEqualTo("256");
        ownerSearch.fill("Mary");
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='petOwners']\")?.collectionState?.window?.totalCount === 1");
        waitForCollectionRows("petOwners", 1);
        assertThat(page.locator("cw-collection[id='petOwners']").innerText()).contains("Mary Smith");
        page.locator("cw-collection[id='petOwners'] [data-causeway-collection-search-clear]").click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='petOwners']\")?.collectionState?.window?.totalCount === 4");
        waitForCollectionRows("petOwners", 2);
        page.locator("cw-collection[id='petOwners'] [data-causeway-collection-sort='name']").click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='petOwners']\")?.collectionState?.window?.ordering === 'REQUESTED'");
        assertThat(page.locator("cw-collection[id='petOwners'] [data-causeway-collection-sort='name']").getAttribute("aria-label"))
                .contains("descending");

        page.locator("cw-collection[id='petOwners'] [data-causeway-grid-next]").click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='petOwners']\")?.collectionState?.window?.offset === 2");
        waitForCollectionRows("petOwners", 2);
        assertThat(page.locator("cw-collection[id='petOwners'] [data-causeway-grid-previous]").isVisible()).isTrue();
        clickObjectLink("Mary Smith");
        waitForRoute("petclinic.PetOwner", "s_owner-mary");
        assertThat(page.locator(ROUTE_PAGE).getAttribute("data-page-kind")).isEqualTo("custom");
        assertThat(page.locator(ROUTE_PAGE).getAttribute("data-page-source")).isEqualTo("resource");
        assertThat(page.locator("[data-testid='petclinic-owner-page']").isVisible()).isTrue();
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
        waitForCollectionRows("visits", 1);
        assertThat(page.locator("cw-collection[id='pets']").getAttribute("sortable")).isEmpty();
        assertThat(page.locator("cw-collection[id='pets']").getAttribute("filterable")).isEmpty();
        final var petSearch = page.locator("cw-collection[id='pets'] [data-causeway-collection-search]");
        assertThat(petSearch.isVisible()).isTrue();
        petSearch.fill("cat");
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='pets']\")?.collectionState?.window?.totalCount === 1");
        waitForCollectionRows("pets", 1);
        assertThat(page.locator("cw-collection[id='pets']").innerText()).contains("Samantha");
        page.locator("cw-collection[id='pets'] [data-causeway-collection-search-clear]").click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='pets']\")?.collectionState?.window?.totalCount === 2");
        waitForCollectionRows("pets", 2);
        page.locator("cw-collection[id='pets'] [data-causeway-collection-sort='name']").click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='pets']\")?.collectionState?.window?.ordering === 'REQUESTED'");
        assertThat(page.locator("cw-collection[id='pets'] cw-action[id='addPet']").isVisible()).isTrue();
        assertThat(page.locator("cw-collection[id='visits']").getAttribute("paged")).isEqualTo("1");
        assertThat(page.locator("cw-collection[id='visits'] [data-causeway-grid-next]").isVisible()).isTrue();
        page.locator("cw-collection[id='visits'] [data-causeway-grid-next]").click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='visits']\")?.collectionState?.window?.offset === 1");
        waitForCollectionRows("visits", 1);
        assertThat(page.locator("cw-collection[id='visits'] [data-causeway-grid-previous]").isVisible()).isTrue();
        page.locator("cw-collection[id='visits'] [data-causeway-grid-previous]").click();
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='visits']\")?.collectionState?.window?.offset === 0");
        waitForCollectionRows("visits", 1);
        assertCollectionHeading("pets", "Companion animals", "Pets currently registered to this owner.");
        assertCollectionHeading("visits", "Visit history", "All visits recorded for this owner's pets.");
        final var visitDisabledReason = page.locator("cw-collection[id='visits'] .causeway-visually-hidden");
        assertThat(visitDisabledReason.textContent()).contains("Cannot edit a mixed-in collection.");
        assertThat(visitDisabledReason.getAttribute("class")).contains("causeway-visually-hidden");
        assertThat(page.locator("cw-collection [title*='Cannot edit']").count()).isZero();
        assertCollectionPresentation("pets", "grid");
        assertCollectionPresentation("visits", "ordering-not-deterministic");
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
        page.setViewportSize(700, 900);
        page.waitForFunction("() => [...document.querySelectorAll(\"cw-collection[id='pets'], cw-collection[id='visits']\")].every(element => element.dataset.causewayGridResponsive === 'narrow' && !element.querySelector('cw-collection-grid'))");
        waitForCollectionRows("pets", 2);
        waitForCollectionRows("visits", 1);
        assertThat(graphQLRequests.size()).isEqualTo(readsBeforeResponsiveSwitch);
        page.setViewportSize(1800, 900);
        if (!nativeToolkit()) {
            page.waitForFunction("() => document.querySelector(\"cw-collection[id='pets']\")?.dataset.causewayGridPresentation.startsWith('grid-') && document.querySelector(\"cw-collection[id='visits']\")?.dataset.causewayGridFallback === 'ordering-not-deterministic'");
        }
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
        assertThat(page.locator("[data-testid='petclinic-breadcrumbs'] cw-object-link").getAttribute("title"))
                .isEqualTo("Mary Smith (Mary)");
        assertThat(page.locator("[data-testid='petclinic-breadcrumbs'] [aria-current='page']").textContent())
                .isEqualTo("Basil · dog");
        waitForMenus();

        page.navigate(url("/htmx/object/petclinic.Visit/s_visit-basil-checkup"),
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        waitForRoute("petclinic.Visit", "s_visit-basil-checkup");
        assertThat(page.locator("[data-testid='petclinic-visit-page']").isVisible()).isTrue();
        waitForBreadcrumbs(2);
        assertThat(page.locator("[data-testid='petclinic-breadcrumbs'] cw-object-link")
                .evaluateAll("elements => elements.map(element => element.getAttribute('title')).join(',')"))
                .isEqualTo("Mary Smith (Mary),Basil · dog");
        page.locator("[data-testid='petclinic-breadcrumbs'] cw-object-link[title='Basil · dog'] button").click();
        waitForRoute("petclinic.Pet", "s_pet-basil");
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

        activateServiceAction("listAll");
        waitForShellResult("listAll", "4 results");
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
        waitForShellResult("findByName", "1 results");
        assertMenuClosedAndFocused("Pet Owners");

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
        waitForShellResult("findByNameLike", "1 results");
        assertMenuClosedAndFocused("Pet Owners");

        openMenu("Pet Owners");
        activateServiceAction("count");
        waitForShellResult("count", "4");
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
        waitForShellResult("listUpcoming", "3 results");
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
            lastVisit.locator("[data-causeway-action='edit']").click();
            final var lastVisitEditorSelector = "cw-property[id='lastVisit'] [data-causeway-editor='lastVisit']";
            final var lastVisitEditor = resolveEditor(lastVisitEditorSelector);
            assertThat(lastVisitEditor.evaluate("element => element.inputElement?.value")).isEqualTo("12/08/2026");
            assertThat(lastVisitEditor.evaluate("element => element.value")).isEqualTo("2026-08-12");
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
            lastVisit.locator("[data-causeway-action='cancel']").click();
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
        assertFocused("cw-action[id='updateName'] [data-causeway-action-control]");

        updateName.click();
        waitForPrompt("updateName", "Change the owner's name");
        resolveEditor(parameter("name")).press("Escape");
        page.locator(PROMPT).waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED));
        assertFocused("cw-action[id='updateName'] [data-causeway-action-control]");
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
        final var visitAt = resolveEditor(parameter("visitAt"));
        final var reason = resolveEditor(parameter("reason"));
        assertThat(page.locator(PROMPT + " [data-parameter='reason'] .causeway-action-parameter-label").textContent())
                .isEqualTo("Reason for visit");
        assertThat(page.locator(PROMPT + " [data-parameter='reason'] .causeway-action-parameter-description").textContent())
                .isEqualTo("Describe the purpose of the appointment.");
        assertThat(reason.evaluate("element => element.localName === 'vaadin-text-area' ? element.maxRows : Number(element.rows)"))
                .isEqualTo(3);
        assertThat(visitAt.count())
                .as(page.locator(PROMPT).evaluate("element => element.outerHTML").toString())
                .isEqualTo(1);
        assertThat(String.valueOf(visitAt.evaluate("element => element.value"))).isNotBlank();
        assertThat(reason.evaluate("element => element.value")).isEqualTo("Routine check-up");
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
        assertThat(ownerRepository.findById("owner-5")).isNotNull();
        assertThat(graphQLMutationCount("delete") - deleteMutations).isZero();
        assertFocused("cw-action[id='delete'] [data-causeway-action-control]");

        objectAction("delete").click();
        confirmation.waitFor();
        page.locator("[data-testid='action-confirmation-confirm']").click();
        page.waitForFunction("() => location.pathname.includes('/object/petclinic.HomePage/')");
        waitForLogicalType("petclinic.HomePage");
        assertThat(page.locator("[data-testid='petclinic-custom-home']").isVisible()).isTrue();
        assertThat(ownerRepository.findById("owner-5")).isNull();
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
            serviceAction(actionId).click();
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

    private void waitForShellResult(final String actionId, final String value) {
        final var result = page.locator("[data-testid='causeway-shell-result']");
        result.waitFor();
        page.waitForFunction("args => { const result = document.querySelector('[data-testid=\"causeway-shell-result\"]'); return !result?.hidden && result.textContent.includes(args.action) && result.textContent.includes(args.value); }",
                java.util.Map.of("action", actionId, "value", value));
    }

    private long graphQLOperationCount(final String operationName) {
        return graphQLRequests.stream()
                .filter(body -> body != null && body.contains(operationName))
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
        assertThat(collection.locator(".causeway-collection-label").innerText()).isEqualTo(expectedName);
        assertThat(collection.locator(".causeway-collection-description").innerText()).isEqualTo(expectedDescription);
        assertThat(collection.locator(".causeway-collection").getAttribute("aria-labelledby")).isNotBlank();
        assertThat(collection.locator(".causeway-collection").getAttribute("aria-describedby")).isNotBlank();
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
