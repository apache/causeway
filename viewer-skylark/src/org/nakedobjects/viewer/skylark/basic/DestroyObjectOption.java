package org.nakedobjects.viewer.skylark.basic;


import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOption;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Workspace;


/**
   Destroy this object
 */
public class DestroyObjectOption extends MenuOption {
	public DestroyObjectOption() {
		super("Destroy Object");
	}

	public void execute(Workspace workspace, View view, Location at) {
		NakedObject object = ((ObjectContent) view.getContent()).getObject();
        
        // TODO fix
/*			if(view.getParent() != null && view.getParent().getContent() instanceof ObjectContent) {
        	NakedObject parent = ((ObjectContent) view.getParent().getContent()).getObject();
        	
        	if( view.getFieldOf() != null) {
        		OneToOneAssociation association = (OneToOneAssociation) view.getFieldOf();	
        		association.clearAssociation(parent, object);
        	}
        }
*/		
        // TODO remove any views in the instance collections
        
        NakedObjectManager manager = object.getContext().getObjectManager();
        manager.destroyObject(object);
        workspace.removeViewsFor(object);
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