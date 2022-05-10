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
package org.apache.isis.extensions.pdfjs.wkt.integration;

import org.apache.wicket.util.lang.Args;

import org.apache.isis.extensions.pdfjs.applib.config.PdfJsConfig;
import org.apache.isis.extensions.pdfjs.applib.config.Scale;

import de.agilecoders.wicket.jquery.AbstractConfig;
import de.agilecoders.wicket.jquery.IKey;
import de.agilecoders.wicket.jquery.Key;

public class PdfJsConfigWkt extends AbstractConfig {

    private static final long serialVersionUID = 1L;

    private static final IKey<Integer> INITIAL_PAGE = new Key<>("initialPage", 1);
    private static final IKey<String> INITIAL_SCALE = new Key<>("initialScale", Scale._1_00.getValue());
    private static final IKey<Integer> INITIAL_HEIGHT = new Key<>("initialHeight", 800);
    private static final IKey<Boolean> WORKER_DISABLED = new Key<>("workerDisabled", false);
    private static final IKey<CharSequence> PDF_DOCUMENT_URL = new Key<>("documentUrl", null);
    private static final IKey<CharSequence> WORKER_URL = new Key<>("workerUrl", null);
    private static final IKey<CharSequence> CANVAS_ID = new Key<>("canvasId", null);

    public static PdfJsConfigWkt from(final PdfJsConfig config) {
        return new PdfJsConfigWkt()
            .withCanvasId(config.getCanvasId())
            .withDocumentUrl(config.getDocumentUrl())
            .withInitialHeight(config.getInitialHeight())
            .withInitialScale(config.getInitialScale())
            .withInitialHeight(config.getInitialHeight())
            .withWorkerUrl(config.getWorkerUrl())
            .disableWorker(config.isWorkerDisabled())
            ;
    }

    public PdfJsConfigWkt withInitialPage(int initialPage) {
        if (initialPage < 1) {
            initialPage = 1;
        }
        put(INITIAL_PAGE, initialPage);
        return this;
    }

    public int getInitialPage() {
        return get(INITIAL_PAGE);
    }

    public PdfJsConfigWkt withInitialHeight(final int initialHeight) {
        Args.isTrue(initialHeight >= 400 && initialHeight <= 2000,
                "'initialHeight' must be between 400 and 2000");
        put(INITIAL_HEIGHT, initialHeight);
        return this;
    }

    public int getInitialHeight() {
        return get(INITIAL_HEIGHT);
    }

    public PdfJsConfigWkt withInitialScale(final Scale initialScale) {
        put(INITIAL_SCALE, initialScale.getValue());
        return this;
    }

    public String getInitialScale() {
        return get(INITIAL_SCALE);
    }

    public PdfJsConfigWkt disableWorker(final boolean disable) {
        put(WORKER_DISABLED, disable);
        return this;
    }

    public boolean isWorkerDisabled() {
        return get(WORKER_DISABLED);
    }

    public PdfJsConfigWkt withDocumentUrl(final CharSequence url) {
        put(PDF_DOCUMENT_URL, url);
        return this;
    }

    public CharSequence getDocumentUrl() {
        return get(PDF_DOCUMENT_URL);
    }

    public PdfJsConfigWkt withWorkerUrl(final CharSequence url) {
        put(WORKER_URL, url);
        return this;
    }

    public CharSequence getWorkerUrl() {
        return get(WORKER_URL);
    }

    public PdfJsConfigWkt withCanvasId(final CharSequence url) {
        put(CANVAS_ID, url);
        return this;
    }

    public CharSequence getCanvasId() {
        return get(CANVAS_ID);
    }

}

