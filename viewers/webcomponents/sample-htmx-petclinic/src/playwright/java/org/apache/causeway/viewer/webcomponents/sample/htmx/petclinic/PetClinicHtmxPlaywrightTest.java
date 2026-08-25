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
                    || request.url().contains("/causeway-webcomponents/vaadin-fields/")) {
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

        assertThat(page.locator("html").getAttribute("data-causeway-editor-toolkit"))
                .isEqualTo(nativeToolkit() ? "native" : "vaadin");
        assertThat(toolkitRequests).isEmpty();
        assertThat(page.locator(ROUTE_PAGE).getAttribute("data-page-kind")).isEqualTo("custom");
        assertThat(page.locator(ROUTE_PAGE).getAttribute("data-page-source")).isEqualTo("resource");
        assertThat(page.locator("[data-testid='petclinic-custom-home']").isVisible()).isTrue();
        assertFocused(ROUTE_PAGE);
        waitForCollectionRows("petOwners", 4);
        waitForCollectionRows("futureVisits", 3);

        clickObjectLink("Mary Smith");
        waitForRoute("petclinic.PetOwner", "s_owner-mary");
        assertThat(page.locator(ROUTE_PAGE).getAttribute("data-page-kind")).isEqualTo("custom");
        assertThat(page.locator(ROUTE_PAGE).getAttribute("data-page-source")).isEqualTo("resource");
        assertThat(page.locator("[data-testid='petclinic-owner-page']").isVisible()).isTrue();
        assertFocused(ROUTE_PAGE);
        waitForCollectionRows("pets", 2);
        waitForCollectionRows("visits", 2);
        assertThat(page.locator(".petclinic-page-toolbar causeway-action[member='delete']").count()).isEqualTo(1);
        assertThat(page.locator("causeway-property[member='name'] > causeway-action[member='updateName']").count())
                .isEqualTo(1);
        assertThat(page.locator("causeway-collection[member='pets'] > causeway-action[member='addPet']").count())
                .isEqualTo(1);
        assertThat(page.locator("causeway-collection[member='pets'] > causeway-action[member='removePet']").count())
                .isEqualTo(1);
        assertThat(page.locator("causeway-collection[member='visits'] > causeway-action[member='bookVisit']").count())
                .isEqualTo(1);
        assertThat(page.locator("causeway-collection[member='pets']")
                .evaluate("element => [...element.children].filter(child => child.localName === 'causeway-action').map(child => child.getAttribute('member')).join(',')"))
                .isEqualTo("addPet,removePet");
        assertThat(page.locator("causeway-property[member='name']")
                .evaluate("element => getComputedStyle(element).gap")).isNotEqualTo("0px");

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
        waitForMenus();

        page.navigate(url("/htmx/object/petclinic.Visit/s_visit-basil-checkup"),
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        waitForRoute("petclinic.Visit", "s_visit-basil-checkup");
        assertThat(page.locator("[data-testid='petclinic-visit-page']").isVisible()).isTrue();
        waitForMenus();

        page.setViewportSize(390, 844);
        final var horizontalOverflow = (Number) page.evaluate(
                "() => document.documentElement.scrollWidth - document.documentElement.clientWidth");
        final var overflowingElements = page.evaluate("() => [...document.querySelectorAll('body *')].map(element => { const rect = element.getBoundingClientRect(); return {tag: element.tagName, className: String(element.className || ''), member: element.getAttribute('member'), left: rect.left, right: rect.right, width: rect.width}; }).filter(value => value.right > document.documentElement.clientWidth + 0.5 || value.left < -0.5).slice(0, 20)");
        assertThat(horizontalOverflow)
                .as("overflowing elements: %s", overflowingElements)
                .isEqualTo(0);
        assertThat(page.locator(".causeway-menubar-bar-disclosure").count()).isGreaterThan(0);

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
        menuDisclosure("Pet Owners").press("Escape");
        assertMenuClosedAndFocused("Pet Owners");
        assertThat(page.locator(PROMPT).count()).isZero();

        openMenu("Pet Owners");
        page.locator(ROUTE_PAGE).dispatchEvent("click");
        assertMenuClosed("Pet Owners");
        openMenu("Pet Owners");
        assertThat(petclinicServiceActionIds()).containsExactlyInAnyOrder(
                "create", "findByName", "findByNameLike", "listAll", "count", "listUpcoming");

        final var listAll = serviceAction("listAll");
        listAll.click();
        waitForShellResult("listAll", "4 results");
        assertMenuClosedAndFocused("Pet Owners");

        openMenu("Pet Owners");
        final var findByName = serviceAction("findByName");
        findByName.click();
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
        final var findByNameLike = serviceAction("findByNameLike");
        findByNameLike.click();
        waitForPrompt("findByNameLike");
        fillParameter("name", "James");
        cancelPrompt();
        assertMenuClosedAndFocused("Pet Owners");
        openMenu("Pet Owners");
        serviceAction("findByNameLike").click();
        waitForPrompt("findByNameLike");
        fillParameter("name", "James");
        submitPrompt();
        waitForShellResult("findByNameLike", "1 results");
        assertMenuClosedAndFocused("Pet Owners");

        openMenu("Pet Owners");
        serviceAction("count").click();
        waitForShellResult("count", "4");
        assertMenuClosedAndFocused("Pet Owners");

        openMenu("Visits");
        serviceAction("listUpcoming").click();
        waitForShellResult("listUpcoming", "3 results");
        assertMenuClosedAndFocused("Visits");
    }

    @Test
    @Order(3)
    void propertyEditingAndPromptFocusRemainDeterministic() {
        openObject("petclinic.PetOwner", "s_owner-mary");

        final var disabledName = page.locator("causeway-property[member='name'] .causeway-property-disabled-indicator");
        disabledName.waitFor();
        assertThat(disabledName.getAttribute("data-tooltip")).isNotBlank();
        assertThat(disabledName.getAttribute("tabindex")).isEqualTo("0");

        final var notes = page.locator("causeway-property[member='notes']");
        final var notesEdit = notes.locator("[data-causeway-action='edit']");
        revealContainingTab(notes, notesEdit);
        assertThat(notes.locator(".causeway-property-label").getAttribute("title"))
                .isEqualTo("Additional notes about this pet owner.");
        notesEdit.click();
        final var notesEditor = resolveEditor("causeway-property[member='notes'] [data-causeway-editor='notes']");
        assertThat(notesEditor.evaluate("element => element.localName === 'vaadin-text-area' ? element.maxRows : Number(element.rows)"))
                .isEqualTo(5);
        fillEditor(notesEditor, "First line\nSecond line");
        notes.locator("[data-causeway-action='cancel']").click();

        editProperty("telephoneNumber", "020 7000 1234");
        waitForPropertyValue("telephoneNumber", "020 7000 1234");
        assertFocused("causeway-property[member='telephoneNumber'] [data-causeway-action='edit']");
        editProperty("telephoneNumber", "020 7946 0312");
        waitForPropertyValue("telephoneNumber", "020 7946 0312");

        final var updateName = objectAction("updateName");
        updateName.click();
        waitForPrompt("updateName");
        assertFocused(parameter("name"));
        fillParameter("name", "Invalid % name");
        submitPrompt();
        waitForPromptError("cannot contain");
        assertFocused(parameter("name"));
        cancelPrompt();
        assertFocused("causeway-action[member='updateName'] button");

        updateName.click();
        waitForPrompt("updateName");
        resolveEditor(parameter("name")).press("Escape");
        page.locator(PROMPT).waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED));
        assertFocused("causeway-action[member='updateName'] button");
    }

    @Test
    @Order(4)
    void disposableOwnerExercisesCreateAndEveryObjectAction() {
        openHome();
        openMenu("Pet Owners");
        serviceAction("create").click();
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
        waitForPrompt("updateName");
        fillParameter("name", "Playwright Owner Updated");
        submitPromptExpectingNavigation();
        waitForObjectTitle("Playwright Owner Updated");
        assertThat(graphQLMutationCount("updateName") - updateNameMutations).isEqualTo(1);
        assertThat(page.url()).isEqualTo(ownerPath);
        assertFocused(ROUTE_PAGE);

        final var addPetMutations = graphQLMutationCount("addPet");
        objectAction("addPet").click();
        waitForPrompt("addPet");
        fillParameter("name", "Turing");
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

        objectAction("delete").click();
        page.locator("[data-testid='causeway-shell-result']").waitFor();
        assertThat(page.locator("[data-testid='causeway-shell-result']").textContent()).contains("Completed");
        assertThat(ownerRepository.findById("owner-5")).isNull();
        try {
            page.waitForFunction("() => ['not-found', 'terminal-error'].includes(document.querySelector('[data-testid=\"causeway-route-page\"]')?.dataset.routeState)");
        } catch (final com.microsoft.playwright.TimeoutError cause) {
            throw new AssertionError("Deleted object remained present; route="
                    + page.locator(ROUTE_PAGE).getAttribute("data-route-state")
                    + "; context=" + page.locator("#causeway-route causeway-object-context")
                            .evaluate("element => element.context?.currentState ?? element.context?.state ?? null"), cause);
        }
    }

    private void openHome() {
        page.navigate(url("/htmx"), new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        waitForPageKind("custom");
        waitForMenus();
    }

    private void waitForMenus() {
        page.waitForFunction("() => ['ready', 'partial-error'].includes(document.querySelector('causeway-menubars')?.dataset.menuState)");
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
        page.waitForFunction("logicalType => { const context = document.querySelector('#causeway-route causeway-object-context'); const page = document.querySelector('[data-testid=\"causeway-route-page\"]'); return context?.getAttribute('logical-type') === logicalType && ['ready', 'partial-error'].includes(page?.dataset.routeState); }", logicalTypeName);
    }

    private void waitForRouteUrl(final String pathOrUrl) {
        final var expectedPath = pathOrUrl.startsWith("http") ? java.net.URI.create(pathOrUrl).getPath() : pathOrUrl;
        page.waitForFunction("path => location.pathname === path && ['ready', 'partial-error'].includes(document.querySelector('[data-testid=\"causeway-route-page\"]')?.dataset.routeState)", expectedPath);
    }

    private void clickObjectLink(final String titlePart) {
        clickObjectLink(page.locator("causeway-object-link button"), titlePart);
    }

    private void clickObjectLinkInCollection(final String member, final String titlePart) {
        clickObjectLink(page.locator("causeway-collection[member='" + member + "'] causeway-object-link button"), titlePart);
    }

    private void clickObjectLink(final Locator links, final String titlePart) {
        final var link = links.filter(new Locator.FilterOptions().setHasText(titlePart)).first();
        link.waitFor();
        link.click();
    }

    private void openMenu(final String name) {
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
        final var disclosure = menuDisclosure(name);
        page.waitForFunction("id => { const disclosure = document.querySelector(`[data-causeway-menu-disclosure][aria-controls='${id}']`); const panel = document.getElementById(id); return disclosure?.getAttribute('aria-expanded') === 'false' && panel?.hidden === true; }",
                disclosure.getAttribute("aria-controls"));
    }

    private void assertMenuClosedAndFocused(final String name) {
        final var disclosure = menuDisclosure(name);
        final var panelId = disclosure.getAttribute("aria-controls");
        assertMenuClosed(name);
        assertFocused("[data-causeway-menu-disclosure][aria-controls='" + panelId + "']");
    }

    private Locator serviceAction(final String actionId) {
        return page.locator("[data-service-logical-type^='petclinic.'][data-action-id='" + actionId + "']").first();
    }

    private Set<String> petclinicServiceActionIds() {
        final var actionIds = new LinkedHashSet<String>();
        for (final var action : page.locator("[data-service-logical-type^='petclinic.'][data-action-id]").all()) {
            actionIds.add(action.getAttribute("data-action-id"));
        }
        return actionIds;
    }

    private Locator objectAction(final String member) {
        final var host = page.locator("causeway-action[member='" + member + "']").first();
        final var action = host.locator("button");
        action.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED));
        revealContainingTab(host, action);
        return action;
    }

    private void waitForPrompt(final String actionId) {
        page.locator(PROMPT).waitFor();
        assertThat(page.locator(PROMPT + " h2").textContent()).isEqualTo(humanize(actionId));
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
        if ("causeway-reference-editor".equals(control.evaluate("element => element.localName"))) {
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

    private void waitForShellResult(final String actionId, final String value) {
        final var result = page.locator("[data-testid='causeway-shell-result']");
        result.waitFor();
        page.waitForFunction("args => { const result = document.querySelector('[data-testid=\"causeway-shell-result\"]'); return !result?.hidden && result.textContent.includes(args.action) && result.textContent.includes(args.value); }",
                java.util.Map.of("action", actionId, "value", value));
    }

    private long graphQLMutationCount(final String actionId) {
        return graphQLRequests.stream()
                .filter(body -> body != null && body.contains("mutation") && body.contains(actionId))
                .count();
    }

    private void editProperty(final String member, final String value) {
        final var property = "causeway-property[member='" + member + "']";
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
                    + "; routeType=" + page.locator("#causeway-route causeway-object-context").getAttribute("logical-type")
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
                    + "; routeType=" + page.locator("#causeway-route causeway-object-context").getAttribute("logical-type")
                    + "; panel=" + panelId + "; host=" + host.evaluate("element => element.outerHTML"), cause);
        }
    }

    private void waitForCollectionRows(final String member, final int count) {
        try {
            page.waitForFunction("args => document.querySelectorAll(`causeway-collection[member='${args.member}'] tbody tr, causeway-collection[member='${args.member}'] .causeway-collection-rows > li`).length === args.count",
                    java.util.Map.of("member", member, "count", count));
        } catch (final com.microsoft.playwright.TimeoutError cause) {
            final var collections = page.locator("causeway-collection").evaluateAll(
                    "elements => elements.map(element => ({member: element.getAttribute('member'), state: element.dataset.state, rows: element.querySelectorAll('tbody tr, .causeway-collection-rows > li').length, text: element.innerText}))");
            throw new AssertionError("Expected " + count + " rows for " + member + " at " + page.url()
                    + "; route=" + page.locator(ROUTE_PAGE).getAttribute("data-route-state")
                    + "; collections=" + collections, cause);
        }
    }

    private void waitForObjectTitle(final String value) {
        page.waitForFunction("value => document.querySelector('causeway-object-header h1')?.textContent.includes(value)", value);
    }

    private void waitForPropertyValue(final String member, final String value) {
        final var selector = "causeway-property[member='" + member + "'] .causeway-property-value";
        try {
            page.waitForFunction("args => document.querySelector(args.selector)?.textContent.includes(args.value)",
                    java.util.Map.of("selector", selector, "value", value));
        } catch (final com.microsoft.playwright.TimeoutError cause) {
            final var property = page.locator("causeway-property[member='" + member + "']");
            final var context = page.locator("#causeway-route causeway-object-context");
            throw new AssertionError("Expected property " + member + " to contain '" + value + "' at " + page.url()
                    + "; route=" + page.locator(ROUTE_PAGE).getAttribute("data-route-state")
                    + "; context=" + context.getAttribute("data-context-state")
                    + "; property=" + property.evaluate("element => element.outerHTML")
                    + "; snapshot=" + context.evaluate("element => element.context?.currentState?.snapshot?.data ?? element.context?.state?.snapshot?.data ?? null"), cause);
        }
    }

    private Locator resolveEditor(final String selector) {
        page.locator(selector).first().waitFor();
        page.waitForFunction("selector => { const element = document.querySelector(selector); return element && element.localName !== 'causeway-field-editor'; }", selector);
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
        page.waitForFunction("selector => [...document.querySelectorAll(selector)].some(element => element === document.activeElement || element.contains(document.activeElement) || element.shadowRoot?.activeElement)", selector);
        assertThat(page.locator(selector).count()).isGreaterThan(0);
    }

    @SuppressWarnings("unchecked")
    private static boolean nativeToolkit() {
        return "native".equalsIgnoreCase(System.getProperty(
                "causeway.viewer.webcomponents.htmx.editor-toolkit", "vaadin"));
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
