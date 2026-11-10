/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.extensions.d3.wkt.integration.components;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serial;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.apache.causeway.extensions.d3.applib.config.D3Config;
import org.apache.causeway.extensions.d3.wkt.integration.D3TestApplication;
import org.apache.causeway.extensions.d3.wkt.integration.callback.NodeClickedCallback;
import org.apache.causeway.extensions.d3.wkt.integration.callback.NodeClickedEvent;

/**
 * Verifies the head rendering of the integration {@link D3Panel}: the D3 webjar and
 * the D3 integration module are referenced, the graph config is emitted as an
 * on-DOM-ready {@code WicketStuff.D3.init(...)} call, and (when a node-click callback
 * is bound) the callback URL is included in the emitted config.
 */
@DisplayName("D3Panel — head rendering")
class D3PanelRenderHeadTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(new D3TestApplication());
    }

    @AfterEach
    void tearDown() {
        tester.destroy();
    }

    private static final class NoopCallback extends NodeClickedCallback {
        @Serial private static final long serialVersionUID = 1L;
        @Override
        protected void onNodeClicked(final NodeClickedEvent event, final AjaxRequestTarget target) {
        }
    }

    @Test
    @DisplayName("emits the D3 webjar, the integration module and the init(...) call")
    void renderHead_emitsWebjarAndIntegrationScripts_andInitCall() {
        var panel = new D3Panel("d3", D3Config.defaults().withGraphData("{\"nodes\":[],\"links\":[]}"));

        tester.startPage(new D3TestPage(panel));

        var response = tester.getLastResponseAsString();
        assertThat(response).contains("webjars/d3/");           // D3Reference (webjar)
        assertThat(response).contains("d3-integration");        // D3IntegrationReference
        assertThat(response).contains(".mjs");                  // ... rendered as an ES module
        assertThat(response).contains("WicketStuff.D3.init(");  // on-DOM-ready config script
        assertThat(response).contains("graphData");
    }

    @Test
    @DisplayName("includes the callback URL in the config when a click callback is bound")
    void renderHead_includesCallbackUrl_whenClickCallbackBound() {
        var panel = new D3Panel("d3", D3Config.defaults()).withClickCallback(new NoopCallback());

        tester.startPage(new D3TestPage(panel));

        var response = tester.getLastResponseAsString();
        assertThat(response).contains("clickCallbackUrl");
        // the placeholder token is present (escaped for inline-script safety)
        assertThat(response).contains("PLACEHOLDER");
    }

    @Test
    @DisplayName("omits the callback URL when no click callback is bound")
    void renderHead_omitsCallbackUrl_whenNoClickCallbackBound() {
        var panel = new D3Panel("d3", D3Config.defaults());

        tester.startPage(new D3TestPage(panel));

        var response = tester.getLastResponseAsString();
        assertThat(response).contains("WicketStuff.D3.init(");
        assertThat(response).doesNotContain("PLACEHOLDER");
    }
}
