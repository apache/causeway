package org.nakedobjects.object.persistence;

import org.nakedobjects.object.Oid;


public class FastSerialOid implements Oid {
    private final String asString;
    private final int hashCode;
    private final long serialNo;

    /**
     * For performance, we cache asString and hashCode.
     */
    public FastSerialOid(long serialNo) {
        this.serialNo = serialNo;
        this.asString = "OID#" + Long.toHexString(serialNo).toUpperCase();
        this.hashCode = 37 * 17 + (int) (serialNo ^ (serialNo >>> 32));
    }

    /**
     * Overloaded to allow compiler to link directly if we know the compile-time
     * type. (possible performance improvement - called 166,000 times in normal
     * ref data fixture.
     */
    public boolean equals(FastSerialOid otherOid) {
        if (otherOid == this) {
            return true;
        }
        return otherOid.serialNo == serialNo;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof FastSerialOid) {
            // we don't delegate to equals(PojoAdapter) because we
            // don't want to do the identity test again.
            return ((FastSerialOid) obj).serialNo == serialNo;
        }
        return false;
    }

    public long getSerialNo() {
        return serialNo;
    }

    public int hashCode() {
        return hashCode;
    }

    public String toString() {
        return asString;
    }

    public boolean hasPrevious() {
        return false;
    }

    public Oid getPrevious() {
        return null;
    }

    public void copyFrom(Oid oid) {}
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