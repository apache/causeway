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
package org.nakedobjects.viewer.lightweight.options;


import org.nakedobjects.viewer.lightweight.AbstractView;
import org.nakedobjects.viewer.lightweight.Bounds;
import org.nakedobjects.viewer.lightweight.Canvas;
import org.nakedobjects.viewer.lightweight.Color;
import org.nakedobjects.viewer.lightweight.Control;
import org.nakedobjects.viewer.lightweight.Location;
import org.nakedobjects.viewer.lightweight.Size;
import org.nakedobjects.viewer.lightweight.Style;
import org.nakedobjects.viewer.lightweight.UserAction;
import org.nakedobjects.viewer.lightweight.View;
import org.nakedobjects.viewer.lightweight.Workspace;


public abstract class WindowButton implements Control {
    private Bounds bounds = new Bounds();
    private UserAction action;

    protected WindowButton(UserAction action) {
        this.action = action;
    }

    public Size getRequiredSize() {
        return new Size(8, 8);
    }

    public void invoke(Workspace frame, View view, Location at) {
        action.execute(frame, view, at);
    }

    public void paint(Canvas canvas) {
        if (AbstractView.DEBUG) {
            canvas.drawRectangle(0, 0, bounds.getWidth(), bounds.getWidth(), Color.DEBUG1);
        }

        canvas.drawFullRectangle(0, 0, bounds.getWidth(), bounds.getWidth(), Style.VIEW_BACKGROUND);

        paint(canvas, Style.OTHER);
    }

    public abstract void paint(Canvas canvas, Color color);

    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
    }

    public Bounds getBounds() {
        return new Bounds(bounds);
    }

    public String toString() {
        Bounds s = getBounds();

        return "WindowButton [" + s + ",action=" + action + "]";
    }
}
