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

import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.value.Option;
import org.nakedobjects.viewer.lightweight.AbstractValueView;
import org.nakedobjects.viewer.lightweight.Canvas;
import org.nakedobjects.viewer.lightweight.Color;
import org.nakedobjects.viewer.lightweight.ObjectView;
import org.nakedobjects.viewer.lightweight.Shape;
import org.nakedobjects.viewer.lightweight.Size;
import org.nakedobjects.viewer.lightweight.Style;


public class OptionField extends AbstractValueView {
	private Option value;

	public void draw(Canvas canvas) {
		Color color;
		if (hasFocus()) {
			color = Style.ACTIVE;
		} else if(((ObjectView)getParent()).getState().isObjectIdentified()) {
			color = Style.IDENTIFIED;
		} else  if(((ObjectView)getParent()).getState().isRootViewIdentified()) {
			color = Style.IN_FOREGROUND;
		} else {
			color = Style.IN_BACKGROUND;
		}

		String title = value.title().toString();
		canvas.drawText(title, 0, getBaseline(), color, Style.NORMAL);
		int height = getSize().getHeight() * 3 / 5;
		int width = getSize().getHeight() * 4 / 5;
		Shape triangle = new Shape(0, height);
		triangle.addLine(width, 0);
		triangle.addLine(-width / 2, -height);
		triangle.addLine(width / 2, height);

		canvas.drawSolidShape(triangle, Style.NORMAL.stringWidth(title), getBaseline(), color);
	}
    
	public int getBaseline() {
		return 9;
	}

	public Size getRequiredSize() {
		String title = value.title().toString();
		int height = defaultFieldHeight();
		Size size = new Size(Style.NORMAL.stringWidth(title) + HPADDING + height * 3 / 5, height);
		size.ensureWidth(80);
		return size;
	}

	public NakedValue getValue() {
		return value;
	}

	public void refresh() {
		if(objectField.isDerived()) {
			ObjectView p = (ObjectView) getParent();
			value = (Option) objectField.get(p.getObject());
		}
	}

	protected void init(NakedValue value) {
		this.value = (Option) value;
	}


}
