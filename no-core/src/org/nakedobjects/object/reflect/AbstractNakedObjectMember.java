package org.nakedobjects.object.reflect;

import org.nakedobjects.object.NakedObjectMember;


public abstract class AbstractNakedObjectMember implements NakedObjectMember {
    protected final String defaultLabel ;
    private final String id;

    protected AbstractNakedObjectMember(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must always be set");
        }

        this.defaultLabel  = NameConvertor.naturalName(name);
        this.id = NameConvertor.simpleName(name);
    }
    
    public abstract Object getExtension(Class cls);
   

    /**
     * Returns the name of the member.
     */
    public String getId() {
        return id;
    }

    public String toString() {
        return "id=" + getId() + ",label='" + getLabel() + "'";
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