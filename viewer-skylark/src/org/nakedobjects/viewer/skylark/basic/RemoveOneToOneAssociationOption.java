package org.nakedobjects.viewer.skylark.basic;

import org.apache.log4j.Logger;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.object.reflect.OneToOneAssociationSpecification;
import org.nakedobjects.security.Session;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOption;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.OneToOneField;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Workspace;


public class RemoveOneToOneAssociationOption extends MenuOption {
    private static final Logger LOG = Logger.getLogger(RemoveOneToOneAssociationOption.class);

    public RemoveOneToOneAssociationOption() {
        super("Clear association");
    }

    public Permission disabled(View view) {
    	NakedObject parentObject = ((ObjectContent) view.getParent().getContent()).getObject();

		// associated object
        OneToOneField content = ((OneToOneField) view.getContent());
		OneToOneAssociationSpecification association = content.getOneToOneAssociation();
        NakedObject associatedObject = content.getObject();
        

        About about = association.getAbout(Session.getSession().getContext(), parentObject, associatedObject);

        Permission edit = about.canUse();

        if (edit.isAllowed()) {
            String status = "Clear the association to this object from '" + parentObject.titleString() + "'";

            return new Allow(status);
        } else {
            return new Veto(edit.getReason());
        }
    }

    public void execute(Workspace frame, View view, Location at) {
    	NakedObject parentObject = ((ObjectContent) view.getParent().getContent()).getObject();

		// associated object
        OneToOneField content = ((OneToOneField) view.getContent());
		OneToOneAssociationSpecification association = content.getOneToOneAssociation();
        NakedObject associatedObject = content.getObject();
        
        LOG.debug("Remove " + associatedObject + " from " + parentObject);
        association.clearAssociation(parentObject, associatedObject);
        
        view.getParent().invalidateContent();
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