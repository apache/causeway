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

package org.apache.isis.viewer.dnd.view.action;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.collection.AbstractCollectionContent;

public class CollectionActionContent extends AbstractCollectionContent implements ActionContent {
    private final ActionHelper invocation;
    private final ParameterContent[] parameters;

    public CollectionActionContent(final ActionHelper invocation) {
        this.invocation = invocation;
        parameters = invocation.createParameters();
    }

    @Override
    public void debugDetails(final DebugBuilder debug) {
        debug.appendln("action", getActionName());
        debug.appendln("target", getAdapter());
        String parameterSet = "";
        for (final ParameterContent parameter : parameters) {
            parameterSet += parameter;
        }
        debug.appendln("parameters", parameterSet);
    }

    @Override
    public Consent canDrop(final Content sourceContent) {
        return Veto.DEFAULT;
    }

    @Override
    public Consent disabled() {
        return invocation.disabled();
    }

    @Override
    public ObjectAdapter drop(final Content sourceContent) {
        throw new NotYetImplementedException();
    }

    @Override
    public ObjectAdapter[] elements() {
        throw new NotYetImplementedException();
    }

    @Override
    public ObjectAdapter execute() {
        return invocation.invoke();
    }

    @Override
    public String getActionName() {
        return invocation.getName();
    }

    @Override
    public ObjectAdapter getCollection() {
        return invocation.getTarget();
    }

    @Override
    public String getDescription() {
        return invocation.getDescription();
    }

    @Override
    public String getHelp() {
        return invocation.getHelp();
    }

    @Override
    public String getIconName() {
        return getAdapter().getIconName();
    }

    @Override
    public String getId() {
        return invocation.getName();
    }

    @Override
    public ObjectAdapter getAdapter() {
        return invocation.getTarget();
    }

    @Override
    public int getNoParameters() {
        return parameters.length;
    }

    @Override
    public ParameterContent getParameterContent(final int index) {
        return parameters[index];
    }

    @Override
    public ObjectAdapter getParameterObject(final int index) {
        return invocation.getParameter(index);
    }

    @Override
    public ObjectSpecification getSpecification() {
        return getAdapter().getSpecification();
    }

    @Override
    public boolean isTransient() {
        return true;
    }

    @Override
    public String title() {
        return getAdapter().titleString();
    }

    @Override
    public String windowTitle() {
        return getActionName();
    }
}
