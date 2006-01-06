package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.utility.DebugString;
import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Image;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Shape;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;


public class DebugCanvas implements Canvas {
    private DebugString buffer;
    private int level;

    public DebugCanvas(DebugString buffer, Bounds bounds) {
        this(buffer, 0);
    }

    private DebugCanvas(DebugString buffer, int level) {
        this.level = level;
        this.buffer = buffer;
    }

    public void clearBackground(View view, Color color) {
        indent();
        buffer.appendln("Clear background of " + view + " to " + color);
    }

    public Canvas createSubcanvas() {
        buffer.blankLine();
        indent();
        buffer.appendln("Create subcanvas for same area");
        return new DebugCanvas(buffer, level + 1);
    }

    public Canvas createSubcanvas(Bounds bounds) {
        return createSubcanvas(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    public Canvas createSubcanvas(int x, int y, int width, int height) {
        buffer.blankLine();
        indent();
        buffer.appendln("Create subcanvas for area " + x + "," + y + " " + width + "x" + height);
        return new DebugCanvas(buffer, level + 1);
    }

    public void draw3DRectangle(int x, int y, int width, int height, Color color, boolean raised) {
        indent();
        buffer.appendln("3D rectangle " + x + "," + y + " " + width + "x" + height);
    }

    public void drawIcon(Image icon, int x, int y) {
        indent();
        buffer.appendln("Icon " + x + "," + y + " " + icon.getWidth() + "x" + icon.getHeight());
    }

    public void drawIcon(Image icon, int x, int y, int width, int height) {
        indent();
        buffer.appendln("Icon " + x + "," + y + " " + width + "x" + height);
    }

    public void drawLine(int x, int y, int x2, int y2, Color color) {
        indent();
        buffer.appendln("Line from " + x + "," + y + " to " + x2 + "," + y2 + " " + color);
    }

    public void drawLine(Location start, int xExtent, int yExtent, Color color) {
        indent();
        buffer.appendln("Line from " + start.getX() + "," + start.getY() + " to " + (start.getX() + xExtent) + ","
                + (start.getY() + yExtent) + " " + color);
    }

    public void drawOval(int x, int y, int width, int height, Color color) {
        indent();
        buffer.appendln("Oval " + x + "," + y + " " + width + "x" + height + " " + color);
    }

    public void drawRectangle(int x, int y, int width, int height, Color color) {
        indent();
        buffer.appendln("Rectangle " + x + "," + y + " " + width + "x" + height + " " + color);
    }

    public void drawRectangleAround(View view, Color color) {
        Bounds bounds = view.getBounds();
        indent();
        buffer.appendln("Rectangle 0,0 " + bounds.getWidth() + "x" + bounds.getHeight() + " " + color);
    }

    public void drawRoundedRectangle(int x, int y, int width, int height, int arcWidth, int arcHeight, Color color) {
        indent();
        buffer.appendln("Rounded Rectangle " + x + "," + y + " " + (x + width) + "x" + (y + height) + " " + color);
    }

    public void drawShape(Shape shape, Color color) {
        indent();
        buffer.appendln("Shape " + shape + " " + color);
    }

    public void drawShape(Shape shape, int x, int y, Color color) {
        indent();
        buffer.appendln("Shape " + shape + " at " + x + "/" + y + " (left, top)" + " " + color);
    }

    public void drawSolidOval(int x, int y, int width, int height, Color color) {
        indent();
        buffer.appendln("Oval (full) " + x + "," + y + " " + width + "x" + height + " " + color);
    }

    public void drawSolidRectangle(int x, int y, int width, int height, Color color) {
        indent();
        buffer.appendln("Rectangle (full) " + x + "," + y + " " + width + "x" + height + " " + color);
    }

    public void drawSolidShape(Shape shape, Color color) {
        indent();
        buffer.appendln("Shape (filled) " + shape + " " + color);
    }

    public void drawSolidShape(Shape shape, int x, int y, Color color) {
        indent();
        buffer.appendln("Shape (filled)" + shape + " at " + x + "/" + y + " (left, top)" + " " + color);
    }

    public void drawText(String text, int x, int y, Color color, Text style) {
        indent();
        buffer.appendln("Text " + x + "," + y + " \"" + text + "\" " + style + " " + color);
    }

    private void indent() {
//        buffer.append("\n");
        for (int i = 0; i < level; i++) {
            buffer.append("   ");
        }
    }

    public void offset(int x, int y) {
        indent();
        buffer.appendln("Offset by " + x + "/" + y + " (left, top)");
    }

    public boolean overlaps(Bounds bounds) {
        return true;
    }

    public String toString() {
        return "Canvas";
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