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
package org.apache.causeway.extensions.pdfjs.wkt.integration.res;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import org.apache.causeway.extensions.pdfjs.applib.config.PdfJsConfig;
import org.apache.causeway.extensions.pdfjs.wkt.integration.CausewayModuleExtPdfjsWicketIntegration;

import lombok.Getter;
import lombok.experimental.Accessors;

public class PdfJsIntegrationReference
extends JavaScriptResourceReference {

    private static final long serialVersionUID = 1L;

    @Getter(lazy = true) @Accessors(fluent = true)
    private static final PdfJsIntegrationReference instance =
        new PdfJsIntegrationReference();

    private PdfJsIntegrationReference() {
        super(PdfJsIntegrationReference.class,
                String.format("pdfjs-integration-%s.js",
                        CausewayModuleExtPdfjsWicketIntegration.getPdfJsVersion().getIntegrationScriptSuffix()));
    }

    /**
     * @return this resource reference singleton instance as header item
     */
    public static HeaderItem asHeaderItem() {
        return JavaScriptHeaderItem.forReference(instance());
    }

    public static OnDomReadyHeaderItem domReadyScript(
            final PdfJsConfig config) {
        return OnDomReadyHeaderItem.forScript(
                String.format("WicketStuff.PDFJS.init(%s)", config.toJsonString()));
    }

}
