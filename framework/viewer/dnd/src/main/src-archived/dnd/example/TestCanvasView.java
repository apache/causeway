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


package org.apache.isis.viewer.dnd.example;

import org.apache.isis.extensions.dndviewer.ColorsAndFonts;
import org.apache.isis.viewer.dnd.Canvas;
import org.apache.isis.viewer.dnd.Click;
import org.apache.isis.viewer.dnd.Drag;
import org.apache.isis.viewer.dnd.DragStart;
import org.apache.isis.viewer.dnd.Toolkit;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.view.simple.AbstractView;


class TestCanvasView extends AbstractView {
    Color white = Toolkit.getColor(0xffffff);
    Color black = Toolkit.getColor(0);
    Color gray = Toolkit.getColor(0xaaaaaa);
    Color lightGray = Toolkit.getColor(0xdddddd);
    Color red = Toolkit.getColor(0xff0000);
    Color green = Toolkit.getColor(0x00ff00);
    Color blue = Toolkit.getColor(0x0000ff);
    Color yellow = Toolkit.getColor(0xff00ff);

    public void draw(final Canvas canvas) {
        canvas.clearBackground(this, white);

        int canvasWidth = getSize().getWidth();
        int canvasHeight = getSize().getHeight();

        canvas.drawRectangle(0, 0, canvasWidth, canvasHeight, black);

        int x = 10;
        int y = 10;
        int width = 50;
        int height = 90;
        // outline shapes
        canvas.drawRectangle(x, y, width, height, gray);
        canvas.drawRoundedRectangle(x, y, width, height, 20, 20, black);
        canvas.drawOval(x, y, width, height, green);
        canvas.drawLine(x, y, x + width - 1, y + height - 1, red);
        canvas.drawLine(x, y + height - 1, x + width - 1, y, red);

        // subcanvas
        x = 80;
        canvas.drawRectangle(x, y, width, height, gray);

        Canvas subcanvas = canvas.createSubcanvas(x + 1, y + 1, width - 1, height - 1);
        subcanvas.drawRectangle(0, 0, width - 2, height - 2, blue);

        x = 150;
        canvas.drawRectangle(x, y, width, height, gray);

        subcanvas = canvas.createSubcanvas(x + 1, y + 1, width - 1, height - 1);
        subcanvas.offset(-100, -200);

        subcanvas.drawRectangle(100, 200, width - 2, height - 2, red);
        subcanvas.drawRectangle(0, 0, 120, 220, green);

        // solid shapes
        x = 10;
        y = 105;

        canvas.drawRectangle(x - 1, y - 1, width + 2, height + 2, gray);
        canvas.drawSolidRectangle(x, y, width, height, black);
        canvas.drawSolidOval(x, y, width, height, green);
        canvas.drawLine(x, y, x + width - 1, y + height - 1, red);
        canvas.drawLine(x, y + height - 1, x + width - 1, y, red);

        x = 80;
        canvas.drawSolidRectangle(x, y, width, height, black);

        subcanvas = canvas.createSubcanvas(x + 1, y + 1, width - 1, height - 1);
        subcanvas.drawSolidRectangle(0, 0, width - 2, height - 2, blue);

        x = 150;
        canvas.drawRectangle(x, y, width, width, black);
        canvas.drawOval(x, y, width, width, green);

        // 3D rectangles
        canvas.drawRectangle(x, y + 10 + width, 20, 20, black);
        canvas.draw3DRectangle(x, y + 10 + width, 20, 20, gray, true);

        canvas.drawRectangle(x + 30, y + 10 + width, 20, 20, black);
        canvas.draw3DRectangle(x + 30, y + 10 + width, 20, 20, gray, true);

        x = 10;
        y = 240;

        int ascent = Toolkit.getText(ColorsAndFonts.TEXT_NORMAL).getAscent();
        int descent = Toolkit.getText(ColorsAndFonts.TEXT_NORMAL).getDescent();
        int midpoint = Toolkit.getText(ColorsAndFonts.TEXT_NORMAL).getMidPoint();
        int lineHeight = Toolkit.getText(ColorsAndFonts.TEXT_NORMAL).getLineHeight();

        // canvas.drawRectangle(left, top, 200, line, gray);
        int baseline = y + ascent;
        drawText(canvas, x, lineHeight, baseline, ascent, descent, midpoint);
        baseline += lineHeight;
        drawText(canvas, x, lineHeight, baseline, ascent, descent, midpoint);
        baseline += lineHeight;
        drawText(canvas, x, lineHeight, baseline, ascent, descent, midpoint);

        /*
         * int width = getSize().getWidth(); int height = getSize().getHeight(); canvas.drawRectangle(0,0,
         * width - 1, height - 1, gray); canvas.drawLine(0, 0, width - 1, height - 1, red);
         * canvas.drawLine(width - 1, 0, 0, height - 1, red);
         */
    }

    private void drawText(
            final Canvas canvas,
            final int x,
            final int lineHeight,
            final int baseline,
            final int ascent,
            final int descent,
            final int midpoint) {
        canvas.drawLine(x, baseline, x + 200 - 1, baseline, gray); // baseline
        // canvas.drawLine(x, baseline - (ascent - descent) / 2, x + 200 - 1, baseline - (ascent - descent) /
        // 2, red); // mid-point
        canvas.drawLine(x, baseline - midpoint, x + 200 - 1, baseline - midpoint, red); // mid-point
        canvas.drawLine(x, baseline - ascent, x + 200 - 1, baseline - ascent, lightGray); // ascent
        // canvas.drawLine(x, baseline - ascent + descent, x + 200 - 1, baseline - ascent + descent,
        // lightGray); // ascent
        canvas.drawLine(x, baseline + descent, x + 200 - 1, baseline + descent, yellow); // descent
        canvas.drawText("12345 abcdefghijk ABCDEFG", x, baseline, black, Toolkit.getText(ColorsAndFonts.TEXT_NORMAL));
    }

    public void firstClick(final Click click) {
        debug("first click " + click);
        super.firstClick(click);
    }

    public void secondClick(final Click click) {
        debug("second click " + click);
        super.secondClick(click);
    }

    public void mouseMoved(final Location location) {
        debug("mouse moved " + location);
        super.mouseMoved(location);
    }

    private void debug(final String str) {
        getViewManager().getSpy().addAction(str);
    }

    public Drag dragStart(final DragStart drag) {
        debug("drag start " + drag);
        return super.dragStart(drag);
    }
}
