package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.TextEntryParseException;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.object.security.Session;


public class ValueField extends ObjectField implements ValueContent {
    // TODO change to NakedValue
    private NakedValue object;

    public ValueField(NakedObject parent, NakedValue object, OneToOneAssociation association) {
        super(parent, association);
        this.object = object;
    }

    public String debugDetails() {
        return super.debugDetails() + "  object:    " + object + "\n" + "  parent:    " + getParent() + "\n";
    }

    public String getIconName() {
        return object.getIconName();
    }

    public Naked getNaked() {
        return object;
    }

    public NakedValue getObject() {
        return object;
    }

    private OneToOneAssociation getOneToOneAssociation() {
        return (OneToOneAssociation) getField();
    }

    public NakedObjectSpecification getSpecification() {
        return getOneToOneAssociation().getSpecification();
    }

    private String getValue() {
        return null;
    }

    public Hint getValueHint(Session session, String entryText) {
        NakedValue example = (NakedValue) getSpecification().acquireInstance();
        try {
            example.parseTextEntry(entryText);
        } catch (final InvalidEntryException e) {
            return new DefaultHint() {
                public Consent isValid() {
                    return new Veto(e.getMessage());
                }
            };
        }
        // TODO need the Value object to parse the entry string
        return getParent().getHint(ClientSession.getSession(), (NakedObjectField) getField(), example);
    }

    public boolean isTransient() {
        return false;
    }

    public void parseEntry(String entryText) throws TextEntryParseException, InvalidEntryException {
        //getParent().parseTextEntry(getOneToOneAssociation(), entryText);
        if(object == null) {			
            object = (NakedValue) getSpecification().acquireInstance();
        } 
        object.parseTextEntry(entryText);
        getParent().setValue(getOneToOneAssociation(), object.getObject());
        
    }

    public String toString() {
        return getValue() + "/" + getField();
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