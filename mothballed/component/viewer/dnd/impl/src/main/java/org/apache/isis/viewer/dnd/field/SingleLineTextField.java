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

package org.apache.isis.viewer.dnd.field;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.ViewConstants;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.content.TextParseableContent;
import org.apache.isis.viewer.dnd.view.text.TextContent;
import org.apache.isis.viewer.dnd.view.text.TextUtils;

public class SingleLineTextField extends TextField {
    private static final int LIMIT = 20;
    private int offset = 0;

    public SingleLineTextField(final TextParseableContent content, final ViewSpecification specification, final boolean showLines) {
        super(content, specification, showLines, TextContent.NO_WRAPPING);
    }

    @Override
    protected void align() {
        final String line = textContent.getText(0);
        if (line != null) {
            final int maxWidth = getMaxFieldWidth();
            final int leftLimit = offset + LIMIT;
            final int rightLimit = offset + maxWidth - LIMIT;

            if (cursor.getCharacter() > line.length()) {
                cursor.end();
            }

            final int cursorPosition = style.stringWidth(line.substring(0, cursor.getCharacter()));
            if (cursorPosition > rightLimit) {
                offset = offset + (cursorPosition - rightLimit);
                offset = Math.min(style.stringWidth(line), offset);
            } else if (cursorPosition < leftLimit) {
                offset = offset - (leftLimit - cursorPosition);
                offset = Math.max(0, offset);
            }
        }
    }

    @Override
    protected void drawHighlight(final Canvas canvas, final int maxWidth) {
        final int baseline = getBaseline();
        final int top = baseline - style.getAscent();

        int from = selection.from().getCharacter();
        int to = selection.to().getCharacter();

        final String line = textContent.getText(0);
        if (to >= line.length()) {
            to = line.length();
        }
        if (from >= line.length()) {
            from = line.length();
        }
        if (line != null) {
            final int start = style.stringWidth(line.substring(0, from));
            final int end = style.stringWidth(line.substring(0, to));
            canvas.drawSolidRectangle(start + (ViewConstants.HPADDING), top, end - start, style.getLineHeight(), Toolkit.getColor(ColorsAndFonts.COLOR_TEXT_HIGHLIGHT));
        }
    }

    @Override
    protected void drawLines(final Canvas canvas, final Color color, final int width) {
        final int baseline = getBaseline();
        canvas.drawLine(ViewConstants.HPADDING, baseline, ViewConstants.HPADDING + width, baseline, color);
    }

    @Override
    protected void drawText(final Canvas canvas, final Color textColor, final int width) {
        final String[] lines = textContent.getDisplayLines();
        if (lines.length > 1) {
            throw new IsisException("Single line text field should contain a string that contains no line breaks; contains " + lines.length);
        }

        final String chars = lines[0];
        if (chars == null) {
            throw new IsisException();
        }
        if (chars.endsWith("\n")) {
            throw new RuntimeException();
        }

        final int baseline = getBaseline();

        // draw cursor
        if (hasFocus() && canChangeValue().isAllowed()) {
            final int at = Math.min(cursor.getCharacter(), chars.length());
            final int pos = style.stringWidth(chars.substring(0, at)) - offset + ViewConstants.HPADDING;
            canvas.drawLine(pos, (baseline + style.getDescent()), pos, baseline - style.getAscent(), Toolkit.getColor(ColorsAndFonts.COLOR_TEXT_CURSOR));
        }

        // draw text
        final String line = hasFocus() ? chars : TextUtils.limitText(chars, style, width);
        canvas.drawText(line, ViewConstants.HPADDING - offset, baseline, textColor, style);
    }

    @Override
    public void setMaximumSize(final Size size) {
        final int width = Math.max(180, size.getWidth() - ViewConstants.HPADDING);
        setWidth(width);
        invalidateLayout();
    }

}
