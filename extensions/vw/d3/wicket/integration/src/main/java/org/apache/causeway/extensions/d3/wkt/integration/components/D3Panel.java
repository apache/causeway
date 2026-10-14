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

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.panel.Panel;

import org.apache.causeway.extensions.d3.applib.config.D3Config;
import org.apache.causeway.extensions.d3.applib.config.D3Config;
import org.apache.causeway.extensions.d3.wkt.integration.callback.NodeClickedCallback;
import org.apache.causeway.extensions.d3.wkt.integration.res.D3IntegrationReference;
import org.apache.causeway.extensions.d3.wkt.integration.res.D3IntegrationReference;
import org.apache.causeway.extensions.d3.wkt.integration.res.D3Reference;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import org.jspecify.annotations.NonNull;

/**
 * A panel for rendering D3.js interactive graphs inline in the page.
 * <p>
 * Contains an SVG canvas (a {@link WebComponent} with a markup id for the D3
 * visualization) and wires the D3 integration script that initialises the graph
 * using the configuration provided.
 * git status</p>
 *
 * @since 2.0 {@index}
 */
public class D3Panel extends Panel {

    private static final long serialVersionUID = 1L;

    private final static String ID_D3_CANVAS = "d3Canvas";

    private D3Config config;

    /**
     * Optional AJAX callback invoked when a graph node is clicked;
     * {@code null} (the default) means clicks are ignored.
     */
    private NodeClickedCallback clickCallback;

    /**
     * Constructor.
     *
     * @param id The component id
     * @param config The D3 configuration
     */
    public D3Panel(final String id, final @NonNull D3Config config) {
        super(id);

        var d3Canvas = Wkt.add(this, Wkt.ajaxEnable(new WebComponent(ID_D3_CANVAS)));

        this.config = config
                .withCanvasId(d3Canvas.getMarkupId());
    }

    /**
     * Binds the node-click AJAX callback to this panel; the callback's URL is
     * included in the rendered configuration so that the client-side script can
     * invoke it (with the clicked node's id) when a node is clicked.
     */
    public D3Panel withClickCallback(final @NonNull NodeClickedCallback clickCallback) {
        this.clickCallback = clickCallback;
        add(clickCallback);
        return this;
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        var configWithCallback = clickCallback != null
                ? config.withClickCallbackUrl(clickCallback.getCallbackUrl().toString())
                : config;

        response.render(D3Reference.asHeaderItem());
        response.render(D3IntegrationReference.asHeaderItem());
        response.render(D3IntegrationReference.domReadyScript(configWithCallback));
    }

    public CharSequence getCanvasId() {
        return config.getCanvasId();
    }

}