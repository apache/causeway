package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.TextEntryParseException;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.object.security.Session;


public class ValueField extends ObjectField implements ValueContent {
    private final NakedObject object;

    public ValueField(NakedObject parent, NakedObject object, OneToOneAssociation association) {
        super(parent, association);
        this.object = object;
    }
    
    public NakedObject getObject() {
        return object;
    }

    public String debugDetails() {
        return super.debugDetails() + "  object:    " + object + "\n" + "  parent:    " + getParent() + "\n";
    }

    private OneToOneAssociation getOneToOneAssociation() {
        return (OneToOneAssociation) getField();
    }

    public NakedObjectSpecification getSpecification() {
        return getOneToOneAssociation().getSpecification();
    }

    public String toString() {
        return getValue() + "/" + getField();
    }

    private String getValue() {
        return null;
    }

    public void parseEntry(String entryText) throws TextEntryParseException, InvalidEntryException {
        getParent().parseTextEntry(getOneToOneAssociation(), entryText);
    }

    public Hint getValueHint(Session session, String entryText) {
        NakedObject example = (NakedObject) getSpecification().acquireInstance();
        return getParent().getHint(ClientSession.getSession(), (NakedObjectField) getField(), example);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */