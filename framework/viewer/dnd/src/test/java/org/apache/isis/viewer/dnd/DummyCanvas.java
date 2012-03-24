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

package org.apache.isis.viewer.dnd;

import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.Image;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Shape;
import org.apache.isis.viewer.dnd.drawing.Text;

public class DummyCanvas implements Canvas {

    public DummyCanvas() {
        super();
    }

    @Override
    public Canvas createSubcanvas() {
        return null;
    }

    @Override
    public Canvas createSubcanvas(final Bounds bounds) {
        return null;
    }

    @Override
    public Canvas createSubcanvas(final int x, final int y, final int width, final int height) {
        return null;
    }

    @Override
    public void draw3DRectangle(final int x, final int y, final int width, final int height, final Color color, final boolean raised) {
    }

    @Override
    public void drawImage(final Image icon, final int x, final int y) {
    }

    @Override
    public void drawImage(final Image icon, final int x, final int y, final int width, final int height) {
    }

    @Override
    public void drawLine(final int x, final int y, final int x2, final int y2, final Color color) {
    }

    @Override
    public void drawLine(final Location start, final int xExtent, final int yExtent, final Color color) {
    }

    @Override
    public void drawOval(final int x, final int y, final int width, final int height, final Color color) {
    }

    @Override
    public void drawRectangle(final int x, final int y, final int width, final int height, final Color color) {
    }

    @Override
    public void drawRectangleAround(final Bounds bounds, final Color color) {
    }

    @Override
    public void drawRoundedRectangle(final int x, final int y, final int width, final int height, final int arcWidth, final int arcHeight, final Color color) {
    }

    @Override
    public void drawShape(final Shape shape, final Color color) {
    }

    @Override
    public void drawShape(final Shape shape, final int x, final int y, final Color color) {
    }

    @Override
    public void drawSolidOval(final int x, final int y, final int width, final int height, final Color color) {
    }

    @Override
    public void drawSolidRectangle(final int x, final int y, final int width, final int height, final Color color) {
    }

    @Override
    public void drawSolidShape(final Shape shape, final Color color) {
    }

    @Override
    public void drawSolidShape(final Shape shape, final int x, final int y, final Color color) {
    }

    @Override
    public void drawText(final String text, final int x, final int y, final Color color, final Text style) {
    }

    @Override
    public void drawText(final String text, final int x, final int y, final int maxWidth, final Color color, final Text style) {
    }

    @Override
    public void offset(final int x, final int y) {
    }

    @Override
    public boolean overlaps(final Bounds bounds) {
        return false;
    }

    @Override
    public void drawDebugOutline(final Bounds bounds, final int baseline, final Color color) {
    }

}
