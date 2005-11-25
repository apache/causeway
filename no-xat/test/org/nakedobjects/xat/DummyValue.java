package org.nakedobjects.xat;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.OneToOneAssociation;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;

public class DummyValue implements OneToOneAssociation {

    public void clearAssociation(NakedObject inObject, NakedObject associate) {}

    public void clearValue(NakedObject inObject) {}

    public void initAssociation(NakedObject inObject, NakedObject associate) {}

    public void initValue(NakedObject inObject, Object value) {}

    public void setAssociation(NakedObject inObject, NakedObject associate) {}

    public void setValue(NakedObject inObject, Object value) {}

    public Consent isAssociationValid(NakedObject inObject, NakedObject associate) {
        return null;
    }

    public Consent isValueValid(NakedObject inObject, NakedValue value) {
        return null;
    }

    public Naked get(NakedObject fromObject) {
        return null;
    }

    public Class[] getExtensions() {
        return null;
    }

    public NakedObjectSpecification getSpecification() {
        return null;
    }

    public boolean isCollection() {
        return false;
    }

    public boolean isDerived() {
        return false;
    }

    public boolean isEmpty(NakedObject adapter) {
        return false;
    }

    public boolean isMandatory() {
        return false;
    }

    public boolean isObject() {
        return false;
    }

    public boolean isValue() {
        return false;
    }

    public String getDescription() {
        return null;
    }

    public Object getExtension(Class cls) {
        return null;
    }

    public String getId() {
        return null;
    }

    public String getName() {
        return null;
    }

    public boolean isAuthorised() {
        return true;
    }

    public Consent isUsable(NakedObject target) {
        return null;
    }

    public Consent isVisible(NakedObject target) {
        return Allow.DEFAULT;
    }

    public boolean isHidden() {
        return false;
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