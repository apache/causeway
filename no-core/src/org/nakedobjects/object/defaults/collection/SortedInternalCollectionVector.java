package org.nakedobjects.object.defaults.collection;

import org.nakedobjects.object.NakedObject;

import java.util.Enumeration;
import java.util.Vector;

public class SortedInternalCollectionVector extends InternalCollectionVector {

    private Sorter sorter;

    public SortedInternalCollectionVector(Class type, NakedObject parent) {
        super(type, parent);
    }

    public SortedInternalCollectionVector(String typeName, NakedObject parent) {
        super(typeName, parent);
    }
    
    public Enumeration elements() {
        if(sorter == null) {
            return super.elements();
        } else {
            resolve();
            return new Enumeration() {
                Enumeration e = sort().elements();
                
                public boolean hasMoreElements() {
                    return e.hasMoreElements();
                }

                public Object nextElement() {
                    Object next = e.nextElement();
                    getObjectManager().resolve((NakedObject) next);
                    return next;
                }
            };
        }
    }
    
    private Vector sort() {
        Vector sorted = new Vector(elements.size());
        int size = 0;
        elements:
        for(Enumeration e = elements.elements(); e.hasMoreElements(); ) {
            Object element = e.nextElement();
            for (int i = 0; i < size; i++) {
                if(sorter.compare((NakedObject) element, (NakedObject) sorted.elementAt(i)) < 0) {
                    sorted.insertElementAt(element, i);
                    size++;
                    continue elements;
                }
            }
            sorted.addElement(element);
            size++;
        }
        return sorted;
    }

    public void setIterator(Sorter sorter) {
        this.sorter = sorter;
    }

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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