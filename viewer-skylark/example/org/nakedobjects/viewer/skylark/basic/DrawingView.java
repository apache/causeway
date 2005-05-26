package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.utility.ToString;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.core.AbstractView;


public abstract class DrawingView extends AbstractView {

    private Size requiredSize;

    public DrawingView(Content content) {
        super(content, null, null);
    }

    public void draw(Canvas canvas) {
        final int width = requiredSize.getWidth();
        final int height = requiredSize.getHeight();
        final int left = 0, top = 0;
        final int right = 10 + width - 1 + 10;
        final int bottom = 10 + height - 1 + 10;

        // horizontal lines
        canvas.drawLine(left, top + 10, right, top + 10, Color.GRAY);
        canvas.drawLine(left, bottom - 10, right, bottom - 10, Color.GRAY);

        // vertical lines
        canvas.drawLine(left + 10, top, left + 10, bottom, Color.GRAY);
        canvas.drawLine(right - 10, top, right - 10, bottom, Color.GRAY);

        canvas.drawRectangle(left + 10, top + 10, width - 1, height - 1, Color.LIGHT_GRAY);

        draw(canvas, left + 10, top + 10);
    }

    protected abstract void draw(Canvas canvas, int x, int y);

    public Size getRequiredSize() {
        Size s = new Size(requiredSize);
        s.extend(20, 20);
        return s;
    }

    public void setRequiredSize(Size size) {
        this.requiredSize = size;
    }

    public String toString() {
        ToString ts = new ToString(this);
        ts.append("size", requiredSize);
        toString(ts);
        return ts.toString();
    }

    protected abstract void toString(ToString ts);
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */