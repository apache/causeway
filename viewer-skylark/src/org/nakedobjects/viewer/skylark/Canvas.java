package org.nakedobjects.viewer.skylark;

public interface Canvas {
    void clearBackground(View view, Color color);

    Canvas createSubcanvas();

    Canvas createSubcanvas(Bounds bounds);

    Canvas createSubcanvas(int x, int y, int width, int height);

    void draw3DRectangle(int x, int y, int width, int height, Color color, boolean raised);

    void drawDebugOutline(Bounds bounds, int baseline, Color color);

    void drawIcon(Image icon, int x, int y);

    void drawIcon(Image icon, int x, int y, int width, int height);

    void drawLine(int x, int y, int x2, int y2, Color color);

    void drawLine(Location start, int xExtent, int yExtent, Color color);

    void drawOval(int x, int y, int width, int height, Color color);

    void drawRectangle(int x, int y, int width, int height, Color color);

    void drawRectangleAround(View view, Color color);

    void drawRoundedRectangle(int x, int y, int width, int height, int arcWidth, int arcHeight, Color color);

    void drawShape(Shape shape, Color color);

    void drawShape(Shape shape, int x, int y, Color color);

    void drawSolidOval(int x, int y, int width, int height, Color color);

    void drawSolidRectangle(int x, int y, int width, int height, Color color);

    void drawSolidShape(Shape shape, Color color);

    void drawSolidShape(Shape shape, int x, int y, Color color);

    void drawText(String text, int x, int y, Color color, Text style);

    void offset(int x, int y);

    boolean overlaps(Bounds bounds);
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */