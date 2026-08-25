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
    private final List<String> toolkitRequests = new ArrayList<>();
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
        if (nativeToolkit()) {
            assertThat(toolkitRequests).isEmpty();
        }
        assertNoBrowserFailures();
        if (browserContext != null) {
            browserContext.close();
        }
    }

    @Test
    void menusChoicesAutocompleteCancellationAndRouteDisposal() {
        openShell();
        assertThat(page.locator("html").getAttribute("data-causeway-editor-toolkit"))
                .isEqualTo(nativeToolkit() ? "native" : "vaadin");
        assertThat(toolkitRequests).isEmpty();
        assertThat(referenceAssetRequests()).isZero();

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
        assertThat(referenceAssetRequests()).isZero();

        openObject("demo.ActionAutoCompletePage", invokeViewModel(
                "demo_ActionAutoCompleteMenu", "autoComplete", "rich__demo_ActionAutoCompletePage"));
        objectAction("selectTvCharacter").click();
        waitForPrompt("selectTvCharacter");
        selectLaterAutocompleteChoice("tvCharacter", "o");
        submitPrompt();
        page.locator(PROMPT).waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED));
        waitForLogicalType("demo.ActionAutoCompletePage");

        openShell();
        assertThat(openCandidateOverlays()).isZero();
        assertThat(referenceAssetRequests()).isZero();
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
        final Locator decimalEditor = resolveEditor("causeway-property[member='readWriteProperty'] [data-causeway-editor='readWriteProperty']");
        final String decimalLocalName = (String) decimalEditor.evaluate("element => element.localName");
        assertThat(decimalLocalName).isEqualTo(fieldFamiliesEnabled() ? "vaadin-text-field" : "input");
        assertThat(decimalEditor.evaluate("element => element.localName === 'input' ? element.inputMode : element.inputElement?.inputMode"))
                .isEqualTo("decimal");
        final String originalDecimal = String.valueOf(decimalEditor.evaluate("element => element.value"));
        if (fieldFamiliesEnabled()) {
            assertThat(fieldAssetRequests("numeric")).isEqualTo(1);
            assertThat(fieldAssetRequests("basic")).isZero();
            assertThat(fieldAssetRequests("local-temporal")).isZero();
        }
        assertThat(originalDecimal).isNotBlank();
        final String updatedDecimal = "98765432109876543210.123456789";
        try {
            final String encodedId = OBJECT_MAPPER.writeValueAsString(decimalEntityId);
            try {
                fillEditor(decimalEditor, updatedDecimal);
                page.waitForFunction("() => document.querySelector(\"causeway-property[member='readWriteProperty'] [data-causeway-action='save']\")?.disabled === false");
                editableDecimal.locator("[data-causeway-action='save']").click();
                page.waitForFunction("() => !document.querySelector(\"causeway-property[member='readWriteProperty'] [data-causeway-editor]\")");
                final JsonNode changed = executeGraphQL("{ rich { demo_BigDecimalEntity(object: {id: "
                        + encodedId + "}) { readWriteProperty { get } } } }");
                assertThat(changed.at("/data/rich/demo_BigDecimalEntity/readWriteProperty/get").asText())
                        .isEqualTo(updatedDecimal);
            } finally {
                final JsonNode restored = executeGraphQL("mutation { demo_BigDecimalEntity__readWriteProperty(_target: {id: "
                        + encodedId + "}, readWriteProperty: " + OBJECT_MAPPER.writeValueAsString(originalDecimal)
                        + ") { readWriteProperty { get } } }");
                assertThat(restored.path("errors").isMissingNode() || restored.path("errors").isEmpty())
                        .as(restored.toPrettyString()).isTrue();
            }
        } catch (Exception ex) {
            throw new AssertionError("Exact decimal default interaction failed", ex);
        }

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
                new ValueFamily("demo_JavaTimeTypesMenu", "localTimes", "rich__demo_LocalTimes", "demo_LocalTimes", "rich__demo_LocalTimeEntity", "demo.LocalTimeEntity"),
                new ValueFamily("demo_JavaTimeTypesMenu", "localDateTimes", "rich__demo_LocalDateTimes", "demo_LocalDateTimes", "rich__demo_LocalDateTimeEntity", "demo.LocalDateTimeEntity"),
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

        assertTemporalEditor(
                "demo_JavaTimeTypesMenu", "localDates", "rich__demo_LocalDates",
                "demo_LocalDates", "rich__demo_LocalDateEntity", "demo.LocalDateEntity", "vaadin-date-picker", "date");
        assertTemporalEditor(
                "demo_JavaTimeTypesMenu", "localTimes", "rich__demo_LocalTimes",
                "demo_LocalTimes", "rich__demo_LocalTimeEntity", "demo.LocalTimeEntity", "vaadin-time-picker", "time");
        assertTemporalEditor(
                "demo_JavaTimeTypesMenu", "localDateTimes", "rich__demo_LocalDateTimes",
                "demo_LocalDateTimes", "rich__demo_LocalDateTimeEntity", "demo.LocalDateTimeEntity", "vaadin-date-time-picker", "datetime-local");
        assertTemporalEditor(
                "demo_JavaTimeTypesMenu", "offsetDateTimes", "rich__demo_OffsetDateTimes",
                "demo_OffsetDateTimes", "rich__demo_OffsetDateTimeEntity", "demo.OffsetDateTimeEntity", null, "text");
        assertFieldEditor(
                "demo_JavaLangWrapperTypesMenu", "booleans", "rich__demo_WrapperBooleans",
                "demo_WrapperBooleans", "rich__demo_WrapperBooleanEntity", "demo.WrapperBooleanEntity",
                "readWriteProperty", "vaadin-select", "select");
        assertFieldEditor(
                "demo_JavaLangWrapperTypesMenu", "booleans", "rich__demo_WrapperBooleans",
                "demo_WrapperBooleans", "rich__demo_WrapperBooleanEntity", "demo.WrapperBooleanEntity",
                "readWriteOptionalProperty", "vaadin-select", "select");
        assertFieldEditor(
                "demo_JavaLangTypesMenu", "enums", "rich__demo_JavaLangEnums",
                "demo_JavaLangEnums", "rich__demo_JavaLangEnumEntity", "demo.JavaLangEnumEntity",
                "readWriteProperty", "vaadin-select", "select");

        final String urlPageId = invokeViewModel("demo_JavaNetTypesMenu", "urls", "rich__demo_Urls");
        openObject("demo.UrlEntity", firstCollectionEntityId(
                "demo_Urls", urlPageId, "rich__demo_UrlEntity"));
        final Locator urlProperty = page.locator("causeway-property[member='readWriteProperty']");
        urlProperty.locator("[data-causeway-action='edit']").click();
        final Locator urlEditor = resolveEditor("causeway-property[member='readWriteProperty'] [data-causeway-editor='readWriteProperty']");
        assertThat(urlEditor.evaluate("element => element.localName === 'input' ? element.type : element.inputElement?.inputMode"))
                .isEqualTo("url");
        assertThat(urlEditor.evaluate("element => element.localName"))
                .isEqualTo(fieldFamiliesEnabled() ? "vaadin-text-field" : "input");
        urlProperty.locator("[data-causeway-action='cancel']").click();

        final String passwordPageId = invokeViewModel(
                "demo_CausewayTypesMenu", "passwords", "rich__demo_CausewayPasswords");
        openObject("demo.CausewayPasswordEntity", firstCollectionEntityId(
                "demo_CausewayPasswords", passwordPageId, "rich__demo_CausewayPasswordEntity"));
        final Locator passwordProperty = page.locator("causeway-property[member='readWriteProperty']");
        passwordProperty.locator("[data-causeway-action='edit']").click();
        final Locator passwordEditor = resolveEditor("causeway-property[member='readWriteProperty'] [data-causeway-editor='readWriteProperty']");
        assertThat(passwordEditor.evaluate("element => element.localName === 'input' ? element.type : element.localName"))
                .isEqualTo(fieldFamiliesEnabled() ? "vaadin-password-field" : "password");
        assertThat(passwordEditor.evaluate("element => element.value")).isEqualTo("");
        passwordProperty.locator("[data-causeway-action='cancel']").click();

        final String compositeId = invokeViewModel(
                "demo_CompositeValueTypeMenu", "compositeValueTypes", "rich__demo_CompositeValuesPage");
        assertThat(compositeId.length()).isGreaterThan(1024).isLessThanOrEqualTo(4096);
        page.evaluate("target => document.dispatchEvent(new CustomEvent('causeway-navigation-request', "
                        + "{bubbles: true, composed: true, cancelable: true, detail: {target}}))",
                Map.of("logicalTypeName", "demo.CompositeValuesPage", "id", compositeId));
        waitForLogicalType("demo.CompositeValuesPage");
        final Locator compositeContext = page.locator("#causeway-route causeway-object-context");
        assertThat(compositeContext.getAttribute("object-id")).isEqualTo(compositeId);
        assertThat(page.evaluate("() => decodeURIComponent(location.pathname.substring(location.pathname.lastIndexOf('/') + 1))"))
                .isEqualTo(compositeId);
        final Locator complexNumber = page.locator("causeway-property[member='complexNumber']");
        complexNumber.waitFor();
        assertThat(complexNumber.count()).isEqualTo(1);
        assertThat(complexNumber.innerText()).isNotBlank();

        page.goBack();
        waitForLogicalType("demo.CausewayPasswordEntity");
        page.goForward();
        waitForLogicalType("demo.CompositeValuesPage");
        assertThat(page.locator("#causeway-route causeway-object-context").getAttribute("object-id"))
                .isEqualTo(compositeId);

        page.navigate(url("/htmx/object/type/id/extra"),
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        page.waitForFunction("() => document.querySelector(\"[data-testid='causeway-route-page']\")?.dataset.routeState === 'invalid-route'");
        assertThat(page.locator(ROUTE_PAGE).innerText()).containsIgnoringCase("invalid")
                .doesNotContain("id/extra");
    }

    @Test
    void textMultilineNullableDisabledInvalidCancelledAndStalePropertyStatesRemainVisible() {
        openObject("demo.PropertyLayoutMultiLinePage", invokeViewModel(
                "demo_PropertyLayoutMenu", "multiLine", "rich__demo_PropertyLayoutMultiLinePage"));
        final Locator multiline = page.locator("causeway-property[member='propertyUsingAnnotation']");
        assertThat(multiline.count()).isEqualTo(1);
        multiline.locator("[data-causeway-action='edit']").click();
        final Locator multilineEditor = resolveEditor("causeway-property[member='propertyUsingAnnotation'] [data-causeway-editor='propertyUsingAnnotation']");
        assertThat(multilineEditor.evaluate("element => element.localName"))
                .isEqualTo(fieldFamiliesEnabled() ? "vaadin-text-field" : "input");
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
        fillEditor(resolveEditor(parameter("customerAge")), "10");
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

        if (fieldFamiliesEnabled()) {
            openObject("demo.PropertyLayoutMultiLinePage", invokeViewModel(
                    "demo_PropertyLayoutMenu", "multiLine", "rich__demo_PropertyLayoutMultiLinePage"));
            page.evaluate("""
                    async () => {
                      const fields = await import('/causeway-webcomponents/field-widget.mjs');
                      fields.configureCausewayFieldWidgets({
                        families: ['basic'],
                        moduleUrls: {basic: '/causeway-webcomponents/vaadin-fields/missing-basic.js'}
                      });
                    }
                    """);
            final Locator fallbackProperty = page.locator("causeway-property[member='propertyUsingAnnotation']");
            fallbackProperty.locator("[data-causeway-action='edit']").click();
            final Locator fallback;
            try {
                fallback = resolveEditor("causeway-property[member='propertyUsingAnnotation'] [data-causeway-editor='propertyUsingAnnotation']");
            } catch (com.microsoft.playwright.TimeoutError cause) {
                throw new AssertionError(String.valueOf(fallbackProperty.evaluate("element => element.outerHTML")), cause);
            }
            assertThat(fallback.evaluate("element => element.localName")).isEqualTo("input");
            assertThat(fallback.inputValue()).isNotBlank();
            assertThat(fallbackProperty.locator("causeway-field-editor").count()).isZero();
            fallbackProperty.locator("[data-causeway-action='cancel']").click();
            browserFailures.removeIf(failure -> failure.contains("missing-basic.js")
                    || failure.contains("status of 500"));
        }
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
        final Locator value = resolveEditor(parameter("value"));
        assertThat(String.valueOf(value.evaluate("element => element.value"))).isNotBlank();
        fillEditor(value, "");
        submitPrompt();
        page.waitForFunction("() => document.querySelector(\"dialog[data-testid='action-prompt'] .causeway-action-prompt-error\")?.textContent?.trim().length > 0");
        assertThat(page.locator(PROMPT + " .causeway-action-prompt-error").textContent()).isNotBlank();
        page.locator("[data-testid='action-prompt-cancel']").click();
        page.locator(PROMPT).waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED));
        assertThat((Boolean) update.evaluate("element => element === document.activeElement")).isTrue();

        update.click();
        waitForPrompt("updatePropertyForIdempotent");
        fillEditor(resolveEditor(parameter("value")), "37");
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

        openObject("demo.ActionChoicesFromPage", invokeViewModel(
                "demo_ActionMenu", "choicesFrom", "rich__demo_ActionChoicesFromPage"));
        assertThat(waitForCollectionOutcome("objects")).isEqualTo("ready");
        final Locator polymorphicObjects = page.locator("causeway-collection[member='objects']");
        assertThat(polymorphicObjects.locator("causeway-object-link").count()).isGreaterThan(0);
        final String polymorphicSelection = (String) polymorphicObjects
                .evaluate("element => JSON.stringify(element.collectionState.rowSelection)");
        assertThat(polymorphicSelection).contains("rich__demo_ActionChoicesFromEntity", "_meta");

        openObject("demo.CollectionTypeOfPage", invokeViewModel(
                "demo_CollectionMenu", "typeOf", "rich__demo_CollectionTypeOfPage"));
        final String typeOfChildrenState = waitForCollectionOutcome("children");
        assertThat(typeOfChildrenState).isEqualTo("ready");
        assertThat(page.locator("causeway-collection[member='children'] causeway-object-link").count())
                .isGreaterThan(0);
        final Locator otherChildren = page.locator("causeway-collection[member='otherChildren']");
        assertThat(otherChildren.count()).isEqualTo(1);
        assertThat(otherChildren.evaluate("element => element.collectionState.status")).isEqualTo("idle");
        final String panelId = (String) otherChildren.evaluate("element => element.closest('[role=tabpanel]')?.id || null");
        if (panelId != null) {
            page.locator("[role=tab][aria-controls='" + panelId + "']").click();
            final String otherChildrenState = waitForCollectionOutcome("otherChildren");
            assertThat(otherChildrenState).isEqualTo("error");
            assertThat(otherChildren.innerText()).containsIgnoringCase("not readable");
        }

        openShell();
        assertThat(page.locator("causeway-collection").count()).isZero();
        assertThat(referenceAssetRequests()).isZero();
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
        toolkitRequests.clear();
        browserContext = browser.newContext(options);
        browserContext.addInitScript("""
                (() => {
                  globalThis.__referenceAppFailures = [];
                  globalThis.__referenceAppKnownGaps = [];
                  globalThis.__referenceAppReferenceAssetRequests = 0;
                  const record = value => globalThis.__referenceAppFailures.push(String(value));
                  globalThis.addEventListener('error', event => record(`page error: ${event.message}`));
                  globalThis.addEventListener('unhandledrejection', event => record(`unhandled rejection: ${event.reason}`));
                  globalThis.addEventListener('securitypolicyviolation', event => record(`CSP ${event.effectiveDirective}: ${event.blockedURI}`));
                  const originalFetch = globalThis.fetch.bind(globalThis);
                  globalThis.fetch = async (...args) => {
                    const response = await originalFetch(...args);
                    const url = String(response.url || args[0]);
                    if (url.includes('/causeway-webcomponents/vaadin-reference/vaadin-reference.js')) {
                      globalThis.__referenceAppReferenceAssetRequests += 1;
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
            if (request.url().contains("/causeway-webcomponents/vaadin-reference/")
                    || request.url().contains("/causeway-webcomponents/vaadin-fields/")) {
                toolkitRequests.add(request.url());
            }
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
        if (Boolean.TRUE.equals(control.evaluate("element => element.localName === 'select'"))) {
            control.evaluate("element => { element.selectedIndex = 0; "
                    + "element.dispatchEvent(new Event('change', {bubbles: true, composed: true})); }");
            return;
        }
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

    private void selectLaterAutocompleteChoice(final String parameterId, final String search) {
        final Locator control = page.locator(parameter(parameterId));
        final Locator parameterContainer = page.locator("[data-parameter='" + parameterId + "']");
        try {
            page.waitForFunction("selector => { const editor = document.querySelector(selector); "
                    + "return editor?.matches('causeway-reference-editor, input[list]') "
                    + "|| editor?.querySelector('causeway-reference-editor, input[list]'); }", parameter(parameterId));
        } catch (final com.microsoft.playwright.TimeoutError cause) {
            throw new AssertionError("Autocomplete editor was unavailable: " + page.locator(PROMPT).innerHTML(), cause);
        }
        if (!Boolean.TRUE.equals(control.evaluate("element => element.localName === 'causeway-reference-editor'"))) {
            final Locator input = control;
            input.fill(search);
            try {
                page.waitForFunction("selector => document.querySelector(selector)?.querySelectorAll('datalist option').length > 0",
                        "[data-parameter='" + parameterId + "']");
            } catch (final com.microsoft.playwright.TimeoutError cause) {
                throw new AssertionError("Native autocomplete did not receive its first window: "
                        + page.locator(PROMPT).innerHTML(), cause);
            }
            assertThat(parameterContainer.locator(".causeway-autocomplete-continuation").innerText())
                    .containsIgnoringCase("more matches");
            final String encoded = parameterContainer.locator("datalist option").first().getAttribute("value");
            input.evaluate("(element, value) => { element.value = value; "
                    + "element.dispatchEvent(new Event('change', {bubbles: true, composed: true})); }", encoded);
            page.waitForFunction("() => document.querySelector('[data-testid=action-prompt-submit]')?.disabled === false");
            return;
        }
        control.locator("vaadin-combo-box").waitFor();
        final JsonNode pageResult = OBJECT_MAPPER.valueToTree(control.evaluate("async (element, search) => {\n"
                + "  const combo = element.querySelector('vaadin-combo-box');\n"
                + "  if (typeof combo?.dataProvider !== 'function') throw new Error('Windowed data provider is unavailable');\n"
                + "  return await new Promise(resolve => combo.dataProvider(\n"
                + "    {filter: search, page: 1, pageSize: 5},\n"
                + "    (items, total) => {\n"
                + "      const selected = items[0];\n"
                + "      combo.dispatchEvent(new CustomEvent('selected-item-changed', {detail: {value: selected}}));\n"
                + "      resolve({count: items.length, total, selected, error: element.dataset.widgetError || null});\n"
                + "    }));\n"
                + "}", search));
        assertThat(pageResult.path("count").asInt()).as(pageResult.toPrettyString()).isGreaterThan(0);
        assertThat(pageResult.path("total").asInt()).isGreaterThan(5);
        assertThat(pageResult.at("/selected/id").asText()).isNotBlank();
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

    private void assertFieldEditor(
            final String serviceField,
            final String actionField,
            final String pageResultType,
            final String objectField,
            final String entityResultType,
            final String entityLogicalType,
            final String member,
            final String vaadinTag,
            final String nativeTag) {
        final String pageId = invokeViewModel(serviceField, actionField, pageResultType);
        openObject(entityLogicalType, firstCollectionEntityId(objectField, pageId, entityResultType));
        final Locator property = page.locator("causeway-property[member='" + member + "']");
        property.locator("[data-causeway-action='edit']").click();
        final Locator editor = resolveEditor("causeway-property[member='" + member + "'] [data-causeway-editor='" + member + "']");
        assertThat(editor.evaluate("element => element.localName"))
                .isEqualTo(fieldFamiliesEnabled() ? vaadinTag : nativeTag);
        if (fieldFamiliesEnabled()) {
            assertThat(fieldAssetRequests("basic")).isEqualTo(1);
            assertThat(fieldAssetRequests("numeric")).isZero();
            assertThat(fieldAssetRequests("local-temporal")).isZero();
        }
        property.locator("[data-causeway-action='cancel']").click();
    }

    private void assertTemporalEditor(
            final String serviceField,
            final String actionField,
            final String pageResultType,
            final String objectField,
            final String entityResultType,
            final String entityLogicalType,
            final String vaadinTag,
            final String nativeType) {
        final String pageId = invokeViewModel(serviceField, actionField, pageResultType);
        final String member = "demo_LocalDateTimes".equals(objectField)
                ? "readWriteOptionalProperty"
                : "readWriteProperty";
        final String entityId = vaadinTag == null
                ? firstCollectionEntityId(objectField, pageId, entityResultType)
                : pickerCompatibleCollectionEntityId(objectField, pageId, entityResultType, member);
        openObject(entityLogicalType, entityId);
        final Locator property = page.locator("causeway-property[member='" + member + "']");
        property.locator("[data-causeway-action='edit']").click();
        final Locator editor = resolveEditor("causeway-property[member='" + member + "'] [data-causeway-editor='" + member + "']");
        final String localName = String.valueOf(editor.evaluate("element => element.localName"));
        if (vaadinTag != null && fieldFamiliesEnabled()) {
            assertThat(localName).isEqualTo(vaadinTag);
            assertThat(fieldAssetRequests("local-temporal")).isEqualTo(1);
            assertThat(fieldAssetRequests("basic")).isZero();
            assertThat(fieldAssetRequests("numeric")).isZero();
        } else {
            assertThat(localName).isEqualTo("input");
            assertThat(editor.getAttribute("type")).isEqualTo(nativeType);
            if (fieldFamiliesEnabled()) {
                assertThat(fieldAssetRequests("local-temporal")).isZero();
            }
        }
        final String current = String.valueOf(editor.evaluate("element => element.value"));
        if (current.isBlank()) {
            final String representative = switch (nativeType) {
                case "date" -> "2026-08-24";
                case "time" -> "13:14:15.123";
                case "datetime-local" -> "2026-08-24T13:14:15.123";
                default -> "2026-08-24T13:14:15.123Z";
            };
            fillEditor(editor, representative);
            assertThat(editor.evaluate("element => element.value")).isEqualTo(representative);
        }
        property.locator("[data-causeway-action='cancel']").click();
    }

    private String pickerCompatibleCollectionEntityId(
            final String objectField,
            final String objectId,
            final String resultType,
            final String member) {
        try {
            final String query = "{ rich { " + objectField + "(object: {id: "
                    + OBJECT_MAPPER.writeValueAsString(objectId) + "}) { entities { get { ... on "
                    + resultType + " { _meta { id } " + member + " { get } } } } } } }";
            final JsonNode payload = executeGraphQL(query);
            assertThat(payload.path("errors").isMissingNode() || payload.path("errors").isEmpty())
                    .as(payload.toPrettyString()).isTrue();
            final JsonNode results = payload.at("/data/rich/" + objectField + "/entities/get");
            for (final JsonNode result : results) {
                final String value = result.at("/" + member + "/get").asText("");
                final int fraction = value.indexOf('.');
                if (fraction < 0 || value.length() - fraction - 1 <= 3) {
                    return result.at("/_meta/id").asText();
                }
            }
            throw new AssertionError("No picker-compatible value in " + objectField);
        } catch (Exception ex) {
            throw new AssertionError("Cannot find picker-compatible value in " + objectField, ex);
        }
    }

    private static boolean nativeToolkit() {
        return "native".equalsIgnoreCase(System.getProperty(
                "causeway.viewer.webcomponents.htmx.editor-toolkit", "vaadin"));
    }

    private boolean fieldFamiliesEnabled() {
        return !String.valueOf(page.locator("html").getAttribute("data-causeway-field-families")).isBlank()
                && !"null".equals(String.valueOf(page.locator("html").getAttribute("data-causeway-field-families")));
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

    private int fieldAssetRequests(final String family) {
        return ((Number) page.evaluate("family => performance.getEntriesByType('resource').filter(entry => entry.name.includes('/causeway-webcomponents/vaadin-fields/vaadin-' + family + '.js')).length", family)).intValue();
    }

    private int referenceAssetRequests() {
        return ((Number) page.evaluate("() => globalThis.__referenceAppReferenceAssetRequests || 0")).intValue();
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
