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
package org.apache.causeway.viewer.wicket.ui.exec;

import org.apache.causeway.viewer.wicket.ui.exec.JavaScriptRedirect.OriginRewrite;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Provides additional context for URL based redirects.
 */
@AllArgsConstructor
@Getter @Accessors(fluent = true)
final class UrlBasedRedirectContext {
	
    private final String fullUrl;
    private final boolean isSameOrigin; 

    static UrlBasedRedirectContext of(
            final String url) {
        var urlRenderer = RequestCycle.get().getUrlRenderer(); //CAUSEWAY[3976] might not reliable work in reverse proxy situations
        var origin = urlRenderer.renderFullUrl(Url.parse("./"));
        var fullUrl = urlRenderer.renderFullUrl(Url.parse(interpolate(url)));
        var isSameOrigin = fullUrl.startsWith(origin);
        return new UrlBasedRedirectContext(fullUrl, isSameOrigin);
    }

    JavaScriptRedirect createJavaScriptRedirect() {
        return new JavaScriptRedirect(
                isSameOrigin
                    ? OriginRewrite.ENABLED
                    : OriginRewrite.DISABLED,
                fullUrl());
    }

    /**
     * very simple template support, the idea being that "antiCache=${currentTimeMillis}"
     * will be replaced automatically.
     */
    private static String interpolate(final String urlStr) {
        return urlStr.contains("antiCache=${currentTimeMillis}")
            ? urlStr.replace("antiCache=${currentTimeMillis}", "antiCache="+System.currentTimeMillis())
            : urlStr;
    }

}
