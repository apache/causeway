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

package org.nakedobjects.viewer.skylark.core;



/*
public class CopyOfDebugCanvas extends Canvas {
	private StringBuffer buffer;
	private int level;

	protected static CopyOfDebugCanvas create(StringBuffer buffer, Bounds bounds) {
	   	CopyOfDebugCanvas canvas = new CopyOfDebugCanvas();
		canvas.buffer = buffer;
		canvas.level = 0;
		canvas.setDrawingArea(bounds);
			
		return canvas;
     }
	
	public Canvas createSubcanvas(int x, int y, int width, int height) {
		buffer.append("\n");
		indent();
		buffer.append("Create subcanvas for area " + x + "," + y + " " + width + "x"  + height);

		return super.createSubcanvas(x, y, width, height);
    }
    
//	private Bounds parentClip;
	/*
	
	private DebugCanvas(StringBuffer buffer, int level, Bounds bounds) {
		super(null, 0, 0, 0, 0);
		this.level = level;
		this.buffer = buffer;
//		this.parentClip = new Bounds(bounds);
		this.drawingArea = new Bounds(bounds);
		
		this.origin =  bounds.getLocation();
	}
	
	public void clearBackground(Color color) {
		indent();
		buffer.append("Clear area to " + color);
	}

    public Canvas createSubcanvas() {
		buffer.append("\n");
		indent();
		buffer.append("Create subcanvas for same area");
		return new DebugCanvas(buffer, level + 1, drawingArea);
    }

	public Canvas createSubcanvas(int x, int y, int width, int height) {
		buffer.append("\n");
		indent();
		buffer.append("Create subcanvas for area " + x + "," + y + " " + width + "x"  + height);
		
		DebugCanvas newCanvas = new DebugCanvas(buffer, level + 1, drawingArea);
		
		int existingWidth = drawingArea.getWidth() - x;
		int existingHeight = drawingArea.getHeight() - y;
		
		newCanvas.origin.translate(x, y);
		newCanvas.drawingArea.translate(x, y);
			
		newCanvas.drawingArea.setWidth(Math.max(0, Math.min(existingWidth, width)));
		newCanvas.drawingArea.setHeight(Math.max(0, Math.min(existingHeight, height)));
		
		/*	
		clip.setWidth(width);
		clip.setHeight(height);
		
		// intersection - check they intersect first
		x = Math.max(clip.getX(), parentClip.getX());
		y = Math.max(clip.getY(), parentClip.getY());
		int x2 = Math.min(width + clip.getX() , parentClip.getWidth() + parentClip.getX());
		int y2 = Math.min(height + clip.getY(), parentClip.getHeight() - parentClip.getY());
		
		clip.setX(x);
		clip.setY(y);
		clip.setWidth(x2 - x);
		clip.setHeight(y2 - y);
		* /
		
		
		if(! drawingArea.contains(newCanvas.drawingArea.getLocation())) {
			buffer.append("\n CANVAS OUTSIDE PARENT");
		}
		return newCanvas;
	}
	
	 * /
	
	protected Canvas createCanvas(Canvas source) {
		CopyOfDebugCanvas from = (CopyOfDebugCanvas) source;
		CopyOfDebugCanvas canvas = new CopyOfDebugCanvas();
		canvas.buffer = from.buffer;
		canvas.level = from.level + 1;
		return canvas;
	}
	
	public void draw3DRectangle(int x, int y, int width, int height, boolean raised) {
		indent();
		buffer.append("3D rectangle " + x + "," + y + " " + width + "x"  + height);
	}

	public void drawFullRectangle(Bounds bounds, Color color) {
		indent();
		buffer.append("Rectangle (full) " + bounds.getX() + "," + bounds.getY() + " " + (bounds.getWidth() - 1) + "x"  + (bounds.getHeight() - 1) + " " + color);
	}

	public void drawIcon(Icon icon, int x, int y) {
		indent();
		buffer.append("Icon " + x + "," + y + " " + icon.getWidth()+ "x"  + icon.getHeight());
	}

	public void drawIcon(Icon icon, int x, int y, int width, int height) {
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

	public void drawRectangle(Bounds bounds, Color color) {
		indent();
		buffer.append("Rectangle " + bounds.getX() + "," + bounds.getY() + " " + (bounds.getWidth() - 1) + "x"  + (bounds.getHeight() - 1) + " " + color);
	}

	public void drawRectangle(int x, int y, int width, int height, Color color) {
		indent();
		buffer.append("Rectangle " + x + "," + y + " " + width + "x"  + height + " " + color);
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

	public void drawRoundedRectangle(Bounds bounds, int arcWidth, int arcHeight, Color color) {
		drawRoundedRectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), arcWidth, arcHeight,
			color);
	}

	public void drawRoundedRectangle(int x, int y, int width, int height, int arcWidth,
		int arcHeight, Color color) {
		indent();
		buffer.append("Rounded Rectangle " + x + "," + y + " " + (x+width) + "x"  + (y+height) + " " + color);
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

	public void drawSolidShape(Shape shape, int x, int y, Color color) {
		indent();
		buffer.append("Shape " + shape + " " + color);
	}

	public void drawText(String text, int x, int y, Color color, Style.Text style) {
		indent();
		buffer.append("Text " + x + "," + y + " \"" + text + "\" " + style + " " + color);
	}
	
	private void indent() {
		buffer.append("\n");
		for (int i = 0; i < level; i++) {
			buffer.append("   ");
		}
		buffer.append("[");
		buffer.append(getOrigin());
		buffer.append("; ");
		buffer.append(getDrawingArea());
		buffer.append("] ");
	}
	
	public void reduce(int left, int top, int right, int bottom) {
		indent();
		buffer.append("Reduce by " + left + "/" + right + " " + top + "/" + bottom +  " (left/right top/bottom)");

		super.reduce(left, top, right, bottom);
	}
}
*/