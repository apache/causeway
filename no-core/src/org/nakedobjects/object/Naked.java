package org.nakedobjects.object;

import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.NakedObjectAssociation;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.security.Session;



/**
 * Definition of an naked object. Anything that conforms to this interface can be used in an naked
 * objects system. Objects are further broken down into two types - reference object (NakedObject) and value
 * objects (NakedValue).
 * <p>
 * A naked object must do the following
 * </p>
 * <ul>
 * <li>Specify whether it is empty (isEmpty())</li>
 * <li>Copy the contents of another, same typed, objects</li>
 * <li>Describe itself in a textual String form (using Title)</li>
 * <li>Be able to decribe which class it is based on (getClassName/getFullClassName)</li>
 * </ul>
 * 
 * @see org.nakedobjects.object.NakedObject - reference objects
 */
public interface Naked {

    /**
     * Copies the content of the specified object into this object.
     */
    abstract void copyObject(Naked object);

    /**
     * Returns the NakedClass that represents this object.
     */
    NakedObjectSpecification getSpecification();

    /**
     * Checks to see if two objects contain the same information. Compare with <code>equals</code>,
     * which determines if the one object is replaceable with another.
     * 
     * @param object
     *                   the object to compare
     * @return true if the objects have the same content, and false if the objects
     *              are of different types or their contents are deemed to be different.
     */
    boolean isSameAs(Naked object);

    String titleString();
    
    Object getObject();

    abstract void clear(NakedObjectAssociation specification, NakedObject ref);
    
    boolean canAccess(Session session, NakedObjectField specification);
    
    boolean canAccess(Session session, Action action);

    abstract boolean canUse(Session session, NakedObjectField field);
    

    NakedObject execute(Action action, Naked[] parameters);
    
    Hint getHint(Session session, Action action, Naked[] parameters);
    
    Hint getHint(Session session, NakedObjectField field, NakedObject value);

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General License for more details.
 * 
 * You should have received a copy of the GNU General License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */