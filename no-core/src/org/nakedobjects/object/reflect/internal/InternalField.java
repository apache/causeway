package org.nakedobjects.object.reflect.internal;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.Session;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.utility.NotImplementedException;

import java.lang.reflect.Method;


public abstract class InternalField extends InternalMember {
    protected final Method getMethod;
    private final boolean isDerived;
    protected final Class type;

    public InternalField(Class type, Method get, boolean isDerived) {
        this.type = type;
        this.isDerived = isDerived;
        this.getMethod = get;
    }

    public Object getExtension(Class cls) {
        return null;
    }

    public Class[] getExtensions() {
        return new Class[0];
    }

    public String getName() {
        return null;
    }

    /**
     * return the object type, as a Class object, that the method returns.
     */
    public NakedObjectSpecification getType() {
        return type == null ? null : NakedObjects.getSpecificationLoader().loadSpecification(type);
    }

    public boolean isAccessible() {
        return true;
    }

    public boolean isAuthorised(Session session) {
        return true;
    }

    /**
     * Returns true if this attribute is derived - is calculated from other data in the object - and should
     * therefore not be editable nor persisted.
     */
    public boolean isDerived() {
        return isDerived;
    }

    public Consent isEditable(NakedObject target) {
        return Allow.DEFAULT;
    }

    public boolean isEmpty(NakedObject inObject) {
        throw new NotImplementedException();
    }

    public boolean isHidden() {
        return false;
    }

    public boolean isMandatory() {
        return false;
    }

    public Consent isAvailable(NakedObject target) {
        return Allow.DEFAULT;
    }
    
    /** @deprecated */
    public Consent isUsable(NakedObject target) {
        return Allow.DEFAULT;
    }

    public Consent isVisible(NakedObject target) {
        return Allow.DEFAULT;
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
