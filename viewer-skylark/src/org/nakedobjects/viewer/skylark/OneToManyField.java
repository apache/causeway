package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.object.reflect.OneToManyAssociation;

import java.util.Enumeration;


public class OneToManyField extends ObjectField implements CollectionContent {
    private final NakedCollection collection;

    public OneToManyField(NakedObject parent, InternalCollection object, OneToManyAssociation association) {
        super(parent, association);
        this.collection = (NakedCollection) object;
    }

    public Enumeration allElements() {
//        return getParent().getFieldElements(getOneToManyAssociation());
        return getCollection().elements();
    }

    public Consent canClear() {
        return Veto.DEFAULT;
    }
    
    public Consent canSet(NakedObject dragSource) {
        return Veto.DEFAULT;
    }

    public void clear() {
        throw new NakedObjectRuntimeException("Invalid call");    
    }

    public String debugDetails() {
        return super.debugDetails() + "  object: collection\n";
    }

    public String getIconName() {
        return "internal-collection";
    }
    
    public NakedCollection getCollection() {
        return collection;
    }

    public Naked getNaked() {
        return collection;
    }

    public OneToManyAssociation getOneToManyAssociation() {
        return (OneToManyAssociation) getField();
    }
    
    public boolean isTransient() {
        return false;
    }
    
    public void setObject(NakedObject object) {
        throw new NakedObjectRuntimeException("Invalid call");
    }

    public String toString() {
        return collection + "/" + getField();
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