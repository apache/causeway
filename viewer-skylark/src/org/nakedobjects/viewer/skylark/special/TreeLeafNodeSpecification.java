package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.reflect.NakedObjectAssociation;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.viewer.skylark.CollectionContent;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.basic.ObjectBorder;
import org.nakedobjects.viewer.skylark.core.ObjectView;

class TreeLeafNodeSpecification implements ViewSpecification, TreeNodeSpecification {
	private ViewSpecification replacementNodeSpecification;

	public boolean canDisplay(Content content) {
	    return content.isObject() && TreeDisplayRules.canDisplay(content.getNaked());
	}

	public boolean canOpen(Content content) {
	    if(content instanceof CollectionContent) {
	        NakedCollection collection = ((CollectionContent) content).getCollection();
	        return  collection.size() > 0;
	    } else if(content instanceof ObjectContent) {
	        NakedObject object = ((ObjectContent) content).getObject();
	        NakedObjectField[] fields = object.getSpecification().getVisibleFields(object, ClientSession.getSession());
	        for (int i = 0; i < fields.length; i++) {
	            if(fields[i] instanceof NakedObjectAssociation && !(object.getSpecification().isLookup())) {
	                return true;
	            }
	        }
	        return false;
		} else {
		    return false;
		}
	}
	
	public View createView(Content content, ViewAxis axis) {
		View treeLeafNode = new TreeLeafNode(content, this, axis);
        return new TreeNodeBorder(new ObjectBorder(treeLeafNode), replacementNodeSpecification);
	}

	public String getName() {
		return null;
	}

	public boolean isOpen() {
		return false;
	}

	public boolean isReplaceable() {
		return false;
	}

	public boolean isSubView() {
		return true;
	}

	void setReplacementNodeSpecification(ViewSpecification replacementNodeSpecification) {
		this.replacementNodeSpecification = replacementNodeSpecification;
	}
}

class TreeLeafNode extends ObjectView {

    public TreeLeafNode(Content content, ViewSpecification design, ViewAxis axis) {
        super(content, design, axis);
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