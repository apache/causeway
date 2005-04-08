package org.nakedobjects.object;

public class Persistable {
    /**
     * Marks a class as immutable - once such an object is persisted it cannot be changed.
     */
    public static final Persistable IMMUTABLE = new Persistable("Immutable");
    /**
     * Marks a class as being persisable, but only by under application program control.
     */
    public static final Persistable PROGRAM_PERSISTABLE = new Persistable("Program Persistable");
    /**
     * Marks a class as transient - such an object cannot be persisted.
     */
    public static final Persistable TRANSIENT = new Persistable("Transient");
    /**
     * Marks a class as being persisable by the user (or under application program control).
     */
    public static final Persistable USER_PERSISTABLE = new Persistable("User Persistable");

    private String name;

    private Persistable(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
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