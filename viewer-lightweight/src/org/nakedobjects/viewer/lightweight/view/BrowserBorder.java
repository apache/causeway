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

import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.viewer.lightweight.Canvas;
import org.nakedobjects.viewer.lightweight.Click;
import org.nakedobjects.viewer.lightweight.Color;
import org.nakedobjects.viewer.lightweight.MenuOptionSet;
import org.nakedobjects.viewer.lightweight.ObjectView;
import org.nakedobjects.viewer.lightweight.ObjectViewState;
import org.nakedobjects.viewer.lightweight.Padding;
import org.nakedobjects.viewer.lightweight.Style;
import org.nakedobjects.viewer.lightweight.View;


public class BrowserBorder extends IconBorder {
	private final static int WIDTH = 18;
	
    public Padding getPadding(View view) {
    	Padding padding = super.getPadding(view);
    	padding.extendLeft(WIDTH);
    	return padding;
    }

    public String debug(View view) {
        Padding in = getPadding(view);

        String name = getClass().getName();

        return name.substring(name.lastIndexOf('.') + 1) + " [top=" + in.getTop() + ",left=" +
        in.getLeft() + ",bottom=" + in.getBottom() + ",right=" + in.getRight() + "]";
    }

    public void draw(View view, Canvas canvas) {
		super.draw(view, canvas);

		boolean canOpen;
        int x = 2;
        int y = view.getBaseline() - 10;

        if (((ObjectView) view).getObject() instanceof InternalCollection) {
            canOpen = ((InternalCollection) ((ObjectView) view).getObject()).size() > 0;
        } else {
            canOpen = true;
        }
        
		Color color;
        ObjectViewState state = ((ObjectView) view).getState();
        if(state.isRootViewIdentified()) {
        	color = Style.IN_FOREGROUND;
        } else {
        	color = Style.FEINT;
        }

		int yCenter = y + 5;
		int xCenter = x + 5;
		canvas.drawLine(x, yCenter, x + WIDTH, yCenter, color);
		canvas.drawRectangle(x, y, 10, 10, color);
		canvas.drawFullRectangle(x + 1, y + 1, 9, 9, Style.VIEW_BACKGROUND);
		if (canOpen) {
            canvas.drawLine(x + 2, yCenter, x + 8, yCenter, color);
			canvas.drawLine(xCenter, y + 2, xCenter, y + 8, color);
        } else {
            canvas.drawLine(x + 2, yCenter, x + 8, yCenter, color);
        }
    }

    public void firstClick(View view, Click click) {
    }

    public void secondClick(View view, Click click) {
    }

    public void viewMenuOptions(View view, MenuOptionSet menuOptions) {
    }
}
