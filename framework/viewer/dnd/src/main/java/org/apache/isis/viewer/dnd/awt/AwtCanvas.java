/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.viewer.dnd.awt;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import java.util.StringTokenizer;

import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Image;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Shape;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.base.AwtImage;

public class AwtCanvas implements Canvas {
    private java.awt.Color color;
    private Font font;
    private final Graphics graphics;
    private final RenderingArea renderingArea;

    private AwtCanvas(final Graphics graphics, final RenderingArea renderingArea) {
        this.graphics = graphics;
        this.renderingArea = renderingArea;
    }

    public AwtCanvas(final Graphics bufferGraphic, final RenderingArea renderingArea, final int x, final int y, final int width, final int height) {
        graphics = bufferGraphic;
        this.renderingArea = renderingArea;
        graphics.clipRect(x, y, width, height);
    }

    private Polygon createOval(final int x, final int y, final int width, final int height) {
        final int points = 40;
        final int xPoints[] = new int[points];
        final int yPoints[] = new int[points];
        double radians = 0.0;
        for (int i = 0; i < points; i++) {
            xPoints[i] = x + (int) (width / 2.0) + (int) (width / 2.0 * Math.cos(radians));
            yPoints[i] = y + (int) (height / 2.0) + (int) (height / 2.0 * Math.sin(radians));
            radians += (2.0 * Math.PI) / points;
        }
        final Polygon p = new Polygon(xPoints, yPoints, points);
        return p;
    }

    @Override
    public Canvas createSubcanvas() {
        return new AwtCanvas(graphics.create(), renderingArea);
    }

