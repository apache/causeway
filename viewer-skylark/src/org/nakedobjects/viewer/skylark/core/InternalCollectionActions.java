package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.security.Session;
import org.nakedobjects.viewer.skylark.ContentDrag;
import org.nakedobjects.viewer.skylark.InternalCollectionContent;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.View;

public class InternalCollectionActions extends CollectionActions {

    public InternalCollectionActions(View wrappedView) {
        super(wrappedView);
    }

    public void dragIn(ContentDrag drag) {
        ObjectContent content = (ObjectContent) drag.getSourceContent();
        
        NakedObject parent = ((ObjectContent) getParent().getContent()).getObject();
        
        About about = getAssociation().getAbout(
                Session.getSession().getSecurityContext(), parent, content.getObject(), true);

        Permission perm = about.canUse();
        
        if (perm.isVetoed()) {
            getState().setCantDrop();
            getViewManager().setStatus(perm.getReason());
        } else {
            getState().setCanDrop();
        }
    }

    public void dragOut(ContentDrag drag) {
        getState().clearObjectIdentified();
        getViewManager().setStatus("");
        super.dragOut(drag);
    }

    public void drop(ContentDrag drag) {
        ObjectContent content = (ObjectContent) drag.getSourceContent();
      
        NakedObject parent = ((ObjectContent) getParent().getContent()).getObject();
        
        getAssociation().setAssociation(parent, content.getObject());

        layout();
    }


    private OneToManyAssociation getAssociation() {
        InternalCollectionContent content = (InternalCollectionContent) getContent();

        return (OneToManyAssociation) content.getField();
    }
    
    public String toString() {
		return wrappedView.toString() + "/InternalCollectionActions";
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