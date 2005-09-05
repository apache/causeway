package org.nakedobjects.object.reflect.internal;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.utility.NotImplementedException;

import java.util.Enumeration;
import java.util.Vector;


public class InternalCollectionVectorAdapter implements InternalCollection {
    private Vector collection;
    private NakedObjectSpecification elementSpecification;
    private NakedObjectSpecification specification;
    private long version;

    public InternalCollectionVectorAdapter(Vector vector, Class type) {
        this.collection = vector;

        Class t = type == null ? Object.class : type;
        elementSpecification = NakedObjects.getSpecificationLoader().loadSpecification(t);
    }

    public boolean contains(NakedObject object) {
        return collection.contains(object.getObject());
    }

    public NakedObject elementAt(int index) {
        Object element = collection.elementAt(index);
        return NakedObjects.getObjectLoader().getAdapterForElseCreateAdapterForTransient(element);
    }

    public Enumeration elements() {
        final Enumeration elements = collection.elements();

        return new Enumeration() {
            public boolean hasMoreElements() {
                return elements.hasMoreElements();
            }

            public Object nextElement() {
                Object element = elements.nextElement();
                return element instanceof NakedObject ? element : NakedObjects.getObjectLoader().getAdapterForElseCreateAdapterForTransient(element); 
                //NakedObjects.getPojoAdapterFactory().createNOAdapter(element);
            }
        };
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
        return collection;
    }

    public Oid getOid() {
        return null;
    }

    public NakedObjectSpecification getSpecification() {
        if (specification == null) {
            specification = NakedObjects.getSpecificationLoader().loadSpecification(getObject().getClass());
        }
        return specification;
    }

    public long getVersion() {
        return version;
    }

    public boolean isAggregated() {
        return false;
    }

    public Enumeration oids() {
        throw new NotImplementedException();
    }

    public NakedObject parent() {
        return null;
    }

    public void setOid(Oid oid) {}

    public void setResolved() {}

    public int size() {
        return collection.size();
    }

    public void sort() {}

    public String titleString() {
        return "vector...";
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