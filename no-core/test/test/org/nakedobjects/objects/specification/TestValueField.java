package test.org.nakedobjects.objects.specification;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.OneToOneAssociation;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.utility.UnexpectedCallException;


public abstract class TestValueField implements OneToOneAssociation {
    public void clearAssociation(NakedObject inObject, NakedObject associate) {
        throw new UnexpectedCallException();
    }

    public String getDescription() {
        return "";
    }

    public Object getExtension(Class cls) {
        return null;
    }

    public Class[] getExtensions() {
        return new Class[0];
    }

    public void initAssociation(NakedObject inObject, NakedObject associate) {
        throw new UnexpectedCallException();
    }

    public Consent isAssociationValid(NakedObject inObject, NakedObject associate) {
        throw new UnexpectedCallException();
    }

    public boolean isAuthorised() {
        return true;
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

    public boolean isHidden() {
        return false;
    }

    public boolean isMandatory() {
        return false;
    }

    public boolean isObject() {
        return false;
    }

    public Consent isAvailable(NakedObject target) {
        return Allow.DEFAULT;
    }

    public boolean isValue() {
        return true;
    }

    public Consent isVisible(NakedObject target) {
        return Allow.DEFAULT;
    }

    public void setAssociation(NakedObject inObject, NakedObject associate) {
        throw new UnexpectedCallException();
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