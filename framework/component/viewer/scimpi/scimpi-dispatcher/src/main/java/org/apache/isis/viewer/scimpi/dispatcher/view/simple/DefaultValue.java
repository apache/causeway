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

import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;

public class DefaultValue extends AbstractElementProcessor {

    @Override
    public void process(final Request request) {
        // String sourceObjectId = objectOrResult(request);
        final String variableName = request.getRequiredProperty(NAME);
        final String defaultValue = request.getOptionalProperty(VALUE);
        final String scopeName = request.getOptionalProperty(SCOPE);
        final Scope scope = RequestContext.scope(scopeName, Scope.REQUEST);

        final RequestContext context = request.getContext();
        final Object currentValue = context.getVariable(variableName);
        if (currentValue == null) {
            request.appendDebug("     " + variableName + " set to " + defaultValue + " (" + scope + ")");
            context.addVariable(variableName, defaultValue, scope);
        } else {
            request.appendDebug("     " + variableName + " alreadt set to " + currentValue);
        }
    }

    @Override
    public String getName() {
        return "default";
    }

}
