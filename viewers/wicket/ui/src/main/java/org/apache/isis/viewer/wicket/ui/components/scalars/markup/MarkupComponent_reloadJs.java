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

import java.util.UUID;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.resource.ResourceReference;

import lombok.val;

/**
 * Helper for MarkupComponents to reload a specified java-script reference, when rendering.
 * @implNote used by AsciiDoc and Markdown extensions
 */
public class MarkupComponent_reloadJs {

    public static CharSequence decorate(CharSequence htmlContent, ResourceReference jsRef) {

        val targetId = UUID.randomUUID().toString();

        val sb = new StringBuilder();
        sb
        .append("<div id=\"").append(targetId).append("\">\n")
        .append(htmlContent)
        .append("\n</div>\n")
        .append("<script type=\"text/javascript\" src=\""+getUrl(jsRef)+"\" defer>\n")
        .append("\n</script>\n");

        return sb.toString();
    }

    private static String getUrl(ResourceReference jsRef) {
        IRequestHandler handler = new ResourceReferenceRequestHandler(jsRef, null);
        return RequestCycle.get().urlFor(handler).toString();
    }

}
