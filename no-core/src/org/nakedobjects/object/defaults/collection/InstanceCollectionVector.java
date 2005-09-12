package org.nakedobjects.object.defaults.collection;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.defaults.InternalNakedObject;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.utility.Assert;

import java.util.Enumeration;
import java.util.Vector;


public class InstanceCollectionVector implements TypedNakedCollection, InternalNakedObject {
    private Vector elements;
    private NakedObjectSpecification elementSpecification;

    private String name;
    private NakedObjectSpecification specification;
    private long version;

    public InstanceCollectionVector(NakedObjectSpecification elementSpecification, NakedObject[] instances) {
        this.elementSpecification = elementSpecification;
        name = elementSpecification.getPluralName();

        int size = instances.length;
        elements = new Vector(size);
        for (int i = 0; i < size; i++) {
            elements.addElement(instances[i]);
        }
    }

    public boolean contains(NakedObject object) {
        return false;
    }

    public NakedObject elementAt(int i) {
        if (i < 0 || i >= size()) {
            throw new IllegalArgumentException("No such element: " + i);
        }
        return (NakedObject) elements.elementAt(i);
    }

    public Enumeration elements() {
        return elements.elements();
    }

    public NakedObjectSpecification getElementSpecification() {
        return elementSpecification;
    }

    public Hint getHint(NakedObjectField field, Naked value) {
        return null;
    }

    public String getIconName() {
        return null;
    }

    public Object getObject() {
        return null;
    }

    public Oid getOid() {
        return null;
    }

    public NakedObjectSpecification getSpecification() {
        if (specification == null) {
            specification = NakedObjects.getSpecificationLoader().loadSpecification(this.getClass());
        }
        return specification;
    }

    public long getVersion() {
        return version;
    }

    public void init(Object[] initElements) {
        Assert.assertEquals("Collection not empty", 0, this.elements.size());
        for (int i = 0; i < initElements.length; i++) {
            elements.addElement(initElements[i]);
        }
    }
    
    public Enumeration oids() {
        return null;
    }

    public void setOid(Oid oid) {}

    public void setResolved() {}

    public int size() {
        return elements.size();
    }

    public void sort() {
        Vector sorted = new Vector(elements.size());

        outer: for (Enumeration e = elements.elements(); e.hasMoreElements();) {
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

        elements = sorted;
    }

    public String titleString() {
        //        return getElementSpecification().getPluralName() + "(" + size() + ")";
        return name + "(" + size() + ")";
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */