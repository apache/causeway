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

package org.apache.isis.viewer.dnd.view.field;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.dnd.view.UserAction;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.content.AbstractObjectContent;

public class OneToOneFieldImpl extends AbstractObjectContent implements OneToOneField {
    private static final UserAction CLEAR_ASSOCIATION = new ClearOneToOneAssociationOption();
    private final ObjectField field;
    private final ObjectAdapter adapter;

    public OneToOneFieldImpl(final ObjectAdapter parentAdapter, final ObjectAdapter adapter, final OneToOneAssociation association) {
        field = new ObjectField(parentAdapter, association);
        this.adapter = adapter;
    }

    @Override
    public Consent canClear() {
        final ObjectAdapter parentAdapter = getParent();
        final OneToOneAssociation association = getOneToOneAssociation();
        // ObjectAdapter associatedObject = getObject();

        final Consent isEditable = isEditable();
        if (isEditable.isVetoed()) {
            return isEditable;
        }

        final Consent consent = association.isAssociationValid(parentAdapter, null);
        if (consent.isAllowed()) {
            consent.setDescription("Clear the association to this object from '" + parentAdapter.titleString() + "'");
        }
        return consent;
    }

    @Override
    public Consent canSet(final ObjectAdapter adapter) {
        final ObjectSpecification targetType = getOneToOneAssociation().getSpecification();
        final ObjectSpecification spec = adapter.getSpecification();

        if (isEditable().isVetoed()) {
            return isEditable();
        }

        if (!spec.isOfType(targetType)) {
            // TODO: move logic into Facet
            return new Veto(String.format("Can only drop objects of type %s", targetType.getSingularName()));
        }

        if (getParent().representsPersistent() && adapter.isTransient()) {
            // TODO: move logic into Facet
            return new Veto("Can't drop a non-persistent into this persistent object");
        }

        final Consent perm = getOneToOneAssociation().isAssociationValid(getParent(), adapter);
        return perm;
    }

    @Override
    public void clear() {
        getOneToOneAssociation().clearAssociation(getParent());
    }

    @Override
    public void debugDetails(final DebugBuilder debug) {
        field.debugDetails(debug);
        debug.appendln("object", adapter);
    }

    @Override
    public String getFieldName() {
        return field.getName();
    }

    @Override
    public ObjectAssociation getField() {
        return field.getObjectAssociation();
    }

    @Override
    public Consent isEditable() {
        return getField().isUsable(IsisContext.getAuthenticationSession(), getParent(), where);
    }

    @Override
    public ObjectAdapter getAdapter() {
        return adapter;
    }

    @Override
    public ObjectAdapter getObject() {
        return adapter;
    }

    private OneToOneAssociation getOneToOneAssociation() {
        return (OneToOneAssociation) getField();
    }

    @Override
    public ObjectAdapter[] getOptions() {
        return getOneToOneAssociation().getChoices(getParent());
    }

    @Override
    public ObjectAdapter getParent() {
        return field.getParent();
    }

    @Override
    public ObjectSpecification getSpecification() {
        return getOneToOneAssociation().getSpecification();
    }

    @Override
    public boolean isMandatory() {
        return getOneToOneAssociation().isMandatory();
    }

    @Override
    public boolean isPersistable() {
        return getObject() != null && super.isPersistable();
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public boolean isOptionEnabled() {
        return getOneToOneAssociation().hasChoices();
    }

    @Override
    public boolean isTransient() {
        return adapter != null && adapter.isTransient();
    }

    @Override
    public void contentMenuOptions(final UserActionSet options) {
        super.contentMenuOptions(options);
        if (getObject() != null && !getOneToOneAssociation().isMandatory()) {
            options.add(CLEAR_ASSOCIATION);
        }
    }

    @Override
    public void setObject(final ObjectAdapter object) {
        getOneToOneAssociation().setAssociation(getParent(), object);
    }

    @Override
    public String title() {
        return adapter == null ? "" : adapter.titleString();
    }

    @Override
    public String toString() {
        return getObject() + "/" + getField();
    }

    @Override
    public String windowTitle() {
        return field.getName() + " for " + field.getParent().titleString();
    }

    @Override
    public String getId() {
        return getOneToOneAssociation().getName();
    }

    @Override
    public String getDescription() {
        final String name = getFieldName();
        String type = getField().getSpecification().getSingularName();
        type = name.indexOf(type) == -1 ? " (" + type + ")" : "";
        final String description = getOneToOneAssociation().getDescription();
        return name + type + " " + description;
    }

    @Override
    public String getHelp() {
        return getOneToOneAssociation().getHelp();
    }
}
