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
package org.nakedobjects.viewer.lightweight.options;

import org.apache.log4j.Logger;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.object.reflect.Association;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.security.Session;
import org.nakedobjects.viewer.lightweight.InternalView;
import org.nakedobjects.viewer.lightweight.Location;
import org.nakedobjects.viewer.lightweight.ObjectView;
import org.nakedobjects.viewer.lightweight.Workspace;


/**
   Destroy this object
 */
public class RemoveAssociationOption extends AbstractObjectOption {
    private static final Logger LOG = Logger.getLogger(RemoveAssociationOption.class);

    public RemoveAssociationOption() {
        super("Clear association");
    }

    public Permission disabled(Workspace workspace, ObjectView view, Location at) {
        InternalView internalView = (InternalView) view;
        NakedObject parent = internalView.parentObjectView().getObject();
        Field field = internalView.getFieldOf();

        About about;
        
        if (field instanceof OneToManyAssociation) {
            about = ((OneToManyAssociation) field).getAbout(Session.getSession().getSecurityContext(), parent, view.getObject(), false);
        } else {
            about = ((OneToOneAssociation) field).getAbout(Session.getSession().getSecurityContext(), parent, view.getObject());
        }

        Permission edit = about.canUse();

        if (edit.isAllowed()) {
            String status = "Clear the association to this object from '" + parent.title() + "'";

            return new Allow(status);
        } else {
            return new Veto(edit.getReason());
        }
    }

    public void execute(Workspace frame, ObjectView view, Location at) {
        InternalView internalView = (InternalView) view;
        Association association = (Association) internalView.getFieldOf();
        NakedObject parent = internalView.parentObjectView().getObject();

        LOG.debug("Delete " + association + " from " + parent);
        association.clearAssociation(parent, view.getObject());

//        view.dispose();
    }
}
