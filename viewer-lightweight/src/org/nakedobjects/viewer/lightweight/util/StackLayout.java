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

package org.nakedobjects.viewer.lightweight.util;

import org.nakedobjects.viewer.lightweight.Layout;
import org.nakedobjects.viewer.lightweight.LayoutTarget;
import org.nakedobjects.viewer.lightweight.Location;
import org.nakedobjects.viewer.lightweight.Padding;
import org.nakedobjects.viewer.lightweight.Size;
import org.nakedobjects.viewer.lightweight.View;


public class StackLayout implements Layout {

	private boolean useMaxWidth;

	public StackLayout() {
		useMaxWidth = false;
	}

	public StackLayout(boolean useMaxWidth) {
		this.useMaxWidth = useMaxWidth;
	}

	public void layout(LayoutTarget target) {
		Padding insets = target.getPadding();
		int y = insets.getTop();
		int x = insets.getLeft();

		Size componentSize;
		View[] components = target.getComponents();
		
		int maxWidth = 0;
		if(useMaxWidth) { 
			for (int i = 0; i < components.length; i++) {
				maxWidth = Math.max(maxWidth, components[i].getRequiredSize().getWidth());
			}
		}
		
		for (int i = 0; i < components.length; i++) {
			componentSize = components[i].getRequiredSize();
			if(useMaxWidth) {
				componentSize.setWidth(maxWidth);
			}
			components[i].setSize(componentSize);
			components[i].setLocation(new Location(x, y));
			y += componentSize.getHeight();
		}
		target.setLayoutValid();
	}

	public Size requiredSize(LayoutTarget target) {
		Size dim = new Size(0, 0);
		Size componentSize;
		View[] components = target.getComponents();

		for (int i = 0; i < components.length; i++) {
			componentSize = components[i].getRequiredSize();
			dim.ensureWidth(componentSize.getWidth());
			dim.extendHeight(componentSize.getHeight());
		}
		dim.addPadding(target.getPadding());

		return dim;
	}
}
