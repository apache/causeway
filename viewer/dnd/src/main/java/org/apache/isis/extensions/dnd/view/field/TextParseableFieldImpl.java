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


package org.apache.isis.extensions.dnd.view.field;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.metamodel.adapter.InvalidEntryException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.propparam.multiline.MultiLineFacet;
import org.apache.isis.core.metamodel.facets.propparam.typicallength.TypicalLengthFacet;
import org.apache.isis.core.metamodel.facets.propparam.validate.maxlength.MaxLengthFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.ObjectContent;
import org.apache.isis.extensions.dnd.view.content.AbstractTextParsableContent;
import org.apache.isis.runtime.context.IsisContext;


public class TextParseableFieldImpl extends AbstractTextParsableContent implements TextParseableField, ObjectContent {
    private final ObjectField field;
    private final ObjectAdapter parent;
    private ObjectAdapter object;

    public TextParseableFieldImpl(final ObjectAdapter parent, final ObjectAdapter object, final OneToOneAssociation association) {
        field = new ObjectField(parent, association);
        this.parent = parent;
        this.object = object;
    }

    public Consent canDrop(final Content sourceContent) {
        return Veto.DEFAULT;
    }

    public Consent canClear() {
        return Allow.DEFAULT; // TODO is this flagged anywhere - getValueAssociation().canClear();
    }

    public boolean canWrap() {
        return getValueAssociation().containsFacet(MultiLineFacet.class);
    }

    @Override
    public void clear() {}

    public void debugDetails(final DebugString debug) {
        field.debugDetails(debug);
        debug.appendln("object", object);
    }

    public ObjectAdapter drop(final Content sourceContent) {
        return null;
    }

    @Override
    public void entryComplete() {
        getValueAssociation().setAssociation(getParent(), object);
    }

    public String getDescription() {
        final String title = object == null ? "" : ": " + object.titleString();
        final String name = field.getName();
        final ObjectSpecification specification = getSpecification();
        final String type = name.indexOf(specification.getShortName()) == -1 ? "" : " (" + specification.getShortName() + ")";
        final String description = getValueAssociation().getDescription();
        return name + type + title + " " + description;
    }

    public String getHelp() {
        return field.getHelp();
    }

    public String getFieldName() {
        return field.getName();
    }

    public ObjectAssociation getField() {
        return field.getObjectAssociation();
    }

    public String getIconName() {
        return object == null ? "" : object.getIconName();
    }

    public ObjectAdapter getAdapter() {
        return object;
    }

    public String getId() {
        return field.getName();
    }

    public ObjectAdapter[] getOptions() {
        return getValueAssociation().getChoices(getParent());
    }

    private OneToOneAssociation getValueAssociation() {
        return (OneToOneAssociation) getField();
    }

    public int getMaximumLength() {
        return maxLengthFacet().value();
    }

    public int getTypicalLineLength() {
        final TypicalLengthFacet facet = field.getObjectAssociation().getFacet(TypicalLengthFacet.class);
        return facet.value();
    }

    public int getNoLines() {
        return multilineFacet().numberOfLines();
    }

    public ObjectAdapter getParent() {
        return field.getParent();
    }

    public ObjectSpecification getSpecification() {
        return getValueAssociation().getSpecification();
    }

    @Override
    public Consent isEditable() {
        return getValueAssociation().isUsable(IsisContext.getAuthenticationSession(), getParent());
    }

    @Override
    public boolean isEmpty() {
        return getField().isEmpty(getParent());
    }

    public boolean isMandatory() {
        return getValueAssociation().isMandatory();
    }

    public boolean isOptionEnabled() {
        return getValueAssociation().hasChoices();
    }

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
        	return p.parseTextEntry(object, entryText);
        } catch(IllegalArgumentException ex) {
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

    public Consent canSet(ObjectAdapter dragSource) {
        return Veto.DEFAULT;
    }

    public ObjectAdapter getObject() {
        return object;
    }

    public void setObject(ObjectAdapter object) {
        this.object = object;
        ((OneToOneAssociation) field.getObjectAssociation()).setAssociation(getParent(), object);
    }

}
