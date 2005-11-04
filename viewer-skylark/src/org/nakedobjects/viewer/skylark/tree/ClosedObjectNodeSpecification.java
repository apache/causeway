package org.nakedobjects.viewer.skylark.tree;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.basic.ObjectBorder;


/**
 * Specification for a tree node that will display a closed object as a root node or within an object. This
 * will indicate that the created view can be opened if: one of it fields is a collection; it is set up to
 * show objects within objects and one of the fields is an object but it is not a lookup.
 * 
 * @see org.nakedobjects.viewer.skylark.tree.OpenObjectNodeSpecification for displaying an open collection as
 *         part of an object.
 */
class ClosedObjectNodeSpecification extends NodeSpecification {
    private final boolean showObjectContents;

    public ClosedObjectNodeSpecification(final boolean showObjectContents) {
        this.showObjectContents = showObjectContents;
    }

    public boolean canDisplay(Content content) {
        return content.isObject() && content.getNaked() != null;
    }

    public int canOpen(Content content) {
        NakedObject object = ((ObjectContent) content).getObject();
        NakedObjectField[] fields = object.getVisibleFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isCollection()) {
                return CAN_OPEN;
            }

            if (showObjectContents && fields[i].isObject() && !(object.getSpecification().isLookup())) {
                return CAN_OPEN;
            }
        }
        return CANT_OPEN;
    }

    protected View createNodeView(Content content, ViewAxis axis) {
        View treeLeafNode = new LeafNodeView(content, this, axis);
        return new ObjectBorder(treeLeafNode);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */