package org.nakedobjects.object;

import org.nakedobjects.object.control.Consent;



public interface NakedObjectField extends NakedObjectMember {

    /**
     * Return the specification of the object (or objects) that this field holds. For a value are one-to-one
     * reference this will be type that the accessor returns. For a collection it will be the type of element,
     * not the type of collection.
     */
    NakedObjectSpecification getSpecification();

    /**
     * Returns true if this field is for a collection
     */
    boolean isCollection();

    /**
     * Returns true if this field is derived - is calculated from other data in the object - and should
     * therefore not be editable nor persisted.
     */
    boolean isDerived();

    boolean isEmpty(NakedObject adapter);

    /**
     * Returns true if this field is for an object, not a collection.
     */
    boolean isObject();

    /**
     * Returns true if this field is for a value
     */
    boolean isValue();

    Class[] getExtensions();


    Naked get(NakedObject fromObject);

    
    
    /**
     * Determines if this field must be complete before the object is in a valid state
     */
    boolean isMandatory();

    /**
     * Determines if this field is editable.
     */
    Consent isEditable();
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