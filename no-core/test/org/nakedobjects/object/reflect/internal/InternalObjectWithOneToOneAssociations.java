package org.nakedobjects.object.reflect.internal;

import org.nakedobjects.object.defaults.InternalNakedObject;


public class InternalObjectWithOneToOneAssociations implements InternalNakedObject {
    public String name;
    public InternalObjectForReferencing referencedObject;

    public void aboutReferencedObject(InternalAbout about, InternalObjectForReferencing person) {}

    public void associateReferencedObject(InternalObjectForReferencing person) {
        setReferencedObject(person);
    }

    public void dissociateReferencedObject(InternalObjectForReferencing person) {
        setReferencedObject(null);
    }

    public String getName() {
        if (name == null) {
            throw new IllegalStateException();
        }

        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public InternalObjectForReferencing getReferencedObject() {
        return referencedObject;
    }

    public void setReferencedObject(InternalObjectForReferencing v) {
        referencedObject = v;
    }

    public String titleString() {
        return name;
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