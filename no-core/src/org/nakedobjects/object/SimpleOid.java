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

package org.nakedobjects.object;


import java.io.Serializable;


public class SimpleOid implements Serializable {
	private final static long serialVersionUID = 1L;
	
    private final long serialNo;
    public SimpleOid(long serialNo) {
        this.serialNo = serialNo;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof SimpleOid) {
            return ((SimpleOid) obj).serialNo == serialNo;
        }
        return false;
    }

    /**
     * 
     * @return long
     */
    public long getSerialNo() {
        return serialNo;
    }

    public int hashCode() {
        return 37 * 17 + (int) (serialNo ^ (serialNo >>> 32));
    }

    public String toString() {
        return "OID#" + Long.toHexString(serialNo);
    }
}
