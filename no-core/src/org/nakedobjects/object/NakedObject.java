
package org.nakedobjects.object;

/**
 * Definition of an naked reference object.
 * <p>
 * A basic implementation is provided by AbstractNakedObject.
 * </p>
 * <p>
 * A naked reference object must do the following:-
 * </p>
 * <ul>
 * <li></li>
 * <li></li>
 * </ul>
 * 
 * @see org.nakedobjects.object.AbstractNakedObject
 */
public interface NakedObject extends Naked {
    /**
     * Called when the object is first created to intialised the object. This
     * will not be called when the object is recreated when retrieved from the
     * object store.
     */
    void created();

    /**
     * Called when the object is delete, after it is removed from the object
     * store.
     */
    void deleted();

    /**
     * Called when a the system destoys an object - permenantly removes it from
     * the object store.
     */
    void destroy() throws ObjectStoreException;

    String getIconName();

    /**
     * The objects unique id. This id allows the object to added to, stored by,
     * and retrieved from the object store.
     */
    Object getOid();

    /**
     * Returns true if the object is used for finding objects. An object cannot
     * be a finder object and persistent.
     */
    boolean isFinder();

    /**
     * returns true if the object is being held by the object store.
     */
    boolean isPersistent();

    /**
     * returns true when the object has been completely read into memory and all
     * it attributes can be accessed.
     */
    boolean isResolved();

    /**
     * When the state of this object changes, e.g., an attribute is set, then
     * this method should be called so that it is persisted and a message is
     * propogated to the users of this object within the system.
     */
    void objectChanged();

    /**
     * Resolves the current object ensuring all its attributes are available in
     * memory.
     */
    void resolve();

    /**
     * Sets the OID for this object. This id must be unique.
     */
    void setOid(Object oid);

    /**
     * sets the object's resolved state to true
     */
    void setResolved();

    /**
     * Returns a String summarising the state of the object.
     */
    Summary summary();

    void makeFinder();
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