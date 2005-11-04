package org.nakedobjects.object.defaults;

import org.nakedobjects.object.InternalNakedObject;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.utility.Assert;

import java.util.Enumeration;
import java.util.Vector;


public class InstanceCollectionVector extends AbstractNakedReference implements TypedNakedCollection, InternalNakedObject {
    private String name;
    private Vector instances;
    private NakedObjectSpecification instanceSpecification;

    public InstanceCollectionVector(NakedObjectSpecification elementSpecification, NakedObject[] instances) {
        this.instanceSpecification = elementSpecification;
        name = elementSpecification.getPluralName();

        int size = instances.length;
        this.instances = new Vector(size);
        for (int i = 0; i < size; i++) {
            this.instances.addElement(instances[i]);
        }
    }

    public NakedObject elementAt(int i) {
        if (i < 0 || i >= size()) {
            throw new IllegalArgumentException("No such element: " + i);
        }
        return (NakedObject) instances.elementAt(i);
    }

    public String titleString() {
        //        return getElementSpecification().getPluralName() + "(" + size() + ")";
        return name + ", " + size();
    }

    public int size() {
        return instances.size();
    }

    public void setOid(Oid oid) {}
    
    public NakedObjectSpecification getElementSpecification() {
        return instanceSpecification;
    }

    public boolean contains(NakedObject object) {
        return false;
    }

    public void destroyed() {}

    public Enumeration elements() {
        return instances.elements();
    }

    public String getIconName() {
        return null;
    }

    public Object getObject() {
        return instances;
    }
    
    public void init(Object[] initElements) {
        Assert.assertEquals("Collection not empty", 0, this.instances.size());
        for (int i = 0; i < initElements.length; i++) {
            instances.addElement(initElements[i]);
        }
    }

    public void sort() {
        Vector sorted = new Vector(instances.size());

        outer: for (Enumeration e = instances.elements(); e.hasMoreElements();) {
            NakedObject element = (NakedObject) e.nextElement();
            String title = element.titleString();

            int i = 0;
            for (Enumeration f = sorted.elements(); f.hasMoreElements();) {
                NakedObject sortedElement = (NakedObject) f.nextElement();
                String sortedTitle = sortedElement.titleString();
                if (sortedTitle.compareTo(title) > 0) {
                    sorted.insertElementAt(element, i);
                    continue outer;
                }
                i++;
            }
            sorted.addElement(element);
        }

        instances = sorted;
    }

    public Persistable persistable() {
        return Persistable.TRANSIENT;
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
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
