package org.nakedobjects.object;

import org.nakedobjects.object.control.Consent;


public interface NakedObjectMember {

    /**
     * Returns a description of how the member is used - this is the essentially the help text.
     */
    String getDescription();

    Object getExtension(Class cls);

    Class[] getExtensions();
    
    /**
     * Returns the identifier of the member, which must not change. This should be all lowercase with no
     * spaces: so if the member is called 'Return Date' then the a suitable id would be 'returndate'.
     */
    String getId();

    /**
     * Return the name for this member - the field or action. This is based on the name of this member.
     * 
     * @see #getId()
     */
    String getName();

    /**
     * Determines if the user is authorised to access this member, and hence whether it is visible.
     * 
     * @see #isVisible(NakedObject)
     * @see NakedObjectField#isHidden()
     */
    boolean isAuthorised();

    /**
     * Detemines whether this method is usable on, or the field is editable witin, the specified object.
     */
    Consent isAvailable(NakedObject target);

    /**
     * Determines if this member is visible according to the current state of the object.
     * 
     * @see #isAuthorised()
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