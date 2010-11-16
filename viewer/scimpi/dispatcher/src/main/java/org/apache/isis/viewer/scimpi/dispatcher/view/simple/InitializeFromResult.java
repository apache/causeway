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
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;


public class InitializeFromResult extends AbstractElementProcessor {

    public void process(Request request) {
        String name = request.getRequiredProperty(NAME);
        String defaultId = request.getOptionalProperty(DEFAULT);
        String objectId = request.getOptionalProperty(OBJECT);
        String scopeName = request.getOptionalProperty(SCOPE);
        Scope scope = RequestContext.scope(scopeName, Scope.REQUEST);

        if (defaultId != null && objectId != null) {
            throw new ScimpiException("Cannot specify both " + OBJECT + " and " + DEFAULT + " for the " + getName() + " element");
        }

        String className = request.getOptionalProperty(TYPE);
        Class<?> cls = null;
        if (className != null) {
            try {
                cls = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new ScimpiException("No class for " + className, e);
            }
        }

        if (objectId == null) {
            objectId = (String) request.getContext().getVariable(RequestContext.RESULT);
        }

        initialize(request, name, objectId, defaultId, cls, scope);
    }

    protected void initialize(Request request, String variableName, String objectId, String defaultId, Class<?> cls, Scope scope) {
        RequestContext context = request.getContext();
        if (defaultId != null) {
                String variable = (String) request.getContext().getVariable(variableName);
                if (variable == null) {
                    context.addVariable(variableName, defaultId, scope);
                }
            
        } else {
            ObjectAdapter variable = context.getMappedObject(objectId);
            if (variable != null && (cls == null || cls.isAssignableFrom(variable.getObject().getClass()))) {
                context.addVariable(variableName, objectId, scope);
            } else {
                Object id2 = context.getVariable(variableName);
                // TODO should remove the variable from other scope
                context.addVariable(variableName, id2, scope);
            }
        }
    }

    public String getName() {
        return "initialize";
    }

}

