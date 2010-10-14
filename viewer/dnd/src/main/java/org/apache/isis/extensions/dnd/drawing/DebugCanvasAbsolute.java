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


package org.apache.isis.extensions.dnd.drawing;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.StringTokenizer;

import org.apache.isis.commons.debug.DebugString;


public class DebugCanvasAbsolute implements Canvas {
    private final DebugString buffer;
    private final int level;
    private int offsetX;
    private int offsetY;

    public DebugCanvasAbsolute(final DebugString buffer, final Bounds bounds) {
        this(buffer, 0, bounds.getX(), bounds.getY());
    }

    private DebugCanvasAbsolute(final DebugString buffer, final int level, final int x, final int y) {
        this.level = level;
        this.buffer = buffer;
        offsetX = x;
        offsetY = y;
    }

    public Canvas createSubcanvas() {
        buffer.blankLine();
        indent();
        buffer.appendln("Create subcanvas for same area");
        return new DebugCanvasAbsolute(buffer, level + 1, offsetX, offsetY);
    }

    public Canvas createSubcanvas(final Bounds bounds) {
        return createSubcanvas(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    public Canvas createSubcanvas(final int x, final int y, final int width, final int height) {
        // buffer.blankLine();
        indent();
        final int dx = offsetX + x;
        final int qx = dx + width - 1;
        final int dy = offsetY + y;
        final int qy = dy + height - 1;
        buffer.appendln("Canvas " + dx + "," + dy + " " + width + "x" + height + " (" + qx + "," + qy + ") " + line());
        // buffer.appendln(line());
        return new DebugCanvasAbsolute(buffer, level + 1, dx, dy);
    }

    public void draw3DRectangle(
            final int x,
            final int y,
            final int width,
            final int height,
            final Color color,
            final boolean raised) {
        indent();
        final int px = offsetX + x;
        final int py = offsetY + y;
        final int qx = px + width - 1;
        final int qy = py + height - 1;
        buffer.appendln("Rectangle (3D) " + px + "," + py + " " + width + "x" + height + " (" + qx + "," + qy + ") " + line());
    }

    public void drawImage(final Image image, final int x, final int y) {
        indent();
        final int px = offsetX + x;
        final int py = offsetY + y;
        final int qx = px + image.getWidth() - 1;
        final int qy = py + image.getHeight() - 1;
        buffer.appendln("Icon " + px + "," + py + " " + image.getWidth() + "x" + image.getHeight() + " (" + qx + "," + qy + ") "
                + line());
    }

    public void drawImage(final Image image, final int x, final int y, final int width, final int height) {
        indent();
        final int px = offsetX + x;
        final int py = offsetY + y;
        final int qx = px + width - 1;
        final int qy = py + height - 1;
        buffer.appendln("Icon " + px + "," + py + " " + width + "x" + height + " (" + qx + "," + qy + ") " + line());
    }

    public void drawLine(final int x, final int y, final int x2, final int y2, final Color color) {
        indent();
        final int px = offsetX + x;
        final int py = offsetY + y;
        final int qx = offsetX + x2;
        final int qy = offsetY + y2;
        buffer.appendln("Line from " + px + "," + py + " to " + qx + "," + qy + " " + color + line());
    }

    public void drawLine(final Location start, final int xExtent, final int yExtent, final Color color) {
        indent();
        buffer.appendln("Line from " + start.getX() + "," + start.getY() + " to " + (start.getX() + xExtent) + ","
                + (start.getY() + yExtent) + " " + color + line());
    }

    public void drawOval(final int x, final int y, final int width, final int height, final Color color) {
        indent();
        final int px = offsetX + x;
        final int py = offsetY + y;
        buffer.appendln("Oval " + px + "," + py + " " + width + "x" + height + " " + color + line());
    }

    public void drawRectangle(final int x, final int y, final int width, final int height, final Color color) {
        indent();
        final int px = offsetX + x;
        final int py = offsetY + y;
        final int qx = px + width - 1;
        final int qy = py + height - 1;

        buffer.appendln("Rectangle " + px + "," + py + " " + width + "x" + height + " (" + qx + "," + qy + ") " + color + line());
    }

    private String line() {
        final RuntimeException e = new RuntimeException();
        StringWriter s;
        final PrintWriter p = new PrintWriter(s = new StringWriter());
        e.printStackTrace(p);
        final StringTokenizer st = new StringTokenizer(s.toString(), "\n\r");
        st.nextElement();
        st.nextElement();
        st.nextElement();
        final String line = st.nextToken();
        return line.substring(line.indexOf('('));
    }

    public void drawRectangleAround(final Bounds bounds, final Color color) {
        indent();
        buffer.appendln("Rectangle 0,0 " + bounds.getWidth() + "x" + bounds.getHeight() + " " + color + line());
    }

    public void drawRoundedRectangle(
            final int x,
            final int y,
            final int width,
            final int height,
            final int arcWidth,
            final int arcHeight,
            final Color color) {
        indent();
        final int px = offsetX + x;
        final int py = offsetY + y;
        final int qx = px + width - 1;
        final int qy = py + height - 1;
        buffer.appendln("Rounded Rectangle " + px + "," + py + " " + width + "x" + height + " (" + qx + "," + qy + ") " + color
                + line());
    }

    public void drawShape(final Shape shape, final Color color) {
        indent();
        buffer.appendln("Shape " + shape + " " + color);
    }

    public void drawShape(final Shape shape, final int x, final int y, final Color color) {
        indent();
        final int px = offsetX + x;
        final int py = offsetY + y;
        buffer.appendln("Shape " + shape + " at " + px + "," + py + " (left, top)" + " " + color + line());
    }

    public void drawSolidOval(final int x, final int y, final int width, final int height, final Color color) {
        indent();
        final int px = offsetX + x;
        final int py = offsetY + y;
        final int qx = px + width - 1;
        final int qy = py + height - 1;
        buffer.appendln("Oval (solid) " + px + "," + py + " " + width + "x" + height + " (" + qx + "," + qy + ") " + color
                + line());
    }

    public void drawSolidRectangle(final int x, final int y, final int width, final int height, final Color color) {
        indent();
        final int px = offsetX + x;
        final int py = offsetY + y;
        final int qx = px + width - 1;
        final int qy = py + height - 1;
        buffer.appendln("Rectangle (solid) " + px + "," + py + " " + width + "x" + height + " (" + qx + "," + qy + ") " + color
                + line());
    }

    public void drawSolidShape(final Shape shape, final Color color) {
        indent();
        buffer.appendln("Shape (solid) " + shape + " " + color);
    }

    public void drawSolidShape(final Shape shape, final int x, final int y, final Color color) {
        indent();
        final int px = offsetX + x;
        final int py = offsetY + y;
        buffer.appendln("Shape (solid)" + shape + " at " + px + "," + py + " (left, top)" + " " + color + line());
    }

    public void drawText(final String text, final int x, final int y, final Color color, final Text style) {
        indent();
        final int px = offsetX + x;
        final int py = offsetY + y;
        buffer.appendln("Text " + px + "," + py + " \"" + text + "\" " + color + line());
    }

    public void drawText(final String text, final int x, final int y, final int maxWidth, final Color color, final Text style) {
        indent();
        final int px = offsetX + x;
        final int py = offsetY + y;
        buffer.appendln("Text " + px + "," + py + " +" + maxWidth + "xh \"" + text + "\" " + color + line());
    }

    private void indent() {
        for (int i = 0; i < level; i++) {
            buffer.append("   ");
        }
    }

    public void offset(final int x, final int y) {
        // indent();
        offsetX += x;
        offsetY += y;
        // buffer.appendln("Offset by " + x + "/" + y + " (left, top)");
    }

    public boolean overlaps(final Bounds bounds) {
        return true;
    }

    public String toString() {
        return "Canvas";
    }

    public void drawDebugOutline(final Bounds bounds, final int baseline, final Color color) {}

}
