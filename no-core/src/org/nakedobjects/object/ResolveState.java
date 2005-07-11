package org.nakedobjects.object;

import org.nakedobjects.utility.ToString;

public final class ResolveState {
    public static ResolveState PART_RESOLVED = new ResolveState("Part Resolved", "r", 0);
    public static ResolveState RESOLVED = new ResolveState("Resolved", "R", 0);
    public static ResolveState RESOLVING = new ResolveState("Resolving", "~R", 0);
    public static ResolveState UPDATING = new ResolveState("Updating", "U", 0);
    public static ResolveState RESOLVING_PART = new ResolveState("Resolving", "~r", 0);
    public static ResolveState TRANSIENT = new ResolveState("Transient", "T", 0);
    public static ResolveState NEW = new ResolveState("New", "-", 0);
    public static ResolveState GHOST = new ResolveState("Ghost", "G", 0);
    
    private final String code;
    private final String name;
    private final int stage;

    private ResolveState(String name, String code, int stage) {
        this.name = name;
        this.code = code;
        this.stage = stage;
    }

    public String code() {
        return code;
    }

    public String name() {
        return name;
    }
    
    /**
     * Determines if the resolved state can be changed from this state to the specified state.  Returns true
     * if the change is valid.
     */
    public boolean isValidToChangeTo(ResolveState nextState) {
        if(this == NEW) {
            return nextState == TRANSIENT || nextState == GHOST;
        } else if(this == TRANSIENT) {
            return nextState == RESOLVED;
        } else if(this == GHOST) {
            return nextState == RESOLVING_PART || nextState == RESOLVING || nextState == UPDATING;
        } else if(this == RESOLVING_PART) {
            return nextState == PART_RESOLVED || nextState == RESOLVED;
        } else if(this == RESOLVING) {
            return nextState == RESOLVED;
        } else if(this == PART_RESOLVED) {
            return nextState == RESOLVING;
        } else if(this == RESOLVED) {
            return nextState == UPDATING;
        } else if(this == UPDATING) {
            return nextState == RESOLVED;
        }
        
        return false;
    }
    
    /**
     * Returns true if the state reflects some form of non-resolved state (GHOST, PART_RESOLVED) or is resolved and
     * it needs to be updated. Hence it can 
     * be changed to loading (RESOLVING_PART, RESOLVING or UPDATING).
     */
    public boolean isLoadable(ResolveState newState) {
        if(this == GHOST || this == PART_RESOLVED || this == RESOLVED) {
            return isValidToChangeTo(newState);
        }
        return false;
    }
    
   /**
    * Return true if the state reflects some kind of loading (RESOLVING_PART, RESOLVING or 
    * UPDATING), and hence can be changed to loaded (PART_RESOLVED OR RESOLVED). 
    */
    public boolean isLoading() {
        return this == RESOLVING || this == RESOLVING_PART || this == UPDATING;
    }
   
    
    public String toString() {
        ToString str = new ToString(this);
        str.append("name", name);
        str.append("code", code);
        str.append("stage", stage);
        return str.toString();
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
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