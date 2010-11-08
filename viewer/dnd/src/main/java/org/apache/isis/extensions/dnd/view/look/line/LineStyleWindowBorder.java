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


package org.apache.isis.extensions.dnd.view.look.line;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.extensions.dnd.drawing.Canvas;
import org.apache.isis.extensions.dnd.drawing.Color;
import org.apache.isis.extensions.dnd.drawing.ColorsAndFonts;
import org.apache.isis.extensions.dnd.drawing.Location;
import org.apache.isis.extensions.dnd.drawing.Size;
import org.apache.isis.extensions.dnd.drawing.Text;
import org.apache.isis.extensions.dnd.view.Toolkit;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.ViewState;
import org.apache.isis.extensions.dnd.view.border.BorderDrawing;
import org.apache.isis.extensions.dnd.view.text.TextUtils;
import org.apache.isis.extensions.dnd.view.window.WindowControl;


public class LineStyleWindowBorder implements BorderDrawing {
    private final static Text TITLE_STYLE = Toolkit.getText(ColorsAndFonts.TEXT_TITLE_SMALL);
    private int titlebarHeight = Math.max(WindowControl.HEIGHT + View.VPADDING + TITLE_STYLE.getDescent(), TITLE_STYLE
            .getTextHeight());

    public void debugDetails(DebugString debug) {}

    public void draw(Canvas canvas, Size s, boolean hasFocus, ViewState state, View[] controls, String title) {
        final Color borderColor = hasFocus ? Toolkit.getColor(ColorsAndFonts.COLOR_BLACK) : Toolkit
                .getColor(ColorsAndFonts.COLOR_SECONDARY1);
        canvas.drawRectangle(0, 0, s.getWidth(), s.getHeight(), borderColor);
        int y = getTop();
        canvas.drawLine(0, y, s.getWidth(), y, borderColor);
        int controlWidth = View.HPADDING + (WindowControl.WIDTH + View.HPADDING) * controls.length;
        String text = TextUtils.limitText(title, TITLE_STYLE, s.getWidth() - controlWidth - View.VPADDING);
        canvas.drawText(text, 6, TITLE_STYLE.getLineHeight(), borderColor, Toolkit.getText(ColorsAndFonts.TEXT_TITLE_SMALL));
    }

    // TODO transiency should be flagged elsewhere and dealt with in the draw method.
    public void drawTransientMarker(Canvas canvas, Size size) {}

    public int getBottom() {
        return 1;
    }

    public int getLeft() {
        return 1;
    }

    public void getRequiredSize(Size size, String title, View[] controls) {
        final int width = getLeft() + View.HPADDING + TITLE_STYLE.stringWidth(title) + View.HPADDING + controls.length
                * (WindowControl.WIDTH + View.HPADDING) + View.HPADDING + getRight();
        size.ensureWidth(width);
    }

    public int getRight() {
        return 1;
    }

    public int getTop() {
        return titlebarHeight + 5;
    }

    public void layoutControls(Size size, View[] controls) {
        int x = size.getWidth() - 1 - (WindowControl.WIDTH + View.HPADDING) * controls.length;
        final int y = 2 + View.VPADDING;
        for (int i = 0; i < controls.length; i++) {
            controls[i].setSize(controls[i].getRequiredSize(new Size()));
            controls[i].setLocation(new Location(x, y));
            x += controls[i].getSize().getWidth() + View.HPADDING;
        }
    }

}

