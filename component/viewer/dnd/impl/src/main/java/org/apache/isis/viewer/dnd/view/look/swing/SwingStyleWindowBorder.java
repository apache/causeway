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

package org.apache.isis.viewer.dnd.view.look.swing;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.DrawingUtil;
import org.apache.isis.viewer.dnd.drawing.Image;
import org.apache.isis.viewer.dnd.drawing.ImageFactory;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewConstants;
import org.apache.isis.viewer.dnd.view.ViewState;
import org.apache.isis.viewer.dnd.view.border.BorderDrawing;
import org.apache.isis.viewer.dnd.view.text.TextUtils;
import org.apache.isis.viewer.dnd.view.window.WindowControl;

public class SwingStyleWindowBorder implements BorderDrawing {
    final protected static int LINE_THICKNESS = 5;
    private final static Text TITLE_STYLE = Toolkit.getText(ColorsAndFonts.TEXT_TITLE_SMALL);

    int titlebarHeight = Math.max(WindowControl.HEIGHT + ViewConstants.VPADDING + TITLE_STYLE.getDescent(), TITLE_STYLE.getTextHeight());
    int baseline = LINE_THICKNESS + WindowControl.HEIGHT;
    int left = LINE_THICKNESS;
    int right = LINE_THICKNESS;
    int top = LINE_THICKNESS + titlebarHeight;
    int bottom = LINE_THICKNESS;

    @Override
    public void debugDetails(final DebugBuilder debug) {
        debug.appendln("titlebar ", top - titlebarHeight);
    }

    @Override
    public void layoutControls(final Size size, final View[] controls) {
        int x = size.getWidth() - right - (WindowControl.WIDTH + ViewConstants.HPADDING) * controls.length;
        final int y = LINE_THICKNESS + ViewConstants.VPADDING;

        for (final View control : controls) {
            control.setSize(control.getRequiredSize(new Size()));
            control.setLocation(new Location(x, y));
            x += control.getSize().getWidth() + ViewConstants.HPADDING;
        }
    }

    @Override
    public void draw(final Canvas canvas, final Size s, final boolean hasFocus, final ViewState state, final View[] controls, final String title) {
        final int x = left;
        final int width = s.getWidth();
        final int height = s.getHeight();

        final Color titleBarBackgroundColor = hasFocus ? Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY3) : Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3);
        final Color titleBarTextColor = hasFocus ? Toolkit.getColor(ColorsAndFonts.COLOR_BLACK) : Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY1);
        final Color borderColor = hasFocus ? Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY1) : Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY1);
        final Color insetColorLight = hasFocus ? Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY2) : Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2);
        final Color insetColorDark = hasFocus ? Toolkit.getColor(ColorsAndFonts.COLOR_BLACK) : Toolkit.getColor(ColorsAndFonts.COLOR_BLACK);

        // slightly rounded grey border
        canvas.drawRectangle(1, 0, width - 2, height, borderColor);
        canvas.drawRectangle(0, 1, width, height - 2, borderColor);

        for (int i = 2; i < left; i++) {
            canvas.drawRectangle(i, i, width - 2 * i, height - 2 * i, borderColor);
        }

        if (state.isActive()) {
            // final int i = left;
            // canvas.drawRectangle(i, top, width - 2 * i, height - 2 * i - top,
            // Toolkit.getColor(ColorsAndFonts.COLOR_ACTIVE));

            final Image busyImage = ImageFactory.getInstance().loadIcon("busy", 16, null);
            canvas.drawImage(busyImage, width - right - 16 - 4, top + 4);
        }

        // vertical lines within border
        canvas.drawLine(2, 15, 2, height - 15, insetColorDark);
        canvas.drawLine(3, 16, 3, height - 14, insetColorLight);
        canvas.drawLine(width - 3, 15, width - 3, height - 15, insetColorDark);
        canvas.drawLine(width - 2, 16, width - 2, height - 14, insetColorLight);

        // horizontal lines within border
        canvas.drawLine(15, 2, width - 15, 2, insetColorDark);
        canvas.drawLine(16, 3, width - 14, 3, insetColorLight);
        canvas.drawLine(15, height - 3, width - 15, height - 3, insetColorDark);
        canvas.drawLine(16, height - 2, width - 14, height - 2, insetColorLight);

        // title bar
        canvas.drawSolidRectangle(left, LINE_THICKNESS, width - left - right, titlebarHeight, titleBarBackgroundColor);
        final int y = LINE_THICKNESS + titlebarHeight - 1;
        canvas.drawLine(x, y, width - right - 1, y, borderColor);

        final int controlWidth = ViewConstants.HPADDING + (WindowControl.WIDTH + ViewConstants.HPADDING) * controls.length;
        final String text = TextUtils.limitText(title, TITLE_STYLE, width - controlWidth - LINE_THICKNESS * 2 - 2);
        canvas.drawText(text, x + ViewConstants.HPADDING, baseline, titleBarTextColor, TITLE_STYLE);

        final Color white = Toolkit.getColor(ColorsAndFonts.COLOR_WHITE);
        final int hatchX = ViewConstants.HPADDING + TITLE_STYLE.stringWidth(title) + 10;
        final int hatchWidth = controls[0].getLocation().getX() - hatchX - 10;
        final int hatchY = LINE_THICKNESS + 2;
        final int hatchHeight = titlebarHeight - 6;
        DrawingUtil.drawHatching(canvas, hatchX, hatchY, hatchWidth, hatchHeight, borderColor, white);

    }

    @Override
    public void drawTransientMarker(final Canvas canvas, final Size size) {
        final int height = top - LINE_THICKNESS - 2;
        final int x = size.getWidth() - 50;
        final Image icon = ImageFactory.getInstance().loadIcon("transient", height, null);
        if (icon == null) {
            canvas.drawText("*", x, baseline, Toolkit.getColor(ColorsAndFonts.COLOR_BLACK), Toolkit.getText(ColorsAndFonts.TEXT_NORMAL));
        } else {
            canvas.drawImage(icon, x, LINE_THICKNESS + 1, height, height);
        }

    }

    @Override
    public void getRequiredSize(final Size size, final String title, final View[] controls) {
        final int width = left + ViewConstants.HPADDING + TITLE_STYLE.stringWidth(title) + ViewConstants.HPADDING + controls.length * (WindowControl.WIDTH + ViewConstants.HPADDING) + ViewConstants.HPADDING + right;
        // size.extendWidth(getLeft() + getRight());
        size.ensureWidth(width);
    }

    @Override
    public int getLeft() {
        return left;
    }

    @Override
    public int getRight() {
        return right;
    }

    @Override
    public int getTop() {
        return top;
    }

    @Override
    public int getBottom() {
        return bottom;
    }

}
