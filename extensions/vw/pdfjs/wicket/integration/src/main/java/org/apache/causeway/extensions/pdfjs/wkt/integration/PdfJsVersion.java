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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PdfJsVersion {
    V2_X("v2x3x", "pdfjs-dist/2.16.105", JavaScriptReferenceType.TEXT_JAVASCRIPT),
    V3_X("v2x3x", "pdfjs-dist/3.11.174", JavaScriptReferenceType.TEXT_JAVASCRIPT),
    V4_X("v4", "pdfjs-dist/4.9.155", JavaScriptReferenceType.MODULE)
    ;
    @Getter private final String integrationScriptSuffix;
    @Getter private final String webjarPath;
    @Getter private final JavaScriptReferenceType javascriptRefType;

}
