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

import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.control.ButtonRender;

public class Button3DStyleRender implements ButtonRender {
    private static final int TEXT_PADDING = 12;
    private static final Text style = Toolkit.getText(ColorsAndFonts.TEXT_CONTROL);
    private final int buttonHeight;

    public Button3DStyleRender() {
        this.buttonHeight = 4 + style.getTextHeight() + 4;
    }

    @Override
    public void draw(final Canvas canvas, final Size size, final boolean isDisabled, final boolean isDefault, final boolean hasFocus, final boolean isOver, final boolean isPressed, final String text) {
        final int x = 0;
        final int y = 0;

        final int buttonWidth = TEXT_PADDING + style.stringWidth(text) + TEXT_PADDING;

        canvas.drawSolidRectangle(x, y, buttonWidth, buttonHeight, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3));

        final Color color = isDisabled ? Toolkit.getColor(ColorsAndFonts.COLOR_MENU_DISABLED) : Toolkit.getColor(ColorsAndFonts.COLOR_BLACK);
        final Color border = isDisabled ? Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3) : Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2);
        canvas.drawRectangle(x, y, buttonWidth, buttonHeight, isOver & !isDisabled ? Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY1) : Toolkit.getColor(ColorsAndFonts.COLOR_BLACK));
        canvas.draw3DRectangle(x + 1, y + 1, buttonWidth - 2, buttonHeight - 2, border, !isPressed);
        canvas.draw3DRectangle(x + 2, y + 2, buttonWidth - 4, buttonHeight - 4, border, !isPressed);
        if (isDefault) {
            canvas.drawRectangle(x + 3, y + 3, buttonWidth - 6, buttonHeight - 6, border);
        }
        if (hasFocus) {
            canvas.drawRectangle(x + 3, y + 3, buttonWidth - 6, buttonHeight - 6, Toolkit.getColor(ColorsAndFonts.COLOR_WHITE));
        }
        canvas.drawText(text, x + TEXT_PADDING, y + buttonHeight / 2 + style.getMidPoint(), color, style);
    }

    @Override
    public Size getMaximumSize(final String text) {
        final int buttonWidth = TEXT_PADDING + Toolkit.getText(ColorsAndFonts.TEXT_CONTROL).stringWidth(text) + TEXT_PADDING;
        return new Size(buttonWidth, buttonHeight);
    }
}
