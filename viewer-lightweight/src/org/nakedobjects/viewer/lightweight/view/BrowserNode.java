/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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
package org.nakedobjects.viewer.lightweight.view;

import org.nakedobjects.viewer.lightweight.AbstractCompositeView;
import org.nakedobjects.viewer.lightweight.Click;
import org.nakedobjects.viewer.lightweight.DragSource;
import org.nakedobjects.viewer.lightweight.DragTarget;
import org.nakedobjects.viewer.lightweight.DragView;
import org.nakedobjects.viewer.lightweight.InternalView;
import org.nakedobjects.viewer.lightweight.ObjectIconView;
import org.nakedobjects.viewer.lightweight.Size;


class BrowserNode extends ObjectIconView implements InternalView, DragSource, DragView, DragTarget {
	private static final BrowserTree treePrototype = new BrowserTree();

	public BrowserNode() {
        setBorder(new BrowserBorder());
	}

     public String getName() {
        return "Browser Node";
    }

    public boolean isReplaceable() {
        return false;
    }

    public Size getRequiredSize() {
        Size size = super.getRequiredSize();
        size.extend(titleSize());

        return size;
    }

    public void firstClick(Click click) {
        getBrowser().setSelected(this);
    }

    private Browser getBrowser() {
		return ((Browser) getRoot());
	}

    public void secondClick(Click click) {
        BrowserTree replacement = (BrowserTree) treePrototype.makeView(getObject(), getFieldOf());
        ((AbstractCompositeView) getParent()).replaceView(this, replacement);

        getBrowser().setSelected(replacement);
    }
}
