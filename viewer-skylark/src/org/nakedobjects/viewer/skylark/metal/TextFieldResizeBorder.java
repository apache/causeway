package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Shape;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Viewer;
import org.nakedobjects.viewer.skylark.special.ResizeBorder;

public class TextFieldResizeBorder extends ResizeBorder {
    public static final int BORDER_WIDTH = NakedObjects.getConfiguration().getInteger(Viewer.PROPERTY_BASE + "resize.border", 5);

    public TextFieldResizeBorder(View view) {
        super(view, RIGHT + DOWN, 2);
    }
    

    protected void drawResizeBorder(Canvas canvas, Size size) {
        if (resizing) {
            Shape shape = new Shape(0, 0);
            int resizeMarkerSize = 10;
            shape.addLine(resizeMarkerSize, 0);
            shape.addLine(0, resizeMarkerSize);
            shape.addLine(-resizeMarkerSize, -resizeMarkerSize);
            canvas.drawSolidShape(shape, size.getWidth() - resizeMarkerSize, size.getHeight(), Style.RESIZE);
            canvas.drawRectangle(0, 0, size.getWidth() - 1, size.getHeight() - 1, Style.RESIZE);
        }
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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