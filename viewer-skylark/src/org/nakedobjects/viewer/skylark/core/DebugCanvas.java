package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Picture;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Shape;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Text;



public class DebugCanvas extends Canvas {
	private StringBuffer buffer;
	private int level;

	public DebugCanvas(StringBuffer buffer, Bounds bounds) {
		this(buffer, 0);
	}
	
	private DebugCanvas(StringBuffer buffer, int level) {
		super(null, 0, 0, 0, 0);
		this.level = level;
		this.buffer = buffer;
	}
	
	private void indent() {
		buffer.append("\n");
		for (int i = 0; i < level; i++) {
			buffer.append("   ");
		}
	}
	
	public void clearBackground(Color color) {
		indent();
		buffer.append("Clear area to " + color);
	}

    public Canvas createSubcanvas() {
		buffer.append("\n");
		indent();
		buffer.append("Create subcanvas for same area");
		return new DebugCanvas(buffer, level + 1);
    }

	public Canvas createSubcanvas(int x, int y, int width, int height) {
		buffer.append("\n");
		indent();
		buffer.append("Create subcanvas for area " + x + "," + y + " " + width + "x"  + height);
		return new DebugCanvas(buffer, level + 1);
	}
	
	public void draw3DRectangle(int x, int y, int width, int height, boolean raised) {
		indent();
		buffer.append("3D rectangle " + x + "," + y + " " + width + "x"  + height);
	}

	public void drawSolidOval(int x, int y, int width, int height, Color color) {
		indent();
		buffer.append("Oval (full) " + x + "," + y + " " + width + "x"  + height + " " + color);
	}

	public void drawSolidRectangle(int x, int y, int width, int height, Color color) {
		indent();
		buffer.append("Rectangle (full) " + x + "," + y + " " + width + "x"  + height + " " + color);
	}

	public void drawSolidRectangle(Location at, Size size, Color color) {
		indent();
		buffer.append("Rectangle (full) " + at.getX() + "," + at.getY() + " " + size.getWidth() + "x"  + size.getHeight() + " " + color);
	}

	public void drawIcon(Picture icon, int x, int y) {
		indent();
		buffer.append("Icon " + x + "," + y + " " + icon.getWidth()+ "x"  + icon.getHeight());
	}

	public void drawIcon(Picture icon, int x, int y, int width, int height) {
		indent();
		buffer.append("Icon " + x + "," + y + " " + width + "x"  + height);
	}

	public void drawLine(int x, int y, int x2, int y2, Color color) {
		indent();
		buffer.append("Line from " + x + "," + y + " to " + x2 + ","  + y2 + " " + color);
	}

	public void drawLine(Location start, int xExtent, int yExtent, Color color) {
		indent();
		buffer.append("Line from " + start.getX() + "," + start.getY() + " to " + (start.getX() + xExtent) + ","  + (start.getY() + yExtent) + " " + color);
	}

	public void drawRectangle(int x, int y, int width, int height, Color color) {
		indent();
		buffer.append("Rectangle " + x + "," + y + " " + width + "x"  + height + " " + color);
	}

	public void drawRectangle(Bounds bounds, Color color) {
		indent();
		buffer.append("Rectangle " + bounds.getX() + "," + bounds.getY() + " " + (bounds.getWidth() - 1) + "x"  + (bounds.getHeight() - 1) + " " + color);
	}

	public void drawRectangle(Location at, Size size, Color color) {
		indent();
		buffer.append("Rectangle " + at.getX() + "," + at.getY() + " " + size.getWidth() + "x"  + size.getHeight() + " " + color);
	}

	public void drawRectangle(Size size, Color color) {
		drawRectangle(0, 0, size.getWidth() - 1, size.getHeight() - 1, color);
		indent();
		buffer.append("Rectangle 0,0 " + size.getWidth() + "x"  + size.getHeight() + " " + color);
	}

	public void drawRoundedRectangle(int x, int y, int width, int height, int arcWidth,
		int arcHeight, Color color) {
		indent();
		buffer.append("Rounded Rectangle " + x + "," + y + " " + (x+width) + "x"  + (y+height) + " " + color);
	}

	public void drawRoundedRectangle(Bounds bounds, int arcWidth, int arcHeight, Color color) {
		drawRoundedRectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), arcWidth, arcHeight,
			color);
	}

	public void drawText(String text, int x, int y, Color color, Text style) {
		indent();
		buffer.append("Text " + x + "," + y + " \"" + text + "\" " + style + " " + color);
	}

	public void drawSolidShape(Shape shape, Color color) {
		indent();
		buffer.append("Shape (filled) " + shape + " " + color);
	}
	
	public void drawShape(Shape shape, Color color) {
		indent();
		buffer.append("Shape " + shape + " " + color);
	}
	
	public void reduce(int left, int top, int right, int bottom) {
		indent();
		buffer.append("Reduce by " + left + "/" + right + " " + top + "/" + bottom +  " (left/right top/bottom)");
	}	
}

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