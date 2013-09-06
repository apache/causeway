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
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneActionParameter;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.content.AbstractObjectContent;
import org.apache.isis.viewer.dnd.view.option.UserActionAbstract;

public class ObjectParameterImpl extends AbstractObjectContent implements ObjectParameter {
    private final ObjectAdapter adapter;
    private final ActionHelper invocation;
    private final int index;
    private final ObjectAdapter[] optionAdapters;
    private final OneToOneActionParameter objectActionParameter;

    public ObjectParameterImpl(final OneToOneActionParameter objectActionParameter, final ObjectAdapter adapter, final ObjectAdapter[] optionAdapters, final int i, final ActionHelper invocation) {
        this.objectActionParameter = objectActionParameter;
        this.optionAdapters = optionAdapters;
        this.index = i;
        this.invocation = invocation;
        this.adapter = adapter;
    }

    public ObjectParameterImpl(final ObjectParameterImpl content, final ObjectAdapter object) {
        objectActionParameter = content.objectActionParameter;
        optionAdapters = content.optionAdapters;
        index = content.index;
        invocation = content.invocation;
        this.adapter = object;
    }

    @Override
    public Consent canClear() {
        return Allow.DEFAULT;
    }

    @Override
    public Consent canSet(final ObjectAdapter dragSource) {
        if (dragSource.getSpecification().isOfType(getSpecification())) {
            // TODO: move logic into Facet
            return Allow.DEFAULT;
        } else {
            // TODO: move logic into Facet
            return new Veto(String.format("Object must be ", getSpecification().getShortIdentifier()));
        }
    }

    @Override
    public void clear() {
        setObject(null);
    }

    @Override
    public void debugDetails(final DebugBuilder debug) {
        debug.appendln("name", getParameterName());
        debug.appendln("required", isRequired());
        debug.appendln("object", adapter);
    }

    @Override
    public ObjectAdapter getAdapter() {
        return adapter;
    }

    @Override
    public ObjectAdapter getObject() {
        return adapter;
    }

    @Override
    public ObjectAdapter[] getOptions() {
        return optionAdapters;
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public boolean isRequired() {
        return !objectActionParameter.isOptional();
    }

    @Override
    public boolean isPersistable() {
        return false;
    }

    @Override
    public boolean isOptionEnabled() {
        return optionAdapters != null && optionAdapters.length > 0;
    }

    @Override
    public boolean isTransient() {
        return adapter != null && adapter.isTransient();
    }

    @Override
    public void contentMenuOptions(final UserActionSet options) {
        if (adapter != null) {
            options.add(new UserActionAbstract("Clear parameter") {

                @Override
                public void execute(final Workspace workspace, final View view, final Location at) {
                    clear();
                    view.getParent().invalidateContent();
                }
            });

            OptionFactory.addObjectMenuOptions(adapter, options);
        } else {
            OptionFactory.addCreateOptions(getSpecification(), options);

        }

    }

    @Override
    public void setObject(final ObjectAdapter object) {
        invocation.setParameter(index, object);
    }

    @Override
    public String title() {
        return adapter == null ? "" : adapter.titleString();
    }

    @Override
    public String toString() {
        final ToString toString = new ToString(this);
        toString.append("label", getParameterName());
        toString.append("required", isRequired());
        toString.append("spec", getSpecification().getFullIdentifier());
        toString.append("object", adapter == null ? "null" : adapter.titleString());
        return toString.toString();
    }

    @Override
    public String getParameterName() {
        return objectActionParameter.getName();
    }

    @Override
    public ObjectSpecification getSpecification() {
        return objectActionParameter.getSpecification();
    }

    @Override
    public String getDescription() {
        final String title = adapter == null ? "" : ": " + adapter.titleString();
        final String name = getParameterName();
        final ObjectSpecification specification = objectActionParameter.getSpecification();
        final String specName = specification.getShortIdentifier();
        final String type = name.indexOf(specName) == -1 ? " (" + specName + ")" : "";
        return name + type + title + " " + objectActionParameter.getDescription();
    }

    @Override
    public String getHelp() {
        return invocation.getHelp();
    }

    @Override
    public String getId() {
        return null;
    }
}
