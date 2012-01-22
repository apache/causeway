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

package org.apache.isis.viewer.dnd.view.border;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;

/**
 * A line border draws a simple box around a view of a given width and color.
 */
public class LineBorder extends AbstractBorder {
    private Color color;
    private final int arcRadius;
    private int width;
    private int padding;

    public LineBorder(final View wrappedView) {
        this(1, wrappedView);
    }

    public LineBorder(final int size, final View wrappedView) {
        this(size, 0, Toolkit.getColor(ColorsAndFonts.COLOR_BLACK), wrappedView);
    }

    public LineBorder(final int size, final int arcRadius, final View wrappedView) {
        this(size, arcRadius, Toolkit.getColor(ColorsAndFonts.COLOR_BLACK), wrappedView);
    }

    public LineBorder(final Color color, final View wrappedView) {
        this(1, 0, color, wrappedView);
    }

    public LineBorder(final int width, final Color color, final View wrappedView) {
        this(width, 0, color, wrappedView);
    }

    public LineBorder(final int width, final int arcRadius, final Color color, final View wrappedView) {
        super(wrappedView);
        setWidth(width);
        this.arcRadius = arcRadius;
        this.color = color;
    }

    @Override
    protected void debugDetails(final DebugBuilder debug) {
        debug.append("LineBorder " + top + " pixels\n");
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);
        final Size s = getSize();
        final int width = s.getWidth();
        for (int i = 0; i < left - padding; i++) {
            // canvas.drawRectangle(i, i, width - 2 * i, s.getHeight() - 2 * i,
            // color);
            canvas.drawRoundedRectangle(i, i, width - 2 * i, s.getHeight() - 2 * i, arcRadius, arcRadius, color);
        }
    }

    @Override
    public String toString() {
        return wrappedView.toString() + "/LineBorder";
    }

    public void setWidth(final int width) {
        this.width = width;
        calculateBorderWidth();
    }

    public void setPadding(final int padding) {
        this.padding = padding;
        calculateBorderWidth();
    }

    private void calculateBorderWidth() {
        top = width + padding;
        left = width + padding;
        bottom = width + padding;
        right = width + padding;
    }

    public void setColor(final Color color) {
        this.color = color;
    }

}
