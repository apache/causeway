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
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.dnd.view.content.AbstractObjectContent;

/**
 * Links an action on an object to a view.
 */
public class ObjectActionContent extends AbstractObjectContent implements ActionContent {
    private final ActionHelper actionHelper;
    private final ParameterContent[] parameters;

    public ObjectActionContent(final ActionHelper invocation) {
        this.actionHelper = invocation;
        parameters = invocation.createParameters();
    }

    @Override
    public Consent canClear() {
        return Veto.DEFAULT;
    }

    @Override
    public Consent canSet(final ObjectAdapter dragSource) {
        return Veto.DEFAULT;
    }

    @Override
    public void clear() {
        throw new IsisException("Invalid call");
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
    public Consent disabled() {
        return actionHelper.disabled();
    }

    @Override
    public ObjectAdapter execute() {
        return actionHelper.invoke();
    }

    @Override
    public String getActionName() {
        return actionHelper.getName();
    }

    @Override
    public String getIconName() {
        return actionHelper.getIconName();
    }

    @Override
    public ObjectAdapter getAdapter() {
        return actionHelper.getTarget();
    }

    @Override
    public int getNoParameters() {
        return parameters.length;
    }

    @Override
    public ObjectAdapter getObject() {
        return actionHelper.getTarget();
    }

    @Override
    public ParameterContent getParameterContent(final int index) {
        return parameters[index];
    }

    @Override
    public ObjectAdapter getParameterObject(final int index) {
        return actionHelper.getParameter(index);
    }

    @Override
    public ObjectSpecification getSpecification() {
        return getObject().getSpecification();
    }

    /**
     * Can't persist actions
     */
    @Override
    public boolean isPersistable() {
        return false;
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public boolean isTransient() {
        return true;
    }

    @Override
    public void setObject(final ObjectAdapter object) {
        throw new IsisException("Invalid call");
    }

    @Override
    public String title() {
        return actionHelper.title();
    }

    @Override
    public String windowTitle() {
        return getActionName();
    }

    @Override
    public String getId() {
        return actionHelper.getName();
    }

    @Override
    public String getDescription() {
        return actionHelper.getDescription();
    }

    @Override
    public String getHelp() {
        return actionHelper.getHelp();
    }

    @Override
    public ObjectAdapter[] getOptions() {
        return null;
    }

    @Override
    public boolean isOptionEnabled() {
        return false;
    }

}
