package org.nakedobjects.object.control;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.security.Role;
import org.nakedobjects.object.security.User;


public abstract class AbstractAbout implements About {
    private final static long serialVersionUID = 1L;
   
    private NakedObjectContext context;
    private String description;
    private boolean isAccessible;
    private String name;
    private StatefulObject statefulObject;
    private StringBuffer unusableReason;
    private StringBuffer debug = new StringBuffer();
    
    /**
     An About for showing that an attribute is can not be changed.
     */
    protected static final About UNUSEABLE = new About(){
    	public Permission canAccess() {
    		return Allow.DEFAULT;
    	}

    	public Permission canUse() {
    		return Veto.DEFAULT;
    	}

    	public String getDescription() {
    		return "";
    	}

    	public String getName() {
    		return null;
    	}
    	
    	public String debug() {
			return "";
		}
    };

    /**
     An About for showing that an attribute is can be changed.
     */
    protected static final About USEABLE = new About(){
    	public Permission canAccess() {
    		return Allow.DEFAULT;
    	}

    	public Permission canUse() {
    		return Allow.DEFAULT;
    	}

    	public String getDescription() {
    		return "";
    	}

    	public String getName() {
    		return null;
    	}
    	
    	public String debug() {
    		return "";
    	}
    };

    public AbstractAbout(NakedObjectContext context, NakedObject object) {
    	this.context = context;
    	isAccessible = true;
    	description = "";
    	if(object instanceof StatefulObject) {
    		statefulObject = (StatefulObject) object;
    	}
    }
    
    public Permission canAccess() {
    	return Permission.allow(isAccessible);
    }

    public Permission canUse() {
        if (unusableReason == null) {
            return Allow.DEFAULT;
        } else {
            return new Veto(unusableReason.toString());
        }
    }

    private boolean currentUserHasRole(Role role) {
    	return context.hasRole(role);
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

    protected AbstractAbout invisible() {
    	concatDebug("unconditionally invisible");
    	vetoAccess();

        return this;
    }

    private void concatDebug(String string) {
		debug.append(debug.length() > 0 ? "; " : "");
		debug.append(string);
	}

	protected AbstractAbout invisibleToUser(User user) {
		concatDebug("Invisible to user " + user);
        if (context.getUser() == user) {
            vetoAccess();
        }

        return this;
    }

    protected AbstractAbout invisibleToUsers(User[] users) {
    	concatDebug("Invisible to users ");
    	for (int i = 0; i < users.length; ++i) {
    		debug.append(i > 0 ? ". " : "" + users[i].getName());
   		}
    	    	
    	for (int i = 0; i < users.length; ++i) {
            if(context.getUser() == users[i]) {
            	vetoAccess();
            	break;
            }
        }

        return this;
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
    	return statefulObject.getState().isSameAs(state);
    }

    //	Absolute methods
    protected AbstractAbout unusable(String reason) {
    	concatDebug("Unconditionally unusable");
        vetoUse(reason);

        return this;
    }

    protected AbstractAbout unusableByUser(User user) {
        if (context.getUser() == user) {
            vetoUse("Not available to current user");
        }

        return this;
    }

    protected AbstractAbout unusableByUsers(User[] users) {
        boolean validUser = true;

        for (int i = 0; i < users.length; ++i) {
        	if(users[i] == null) {
        		continue;
        	}
            validUser = validUser && (context.getUser() != users[i]);
        }

        if (!validUser) {
            vetoUse("Not available to current user");
        }

        return this;
    }

    protected AbstractAbout unusableInState(State state) {
    	concatDebug("Unusable in state " + state);
    	if (stateIsSameAs(state)) {
            vetoUse("Unusable when object is in its current State");
        }

        return this;
    }

    protected AbstractAbout unusableInStates(State[] states) {
        boolean inUsableState = true;
        String listOfValidStates = new String();

        for (int i = 0; i < states.length; ++i) {
        	if(states[i] == null) {
        		continue;
        	}
        	
            inUsableState = inUsableState && !stateIsSameAs(states[i]);
            listOfValidStates = listOfValidStates +
                states[i].titleString();

            if (i < states.length) {
                listOfValidStates = listOfValidStates + ", ";
            }
        }

        if (!inUsableState) {
            vetoUse("Unusable when object is in any of these states: " +
                listOfValidStates);
        }

        return this;
    }

    //	Business logic methods
    protected AbstractAbout unusableOnCondition(boolean conditionMet,
    	String reasonNotMet) {
    	concatDebug("Conditionally unusable " + reasonNotMet);
    	if (conditionMet) {
            vetoUse(reasonNotMet);
        }

        return this;
    }

    protected AbstractAbout usableOnlyByRole(Role role) {
    	concatDebug("Usable only for role " + role.getName());
    	if (!currentUserHasRole(role)) {
            vetoUse("User does not have the appropriate role");
        }

        return this;
    }

    protected AbstractAbout usableOnlyByRoles(Role[] roles) {
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

        return this;
    }

    protected AbstractAbout usableOnlyByUser(User user) {
        if (context.getUser() != user) {
            vetoUse("Not available to current user");
        }

        return this;
    }

    protected AbstractAbout usableOnlyByUsers(User[] users) {
        boolean validUser = false;

        for (int i = 0; i < users.length; ++i) {
        	if(users[i] == null) {
        		continue;
        	}
        	
            validUser = validUser || (context.getUser() == users[i]);
        }

        if (!validUser) {
            vetoUse("Not available to current user");
        }

        return this;
    }

    //	State based methods
    protected AbstractAbout usableOnlyInState(State state) {
        if (!stateIsSameAs(state)) {
            vetoUse("Usable only when object is in the state: " +
                state.titleString());
        }

        return this;
    }

    protected AbstractAbout usableOnlyInStates(State[] states) {
        boolean inUsableState = false;
        StringBuffer listOfValidStates = new StringBuffer();

        for (int i = 0; i < states.length; ++i) {
        	if(states[i] == null) {
        		continue;
        	}
        	
            inUsableState = inUsableState || stateIsSameAs(states[i]);
            listOfValidStates.append(listOfValidStates);
            listOfValidStates.append(i > 0 ? ", ": "");
            listOfValidStates.append(states[i].titleString());
        }

        if (!inUsableState) {
            vetoUse("Usable only when object is in any of these states: " +
                listOfValidStates);
        }

        return this;
    }

    protected void vetoAccess() {
        isAccessible = false;
    }

    protected void vetoUse(String reason) {
        if (unusableReason == null) {
        	unusableReason = new StringBuffer();
        } else {
        	unusableReason.append("; ");
        }

        unusableReason.append(reason);
    }

    //	Role based methods
    protected AbstractAbout visibleOnlyToRole(Role role) {
    	return visibleOnlyToRoles(new Role[] {role});
    }

    protected AbstractAbout visibleOnlyToRoles(Role[] roles) {
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

        return this;
    }

    //	User based methods
    protected AbstractAbout visibleOnlyToUser(User user) {
        return visibleOnlyToUsers(new User[] {user});
    }

    protected AbstractAbout visibleOnlyToUsers(User[] users) {
        for (int i = 0; i < users.length; ++i) {
        	if(users[i] == null) {
        		continue;
        	}
        	
        	if(context.isCurrentUser(users[i])) {
        		return this;
        	}
        }

        vetoAccess();
        return this;
    }
}


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

