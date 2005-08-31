package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;


public abstract class NakedObjectField extends NakedObjectMember {
    private final NakedObjectSpecification specification;
    
    public NakedObjectField(String name, NakedObjectSpecification type, MemberIdentifier identifier) {
        super(name, identifier);
        if(type == null)
        {
            throw new IllegalArgumentException("type cannot be null");
        }
        this.specification = type;
	}

   protected abstract Naked get(NakedObject fromObject);

    /**
     return the object type, as a Class object, that the method returns.
     */
    public NakedObjectSpecification getSpecification() {
        return specification;
    }

    public boolean isCollection() {
        return false;
    }
    
    /**
     Returns true if this attribute is derived - is calculated from other data in the object - and should
     therefore not be editable nor persisted.
     */
    // TODO confirm that Value is the only type that can be derived.  If so move it?
    public abstract boolean isDerived();

    public abstract boolean isEmpty(NakedObject adapter);

    public boolean isObject() {
		return specification.isObject();
    }
    
    public boolean isValue() {
	    return specification.isValue();
    }
    
    public boolean isMandatory() {
        return false;
    }

    public abstract Class[] getExtensions();
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/
