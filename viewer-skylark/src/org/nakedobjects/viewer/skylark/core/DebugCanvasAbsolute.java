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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.StringTokenizer;


public class DebugCanvasAbsolute implements Canvas {
    private DebugString buffer;
    private int level;
    private int offsetX;
    private int offsetY;

    public DebugCanvasAbsolute(DebugString buffer, Bounds bounds) {
        this(buffer, 0, bounds.getX(), bounds.getY());
    }

    private DebugCanvasAbsolute(DebugString buffer, int level, int x, int y) {
        this.level = level;
        this.buffer = buffer;
        offsetX = x;
        offsetY = y;
    }

    public void clearBackground(View view, Color color) {
        indent();
        buffer.appendln("Clear background of " + view + " to " + color + line());
    }

    public Canvas createSubcanvas() {
        buffer.blankLine();
        indent();
        buffer.appendln("Create subcanvas for same area");
        return new DebugCanvasAbsolute(buffer, level + 1, offsetX, offsetY);
    }

    public Canvas createSubcanvas(Bounds bounds) {
        return createSubcanvas(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    public Canvas createSubcanvas(int x, int y, int width, int height) {
//        buffer.blankLine();
        indent();
        int dx = offsetX + x;
        int qx = dx + width - 1;
        int dy = offsetY + y;
        int qy = dy + height - 1;
        buffer.appendln("Canvas " + dx + "," + dy + " " + width + "x" + height + " ("+ qx + "," + qy + ") " + line());
      // buffer.appendln(line());
        return new DebugCanvasAbsolute(buffer, level + 1, dx, dy);
    }

    public void draw3DRectangle(int x, int y, int width, int height, Color color, boolean raised) {
        indent();
        int px = offsetX + x;
        int py = offsetY + y;
        int qx = px + width - 1;
        int qy = py + height - 1;
        buffer.appendln("Rectangle (3D) " + px + "," + py + " " + width + "x" + height+  " (" + qx + "," + qy + ") " + line());
    }

    public void drawIcon(Image icon, int x, int y) {
        indent();
        int px = offsetX + x;
        int py = offsetY + y;
        int qx = px + icon.getWidth() - 1;
        int qy = py + icon.getHeight() - 1;
        buffer.appendln("Icon " + px + "," + py + " " + icon.getWidth() + "x" + icon.getHeight()+  " (" + qx + "," + qy + ") " + line());
    }

    public void drawIcon(Image icon, int x, int y, int width, int height) {
        indent();
        int px = offsetX + x;
        int py = offsetY + y;
        int qx = px + width - 1;
        int qy = py + height - 1;
        buffer.appendln("Icon " + px + "," + py + " " + width + "x" + height+  " (" + qx + "," + qy + ") " + line());
    }

    public void drawLine(int x, int y, int x2, int y2, Color color) {
        indent();
        int px = offsetX + x;
        int py = offsetY + y;
        int qx = offsetX + x2;
        int qy = offsetY + y2;
        buffer.appendln("Line from " + px + "," + py + " to " + qx + "," + qy + " " + color + line());
    }

    public void drawLine(Location start, int xExtent, int yExtent, Color color) {
        indent();
        buffer.appendln("Line from " + start.getX() + "," + start.getY() + " to " + (start.getX() + xExtent) + ","
                + (start.getY() + yExtent) + " " + color + line());
    }

    public void drawOval(int x, int y, int width, int height, Color color) {
        indent();
        int px = offsetX + x;
        int py = offsetY + y;
        buffer.appendln("Oval " + px + "," + py + " " + width + "x" + height + " " + color + line());
    }

    public void drawRectangle(int x, int y, int width, int height, Color color) {
        indent();
        int px = offsetX + x;
        int py = offsetY + y;
        int qx = px + width - 1;
        int qy = py + height - 1;
        
        buffer.appendln("Rectangle " + px + "," + py + " " + width + "x" + height + " (" + qx + "," + qy + ") " + color + line());
    }

    private String line() {
        RuntimeException e = new RuntimeException();
        StringWriter s;
        PrintWriter p = new PrintWriter(s = new StringWriter());
        e.printStackTrace(p);
        StringTokenizer st = new StringTokenizer(s.toString(), "\n\r");
        st.nextElement();
        st.nextElement();
        st.nextElement();
        String line = st.nextToken();
        return line.substring(line.indexOf('('));
    }

    public void drawRectangleAround(View view, Color color) {
        Bounds bounds = view.getBounds();
        indent();
        buffer.appendln("Rectangle 0,0 " + bounds.getWidth() + "x" + bounds.getHeight() + " " + color + line());
    }

    public void drawRoundedRectangle(int x, int y, int width, int height, int arcWidth, int arcHeight, Color color) {
        indent();
        int px = offsetX + x;
        int py = offsetY + y;
        int qx = px + width - 1;
        int qy = py + height - 1;
        buffer.appendln("Rounded Rectangle " + px + "," + py + " " + width + "x" + height  + " (" + qx + "," + qy + ") " + color + line());
    }

    public void drawShape(Shape shape, Color color) {
        indent();
        buffer.appendln("Shape " + shape + " " + color);
    }

    public void drawShape(Shape shape, int x, int y, Color color) {
        indent();
        int px = offsetX + x;
        int py = offsetY + y;
        buffer.appendln("Shape " + shape + " at " + px + "," + py + " (left, top)" + " " + color + line());
    }

    public void drawSolidOval(int x, int y, int width, int height, Color color) {
        indent();
        int px = offsetX + x;
        int py = offsetY + y;
        int qx = px + width - 1;
        int qy = py + height - 1;
        buffer.appendln("Oval (solid) " + px + "," + py + " " + width + "x" + height +  " (" + qx + "," + qy + ") " + color + line());
    }

    public void drawSolidRectangle(int x, int y, int width, int height, Color color) {
        indent();
        int px = offsetX + x;
        int py = offsetY + y;
        int qx = px + width - 1;
        int qy = py + height - 1;
        buffer.appendln("Rectangle (solid) " + px + "," + py + " " + width + "x" + height  + " (" + qx + "," + qy + ") " + color + line());
    }

    public void drawSolidShape(Shape shape, Color color) {
        indent();
        buffer.appendln("Shape (solid) " + shape + " " + color);
    }

    public void drawSolidShape(Shape shape, int x, int y, Color color) {
        indent();
        int px = offsetX + x;
        int py = offsetY + y;
        buffer.appendln("Shape (solid)" + shape + " at " + px + "," + py + " (left, top)" + " " + color + line());
    }

    public void drawText(String text, int x, int y, Color color, Text style) {
        indent();
        int px = offsetX + x;
        int py = offsetY + y;
        buffer.appendln("Text " + px + "," + py + " \"" + text + "\" " + color + line());
    }

    private void indent() {
        for (int i = 0; i < level; i++) {
            buffer.append("   ");
        }
    }

    public void offset(int x, int y) {
//        indent();
        offsetX += x;
        offsetY += y;
 //       buffer.appendln("Offset by " + x + "/" + y + " (left, top)");
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