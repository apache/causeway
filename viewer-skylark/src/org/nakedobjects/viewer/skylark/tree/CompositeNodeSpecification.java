package org.nakedobjects.viewer.skylark.tree;

import org.nakedobjects.viewer.skylark.CompositeViewBuilder;
import org.nakedobjects.viewer.skylark.CompositeViewSpecification;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.special.SubviewSpec;


public abstract class CompositeNodeSpecification extends NodeSpecification implements  CompositeViewSpecification, SubviewSpec {
    protected CompositeViewBuilder builder;
    private NodeSpecification collectionLeafNodeSpecification;
    private NodeSpecification objectLeafNodeSpecification;

    public void setCollectionSubNodeSpecification(NodeSpecification collectionLeafNodeSpecification) {
        this.collectionLeafNodeSpecification = collectionLeafNodeSpecification;
    }
    
    public void setObjectSubNodeSpecification(NodeSpecification objectLeafNodeSpecification) {
        this.objectLeafNodeSpecification = objectLeafNodeSpecification;
    }

    public boolean canOpen(Content content) {
    	return true;
    }

	protected View createView(View treeLeafNode, Content content, ViewAxis axis) {
        return builder.createCompositeView(content, this, axis);
    }
	
    public View decorateSubview(View view) {
        return view;
    }
    
    public CompositeViewBuilder getSubviewBuilder() {
        return builder;
    }

    public View createSubview(Content content, ViewAxis axis) {
        if(collectionLeafNodeSpecification.canDisplay(content)) {
            return collectionLeafNodeSpecification.createView(content, axis);
        }
        
        if(objectLeafNodeSpecification.canDisplay(content)) {
            return objectLeafNodeSpecification.createView(content, axis);
        }
        
        return null;
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
