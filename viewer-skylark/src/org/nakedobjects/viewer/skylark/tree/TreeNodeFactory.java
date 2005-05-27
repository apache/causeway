package org.nakedobjects.viewer.skylark.tree;

import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;


public class TreeNodeFactory {
    private final CollectionCompositeNodeSpecification collectionCompositeNode;
//    private final CollectionLeafNodeSpecification collectionLeafNode;
    private final ObjectCompositeNodeSpecification objectCompositeNode;
//    private final ObjectLeafNodeSpecification objectLeafNode;

    public TreeNodeFactory() {
        ObjectLeafNodeSpecification objectLeafNode = new ObjectLeafNodeSpecification();
        CollectionLeafNodeSpecification collectionLeafNode = new CollectionLeafNodeSpecification();

        objectCompositeNode = new ObjectCompositeNodeSpecification();

        objectCompositeNode.setCollectionLeafNodeSpecification(collectionLeafNode);
        objectCompositeNode.setObjectLeafNodeSpecification(objectLeafNode);
        objectCompositeNode.setReplacementNodeSpecification(objectLeafNode);

        objectLeafNode.setReplacementNodeSpecification(objectCompositeNode);

        collectionCompositeNode = new CollectionCompositeNodeSpecification();

        collectionCompositeNode.setCollectionLeafNodeSpecification(collectionLeafNode);
        collectionCompositeNode.setObjectLeafNodeSpecification(objectLeafNode);
        collectionCompositeNode.setReplacementNodeSpecification(objectLeafNode);

        collectionLeafNode.setReplacementNodeSpecification(collectionCompositeNode);

        
  //     objectCompositeNode = TreeCompositeNodeSpecification.createObjectNode(objectLeafNode, collectionLeafNode);
 //       collectionCompositeNode = TreeCompositeNodeSpecification.createCollectionNode(objectLeafNode, collectionLeafNode);


//        this.objectLeafNode = objectLeafNode;
 //       this.collectionLeafNode = collectionLeafNode;
    }

    public View createRootCollectionNode(Content content, ViewAxis axis) {
        return collectionCompositeNode.createView(content, axis);
    }

    public View createRootObjectNode(Content content, ViewAxis axis) {
        return objectCompositeNode.createView(content, axis);
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