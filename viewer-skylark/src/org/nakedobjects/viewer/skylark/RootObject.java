package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.viewer.skylark.basic.ObjectOption;

public class RootObject implements ObjectContent {
    private final NakedObject object;

    public RootObject(NakedObject object) {
        this.object = object;
    }

    public String debugDetails() {
        String type = getClass().getName();
        type = type.substring(type.lastIndexOf('.') + 1);
        return type + "\n" + "  object: " + object + "\n";
    }

    public NakedObject getObject() {
        return object;
    }

    public String toString() {
        return "" + object;
    }

    public void menuOptions(MenuOptionSet options) {
		ObjectOption.menuOptions(object, options);    
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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