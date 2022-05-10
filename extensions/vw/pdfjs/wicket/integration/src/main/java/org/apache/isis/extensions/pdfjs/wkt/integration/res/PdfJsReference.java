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
package org.apache.isis.extensions.pdfjs.wkt.integration.res;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import org.apache.isis.extensions.pdfjs.applib.config.PdfJsConfig;

import lombok.Getter;
import lombok.experimental.Accessors;

public class PdfJsReference
extends JavaScriptResourceReference {

    private static final long serialVersionUID = 1L;

    @Getter(lazy = true) @Accessors(fluent = true)
    private static final PdfJsReference instance =
        new PdfJsReference();

    private PdfJsReference() {
        super(PdfJsReference.class, "pdf.js");
    }

    /**
     * @return this resource reference singleton instance as header item
     */
    public static HeaderItem asHeaderItem() {
        return JavaScriptHeaderItem.forReference(instance());
    }

    public static PdfJsConfig configureWorkerUrl(final PdfJsConfig config) {
        return config.withWorkerUrl(createPdfJsWorkerUrl());
    }

    // -- HELPER

    private static String createPdfJsWorkerUrl() {
        final CharSequence _pdfJsUrl = RequestCycle.get().urlFor(instance(), null);
        final Url pdfJsUrl = Url.parse(_pdfJsUrl);
        final Url pdfJsWorkerUrl = Url.parse("./pdf.worker.js");
        pdfJsUrl.resolveRelative(pdfJsWorkerUrl);
        return pdfJsUrl.toString();
    }

}
