package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.FieldContent;
import org.nakedobjects.viewer.skylark.InternalDrag;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;

public class TableCellResizeBorder extends AbstractBorder {
	public TableCellResizeBorder(View view) {
		super(view);
		
		bottom = 1;
		right = 3;
	}
	
	public void mouseMoved(Location at) {
		int x = at.getX();
		int boundary = boundary();
		if(x >= boundary - 2 && x <= boundary + 2) {
			getViewManager().showResizeRightCursor();
		} else {
			getViewManager().showDefaultCursor();
		} 
	}
	
	private int boundary() {
		return getSize().getWidth() - 2;
	}

	public void draw(Canvas canvas) {
		int x1 = 0;
		int y1 = 0;
		int x2 = getSize().getWidth() - 1;
		int y2 = getSize().getHeight() - 1;
		canvas.drawLine(x1, y2, x2, y2, Style.SECONDARY2);
		canvas.drawLine(x2, y1, x2, y2, Style.SECONDARY2);

		canvas.drawLine(boundary(), y1, boundary(), y2, Style.SECONDARY2);

		super.draw(canvas);
	}
	
	public View dragFrom(InternalDrag drag) {
		return new ViewResizeOutline(drag, this, ViewResizeOutline.RIGHT);
	}
	
	public void dragTo(InternalDrag drag) {
		View target = getView();
		TableColumnAxis axis = (TableColumnAxis) target.getViewAxis();
		FieldContent content = (FieldContent) target.getContent();
		FieldSpecification field = content.getField();
		
		FieldSpecification[] fields = axis.getFields();
		for (int i = 0; i < fields.length; i++) {
			if(fields[i] == field) {
				int width = drag.getLocation().getX();
				axis.setWidth(i, width);
				axis.invalidateLayout();
				break;
			}
		}
	}
	
	public ViewAreaType viewAreaType(Location at) {
		int x = at.getX();
		int boundary = boundary();
		if(x >= boundary) {
			return ViewAreaType.INTERNAL;
		} else {
			return super.viewAreaType(at);
		}
	}
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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