package org.nakedobjects.application.control;


import org.nakedobjects.application.Title;
import org.nakedobjects.application.TitledObject;
import org.nakedobjects.application.valueholder.TextString;

import java.util.Vector;


public final class User implements TitledObject {
	private static final long serialVersionUID = 1L;
	private final TextString name;
    private final Vector roles;
    private Object rootObject;
    
    public User() {
        name = new TextString();
        roles = new Vector();
    }

    public User(String name) {
    	this();
    	this.name.setValue(name);
    }
    
    public static String fieldOrder() {
    	return "name";
    }
    
    public void aboutFieldDefault(FieldAbout about) {
    	about.visibleOnlyToRole(Role.SYSADMIN);
    }
    
    public final TextString getName() {
    	return name;
    }

    public final Vector getRoles() {
        return roles;
    }
    
    public final void addToRoles(Role role) {
        roles.addElement(role);
		objectChanged();
    }    
    
    public final void removeFromRoles(Role role) {
        roles.addElement(role);
		objectChanged();
    }

    private void objectChanged() {}

    public Object getRootObject() {
//    	resolve(rootObject);
		return rootObject;
	}
	
    public void setRootObject(Object rootObject) {
		this.rootObject = rootObject;
		objectChanged();
	}

    public Title title() {
        return name.title();
    }
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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
