package org.nakedobjects.object;

import java.util.Enumeration;

import org.nakedobjects.object.control.Permission;


public interface NakedCollection extends NakedObject {
	
    /**
     adds <code>object</code> to the collection and notifies all views that the collection has changed.
     */
    public void add(NakedObject object);
    
    public void added(NakedObject object);

    /**
     Vetos the addition to this collection if the object being added requests it.  Returns the
     result of <code>canAddTo()</code> called on the object reference. Also disallows the addition of itself.
     By default a collection can be added to another collection (but not to itself).   Any type of object
     which wishes to restrict its placement in a collection should override this method.
     */
    public Permission canAdd(NakedObject object);

    /**
     Vetos the removal from this collection if the object being removed requests it.  Returns the
     result of <code>canRemoveFrom()</code> called on the object reference.
     */
    public Permission canRemove(NakedObject object);

    /**
     Returns true if the logical collection contains the specified object. 
     */
    public boolean contains(NakedObject object);

    /**
      *  Return cache to be viewed on current page
      */
    public Enumeration displayElements();

    /**
     *  Return all elements in this collection
     */
    public Enumeration elements();

    /**
     *  Position cursor at first element
     */
    public void first();
    
	public int getDisplaySize();

    /**
     *  If true there is a next page to display, and 'next' and 'last' options are valid
     */
    public boolean hasNext();

    public boolean hasPrevious();

    /**
     *  Position cursor at last
     */
    public void last();

    /**
     *  Position cursor at beginning of next page
     */
    public void next();
    
	public int position();

    /**
    *  Position cursor at beginning of previous page
    */
    public void previous();

    /**
     removes <code>object</code> from the collection and notifies all views that the collection has changed.
     */
    public void remove(NakedObject element);
    
    public void removed(NakedObject element);

    /**
     *  Return a NakedCollection of objects which match the specified pattern from within the current collection
     */

    public int size();

    /**
     by default returns the collections name and its number of elements
     */
    public Title title();

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