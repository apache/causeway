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
package org.apache.causeway.extensions.pdfjs.wkt.integration;

import org.apache.wicket.markup.head.JavaScriptReferenceType;

import org.apache.causeway.viewer.commons.model.webjar.WebjarEnumerator;

public record PdfJsVersion(String webjarPath) {
//    V2_X("v2x3x", "pdfjs-dist/2.16.105", JavaScriptReferenceType.TEXT_JAVASCRIPT),
//    V3_X("v2x3x", "pdfjs-dist/3.11.174", JavaScriptReferenceType.TEXT_JAVASCRIPT),
//    V4_X("v4x5x", "pdfjs-dist/4.10.38", JavaScriptReferenceType.MODULE),
//    V5_X("v4x5x", "pdfjs-dist/5.4.530", JavaScriptReferenceType.MODULE);

    PdfJsVersion() {
        this("pdfjs-dist/" + WebjarEnumerator.lookupElseFail("pdfjs-dist")
            .version());
    }

    public String integrationScriptSuffix() {
        return "v4x5x";
    }
    public JavaScriptReferenceType javascriptRefType() {
        return JavaScriptReferenceType.MODULE;
    }

}
