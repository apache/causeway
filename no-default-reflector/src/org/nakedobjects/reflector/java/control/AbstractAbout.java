package org.nakedobjects.reflector.java.control;

import org.nakedobjects.application.control.Role;
import org.nakedobjects.application.control.State;
import org.nakedobjects.application.control.StatefulObject;
import org.nakedobjects.application.control.User;
import org.nakedobjects.object.control.AbstractConsent;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.object.security.Session;


public abstract class AbstractAbout implements Hint {
    private final static long serialVersionUID = 1L;
   
    private Session session;
    private String description;
    private boolean isAccessible;
    private String name;
    private StatefulObject statefulObject;
    private StringBuffer unusableReason;
    private StringBuffer invalidReason;
    private StringBuffer debug = new StringBuffer();

    public AbstractAbout(Session session, Object object) {
    	this.session = session;
    	isAccessible = true;
    	description = "";
    	if(object instanceof StatefulObject) {
    		statefulObject = (StatefulObject) object;
    	}
    }
    
    public Consent canAccess() {
    	return AbstractConsent.allow(isAccessible);
    }

    public Consent canUse() {
        if (unusableReason == null) {
            return Allow.DEFAULT;
        } else {
            return new Veto(unusableReason.toString());
        }
    }
    
    public Consent isValid() {
        if (invalidReason == null) {
            return Allow.DEFAULT;
        } else {
            return new Veto(invalidReason.toString());
        }
    }

    private boolean currentUserHasRole(Role role) {
    	return ((SimpleSession) session).hasRole(role);
    }

    public String debug() {
		return debug.toString();
	}
    
    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    protected void invisible() {
    	concatDebug("unconditionally invisible");
    	vetoAccess();
    }

    private void concatDebug(String string) {
		debug.append(debug.length() > 0 ? "; " : "");
		debug.append(string);
	}

	protected void invisibleToUser(User user) {
		concatDebug("Invisible to user " + user);
        if (getCurrentUser() == user) {
            vetoAccess();
        }
    }

    protected void invisibleToUsers(User[] users) {
    	concatDebug("Invisible to users ");
    	for (int i = 0; i < users.length; ++i) {
    		debug.append(i > 0 ? ". " : "" + users[i].getName());
   		}
    	    	
    	for (int i = 0; i < users.length; ++i) {
            if(getCurrentUser() == users[i]) {
            	vetoAccess();
            	break;
            }
        }
    }

    public void setDescription(String description) {
    	this.description = description;
    }

    public void setName(String name) {
    	this.name = name;
    }

    private boolean stateIsSameAs(State state) {
    	if(statefulObject == null) {
    		throw new IllegalStateException("Cannot check state of object.  About not instantiated with object reference.");
    	}
    	return statefulObject.getState().equals(state);
    }

    //	Absolute methods
    protected void unusable(String reason) {
    	concatDebug("Unconditionally unusable");
        vetoUse(reason);
    }

    protected void unusableByUser(User user) {
        if (getCurrentUser() == user) {
            vetoUse("Not available to current user");
        }
    }

    private User getCurrentUser() {
        return ((SimpleSession) session).getName();
    }

    protected void unusableByUsers(User[] users) {
        boolean validUser = true;

        for (int i = 0; i < users.length; ++i) {
        	if(users[i] == null) {
        		continue;
        	}
            validUser = validUser && (getCurrentUser() != users[i]);
        }

        if (!validUser) {
            vetoUse("Not available to current user");
        }
    }

    protected void unusableInState(State state) {
    	concatDebug("Unusable in state " + state);
    	if (stateIsSameAs(state)) {
            vetoUse("Unusable when object is in its current State");
        }
    }

    protected void unusableInStates(State[] states) {
        boolean inUsableState = true;
        String listOfValidStates = new String();

        for (int i = 0; i < states.length; ++i) {
        	if(states[i] == null) {
        		continue;
        	}
        	
            inUsableState = inUsableState && !stateIsSameAs(states[i]);
            listOfValidStates = listOfValidStates +
                states[i].toString();

            if (i < states.length) {
                listOfValidStates = listOfValidStates + ", ";
            }
        }

        if (!inUsableState) {
            vetoUse("Unusable when object is in any of these states: " +
                listOfValidStates);
        }
    }

