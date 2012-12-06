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

public class Variable extends AbstractElementProcessor {

    @Override
    public void process(final Request request) {
        final String name = request.getOptionalProperty(NAME);
        final String value = request.getOptionalProperty(VALUE);
        final String defaultTo = request.getOptionalProperty(DEFAULT);
        final String scopeName = request.getOptionalProperty(SCOPE);
        final boolean isClear = request.getOptionalProperty("action", "set").equals("clear");
        final Scope scope = RequestContext.scope(scopeName, isClear ? Scope.SESSION : Scope.REQUEST);
        process(request, name, value, defaultTo, isClear, scope);
    }

    protected void process(final Request request, final String name, final String value, final String defaultTo, final boolean isClear, final Scope scope) {
        request.pushNewBuffer();
        request.processUtilCloseTag();
        String source = request.popBuffer();
        if (isClear) {
            request.appendDebug("variable: " + name + " (cleared)");
            request.getContext().clearVariable(name, scope);
        } else {
            if (source.length() == 0 && value != null) {
                source = value;
            }
            if (source.length() == 0) {
                source = defaultTo;
            }
            request.appendDebug("    " + name + " (" + scope + ") set to " + source);
            request.getContext().addVariable(name, source, scope);
        }
    }

    @Override
    public String getName() {
        return "variable";
    }

}
