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
package org.nakedobjects.viewer.lightweight;

import java.awt.Font;
import java.awt.Graphics;


public class Canvas {
    private Bounds activeArea;
    private java.awt.Color color;
    private Font font;
    private Graphics graphics;

    Canvas(Graphics bufferGraphic, int width, int height) {
        graphics = bufferGraphic;
        activeArea = new Bounds(0, 0, width, height);
    }

    public void clearBackground(Color color) {
        drawFullRectangle(activeArea, color);
    }

    public Canvas createSubcanvas(int x, int y, int width, int height) {
        Graphics g = graphics.create();
        g.translate(x, y);
        g.setClip(0, 0, width, height);

        return new Canvas(g, width, height);
    }

    public void draw3DRectangle(int x, int y, int width, int height, boolean raised) {
        graphics.draw3DRect(x, y, width, height, raised);
    }

    public void drawFullOval(int x, int y, int width, int height, Color color) {
        useColor(color);
        graphics.fillOval(x, y, width, height);
    }

    public void drawFullRectangle(int x, int y, int width, int height, Color color) {
        useColor(color);
        graphics.fillRect(x, y, width, height);
    }

    public void drawFullRectangle(Bounds bounds, Color color) {
        drawFullRectangle(bounds.x, bounds.y, bounds.width, bounds.height, color);
    }

    public void drawFullRectangle(Location at, Size size, Color color) {
        drawFullRectangle(at.getX(), at.getY(), size.getWidth(), size.getHeight(), color);
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

    public void drawRectangle(Bounds bounds, Color color) {
        drawRectangle(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1, color);
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

    public void drawRoundedRectangle(Bounds bounds, int arcWidth, int arcHeight, Color color) {
        drawRoundedRectangle(bounds.x, bounds.y, bounds.width, bounds.height, arcWidth, arcHeight,
            color);
    }

    public void drawText(String text, int x, int y, Color color, Style.Text style) {
        useColor(color);
        useFont(style);
        graphics.drawString(text, x, y);
    }

    public boolean intersects(Bounds view) {
        return activeArea.intersects(view);
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

	public void drawSolidShape(Shape shape, int x, int y, Color color) {
		Shape copy = new Shape(shape);
		copy.translate(x, y);
		useColor(color);
		graphics.fillPolygon(copy.getX(), copy.getY(), copy.count());
	}
}
