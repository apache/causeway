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
package org.nakedobjects.viewer.skylark;

public abstract class CopyOfCanvas {
    private Bounds drawingArea;
    private Location origin;

    protected CopyOfCanvas() {
        origin = new Location();
        drawingArea = new Bounds();
    }

    protected void setDrawingArea(Bounds bounds) {
		this.drawingArea = bounds;
	}

	protected Bounds getDrawingArea() {
		return drawingArea;
	}

	protected Location getOrigin() {
		return origin;
	}

    public CopyOfCanvas createSubcanvas() {
        return createSubcanvas(drawingArea.x, drawingArea.y, drawingArea.width, drawingArea.height);
		
		// new Canvas(graphics.create(), drawingArea.x, drawingArea.y, drawingArea.width,
            // drawingArea.height);
    }

    public CopyOfCanvas createSubcanvas(Bounds bounds) {
        return createSubcanvas(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public CopyOfCanvas createSubcanvas(int x, int y, int width, int height) {
        CopyOfCanvas newCanvas = createCanvas(this); //buffer, level + 1, drawingArea);

        int existingWidth = drawingArea.getWidth() - x;
        int existingHeight = drawingArea.getHeight() - y;

        newCanvas.origin.move(x, y);
        newCanvas.drawingArea.translate(x, y);

        newCanvas.drawingArea.setWidth(Math.max(0, Math.min(existingWidth, width)));
        newCanvas.drawingArea.setHeight(Math.max(0, Math.min(existingHeight, height)));

        return newCanvas;
    }
    
    protected abstract CopyOfCanvas createCanvas(CopyOfCanvas source);

    public abstract void draw3DRectangle(int x, int y, int width, int height, boolean raised);

    public abstract void drawIcon(Image icon, int x, int y);

    public abstract void drawIcon(Image icon, int x, int y, int width, int height);

    public abstract void drawLine(int x, int y, int x2, int y2, Color color);

    public abstract void drawLine(Location start, int xExtent, int yExtent, Color color);

    public abstract void drawRectangle(int x, int y, int width, int height, Color color);

    public abstract void drawRectangle(Location at, Size size, Color color);

    public abstract void drawRectangle(Size size, Color color);

    public abstract void drawRoundedRectangle(int x, int y, int width, int height, int arcWidth,
        int arcHeight, Color color);

    public abstract void drawSolidOval(int x, int y, int width, int height, Color color);

    public abstract void drawSolidRectangle(int x, int y, int width, int height, Color color);

    public abstract void drawSolidRectangle(Location at, Size size, Color color);

    public abstract void drawSolidShape(Shape shape, int x, int y, Color color);

    public abstract void drawText(String text, int x, int y, Color color, Text style);

    public boolean intersects(Bounds view) {
        return drawingArea.intersects(view);
    }

    public void reduce(int left, int top, int right, int bottom) {
        origin.move(left, top);
        drawingArea.translate(left, top);
        drawingArea.contractHeight(top + bottom);
        drawingArea.contractWidth(left + right);
    }
}
