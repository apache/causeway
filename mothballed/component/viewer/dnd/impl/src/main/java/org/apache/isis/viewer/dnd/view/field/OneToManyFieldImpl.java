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

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.dnd.drawing.Image;
import org.apache.isis.viewer.dnd.drawing.ImageFactory;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.collection.AbstractCollectionContent;

public class OneToManyFieldImpl extends AbstractCollectionContent implements OneToManyField {

    // REVIEW: should provide this rendering context, rather than hardcoding.
    // the net effect currently is that class members annotated with 
    // @Hidden(where=Where.ANYWHERE) or @Disabled(where=Where.ANYWHERE) will indeed
    // be hidden/disabled, but will be visible/enabled (perhaps incorrectly) 
    // for any other value for Where
    private final Where where = Where.ANYWHERE;

    private final ObjectAdapter collection;
    private final ObjectField field;

    public OneToManyFieldImpl(final ObjectAdapter parent, final ObjectAdapter object, final OneToManyAssociation association) {
        field = new ObjectField(parent, association);
        this.collection = object;
    }

    @Override
    public Consent canDrop(final Content sourceContent) {
        if (sourceContent.getAdapter() instanceof ObjectAdapter) {
            final ObjectAdapter sourceAdapter = sourceContent.getAdapter();
            final ObjectAdapter parentAdapter = field.getParent();

            final ObjectAdapter collection = getAdapter();
            if (collection == null) {
                // TODO: move logic into Facet
                return new Veto("Collection not set up; can't add elements to a non-existant collection");
            }

            final Consent usableInState = getOneToManyAssociation().isUsable(IsisContext.getAuthenticationSession(), parentAdapter, where);
            if (usableInState.isVetoed()) {
                return usableInState;
            }

            final ObjectSpecification specification = sourceAdapter.getSpecification();
            final ObjectSpecification elementSpecification = getElementSpecification();
            if (!specification.isOfType(elementSpecification)) {
                // TODO: move logic into Facet
                return new Veto(String.format("Only objects of type %s are allowed in this collection", elementSpecification.getSingularName()));
            }
            if (parentAdapter.representsPersistent() && sourceAdapter.isTransient()) {
                // TODO: move logic into Facet
                return new Veto("Can't set field in persistent object with reference to non-persistent object");
            }
            return getOneToManyAssociation().isValidToAdd(parentAdapter, sourceAdapter);
        } else {
            return Veto.DEFAULT;
        }
    }

    public Consent canSet(final ObjectAdapter dragSource) {
        return Veto.DEFAULT;
    }

    @Override
    public void contentMenuOptions(final UserActionSet options) {
        super.contentMenuOptions(options);
        // OptionFactory.addCreateOptions(getOneToManyAssociation().getSpecification(),
        // options);
    }

    @Override
    public void debugDetails(final DebugBuilder debug) {
        field.debugDetails(debug);
        debug.appendln("collection", collection);
        super.debugDetails(debug);
    }

    @Override
    public ObjectAdapter drop(final Content sourceContent) {
        final ObjectAdapter object = sourceContent.getAdapter();
        final ObjectAdapter parent = field.getParent();
        final Consent perm = canDrop(sourceContent);
        if (perm.isAllowed()) {
            getOneToManyAssociation().addElement(parent, object);
        }
        return null;
    }

    @Override
    public ObjectAdapter getCollection() {
        return collection;
    }

    @Override
    public String getDescription() {
        final String name = getFieldName();
        String type = getField().getSpecification().getSingularName();
        type = name.indexOf(type) == -1 ? " (" + type + ")" : "";
        final String description = getOneToManyAssociation().getDescription();
        return name + type + " " + description;
    }

    @Override
    public ObjectAssociation getField() {
        return field.getObjectAssociation();
    }

    @Override
    public String getFieldName() {
        return field.getName();
    }

    @Override
    public String getHelp() {
        return getOneToManyAssociation().getHelp();
    }

    @Override
    public String getIconName() {
        return null;
        // return "internal-collection";
    }

    @Override
    public Image getIconPicture(final int iconHeight) {
        final ObjectSpecification specification = getOneToManyAssociation().getSpecification();
        Image icon = ImageFactory.getInstance().loadIcon(specification, iconHeight, null);
        if (icon == null) {
            icon = ImageFactory.getInstance().loadDefaultIcon(iconHeight, null);
        }
        return icon;
    }

    @Override
    public String getId() {
        return getOneToManyAssociation().getId();
    }

    @Override
    public ObjectAdapter getAdapter() {
        return collection;
    }

    @Override
    public OneToManyAssociation getOneToManyAssociation() {
        return (OneToManyAssociation) field.getObjectAssociation();
    }

    @Override
    public ObjectAdapter getParent() {
        return field.getParent();
    }

    @Override
    public ObjectSpecification getSpecification() {
        return field.getSpecification();
    }

    @Override
    public Consent isEditable() {
        return getField().isUsable(IsisContext.getAuthenticationSession(), getParent(), where);
    }

    @Override
    public boolean isMandatory() {
        return getOneToManyAssociation().isMandatory();
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    public void setObject(final ObjectAdapter object) {
        throw new IsisException("Invalid call");
    }

    @Override
    public final String title() {
        return field.getName();
    }

    @Override
    public String toString() {
        return collection + "/" + field.getObjectAssociation();
    }

    @Override
    public String windowTitle() {
        return title() + " for " + field.getParent().titleString();
    }

}
