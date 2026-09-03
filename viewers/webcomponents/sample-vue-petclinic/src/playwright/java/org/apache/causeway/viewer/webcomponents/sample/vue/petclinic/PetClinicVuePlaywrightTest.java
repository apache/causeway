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
        page = browser.newPage(new Browser.NewPageOptions().setViewportSize(1440, 900));
        page.onPageError(error -> browserFailures.add(error));
        page.onConsoleMessage(message -> {
            if ("error".equals(message.type()) && !message.text().contains("favicon.ico")) {
                browserFailures.add(message.text());
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
    void semanticNavigationUsesGenericFallbackAndBrowserHistory() {
        open("/vue/object/petclinic.PetOwner/s_owner-mary");
        page.locator("[data-route-state='ready']").waitFor();
        page.evaluate("""
                document.querySelector('[data-testid="petclinic-vue-application-shell"]').dispatchEvent(
                  new CustomEvent('causeway-navigation-request', {
                    bubbles: true,
                    composed: true,
                    cancelable: true,
                    detail: {target: {logicalTypeName: 'petclinic.Pet', id: 's_pet-basil'}}
                  }))
                """);
        page.waitForURL("**/vue/object/petclinic.Pet/s_pet-basil");
        page.locator("[data-page-kind='generic'][data-route-state='ready']").waitFor();
        page.waitForFunction("document.body.innerText.includes('Basil')");
        assertThat(page.locator("body").innerText()).contains("Basil");

        page.goBack();
        page.locator("[data-page-kind='pet-owner'][data-route-state='ready']").waitFor();
        assertThat(page.url()).endsWith("/vue/object/petclinic.PetOwner/s_owner-mary");

        page.goForward();
        page.locator("[data-page-kind='generic'][data-route-state='ready']").waitFor();
        assertThat(page.url()).endsWith("/vue/object/petclinic.Pet/s_pet-basil");
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
    void directRefreshAndInvalidRoutesRemainWithinTheApplicationShell() {
        open("/vue/object/petclinic.Pet/s_pet-basil");
        page.locator("[data-page-kind='generic'][data-route-state='ready']").waitFor();
        page.reload();
        page.locator("[data-page-kind='generic'][data-route-state='ready']").waitFor();
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
            assertThat(page.locator("header").count()).isEqualTo(1);
            assertThat(page.locator("main#causeway-vue-route").count()).isEqualTo(1);
            assertThat(page.locator("footer").count()).isEqualTo(1);
            page.locator("cw-menubars[data-menu-state='ready']").waitFor();
            assertThat(page.locator("cw-menubars[data-menu-state='ready']").count()).isEqualTo(1);
            final var skipLink = page.locator("a[href='#causeway-vue-route']");
            assertThat(skipLink.innerText()).isEqualTo("Skip to main content");
            skipLink.focus();
            page.keyboard().press("Enter");
            assertThat(page.evaluate("document.activeElement?.id")).isEqualTo("causeway-vue-route");
        }
    }

    private void open(final String path) {
        page.navigate("http://localhost:" + port + path);
        page.locator("[data-testid='petclinic-vue-application-shell']").waitFor();
    }
}
