package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.defaults.Lookup;
import org.nakedobjects.object.reflect.AssociationSpecification;
import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.core.ObjectView;

class TreeLeafNodeSpecification implements ViewSpecification, TreeNodeSpecification {
	private ViewSpecification replacementNodeSpecification;

	public boolean canDisplay(Naked object) {
		return object instanceof NakedObject && !(object instanceof Lookup);
	}

	public boolean canOpen(Content content) {
		NakedObject object = ((ObjectContent) content).getObject();
		if(object instanceof NakedCollection) {
			return ((NakedCollection) object).size() > 0;
		} else {
			FieldSpecification[] fields = object.getSpecification().getVisibleFields(object, ClientSession.getSession());
			for (int i = 0; i < fields.length; i++) {
				if(fields[i] instanceof AssociationSpecification && !(object instanceof Lookup)) {
					return true;
				}
			}
			return false;
		}
	}
	
	public View createView(Content content, ViewAxis axis) {
		return new TreeNodeBorder(new TreeLeafNode(content, this, axis), replacementNodeSpecification);
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