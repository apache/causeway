package org.nakedobjects.object.defaults.collection;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.control.defaults.Veto;
import org.nakedobjects.object.defaults.Title;


public class InternalCollectionVector extends AbstractTypedNakedCollectionVector implements InternalCollection {
    private NakedObject parent;

    public InternalCollectionVector(Class type, NakedObject parent) {
        super(type);
        this.parent = parent;
    }

    public InternalCollectionVector(String typeName, NakedObject parent) {
        super(typeName);
        this.parent = parent;
    }

    public void add(NakedObject object) {
        if (object == null) {
            throw new NullPointerException("Cannot add null");
        }
        super.add(object);
    }

    public Permission canAdd(NakedObject object) {
        if (object == parent) {
            return new Veto("Cannot add parent object");
        } else {
            return super.canAdd(object);
        }
    }

    public String debug() {
        String str = "";
        for (int i = 0; i < elements.size(); i++) {
            str += elements.elementAt(i) + "; ";
        }
        if (str.length() == 0) {
            str = "empty";
        }
        return str;
    }

    public NakedObject elementAt(int index) {
        return (NakedObject) elements.elementAt(index);
    }

    public NakedObjectContext getContext() {
        return parent.getContext();
    }

    public boolean isAggregated() {
        return parent != null;
    }

    public NakedObject parent() {
        return parent;
    }

    public void remove(NakedObject object) {
        if (object == null) {
            throw new NullPointerException("Cannot remove null");
        }
        super.remove(object);
        getObjectManager().objectChanged(parent);
    }

    public Title title() {
        return new Title();
    }

    public String toString() {
        StringBuffer s = new StringBuffer();

        s.append("InternalCollectionVector");
        s.append(" [state=");

        // Persistent/transient & Resolved or not
        s.append(isPersistent() ? "P" : (isFinder() ? "F" : "T"));
        s.append(isResolved() ? "R" : "-");

        // obect identifier
        if (getOid() != null) {
            s.append(",oid=");
            s.append(getOid().toString().toUpperCase());
        } else {
            s.append(",oid=none");
        }

        // title
        s.append(",size=");
        s.append(size());

        s.append("]");

        s.append("  " + Long.toHexString(super.hashCode()).toUpperCase());

        return s.toString();
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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
