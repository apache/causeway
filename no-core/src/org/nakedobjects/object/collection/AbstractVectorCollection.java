package org.nakedobjects.object.collection;

import org.nakedobjects.object.AbstractNakedObject;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.utility.NotImplementedException;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;


/**
 * This is the new type of collection for Naked Objects. It is designed to work
 * efficiently is a distributed envirmoment by providing a window on a logical
 * collection. This collection is therefore just a cache of a subset of the
 * complete collection. The real collection is maintained by the ObjectStore.
 * Within this cache there is also a viewing window, which the user is expected
 * to be viewing. As the user pages through the cache more items will be
 * retrieved as the bounds of the cache are hit.
 * 
 * @see #setDisplaySize(int) to set the size of the viewing window. To move
 *              through the collection on a page-by-page basis
 * @see #first()
 * @see #next()
 * @see #last()
 * @see #previous()
 */
public abstract class AbstractVectorCollection extends AbstractNakedObject implements NakedCollection {
    final static Logger LOG = Logger.getLogger(AbstractVectorCollection.class);
    protected Vector elements;

    public AbstractVectorCollection() {
        elements = new Vector();
        reset();
    }

    public void add(NakedObject object) {
        if (object == null) {
            throw new NullPointerException("Cannot add null");
        }
        resolve();
        if (canAdd(object).isAllowed()) {
            elements.addElement(object);
            objectChanged();
        }
    }

    /**
     * Add all elements from the collection. If the reference to the collection
     * is null, then does nothing.
     * <p>
     * Implementation note: this code is not threadsafe and does not synchronize
     * on either itself or the collection passed in.
     * </p>
     */
    public void addAll(final NakedCollection collection) {
        resolve();
        if (collection == null) {
            return;
        }

        for (Enumeration enum = collection.elements(); enum.hasMoreElements();) {
            NakedObject obj = (NakedObject) enum.nextElement();
            this.add(obj);
        }
        this.objectChanged();
    }

    public void added(NakedObject object) {
        elements.addElement(object);
    }

    public abstract Permission canAdd(NakedObject object);

    public abstract Permission canRemove(NakedObject object);

    /**
     * Returns true if the logical collection contains the specified object.
     */
    public boolean contains(NakedObject object) {
        resolve();
        return elements.contains(object);
    }

    public NakedObject elementAt(int index) {
        return (NakedObject) elements.elementAt(index);
    }
    
    /**
     * Return all elements in this collection
     */
    public Enumeration elements() {
        resolve();
        return new Enumeration() {
            Enumeration e = elements.elements();

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

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean isSameAs(Naked object) {
        if (super.isSameAs(object)) {
            return true;
        }

        if (object instanceof NakedCollection) {
            NakedCollection collection = (NakedCollection) object;

            if (size() == collection.size()) {
                Enumeration e = elements();
                while (e.hasMoreElements()) {
                    if (!collection.contains((NakedObject) e.nextElement())) {
                        break;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public void remove(NakedObject object) {
        if (object == null) {
            throw new NullPointerException("Cannot remove null");
        }
        resolve();
        elements.removeElement(object);
        objectChanged();
    }

    /**
     * Removes all objects from the collection.
     * <p>
     * Implementation note: this code is not threadsafe; it does not synchronize
     * on itself.
     * </p>
     * <p>
     * Implemented by invoking <code>remove(NakedObject)</code> on each object
     * </p>
     */
    public void removeAll() {
        resolve();
        // create array holding references to all the objects in the
        // collection. Do it this way since not sure if can delete
        // through an Enumeration
        NakedObject[] objects = new NakedObject[size()];
        int i = 0;
        for (Enumeration enum = elements(); enum.hasMoreElements();) {
            objects[i++] = (NakedObject) enum.nextElement();
        }
        // now remove them
        for (i = 0; i < objects.length; i++) {
            remove(objects[i]);
        }
    }

    public void removed(NakedObject object) {
        elements.removeElement(object);
    }

    private void reset() {
        elements.removeAllElements();
    }

    /**
     * Return a NakedCollection of objects which match the specified pattern
     * from within the current collection
     */
    public AbstractVectorCollection search(NakedObject pattern) {
        throw new NotImplementedException();
    }

    public int size() {
        return elements.size();
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

