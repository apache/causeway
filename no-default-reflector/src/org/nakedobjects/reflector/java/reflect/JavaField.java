package org.nakedobjects.reflector.java.reflect;


import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.Session;
import org.nakedobjects.object.reflect.MemberIdentifier;

import java.lang.reflect.Method;


public abstract class JavaField extends JavaMember {
    protected final Method getMethod;
    private final boolean isDerived;
    protected final  Class type;

    public JavaField(MemberIdentifier identifier, Class type, Method get, Method about, boolean isDerived) {
        super(identifier, about);
        this.type = type;
        this.isDerived = isDerived;
        this.getMethod = get;
    }

   /**
     return the object type, as a Class object, that the method returns.
     */
    public NakedObjectSpecification getType() {
        return type == null ? null : NakedObjects.getSpecificationLoader().loadSpecification(type);
    }
    
    /**
     Returns true if this attribute is derived - is calculated from other data in the object - and should
     therefore not be editable nor persisted.
     */
    public boolean isDerived() {
        return isDerived;
    }
    
    public String getDescription() {
        return "";
    }
    
    public boolean isMandatory() {
        return false;
    }

    public boolean isAuthorised(Session session) {
        return true;
    }
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
