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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Generates client-side java-script to help with page redirecting.
 *
 * @implNote in some certain reverse proxy scenarios Wicket (at the time of writing)
 *  might not be able to produce the correct
 *  URL origin for the currently rendered page.
 *  We workaround that, by asking the client/browser, what its 'window.location.origin' is
 *  and rewrite redirect URLs if required.
 *
 * @apiNote {@link OriginRewrite} was introduced as a workaround,
 *  perhaps can be removed in future versions,
 *  when reverting non-rewriting logic
 */
@AllArgsConstructor
@Getter @Accessors(fluent = true)
final class JavaScriptRedirect {
	
	private final OriginRewrite originRewrite;
	private final String url;

    enum OriginRewrite {
        DISABLED,
        ENABLED;
        boolean isDisabled() { return this!=ENABLED; }
    }

    String javascriptFor_newWindow() {

        if(originRewrite.isDisabled())
            return String.format("function(){\n"
            		+ "    const url = '%s';\n"
            		+ "    Wicket.Event.publish(Causeway.Topic.OPEN_IN_NEW_TAB, url);\n"
            		+ "}", url);

        return String.format("function(){\n"
        		+ "    const url = '%s';\n"
        		+ "    const requiredOrigin = window.location.origin;\n"
        		+ "    const replacedUrl = url.startsWith(requiredOrigin)\n"
        		+ "      ? url\n"
        		+ "      : (() => {\n"
        		+ "          const urlObj = new URL(url);\n"
        		+ "          return requiredOrigin + urlObj.pathname + urlObj.search + urlObj.hash;\n"
        		+ "        })();\n"
        		+ "    Wicket.Event.publish(Causeway.Topic.OPEN_IN_NEW_TAB, replacedUrl);\n"
        		+ "}", url);
    }

    String javascriptFor_sameWindow() {

        if(originRewrite.isDisabled())
        	return String.format("function(){\n"
            		+ "    const url = '%s';\n"
            		+ "    window.location.href=url;\n"
            		+ "}", url);

        return String.format("function(){\n"
        		+ "    const url = '%s';\n"
        		+ "    const requiredOrigin = window.location.origin;\n"
        		+ "    const replacedUrl = url.startsWith(requiredOrigin)\n"
        		+ "      ? url\n"
        		+ "      : (() => {\n"
        		+ "          const urlObj = new URL(url);\n"
        		+ "          return requiredOrigin + urlObj.pathname + urlObj.search + urlObj.hash;\n"
        		+ "        })();\n"
        		+ "    window.location.href=replacedUrl;\n"
        		+ "}", url);
    }

}
