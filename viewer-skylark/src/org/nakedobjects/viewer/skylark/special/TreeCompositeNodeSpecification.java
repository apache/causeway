package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.viewer.skylark.CompositeViewBuilder;
import org.nakedobjects.viewer.skylark.CompositeViewSpecification;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.OneToManyField;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;


public class TreeCompositeNodeSpecification implements CompositeViewSpecification, SubviewSpec,
    TreeNodeSpecification {
    private CompositeViewBuilder builder;
    private TreeLeafNodeSpecification collectionNodeSpecification;
    private TreeLeafNodeSpecification objectNodeSpecification;
    private TreeLeafNodeSpecification replacementSpecification;

    private TreeCompositeNodeSpecification() {
    }

    public static ViewSpecification createCollectionNode(TreeLeafNodeSpecification objectLeafNode,
        TreeLeafNodeSpecification collectionLeafNode) {
        TreeCompositeNodeSpecification spec = new TreeCompositeNodeSpecification();
        spec.builder = new StackLayout(new CollectionElementBuilder(spec, true));
        spec.replacementSpecification = collectionLeafNode;
        spec.collectionNodeSpecification = collectionLeafNode;
        spec.objectNodeSpecification = objectLeafNode;

        return spec;
    }

    public static ViewSpecification createObjectNode(TreeLeafNodeSpecification objectLeafNode,
        TreeLeafNodeSpecification collectionLeafNode) {
        TreeCompositeNodeSpecification spec = new TreeCompositeNodeSpecification();
        spec.builder = new StackLayout(new ObjectFieldBuilder(spec));
        spec.replacementSpecification = objectLeafNode;
        spec.collectionNodeSpecification = collectionLeafNode;
        spec.objectNodeSpecification = objectLeafNode;

        return spec;
    }

    public boolean canDisplay(Naked object) {
        return TreeDisplayRules.canDisplay(object);
    }

    public boolean canOpen(Content content) {
    	return true;
    }

    public View createSubview(Content content, ViewAxis axis) {
        if(content instanceof OneToManyField) {
            return collectionNodeSpecification.createView(content, axis);
        }
        
        if (content instanceof ObjectContent) {
            NakedObject object = ((ObjectContent) content).getObject();
            if(! TreeDisplayRules.canDisplay(object)) {
                return null;
            }
            
            if(((ObjectContent) content).getSpecification().isValue()) {
                return null;
            }
            
            if(object == null) {
            	return null;
            } else if (object instanceof NakedCollection) {
                return collectionNodeSpecification.createView(content, axis);
            } else {
                return objectNodeSpecification.createView(content, axis);
            }
        }

        return null;
    }

    public View createView(Content content, ViewAxis axis) {
        return new TreeNodeBorder(builder.createCompositeView(content, this, axis),
            replacementSpecification);
    }

    public View decorateSubview(View view) {
        return view;
    }

    public String getName() {
        return null;
    }

    public CompositeViewBuilder getSubviewBuilder() {
        return builder;
    }

    public boolean isOpen() {
        return true;
    }

    public boolean isReplaceable() {
        return false;
    }

    public boolean isSubView() {
        return true;
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
