package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;

public class TableColumnAxis implements ViewAxis {
	private FieldSpecification[] fields;
	private int[] widths;
	private int rowHeaderOffet;
	private View table;
	
	public TableColumnAxis(FieldSpecification[] fields, int defaultWidth) {
		this.fields = fields;
		widths = new int[fields.length];
		for (int i = 0; i < widths.length; i++) {
			 widths[i] = defaultWidth;
		}
	}

	public FieldSpecification[] getFields() {
		return fields;
	}

	public int[] getWidths() {
		return widths;
	}

	public int getOffset() {
		return rowHeaderOffet;
	}
	
	public void setWidth(int index, int width) {
		widths[index] = width;
	}

	public void setOffset(int offset) {
		rowHeaderOffet = Math.max(rowHeaderOffet, offset + 5);
	}
	
	public void setRoot(View view) {
		table = view;
	}

	public void invalidateLayout() {
		View[] rows = table.getSubviews();
		for (int i = 0; i < rows.length; i++) {
			rows[i].invalidateLayout();
		}
		table.invalidateLayout();
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