package org.nakedobjects.viewer.skylark.tree;

import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.NullFocusManager;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;


abstract class NodeSpecification implements ViewSpecification {
    public static final int CAN_OPEN = 1;
    public static final int CANT_OPEN = 2;
    public static final int UNKNOWN = 0;
    private ViewSpecification replacementNodeSpecification;

    public abstract int canOpen(Content content);

    protected abstract View createNodeView(Content content, ViewAxis axis);

    public final View createView(final Content content, final ViewAxis axis) {
        View view = createNodeView(content, axis);
        TreeNodeBorder newView = new TreeNodeBorder(view, replacementNodeSpecification);
        newView.setFocusManager(new NullFocusManager());
        return newView;
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

    final void setReplacementNodeSpecification(final ViewSpecification replacementNodeSpecification) {
        this.replacementNodeSpecification = replacementNodeSpecification;
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