package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.control.defaults.Allow;
import org.nakedobjects.object.control.defaults.Veto;
import org.nakedobjects.object.reflect.OneToManyAssociationSpecification;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOption;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.OneToManyElement;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Workspace;

import org.apache.log4j.Logger;


public class RemoveOneToManyAssociationOption extends MenuOption {
    private static final Logger LOG = Logger.getLogger(RemoveOneToManyAssociationOption.class);

    public RemoveOneToManyAssociationOption() {
        super("Clear association");
    }

    public Permission disabled(View view) {
        InternalCollection collection = (InternalCollection) ((ObjectContent) view.getParent().getContent()).getObject();
    	NakedObject parentObject = collection.parent();

		// associated object
    	OneToManyElement content = (OneToManyElement) view.getContent();
		OneToManyAssociationSpecification association = content.getOneToManyAssociation();
        NakedObject associatedObject = content.getObject();
        

        About about = association.getAbout(ClientSession.getSession(), parentObject, associatedObject, false);

        Permission edit = about.canUse();

        if (edit.isAllowed()) {
            String status = "Clear the association to this object from '" + parentObject.titleString() + "'";

            return new Allow(status);
        } else {
            return new Veto(edit.getReason());
        }
    }

    public void execute(Workspace frame, View view, Location at) {
    	NakedObject parentObject = ((ObjectContent) view.getParent().getParent().getContent()).getObject();

		// associated object
    	OneToManyElement content = (OneToManyElement) view.getContent();
		OneToManyAssociationSpecification association = content.getOneToManyAssociation();
        NakedObject associatedObject = content.getObject();
        
        LOG.debug("Remove " + associatedObject + " from " + parentObject);
        association.clearAssociation(parentObject, associatedObject);
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
