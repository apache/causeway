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


import java.io.Serializable;


public abstract class Permission implements Serializable {
    private String reason;
    protected Permission() {}

    protected Permission(String reason) {
        this.reason = reason;
    }

    /**
     Returns an Allow (Allow.DEFAULT) object if true; Veto (Veto.DEFAULT) if false; 
     @deprecated
     @see #allow(boolean)
     */
    public final static Permission create(boolean allow) {
        return allow(allow);
    }

    /**
     Returns an Allow (Allow.DEFAULT) object if true; Veto (Veto.DEFAULT) if false; 
     */
    public final static Permission allow(boolean allow) {
        if (allow) {
            return Allow.DEFAULT;
        } else {
            return Veto.DEFAULT;
        }
    }

    /**
     Returns a new Allow object if <code>allow</code> is true; a new Veto if false.  The respective reason is
     passed to the newly created object.
     */
    public final static Permission create(boolean allow, String reasonAllowed, String reasonVeteod) {
        if (allow) {
            return new Allow(reasonAllowed);
        } else {
            return new Veto(reasonVeteod);
        }
    }

    /**
     Returns the persmission's reason
     */
    public String getReason() {
        return reason == null ? "" : reason;
    }

    /**
     Returns true if this object is giving permission.
     */
    public abstract boolean isAllowed();

    /**
     Returns true if this object is NOT giving permission.
     */
    public abstract boolean isVetoed();

    public String toString() {
        return "Permission [type=" + (isVetoed() ? "VETOED" : "ALLOWED") + ", reason=" + reason + "]";
    }
}
