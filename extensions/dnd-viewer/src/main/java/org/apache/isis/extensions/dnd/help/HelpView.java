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


package org.apache.isis.extensions.dnd.help;

import org.apache.isis.extensions.dnd.drawing.Canvas;
import org.apache.isis.extensions.dnd.drawing.Color;
import org.apache.isis.extensions.dnd.drawing.ColorsAndFonts;
import org.apache.isis.extensions.dnd.drawing.Size;
import org.apache.isis.extensions.dnd.drawing.Text;
import org.apache.isis.extensions.dnd.view.Click;
import org.apache.isis.extensions.dnd.view.Toolkit;
import org.apache.isis.extensions.dnd.view.base.AbstractView;
import org.apache.isis.extensions.dnd.view.content.NullContent;
import org.apache.isis.extensions.dnd.view.text.TextBlockTarget;
import org.apache.isis.extensions.dnd.view.text.TextContent;


public class HelpView extends AbstractView implements TextBlockTarget {
    private static final int HEIGHT = 350;
    private static final int WIDTH = 400;
    private static final int MAX_TEXT_WIDTH = 375;
    private final TextContent content;

    public HelpView(final String name, final String description, final String help) {
        super(new NullContent());
        final String text = (name == null || name.trim().equals("") ? "" : (name + "\n"))
                + (description == null || description.trim().equals("") ? "" : (description + "\n")) + (help == null ? "" : help);
        content = new TextContent(this, 10, TextContent.WRAPPING);
        content.setText(text);
    }

    @Override
    public void draw(final Canvas canvas) {
        int x = 0;
        int y = 0;
        final int xEntent = getSize().getWidth() - 1;
        final int yExtent = getSize().getHeight() - 1;

        final int arc = 9;
        canvas.drawSolidRectangle(x + 2, y + 2, xEntent - 4, yExtent - 4, Toolkit.getColor(ColorsAndFonts.COLOR_WHITE));
        Color black = Toolkit.getColor(ColorsAndFonts.COLOR_BLACK);
        canvas.drawRoundedRectangle(x, y++, xEntent, yExtent, arc, arc, black);
        canvas.drawRoundedRectangle(x + 1, y++, xEntent - 2, yExtent - 2, arc, arc, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2));
        canvas.drawRoundedRectangle(x + 2, y++, xEntent - 4, yExtent - 4, arc, arc, black);

        x += 10;
        y += VPADDING;
        y += Toolkit.getText(ColorsAndFonts.TEXT_TITLE).getTextHeight();
        canvas.drawText("Help", x, y, black, Toolkit.getText(ColorsAndFonts.TEXT_TITLE));

        final String[] lines = content.getDisplayLines();
        for (int i = 0; i < lines.length; i++) {
            y += Toolkit.getText(ColorsAndFonts.TEXT_NORMAL).getLineHeight();
            canvas.drawText(lines[i], x, y, MAX_TEXT_WIDTH, black, Toolkit.getText(ColorsAndFonts.TEXT_NORMAL));
        }
    }

    @Override
    public Size getRequiredSize(final Size availableSpace) {
        final int height = Math.min(HEIGHT, availableSpace.getHeight());
        final int width = Math.min(WIDTH, availableSpace.getWidth());
        return new Size(width, height);
    }

    /**
     * Removes the help view when clicked on.
     */
    @Override
    public void firstClick(final Click click) {
        getViewManager().clearOverlayView(this);
    }

    public int getMaxFieldWidth() {
        return WIDTH - 20;
    }

    public Text getText() {
        return Toolkit.getText(ColorsAndFonts.TEXT_NORMAL);
    }
}