    @Override
    public Canvas createSubcanvas(final Bounds bounds) {
        return createSubcanvas(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    @Override
    public Canvas createSubcanvas(final int x, final int y, final int width, final int height) {
        final Graphics g = graphics.create();
        // this form of clipping must go here!
        g.translate(x, y);
        return new AwtCanvas(g, renderingArea, 0, 0, width, height);
    }

    @Override
    public void draw3DRectangle(final int x, final int y, final int width, final int height, final Color color, final boolean raised) {
        useColor(color);
        graphics.draw3DRect(x, y, width - 1, height - 1, raised);
    }

    @Override
    public void drawDebugOutline(final Bounds bounds, final int baseline, final Color color) {
        final int width = bounds.getWidth();
        final int height = bounds.getHeight();
        drawRectangle(bounds.getX(), bounds.getY(), width, height, color);
        final int midpoint = bounds.getY() + height / 2;
        drawLine(bounds.getX(), midpoint, width - 2, midpoint, color);
        if (baseline > 0) {
            drawLine(bounds.getX(), baseline, width - 1, baseline, Toolkit.getColor(ColorsAndFonts.COLOR_DEBUG_BASELINE));
        }
    }

    @Override
    public void drawImage(final Image image, final int x, final int y) {
        graphics.drawImage(((AwtImage) image).getAwtImage(), x, y, (ImageObserver) renderingArea);
    }

    @Override
    public void drawImage(final Image image, final int x, final int y, final int width, final int height) {
        graphics.drawImage(((AwtImage) image).getAwtImage(), x, y, width - 1, height - 1, (ImageObserver) renderingArea);
    }

    @Override
    public void drawLine(final int x, final int y, final int x2, final int y2, final Color color) {
        useColor(color);
        graphics.drawLine(x, y, x2, y2);
    }

    @Override
    public void drawLine(final Location start, final int xExtent, final int yExtent, final Color color) {
        drawLine(start.getX(), start.getY(), start.getX() + xExtent, start.getY() + yExtent, color);
    }

    @Override
    public void drawOval(final int x, final int y, final int width, final int height, final Color color) {
        useColor(color);
        final Polygon p = createOval(x, y, width - 1, height - 1);
        graphics.drawPolygon(p);
    }

    @Override
    public void drawRectangle(final int x, final int y, final int width, final int height, final Color color) {
        useColor(color);
        graphics.drawRect(x, y, width - 1, height - 1);
    }

    @Override
    public void drawRectangleAround(final Bounds bounds, final Color color) {
        drawRectangle(0, 0, bounds.getWidth(), bounds.getHeight(), color);
    }

    @Override
    public void drawRoundedRectangle(final int x, final int y, final int width, final int height, final int arcWidth, final int arcHeight, final Color color) {
        useColor(color);
        graphics.drawRoundRect(x, y, width - 1, height - 1, arcWidth, arcHeight);
    }

    @Override
    public void drawShape(final Shape shape, final Color color) {
        useColor(color);
        graphics.drawPolygon(shape.getX(), shape.getY(), shape.count());
    }

    @Override
    public void drawShape(final Shape shape, final int x, final int y, final Color color) {
        final Shape copy = new Shape(shape);
        copy.translate(x, y);
        drawShape(copy, color);
    }

    @Override
    public void drawSolidOval(final int x, final int y, final int width, final int height, final Color color) {
        useColor(color);
        final Polygon p = createOval(x, y, width, height);
        graphics.fillPolygon(p);
    }

    @Override
    public void drawSolidRectangle(final int x, final int y, final int width, final int height, final Color color) {
        useColor(color);
        graphics.fillRect(x, y, width, height);
    }

    @Override
    public void drawSolidShape(final Shape shape, final Color color) {
        useColor(color);
        graphics.fillPolygon(shape.getX(), shape.getY(), shape.count());
    }

    @Override
    public void drawSolidShape(final Shape shape, final int x, final int y, final Color color) {
        final Shape copy = new Shape(shape);
        copy.translate(x, y);
        drawSolidShape(copy, color);
    }

    @Override
    public void drawText(final String text, final int x, final int y, final Color color, final Text style) {
        useColor(color);
        useFont(style);
        graphics.drawString(text, x, y);
    }

    @Override
    public void drawText(final String text, final int x, final int y, final int maxWidth, final Color color, final Text style) {
        useColor(color);
        useFont(style);

        int top = y;
        final StringTokenizer lines = new StringTokenizer(text, "\n\r");
        while (lines.hasMoreTokens()) {
            final String line = lines.nextToken();
            final StringTokenizer words = new StringTokenizer(line, " ");
            final StringBuffer l = new StringBuffer();
            int width = 0;
            while (words.hasMoreTokens()) {
                final String nextWord = words.nextToken();
                final int wordWidth = style.stringWidth(nextWord);
                width += wordWidth;
                if (width >= maxWidth) {
                    graphics.drawString(l.toString(), x + (line.startsWith("\t") ? 20 : 00), top);
                    top += style.getLineHeight();
                    l.setLength(0);
                    width = wordWidth;
                }
                l.append(nextWord);
                l.append(" ");
                width += style.stringWidth(" ");
            }
            graphics.drawString(l.toString(), x + (line.startsWith("\t") ? 20 : 00), top);
            top += style.getLineHeight();
        }
    }

    @Override
    public void offset(final int x, final int y) {
        graphics.translate(x, y);
    }

    @Override
    public boolean overlaps(final Bounds bounds) {
        final Rectangle clip = graphics.getClipBounds();
        final Bounds activeArea = new Bounds(clip.x, clip.y, clip.width, clip.height);
        return bounds.intersects(activeArea);
    }

    @Override
    public String toString() {
        final Rectangle cb = graphics.getClipBounds();
        return "Canvas [area=" + cb.x + "," + cb.y + " " + cb.width + "x" + cb.height + ",color=" + color + ",font=" + font + "]";
    }

    private void useColor(final Color color) {
        final java.awt.Color awtColor = ((AwtColor) color).getAwtColor();

        if (this.color != awtColor) {
            this.color = awtColor;
            graphics.setColor(awtColor);
        }
    }

    private void useFont(final Text style) {
        final Font font = ((AwtText) style).getAwtFont();
        if (this.font != font) {
            this.font = font;
            graphics.setFont(font);
        }
    }
}
