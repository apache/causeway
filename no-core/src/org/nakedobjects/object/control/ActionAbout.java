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
 An About for contolling the action methods within a NakedObject.
 */
public class ActionAbout extends AbstractAbout {
	   private final static long serialVersionUID = 1L;

    /**
     An About for showing that an action is not yet implemented.
     */
    public static final About UNIMPLEMENTED = UNUSEABLE;

    /**
     An About for showing that an action is disabled.
     */
    public static final About DISABLE = UNUSEABLE;

    /**
     An About for showing that an action is enabled.
     */
    public static final About ENABLE = USEABLE;

    
    /**
     * @deprecated
     */
    ActionAbout(String actionName, Permission useable) {
    	super(null, null);
    	this.setName(actionName);
    	if(useable.isVetoed()) {
    		vetoUse(useable.getReason());
    	}
    }

    /**
     * @deprecated
     */
    ActionAbout(String actionName, boolean allow) {
        this(actionName, Permission.allow(allow));
    }

    public ActionAbout(SecurityContext context, NakedObject object) {
    	super(context, object);
    }

    
    /**
     Returns a disable action About (ActionAbout.DISABLE) if true; veto action 
     (ActionAbout.ENABLE) if false.
     */
    public static About disable(boolean disable) {
        if (disable) {
            return DISABLE;
        } else {
            return ENABLE;
        }
    }

    /**
     Returns an enable action About (ActionAbout.ENABLE) if true; veto action 
     (ActionAbout.DISABLE) if false.
     */
    public static About enable(boolean enable) {
        if (enable) {
            return ENABLE;
        } else {
            return DISABLE;
        }
    }
    
    public static About enable(boolean enable, String reason) {
        if (enable) {
            return ENABLE;
        } else {
            return new ActionAbout(null, new Veto(reason));
        }
    }
    
    
    
    
    public AbstractAbout visibleOnlyToRole(Role role) {
    	return super.visibleOnlyToRole(role);
    }
    
    public AbstractAbout visibleOnlyToRoles(Role[] roles) {
    	return super.visibleOnlyToRoles(roles);
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

//	State based methods
    //  Action can not be made invisible by state.
    public AbstractAbout unusable() {
    	return super.unusable("Cannot be invoked");
    } 
    
    public AbstractAbout unusable(String reason) {
    	return super.unusable(reason);
    } 
    
    
    public AbstractAbout unusableInState(State state) {
    	return super.unusableInState(state);
    }
    
    public AbstractAbout unusableInStates(State[] states) {
    	return super.unusableInStates(states);
    }
    
    public AbstractAbout usableOnlyInState(State state) {
    	return super.usableOnlyInState(state);
    }
    
    public AbstractAbout usableOnlyInStates(State[] states) {
    	return super.usableOnlyInStates(states);
    }
    
//	Business logic methods
    public AbstractAbout unusableOnCondition(boolean conditionMet, 
    		String reasonNotMet) {
    	return super.unusableOnCondition(conditionMet, reasonNotMet);
    }

	public void changeNameIfUsable(String name) {
		if(canUse().isAllowed()) {
			setName(name);
		}
		
	}
}
;