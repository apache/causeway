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
import org.nakedobjects.viewer.lightweight.Padding;
import org.nakedobjects.viewer.lightweight.Size;
import org.nakedobjects.viewer.lightweight.View;


public class RowLayout implements Layout {
    private static final int DEFAULT_WIDTH = 90;
	private static final int MINIMUM_WIDTH = 0;
    private int[] columnWidth;
    private int gap;

    public RowLayout(int gap) {
        this.gap = gap;
    }

    public void setColumnSize(int columnNo, int width) {
		if (columnWidth == null) {
			throw new IllegalStateException("Column size cannot be changed before layout() or requiredSize()");
		}
		int difference = columnWidth[columnNo] - width;
        columnWidth[columnNo] = width;
        if(columnNo + 1 < columnWidth.length) {
        	columnWidth[columnNo + 1] += difference;
        }
    }

    
    public void extendColumn(int column, int position) {
    	int pos = 0;
    	for (int i = 0; i < column; i++) {
			pos += columnWidth[i] + gap;
			
		}
        columnWidth[column] = position - pos;
        
        for(int i = column - 1; i >= 0; i--) {
        	if(columnWidth[i] > position) {
				columnWidth[i] = position - MINIMUM_WIDTH;
        	}
        }
    }
    
    public int[] getPositions() {
		if (columnWidth == null) {
			throw new IllegalStateException("Column size cannot be changed before layout() or requiredSize()");
		}
        int noColumns = columnWidth.length;
        int position = 0;
        int[] positions = new int[noColumns];

        for (int i = 0; i < noColumns; i++) {
            position += columnWidth[i] + gap;
            positions[i] = position;
        }

        return positions;
    }

    public void layout(LayoutTarget target) {
        Padding insets = target.getPadding();
        int left = insets.getLeft();
        int x = left;
        int y = insets.getTop();

        Size comp;
        View[] views = views(target);

        for (int i = 0; i < views.length; i++) {
            comp = views[i].getRequiredSize();
            comp.setWidth(columnWidth[i]);
            views[i].setLocation(new Location(x, y));
            views[i].setSize(comp);
            x += comp.getWidth() + gap;
        }
    }

    public Size requiredSize(LayoutTarget target) {
        Size container = new Size();
        Size component;
        View[] views = views(target);

        for (int i = 0; i < views.length; i++) {
            component = views[i].getRequiredSize();
            component.setWidth(columnWidth[i]);
            container.ensureHeight(component.getHeight());
            container.extendWidth(component.getWidth() + gap);
        }

        container.addPadding(target.getPadding());

        return container;
    }

    private View[] views(LayoutTarget target) {
        View[] components = target.getComponents();

        if (columnWidth == null) {
            columnWidth = new int[components.length];

            for (int i = 0; i < columnWidth.length; i++) {
                columnWidth[i] = DEFAULT_WIDTH;
            }
        }

        return components;
    }
}
