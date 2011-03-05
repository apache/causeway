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


package org.apache.isis.viewer.scimpi.dispatcher.view.simple;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtimes.dflt.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;


public class InitializeFromCookie extends AbstractElementProcessor {
    private static final String SEVEN_DAYS = Integer.toString(60 * 24 * 7);

    public void process(Request request) {
        String name = request.getRequiredProperty(NAME);

        RequestContext context = request.getContext();
        if (context.getVariable(name) != null) {
            request.skipUntilClose();
        } else {
            String scopeName = request.getOptionalProperty(SCOPE);
            Scope scope = RequestContext.scope(scopeName, Scope.SESSION);

            String cookieName = request.getOptionalProperty("cookie", name);
            String cookieValue = context.getCookie(cookieName);
            boolean hasObject;
            if (cookieValue != null) {
                try {
                    context.getMappedObject((String) cookieValue);
                    hasObject = true;
                } catch (ObjectNotFoundException e) {
                    hasObject = false;
                }
            } else {
                hasObject = false;
            }
            
            
            
            if (hasObject) {
                request.skipUntilClose();
                context.addVariable(name, cookieValue, scope);
            } else {
                String expiresString = request.getOptionalProperty("expires", SEVEN_DAYS);
                request.pushNewBuffer();
                request.processUtilCloseTag();
                request.popBuffer();
                String id = (String) context.getVariable(RequestContext.RESULT);
                ObjectAdapter variable = context.getMappedObject(id);
                if (variable != null) {
                    context.addCookie(cookieName, id, Integer.valueOf(expiresString));
                    context.addVariable(name, id, scope);
                }
            }
        }
    }

    public String getName() {
        return "initialize-from-cookie";
    }

}

