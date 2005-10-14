package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;


/**
   Each option that a user is shown in an objects popup menu a MenuOption.  A MenuOption details:
   the name of an option (in the users language); <ul>the type of object that might result when
   requesting this option</ul>; a way to determine whether a user can select this option on the
   current object.
 */
public abstract class MenuOption implements UserAction {
    private String name;
    private String description;

    /**
       Constructs a default menu option, with no name.
     */
    public MenuOption() {
        this("");
    }

    public MenuOption(String name) {
        this.name = name;
    }

    public abstract void execute(Workspace workspace, View view, Location at);

    public void setName(String name) {
        this.name = name;
    }

    /**
       Returns the stored name of the menu option.
     */
    public String getName(View view) {
        return name;
    }
    
    public String getDescription(View view) {
        return description;
    }

    public Consent disabled(View view) {
        return Allow.DEFAULT;
    }

    public String toString() {
        String name = getClass().getName();

        return name.substring(name.lastIndexOf('.') + 1);
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
