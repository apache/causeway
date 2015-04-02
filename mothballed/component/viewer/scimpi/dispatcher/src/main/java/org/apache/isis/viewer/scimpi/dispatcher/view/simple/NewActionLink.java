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

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.scimpi.dispatcher.Dispatcher;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.util.MethodsUtils;

public class NewActionLink extends AbstractLink {

    // REVIEW: confirm this rendering context
    private final Where where = Where.OBJECT_FORMS;

    @Override
    protected boolean valid(final Request request, final ObjectAdapter object) {
        final String method = request.getRequiredProperty(METHOD);
        final ObjectAction action = MethodsUtils.findAction(object, method);
        return MethodsUtils.isVisibleAndUsable(object, action, where);
    }

    @Override
    protected String linkLabel(final String name, final ObjectAdapter object) {
        return "run";
    }

    @Override
    protected String defaultView() {
        return "_generic_action." + Dispatcher.EXTENSION;
    }

    @Override
    protected String additionalParameters(final Request request) {
        final String method = request.getRequiredProperty(METHOD);
        return "method=" + method;
    }

    @Override
    public String getName() {
        return "new-action-link";
    }

}
