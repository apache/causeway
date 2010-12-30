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

import org.apache.log4j.Logger;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent2.Consent;
import org.apache.isis.core.metamodel.consent2.Veto;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.content.AbstractObjectContent;


public class OneToManyFieldElementImpl extends AbstractObjectContent implements OneToManyFieldElement {
    private static final Logger LOG = Logger.getLogger(OneToManyFieldElementImpl.class);
    private final ObjectAdapter element;
    private final ObjectField field;

    public OneToManyFieldElementImpl(final ObjectAdapter parent, final ObjectAdapter element, final OneToManyAssociation association) {
        field = new ObjectField(parent, association);
        this.element = element;
    }

    @Override
    public Consent canClear() {
        final ObjectAdapter parentObject = getParent();
        final OneToManyAssociation association = getOneToManyAssociation();
        final ObjectAdapter associatedObject = getObject();

        final Consent isEditable = isEditable();
        if (isEditable.isVetoed()) {
            return isEditable;
        }

        final Consent consent = association.isValidToRemove(parentObject, associatedObject);
        if (consent.isAllowed()) {
            consent.setDescription("Clear the association to this object from '" + parentObject.titleString() + "'");
        }
        return consent;
    }

    public Consent isEditable() {
        return getField().isUsable(IsisContext.getAuthenticationSession(), getParent());
    }

    @Override
    public Consent canSet(final ObjectAdapter dragSource) {
        return Veto.DEFAULT;
    }

    @Override
    public void clear() {
        final ObjectAdapter parentObject = getParent();
        final OneToManyAssociation association = getOneToManyAssociation();
        LOG.debug("remove " + element + " from " + parentObject);
        association.removeElement(parentObject, element);
    }

    public void debugDetails(final DebugString debug) {
        field.debugDetails(debug);
        debug.appendln("element", element);
    }

    public String getFieldName() {
        return field.getName();
    }

    public ObjectAssociation getField() {
        return field.getObjectAssociation();
    }

    public ObjectAdapter getAdapter() {
        return element;
    }

    @Override
    public ObjectAdapter getObject() {
        return element;
    }

    public ObjectAdapter[] getOptions() {
        return null;
    }

    private OneToManyAssociation getOneToManyAssociation() {
        return (OneToManyAssociation) field.getObjectAssociation();
    }

    public ObjectAdapter getParent() {
        return field.getParent();
    }

    public ObjectSpecification getSpecification() {
        return field.getSpecification();
    }

    public boolean isMandatory() {
        return false;
    }

    @Override
    public boolean isObject() {
        return true;
    }

    public boolean isOptionEnabled() {
        return false;
    }

    public boolean isTransient() {
        return false;
    }

    @Override
    public void contentMenuOptions(final UserActionSet options) {
        // ObjectOption.menuOptions(element, options);
        super.contentMenuOptions(options);
        options.add(new ClearOneToManyAssociationOption());
    }

    @Override
    public void setObject(final ObjectAdapter object) {
    /*
     * ObjectAdapter parentObject = getParent(); OneToManyAssociationSpecification association =
     * getOneToManyAssociation(); ObjectAdapter associatedObject = getObject(); LOG.debug("remove " +
     * associatedObject + " from " + parentObject); association.clearAssociation(parentObject,
     * associatedObject);
     */

    }

    public String title() {
        return element.titleString();
    }

    @Override
    public String toString() {
        return getObject() + "/" + field.getObjectAssociation();
    }

    @Override
    public String windowTitle() {
        return field.getName() + " element" + " for " + field.getParent().titleString();
    }

    public String getId() {
        return getOneToManyAssociation().getName();
    }

    public String getDescription() {
        return field.getName() + ": " + getOneToManyAssociation().getDescription();
    }

    public String getHelp() {
        return getOneToManyAssociation().getHelp();
    }
}
