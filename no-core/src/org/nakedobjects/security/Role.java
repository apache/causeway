package org.nakedobjects.security;

import org.nakedobjects.object.AbstractNakedObject;
import org.nakedobjects.object.Title;
import org.nakedobjects.object.control.FieldAbout;
import org.nakedobjects.object.value.MultilineTextString;
import org.nakedobjects.object.value.TextString;


public final class Role extends AbstractNakedObject {
	private static final long serialVersionUID = 1L;
	public static final Role SYSADMIN = new Role("sysadmin");
	private final TextString name;
    private final MultilineTextString description;

    public Role() {
    	name = new TextString();
    	description = new MultilineTextString();
    }

    public Role(String name) {
    	this();
    	this.name.setValue(name);
    }

    public void aboutFieldDefault(FieldAbout about ) {
    	about.visibleOnlyToRole(SYSADMIN);
    }
    
	public final TextString getName() {
    	return name;
    }

    public final MultilineTextString getDescription() {
    	return description;
    }

    public Title title() {
        return name.title();
    }
}

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
