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

import org.nakedobjects.viewer.lightweight.Layout;
import org.nakedobjects.viewer.lightweight.LayoutTarget;
import org.nakedobjects.viewer.lightweight.Location;
import org.nakedobjects.viewer.lightweight.Size;
import org.nakedobjects.viewer.lightweight.View;


public class BrowserLayout implements Layout {
	public void layout(LayoutTarget target) {
		int top = target.getPadding().getTop();
		int left = target.getPadding().getLeft();
		
		Size size;
		View[] components = target.getComponents();

		for (int i = 0; i < components.length; i++) {
			size = components[i].getRequiredSize();
			components[i].setLocation(new Location(left, top));
			components[i].setSize(size);
			left += size.getWidth() + 3;
		}
	}

	public Size requiredSize(LayoutTarget target) {
		Size targetSize = new Size(0, 0);
		Size componentSize;
		View[] components = target.getComponents();

		for (int i = 0; i < components.length; i++) {
			componentSize = components[i].getRequiredSize();
			targetSize.extendWidth(componentSize.getWidth());
			targetSize.ensureHeight(componentSize.getHeight());
		}

		targetSize.addPadding(target.getPadding());
		return targetSize;
	}

}
