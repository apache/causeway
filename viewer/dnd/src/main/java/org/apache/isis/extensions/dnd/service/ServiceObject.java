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


package org.apache.isis.extensions.dnd.service;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.Veto;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.extensions.dnd.drawing.Image;
import org.apache.isis.extensions.dnd.drawing.ImageFactory;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.UserActionSet;
import org.apache.isis.extensions.dnd.view.content.AbstractContent;


public class ServiceObject extends AbstractContent {
    private final ObjectAdapter adapter;

    public ServiceObject(final ObjectAdapter adapter) {
        this.adapter = adapter;
    }

    public Consent canClear() {
        return Veto.DEFAULT;
    }

    public Consent canSet(final ObjectAdapter dragSource) {
        return Veto.DEFAULT;
    }

    public void clear() {
        throw new IsisException("Invalid call");
    }

    public void debugDetails(final DebugString debug) {
        debug.appendln("service", adapter);
    }

    public ObjectAdapter getAdapter() {
        return adapter;
    }

    public String getDescription() {
        final String specName = getSpecification().getSingularName();
        final String objectTitle = getObject().titleString();
        return specName + (specName.equalsIgnoreCase(objectTitle) ? "" : ": " + objectTitle) + " "
                + getSpecification().getDescription();
    }

    public String getHelp() {
        return "";
    }

    public String getId() {
        return "";
    }

    public ObjectAdapter getObject() {
        return adapter;
    }

    public ObjectAdapter[] getOptions() {
        return null;
    }

    public ObjectSpecification getSpecification() {
        return adapter.getSpecification();
    }

    @Override
    public boolean isObject() {
        return false;
    }

    public boolean isOptionEnabled() {
        return false;
    }

    public boolean isTransient() {
        return adapter != null && adapter.isTransient();
    }

    public void setObject(final ObjectAdapter object) {
        throw new IsisException("Invalid call");
    }

    public String title() {
        return adapter.titleString();
    }

    @Override
    public String toString() {
        return "Service Object [" + adapter + "]";
    }

    @Override
    public String windowTitle() {
        return (isTransient() ? "UNSAVED " : "") + getSpecification().getSingularName();
    }

    public Consent canDrop(final Content sourceContent) {
        ObjectAction action = actionFor(sourceContent);
        if (action == null) {
            return Veto.DEFAULT;
        } else {
            ObjectAdapter source = sourceContent.getAdapter();
            final Consent parameterSetValid = action.isProposedArgumentSetValid(adapter, new ObjectAdapter[] { source });
            parameterSetValid.setDescription("Execute '" + action.getName() + "' with " + source.titleString());
            return parameterSetValid;
        }
      }

    private ObjectAction actionFor(final Content sourceContent) {
        ObjectAction action;
        action = adapter.getSpecification().getObjectAction(ObjectActionType.USER, null,
                new ObjectSpecification[] { sourceContent.getSpecification() });
        return action;
    }

    public ObjectAdapter drop(final Content sourceContent) {
        ObjectAction action = actionFor(sourceContent);
        ObjectAdapter source = sourceContent.getAdapter();
        return action.execute(adapter, new ObjectAdapter[] { source });
    }

    public String getIconName() {
        final ObjectAdapter object = getObject();
        return object == null ? null : object.getIconName();
    }

    public Image getIconPicture(final int iconHeight) {
        final ObjectAdapter adapter = getObject();
        final ObjectSpecification specification = adapter.getSpecification();
        final Image icon = ImageFactory.getInstance().loadIcon(specification, iconHeight, null);
        return icon;
    }

    public void parseTextEntry(final String entryText) {
        throw new UnexpectedCallException();
    }

    @Override
    public void viewMenuOptions(final UserActionSet options) {}

}
