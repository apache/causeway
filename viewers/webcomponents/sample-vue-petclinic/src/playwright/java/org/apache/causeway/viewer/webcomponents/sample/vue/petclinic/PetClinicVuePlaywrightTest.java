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
package org.apache.causeway.viewer.webcomponents.sample.vue.petclinic;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = PetClinicVueApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PetClinicVuePlaywrightTest {

    @LocalServerPort
    private int port;

    private final List<String> browserFailures = new ArrayList<>();
    private final List<String> graphQLRequests = new ArrayList<>();
    private Playwright playwright;
    private Browser browser;
    private Page page;

    @BeforeAll
    void startBrowser() {
        playwright = Playwright.create();
        final var options = new BrowserType.LaunchOptions()
                .setHeadless(Boolean.parseBoolean(System.getProperty("playwright.headless", "true")));
        final var executable = System.getProperty("playwright.chromium.executable", "").trim();
        if (!executable.isEmpty()) {
            options.setExecutablePath(Path.of(executable));
        }
        browser = playwright.chromium().launch(options);
    }

    @AfterAll
    void stopBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void openPage() {
        browserFailures.clear();
        graphQLRequests.clear();
        page = browser.newPage(new Browser.NewPageOptions().setViewportSize(1440, 900));
        page.onPageError(error -> browserFailures.add(error));
        page.onConsoleMessage(message -> {
            if ("error".equals(message.type()) && !message.text().contains("favicon.ico")) {
                browserFailures.add("console: " + message.text());
            }
        });
        page.onRequest(request -> {
            if (request.url().contains("/graphql") && request.postData() != null) {
                graphQLRequests.add(request.postData());
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
        assertThat(browserFailures).isEmpty();
        page.close();
    }

    @Test
    void customOwnerPageUsesOneStableShellAndDeclarativeRouteBoundary() {
        open("/vue/object/petclinic.PetOwner/s_owner-mary");
        page.locator("[data-causeway-route-page][data-route-state='ready']").waitFor();

        assertThat(page.locator("[data-testid='petclinic-vue-application-shell']").count()).isEqualTo(1);
        assertThat(page.locator("cw-graphql-client[data-causeway-shell-client]").count()).isEqualTo(1);
        assertThat(page.locator("[data-causeway-route-page][data-route-state='ready']").count()).isEqualTo(1);
        assertThat(page.locator("cw-object-context[data-causeway-route-context]").count()).isEqualTo(1);
        assertThat(page.locator("cw-interaction-controller[data-causeway-route-interactions]").count()).isEqualTo(1);
        assertThat(page.locator("body").innerText()).contains("Mary Smith (Mary)", "Pets currently registered");
    }

    @Test
    void authoredPdfModesRemainFrameworkNeutralAndProgressive() {
        open("/vue/object/petclinic.Visit/s_visit-basil-checkup");
        page.waitForFunction("() => document.querySelector('cw-property#pdfLink')?.dataset.renderer === 'blob'");
        final var linked = page.locator("cw-property#pdfLink");
        assertThat(linked.getAttribute("data-renderer")).isEqualTo("blob");
        assertThat(linked.locator("[data-causeway-pdf-reader]").count()).isZero();
        final var linkedHref = linked.locator(".causeway-value-lob-link").getAttribute("href");
        assertThat((Boolean) page.evaluate("href => performance.getEntriesByType('resource').some(entry => entry.name === new URL(href, document.baseURI).href)", linkedHref)).isFalse();
        assertThat((Boolean) page.evaluate("() => performance.getEntriesByType('resource').some(entry => entry.name.includes('/pdfjs/'))")).isFalse();

        open("/vue/object/petclinic.Pet/s_pet-basil");
        page.waitForFunction("() => document.querySelector('cw-property#pdfManual [data-causeway-pdf-reader]')?.dataset.causewayPdfState === 'inactive'");
        final var manual = page.locator("cw-property#pdfManual [data-causeway-pdf-reader]");
        assertThat(manual.getAttribute("data-causeway-pdf-state")).isEqualTo("inactive");
        final var manualHref = manual.locator(".causeway-value-lob-link").getAttribute("href");
        assertThat((Boolean) page.evaluate("href => performance.getEntriesByType('resource').some(entry => entry.name === new URL(href, document.baseURI).href)", manualHref)).isFalse();
        assertThat((Boolean) page.evaluate("() => performance.getEntriesByType('resource').some(entry => entry.name.includes('/pdfjs/'))")).isFalse();
        manual.locator("[data-causeway-pdf-activate]").click();
        page.waitForFunction("() => document.querySelector('cw-property#pdfManual [data-causeway-pdf-reader]')?.dataset.causewayPdfState === 'ready'");
        assertThat(manual.locator("[data-causeway-pdf-status]").innerText()).contains("Page 2 of 3");
        assertThat((Boolean) page.evaluate("() => performance.getEntriesByType('resource').some(entry => entry.name.includes('/pdfjs/pdf.min.mjs'))")).isTrue();

        open("/vue/object/petclinic.PetOwner/s_owner-mary");
        page.waitForFunction("() => document.querySelector('cw-property#pdfAuto [data-causeway-pdf-reader]')?.dataset.causewayPdfState === 'ready'");
        final var automatic = page.locator("cw-property#pdfAuto [data-causeway-pdf-reader]");
        assertThat(automatic.locator("[data-causeway-pdf-page]").count()).isEqualTo(3);
        automatic.locator(".causeway-pdf-page-canvas").first().waitFor();
        automatic.locator("[data-causeway-pdf-page='3']").scrollIntoViewIfNeeded();
        automatic.locator("[data-causeway-pdf-page='3'] canvas").waitFor();
        assertThat(automatic.locator(".causeway-value-lob-link").getAttribute("href")).isNotBlank();
    }

    @Test
    void semanticNavigationUsesExactPagesGenericFallbackAndBrowserHistory() {
        open("/vue/object/petclinic.Pet/s_pet-basil");
        page.locator("[data-page-kind='pet'][data-route-state='ready']").waitFor();
        assertThat(page.locator("body").innerText()).contains("Basil");

        navigateTo("petclinic.ViewerFallback", "s_viewer-fallback");
        page.waitForURL("**/vue/object/petclinic.ViewerFallback/s_viewer-fallback");
        page.locator("[data-page-kind='generic'][data-route-state='ready']").waitFor();
        page.waitForFunction("() => document.body.innerText.includes('Generic viewer fallback')");
        assertThat(page.locator("body").innerText()).contains("Generic viewer fallback", "Message");

        page.goBack();
        page.locator("[data-page-kind='pet'][data-route-state='ready']").waitFor();
        assertThat(page.url()).endsWith("/vue/object/petclinic.Pet/s_pet-basil");

        page.goForward();
        page.locator("[data-page-kind='generic'][data-route-state='ready']").waitFor();
        assertThat(page.url()).endsWith("/vue/object/petclinic.ViewerFallback/s_viewer-fallback");
    }

    @Test
    void exactPetclinicPagesMatchTheHtmxSemanticComposition() {
        open("/vue/");
        page.locator("[data-page-kind='home'][data-route-state='ready']").waitFor();
        page.waitForFunction("() => document.querySelector('cw-collection#petOwners')?.collectionState?.rows?.length === 5");
        page.waitForFunction("() => document.querySelector('cw-collection#futureVisits')?.collectionState?.rows?.length === 10");
        assertThat(page.locator(".petclinic-dashboard-grid > section > h2")
                .evaluateAll("elements => elements.map(element => element.textContent.trim()).join(',')"))
                .isEqualTo("Pet owners,Upcoming visits");
        assertThat(page.locator("cw-collection#petOwners").getAttribute("paged")).isEqualTo("5");
        assertThat(page.locator("cw-collection#futureVisits").getAttribute("paged")).isEqualTo("10");
        assertThat(page.locator("cw-collection#petOwners > cw-preview").count()).isEqualTo(1);
        assertThat(page.locator("cw-collection#futureVisits > cw-preview").count()).isEqualTo(1);
        assertThat(page.locator("cw-collection#petOwners [data-causeway-preview-toggle]").count()).isGreaterThan(0);
        assertThat(page.title()).endsWith(" · Pet Clinic");

        open("/vue/object/petclinic.PetOwner/s_owner-mary");
        page.locator("[data-page-kind='pet-owner'][data-route-state='ready']").waitFor();
        page.waitForFunction("() => document.querySelector('.petclinic-object-collections > section:last-child cw-collection#visits')?.collectionState?.rows?.length === 2");
        assertThat(page.locator(".petclinic-owner-page h2")
                .evaluateAll("elements => elements.map(element => element.textContent.trim()).join(',')"))
                .isEqualTo("Identity,Contact,Details,Pets,Companion animals,Visits,Visit history,Documents");
        assertThat(page.locator(".petclinic-page-toolbar > cw-action")
                .evaluateAll("elements => elements.map(element => element.id).join(',')"))
                .isEqualTo("allOwners,noOwners,relatedOwners,delete");
        assertThat(page.locator("cw-action#relatedOwners cw-standalone-collection > cw-collection-column")
                .evaluateAll("elements => elements.map(element => element.id).join(',')"))
                .isEqualTo("name,knownAs,notes");
        assertThat(page.locator(".petclinic-object-details cw-property")
                .evaluateAll("elements => elements.map(element => element.id).join(',')"))
                .isEqualTo("name,knownAs,telephoneNumber,emailAddress,notes,lastVisit,daysSinceLastVisit");
        assertThat(page.locator(".petclinic-object-collections > section").count()).isEqualTo(2);
        assertThat(page.locator("cw-collection#visits > cw-collection-column")
                .evaluateAll("elements => elements.map(element => element.id).join(',')"))
                .isEqualTo("visitAt,reason,notes");
        assertThat(page.locator("cw-collection#visits").getAttribute("paged")).isEqualTo("8");
        assertThat(page.locator("cw-collection#visits > cw-preview").count()).isEqualTo(1);
        assertThat(page.locator("cw-collection#visits [data-causeway-preview-toggle]").count()).isGreaterThan(0);

        open("/vue/object/petclinic.Pet/s_pet-basil");
        page.locator("[data-page-kind='pet'][data-route-state='ready']").waitFor();
        assertThat(page.locator(".petclinic-pet-page h2")
                .evaluateAll("elements => elements.map(element => element.textContent.trim()).join(',')"))
                .isEqualTo("Identity,Details,Documents");
        assertThat(page.locator(".petclinic-pet-page cw-property")
                .evaluateAll("elements => elements.map(element => element.id).join(',')"))
                .isEqualTo("petOwner,name,species,notes,pdfManual");
        assertThat(page.locator(".petclinic-pet-page cw-property#id, .petclinic-pet-page cw-property#version").count()).isZero();

        open("/vue/object/petclinic.Visit/s_visit-basil-checkup");
        page.locator("[data-page-kind='visit'][data-route-state='ready']").waitFor();
        assertThat(page.locator(".petclinic-visit-page h2")
                .evaluateAll("elements => elements.map(element => element.textContent.trim()).join(',')"))
                .isEqualTo("Appointment,Details,Documents");
        assertThat(page.locator(".petclinic-visit-page cw-property")
                .evaluateAll("elements => elements.map(element => element.id).join(',')"))
                .isEqualTo("pet,visitAt,reason,notes,pdfLink");
        assertThat(page.locator(".petclinic-visit-page cw-property#id, .petclinic-visit-page cw-property#version").count()).isZero();
    }

    @Test
    void shellAndOwnerLayoutMatchTheWideAndNarrowReferenceInvariants() {
        open("/vue/object/petclinic.PetOwner/s_owner-mary");
        page.locator("[data-route-state='ready']").waitFor();
        page.locator("cw-menubars[data-menu-state='ready']").waitFor();

        final var wide = (List<Number>) page.locator("[data-testid='petclinic-vue-application-shell']").evaluate("""
                shell => {
                  const header = shell.querySelector('header').getBoundingClientRect();
                  const main = shell.querySelector('main').getBoundingClientRect();
                  const details = shell.querySelector('.petclinic-object-details').getBoundingClientRect();
                  const collections = shell.querySelector('.petclinic-object-collections').getBoundingClientRect();
                  return [header.height, main.left, details.left, details.right, collections.left, collections.top - details.top,
                    document.documentElement.scrollWidth - document.documentElement.clientWidth];
                }
                """);
        assertThat(wide.get(0).doubleValue()).isBetween(50.0, 54.0);
        assertThat(wide.get(1).doubleValue()).isBetween(15.0, 17.0);
        assertThat(wide.get(4).doubleValue()).isGreaterThanOrEqualTo(wide.get(3).doubleValue());
        assertThat(Math.abs(wide.get(5).doubleValue())).isLessThan(1.0);
        assertThat(wide.get(6).intValue()).isZero();
        assertThat(page.locator("header.causeway-shell-header").evaluate("element => getComputedStyle(element).backgroundColor"))
                .isEqualTo("rgb(23, 105, 170)");
        assertThat(page.locator("body").evaluate("element => getComputedStyle(element).fontFamily"))
                .asString().contains("Inter");
        assertThat(page.locator("cw-menubars").innerText()).containsSubsequence("Pet Owners", "Visits", "Account");
        assertThat(page.locator("cw-menubars").innerText()).doesNotContain("System");
        assertThat(page.locator("cw-menubar-secondary").count()).isZero();
        assertThat(page.locator("cw-menubar-tertiary").evaluate(
                "element => element._projection.menus.map(menu => menu.label).join(',')")).isEqualTo("Account");
        assertThat(page.locator("footer").innerText()).contains("Powered by Apache Causeway", "Vue viewer");
        assertThat(page.locator(".causeway-object-identity").isVisible()).isFalse();
        assertThat(page.locator("[data-causeway-route-page]").evaluate("element => getComputedStyle(element).outlineStyle"))
                .isEqualTo("none");

        page.setViewportSize(500, 900);
        page.waitForFunction("() => { const grid = document.querySelector('.petclinic-object-grid'); return getComputedStyle(grid).gridTemplateColumns.split(' ').length === 1; }");
        final var narrow = (List<Number>) page.locator("[data-testid='petclinic-vue-application-shell']").evaluate("""
                shell => {
                  const details = shell.querySelector('.petclinic-object-details').getBoundingClientRect();
                  const collections = shell.querySelector('.petclinic-object-collections').getBoundingClientRect();
                  const footer = shell.querySelector('footer');
                  return [collections.top - details.bottom,
                    document.documentElement.scrollWidth - document.documentElement.clientWidth,
                    getComputedStyle(footer).flexDirection === 'column' ? 1 : 0];
                }
                """);
        assertThat(narrow.get(0).doubleValue()).isGreaterThanOrEqualTo(0.0);
        assertThat(narrow.get(1).intValue()).isZero();
        assertThat(narrow.get(2).intValue()).isEqualTo(1);
    }

    @Test
    void scalarActionResultsUseTheActivePageOutlet() {
        open("/vue/object/petclinic.PetOwner/s_owner-mary");
        page.locator("[data-route-state='ready']").waitFor();
        page.evaluate("""
                const source = document.querySelector('cw-action#allOwners');
                source.dispatchEvent(new CustomEvent('causeway-action-request', {
                  bubbles: true, composed: true, detail: {actionId: 'allOwners'}
                }));
                source.dispatchEvent(new CustomEvent('causeway-action-result', {
                  bubbles: true, composed: true,
                  detail: {actionId: 'allOwners', result: {kind: 'scalar', value: 'Completed in Vue'}}
                }))
                """);
        page.waitForFunction("document.body.innerText.includes('Completed in Vue')");
        assertThat(page.locator("cw-action-results[data-causeway-page-result]").innerText())
                .contains("allOwners result", "Completed in Vue");
    }

    @Test
    void frameworkLogoutFailsClosedWhileApplicationActionsAndLocalResourcesRemainAvailable() {
        open("/vue/object/petclinic.ViewerFallback/s_viewer-fallback");
        page.locator("[data-page-kind='generic'][data-route-state='ready']").waitFor();
        page.waitForFunction("() => document.querySelector('cw-object cw-action#openLocalResource')?.componentState?.status === 'ready'");
        page.locator("cw-menubars[data-menu-state='ready']").waitFor();

        final var accountMenu = page.locator("vaadin-menu-bar-button")
                .filter(new com.microsoft.playwright.Locator.FilterOptions().setHasText("Account")).first();
        accountMenu.waitFor();
        accountMenu.click();
        final var accountOverlay = page.locator("vaadin-menu-bar-overlay[opened]");
        accountOverlay.waitFor();
        assertThat(accountOverlay.innerText()).doesNotContain("Logout");
        page.keyboard().press("Escape");

        final var logoutInvocationsBefore = graphQLRequests.stream()
                .filter(body -> body.contains("CausewayInvokeServiceAction") && body.contains("LogoutMenu"))
                .count();
        final var logoutAnnouncement = (String) page.locator("[data-testid='petclinic-vue-application-shell']").evaluate("element => { element.dispatchEvent(new CustomEvent('causeway-action-request', { bubbles: true, composed: true, cancelable: true, detail: { serviceLogicalTypeName: 'causeway.security.LogoutMenu', actionId: 'logout', context: {} } })); return element.querySelector('[data-causeway-route-announcement]').textContent; }");
        page.waitForTimeout(50);
        assertThat(graphQLRequests.stream()
                .filter(body -> body.contains("CausewayInvokeServiceAction") && body.contains("LogoutMenu"))
                .count()).isEqualTo(logoutInvocationsBefore);
        assertThat(logoutAnnouncement).contains("host authentication");

        page.locator("cw-object cw-action#logout [data-causeway-action-control]").click();
        page.waitForFunction("() => document.body.innerText.includes('Application action completed')");
        assertThat(page.locator("cw-action-results:not([hidden])").innerText()).contains("Application action completed");

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
    void directRefreshAndInvalidRoutesRemainWithinTheApplicationShell() {
        open("/vue/object/petclinic.Pet/s_pet-basil");
        page.locator("[data-page-kind='pet'][data-route-state='ready']").waitFor();
        page.reload();
        page.locator("[data-page-kind='pet'][data-route-state='ready']").waitFor();
        assertThat(page.locator("cw-graphql-client[data-causeway-shell-client]").count()).isEqualTo(1);

        open("/vue/invalid-route");
        page.locator("[data-route-state='invalid-route']").waitFor();
        assertThat(page.locator("[data-route-state='invalid-route']").count()).isEqualTo(1);
        assertThat(page.locator("body").innerText()).contains("Invalid route");

        open("/vue/object/petclinic.Pet/s_pet-does-not-exist");
        page.locator("[data-route-state='unavailable']").waitFor();
        assertThat(page.locator("body").innerText()).doesNotContain("authorization", "stack trace");
    }

    @Test
    void bothToolkitPoliciesPreserveAccessibleShellLandmarksAndMenus() {
        for (final String toolkit : List.of("native", "vaadin")) {
            open("/vue/?toolkit=" + toolkit);
            assertThat(page.locator("html").getAttribute("data-causeway-component-toolkit")).isEqualTo(toolkit);
            assertThat(page.locator("header.causeway-shell-header").count()).isEqualTo(1);
            assertThat(page.locator("main#causeway-vue-route").count()).isEqualTo(1);
            assertThat(page.locator("footer").count()).isEqualTo(1);
            page.locator("cw-menubars[data-menu-state='ready']").waitFor();
            assertThat(page.locator("cw-menubars[data-menu-state='ready']").count()).isEqualTo(1);
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
            final var skipLink = page.locator("a[href='#causeway-vue-route']");
            assertThat(skipLink.innerText()).isEqualTo("Skip to main content");
            skipLink.focus();
            page.keyboard().press("Enter");
            assertThat(page.evaluate("document.activeElement?.id")).isEqualTo("causeway-vue-route");
        }
    }

    private void navigateTo(final String logicalTypeName, final String id) {
        page.evaluate("""
                ([logicalTypeName, id]) => document.querySelector('[data-testid="petclinic-vue-application-shell"]').dispatchEvent(
                  new CustomEvent('causeway-navigation-request', {
                    bubbles: true,
                    composed: true,
                    cancelable: true,
                    detail: {target: {logicalTypeName, id}}
                  }))
                """, List.of(logicalTypeName, id));
    }

    private void open(final String path) {
        page.navigate("http://localhost:" + port + path);
        page.locator("[data-testid='petclinic-vue-application-shell']").waitFor();
    }
}
