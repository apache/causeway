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
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.viewer.lightweight.InternalView;
import org.nakedobjects.viewer.lightweight.Location;
import org.nakedobjects.viewer.lightweight.ObjectView;
import org.nakedobjects.viewer.lightweight.Workspace;


/**
   Destroy this object
 */
public class DestroyObjectOption extends AbstractObjectOption {
	private static final Logger LOG = Logger.getLogger(DestroyObjectOption.class);
	
	public DestroyObjectOption() {
		super("Destroy Object", "Destroy this object - remove it from the object store");
	}

	public void execute(Workspace workspace, ObjectView view, Location at) {
		try {
			NakedObject object = view.getObject();
			
			if(view instanceof InternalView && view.getParent() != null) {
				InternalView internalView = (InternalView) view;
				NakedObject parent = internalView.parentObjectView().getObject();
				if( internalView.getFieldOf() != null) {
					OneToOneAssociation association = (OneToOneAssociation) internalView.getFieldOf();	
					association.clearAssociation(parent, object);
				}
			}
			
			// TODO remove any views in the instance collections
			
			object.destroy();
			workspace.removeViewsFor(object);
		} catch (ObjectStoreException e) {
			LOG.error("Object store problem", e);
		}
	}
}
