package org.nakedobjects.viewer.skylark.tree;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.special.ObjectFieldBuilder;
import org.nakedobjects.viewer.skylark.special.StackLayout;

/**
 * Specification for a tree node that will display an open object as a root node or within an object. 
 * 
 * @see org.nakedobjects.viewer.skylark.tree.ClosedObjectNodeSpecification for displaying 
 * a closed collection within an object.
 */
public class OpenObjectNodeSpecification extends CompositeNodeSpecification {

    public OpenObjectNodeSpecification() {
        builder = new StackLayout(new ObjectFieldBuilder(this));
    }

    /**
     * This is only used to control root nodes. Therefore a object tree can only be displayed for an object
     * with fields that are collections.
     */
    public boolean canDisplay(Content content) {
        if (content.isObject() && content.getNaked() != null) {
            NakedObject object = (NakedObject) content.getNaked();
            NakedObjectField[] fields = object.getVisibleFields();
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].isCollection()) {
                    return true;
                }
            }
        }

        return false;
    }

    public int canOpen(Content content) {
        return CAN_OPEN;
    }

    public boolean isOpen() {
        return true;
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
