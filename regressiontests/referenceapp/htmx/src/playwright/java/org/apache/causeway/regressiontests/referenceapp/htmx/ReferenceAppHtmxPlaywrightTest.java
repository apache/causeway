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
import com.microsoft.playwright.Route;
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
    private final List<String> graphQLRequests = new ArrayList<>();
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
        assertThat(page.locator("html").getAttribute("data-causeway-component-toolkit"))
                .isEqualTo(nativeToolkit() ? "native" : "vaadin");
        assertThat(page.locator("html").getAttribute("data-causeway-presentation"))
                .isEqualTo(nativeToolkit() ? "native" : "vaadin");
        assertThat(page.locator("html").getAttribute("data-causeway-action-buttons"))
                .isEqualTo(nativeToolkit() ? "native" : "vaadin");
        assertThat(page.locator("html").getAttribute("data-causeway-application-menubar"))
                .isEqualTo(nativeToolkit() ? "native" : "vaadin");
        if (nativeToolkit()) {
            assertThat(toolkitRequests).isEmpty();
        } else {
            assertThat(fieldAssetRequests("basic")).isLessThanOrEqualTo(1);
            page.waitForFunction("() => document.querySelector('cw-action-control')?.dataset.widgetState === 'ready'");
            assertThat(toolkitRequests.stream()
                    .filter(request -> request.contains("/vaadin-actions/vaadin-actions.js"))
                    .count()).as(toolkitRequests.toString()).isEqualTo(1);
            assertThat(toolkitRequests.stream()
                    .filter(request -> request.contains("/vaadin-menubar/vaadin-menubar.js"))
                    .count()).as(toolkitRequests.toString()).isEqualTo(1);
        }
        assertThat(referenceAssetRequests()).isZero();

        openMenu("Prog Model");
        activateServiceAction("demo.ActionChoicesMenu", "choices");
        waitForLogicalType("demo.ActionChoices");
        page.waitForFunction("() => ['ready', 'fallback', 'partial-error'].includes(document.querySelector('#causeway-route cw-object')?.dataset.layoutState)");
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
    void menuBarHierarchyResponsiveRefreshAndStaleItemsRemainCausewayOwned() {
        openShell();
        final List<?> hierarchy = (List<?>) page.evaluate("""
                () => [...document.querySelectorAll('cw-menubar-primary, cw-menubar-secondary, cw-menubar-tertiary')]
                  .filter(host => host._projection?.accepted)
                  .map(host => ({
                    role: host._projection.role,
                    generation: host._projection.generation,
                    menus: host._projection.menus.map(menu => ({
                      label: menu.label,
                      sections: menu.sections.map(section => ({
                        label: section.label,
                        actions: section.actions.map(action => ({
                          service: action.serviceLogicalTypeName,
                          id: action.actionId,
                          label: action.label,
                          disabled: action.disabled,
                          reason: action.disabledReason
                        }))
                      }))
                    }))
                  }))
                """);
        assertThat(hierarchy).isNotEmpty();
        assertThat(hierarchy.stream().map(value -> String.valueOf(((Map<?, ?>) value).get("role"))).toList())
                .contains("primary", "secondary", "tertiary");
        assertThat(hierarchy.toString()).contains("Prog Model").contains("choices");
        assertThat(hierarchy.toString()).contains("disabled=true").contains("reason=");
        if (nativeToolkit()) {
            assertThat(page.locator("cw-menubar-control").count()).isZero();
        } else {
            assertThat(page.locator("cw-menubar-control vaadin-menu-bar").count()).isEqualTo(hierarchy.size());
            assertThat(toolkitRequests.stream().filter(url -> url.contains("/vaadin-menubar/vaadin-menubar.js")).count())
                    .isEqualTo(1);
        }

        final int readsBeforeResize = graphQLRequests.size();
        page.setViewportSize(390, 844);
        try {
            page.waitForFunction("() => [...document.querySelectorAll('cw-menubar-primary, cw-menubar-secondary, cw-menubar-tertiary')].filter(element => !element.hidden).every(element => element.dataset.causewayMenubarResponsive === 'narrow')");
        } catch (final com.microsoft.playwright.TimeoutError cause) {
            final var diagnostics = page.locator("cw-menubar-primary, cw-menubar-secondary, cw-menubar-tertiary")
                    .evaluateAll("elements => elements.map(element => ({role: element.dataset.causewayBar, hidden: element.hidden, responsive: element.dataset.causewayMenubarResponsive, presentation: element.dataset.causewayMenubarPresentation, width: element.getBoundingClientRect().width}))");
            throw new AssertionError("Menu Bar did not become narrow: " + diagnostics, cause);
        }
        assertThat(page.locator("body").evaluate("body => body.scrollWidth <= document.documentElement.clientWidth")).isEqualTo(true);
        page.setViewportSize(1440, 900);
        page.waitForFunction("() => document.querySelector('cw-menubar-primary')?.dataset.causewayMenubarResponsive === 'wide'");
        assertThat(graphQLRequests.size()).isEqualTo(readsBeforeResize);

        final int generation = ((Number) page.locator("cw-menubar-primary")
                .evaluate("host => host._projection?.generation ?? 0")).intValue();
        final int requestsBeforeRefresh = graphQLRequests.size();
        page.evaluate("() => document.querySelector('cw-menubars').refresh()");
        page.waitForFunction("generation => document.querySelector('cw-menubar-primary')?._projection?.generation > generation", generation);
        if (!nativeToolkit()) {
            page.waitForFunction("() => document.querySelector('cw-menubar-primary cw-menubar-control')?.dataset.widgetState === 'ready'");
            final int afterRefresh = graphQLRequests.size();
            page.locator("cw-menubar-primary").evaluate("""
                    host => {
                      const current = Object.values(host._projection.actions).find(action => !action.disabled);
                      const staleKey = `${host._projection.generation - 1}:primary:0:0:0`;
                      host.querySelector('vaadin-menu-bar').dispatchEvent(new CustomEvent('item-selected', {detail: {value: {causewayKey: staleKey}}}));
                    }
                    """);
            page.waitForTimeout(50);
            assertThat(graphQLRequests.size()).isEqualTo(afterRefresh);
        }
        assertThat(graphQLRequests.size()).isGreaterThan(requestsBeforeRefresh);
    }

    @Test
    void valuesCollectionsHistoryAndWicketComparisonShareTheFixture() {
        final String decimalPageId = invokeViewModel(
                "demo_JavaMathTypesMenu", "bigDecimals", "rich__demo_BigDecimals");
        openObject("demo.BigDecimals", decimalPageId);
        final String decimalEntityId = firstCollectionEntityId(
                "demo_BigDecimals", decimalPageId, "rich__demo_BigDecimalEntity");
        openObject("demo.BigDecimalEntity", decimalEntityId);

        final Locator context = page.locator("#causeway-route cw-object-context");
        final String id = context.getAttribute("object-id");
        assertThat(id).isNotBlank();
        assertThat(page.locator("cw-property").count()).isGreaterThan(4);
        final Locator editableDecimal = page.locator("cw-property[id='readWriteProperty']");
        assertThat(editableDecimal.count()).isEqualTo(1);
        assertReadOnlyPresentation(editableDecimal, "vaadin-text-field");
        editableDecimal.locator("[data-causeway-action='edit']").click();
        final Locator decimalEditor = resolveEditor("cw-property[id='readWriteProperty'] [data-causeway-editor='readWriteProperty']");
        final String decimalLocalName = (String) decimalEditor.evaluate("element => element.localName");
        assertThat(decimalLocalName).isEqualTo(fieldFamiliesEnabled() ? "vaadin-text-field" : "input");
        assertThat(decimalEditor.evaluate("element => element.localName === 'input' ? element.inputMode : element.inputElement?.inputMode"))
                .isEqualTo("decimal");
        final String originalDecimal = String.valueOf(decimalEditor.evaluate("element => element.value"));
        if (fieldFamiliesEnabled()) {
            assertThat(fieldAssetRequests("numeric")).isEqualTo(1);
        }
        assertThat(originalDecimal).isNotBlank();
        final String updatedDecimal = "98765432109876543210.123456789";
        try {
            final String encodedId = OBJECT_MAPPER.writeValueAsString(decimalEntityId);
            try {
                fillEditor(decimalEditor, updatedDecimal);
                page.waitForFunction("() => document.querySelector(\"cw-property[id='readWriteProperty'] [data-causeway-action='save']\")?.disabled === false");
                editableDecimal.locator("[data-causeway-action='save']").click();
                page.waitForFunction("() => !document.querySelector(\"cw-property[id='readWriteProperty'] [data-causeway-editor]\")");
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
        final Locator urlProperty = page.locator("cw-property[id='readWriteProperty']");
        assertReadOnlyPresentation(urlProperty, "vaadin-text-field");
        urlProperty.locator("[data-causeway-action='edit']").click();
        final Locator urlEditor = resolveEditor("cw-property[id='readWriteProperty'] [data-causeway-editor='readWriteProperty']");
        assertThat(urlEditor.evaluate("element => element.localName === 'input' ? element.type : element.inputElement?.inputMode"))
                .isEqualTo("url");
        assertThat(urlEditor.evaluate("element => element.localName"))
                .isEqualTo(fieldFamiliesEnabled() ? "vaadin-text-field" : "input");
        urlProperty.locator("[data-causeway-action='cancel']").click();

        final String passwordPageId = invokeViewModel(
                "demo_CausewayTypesMenu", "passwords", "rich__demo_CausewayPasswords");
        openObject("demo.CausewayPasswordEntity", firstCollectionEntityId(
                "demo_CausewayPasswords", passwordPageId, "rich__demo_CausewayPasswordEntity"));
        final Locator passwordProperty = page.locator("cw-property[id='readWriteProperty']");
        assertReadOnlyPresentation(passwordProperty, null);
        passwordProperty.locator("[data-causeway-action='edit']").click();
        final Locator passwordEditor = resolveEditor("cw-property[id='readWriteProperty'] [data-causeway-editor='readWriteProperty']");
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
        final Locator compositeContext = page.locator("#causeway-route cw-object-context");
        assertThat(compositeContext.getAttribute("object-id")).isEqualTo(compositeId);
        assertThat(page.evaluate("() => decodeURIComponent(location.pathname.substring(location.pathname.lastIndexOf('/') + 1))"))
                .isEqualTo(compositeId);
        final Locator complexNumber = page.locator("cw-property[id='complexNumber']");
        complexNumber.waitFor();
        assertThat(complexNumber.count()).isEqualTo(1);
        assertThat(complexNumber.innerText()).isNotBlank();

        page.goBack();
        waitForLogicalType("demo.CausewayPasswordEntity");
        page.goForward();
        waitForLogicalType("demo.CompositeValuesPage");
        assertThat(page.locator("#causeway-route cw-object-context").getAttribute("object-id"))
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
        final Locator multiline = page.locator("cw-property[id='propertyUsingAnnotation']");
        assertThat(multiline.count()).isEqualTo(1);
        multiline.locator("[data-causeway-action='edit']").click();
        final Locator multilineEditor = resolveEditor("cw-property[id='propertyUsingAnnotation'] [data-causeway-editor='propertyUsingAnnotation']");
        assertThat(multilineEditor.evaluate("element => element.localName"))
                .isEqualTo(fieldFamiliesEnabled() ? "vaadin-text-field" : "input");
        multiline.locator("[data-causeway-action='cancel']").click();
        assertThat(page.locator("cw-property[id='propertyUsingAnnotationReadOnly'] "
                + ".causeway-property-label.causeway-property-disabled-tooltip").count()).isEqualTo(1);

        openObject("demo.PropertyOptionalityPage", invokeViewModel(
                "demo_PropertyMenu", "optionality", "rich__demo_PropertyOptionalityPage"));
        assertThat(page.locator("cw-property[id='mandatoryProperty']").count()).isEqualTo(1);
        assertThat(page.locator("cw-property[id='nullableProperty']").count()).isEqualTo(1);
        assertThat(page.locator("cw-property[id='optionalProperty']").count()).isEqualTo(1);

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
        assertThat(page.locator("cw-property").count()).isEqualTo(2);
        assertThat(page.locator("cw-property[id^='name']").count()).isZero();

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
            final Locator fallbackProperty = page.locator("cw-property[id='propertyUsingAnnotation']");
            fallbackProperty.locator("[data-causeway-action='edit']").click();
            final Locator fallback;
            try {
                fallback = resolveEditor("cw-property[id='propertyUsingAnnotation'] [data-causeway-editor='propertyUsingAnnotation']");
            } catch (com.microsoft.playwright.TimeoutError cause) {
                throw new AssertionError(String.valueOf(fallbackProperty.evaluate("element => element.outerHTML")), cause);
            }
            assertThat(fallback.evaluate("element => element.localName")).isEqualTo("input");
            assertThat(fallback.inputValue()).isNotBlank();
            assertThat(fallbackProperty.locator("cw-field-editor").count()).isZero();
            fallbackProperty.locator("[data-causeway-action='cancel']").click();
            browserFailures.removeIf(failure -> failure.contains("missing-basic.js")
                    || failure.contains("status of 500"));
        }
    }

    @Test
    void parameterlessParameterizedDefaultValidationAndSuccessfulActionStatesRemainVisible() {
        openObject("demo.ActionSemanticsVm", invokeViewModel(
                "demo_ActionMenu", "semantics", "rich__demo_ActionSemanticsVm"));
        assertOrdinaryActionPresentation("reportPropertyForSafe");
        assertOrdinaryActionPresentation("updatePropertyForIdempotent");
        page.evaluate("() => globalThis.__referenceAppActionContextBeforeInvoke = document.querySelector('#causeway-route cw-object-context')");
        objectAction("reportPropertyForSafe").click();
        page.waitForFunction("() => document.querySelector('#causeway-route cw-object-context') !== globalThis.__referenceAppActionContextBeforeInvoke");
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
        assertThat((Boolean) update.evaluate(
                "element => element === document.activeElement || element.contains(document.activeElement)"))
                .isTrue();

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
        page.setViewportSize(1600, 900);
        openObject("demo.CollectionLayoutPagedPage", invokeViewModel(
                "demo_CollectionLayoutMenu", "paged", "rich__demo_CollectionLayoutPagedPage"));
        final String childrenState = waitForCollectionOutcome("children");
        assertThat(childrenState).isEqualTo("ready");
        final int pageRows = ((Number) page.locator("cw-collection[id='children']")
                .evaluate("element => element.collectionState.rows.length")).intValue();
        assertThat(pageRows).isBetween(1, 20);
        assertCollectionPresentation("children", false);
        if (!nativeToolkit()) {
            assertThat(page.locator("cw-collection[id='children']")
                    .getAttribute("data-causeway-grid-fallback"))
                    .isEqualTo("ordering-not-deterministic");
        }
        assertThat(page.locator("cw-collection[id='children'] cw-object-link").count())
                .isEqualTo(pageRows);
        assertThat(waitForCollectionOutcome("moreChildren")).isEqualTo("ready");

        openObject("demo.CollectionLayoutSortedByPage", invokeViewModel(
                "demo_CollectionLayoutMenu", "sortedBy", "rich__demo_CollectionLayoutSortedByPage"));
        assertThat(waitForCollectionOutcome("children")).isEqualTo("ready");
        final int configuredRows = ((Number) page.locator("cw-collection[id='children']")
                .evaluate("element => element.collectionState.rows.length")).intValue();
        assertThat(configuredRows).isGreaterThan(0);
        assertCollectionPresentation("children", true);
        assertThat(page.locator("cw-collection[id='children'] cw-object-link").count())
                .isEqualTo(configuredRows);

        openObject("demo.ActionChoicesFromPage", invokeViewModel(
                "demo_ActionMenu", "choicesFrom", "rich__demo_ActionChoicesFromPage"));
        assertThat(waitForCollectionOutcome("objects")).isEqualTo("ready");
        final Locator polymorphicObjects = page.locator("cw-collection[id='objects']");
        assertThat(polymorphicObjects.locator("cw-object-link").count()).isGreaterThan(0);
        final String polymorphicSelection = (String) polymorphicObjects
                .evaluate("element => JSON.stringify(element.collectionState.rowSelection)");
        assertThat(polymorphicSelection).contains("rich__demo_ActionChoicesFromEntity", "_meta");

        openObject("demo.CollectionTypeOfPage", invokeViewModel(
                "demo_CollectionMenu", "typeOf", "rich__demo_CollectionTypeOfPage"));
        final String typeOfChildrenState = waitForCollectionOutcome("children");
        assertThat(typeOfChildrenState).isEqualTo("ready");
        assertThat(page.locator("cw-collection[id='children'] cw-object-link").count())
                .isGreaterThan(0);
        final Locator otherChildren = page.locator("cw-collection[id='otherChildren']");
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
        assertThat(page.locator("cw-collection").count()).isZero();
        assertThat(referenceAssetRequests()).isZero();
    }

    @Test
    void membershipRefreshRetiresStaleRangesContextsCellsTotalsAndFocus() {
        page.setViewportSize(1600, 900);
        openObject("demo.CollectionLayoutSortedByPage", invokeViewModel(
                "demo_CollectionLayoutMenu", "sortedBy", "rich__demo_CollectionLayoutSortedByPage"));
        assertThat(waitForCollectionOutcome("children")).isEqualTo("ready");
        final Locator collection = page.locator("cw-collection[id='children']");
        if (!nativeToolkit()) assertCollectionPresentation("children", true);
        final Map<?, ?> outcome = (Map<?, ?>) collection.evaluate("""
                async element => {
                  const firstKey = element.gridProjection.rows[0].key;
                  const context = element._resolvedContext;
                  const delegate = context.loadCollection.bind(context);
                  let release;
                  context.loadCollection = options => {
                    if (!options.force && options.offset === 5) {
                      return new Promise((resolve, reject) => {
                        release = () => delegate(options).then(resolve, reject);
                        globalThis.__releaseStaleCollectionRange = release;
                      });
                    }
                    return delegate(options).then(result => {
                      if (!options.force || options.offset !== 0) return result;
                      const rows = Object.freeze(result.rows.slice(1));
                      const window = Object.freeze({
                        ...result.window,
                        returnedCount: rows.length,
                        totalCount: result.window.totalCount - 1,
                        hasNext: rows.length < result.window.totalCount - 1,
                        nextOffset: rows.length
                      });
                      return Object.freeze({
                        ...result,
                        rows,
                        window,
                        data: Object.freeze({
                          ...result.data,
                          window: Object.freeze({...result.data.window, ...window, rows})
                        })
                      });
                    });
                  };
                  element.gridFocusIntent = Object.freeze({
                    rowKey: firstKey,
                    member: '_meta',
                    role: 'object-link',
                    objectGeneration: element.componentState.generation,
                    collectionMember: element.id
                  });
                  if (!element.querySelector('cw-collection-grid')) {
                    await element.load({force: true, offset: 0, size: 5});
                    return {firstKey, keys: element.gridProjection.rows.map(row => row.key), staleCallback: false};
                  }
                  element.querySelector('cw-collection-grid')._control.dataProvider(
                    {page: 1, pageSize: 5},
                    () => { globalThis.__staleCollectionCallback = true; }
                  );
                  while (!globalThis.__releaseStaleCollectionRange) await new Promise(resolve => setTimeout(resolve));
                  await element.load({force: true, offset: 0, size: 5});
                  await globalThis.__releaseStaleCollectionRange().catch(() => {});
                  await new Promise(resolve => setTimeout(resolve, 25));
                  return {
                    firstKey,
                    keys: element.gridProjection.rows.map(row => row.key),
                    staleCallback: globalThis.__staleCollectionCallback === true
                  };
                }
                """);
        assertThat(((List<?>) outcome.get("keys")).contains(outcome.get("firstKey"))).isFalse();
        assertThat(outcome.get("staleCallback")).isEqualTo(false);
        assertThat(((Number) collection.evaluate("element => element.rangeBroker?.snapshot().entries ?? 0")).intValue())
                .isLessThanOrEqualTo(6);
    }

    @Test
    void gridFailuresBeforeAndAfterConnectionRecoverIndependentlyWithoutStaleControls() {
        page.setViewportSize(1600, 900);
        openShell();
        if (nativeToolkit()) {
            assertThat(toolkitRequests).isEmpty();
            return;
        }
        final String gridRoute = "**/causeway-webcomponents/vaadin-grid/vaadin-grid.js";
        page.route(gridRoute, route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/javascript")
                .setBody("throw new Error('Intentional value-free Grid family failure.');")));
        openObject("demo.CollectionLayoutSortedByPage", invokeViewModel(
                "demo_CollectionLayoutMenu", "sortedBy", "rich__demo_CollectionLayoutSortedByPage"));
        final Locator collection = page.locator("cw-collection[id='children']");
        assertThat(waitForCollectionOutcome("children")).isEqualTo("ready");
        page.waitForFunction("() => document.documentElement.dataset.causewayGridFamily === 'failed' && document.querySelector(\"cw-collection[id='children']\")?.dataset.causewayGridFallback === 'family-failed'");
        assertThat(collection.locator("cw-collection-grid").count()).isZero();
        final int rowCount = ((Number) collection.evaluate("element => element.collectionState.rows.length")).intValue();
        assertThat(rowCount).isGreaterThan(0);
        assertThat(page.locator("html").getAttribute("data-causeway-grid-failure-classification"))
                .isEqualTo("GRID_MODULE_UNAVAILABLE");

        page.unroute(gridRoute);
        page.evaluate("""
                async () => {
                  const components = await import('/causeway-webcomponents/index.mjs');
                  components.configureCausewayGridWidgets({
                    enabled: true,
                    moduleUrl: document.documentElement.dataset.causewayGridModuleUrl + '?recovery=1'
                  });
                }
                """);
        assertCollectionPresentation("children", true);
        assertThat(((Number) collection.evaluate("element => element.collectionState.rows.length")).intValue())
                .isEqualTo(rowCount);

        page.evaluate("""
                async () => {
                  const components = await import('/causeway-webcomponents/index.mjs');
                  components.failCausewayGridWidget({
                    phase: 'renderer',
                    classification: 'GRID_RENDERER_UNAVAILABLE'
                  });
                }
                """);
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='children']\")?.dataset.causewayGridFallback === 'family-failed'");
        assertThat(collection.locator("cw-collection-grid").count()).isZero();
        assertThat(((Number) collection.evaluate("element => element.collectionState.rows.length")).intValue())
                .isEqualTo(rowCount);
        assertThat(page.locator("html").getAttribute("data-causeway-grid-failure-phase")).isEqualTo("renderer");
        assertThat(page.locator("html").getAttribute("data-causeway-grid-failure-classification"))
                .isEqualTo("GRID_RENDERER_UNAVAILABLE");
    }

    @Test
    void menuBarFailureFallsBackAcrossTiersAndRecoversWithoutAffectingOtherFamilies() {
        if (nativeToolkit()) {
            openShell();
            assertThat(page.locator("cw-menubar-control").count()).isZero();
            return;
        }
        final String menuBarRoute = "**/causeway-webcomponents/vaadin-menubar/vaadin-menubar.js";
        page.route(menuBarRoute, route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/javascript")
                .setBody("throw new Error('Intentional value-free Menu Bar family failure.');")));
        page.navigate(url("/htmx"), new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        page.waitForFunction("() => ['ready', 'partial-error'].includes(document.querySelector('cw-menubars')?.dataset.menuState)");
        try {
            page.waitForFunction("() => document.documentElement.dataset.causewayMenubarFamily === 'failed' && [...document.querySelectorAll('cw-menubar-primary, cw-menubar-secondary, cw-menubar-tertiary')].filter(element => !element.hidden).every(element => element.dataset.causewayMenubarFallback === 'family-failed' && !element.querySelector('cw-menubar-control'))");
        } catch (final com.microsoft.playwright.TimeoutError cause) {
            final var diagnostics = page.evaluate("() => ({family: document.documentElement.dataset.causewayMenubarFamily, phase: document.documentElement.dataset.causewayMenubarFailurePhase, classification: document.documentElement.dataset.causewayMenubarFailureClassification, bars: [...document.querySelectorAll('cw-menubar-primary, cw-menubar-secondary, cw-menubar-tertiary')].map(element => ({role: element.dataset.causewayBar, hidden: element.hidden, presentation: element.dataset.causewayMenubarPresentation, fallback: element.dataset.causewayMenubarFallback, control: element.querySelector('cw-menubar-control')?.dataset.widgetState}))})");
            throw new AssertionError("Menu Bar failure did not settle: " + diagnostics + "; requests=" + toolkitRequests, cause);
        }
        assertThat(page.locator("[data-causeway-menu-disclosure]").count()).isGreaterThan(0);
        assertThat(page.locator("html").getAttribute("data-causeway-menubar-failure-classification"))
                .isEqualTo("MENUBAR_MODULE_UNAVAILABLE");
        assertThat(page.locator("html").getAttribute("data-causeway-grid-family")).isNotEqualTo("failed");

        page.unroute(menuBarRoute);
        page.evaluate("""
                async () => {
                  const components = await import('/causeway-webcomponents/index.mjs');
                  components.configureCausewayMenubarWidgets({
                    enabled: true,
                    moduleUrl: document.documentElement.dataset.causewayApplicationMenubarUrl + '?recovery=1'
                  });
                }
                """);
        page.waitForFunction("() => [...document.querySelectorAll('cw-menubar-primary, cw-menubar-secondary, cw-menubar-tertiary')].filter(element => !element.hidden).every(element => element.dataset.causewayMenubarPresentation?.startsWith('vaadin-') && element.querySelector('cw-menubar-control')?.dataset.widgetState === 'ready')");
        assertThat(page.locator("[data-causeway-menu-disclosure]").count()).isZero();
        assertThat(toolkitRequests.stream().filter(url -> url.contains("/vaadin-menubar/vaadin-menubar.js")).count())
                .isBetween(1L, 2L);

        page.evaluate("""
                async () => {
                  const components = await import('/causeway-webcomponents/index.mjs');
                  components.failCausewayMenubarWidget({phase: 'event', classification: 'MENUBAR_EVENT_UNAVAILABLE'});
                }
                """);
        page.waitForFunction("() => [...document.querySelectorAll('cw-menubar-primary, cw-menubar-secondary, cw-menubar-tertiary')].filter(element => !element.hidden).every(element => element.dataset.causewayMenubarFallback === 'family-failed' && !element.querySelector('cw-menubar-control'))");
        assertThat(page.locator("html").getAttribute("data-causeway-menubar-failure-phase")).isEqualTo("event");
        assertThat(page.locator("html").getAttribute("data-causeway-menubar-failure-classification"))
                .isEqualTo("MENUBAR_EVENT_UNAVAILABLE");
        assertThat(page.locator("html").getAttribute("data-causeway-grid-family")).isNotEqualTo("failed");
    }

    @Test
    void overlappingAndRepeatedVirtualRangesRemainBoundedAndDeduplicated() {
        page.setViewportSize(1600, 900);
        openObject("demo.CollectionLayoutSortedByPage", invokeViewModel(
                "demo_CollectionLayoutMenu", "sortedBy", "rich__demo_CollectionLayoutSortedByPage"));
        assertThat(waitForCollectionOutcome("children")).isEqualTo("ready");
        if (nativeToolkit()) {
            assertCollectionPresentation("children", false);
            return;
        }
        assertCollectionPresentation("children", true);
        final long before = graphQLRequests.stream()
                .filter(request -> request.contains("CausewayReadCollectionWindow"))
                .count();
        final List<?> ranges = (List<?>) page.locator("cw-collection[id='children'] cw-collection-grid").evaluate("""
                async adapter => {
                  const provider = adapter._control.dataProvider;
                  const request = page => new Promise(resolve => provider(
                    {page, pageSize: 5},
                    (items, total) => resolve({keys: items.map(item => item.key), total})
                  ));
                  return Promise.all([request(0), request(1), request(1)]);
                }
                """);
        assertThat(graphQLRequests.stream()
                .filter(request -> request.contains("CausewayReadCollectionWindow"))
                .count() - before).isEqualTo(2);
        assertThat(ranges).hasSize(3);
        assertThat(((Map<?, ?>) ranges.get(0)).get("total")).isEqualTo(13);
        assertThat(((List<?>) ((Map<?, ?>) ranges.get(0)).get("keys"))).hasSize(5);
        assertThat(((Map<?, ?>) ranges.get(1)).get("keys"))
                .isEqualTo(((Map<?, ?>) ranges.get(2)).get("keys"));
        final long after = graphQLRequests.stream()
                .filter(request -> request.contains("CausewayReadCollectionWindow"))
                .count();
        page.locator("cw-collection[id='children'] cw-collection-grid").evaluate("""
                adapter => new Promise(resolve => adapter._control.dataProvider(
                  {page: 1, pageSize: 5},
                  (items, total) => resolve({items, total})
                ))
                """);
        assertThat(graphQLRequests.stream()
                .filter(request -> request.contains("CausewayReadCollectionWindow"))
                .count()).isEqualTo(after);
    }

    @Test
    void unavailableTotalUsesBoundedGridAndCausewayOwnedPagingWithoutInventedSize() {
        page.setViewportSize(1600, 900);
        openObject("demo.CollectionLayoutSortedByPage", invokeViewModel(
                "demo_CollectionLayoutMenu", "sortedBy", "rich__demo_CollectionLayoutSortedByPage"));
        assertThat(waitForCollectionOutcome("children")).isEqualTo("ready");
        final Locator collection = page.locator("cw-collection[id='children']");
        collection.evaluate("""
                element => {
                  const context = element._resolvedContext;
                  const delegate = context.loadCollection.bind(context);
                  context.loadCollection = async options => {
                    const result = await delegate(options);
                    const window = Object.freeze({...result.window, totalCount: null, countAvailable: false});
                    return Object.freeze({...result, window});
                  };
                }
                """);
        collection.evaluate("element => element.load({force: true, offset: 0, size: 5})");
        try {
            page.waitForFunction("() => document.querySelector(\"cw-collection[id='children']\")?.collectionState?.window?.requestedSize === 5 && document.querySelector(\"cw-collection[id='children']\")?.collectionState?.window?.totalCount == null");
        } catch (final com.microsoft.playwright.TimeoutError cause) {
            final var diagnostics = collection.evaluate("element => ({state: element.collectionState, dataset: {...element.dataset}})");
            throw new AssertionError("Nullable-total response was not accepted: " + diagnostics, cause);
        }
        assertThat(collection.evaluate("element => element.collectionState.window.countAvailable")).isEqualTo(false);
        if (nativeToolkit()) {
            assertThat(collection.getAttribute("data-causeway-grid-presentation")).isEqualTo("native");
            collection.evaluate("element => element.load({offset: 5, size: 5})");
        } else {
            page.waitForFunction("() => document.querySelector(\"cw-collection[id='children']\")?.dataset.causewayGridPresentation === 'grid-bounded'");
            assertThat(collection.innerText()).doesNotContain(" of ");
            collection.locator("[data-causeway-grid-next]").click();
        }
        page.waitForFunction("() => document.querySelector(\"cw-collection[id='children']\")?.collectionState?.window?.offset === 5");
        assertThat(collection.evaluate("element => element.collectionState.window.totalCount")).isNull();
        assertThat(collection.evaluate("element => element.collectionState.rows.length")).isEqualTo(5);
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
        if (nativeToolkit()) {
            assertThat(page.locator("[data-causeway-menu-disclosure]").first().isVisible()).isTrue();
        } else {
            assertThat(page.locator("cw-menubar-control vaadin-menu-bar").first().isVisible()).isTrue();
        }
    }

    private void openBrowserContext(final Browser.NewContextOptions options) {
        browserFailures.clear();
        toolkitRequests.clear();
        graphQLRequests.clear();
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
        page.waitForFunction("() => ['ready', 'fallback', 'unsupported', 'partial-error'].includes(document.querySelector('#causeway-route cw-object')?.dataset.layoutState)");
        waitForLogicalType(logicalType);
    }

    private void waitForMenus() {
        page.waitForFunction("() => ['ready', 'partial-error'].includes(document.querySelector('cw-menubars')?.dataset.menuState)");
        if (nativeToolkit()) {
            assertThat(page.locator("cw-menubar-control").count()).isZero();
            return;
        }
        try {
            page.waitForFunction("() => [...document.querySelectorAll('cw-menubar-primary, cw-menubar-secondary, cw-menubar-tertiary')].filter(element => !element.hidden).every(element => element.dataset.causewayMenubarPresentation?.startsWith('vaadin-') && element.querySelector('cw-menubar-control')?.dataset.widgetState === 'ready')");
        } catch (final com.microsoft.playwright.TimeoutError cause) {
            final var diagnostics = page.locator("cw-menubar-primary, cw-menubar-secondary, cw-menubar-tertiary")
                    .evaluateAll("elements => elements.map(element => ({role: element.dataset.causewayBar, hidden: element.hidden, state: element.dataset.menuState, presentation: element.dataset.causewayMenubarPresentation, responsive: element.dataset.causewayMenubarResponsive, fallback: element.dataset.causewayMenubarFallback, width: element.getBoundingClientRect().width, control: element.querySelector('cw-menubar-control')?.dataset.widgetState, error: element.querySelector('cw-menubar-control')?.dataset.widgetError}))");
            throw new AssertionError("Menu Bar did not qualify: " + diagnostics + "; requests=" + toolkitRequests, cause);
        }
    }

    private void openMenu(final String name) {
        if (!nativeToolkit()) {
            page.waitForFunction("name => [...document.querySelectorAll('cw-menubar-primary, cw-menubar-secondary, cw-menubar-tertiary')].some(host => host._projection?.menus?.some(menu => menu.label === name))", name);
            return;
        }
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

    private void activateServiceAction(final String logicalType, final String actionId) {
        if (nativeToolkit()) {
            serviceAction(logicalType, actionId).click();
            return;
        }
        final var activated = page.evaluate("""
                args => {
                  for (const host of document.querySelectorAll('cw-menubar-primary, cw-menubar-secondary, cw-menubar-tertiary')) {
                    const descriptor = Object.values(host._projection?.actions ?? {}).find(action => action.serviceLogicalTypeName === args.logicalType && action.actionId === args.actionId);
                    const control = host.querySelector('vaadin-menu-bar');
                    if (descriptor && control) {
                      const find = items => {
                        for (const item of items ?? []) {
                          if (item.causewayKey === descriptor.key) return item;
                          const nested = find(item.children);
                          if (nested) return nested;
                        }
                        return null;
                      };
                      const item = find(control.items);
                      if (!item || item.disabled) return false;
                      control.dispatchEvent(new CustomEvent('item-selected', {detail: {value: item}}));
                      return true;
                    }
                  }
                  return false;
                }
                """, Map.of("logicalType", logicalType, "actionId", actionId));
        assertThat(activated).isEqualTo(true);
    }

    private void assertOrdinaryActionPresentation(final String member) {
        final String selector = "cw-action[id='" + member + "'] [data-causeway-action-control]";
        final Locator control = page.locator(selector).first();
        control.waitFor();
        if (nativeToolkit()) {
            assertThat(control.evaluate("element => element.localName")).isEqualTo("button");
            return;
        }
        assertThat(control.evaluate("element => element.localName")).isEqualTo("cw-action-control");
        page.waitForFunction("selector => document.querySelector(selector)?.dataset.widgetState === 'ready'", selector);
        assertThat(control.locator("vaadin-button").count()).isEqualTo(1);
        assertThat(toolkitRequests.stream().filter(url -> url.contains("/vaadin-actions/vaadin-actions.js")).count())
                .isEqualTo(1);
    }

    private Locator objectAction(final String member) {
        final Locator host = page.locator("cw-action[id='" + member + "']").first();
        final Locator action = host.locator("[data-causeway-action-control]");
        action.waitFor();
        action.scrollIntoViewIfNeeded();
        return action;
    }

    private void waitForLogicalType(final String logicalType) {
        try {
            page.waitForFunction("logicalType => { const context = document.querySelector('#causeway-route cw-object-context'); const route = document.querySelector(\"[data-testid='causeway-route-page']\"); return context?.getAttribute('logical-type') === logicalType && ['ready', 'partial-error'].includes(route?.dataset.routeState); }", logicalType);
        } catch (com.microsoft.playwright.TimeoutError cause) {
            final Object state = page.evaluate("() => ({url: location.href, page: document.querySelector(\"[data-testid='causeway-route-page']\")?.dataset, context: document.querySelector('#causeway-route cw-object-context')?.outerHTML, prompt: document.querySelector(\"dialog[data-testid='action-prompt']\")?.outerHTML, failures: globalThis.__referenceAppFailures})");
            throw new AssertionError("Expected logical type " + logicalType + "; state=" + state, cause);
        }
    }

    private void waitForCollectionRows(final String member) {
        assertThat(waitForCollectionOutcome(member)).isEqualTo("ready");
        assertThat(page.locator("cw-collection[id='" + member + "'] tbody tr").count()).isGreaterThan(0);
    }

    private String waitForCollectionOutcome(final String member) {
        page.waitForFunction("member => ['ready', 'partial-error', 'error'].includes(document.querySelector(`cw-collection[id='${member}']`)?.collectionState?.status)", member);
        return (String) page.locator("cw-collection[id='" + member + "']")
                .evaluate("element => element.collectionState.status");
    }

    private void assertCollectionPresentation(final String member, final boolean expectedGrid) {
        final Locator collection = page.locator("cw-collection[id='" + member + "']");
        if (nativeToolkit() || !expectedGrid) {
            assertThat(collection.getAttribute("data-causeway-grid-presentation")).isEqualTo("native");
            assertThat(collection.locator("cw-collection-grid").count()).isZero();
            return;
        }
        try {
            page.waitForFunction("member => { const collection = document.querySelector(`cw-collection[id='${member}']`); return collection?.dataset?.causewayGridPresentation?.startsWith('grid-') && collection.querySelector('cw-collection-grid')?.dataset.widgetState === 'ready'; }", member);
        } catch (final com.microsoft.playwright.TimeoutError cause) {
            final var diagnostics = collection.evaluate("element => ({dataset: {...element.dataset}, width: element.getBoundingClientRect().width, state: element.collectionState?.status, window: element.collectionState?.window, columns: element.columns})");
            throw new AssertionError("Collection " + member + " did not qualify for Grid: " + diagnostics, cause);
        }
        assertThat(collection.locator("cw-collection-grid").count()).isEqualTo(1);
        assertThat(toolkitRequests.stream().anyMatch(request -> request.contains("/vaadin-grid/vaadin-grid.js"))).isTrue();
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
                    + "return editor?.matches('cw-reference-editor, input[list]') "
                    + "|| editor?.querySelector('cw-reference-editor, input[list]'); }", parameter(parameterId));
        } catch (final com.microsoft.playwright.TimeoutError cause) {
            throw new AssertionError("Autocomplete editor was unavailable: " + page.locator(PROMPT).innerHTML(), cause);
        }
        if (!Boolean.TRUE.equals(control.evaluate("element => element.localName === 'cw-reference-editor'"))) {
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

    private void assertReadOnlyPresentation(final Locator property, final String vaadinTag) {
        property.waitFor();
        final String propertySelector = "cw-property[id='" + property.getAttribute("id") + "']";
        page.waitForFunction("selector => Boolean(document.querySelector(selector)?.dataset.renderer)", propertySelector);
        final Locator adapter = property.locator("cw-field-editor[data-mode='view']");
        if (nativeToolkit() || vaadinTag == null) {
            assertThat(adapter.count()).isZero();
            assertThat(property.getAttribute("data-renderer")).isNotEqualTo("vaadin-field-view");
            return;
        }
        if (adapter.count() == 0) {
            assertThat(property.locator(".causeway-value-null").count())
                    .as("Only null values may retain native presentation for a qualified family")
                    .isEqualTo(1);
            return;
        }
        page.waitForFunction("selector => document.querySelector(selector)?.dataset.widgetState === 'ready'",
                propertySelector + " cw-field-editor[data-mode='view']");
        final Locator control = adapter.locator(vaadinTag);
        assertThat(control.count()).isEqualTo(1);
        assertThat(control.evaluate("element => Boolean(element.readOnly || element.hasAttribute('readonly'))"))
                .isEqualTo(true);
        assertThat(property.locator("cw-field-editor[data-mode='view']").count()).isEqualTo(1);
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
        final Locator property = page.locator("cw-property[id='" + member + "']");
        final String readOnlyVaadinTag = entityLogicalType.contains("Boolean") ? "vaadin-checkbox" : vaadinTag;
        assertReadOnlyPresentation(property, readOnlyVaadinTag);
        property.locator("[data-causeway-action='edit']").click();
        final Locator editor = resolveEditor("cw-property[id='" + member + "'] [data-causeway-editor='" + member + "']");
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
        final Locator property = page.locator("cw-property[id='" + member + "']");
        assertReadOnlyPresentation(property, vaadinTag);
        property.locator("[data-causeway-action='edit']").click();
        final Locator editor = resolveEditor("cw-property[id='" + member + "'] [data-causeway-editor='" + member + "']");
        final String localName = String.valueOf(editor.evaluate("element => element.localName"));
        if (vaadinTag != null && fieldFamiliesEnabled()) {
            assertThat(localName).isEqualTo(vaadinTag);
            assertThat(fieldAssetRequests("local-temporal")).isEqualTo(1);
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
                "causeway.viewer.webcomponents.htmx.component-toolkit",
                System.getProperty("causeway.viewer.webcomponents.htmx.editor-toolkit", "vaadin")));
    }

    private boolean fieldFamiliesEnabled() {
        return !String.valueOf(page.locator("html").getAttribute("data-causeway-field-families")).isBlank()
                && !"null".equals(String.valueOf(page.locator("html").getAttribute("data-causeway-field-families")));
    }

    private void assertSemanticAccessibility() {
        final Map<?, ?> result = (Map<?, ?>) page.evaluate("""
                () => {
                  const ids = [...document.body.querySelectorAll('[id]')].map(element => element.id);
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
