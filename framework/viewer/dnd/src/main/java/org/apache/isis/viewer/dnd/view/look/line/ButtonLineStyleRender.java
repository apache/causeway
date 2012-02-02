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

package org.apache.isis.viewer.dnd.view.look.line;

import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.control.ButtonRender;

public class ButtonLineStyleRender implements ButtonRender {
    private static final int TEXT_PADDING = 12;
    private static final Text style = Toolkit.getText(ColorsAndFonts.TEXT_CONTROL);
    private final int buttonHeight;

    public ButtonLineStyleRender() {
        this.buttonHeight = 2 + style.getTextHeight() + 2;
    }

    @Override
    public void draw(final Canvas canvas, final Size size, final boolean isDisabled, final boolean isDefault, final boolean hasFocus, final boolean isOver, final boolean isPressed, final String text) {
        final int buttonWidth = TEXT_PADDING + style.stringWidth(text) + TEXT_PADDING;

        final Color borderColor;
        Color textColor = Toolkit.getColor(ColorsAndFonts.COLOR_BLACK);
        if (isDisabled) {
            borderColor = textColor = Toolkit.getColor(ColorsAndFonts.COLOR_MENU_DISABLED);
        } else if (isDefault) {
            borderColor = Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY1);
        } else if (isOver || hasFocus) {
            borderColor = Toolkit.getColor(ColorsAndFonts.COLOR_BLACK);
        } else {
            borderColor = Toolkit.getColor(ColorsAndFonts.COLOR_BLACK);
        }
        canvas.drawRoundedRectangle(0, 0, buttonWidth, buttonHeight, 0, 0, borderColor);
        canvas.drawText(text, TEXT_PADDING, buttonHeight / 2 + style.getMidPoint(), textColor, style);
    }

    @Override
    public Size getMaximumSize(final String text) {
        final int buttonWidth = TEXT_PADDING + Toolkit.getText(ColorsAndFonts.TEXT_CONTROL).stringWidth(text) + TEXT_PADDING;
        return new Size(buttonWidth, buttonHeight);
    }
}
