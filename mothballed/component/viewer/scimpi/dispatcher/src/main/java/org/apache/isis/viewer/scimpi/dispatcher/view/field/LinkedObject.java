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

package org.apache.isis.viewer.scimpi.dispatcher.view.field;

import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;

public class LinkedObject {
    private final String variable;
    private final String scope;
    private String forwardView;

    public LinkedObject(final String variable, final String scope, final String forwardView) {
        this.variable = variable;
        this.scope = scope;
        this.forwardView = forwardView;
    }

    public LinkedObject(final String forwardView) {
        this.forwardView = forwardView;
        scope = Scope.INTERACTION.toString();
        variable = RequestContext.RESULT;
    }

    public String getVariable() {
        return variable;
    }

    public String getScope() {
        return scope;
    }

    public String getForwardView() {
        return forwardView;
    }

    public void setForwardView(final String path) {
        forwardView = path;
    }

}
