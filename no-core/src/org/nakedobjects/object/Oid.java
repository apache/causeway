package org.nakedobjects.object;

public interface Oid {

    /**
     * Returns true if this oid contains a previous oid. This is needed when oids are not static and
     * change when the identified object is changed.
     */
    boolean hasPrevious();

    /**
     * Returns the privious oid if there is one (hasPrevious() returns true). Returns null otherwise
     * (hasPrevious() returns false).
     */
    Oid getPrevious();

    /**
     * Copies the content of the specified oid into this oid. After this call the hashcode return by
     * both the specified object and this object will be the same, and both objects will equal
     * (this.equal(oid) returns true).
     */
    void copyFrom(Oid oid);

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