    //	Business logic methods
    protected void unusableOnCondition(boolean conditionMet,
    	String reasonNotMet) {
    	concatDebug("Conditionally unusable " + reasonNotMet);
    	if (conditionMet) {
            vetoUse(reasonNotMet);
        }
    }

    protected void usableOnlyByRole(Role role) {
    	concatDebug("Usable only for role " + role.getName());
    	if (!currentUserHasRole(role)) {
            vetoUse("User does not have the appropriate role");
        }
    }

    protected void usableOnlyByRoles(Role[] roles) {
        boolean validRole = false;

        for (int i = 0; i < roles.length; ++i) {
        	if(roles[i] == null) {
        		continue;
        	}
        	
            validRole = validRole || (currentUserHasRole(roles[i]));
        }

        if (!validRole) {
            vetoUse("User does not have the appropriate role");
        }
    }

    protected void usableOnlyByUser(User user) {
        if (getCurrentUser() != user) {
            vetoUse("Not available to current user");
        }
    }

    protected void usableOnlyByUsers(User[] users) {
        boolean validUser = false;

        for (int i = 0; i < users.length; ++i) {
        	if(users[i] == null) {
        		continue;
        	}
        	
            validUser = validUser || (getCurrentUser() == users[i]);
        }

        if (!validUser) {
            vetoUse("Not available to current user");
        }
    }

    //	State based methods
    protected void usableOnlyInState(State state) {
        if (!stateIsSameAs(state)) {
            vetoUse("Usable only when object is in the state: " +
                state.toString());
        }
    }

    protected void usableOnlyInStates(State[] states) {
       	concatDebug("usable only to certain roles");
       	boolean inUsableState = false;
        StringBuffer listOfValidStates = new StringBuffer();

        for (int i = 0; i < states.length; ++i) {
        	if(states[i] == null) {
        		continue;
        	}
        	
            inUsableState = inUsableState || stateIsSameAs(states[i]);
            listOfValidStates.append(listOfValidStates);
            listOfValidStates.append(i > 0 ? ", ": "");
            listOfValidStates.append(states[i].toString());
        }

        if (!inUsableState) {
            vetoUse("Usable only when object is in any of these states: " +
                listOfValidStates);
        }
    }

    protected void vetoAccess() {
       	concatDebug("Access unconditionally vetoed");
        isAccessible = false;
    }

    protected void vetoUse(String reason) {
    	concatDebug("Use unconditionally vetoed; " + reason);

        if (unusableReason == null) {
        	unusableReason = new StringBuffer();
        } else {
        	unusableReason.append("; ");
        }

        unusableReason.append(reason);
    }

    //	Role based methods
    protected void visibleOnlyToRole(Role role) {
    	visibleOnlyToRoles(new Role[] {role});
    }

    protected void visibleOnlyToRoles(Role[] roles) {
    	concatDebug("Visible only to roles ");
    	
    	boolean validRole = false;

        for (int i = 0; i < roles.length; ++i) {
        	if(roles[i] == null) {
        		continue;
        	}
        	
        	debug.append(i > 0 ? ", " : "" + roles[i].getName());
            validRole = validRole || (currentUserHasRole(roles[i]));
        }

        if (!validRole) {
            vetoAccess();
        }
    }

    //	User based methods
    protected void visibleOnlyToUser(User user) {
        visibleOnlyToUsers(new User[] {user});
    }

    protected void visibleOnlyToUsers(User[] users) {
        for (int i = 0; i < users.length; ++i) {
        	if(users[i] == null) {
        		continue;
        	}
        	
        	if(getCurrentUser() == users[i]) {
        		return;
        	}
        }

        vetoAccess();
    }
    
    protected void invalidOnCondition(boolean conditionMet, String reasonNotMet) {
        	concatDebug("Conditionally invalid; " + reasonNotMet);
        	if (conditionMet) {
                invalid(reasonNotMet);
            }
        }


    protected void invalid(String reason) {
    	concatDebug("unconditionally invalid; " + reason);
        if (invalidReason == null) {
            invalidReason = new StringBuffer();
        } else {
            invalidReason.append("; ");
        }

        invalidReason.append(reason);
    }

    protected void invalid() {
        invalid("unconditionally invalid");
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

