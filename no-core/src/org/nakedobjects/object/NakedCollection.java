package org.nakedobjects.object;

import java.util.Enumeration;

import org.nakedobjects.object.control.Permission;


public interface NakedCollection extends NakedObject {
	

    void addAll(NakedCollection coll);

    /**
     adds <code>object</code> to the collection and notifies all views that the collection has changed.
     */
    void add(NakedObject object);
    
    void added(NakedObject object);

    /**
     Vetos the addition to this collection if the object being added requests it.  Returns the
     result of <code>canAddTo()</code> called on the object reference. Also disallows the addition of itself.
     By default a collection can be added to another collection (but not to itself).   Any type of object
     which wishes to restrict its placement in a collection should override this method.
     */
    Permission canAdd(NakedObject object);

    /**
     Vetos the removal from this collection if the object being removed requests it.  Returns the
     result of <code>canRemoveFrom()</code> called on the object reference.
     */
    Permission canRemove(NakedObject object);

    /**
     Returns true if the logical collection contains the specified object. 
     */
    boolean contains(NakedObject object);

    /**
     *  Return all elements in this collection
     */
    Enumeration elements();

    /**
     removes <code>object</code> from the collection and notifies all views that the collection has changed.
     */
    void remove(NakedObject element);

    /**
     * Removes all objects from the collection.
     */
    public void removeAll();
    
    void removed(NakedObject element);

    /**
     *  Return a NakedCollection of objects which match the specified pattern from within the current collection
     */

    int size();
    
    boolean isEmpty();
    
    NakedObject elementAt(int index);
}


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