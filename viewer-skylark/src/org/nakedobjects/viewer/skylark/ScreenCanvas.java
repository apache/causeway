package org.nakedobjects.viewer.skylark;

/*
public class ScreenCanvas extends Canvas {
    private java.awt.Color color;
    private Font font;
    private Graphics graphics;

    protected static ScreenCanvas create(Graphics graphics, Bounds bounds) {
    	ScreenCanvas canvas = new ScreenCanvas();
		canvas.graphics = graphics;
		canvas.setDrawingArea(bounds);
		
		return canvas;
     }

	protected Canvas createCanvas(Canvas source) {
		ScreenCanvas from = (ScreenCanvas) source;
		ScreenCanvas canvas = new ScreenCanvas();
		canvas.graphics = from.graphics.create();

		canvas.graphics.translate(getOrigin().getX(), getOrigin().getY());
		return canvas;
	}

    public void draw3DRectangle(int x, int y, int width, int height, boolean raised) {
        graphics.draw3DRect(x, y, width, height, raised);
    }

    public void drawSolidOval(int x, int y, int width, int height, Color color) {
        useColor(color);
        graphics.fillOval(x, y, width, height);
    }

    public void drawSolidRectangle(int x, int y, int width, int height, Color color) {
        useColor(color);
        graphics.fillRect(x, y, width, height);
    }

    public void drawSolidRectangle(Location at, Size size, Color color) {
        drawSolidRectangle(at.getX(), at.getY(), size.getWidth(), size.getHeight(), color);
    }

    public void drawIcon(Icon icon, int x, int y) {
        graphics.drawImage(icon.getAwtImage(), x, y, null);
    }

    public void drawIcon(Icon icon, int x, int y, int width, int height) {
        graphics.drawImage(icon.getAwtImage(), x, y, width, height, null);
    }

    public void drawLine(int x, int y, int x2, int y2, Color color) {
        useColor(color);
        graphics.drawLine(x, y, x2, y2);
    }

    public void drawLine(Location start, int xExtent, int yExtent, Color color) {
        drawLine(start.getX(), start.getY(), start.getX() + xExtent, start.getY() + yExtent, color);
    }

    public void drawRectangle(int x, int y, int width, int height, Color color) {
        useColor(color);
        graphics.drawRect(x, y, width, height);
    }

    public void drawRectangle(Location at, Size size, Color color) {
        drawRectangle(at.getX(), at.getY(), size.getWidth() - 1, size.getHeight() - 1, color);
    }

    public void drawRectangle(Size size, Color color) {
        drawRectangle(0, 0, size.getWidth() - 1, size.getHeight() - 1, color);
    }

    public void drawRoundedRectangle(int x, int y, int width, int height, int arcWidth,
        int arcHeight, Color color) {
        useColor(color);
        graphics.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

	public void drawSolidShape(Shape shape, int x, int y, Color color) {
		Shape copy = new Shape(shape);
		copy.translate(x, y);
		useColor(color);
		graphics.fillPolygon(copy.getX(), copy.getY(), copy.count());
	}

    public void drawText(String text, int x, int y, Color color, Style.Text style) {
        useColor(color);
        useFont(style);
        graphics.drawString(text, x, y);
    }

    private void useColor(Color color) {
        java.awt.Color awtColor = color.getAwtColor();

        if (this.color != awtColor) {
            this.color = awtColor;
            graphics.setColor(awtColor);
        }
    }

    private void useFont(Style.Text style) {
        Font font = style.getAwtFont();

        if (this.font != font) {
            this.font = font;
            graphics.setFont(font);
        }
    }

	public String toString() {
		return "Canvas [area=" + getDrawingArea() + ",color=" + color + ",font=" + font + "]";
	}

	
	public void reduce(int left, int top, int right, int bottom) {
		super.reduce(left, top, right, bottom);

		graphics.translate(left, top);
		Bounds b = getDrawingArea();
	    graphics.setClip(b.getX(), b.getY(), b.getWidth(), b.getHeight());
	}
	
	
	
	/*
	   public Canvas createSubcanvas(int x, int y, int width, int height) {
		Canvas newCanvas = new Canvas(graphics.create(), 0, 0, width, height);

		int existingWidth = drawingArea.getWidth() - x;
		int existingHeight = drawingArea.getHeight() - y;
		
		newCanvas.graphics.translate(x, y);
		newCanvas.drawingArea.translate(x, y);
			
		newCanvas.drawingArea.setWidth(Math.max(0, Math.min(existingWidth, width)));
		newCanvas.drawingArea.setHeight(Math.max(0, Math.min(existingHeight, height)));

	//	newCanvas.graphics.setClip(newCanvas.drawingArea.x, newCanvas.drawingArea.y, newCanvas.drawingArea.width, newCanvas.drawingArea.height);
		
	/*	int existingWidth = graphics.getClipBounds().width - x;
		int existingHeight = graphics.getClipBounds().height - y;
		
		newCanvas.graphics.translate(x, y);
			
		int w = Math.max(0, Math.min(existingWidth, width));
		int h = Math.max(0, Math.min(existingHeight, height));
		
		newCanvas.graphics.setClip(0,0, w, h);
* /
		return newCanvas;
    }
	   
		
	   /*
	   	public void reduce(int left, int top, int right, int bottom) {
	           graphics.translate(left, top);
	           int width = graphics.getClipBounds().width;
	           int height = graphics.getClipBounds().height;
	           graphics.setClip(0, 0, width - left - right, height - top - bottom);
	   	}
	   	*/


//}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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