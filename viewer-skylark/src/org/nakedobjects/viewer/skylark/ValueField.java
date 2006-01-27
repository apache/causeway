package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.OneToOneAssociation;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.utility.DebugString;


public class ValueField extends ValueContent implements FieldContent {
    private final ObjectField field;
    private NakedValue object;

    public ValueField(NakedObject parent, NakedValue object, OneToOneAssociation association) {
        field = new ObjectField(parent, association);
        this.object = object;
    }

    public Consent canDrop(Content sourceContent) {
        return Veto.DEFAULT;
    }

    private void checkValidEntry(String entryText) {
        NakedValue example = NakedObjects.getObjectLoader().createValueInstance(getSpecification());
        example.parseTextEntry(entryText);
        Consent valid = getParent().isValid((OneToOneAssociation) getField(), example);
        if (valid.isVetoed()) {
            throw new InvalidEntryException(valid.getReason());
        }
    }

    public void clear() {
        object.clear();
    }

    public void debugDetails(DebugString debug) {
        field.debugDetails(debug);
        debug.appendln(4, "object", object);
    }

    public Naked drop(Content sourceContent) {
        return null;
    }

    public void entryComplete() {
        getParent().setValue(getOneToOneAssociation(), object.getObject());
    }

    public String getDescription() {
        return field.getDescription();
    }

    public String getFieldName() {
        return field.getName();
    }

    public NakedObjectField getField() {
        return field.getFieldReflector();
    }

    public String getIconName() {
        return object.getIconName();
    }

    public Naked getNaked() {
        return object;
    }

    public String getId() {
        return field.getName();
    }

    public NakedValue getObject() {
        return object;
    }

    private OneToOneAssociation getOneToOneAssociation() {
        return (OneToOneAssociation) getField();
    }

    private NakedObject getParent() {
        return field.getParent();
    }

    public NakedObjectSpecification getSpecification() {
        return getOneToOneAssociation().getSpecification();
    }

    public boolean isDerived() {
        return getOneToOneAssociation().isDerived();
    }

    public Consent isEditable() {
        return getOneToOneAssociation().isAvailable(getParent());
    }
    
    public boolean isEmpty() {
        return getParent().isEmpty(getField());
    }

    public boolean isMandatory() {
        return getOneToOneAssociation().isMandatory();
    }

    public void parseTextEntry(String entryText) {
        checkValidEntry(entryText);
        saveEntry(entryText);
    }

    private void saveEntry(String entryText) {
        if (object == null) {
            object = NakedObjects.getObjectLoader().createValueInstance(getSpecification());
        }
        object.parseTextEntry(entryText);
    }

    public String title() {
        return field.getName();
    }

    public String toString() {
        return (object == null ? "null" : object.titleString()) + "/" + getField();
    }

    public String windowTitle() {
        return title();
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */