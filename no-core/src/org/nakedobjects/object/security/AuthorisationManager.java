package org.nakedobjects.object.security;

import org.nakedobjects.object.MemberIdentifier;
import org.nakedobjects.object.NakedObjectsComponent;
import org.nakedobjects.object.Session;


/**
 * Authorises the user in the current session view and use members of an object.
 */
public interface AuthorisationManager extends NakedObjectsComponent {

    /**
     * Returns true when the user respresented by the specified session is authorised to use the member of the
     * class/object represented by the menber identifier.
     * 
     * <p>
     * Normally a user is authorised to access a particular member, whcih means they can see it, hence we rely
     * on the isVisible() method. However, sometimes we want leave thing visible, but just allow them to be
     * used. It is in these situations that this method is used.
     */
    boolean isUsable(Session session, MemberIdentifier identifier);

    /**
     * Returns true when the user respresented by the specified session is authorised to view the member of
     * the class/object represented by the menber identifier. Normally the view of the specified field, or the
     * display of the action will be supressed if this returns false.
     */
    boolean isVisible(Session session, MemberIdentifier identifier);

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2004 Naked Objects Group Ltd
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