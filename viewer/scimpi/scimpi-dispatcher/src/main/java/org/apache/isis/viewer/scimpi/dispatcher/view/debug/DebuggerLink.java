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

package org.apache.isis.viewer.scimpi.dispatcher.view.debug;

import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;

public class DebuggerLink extends AbstractElementProcessor {

    @Override
    public void process(final Request request) {
        if (request.getContext().isDebugDisabled()) {
            request.skipUntilClose();
            return;
        }

        final RequestContext context = request.getContext();
        final Object result = context.getVariable(RequestContext.RESULT);
        request.appendHtml("<div class=\"debug\">");
        request.appendHtml("<a class=\"debug-link\" href=\"/debug/debug.shtml\" target=\"debug\" title=\"debug\" >...</a>");
        if (result != null) {
            request.appendHtml(" <a href=\"/debug/object.shtml?_result=" + result + "\" target=\"debug\"  title=\"debug instance\">...</a>");
        }
        request.appendHtml(" <span class=\"debug-link\" onclick=\"$('#page-debug').toggle()\" alt=\"show/hide debug details\">...</span>");
        request.appendHtml("</div>");

        request.processUtilCloseTag();
    }

    @Override
    public String getName() {
        return "debugger";
    }

}
