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


package org.apache.isis.extensions.dnd;

import org.apache.isis.extensions.dnd.drawing.Bounds;
import org.apache.isis.extensions.dnd.drawing.Canvas;
import org.apache.isis.extensions.dnd.drawing.Color;
import org.apache.isis.extensions.dnd.drawing.Image;
import org.apache.isis.extensions.dnd.drawing.Location;
import org.apache.isis.extensions.dnd.drawing.Shape;
import org.apache.isis.extensions.dnd.drawing.Text;


public class DummyCanvas implements Canvas {

    public DummyCanvas() {
        super();
    }

    public Canvas createSubcanvas() {
        return null;
    }

    public Canvas createSubcanvas(final Bounds bounds) {
        return null;
    }

    public Canvas createSubcanvas(final int x, final int y, final int width, final int height) {
        return null;
    }

    public void draw3DRectangle(
            final int x,
            final int y,
            final int width,
            final int height,
            final Color color,
            final boolean raised) {}

    public void drawImage(final Image icon, final int x, final int y) {}

    public void drawImage(final Image icon, final int x, final int y, final int width, final int height) {}

    public void drawLine(final int x, final int y, final int x2, final int y2, final Color color) {}

    public void drawLine(final Location start, final int xExtent, final int yExtent, final Color color) {}

    public void drawOval(final int x, final int y, final int width, final int height, final Color color) {}

    public void drawRectangle(final int x, final int y, final int width, final int height, final Color color) {}

    public void drawRectangleAround(final Bounds bounds, final Color color) {}

    public void drawRoundedRectangle(
            final int x,
            final int y,
            final int width,
            final int height,
            final int arcWidth,
            final int arcHeight,
            final Color color) {}

    public void drawShape(final Shape shape, final Color color) {}

    public void drawShape(final Shape shape, final int x, final int y, final Color color) {}

    public void drawSolidOval(final int x, final int y, final int width, final int height, final Color color) {}

    public void drawSolidRectangle(final int x, final int y, final int width, final int height, final Color color) {}

    public void drawSolidShape(final Shape shape, final Color color) {}

    public void drawSolidShape(final Shape shape, final int x, final int y, final Color color) {}

    public void drawText(final String text, final int x, final int y, final Color color, final Text style) {}

    public void drawText(final String text, final int x, final int y, final int maxWidth, final Color color, final Text style) {}

    public void offset(final int x, final int y) {}

    public boolean overlaps(final Bounds bounds) {
        return false;
    }

    public void drawDebugOutline(final Bounds bounds, final int baseline, final Color color) {}

}
