package org.nakedobjects.viewer.skylark.tree;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.basic.ObjectBorder;


class ElementNodeSpecification extends CollectionLeafNodeSpecification {
    public boolean canDisplay(Content content) {
        return content.isObject() && content.getNaked() != null;
    }

    public boolean canOpen(Content content) {
        NakedObject object = (NakedObject) content.getNaked();
        NakedObjectField[] fields = object.getVisibleFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isCollection()) {
                return true;
            }
        }

        return false;
    }

    protected View createView(View treeLeafNode, Content content, ViewAxis axis) {
        return new ObjectBorder(treeLeafNode);
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