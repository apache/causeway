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


package org.apache.isis.webapp.view.display;

import org.apache.isis.webapp.AbstractElementProcessor;
import org.apache.isis.webapp.context.RequestContext;
import org.apache.isis.webapp.processor.Request;

/**
 * Adds the errors contained in the _error variable to the page if it is set
 */
public class Errors extends AbstractElementProcessor {

    public void process(Request request) {
        String cls = request.getOptionalProperty(CLASS);       
        append(request, cls);
    }

    public static void append(Request request, String cls) {
        StringBuffer buffer = new StringBuffer();
        write(cls, buffer, request);
        request.appendHtml(buffer.toString());
    }

    public static void write(String cls, StringBuffer buffer, Request request) {
        if (cls == null) {
            cls = "errors";
        }
        Object errors = request.getContext().getVariable(RequestContext.ERROR);
        if (errors != null) {
            buffer.append("<div class=\"" + cls + "\">" + errors + "</div>");
        }
    }

    public String getName() {
        return "errors";
    }

}

