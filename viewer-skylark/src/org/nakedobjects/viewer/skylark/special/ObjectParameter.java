package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.viewer.skylark.ActionField;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOption;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Workspace;


public class ObjectParameter extends ActionField implements ObjectContent {
    private NakedObjectSpecification parameterClass;
    private NakedObject object;
    
    public ObjectParameter(String label, Naked naked, NakedObjectSpecification parameterClass) {
        super(label);
        this.parameterClass = parameterClass;
        object = (NakedObject) naked;
    }
    
    public NakedObjectSpecification getNakedClass() {
        return parameterClass;
    }

    public void menuOptions(MenuOptionSet options) {
        if(object != null) {
            options.add(MenuOptionSet.VIEW, new MenuOption("Clear field") {

                public void execute(Workspace workspace, View view, Location at) {
                    object = null;
                    view.getParent().invalidateContent();
                }});
        }
    }

    public NakedObject getObject() {
        return object;
    }
    
    public Naked getNaked() {
        return object;
    }

    public void setObject(NakedObject object) {
        this.object = object;
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