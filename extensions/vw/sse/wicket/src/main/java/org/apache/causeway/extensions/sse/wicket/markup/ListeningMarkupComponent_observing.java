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
package org.apache.causeway.extensions.sse.wicket.markup;

import java.io.IOException;
import java.util.UUID;

import org.apache.causeway.applib.value.LocalResourcePath;
import org.apache.causeway.commons.internal.resources._Resources;
import org.apache.causeway.core.config.viewer.web.WebAppContextPath;

import static org.apache.causeway.commons.internal.base._Strings.isNullOrEmpty;

import lombok.extern.log4j.Log4j2;

@Log4j2
final class ListeningMarkupComponent_observing  {

    private static final String jScriptTemplateResource = "js/ObservingComponent.js";

    static CharSequence decorate(
            final CharSequence htmlContent,
            final LocalResourcePath observing,
            final WebAppContextPath webAppContextPath) {
        if(observing==null) {
            return htmlContent;
        }
        final String jScriptTemplate;
        try {
            jScriptTemplate = _Resources.loadAsStringUtf8(
                    ListeningMarkupComponent_observing.class, jScriptTemplateResource);

        } catch (IOException e) {
            log.warn("resource not found class:{} path:{}",
                    ListeningMarkupComponent_observing.class, jScriptTemplateResource, e);
            return resourceNotFound();
        }

        if(isNullOrEmpty(jScriptTemplate)) {
            return resourceNotFound();
        }

        final String targetId = UUID.randomUUID().toString();
        final String observingPath = observing.getEffectivePath(webAppContextPath::prependContextPath);

        final StringBuilder sb = new StringBuilder();
        sb
        .append("<div id=\"").append(targetId).append("\">\n")
        .append(htmlContent)
        .append("\n</div>\n")
        .append("<script type=\"text/javascript\" defer>\n")
        .append(jScriptTemplate
                .replace("${targetId}", targetId)
                .replace("${observing}", observingPath))
        .append("\n</script>\n");

        return sb.toString();
    }

    private static String resourceNotFound() {
        return "Template resource not found: '"+jScriptTemplateResource+"'.";
    }

}
