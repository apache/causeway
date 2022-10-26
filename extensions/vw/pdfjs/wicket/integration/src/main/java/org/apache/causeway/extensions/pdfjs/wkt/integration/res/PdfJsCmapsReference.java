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

import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.Strings;

import org.apache.causeway.extensions.pdfjs.wkt.integration.CausewayModuleExtPdfjsWicketIntegration;

import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;
import lombok.Getter;
import lombok.val;
import lombok.experimental.Accessors;

public class PdfJsCmapsReference
extends WebjarsJavaScriptResourceReference {

    private static final long serialVersionUID = 1L;

    @Getter(lazy = true) @Accessors(fluent = true)
    private static final PdfJsCmapsReference instance =
        new PdfJsCmapsReference();

    private PdfJsCmapsReference() {
        super(String.format("%s/cmaps/_.bcmap",
                CausewayModuleExtPdfjsWicketIntegration.getPdfJsVersion().getWebjarPath()));
    }

    public static String cmapsUrl() {
        return Strings.stripEnding(asUrl().toString(), "_.bcmap");
    }

    // -- HELPER

    private static Url asUrl() {
        val pdfJsUrl = RequestCycle.get().urlFor(instance(), null);
        return Url.parse(pdfJsUrl);
    }



}
