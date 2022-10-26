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
package org.apache.causeway.extensions.pdfjs.applib.config;

import java.io.Serializable;

import org.springframework.util.Assert;

import org.apache.causeway.commons.internal.resources._Json;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;

/**
 * @since 2.0 {@index}
 */
@Getter @AllArgsConstructor @NoArgsConstructor @Builder
public class PdfJsConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private int initialPage = 1;

    @Builder.Default @With
    private Scale initialScale = Scale._1_00;

    @Builder.Default
    private int initialHeight = 800;

    @With
    private CharSequence documentUrl;
    @With
    private CharSequence workerUrl;
    @With
    private CharSequence cmapsUrl;
    @With
    private CharSequence canvasId;

    public PdfJsConfig withInitialPage(int initialPage) {
        if (initialPage < 1) {
            initialPage = 1;
        }
        return asBuilder().initialPage(initialPage).build();
    }

    public PdfJsConfig withInitialHeight(final int initialHeight) {
        Assert.isTrue(initialHeight >= 400 && initialHeight <= 2000,
                ()->String.format("'initialHeight' must be between 400 and 2000; got %d", initialHeight));
        return asBuilder().initialHeight(initialHeight).build();
    }

    public PdfJsConfig.PdfJsConfigBuilder asBuilder() {
        return PdfJsConfig.builder()
                .canvasId(getCanvasId())
                .documentUrl(getDocumentUrl())
                .initialHeight(getInitialHeight())
                .initialPage(getInitialPage())
                .initialScale(getInitialScale())
                .workerUrl(getWorkerUrl())
                .cmapsUrl(getCmapsUrl());
    }

    public String toJsonString() {
        return _Json.toString(this);
    }

}

