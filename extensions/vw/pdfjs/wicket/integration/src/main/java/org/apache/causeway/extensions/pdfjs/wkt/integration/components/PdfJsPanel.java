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
package org.apache.causeway.extensions.pdfjs.wkt.integration.components;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.panel.Panel;

import org.apache.causeway.extensions.pdfjs.applib.config.PdfJsConfig;
import org.apache.causeway.extensions.pdfjs.wkt.integration.res.PdfJsCmapsReference;
import org.apache.causeway.extensions.pdfjs.wkt.integration.res.PdfJsIntegrationReference;
import org.apache.causeway.extensions.pdfjs.wkt.integration.res.PdfJsReference;
import org.apache.causeway.extensions.pdfjs.wkt.integration.res.PdfJsWorkerReference;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.NonNull;
import lombok.val;

/**
 * A panel for rendering PDF documents inline in the page
 */
public class PdfJsPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private final static String ID_PDFJSCANVAS = "pdfJsCanvas";

    private PdfJsConfig config;

    /**
     * Constructor.
     *
     * @param id The component id
     */
    public PdfJsPanel(final String id, final @NonNull PdfJsConfig config) {
        super(id);

        val pdfJsCanvas = Wkt.add(this, Wkt.ajaxEnable(new WebComponent(ID_PDFJSCANVAS)));

        this.config = config
                .withWorkerUrl(PdfJsWorkerReference.workerUrl())
                .withCmapsUrl(PdfJsCmapsReference.cmapsUrl())
                .withCanvasId(pdfJsCanvas.getMarkupId());
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        response.render(PdfJsReference.asHeaderItem());
        response.render(PdfJsIntegrationReference.asHeaderItem());
        response.render(PdfJsIntegrationReference.domReadyScript(config));
    }

    public CharSequence getCanvasId() {
        return config.getCanvasId();
    }

}
