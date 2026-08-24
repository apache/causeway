/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */
package org.apache.causeway.regressiontests.referenceapp.htmx;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.ColorScheme;
import com.microsoft.playwright.options.ForcedColors;
import com.microsoft.playwright.options.ReducedMotion;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = ReferenceAppHtmxApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("demo-jpa")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReferenceAppHtmxPlaywrightTest {

    private static final String ROUTE_PAGE = "[data-testid='causeway-route-page']";
    private static final String PROMPT = "dialog[data-testid='action-prompt']";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @LocalServerPort
    private int port;

    private final List<String> browserFailures = new ArrayList<>();
    private Playwright playwright;
    private Browser browser;
    private BrowserContext browserContext;
    private Page page;

    @BeforeAll
    void startBrowser() {
        playwright = Playwright.create();
        final var options = new BrowserType.LaunchOptions()
                .setHeadless(Boolean.parseBoolean(System.getProperty("playwright.headless", "true")));
        final String executable = System.getProperty("playwright.chromium.executable", "").trim();
        final String channel = System.getProperty("playwright.chromium.channel", "").trim();
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

    @BeforeEach
    void openBrowserContext() {
        openBrowserContext(new Browser.NewContextOptions()
                .setViewportSize(1440, 900)
                .setColorScheme(ColorScheme.LIGHT));
    }

    @AfterEach
    void closeBrowserContext() {
        assertNoBrowserFailures();
        if (browserContext != null) {
            browserContext.close();
        }
    }

    @Test
    void menusChoicesAutocompleteCancellationAndRouteDisposal() {
        openShell();
        assertThat(candidateRequests()).isZero();

        openMenu("Prog Model");
        serviceAction("demo.ActionChoicesMenu", "choices").click();
        waitForLogicalType("demo.ActionChoices");
        page.waitForFunction("() => ['ready', 'fallback', 'partial-error'].includes(document.querySelector('#causeway-route causeway-object')?.dataset.layoutState)");
        assertThat(page.locator(PROMPT).count()).isZero();

        final Locator invokingControl = objectAction("selectTvCharacter");
        invokingControl.click();
        waitForPrompt("selectTvCharacter");
        selectFirstAvailableChoice("tvCharacter");
        submitPrompt();
        page.locator(PROMPT).waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED));
        waitForLogicalType("demo.ActionChoices");
        assertThat(candidateRequests()).isZero();

        openObject("demo.ActionAutoCompletePage", invokeViewModel(
                "demo_ActionAutoCompleteMenu", "autoComplete", "rich__demo_ActionAutoCompletePage"));
        objectAction("selectTvCharacter").click();
        waitForPrompt("selectTvCharacter");
        selectFirstAutocompleteChoice("tvCharacter", "Tom");
        submitPrompt();
        page.locator(PROMPT).waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED));
        waitForLogicalType("demo.ActionAutoCompletePage");

        openShell();
        assertThat(openCandidateOverlays()).isZero();
        assertThat(candidateRequests()).isZero();
        assertSemanticAccessibility();
    }

    @Test
    void valuesCollectionsHistoryAndWicketComparisonShareTheFixture() {
        final String decimalPageId = invokeViewModel(
                "demo_JavaMathTypesMenu", "bigDecimals", "rich__demo_BigDecimals");
        openObject("demo.BigDecimals", decimalPageId);
        final String decimalEntityId = firstCollectionEntityId(
                "demo_BigDecimals", decimalPageId, "rich__demo_BigDecimalEntity");
        openObject("demo.BigDecimalEntity", decimalEntityId);

        final Locator context = page.locator("#causeway-route causeway-object-context");
        final String id = context.getAttribute("object-id");
        assertThat(id).isNotBlank();
        assertThat(page.locator("causeway-property").count()).isGreaterThan(4);
        final Locator editableDecimal = page.locator("causeway-property[member='readWriteProperty']");
        assertThat(editableDecimal.count()).isEqualTo(1);
        editableDecimal.locator("[data-causeway-action='edit']").click();
        final Locator decimalEditor = editableDecimal.locator("input[data-causeway-editor='readWriteProperty']");
        decimalEditor.waitFor();
        assertThat(decimalEditor.getAttribute("type")).isEqualTo("text");
        assertThat(decimalEditor.getAttribute("inputmode")).isEqualTo("decimal");
        final String originalDecimal = decimalEditor.inputValue();
        editableDecimal.locator("[data-causeway-action='cancel']").click();

        assertThat(originalDecimal).isNotBlank();

        page.goBack();
        waitForLogicalType("demo.BigDecimals");
        page.goForward();
        waitForLogicalType("demo.BigDecimalEntity");

        signInToWicket();
        page.navigate(url("/wicket/object/demo.BigDecimalEntity:"
                        + URLEncoder.encode(id, StandardCharsets.UTF_8).replace("+", "%20")),
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        assertThat(page.url()).doesNotContain("/signin");
        assertThat(page.locator("body").innerText()).containsIgnoringCase("BigDecimal");
    }

    @RepeatedTest(2)
    void stringMutationRestoresDeterministicFixtureState() throws Exception {
        final String pageId = invokeViewModel(
                "demo_JavaLangTypesMenu", "strings", "rich__demo_JavaLangStrings");
        final String entityId = firstCollectionEntityId(
                "demo_JavaLangStrings", pageId, "rich__demo_JavaLangStringEntity");
        final String encodedId = OBJECT_MAPPER.writeValueAsString(entityId);
        final JsonNode before = executeGraphQL("{ rich { demo_JavaLangStringEntity(object: {id: "
                + encodedId + "}) { readWriteProperty { get } } } }");
        final String originalValue = before.at("/data/rich/demo_JavaLangStringEntity/readWriteProperty/get").asText();
        assertThat(originalValue).isNotBlank();

        final String mutationPrefix = "mutation { demo_JavaLangStringEntity__readWriteProperty(_target: {id: "
                + encodedId + "}, readWriteProperty: ";
        try {
            final JsonNode changed = executeGraphQL(mutationPrefix
                    + OBJECT_MAPPER.writeValueAsString("Reference App disposable mutation")
                    + ") { readWriteProperty { get } } }");
            assertThat(changed.path("errors").isMissingNode() || changed.path("errors").isEmpty())
                    .as(changed.toPrettyString()).isTrue();
            assertThat(changed.at("/data/demo_JavaLangStringEntity__readWriteProperty/readWriteProperty/get").asText())
                    .isEqualTo("Reference App disposable mutation");
        } finally {
            final JsonNode restored = executeGraphQL(mutationPrefix
                    + OBJECT_MAPPER.writeValueAsString(originalValue)
                    + ") { readWriteProperty { get } } }");
            assertThat(restored.path("errors").isMissingNode() || restored.path("errors").isEmpty())
                    .as(restored.toPrettyString()).isTrue();
            assertThat(restored.at("/data/demo_JavaLangStringEntity__readWriteProperty/readWriteProperty/get").asText())
                    .isEqualTo(originalValue);
        }
    }

    @Test
    void representativeBooleanEnumTemporalResourceAndCustomValueFamiliesAreRoutable() {
        final List<ValueFamily> families = List.of(
                new ValueFamily("demo_JavaLangWrapperTypesMenu", "booleans", "rich__demo_WrapperBooleans", "demo_WrapperBooleans", "rich__demo_WrapperBooleanEntity", "demo.WrapperBooleanEntity"),
                new ValueFamily("demo_JavaLangTypesMenu", "enums", "rich__demo_JavaLangEnums", "demo_JavaLangEnums", "rich__demo_JavaLangEnumEntity", "demo.JavaLangEnumEntity"),
                new ValueFamily("demo_JavaTimeTypesMenu", "localDates", "rich__demo_LocalDates", "demo_LocalDates", "rich__demo_LocalDateEntity", "demo.LocalDateEntity"),
                new ValueFamily("demo_JavaTimeTypesMenu", "offsetDateTimes", "rich__demo_OffsetDateTimes", "demo_OffsetDateTimes", "rich__demo_OffsetDateTimeEntity", "demo.OffsetDateTimeEntity"),
                new ValueFamily("demo_JavaTimeTypesMenu", "zonedDateTimes", "rich__demo_ZonedDateTimes", "demo_ZonedDateTimes", "rich__demo_ZonedDateTimeEntity", "demo.ZonedDateTimeEntity"),
                new ValueFamily("demo_JavaNetTypesMenu", "urls", "rich__demo_Urls", "demo_Urls", "rich__demo_UrlEntity", "demo.UrlEntity"),
                new ValueFamily("demo_CausewayTypesMenu", "passwords", "rich__demo_CausewayPasswords", "demo_CausewayPasswords", "rich__demo_CausewayPasswordEntity", "demo.CausewayPasswordEntity"),
                new ValueFamily("demo_CausewayTypesMenu", "blobs", "rich__demo_CausewayBlobs", "demo_CausewayBlobs", "rich__demo_CausewayBlobEntity", "demo.CausewayBlobEntity"),
                new ValueFamily("demo_CausewayTypesMenu", "clobs", "rich__demo_CausewayClobs", "demo_CausewayClobs", "rich__demo_CausewayClobEntity", "demo.CausewayClobEntity"));

        for (final ValueFamily family : families) {
            final String pageId = invokeViewModel(family.serviceField(), family.actionField(), family.pageResultType());
            final String entityId = firstCollectionEntityId(family.objectField(), pageId, family.entityResultType());
            openObject(family.entityLogicalType(), entityId);
            assertThat(page.locator(".causeway-object-header").count()).as(family.toString()).isEqualTo(1);
            assertThat(page.locator(ROUTE_PAGE).getAttribute("data-route-state")).isIn("ready", "partial-error");
        }

        final String urlPageId = invokeViewModel("demo_JavaNetTypesMenu", "urls", "rich__demo_Urls");
        openObject("demo.UrlEntity", firstCollectionEntityId(
                "demo_Urls", urlPageId, "rich__demo_UrlEntity"));
        final Locator urlProperty = page.locator("causeway-property[member='readWriteProperty']");
        urlProperty.locator("[data-causeway-action='edit']").click();
        assertThat(urlProperty.locator("input[data-causeway-editor='readWriteProperty']").getAttribute("type"))
                .isEqualTo("url");
        urlProperty.locator("[data-causeway-action='cancel']").click();

        final String passwordPageId = invokeViewModel(
                "demo_CausewayTypesMenu", "passwords", "rich__demo_CausewayPasswords");
        openObject("demo.CausewayPasswordEntity", firstCollectionEntityId(
                "demo_CausewayPasswords", passwordPageId, "rich__demo_CausewayPasswordEntity"));
        final Locator passwordProperty = page.locator("causeway-property[member='readWriteProperty']");
        passwordProperty.locator("[data-causeway-action='edit']").click();
        final Locator passwordEditor = passwordProperty.locator("input[data-causeway-editor='readWriteProperty']");
        assertThat(passwordEditor.getAttribute("type")).isEqualTo("password");
        assertThat(passwordEditor.inputValue()).isEmpty();
        passwordProperty.locator("[data-causeway-action='cancel']").click();

        final String compositeId = invokeViewModel(
                "demo_CompositeValueTypeMenu", "compositeValueTypes", "rich__demo_CompositeValuesPage");
        page.navigate(url("/htmx/object/demo.CompositeValuesPage/"
                        + URLEncoder.encode(compositeId, StandardCharsets.UTF_8).replace("+", "%20")),
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        page.waitForFunction("() => document.querySelector(\"[data-testid='causeway-route-page']\")?.dataset.routeState === 'invalid-route'");
        assertThat(page.locator(ROUTE_PAGE).innerText()).containsIgnoringCase("invalid");
    }

    @Test
    void textMultilineNullableDisabledInvalidCancelledAndStalePropertyStatesRemainVisible() {
        openObject("demo.PropertyLayoutMultiLinePage", invokeViewModel(
                "demo_PropertyLayoutMenu", "multiLine", "rich__demo_PropertyLayoutMultiLinePage"));
        final Locator multiline = page.locator("causeway-property[member='propertyUsingAnnotation']");
        assertThat(multiline.count()).isEqualTo(1);
        multiline.locator("[data-causeway-action='edit']").click();
        final Locator multilineEditor = multiline.locator("[data-causeway-editor='propertyUsingAnnotation']");
        multilineEditor.waitFor();
        assertThat(multilineEditor.evaluate("element => element.tagName")).isIn("INPUT", "TEXTAREA");
        multiline.locator("[data-causeway-action='cancel']").click();
        assertThat(page.locator("causeway-property[member='propertyUsingAnnotationReadOnly'] "
                + ".causeway-property-disabled-indicator").count()).isEqualTo(1);

        openObject("demo.PropertyOptionalityPage", invokeViewModel(
                "demo_PropertyMenu", "optionality", "rich__demo_PropertyOptionalityPage"));
        assertThat(page.locator("causeway-property[member='mandatoryProperty']").count()).isEqualTo(1);
        assertThat(page.locator("causeway-property[member='nullableProperty']").count()).isEqualTo(1);
        assertThat(page.locator("causeway-property[member='optionalProperty']").count()).isEqualTo(1);

        openObject("demo.PropertyMustSatisfyPage", invokeViewModel(
                "demo_PropertyMenu", "mustSatisfy", "rich__demo_PropertyMustSatisfyPage"));
        objectAction("updateCustomerAge").click();
        waitForPrompt("updateCustomerAge");
        page.locator(parameter("customerAge")).fill("10");
        submitPrompt();
        page.waitForFunction("() => document.querySelector(\"dialog[data-testid='action-prompt'] .causeway-action-prompt-error\")?.textContent?.trim().length > 0");
        assertThat(page.locator(PROMPT + " .causeway-action-prompt-error").textContent()).containsIgnoringCase("age");
        page.locator("[data-testid='action-prompt-cancel']").click();
        page.locator(PROMPT).waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED));

        openObject("demo.PropertyLayoutHiddenPage", invokeViewModel(
                "demo_PropertyLayoutMenu", "hidden", "rich__demo_PropertyLayoutHiddenPage"));
        assertThat(page.locator("causeway-property").count()).isEqualTo(2);
        assertThat(page.locator("causeway-property[member^='name']").count()).isZero();
    }

    @Test
    void parameterlessParameterizedDefaultValidationAndSuccessfulActionStatesRemainVisible() {
        openObject("demo.ActionSemanticsVm", invokeViewModel(
                "demo_ActionMenu", "semantics", "rich__demo_ActionSemanticsVm"));
        page.evaluate("() => globalThis.__referenceAppActionContextBeforeInvoke = document.querySelector('#causeway-route causeway-object-context')");
        objectAction("reportPropertyForSafe").click();
        page.waitForFunction("() => document.querySelector('#causeway-route causeway-object-context') !== globalThis.__referenceAppActionContextBeforeInvoke");
        waitForLogicalType("demo.ActionSemanticsVm");
        assertThat(page.locator(PROMPT).count()).isZero();

        final Locator update = objectAction("updatePropertyForIdempotent");
        update.click();
        waitForPrompt("updatePropertyForIdempotent");
        final Locator value = page.locator(parameter("value"));
        assertThat(value.inputValue()).isNotBlank();
        value.fill("");
        submitPrompt();
        page.waitForFunction("() => document.querySelector(\"dialog[data-testid='action-prompt'] .causeway-action-prompt-error\")?.textContent?.trim().length > 0");
        assertThat(page.locator(PROMPT + " .causeway-action-prompt-error").textContent()).isNotBlank();
        page.locator("[data-testid='action-prompt-cancel']").click();
        page.locator(PROMPT).waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED));
        assertThat((Boolean) update.evaluate("element => element === document.activeElement")).isTrue();

        update.click();
        waitForPrompt("updatePropertyForIdempotent");
        page.locator(parameter("value")).fill("37");
        submitPrompt();
        page.locator(PROMPT).waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED));
        waitForLogicalType("demo.ActionSemanticsVm");
    }

    @Test
    void populatedPagedConfiguredPolymorphicAndRouteReplacedCollectionsRemainVisible() {
        openObject("demo.CollectionLayoutPagedPage", invokeViewModel(
                "demo_CollectionLayoutMenu", "paged", "rich__demo_CollectionLayoutPagedPage"));
        final String childrenState = waitForCollectionOutcome("children");
        assertThat(childrenState).isEqualTo("ready");
        final int pageRows = page.locator("causeway-collection[member='children'] .causeway-collection-rows li").count();
        assertThat(pageRows).isBetween(1, 20);
        assertThat(page.locator("causeway-collection[member='children'] causeway-object-link").count())
                .isEqualTo(pageRows);
        assertThat(waitForCollectionOutcome("moreChildren")).isEqualTo("ready");

        openObject("demo.CollectionTypeOfPage", invokeViewModel(
                "demo_CollectionMenu", "typeOf", "rich__demo_CollectionTypeOfPage"));
        final String typeOfChildrenState = waitForCollectionOutcome("children");
        assertThat(typeOfChildrenState).isIn("ready", "partial-error", "error");
        if ("ready".equals(typeOfChildrenState)) {
            assertThat(page.locator("causeway-collection[member='children'] causeway-object-link").count())
                    .isGreaterThan(0);
        } else {
            assertThat(page.locator("causeway-collection[member='children']").innerText()).isNotBlank();
        }
        final Locator otherChildren = page.locator("causeway-collection[member='otherChildren']");
        assertThat(otherChildren.count()).isEqualTo(1);
        assertThat(otherChildren.evaluate("element => element.collectionState.status")).isEqualTo("idle");
        final String panelId = (String) otherChildren.evaluate("element => element.closest('[role=tabpanel]')?.id || null");
        if (panelId != null) {
            page.locator("[role=tab][aria-controls='" + panelId + "']").click();
            final String otherChildrenState = waitForCollectionOutcome("otherChildren");
            assertThat(otherChildrenState).isIn("ready", "partial-error", "error");
            if ("ready".equals(otherChildrenState)) {
                assertThat(otherChildren.locator("causeway-object-link").count()).isGreaterThan(0);
            } else {
                assertThat(otherChildren.innerText()).isNotBlank();
            }
        }

        openShell();
        assertThat(page.locator("causeway-collection").count()).isZero();
        assertThat(candidateRequests()).isZero();
    }

    @Test
    void responsiveThemesForcedColorsCspAndExternalIsolation() {
        closeCurrentContextWithoutAssertions();
        openBrowserContext(new Browser.NewContextOptions()
                .setViewportSize(390, 844)
                .setColorScheme(ColorScheme.DARK)
                .setReducedMotion(ReducedMotion.REDUCE));
        openShell();
        assertThat(page.locator("body").evaluate("body => body.scrollWidth <= document.documentElement.clientWidth")).isEqualTo(true);
        assertSemanticAccessibility();

        closeCurrentContextWithoutAssertions();
        openBrowserContext(new Browser.NewContextOptions()
                .setViewportSize(1280, 800)
                .setForcedColors(ForcedColors.ACTIVE));
        openShell();
        assertSemanticAccessibility();
        assertThat(page.locator("[data-causeway-menu-disclosure]").first().isVisible()).isTrue();
    }

    private void openBrowserContext(final Browser.NewContextOptions options) {
        browserFailures.clear();
        browserContext = browser.newContext(options);
        browserContext.addInitScript("""
                (() => {
                  globalThis.__referenceAppFailures = [];
                  globalThis.__referenceAppKnownGaps = [];
                  globalThis.__referenceAppCandidateRequests = 0;
                  const record = value => globalThis.__referenceAppFailures.push(String(value));
                  globalThis.addEventListener('error', event => record(`page error: ${event.message}`));
                  globalThis.addEventListener('unhandledrejection', event => record(`unhandled rejection: ${event.reason}`));
                  globalThis.addEventListener('securitypolicyviolation', event => record(`CSP ${event.effectiveDirective}: ${event.blockedURI}`));
                  const originalFetch = globalThis.fetch.bind(globalThis);
                  globalThis.fetch = async (...args) => {
                    const response = await originalFetch(...args);
                    const url = String(response.url || args[0]);
                    if (url.includes('/causeway-webcomponents/vaadin-reference/vaadin-reference.js')) {
                      globalThis.__referenceAppCandidateRequests += 1;
                    }
                    const isGraphQL = new URL(url, location.href).pathname === '/graphql';
                    if (!response.ok && !isGraphQL) record(`HTTP ${response.status}: ${url}`);
                    if (isGraphQL) {
                      try {
                        const payload = await response.clone().json();
                        const errors = Array.isArray(payload?.errors) ? payload.errors : [];
                        const known = errors.filter(error =>
                          (error.message?.includes("Field 'version'")
                            && (error.message?.includes('DependentArgsDemoItem') || error.message?.includes('__gqlv_meta')))
                          || (error.message?.includes("Field '_meta'") && error.message?.includes('ValueHolder__gqlv_union')));
                        const unknown = errors.filter(error => !known.includes(error));
                        globalThis.__referenceAppKnownGaps.push(...known.map(error => error.message));
                        if (unknown.length) record(`GraphQL: ${unknown.map(error => error.message).join(' | ')}`);
                      } catch (error) {
                        if (error?.name !== 'AbortError') record(`GraphQL response: ${error}`);
                      }
                    }
                    return response;
                  };
                })();
                """);
        page = browserContext.newPage();
        page.onConsoleMessage(message -> {
            if ("error".equals(message.type())
                    && !message.text().contains("Failed to load resource: the server responded with a status of 400")) {
                browserFailures.add("console: " + message.text());
            }
        });
        page.onPageError(error -> browserFailures.add("page: " + error));
        page.onRequest(request -> {
            final URI uri = URI.create(request.url());
            if (uri.getHost() != null && !Set.of("localhost", "127.0.0.1").contains(uri.getHost())) {
                browserFailures.add("external request: " + request.url());
            }
        });
        page.onRequestFailed(request -> {
            final String failure = request.failure();
            if (!(request.url().contains("/graphql") && failure != null && failure.contains("ERR_ABORTED"))) {
                browserFailures.add("request failed: " + request.url() + " " + failure);
            }
        });
    }

    private void openShell() {
        page.navigate(url("/htmx"), new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        waitForMenus();
        waitForLogicalType("demo.Homepage");
    }

    private void openObject(final String logicalType, final String id) {
        page.navigate(url("/htmx/object/" + logicalType + "/"
                        + URLEncoder.encode(id, StandardCharsets.UTF_8).replace("+", "%20")),
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        waitForLogicalType(logicalType);
        page.waitForFunction("() => ['ready', 'fallback', 'unsupported', 'partial-error'].includes(document.querySelector('#causeway-route causeway-object')?.dataset.layoutState)");
        waitForLogicalType(logicalType);
    }

    private void waitForMenus() {
        page.waitForFunction("() => ['ready', 'partial-error'].includes(document.querySelector('causeway-menubars')?.dataset.menuState)");
    }

    private void openMenu(final String name) {
        final Locator disclosure = page.locator("[data-causeway-menu-disclosure]")
                .filter(new Locator.FilterOptions().setHasText(name)).first();
        disclosure.waitFor();
        if (!"true".equals(disclosure.getAttribute("aria-expanded"))) {
            disclosure.click();
        }
    }

    private Locator serviceAction(final String logicalType, final String actionId) {
        final Locator action = page.locator("button[data-service-logical-type='" + logicalType
                + "'][data-action-id='" + actionId + "']").first();
        action.waitFor();
        return action;
    }

    private Locator objectAction(final String member) {
        final Locator host = page.locator("causeway-action[member='" + member + "']").first();
        final Locator action = host.locator("button");
        action.waitFor();
        action.scrollIntoViewIfNeeded();
        return action;
    }

    private void waitForLogicalType(final String logicalType) {
        try {
            page.waitForFunction("logicalType => { const context = document.querySelector('#causeway-route causeway-object-context'); const route = document.querySelector(\"[data-testid='causeway-route-page']\"); return context?.getAttribute('logical-type') === logicalType && ['ready', 'partial-error'].includes(route?.dataset.routeState); }", logicalType);
        } catch (com.microsoft.playwright.TimeoutError cause) {
            final Object state = page.evaluate("() => ({url: location.href, page: document.querySelector(\"[data-testid='causeway-route-page']\")?.dataset, context: document.querySelector('#causeway-route causeway-object-context')?.outerHTML, prompt: document.querySelector(\"dialog[data-testid='action-prompt']\")?.outerHTML, failures: globalThis.__referenceAppFailures})");
            throw new AssertionError("Expected logical type " + logicalType + "; state=" + state, cause);
        }
    }

    private void waitForCollectionRows(final String member) {
        assertThat(waitForCollectionOutcome(member)).isEqualTo("ready");
        assertThat(page.locator("causeway-collection[member='" + member + "'] tbody tr").count()).isGreaterThan(0);
    }

    private String waitForCollectionOutcome(final String member) {
        page.waitForFunction("member => ['ready', 'partial-error', 'error'].includes(document.querySelector(`causeway-collection[member='${member}']`)?.collectionState?.status)", member);
        return (String) page.locator("causeway-collection[member='" + member + "']")
                .evaluate("element => element.collectionState.status");
    }

    private void waitForPrompt(final String actionId) {
        page.locator(PROMPT).waitFor();
        assertThat(page.locator(PROMPT + " h2").textContent().replace(" ", "").toLowerCase())
                .contains(actionId.toLowerCase());
    }

    private void selectFirstAvailableChoice(final String parameterId) {
        final Locator control = page.locator(parameter(parameterId));
        try {
            page.waitForFunction("selector => document.querySelector(selector)?.dataset.widgetState === 'ready'", parameter(parameterId));
        } catch (com.microsoft.playwright.TimeoutError cause) {
            throw new AssertionError("Reference editor did not become ready: "
                    + page.locator(PROMPT).evaluate("element => element.outerHTML"), cause);
        }
        final String label = (String) control.evaluate("element => element.querySelector('vaadin-combo-box').items[0].title");
        final Locator input = control.locator("vaadin-combo-box input");
        input.fill(label);
        input.focus();
        page.keyboard().press("ArrowDown");
        page.keyboard().press("Enter");
        page.waitForFunction("selector => document.querySelector(selector)?.value?.id", parameter(parameterId));
    }

    private void selectFirstAutocompleteChoice(final String parameterId, final String search) {
        final Locator control = page.locator(parameter(parameterId));
        final Locator input = control.locator("vaadin-combo-box input");
        input.fill(search);
        page.waitForFunction("selector => { const element = document.querySelector(selector); return element?.dataset.widgetState === 'ready' && element.querySelector('vaadin-combo-box')?.items?.length > 0; }", parameter(parameterId));
        final String label = (String) control.evaluate("element => element.querySelector('vaadin-combo-box').items[0].title");
        input.fill(label);
        input.focus();
        page.keyboard().press("ArrowDown");
        page.keyboard().press("Enter");
        page.waitForFunction("selector => document.querySelector(selector)?.value?.id", parameter(parameterId));
    }

    private void submitPrompt() {
        page.locator("[data-testid='action-prompt-submit']").click();
    }

    private String invokeViewModel(final String serviceField, final String actionField, final String resultType) {
        try {
            final String selection = "{ rich { " + serviceField + " { " + actionField
                    + " { invoke { results { ... on " + resultType
                    + " { _meta { id logicalTypeName title } } } } } } } }";
            JsonNode payload = executeGraphQL("query " + selection);
            JsonNode metadata = payload.at("/data/rich/" + serviceField + "/" + actionField + "/invoke/results/_meta");
            if (!payload.path("errors").isMissingNode() && !payload.path("errors").isEmpty()) {
                final String mutationField = serviceField + "__" + actionField;
                payload = executeGraphQL("mutation { " + mutationField + " { ... on " + resultType
                        + " { _meta { id logicalTypeName title } } } }");
                metadata = payload.at("/data/" + mutationField + "/_meta");
            }
            assertThat(payload.path("errors").isMissingNode() || payload.path("errors").isEmpty())
                    .as(payload.toPrettyString()).isTrue();
            assertThat(metadata.path("logicalTypeName").asText()).isNotBlank();
            assertThat(metadata.path("id").asText()).isNotBlank();
            return metadata.path("id").asText();
        } catch (Exception ex) {
            throw new AssertionError("Cannot invoke " + serviceField + "." + actionField, ex);
        }
    }

    private String firstCollectionEntityId(
            final String objectField,
            final String objectId,
            final String resultType) {
        return firstCollectionEntityId(objectField, objectId, "entities", resultType);
    }

    private String firstCollectionEntityId(
            final String objectField,
            final String objectId,
            final String collectionMember,
            final String resultType) {
        try {
            final String query = "{ rich { " + objectField + "(object: {id: "
                    + OBJECT_MAPPER.writeValueAsString(objectId) + "}) { " + collectionMember
                    + " { get { ... on " + resultType + " { _meta { id logicalTypeName title } } } } } } }";
            final JsonNode payload = executeGraphQL(query);
            assertThat(payload.path("errors").isMissingNode() || payload.path("errors").isEmpty())
                    .as(payload.toPrettyString()).isTrue();
            final JsonNode results = payload.at("/data/rich/" + objectField + "/" + collectionMember + "/get");
            assertThat(results.isArray()).as(payload.toPrettyString()).isTrue();
            assertThat(results.size()).as(payload.toPrettyString()).isGreaterThan(0);
            return results.get(0).path("_meta").path("id").asText();
        } catch (Exception ex) {
            throw new AssertionError("Cannot read " + objectField + "." + collectionMember, ex);
        }
    }

    private JsonNode executeGraphQL(final String query) throws Exception {
        final HttpRequest request = HttpRequest.newBuilder(URI.create(url("/graphql")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        OBJECT_MAPPER.writeValueAsString(Map.of("query", query)),
                        StandardCharsets.UTF_8))
                .build();
        final HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
        return OBJECT_MAPPER.readTree(response.body());
    }

    private void signInToWicket() {
        page.navigate(url("/wicket/"), new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        if (page.url().contains("/signin")) {
            page.locator("input[name='username']").fill("sven");
            page.locator("input[name='password']").fill("pass");
            page.locator("select[name='timezone']").selectOption(new com.microsoft.playwright.options.SelectOption().setLabel("UTC"));
            page.locator("form button[type='submit']").click();
            page.waitForFunction("() => !location.pathname.includes('/signin')");
        }
    }

    private void assertSemanticAccessibility() {
        final Map<?, ?> result = (Map<?, ?>) page.evaluate("""
                () => {
                  const ids = [...document.querySelectorAll('[id]')].map(element => element.id);
                  const duplicates = ids.filter((id, index) => ids.indexOf(id) !== index);
                  const unlabeled = [...document.querySelectorAll('input,select,textarea,button')].filter(element => {
                    if (element.type === 'hidden') return false;
                    return !(element.getAttribute('aria-label') || element.getAttribute('aria-labelledby') || element.labels?.length || element.textContent?.trim() || element.title);
                  });
                  return {duplicates: [...new Set(duplicates)], unlabeled: unlabeled.length, active: document.activeElement?.localName};
                }
                """);
        assertThat((List<?>) result.get("duplicates")).isEmpty();
        assertThat(((Number) result.get("unlabeled")).intValue()).isZero();
    }

    private int candidateRequests() {
        return ((Number) page.evaluate("() => globalThis.__referenceAppCandidateRequests || 0")).intValue();
    }

    private int knownGapCount() {
        return ((Number) page.evaluate("() => globalThis.__referenceAppKnownGaps?.length || 0")).intValue();
    }

    private int openCandidateOverlays() {
        return ((Number) page.evaluate("() => document.querySelectorAll('vaadin-combo-box-overlay[opened], vaadin-multi-select-combo-box-overlay[opened]').length")).intValue();
    }

    @SuppressWarnings("unchecked")
    private void assertNoBrowserFailures() {
        if (page != null && !page.isClosed()) {
            browserFailures.addAll((List<String>) page.evaluate("() => globalThis.__referenceAppFailures || []"));
        }
        assertThat(browserFailures).isEmpty();
    }

    private void closeCurrentContextWithoutAssertions() {
        assertNoBrowserFailures();
        if (browserContext != null) {
            browserContext.close();
            browserContext = null;
            page = null;
        }
    }

    private String parameter(final String id) {
        return PROMPT + " [data-parameter='" + id + "'] [data-causeway-editor]";
    }

    private String url(final String path) {
        return "http://localhost:" + port + path;
    }

    private record ValueFamily(
            String serviceField,
            String actionField,
            String pageResultType,
            String objectField,
            String entityResultType,
            String entityLogicalType) {
    }
}
