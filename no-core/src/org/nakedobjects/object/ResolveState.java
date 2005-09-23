package org.nakedobjects.object;

import org.nakedobjects.utility.Assert;
import org.nakedobjects.utility.ToString;

import java.util.Hashtable;


public final class ResolveState {
    private static final Hashtable states = new Hashtable();
    public static final ResolveState GHOST = new ResolveState("Ghost", "PG");
    public static final ResolveState NEW = new ResolveState("New", "-");
    public static final ResolveState PART_RESOLVED = new ResolveState("Part Resolved", "Pr");
    public static final ResolveState RESOLVED = new ResolveState("Resolved", "PR");
    public static final ResolveState RESOLVING = new ResolveState("Resolving", "P~R", RESOLVED);
    public static final ResolveState RESOLVING_PART = new ResolveState("Resolving Part", "P~r", PART_RESOLVED);
    public static final ResolveState SERIALIZING_GHOST = new ResolveState("Serializing Resolved", "SG", GHOST);
    public static final ResolveState SERIALIZING_PART_RESOLVED = new ResolveState("Serializing Part Resolved", "Sr",
            PART_RESOLVED);
    public static final ResolveState SERIALIZING_RESOLVED = new ResolveState("Serializing Resolved", "SR", RESOLVED);
    public static final ResolveState TRANSIENT = new ResolveState("Transient", "T");
    public static final ResolveState SERIALIZING_TRANSIENT = new ResolveState("Serializing Transient", "ST", TRANSIENT);
    public static final ResolveState UPDATING = new ResolveState("Updating", "PU", RESOLVED);
    private final String code;
    private final ResolveState endState;
    private final String name;
    private static boolean updatesContainCompleteState = true;

    public static ResolveState getResolveState(String name) {
        return (ResolveState) states.get(name);
    }

    private ResolveState(String name, String code) {
        this(name, code, null);
    }

    private ResolveState(String name, String code, ResolveState endState) {
        this.name = name;
        this.code = code;
        this.endState = endState;
        states.put(name, this);
    }

    public String code() {
        return code;
    }

    public ResolveState getEndState() {
        return endState;
    }

    public boolean isGhost() {
        return this == GHOST;
    }

    /**
     * Returns true while object is having its field set up.
     */
    public boolean isIgnoreChanges() {
        return this == ResolveState.TRANSIENT || this == ResolveState.RESOLVING || this == ResolveState.RESOLVING_PART
                || this == ResolveState.UPDATING;
    }

    public boolean isPartlyResolved() {
        return this == ResolveState.PART_RESOLVED;
    }

    public boolean isPersistent() {
        return this == GHOST || this == PART_RESOLVED || this == RESOLVED || this == RESOLVING || this == RESOLVING_PART
                || this == UPDATING || this == SERIALIZING_PART_RESOLVED || this == SERIALIZING_RESOLVED|| this == SERIALIZING_GHOST;
    }

    /**
     * Returns true if the state reflects some form of non-resolved state (GHOST, PART_RESOLVED) or
     * is resolved and it needs to be updated. The spcified new state, which you intend to resolve
     * at should be one of: RESOLVING_PART; RESOLVING; or UPDATING).
     */
    public boolean isResolvable(ResolveState newState) {
        Assert.assertNotNull("new state must be specified", newState);
        if (this == GHOST || this == PART_RESOLVED || this == RESOLVED) {
            return isValidToChangeTo(newState);
        }
        return false;
    }

    public boolean isResolved() {
        return this == ResolveState.RESOLVED;
    }

    /**
     * Return true if the state reflects some kind of loading (RESOLVING_PART, RESOLVING or
     * UPDATING), and hence can be changed to loaded (PART_RESOLVED OR RESOLVED).
     */
    public boolean isResolving() {
        return this == RESOLVING || this == RESOLVING_PART || this == UPDATING;
    }

    public boolean isSerializing() {
        return this == SERIALIZING_GHOST || this == SERIALIZING_PART_RESOLVED || this == SERIALIZING_RESOLVED
                || this == SERIALIZING_TRANSIENT;
    }

    /**
     * Returns true when an object has not yet been made persistent.
     */
    public boolean isTransient() {
        return this == ResolveState.TRANSIENT;
    }

    /**
     * Determines if the resolved state can be changed from this state to the specified state.
     * Returns true if the change is valid.
     */
    public boolean isValidToChangeTo(ResolveState nextState) {
        Assert.assertNotNull("new state must be specified", nextState);
        if (this == NEW) {
            return nextState == TRANSIENT || nextState == GHOST;
        } else if (this == TRANSIENT) {
            return nextState == RESOLVED || nextState == SERIALIZING_TRANSIENT;
        } else if (this == GHOST) {
            return nextState == RESOLVING_PART || nextState == RESOLVING || nextState == UPDATING
                    || nextState == SERIALIZING_GHOST;
        } else if (this == RESOLVING_PART) {
            return nextState == PART_RESOLVED || nextState == RESOLVED;
        } else if (this == RESOLVING) {
            return nextState == RESOLVED;
        } else if (this == PART_RESOLVED) {
            return nextState == RESOLVING_PART || nextState == RESOLVING || nextState == SERIALIZING_PART_RESOLVED
                    || (nextState == UPDATING && updatesContainCompleteState);
        } else if (this == RESOLVED) {
            return nextState == UPDATING || nextState == SERIALIZING_RESOLVED;
        } else if (this == UPDATING) {
            return nextState == RESOLVED;
        } else if (this == SERIALIZING_TRANSIENT) {
            return nextState == TRANSIENT;
        } else if (this == SERIALIZING_GHOST) {
            return nextState == GHOST;
        } else if (this == SERIALIZING_PART_RESOLVED) {
            return nextState == PART_RESOLVED;
        } else if (this == SERIALIZING_RESOLVED) {
            return nextState == RESOLVED;
        }

        return false;
    }

    public String name() {
        return name;
    }

    public ResolveState serializeFrom() {
        if (this == RESOLVED) {
            return SERIALIZING_RESOLVED;
        } else if (this == PART_RESOLVED) {
            return SERIALIZING_PART_RESOLVED;
        } else if (this == GHOST) {
            return SERIALIZING_GHOST;
        } else if (this == TRANSIENT) {
            return SERIALIZING_TRANSIENT;
        } else {
            return null;
        }
    }

    public String toString() {
        ToString str = new ToString(this);
        str.append("name", name);
        str.append("code", code);
        if (endState != null) {
            str.append("endstate", endState.name());
        }
        return str.toString();
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */