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

package org.nakedobjects.object.control;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.security.Role;
import org.nakedobjects.security.SecurityContext;
import org.nakedobjects.security.User;

/**
 An About for contolling the use of fields within a NakedObject.
 */
public class FieldAbout extends AbstractAbout {
	   private final static long serialVersionUID = 1L;

	/**
	 An About for showing that an attribute is can not be changed.
	 */
	public static final About READ_ONLY = UNUSEABLE;

	/**
	 An About for showing that an attribute is can be changed.
	 */
	public static final About READ_WRITE = USEABLE;
		
     public FieldAbout(SecurityContext context, NakedObject object) {
    	super(context, object);
    }
 
    /**
     Returns a read only About (FieldAbout.READ_ONLY) if true; read-only 
     (FieldAbout.READ_WRITE) if false.
     */
    public static About readOnly(boolean isReadOnly) {
        if (isReadOnly) {
            return READ_ONLY;
        } else {
            return READ_WRITE;
        }
    }

    /**
     Returns a read/write About (FieldAbout.READ_WRITE) if true; read-only 
     (FieldAbout.READ_ONLY) if false.
     */
    public static About readWrite(boolean isReadWrite) {
        if (isReadWrite) {
            return READ_WRITE;
        } else {
            return READ_ONLY;
        }
    }
    
    public AbstractAbout visibleOnlyToRole(Role role) {
    	return super.visibleOnlyToRole(role);
    }
    
    public AbstractAbout visibleOnlyToRoles(Role[] roles) {
    	return super.visibleOnlyToRoles(roles);
    }
    
    public AbstractAbout modifiableOnlyByRole(Role role) {
    	return super.usableOnlyByRole(role);
    }
    
    public AbstractAbout modifiableOnlyByRoles(Role[] roles) {
    	return super.usableOnlyByRoles(roles);
    }
    
//	User based methods
    
    public AbstractAbout invisibleToUser(User user) {
    	return super.invisibleToUser(user);
    }
    
    public AbstractAbout invisibleToUsers(User[] users) {
    	return super.invisibleToUsers(users);
    }
    
    public AbstractAbout visibleOnlyToUser(User user) {
    	return super.visibleOnlyToUser(user);
    }
    
    public AbstractAbout visibleOnlyToUsers(User[] users) {
    	return super.visibleOnlyToUsers(users);
    }
    
    public AbstractAbout unmodifiableByUser(User user) {
    	return super.unusableByUser(user);
    }
    
    public AbstractAbout unmodifiableByUsers(User[] users) {
    	return super.unusableByUsers(users);
    }
    
    public AbstractAbout modifiableOnlyByUser(User user) {
    	return super.usableOnlyByUser(user);
    }
    
    public AbstractAbout modifiableOnlyByUsers(User[] users) {
    	return super.usableOnlyByUsers(users);
    }

//	State based methods
//  Field can not be made invisible by state.
    
    public AbstractAbout unmodifiableInState(State state) {
    	return super.unusableInState(state);
    }
    
    public AbstractAbout unmodifiableInStates(State[] states) {
    	return super.unusableInStates(states);
    }
    
    public AbstractAbout modifiableOnlyInState(State state) {
    	return super.usableOnlyInState(state);
    }
    
    public AbstractAbout modifiableOnlyInStates(State[] states) {
    	return super.usableOnlyInStates(states);
    }
    
    //	Business logic methods
    public AbstractAbout unmodifiableOnCondition(boolean conditionMet, 
    		String reasonNotMet) {
    	return super.unusableOnCondition(conditionMet, reasonNotMet);
    }
    
    // Absolute methods
    public AbstractAbout invisible() {
    	return super.invisible();
    } 
    
    public AbstractAbout unmodifiable() {
    	return super.unusable("Cannot be modified");
    }

	public AbstractAbout unmodifiable(String reason) {
		return super.unusable(reason);
	} 
    
    
    
    
    
    
    
    
}
