package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.object.Aggregated;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.control.defaults.Veto;
import org.nakedobjects.object.reflect.OneToManyAssociationSpecification;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.viewer.skylark.ContentDrag;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.OneToManyField;
import org.nakedobjects.viewer.skylark.View;

public class InternalCollectionActions extends CollectionActions {

    public InternalCollectionActions(View wrappedView) {
        super(wrappedView);
    }

    public void dragIn(ContentDrag drag) {
        NakedObject object = ((ObjectContent) drag.getSourceContent()).getObject();
        NakedObject parent = ((ObjectContent) getParent().getContent()).getObject();
        Permission perm = canDrop(parent, object);
        if (perm.isVetoed()) {
            getState().setCantDrop();
            getViewManager().setStatus(perm.getReason());
        } else {
            getState().setCanDrop();
        }
    }

    private Permission canDrop(NakedObject parent, NakedObject object) {
        InternalCollection collection = (InternalCollection) getAssociation().get(parent);
        if(!object.getSpecification().isOfType(collection.getElementSpecification())) {
            return new Veto("Only objects of type " + collection.getElementSpecification().getSingularName() + " are allowed in this collection");
        }
        if(parent.getOid() != null && object.getOid() == null) {
            return new Veto("Can't set field in persistent object with reference to non-persistent object");
        }
        if(object instanceof Aggregated) {
            Aggregated aggregated = ((Aggregated) object);
            if(aggregated.isAggregated() && aggregated.parent() != parent) {
                return new Veto("Object is already associated with another object: " + aggregated.parent());
            }
        }
        About about = getAssociation().getAbout(ClientSession.getSession(), parent, object, true);
        return about.canUse();
    }

    public void dragOut(ContentDrag drag) {
        getState().clearObjectIdentified();
        getViewManager().setStatus("");
        super.dragOut(drag);
    }

    public void drop(ContentDrag drag) {
        NakedObject object = ((ObjectContent) drag.getSourceContent()).getObject();
        NakedObject parent = ((ObjectContent) getParent().getContent()).getObject();
        Permission perm = canDrop(parent, object);
        if (perm.isAllowed()) {
	        getAssociation().setAssociation(parent, object);
	        layout();
        }
    }


    private OneToManyAssociationSpecification getAssociation() {
        OneToManyField content = (OneToManyField) getContent();
        return (OneToManyAssociationSpecification) content.getField();
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