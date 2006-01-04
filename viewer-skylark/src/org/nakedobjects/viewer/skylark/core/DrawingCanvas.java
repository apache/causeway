package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Image;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Shape;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;


public class DrawingCanvas implements Canvas {
    private java.awt.Color color;
    private Font font;
    private Graphics graphics;

    private DrawingCanvas(Graphics graphics) {
        this.graphics = graphics;
    }

    public DrawingCanvas(Graphics bufferGraphic, int x, int y, int width, int height) {
        graphics = bufferGraphic;
        graphics.clipRect(x, y, width, height);
    }

    public Canvas createSubcanvas() {
        return new DrawingCanvas(graphics.create());
    }

    public Canvas createSubcanvas(Bounds bounds) {
        return createSubcanvas(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    public Canvas createSubcanvas(int x, int y, int width, int height) {
        Graphics g = graphics.create();
        // this form of clipping must go here!
        g.translate(x, y);
        return new DrawingCanvas(g, 0, 0, width, height);
    }

    public void draw3DRectangle(int x, int y, int width, int height, boolean raised) {
        graphics.draw3DRect(x, y, width, height, raised);
    }

    public void clearBackground(View view, Color color) {
        Bounds bounds = view.getBounds();
        drawSolidRectangle(0, 0, bounds.getWidth(), bounds.getHeight(), color);
    }

    public void drawIcon(Image icon, int x, int y) {
        graphics.drawImage(((AwtImage) icon).getAwtImage(), x, y, null);
    }

    public void drawIcon(Image icon, int x, int y, int width, int height) {
        graphics.drawImage(((AwtImage) icon).getAwtImage(), x, y, width, height, null);
    }

    public void drawLine(int x, int y, int x2, int y2, Color color) {
        useColor(color);
        graphics.drawLine(x, y, x2, y2);
    }

    public void drawLine(Location start, int xExtent, int yExtent, Color color) {
        drawLine(start.getX(), start.getY(), start.getX() + xExtent, start.getY() + yExtent, color);
    }

    public void drawOval(int x, int y, int width, int height, Color color) {
        useColor(color);
        
        int points = 50;
        int xPoints[] = new int[points];
        int yPoints[] = new int[points];
        double radians = 0.0;
        for (int i = 0; i <points; i++) {
            xPoints[i] = x + width / 2  + (int) (width /2 * Math.cos(radians));
            yPoints[i] = y + height / 2  + (int) (height / 2 * Math.sin(radians));
            radians += (2.0 * Math.PI) / points;
        }
        graphics.drawPolygon(xPoints, yPoints, points);
     //   graphics.drawOval(x, y, width, height);
    }
    
    public void drawRectangle(int x, int y, int width, int height, Color color) {
        useColor(color);
        graphics.drawRect(x, y, width, height);
    }

    public void drawRectangleAround(View view, Color color) {
        Bounds bounds = view.getBounds();
        drawRectangle(0, 0, bounds.getWidth() - 1, bounds.getHeight() - 1, color);
    }

    public void drawRoundedRectangle(int x, int y, int width, int height, int arcWidth, int arcHeight, Color color) {
        useColor(color);
        graphics.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    public void drawShape(Shape shape, Color color) {
        useColor(color);
        graphics.drawPolygon(shape.getX(), shape.getY(), shape.count());
    }

    public void drawShape(Shape shape, int x, int y, Color color) {
        Shape copy = new Shape(shape);
        copy.translate(x, y);
        drawShape(copy, color);
    }

    public void drawSolidOval(int x, int y, int width, int height, Color color) {
        useColor(color);
        
        int points = 50;
        int xPoints[] = new int[points];
        int yPoints[] = new int[points];
        double radians = 0.0;
        for (int i = 0; i <points; i++) {
            xPoints[i] = x + width / 2  + (int) (width /2 * Math.cos(radians));
            yPoints[i] = y + height / 2  + (int) (height / 2 * Math.sin(radians));
            radians += (2.0 * Math.PI) / points;
        }
        graphics.fillPolygon(xPoints, yPoints, points);

//        graphics.fillOval(x, y, width, height);
    }

    public void drawSolidRectangle(int x, int y, int width, int height, Color color) {
        useColor(color);
        graphics.fillRect(x, y, width, height);
    }

    public void drawSolidShape(Shape shape, Color color) {
        useColor(color);
        graphics.fillPolygon(shape.getX(), shape.getY(), shape.count());
    }

    public void drawSolidShape(Shape shape, int x, int y, Color color) {
        Shape copy = new Shape(shape);
        copy.translate(x, y);
        drawSolidShape(copy, color);
    }

    public void drawText(String text, int x, int y, Color color, Text style) {
        useColor(color);
        useFont(style);
        graphics.drawString(text, x, y + 1);
    }

    public void offset(int x, int y) {
        graphics.translate(x, y);
    }

    public boolean overlaps(Bounds bounds) {
        Rectangle clip = graphics.getClipBounds();
        Bounds activeArea = new Bounds(clip.x, clip.y, clip.width, clip.height);
        return bounds.intersects(activeArea);
    }

    public String toString() {
        Rectangle cb = graphics.getClipBounds();
        return "Canvas [area=" + cb.x + "," + cb.y + " " + cb.width + "x" + cb.height + ",color=" + color + ",font=" + font + "]";
    }

    private void useColor(Color color) {
        java.awt.Color awtColor = color.getAwtColor();

        if (this.color != awtColor) {
            this.color = awtColor;
            graphics.setColor(awtColor);
        }
    }

    private void useFont(Text style) {
        Font font = style.getAwtFont();

        if (this.font != font) {
            this.font = font;
            graphics.setFont(font);
        }
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */
