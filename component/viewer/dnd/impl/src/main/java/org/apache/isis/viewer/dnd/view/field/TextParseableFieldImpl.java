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
import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.facets.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.multiline.MultiLineFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.InvalidEntryException;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.ObjectContent;
import org.apache.isis.viewer.dnd.view.content.AbstractTextParsableContent;

public class TextParseableFieldImpl extends AbstractTextParsableContent implements TextParseableField, ObjectContent {
    private final ObjectField field;
    private final ObjectAdapter parent;
    private ObjectAdapter object;

    public TextParseableFieldImpl(final ObjectAdapter parent, final ObjectAdapter object, final OneToOneAssociation association) {
        field = new ObjectField(parent, association);
        this.parent = parent;
        this.object = object;
    }

    @Override
    public Consent canDrop(final Content sourceContent) {
        return Veto.DEFAULT;
    }

    @Override
    public Consent canClear() {
        return Allow.DEFAULT; // TODO is this flagged anywhere -
                              // getValueAssociation().canClear();
    }

    @Override
    public boolean canWrap() {
        return getValueAssociation().containsFacet(MultiLineFacet.class);
    }

    @Override
    public void clear() {
    }

    @Override
    public void debugDetails(final DebugBuilder debug) {
        field.debugDetails(debug);
        debug.appendln("object", object);
    }

    @Override
    public ObjectAdapter drop(final Content sourceContent) {
        return null;
    }

    @Override
    public void entryComplete() {
        getValueAssociation().setAssociation(getParent(), object);
    }

    @Override
    public String getDescription() {
        final String title = object == null ? "" : ": " + object.titleString();
        final String name = field.getName();
        final ObjectSpecification specification = getSpecification();
        final String type = name.indexOf(specification.getShortIdentifier()) == -1 ? "" : " (" + specification.getShortIdentifier() + ")";
        final String description = getValueAssociation().getDescription();
        return name + type + title + " " + description;
    }

    @Override
    public String getHelp() {
        return field.getHelp();
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
    public String getIconName() {
        return object == null ? "" : object.getIconName();
    }

    @Override
    public ObjectAdapter getAdapter() {
        return object;
    }

    @Override
    public String getId() {
        return field.getName();
    }

    @Override
    public ObjectAdapter[] getOptions() {
        return getValueAssociation().getChoices(getParent());
    }

    private OneToOneAssociation getValueAssociation() {
        return (OneToOneAssociation) getField();
    }

    @Override
    public int getMaximumLength() {
        return maxLengthFacet().value();
    }

    @Override
    public int getTypicalLineLength() {
        final TypicalLengthFacet facet = field.getObjectAssociation().getFacet(TypicalLengthFacet.class);
        return facet.value();
    }

    @Override
    public int getNoLines() {
        return multilineFacet().numberOfLines();
    }

    @Override
    public ObjectAdapter getParent() {
        return field.getParent();
    }

    @Override
    public ObjectSpecification getSpecification() {
        return getValueAssociation().getSpecification();
    }

    @Override
    public Consent isEditable() {
        return getValueAssociation().isUsable(IsisContext.getAuthenticationSession(), getParent(), Where.ANYWHERE);
    }

    @Override
    public boolean isEmpty() {
        return getField().isEmpty(getParent());
    }

    @Override
    public boolean isMandatory() {
        return getValueAssociation().isMandatory();
    }

    @Override
    public boolean isOptionEnabled() {
        return getValueAssociation().hasChoices();
    }

    @Override
    public String titleString(final ObjectAdapter value) {
        return titleString(value, field.getObjectAssociation(), field.getSpecification());
    }

    private ObjectAdapter validateAndParse(final String entryText) {
        final ObjectAdapter newValue = parse(entryText);
        final OneToOneAssociation objectAssociation = (OneToOneAssociation) field.getObjectAssociation();
        final Consent valid = objectAssociation.isAssociationValid(parent, newValue);
        if (valid.isVetoed()) {
            throw new InvalidEntryException(valid.getReason());
        }
        return newValue;
    }

    private ObjectAdapter parse(final String entryText) {
        final ObjectSpecification fieldSpecification = field.getSpecification();
        final ParseableFacet p = fieldSpecification.getFacet(ParseableFacet.class);
        try {
            Localization localization = IsisContext.getLocalization(); 
            return p.parseTextEntry(object, entryText, localization);
        } catch (final IllegalArgumentException ex) {
            throw new InvalidEntryException(ex.getMessage(), ex);
        }
    }

    @Override
    public void parseTextEntry(final String entryText) {
        object = validateAndParse(entryText);
        final Consent valid = ((OneToOneAssociation) getField()).isAssociationValid(getParent(), object);
        if (valid.isVetoed()) {
            throw new InvalidEntryException(valid.getReason());
        }
        if (getValueAssociation().isMandatory() && object == null) {
            throw new InvalidEntryException("Mandatory field cannot be empty");
        }
    }

    @Override
    public String title() {
        return field.getName();
    }

    @Override
    public String toString() {
        return (object == null ? "null" : object.titleString()) + "/" + getField();
    }

    @Override
    public String windowTitle() {
        return title();
    }

    private MaxLengthFacet maxLengthFacet() {
        return getValueAssociation().getFacet(MaxLengthFacet.class);
    }

    private MultiLineFacet multilineFacet() {
        return getValueAssociation().getFacet(MultiLineFacet.class);
    }

    @Override
    public Consent canSet(final ObjectAdapter dragSource) {
        return Veto.DEFAULT;
    }

    @Override
    public ObjectAdapter getObject() {
        return object;
    }

    @Override
    public void setObject(final ObjectAdapter object) {
        this.object = object;
        ((OneToOneAssociation) field.getObjectAssociation()).setAssociation(getParent(), object);
    }

}
