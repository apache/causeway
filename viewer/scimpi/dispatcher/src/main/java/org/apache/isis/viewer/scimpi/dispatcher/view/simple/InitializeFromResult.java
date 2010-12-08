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
        disallowSourceAndDefault(request); 
        String sourceObjectId = objectOrResult(request); 
        Class<?> cls = forClass(request); 
        String variableName = request.getRequiredProperty(NAME); 
        //        String defaultObjectId = request.getOptionalProperty(DEFAULT);         
        String scopeName = request.getOptionalProperty(SCOPE);
        Scope scope = RequestContext.scope(scopeName, Scope.REQUEST);

        RequestContext context = request.getContext(); 
        ObjectAdapter sourceObject = context.getMappedObject(sourceObjectId); 
        boolean isSourceSet = sourceObject != null; 
        boolean isSourceAssignable = isSourceSet && (cls == null || cls.isAssignableFrom(sourceObject.getObject().getClass())); 
        if (isSourceAssignable) {
            request.appendDebug("     " + variableName + " set to " + sourceObjectId + " (" + scope + ")"); 
            context.addVariable(variableName, sourceObjectId, scope); 
        } else { 
            request.appendDebug("     " + variableName + " set to " + sourceObjectId + " (" + scope + ")"); 
            context.changeScope(variableName, scope); 
        } 
    } 
    
    private String objectOrResult(Request request) { 
        String sourceObjectId = request.getOptionalProperty(OBJECT); 
        if (sourceObjectId == null) { 
            return (String) request.getContext().getVariable(RequestContext.RESULT); 
        } else { 
            return sourceObjectId; 
        } 
    } 

    private void disallowSourceAndDefault(Request request) { 
        if (request.getOptionalProperty(DEFAULT) != null && request.getOptionalProperty(OBJECT) != null) { 
            throw new ScimpiException("Cannot specify both " + OBJECT + " and " + DEFAULT + " for the " + getName() + " element");     
        }
    }
    
    public String getName() {
        return "initialize";
    }

}

