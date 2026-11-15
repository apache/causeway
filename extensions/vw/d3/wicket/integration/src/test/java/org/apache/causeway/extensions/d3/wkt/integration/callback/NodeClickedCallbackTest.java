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
package org.apache.causeway.extensions.d3.wkt.integration.callback;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import org.apache.causeway.extensions.d3.applib.config.D3Config;
import org.apache.causeway.extensions.d3.wkt.integration.D3TestApplication;
import org.apache.causeway.extensions.d3.wkt.integration.components.D3Panel;

/**
 * Verifies the AJAX request handling of {@link NodeClickedCallback}: it invokes
 * {@code onNodeClicked} with the {@code nodeId} request parameter, and ignores the
 * request (without invoking) when that parameter is absent or empty.
 */
@DisplayName("NodeClickedCallback — AJAX request handling")
class NodeClickedCallbackTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(new D3TestApplication());
    }

    @AfterEach
    void tearDown() {
        tester.destroy();
    }

    /** Records the node-click event it was invoked with. */
    private static final class RecordingCallback extends NodeClickedCallback {
        @Serial private static final long serialVersionUID = 1L;
        final AtomicReference<NodeClickedEvent> received = new AtomicReference<>();
        @Override
        protected void onNodeClicked(final NodeClickedEvent event, final AjaxRequestTarget target) {
            received.set(event);
        }
    }

    private void startPanelWith(final NodeClickedCallback callback) {
        tester.startComponentInPage(new D3Panel("d3", D3Config.defaults()).withClickCallback(callback));
    }

    private static Url callbackUrl(final NodeClickedCallback callback, final String nodeIdOrNull) {
        // mirror the client-side script: drop the placeholder token, then append the node id
        var raw = callback.getCallbackUrl().toString().replace(D3AjaxCallback.PLACEHOLDER, "");
        var url = Url.parse(raw, StandardCharsets.UTF_8);
        if (nodeIdOrNull != null) {
            url.getQueryParameters().add(new Url.QueryParameter("nodeId", nodeIdOrNull));
        }
        return url;
    }

    @Test
    @DisplayName("invokes onNodeClicked(...) with the nodeId request parameter")
    void invokesOnNodeClicked_withNodeId() {
        var callback = new RecordingCallback();
        startPanelWith(callback);

        tester.executeAjaxUrl(callbackUrl(callback, "node-42"));

        assertThat(callback.received.get()).isNotNull();
        assertThat(callback.received.get().nodeId()).isEqualTo("node-42");
    }

    @ParameterizedTest(name = "ignores request when nodeId is [{0}]")
    @NullAndEmptySource
    @DisplayName("ignores the request (without invoking) when nodeId is absent or empty")
    void ignores_whenNodeIdAbsentOrEmpty(final String nodeId) {
        var callback = new RecordingCallback();
        startPanelWith(callback);

        tester.executeAjaxUrl(callbackUrl(callback, nodeId));

        assertThat(callback.received.get()).isNull();
    }
}
