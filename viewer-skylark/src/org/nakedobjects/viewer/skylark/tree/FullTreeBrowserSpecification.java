package org.nakedobjects.viewer.skylark.tree;

import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.metal.WindowBorder;

/**
 * Specification for a tree browser frame with a tree displaying only collections and objects containing collections.
 */
public class FullTreeBrowserSpecification implements ViewSpecification {
    private final CollectionCompositeNodeSpecification rootCollectionNode;
    private final ObjectCompositeNodeSpecification rootObjectNode;

    public FullTreeBrowserSpecification() {
        ObjectLeafNodeSpecification objectLeafNode = new ObjectLeafNodeSpecification();
        CollectionLeafNodeSpecification collectionLeafNode = new CollectionLeafNodeSpecification();

        rootObjectNode = new ObjectCompositeNodeSpecification();

        rootObjectNode.setCollectionSubNodeSpecification(collectionLeafNode);
        rootObjectNode.setObjectSubNodeSpecification(objectLeafNode);
        rootObjectNode.setReplacementNodeSpecification(objectLeafNode);

        objectLeafNode.setReplacementNodeSpecification(rootObjectNode);

        rootCollectionNode = new CollectionCompositeNodeSpecification();

        rootCollectionNode.setCollectionSubNodeSpecification(collectionLeafNode);
        rootCollectionNode.setObjectSubNodeSpecification(objectLeafNode);
        rootCollectionNode.setReplacementNodeSpecification(objectLeafNode);

        collectionLeafNode.setReplacementNodeSpecification(rootCollectionNode);
    }

    public boolean canDisplay(Content content) {
        return rootCollectionNode.canDisplay(content) || rootObjectNode.canDisplay(content);
    }

    public View createView(Content content, ViewAxis axis) {
        TreeBrowserFrame frame = new TreeBrowserFrame(content, this);

        View view = addBorder(frame);
        View rootNode;
        axis = frame;
        if (rootCollectionNode.canDisplay(content)) {
            rootNode = rootCollectionNode.createView(content, axis);
        } else {
            rootNode = rootObjectNode.createView(content, axis);
            frame.setSelectedNode(rootNode);
        }
        View leftPane = rootNode;
        frame.initLeftPane(leftPane);

        Size size = leftPane.getRequiredSize();
        size.setWidth(220);
        leftPane.setRequiredSize(size);

        return view;
    }

    protected View addBorder(View frame) {
        return new WindowBorder(frame, false);
    }

    public String getName() {
        return "Tree Browser";
    }

    public boolean isOpen() {
        return true;
    }

    public boolean isReplaceable() {
        return true;
    }

    public boolean isSubView() {
        return false;
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */
