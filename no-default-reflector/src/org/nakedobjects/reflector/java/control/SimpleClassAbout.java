package org.nakedobjects.reflector.java.control;

import org.nakedobjects.application.control.ClassAbout;
import org.nakedobjects.application.control.Role;
import org.nakedobjects.application.control.User;
import org.nakedobjects.object.security.Session;


/**
 An About for contolling the use of fields within a NakedObject.
 */
public class SimpleClassAbout extends AbstractAbout implements ClassAbout {

    public SimpleClassAbout(Session session,  Object object) {
        super(session, object);
    }

    public void uninstantiable() {
        super.unusable("");
    }

    public void uninstantiable(String reason) {
        super.unusable(reason);
    }

    public void instantiableOnlyByRole(Role role) {
        super.usableOnlyByRole(role);
    }

    public void instantiableOnlyByRoles(Role[] roles) {
        super.usableOnlyByRoles(roles);
    }

    public void instantiableOnlyByUser(User user) {
        super.usableOnlyByUser(user);
    }

    public void instantiableOnlyByUsers(User[] users) {
        super.usableOnlyByUsers(users);
    }

    public void instancesUnavailable() {
        super.invisible();
    }
    
    public void instancesAvailableOnlyToRole(Role role) {
        super.visibleOnlyToRole(role);
    }

    public void instancesAvailableOnlyToRoles(Role[] roles) {
        super.visibleOnlyToRoles(roles);
    }

    public void instancesAvailableOnlyToUser(User user) {
        super.visibleOnlyToUser(user);
    }

    public void instancesAvailableOnlyToUsers(User[] users) {
        super.visibleOnlyToUsers(users);
    }

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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
