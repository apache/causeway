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

package org.apache.isis.viewer.wicket.ui.components.scalars.markup;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.commons.internal.resources._Resources;

import static org.apache.isis.commons.internal.base._Strings.isNullOrEmpty;

final class MarkupComponent_observing  {

    private static final String jScriptTemplateResource = "ObservingComponent.js";
    
    static CharSequence decorate(CharSequence htmlContent, LocalResourcePath observing) {
        if(observing==null) {
            return htmlContent;
        }
        final String jScriptTemplate;
        try {
            jScriptTemplate = _Resources.loadAsString(
                    MarkupComponent_observing.class, jScriptTemplateResource, StandardCharsets.UTF_8);
            
        } catch (IOException e) {
            e.printStackTrace();
            return resourceNotFound();
        }
        
        if(isNullOrEmpty(jScriptTemplate)) {
            return resourceNotFound();
        }
        
        final String targetId = UUID.randomUUID().toString();
        final String observingPath = _Resources.prependContextPathIfPresent(observing.getPath());
        
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
