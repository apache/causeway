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


package org.apache.isis.webapp.view.debug;

import java.util.Arrays;

import org.apache.isis.core.commons.debug.DebugInfo;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.util.Dump;
import org.apache.isis.webapp.AbstractElementProcessor;
import org.apache.isis.webapp.context.RequestContext;
import org.apache.isis.webapp.processor.Request;


public class Debug extends AbstractElementProcessor {

    public void process(Request request) {
        String type = request.getOptionalProperty("type");
        String value = request.getOptionalProperty("value");
        if (type != null) {
            if (type.equals("system")) {
                DebugInfo[] debug = IsisContext.debugSystem();
                request.appendHtml("<h2>System</h2>");
                for (int i = 0; i < debug.length; i++) {
                    DebugString str = new DebugString();
                    debug[i].debugData(str);
                    request.appendHtml("<h4>" + debug[i].debugTitle() + "</h4>");
                    request.appendHtml("<pre class=\"debug\">" + str + "</pre>");
                }

            } else if (type.equals("list-specifications")) {
                ObjectSpecification[] allSpecifications = IsisContext.getSpecificationLoader().allSpecifications();
                String[] names = new String[allSpecifications.length];
                for (int j = 0; j < allSpecifications.length; j++) {
                    names[j] = allSpecifications[j].getFullName();
                }
                Arrays.sort(names);
                request.appendHtml("<h2>Specifications</h2><ol>");
                for (int j = 0; j < names.length; j++) {
                    request.appendHtml("<li><a href=\"specification.shtml?spec=" + names[j] + "\">" + names[j] + "</a></p>");
                }
                request.appendHtml("</ol>");
                
            } else if (type.equals("list-objects")) {
                request.appendHtml("<h2>Objects</h2><ol>");
                request.getContext().append(request, "variables");
                
            } else if (type.equals("specification")) {
                ObjectSpecification spec = IsisContext.getSpecificationLoader().loadSpecification(value);
                DebugString str = new DebugString();
                Dump.specification(spec, str);
                request.appendHtml("<h2>" + spec.getFullName() + "</h2>");
                request.appendHtml("<pre class=\"debug\">" + str + "</pre>");

            } else if (type.equals("object")) {
                RequestContext context = request.getContext();
                ObjectAdapter object = context.getMappedObject(value);
                DebugString str = new DebugString();
                Dump.adapter(object, str);
                Dump.graph(object, str, IsisContext.getAuthenticationSession());
                request.appendHtml("<h2>" + object.getSpecification().getFullName() + "</h2>");
                request.appendHtml("<pre class=\"debug\">" + str + "</pre>");
            }

        }

        if (request.getContext().getDebug() == RequestContext.Debug.ON) {

            RequestContext context = request.getContext();
            
            
            

            String id = request.getOptionalProperty("object");
            if (id != null) {
                ObjectAdapter object = context.getMappedObject(id);
                if (object instanceof DebugInfo) {
                    DebugString debug = new DebugString();
                    ((DebugInfo) object).debugData(debug);
                    request.appendHtml("<pre class=\"debug\">" + debug + "</pre>");
                } else {
                    request.appendHtml(object.toString());
                }
            }

            String variable = request.getOptionalProperty("variable");
            if (variable != null) {
                Object object = context.getVariable(variable);
                request.appendHtml(variable + " => " + (object == null ? "null" : object.toString()));
            }

            if (value != null) {
                request.appendHtml(value);
            }

            String list = request.getOptionalProperty("list");
            if (list != null) {
                request.appendHtml("<pre class=\"debug\">");
                context.append(request, list);
                request.appendHtml("</pre>");
            }

            String uri = request.getOptionalProperty("uri");
            if (uri != null) {
                request.appendHtml("<pre class=\"debug\">");
                request.appendHtml(context.getUri());
                request.appendHtml("</pre>");
            }

        }
    }

    public String getName() {
        return "debug";
    }

}

