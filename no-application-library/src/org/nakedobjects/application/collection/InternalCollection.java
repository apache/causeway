package org.nakedobjects.application.collection;

import java.util.Enumeration;
import java.util.Vector;


public class InternalCollection {
    private Vector elements;
    private String type;

    public InternalCollection(String type) {
        elements = new Vector();
        this.type = type;
    }

    public void add(Object object) {
        if (object == null) {
            throw new NullPointerException("Cannot add null");
        }
        elements.addElement(object);
    }

    /**
     * Returns true if the logical collection contains the specified object.
     */
    public boolean contains(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("null is not a valid element for a collection");
        }
        return elements.contains(object);
    }

    public Object elementAt(int index) {
        return elements.elementAt(index);
    }

    /**
     * Return all elements in this collection
     */
    public Enumeration elements() {
        return elements.elements();
    }

    public String getType() {
        return type;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public void remove(Object object) {
        if (object == null) {
            throw new NullPointerException("Cannot remove null");
        }
        elements.removeElement(object);
    }

    public void removeAllElements() {
        elements.removeAllElements();
    }

    public int size() {
        return elements.size();
    }

    public String toString() {
        StringBuffer s = new StringBuffer();

        s.append("InternalCollectionVector");
        s.append(" [");

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
