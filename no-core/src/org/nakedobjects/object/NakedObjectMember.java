package org.nakedobjects.object;

import org.nakedobjects.object.control.Consent;


public interface NakedObjectMember {

    /**
     * Returns a description of how the member is used - this is the essentially the help text.
     */
    String getDescription();

    Object getExtension(Class cls);

    /**
     * Return the default label for this member. This is based on the name of this member.
     * 
     * @see #getName()
     */
    String getLabel();

    /**
     * Returns the name of the member.
     */
    String getName();

    /**
     * Determines if the user has acces to this member, and hence whether it is visible
     * 
     * @see #isVisible(NakedObject)
     */
    boolean isAccessible();

    /**
     * Determines if this member is visible according to the current state of the object.
     * 
     * @see #isAccessible()
     */
    Consent isVisible(NakedObject target);
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