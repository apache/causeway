package org.nakedobjects.object;

import org.nakedobjects.utility.ToString;

public final class ResolvedState {
    public static ResolvedState PART_RESOLVED = new ResolvedState("Part Resolved", "r", 0);
    public static ResolvedState RESOLVED = new ResolvedState("Resolved", "R", 0);
    public static ResolvedState RESOLVING = new ResolvedState("Resolving", "~R", 0);
    public static ResolvedState RESOLVING_PART = new ResolvedState("Resolving", "~r", 0);
    public static ResolvedState TRANSIENT = new ResolvedState("Transient", "T", 0);
    public static ResolvedState NEW = new ResolvedState("New", "-", 0);
    public static ResolvedState FAKE = new ResolvedState("Fake", "F", 0);
    public static ResolvedState GHOST = new ResolvedState("Ghost", "G", 0);
    
    private final String code;
    private final String name;
    private final int stage;

    private ResolvedState(String name, String code, int stage) {
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
    
    public boolean isValidToChangeTo(ResolvedState nextState) {
        if(this == NEW) {
            return nextState == TRANSIENT || nextState == GHOST;
        } else if(this == TRANSIENT) {
            return nextState == RESOLVED;
        } else if(this == GHOST) {
            return nextState == RESOLVING_PART || nextState == RESOLVING;
        } else if(this == RESOLVING_PART) {
            return nextState == PART_RESOLVED || nextState == RESOLVED;
        } else if(this == RESOLVING) {
            return nextState == RESOLVED;
        } else if(this == PART_RESOLVED) {
            return nextState == RESOLVING;
        }
        
        return false;
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