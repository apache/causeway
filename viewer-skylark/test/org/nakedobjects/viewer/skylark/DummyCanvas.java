package org.nakedobjects.viewer.skylark;

public class DummyCanvas implements Canvas {

    public DummyCanvas() {
        super();
    }

    public void clearBackground(View view, Color color) {}

    public Canvas createSubcanvas() {
        return null;
    }

    public Canvas createSubcanvas(Bounds bounds) {
        return null;
    }

    public Canvas createSubcanvas(int x, int y, int width, int height) {
        return null;
    }

    public void draw3DRectangle(int x, int y, int width, int height, Color color, boolean raised) {}

    public void drawIcon(Image icon, int x, int y) {}

    public void drawIcon(Image icon, int x, int y, int width, int height) {}

    public void drawLine(int x, int y, int x2, int y2, Color color) {}

    public void drawLine(Location start, int xExtent, int yExtent, Color color) {}

    public void drawOval(int x, int y, int width, int height, Color color) {}

    public void drawRectangle(int x, int y, int width, int height, Color color) {}

    public void drawRectangleAround(View view, Color color) {}

    public void drawRoundedRectangle(int x, int y, int width, int height, int arcWidth, int arcHeight, Color color) {}

    public void drawShape(Shape shape, Color color) {}

    public void drawShape(Shape shape, int x, int y, Color color) {}

    public void drawSolidOval(int x, int y, int width, int height, Color color) {}

    public void drawSolidRectangle(int x, int y, int width, int height, Color color) {}

    public void drawSolidShape(Shape shape, Color color) {}

    public void drawSolidShape(Shape shape, int x, int y, Color color) {}

    public void drawText(String text, int x, int y, Color color, Text style) {}

    public void offset(int x, int y) {}

    public boolean overlaps(Bounds bounds) {
        return false;
    }

    public void drawDebugOutline(Bounds bounds, int baseline, Color color) {}

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */