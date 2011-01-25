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

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.session.IsisSession;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;


public class Diagnostics extends AbstractElementProcessor {

    public void process(Request request) {
        boolean isForced = request.isRequested("force");
        boolean isExcludeVariables = request.isRequested("exclude-variables");
        boolean isExcludeProcessing = request.isRequested("exclude-processing");
        if (isForced || request.getContext().getDebug() == RequestContext.Debug.ON) {
            RequestContext context = request.getContext();
            request.appendHtml("<div class=\"debug\">");
            request.appendHtml("<pre>");  
            request.appendHtml("URI:  " + context.getUri());
            request.appendHtml("\n");
            request.appendHtml("File: " + context.fullFilePath(context.getResourceFile()));
            request.appendHtml("\n");
            
            AuthenticationSession session = IsisContext.getAuthenticationSession();
            request.appendHtml("Session:  " + session.getUserName() + " " + session.getRoles());
            
            if (!isExcludeVariables) {
                request.appendHtml("\n\n");
                request.appendHtml("<a class=\"option\" target=\"debug\" href=\"debug.app\">Object</a>");
                context.append(request, "variables");
            }
            if (!isExcludeProcessing) {
                request.appendHtml("\n\n"); 
                request.appendHtml(request.getContext().getDebugTrace());      
            }
            request.appendHtml("</pre>");
            request.appendHtml("</div>");
        }
    }

    public String getName() {
        return "diagnostics";
    }

}

