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

/**
 * The flow layout gets the targets required width, and layouts its components across
 * the container, wrapping onto the next row when necessary.
 */
public class FlowLayout  implements Layout{
	private int maxWidth;
	private int minHeight;

	public FlowLayout(int maxWidth, int minHeight) {
		this.maxWidth = maxWidth;
		this.minHeight = minHeight;
	}
	
	public void layout(LayoutTarget target) {
		Padding padding = target.getPadding();
		int top = padding.getTop();
		int left = padding.getLeft();
		
		View[] components = target.getComponents();
		for (int i = 0; i < components.length; i++) {
			Size size = components[i].getRequiredSize();
			components[i].setSize(size);
			if(left + size.getWidth() > maxWidth) {
				left = padding.getLeft();
				top += size.getHeight();
			} 
			components[i].setLocation(new Location(left, top));
			left += size.getWidth();
		}
	}

	public Size requiredSize(LayoutTarget target) {
		Size targetSize = new Size(maxWidth, 0);
		View[] components = target.getComponents();

		Size rowSize = new Size(); 
		for (int i = 0; i < components.length; i++) {
			Size componentSize = components[i].getRequiredSize();
			rowSize.extendWidth(componentSize.getWidth());
			rowSize.ensureHeight(componentSize.getHeight());
			if(rowSize.getWidth() > maxWidth) {
				targetSize.extendHeight(rowSize.getHeight());
				rowSize = new Size(componentSize.getWidth(), 0);
			}
		}
		targetSize.extendHeight(rowSize.getHeight());
		targetSize.ensureHeight(minHeight);
		Padding insets = target.getPadding();

		targetSize.addPadding(insets);

		return targetSize;
	}
}
