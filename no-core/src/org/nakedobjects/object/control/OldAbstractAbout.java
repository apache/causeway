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


public abstract class OldAbstractAbout implements About {
    private String name;
    private String description;
    private Permission isAccessible;
    private Permission isUsable;

    /**
     * 
     */
    OldAbstractAbout() {}

    public OldAbstractAbout(String name, String description, Permission accessible, 
        Permission usable) {
        this.name = name;
        this.description = description;
        isAccessible = accessible;
        isUsable = usable;
    }

    public Permission canAccess() {
        if (isAccessible == null) {
            return Allow.DEFAULT;
        } else {
            return isAccessible;
        }
    }

    public Permission canUse() {
        if (isUsable == null) {
            return Allow.DEFAULT;
        } else {
            return isUsable;
        }
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }
    
    public String debug() {
		return "no details (AbstractAbout)";
	}
}
