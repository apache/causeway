package org.nakedobjects.object.control.defaults;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.FieldAbout;
import org.nakedobjects.object.control.State;
import org.nakedobjects.object.security.Role;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.object.security.User;


/**
 * An About for contolling the use of fields within a NakedObject.
 */
public class SimpleFieldAbout extends AbstractAbout implements FieldAbout {

    /**
     * An About for showing that an attribute is can not be changed.
     */
    public static final About READ_ONLY = UNUSEABLE;

    /**
     * An About for showing that an attribute is can be changed.
     */
    public static final About READ_WRITE = USEABLE;
    private final static long serialVersionUID = 1L;

    /**
     * Returns a read only About (FieldAbout.READ_ONLY) if true; read-only
     * (FieldAbout.READ_WRITE) if false.
     */
    public static About readOnly(boolean isReadOnly) {
        if (isReadOnly) {
            return READ_ONLY;
        } else {
            return READ_WRITE;
        }
    }

    /**
     * Returns a read/write About (FieldAbout.READ_WRITE) if true; read-only
     * (FieldAbout.READ_ONLY) if false.
     */
    public static About readWrite(boolean isReadWrite) {
        if (isReadWrite) {
            return READ_WRITE;
        } else {
            return READ_ONLY;
        }
    }

    public SimpleFieldAbout(Session session, NakedObject object) {
        super(session, object);
    }

    public void invisible() {
        super.invisible();
    }

    public void invisibleToUser(User user) {
        super.invisibleToUser(user);
    }

    public void invisibleToUsers(User[] users) {
        super.invisibleToUsers(users);
    }

    public void modifiableOnlyByRole(Role role) {
        super.usableOnlyByRole(role);
    }

    public void modifiableOnlyByRoles(Role[] roles) {
        super.usableOnlyByRoles(roles);
    }

    public void modifiableOnlyByUser(User user) {
        super.usableOnlyByUser(user);
    }

    public void modifiableOnlyByUsers(User[] users) {
        super.usableOnlyByUsers(users);
    }

    public void modifiableOnlyInState(State state) {
        super.usableOnlyInState(state);
    }

    public void modifiableOnlyInStates(State[] states) {
        super.usableOnlyInStates(states);
    }

    public void unmodifiable() {
        super.unusable("Cannot be modified");
    }

    public void unmodifiable(String reason) {
        super.unusable(reason);
    }

    public void unmodifiableByUser(User user) {
        super.unusableByUser(user);
    }

    public void unmodifiableByUsers(User[] users) {
        super.unusableByUsers(users);
    }

    public void unmodifiableInState(State state) {
        super.unusableInState(state);
    }

    public void unmodifiableInStates(State[] states) {
        super.unusableInStates(states);
    }

    public void unmodifiableOnCondition(boolean conditionMet, String reasonNotMet) {
        super.unusableOnCondition(conditionMet, reasonNotMet);
    }

    public void visibleOnlyToRole(Role role) {
        super.visibleOnlyToRole(role);
    }

    public void visibleOnlyToRoles(Role[] roles) {
        super.visibleOnlyToRoles(roles);
    }

    public void visibleOnlyToUser(User user) {
        super.visibleOnlyToUser(user);
    }

    public void visibleOnlyToUsers(User[] users) {
        super.visibleOnlyToUsers(users);
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

