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

import java.util.Vector;

import org.nakedobjects.utility.Configuration;
import org.nakedobjects.viewer.lightweight.Border;
import org.nakedobjects.viewer.lightweight.Bounds;
import org.nakedobjects.viewer.lightweight.Canvas;
import org.nakedobjects.viewer.lightweight.Click;
import org.nakedobjects.viewer.lightweight.Color;
import org.nakedobjects.viewer.lightweight.Control;
import org.nakedobjects.viewer.lightweight.Location;
import org.nakedobjects.viewer.lightweight.ObjectView;
import org.nakedobjects.viewer.lightweight.ObjectViewState;
import org.nakedobjects.viewer.lightweight.Padding;
import org.nakedobjects.viewer.lightweight.Size;
import org.nakedobjects.viewer.lightweight.Style;
import org.nakedobjects.viewer.lightweight.View;


public abstract class SimpleBorder implements Border {
    private static final boolean hasControls = Configuration.getInstance().getBoolean("viewer.lightweight.border-controls",
            true);
    private static int next = 0;
    private static final int handleWidth = 14;
    private int borderWidth;
    private final int no = next++;
    private Vector controls = new Vector();

    public SimpleBorder(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    public Padding getPadding(View view) {
        return new Padding(borderWidth + 1, borderWidth + 1, borderWidth + 1,
            borderWidth + handleWidth + 1);
    }

    public String debug(View view) {
        Padding in = getPadding(view);

        String name = getClass().getName();

        return name.substring(name.lastIndexOf('.') + 1) + " [top=" + in.getTop() + ",left=" +
        in.getLeft() + ",bottom=" + in.getBottom() + ",right=" + in.getRight() + "]";
    }

    public void draw(View view, Canvas canvas) {
        ObjectViewState state = ((ObjectView) view).getState();

        Color color;

        if (state.canDrop()) {
            color = Style.VALID;
        } else if (state.cantDrop()) {
            color = Style.INVALID;
        } else if (state.isObjectIdentified()) {
            color = getObjectIdentified();
        } else if (state.isViewIdentified()) {
            color = getViewIdentified();
        } else if (state.isRootViewIdentified()) {
            color = getRootViewIdentified();
        } else {
        	color = getInBackground();
        }
        if (color == null) {
        	return;
        }

        Size size = view.getSize();
        int width = size.getWidth();
        int height = size.getHeight();

        for (int i = 0; i < borderWidth; i++) {
            canvas.drawRectangle(i, i, width - (i * 2 + 1), height - (i * 2 + 1), color);
        }

        // draw view handle 
        if (state.isViewIdentified() && !state.isObjectIdentified()) {
			int x1 = width - handleWidth - borderWidth;
            int x2 = width - borderWidth;
            int y1 = borderWidth + 1;
            int y2 = height - borderWidth - 2;

            for (int x = x1; x < x2; x += 2) {
                canvas.drawLine(x, y1, x, y2, Style.FEINT);
            }
        }

        // draw controls
        int x = width - borderWidth - 3;
        int y = 0;

        if (state.isViewIdentified() && !state.isObjectIdentified()) {
            for (int i = 0; i < controls.size(); i++) {
                Control control = (Control) controls.elementAt(i);
                Size controlSize = control.getRequiredSize();
                int cwidth = controlSize.getWidth();
                int cheight = controlSize.getHeight();

                y += cheight + 4;

                Bounds controlBounds = new Bounds(x - cwidth, y, cwidth, cheight);
                control.setBounds(controlBounds);

                // TODO setting up bounds here seems misplaced 
                Canvas controlCanvas = canvas.createSubcanvas(controlBounds.getX(),
                        controlBounds.getY(), controlBounds.getWidth(), controlBounds.getHeight());
                control.paint(controlCanvas);
            }
        }
    }

	protected Color getInBackground() {
		return null;
	}

	protected Color getObjectIdentified() {
		return Style.IDENTIFIED;
	}

	protected Color getViewIdentified() {
		return Style.ACTIVE;
	}

	protected Color getRootViewIdentified() {
		return null;
	}


	public void firstClick(View view, Click click) {
        Location at = click.getLocation();

        for (int i = 0; i < controls.size(); i++) {
            Control control = (Control) controls.elementAt(i);

            if (control.getBounds().contains(at)) {
                control.invoke(view.getWorkspace(), view, click.getLocation());

                return;
            }
        }
    }

    public void secondClick(View view, Click click) {
    }

    public String toString() {
        String name = getClass().getName();

        return name.substring(name.lastIndexOf('.') + 1) + no;
    }

    protected void addControl(Control control) {
        if (hasControls) {
            controls.addElement(control);
        }
    }
}
