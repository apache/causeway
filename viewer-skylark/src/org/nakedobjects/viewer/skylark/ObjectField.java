package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.utility.DebugString;


final class ObjectField {
    private final NakedObjectField field;
    private final NakedObject parent;

    ObjectField(NakedObject parent, NakedObjectField field) {
        this.parent = parent;
        this.field = field;
    }

    public void debugDetails(DebugString debug) {
        debug.appendln(4, "field", getFieldReflector());
        debug.appendln(4, "name", getName());
        debug.appendln(4, "specification", getSpecification());
        debug.appendln(4, "parent", parent);
    }

    public NakedObjectField getFieldReflector() {
        return field;
    }

     public final String getName() {
        return parent.getLabel(ClientSession.getSession(), field);
    }

    public NakedObject getParent() {
        return parent;
    }

    public NakedObjectSpecification getSpecification() {
        return field.getSpecification();
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