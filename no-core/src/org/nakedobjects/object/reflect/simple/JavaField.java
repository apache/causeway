/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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

package org.nakedobjects.object.reflect.simple;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.nakedobjects.object.CompositePart;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedValue;


public abstract class JavaField extends JavaMember {
    final static Logger LOG = Logger.getLogger(JavaAction.class);

    protected final Method getMethod;

    private Class type;
    private boolean isDerived;

    public JavaField(String name, Class type, Method get, Method about, boolean isDerived) {
        super(name, about);
        this.type = type;
        this.isDerived = isDerived;
        this.getMethod = get;
    }

   public Naked get(NakedObject fromObject) {
       try {
           return (Naked) getMethod.invoke(fromObject, new Object[0]);
       } catch (InvocationTargetException e) {
           LOG.error("Exception executing " + getMethod, e.getTargetException());
       } catch (IllegalAccessException ignore) {
           LOG.error("Illegal access of " + getMethod, ignore);
       }
       return null;
   }

   /**
     return the object type, as a Class object, that the method returns.
     */
    public Class getType() {
        return type;
    }

    /**
     * Determines if this field hold a part, i.e. an object that is part of a composite object.
     * @return boolean
     */
    public boolean isPart() {
        return type != null && CompositePart.class.isAssignableFrom(type);
    }

    /**
     Returns true if this attribute is derived - is calculated from other data in the object - and should
     therefore not be editable nor persisted.
     */
    public boolean isDerived() {
        return isDerived;
    }

    /**
     * 
     * @return boolean
     */
    public boolean isValue() {
        return type != null && NakedValue.class.isAssignableFrom(type);
    }
}
