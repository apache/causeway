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

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.collection.TypedCollection;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.viewer.lightweight.Canvas;
import org.nakedobjects.viewer.lightweight.InternalView;
import org.nakedobjects.viewer.lightweight.Padding;
import org.nakedobjects.viewer.lightweight.RootView;
import org.nakedobjects.viewer.lightweight.Size;
import org.nakedobjects.viewer.lightweight.Style;



public class Table extends StandardList implements RootView, InternalView {
	private TableRow rowPrototype = new TableRow();
	private RowLayout rowLayout;
	private int gap = 3;
	private Style.Text label = Style.LABEL; 
	
	protected InternalView createListElement(NakedObject obj) {
		return (TableRow) rowPrototype.makeView(obj,  null);
	}

	protected void init(NakedObject object) {
		super.init(object);
		rowLayout = new RowLayout(gap);
		rowPrototype.setLayout(rowLayout);
		if(getFieldOf() == null) {
			setBorder(new RootBorder());
		} else {
			setBorder(new OpenFieldBorder());
		}
	}

	public Padding getPadding() {
		Padding padding = super.getPadding();
		padding.extendTop(VPADDING + label.getAscent() + VPADDING);
		return padding;
	}
	
	public void draw(Canvas canvas) {
		super.draw(canvas);
		
		int[] widths = rowLayout.getPositions();
		int left = rowPrototype.getPadding().getLeft();

		// labels
		String name = ((TypedCollection) getObject()).getType().getName();
        Field[] fields = NakedObjectManager.getInstance().getNakedClass(name).getFields();
		int x = left + gap;
		int y = super.getPadding().getTop() + VPADDING + label.getAscent();
		for (int i = 0; i < fields.length; i++) {
			canvas.drawText(fields[i].getName(), x, y, Style.IN_FOREGROUND, Style.LABEL);
			x = left + widths[i] + gap;
		}
		
		// dividers
		Padding padding = getPadding();
		Size size = getSize(); 
		int top = padding.getTop() + gap;
		int bottom = size.getHeight() - padding.getBottom() - gap;
		x = left + gap;
		for (int i = 0; i < widths.length; i++) {
			canvas.drawLine(x, top, x, bottom, Style.FEINT);
			x = left + widths[i] + gap;
		}
	}
	
	public String getName() {
		return "Table";
	}

}
