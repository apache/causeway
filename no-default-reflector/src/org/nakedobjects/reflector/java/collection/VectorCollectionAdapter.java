package org.nakedobjects.reflector.java.collection;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.defaults.AbstractNakedReference;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.utility.ToString;

import java.util.Enumeration;
import java.util.Vector;


public class VectorCollectionAdapter extends AbstractNakedReference implements InternalCollection {
    private Vector collection;
    private NakedObjectSpecification elementSpecification;

    public VectorCollectionAdapter(Vector vector, NakedObjectSpecification spec) {
        this.collection = vector;
        elementSpecification = spec;
    }

    private NakedObject adapter(Object element) {
        return NakedObjects.getObjectLoader().getAdapterForElseCreateAdapterForTransient(element);
    }

    public boolean contains(NakedObject object) {
        return collection.contains(object.getObject());
    }

    public void destroyed() {}

    public NakedObject elementAt(int index) {
        Object element = collection.elementAt(index);
        return adapter(element);
    }

    public Enumeration elements() {
        final Enumeration elements = collection.elements();

        return new Enumeration() {
            public boolean hasMoreElements() {
                return elements.hasMoreElements();
            }

            public Object nextElement() {
                Object element = elements.nextElement();
                return adapter(element);
            }
        };
    }

    public NakedObjectSpecification getElementSpecification() {
        return elementSpecification;
    }

    public Object getObject() {
        return collection;
    }

    public void init(Object[] initElements) {
        Assert.assertEquals("Collection not empty", 0, this.collection.size());
        for (int i = 0; i < initElements.length; i++) {
            collection.addElement(initElements[i]);
        }
    }

    public boolean isAggregated() {
        return false;
    }

    public NakedObject parent() {
        return null;
    }

    public Persistable persistable() {
        return Persistable.TRANSIENT;
    }

    public int size() {
        return collection.size();
    }

    public void sort() {}

    public String titleString() {
        return "Vector";
    }

    public String toString() {
        ToString s = new ToString(this);
        toString(s);
        s.append("elements", elementSpecification.getFullName());

        // title
        String title;
        try {
            title = "'" + this.titleString() + "'";
        } catch (NullPointerException e) {
            title = "none";
        }
        s.append("title", title);

        s.append("vector", collection);

        return s.toString();
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