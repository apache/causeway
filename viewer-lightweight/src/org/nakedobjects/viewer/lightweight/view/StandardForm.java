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

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.viewer.lightweight.Canvas;
import org.nakedobjects.viewer.lightweight.Color;
import org.nakedobjects.viewer.lightweight.DragSource;
import org.nakedobjects.viewer.lightweight.DragTarget;
import org.nakedobjects.viewer.lightweight.InternalView;
import org.nakedobjects.viewer.lightweight.Padding;
import org.nakedobjects.viewer.lightweight.PrintableView;
import org.nakedobjects.viewer.lightweight.RootView;
import org.nakedobjects.viewer.lightweight.Style;
import org.nakedobjects.viewer.lightweight.View;
import org.nakedobjects.viewer.lightweight.util.StackLayout;
import org.nakedobjects.viewer.lightweight.util.ViewFactory;


public class StandardForm extends Form implements RootView, InternalView, DragSource, DragTarget, PrintableView {
	public StandardForm() {
		setLayout(new StackLayout());
	}

	public Padding getPadding() {
		Padding padding = super.getPadding();
		
		// labels
		int width = 0;

		if (getObject() != null) {
			Field[] fields = getObject().getNakedClass().getVisibleFields(getObject());

			for (int i = 0; i < fields.length; i++) {
				int labelWidth = Style.LABEL.stringWidth(fields[i].getName() + ":");
				width = Math.max(width, labelWidth);
			}
		}

		padding.extendLeft(width + HPADDING);
		
		return padding;
	}
	
	protected Style.Text getTitleTextStyle() {
		return isRoot() ? Style.TITLE : Style.NORMAL;
	}

	public void draw(Canvas canvas) {
		super.draw(canvas);

		// labels
		Field[] fields = getObject().getNakedClass().getVisibleFields(getObject());
		View[] components = getComponents();

		Color color;
		if (getState().canDrop()) {
			color = Style.VALID;
		} else if (getState().cantDrop()) {
			color = Style.INVALID;
		} else if (getState().isViewIdentified()) {
			color = Style.IN_FOREGROUND;
		} else if (getState().isRootViewIdentified()) {
			color = Style.IN_FOREGROUND;
		} else {
			color = Style.IN_BACKGROUND;
		}

		int top = getPadding().getTop();
		int left = super.getPadding().getLeft() + HPADDING;
		
		for (int i = 0; i < components.length; i++) {
			Field field = fields[i];
			int baseline = top + components[i].getBaseline();
			canvas.drawText(field.getName() + ":", left, baseline, color, Style.LABEL);
			top += components[i].getSize().getHeight();
		}
	}

	/**
	 * Creates all internal collections as open views, all others as icons
	 */
	public InternalView createFieldElement(Naked object, Field field) {
		boolean iconised = !(object instanceof InternalCollection); 
		return ViewFactory.getViewFactory().createInternalView(object, field, iconised);
	}
}
