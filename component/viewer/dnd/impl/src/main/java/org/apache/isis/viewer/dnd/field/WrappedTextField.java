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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.ViewConstants;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.content.TextParseableContent;
import org.apache.isis.viewer.dnd.view.text.CursorPosition;
import org.apache.isis.viewer.dnd.view.text.TextContent;

public class WrappedTextField extends TextField {
    private static final Logger LOG = LoggerFactory.getLogger(WrappedTextField.class);

    public WrappedTextField(final TextParseableContent content, final ViewSpecification specification, final boolean showLines) {
        super(content, specification, showLines, TextContent.WRAPPING);
    }

    public void setWrapping(final boolean wrapping) {
    }

    @Override
    protected void drawLines(final Canvas canvas, final Color color, final int width) {
        int baseline = getBaseline();
        final int noDisplayLines = textContent.getNoDisplayLines();
        for (int line = 0; line < noDisplayLines; line++) {
            canvas.drawLine(ViewConstants.HPADDING, baseline, ViewConstants.HPADDING + width, baseline, color);
            baseline += getText().getLineHeight();
        }
    }

    @Override
    protected void drawHighlight(final Canvas canvas, final int maxWidth) {
        final int baseline = getBaseline();
        int top = baseline - style.getAscent();

        final CursorPosition from = selection.from();
        final CursorPosition to = selection.to();

        final String[] lines = textContent.getDisplayLines();
        final int displayFromLine = textContent.getDisplayFromLine();
        final int displayToLine = displayFromLine + lines.length;
        for (int i = displayFromLine; i <= displayToLine; i++) {
            if ((i >= from.getLine()) && (i <= to.getLine())) {
                final String line = textContent.getText(i);
                int start = 0;
                int end = style.stringWidth(line);

                if (from.getLine() == i) {
                    final int at = Math.min(from.getCharacter(), line.length());
                    start = style.stringWidth(line.substring(0, at));
                }

                if (to.getLine() == i) {
                    final int at = Math.min(to.getCharacter(), line.length());
                    end = style.stringWidth(line.substring(0, at));
                }

                canvas.drawSolidRectangle(start + (ViewConstants.HPADDING), top, end - start, getText().getLineHeight(), Toolkit.getColor(ColorsAndFonts.COLOR_TEXT_HIGHLIGHT));
            }

            top += getText().getLineHeight();
        }
    }

    @Override
    protected void drawText(final Canvas canvas, final Color textColor, final int width) {
        int baseline = getBaseline();
        final String[] lines = textContent.getDisplayLines();
        final int cursorLine = cursor.getLine() - textContent.getDisplayFromLine();
        for (int i = 0; i < lines.length; i++) {
            final String chars = lines[i];
            if (chars == null) {
                throw new IsisException();
            }
            if (chars.endsWith("\n")) {
                throw new RuntimeException();
            }

            // draw cursor
            if (hasFocus() && canChangeValue().isAllowed() && cursorLine == i) {
                final int at = Math.min(cursor.getCharacter(), chars.length());
                final int pos = style.stringWidth(chars.substring(0, at)) + ViewConstants.HPADDING;
                canvas.drawLine(pos, (baseline + style.getDescent()), pos, baseline - style.getAscent(), Toolkit.getColor(ColorsAndFonts.COLOR_TEXT_CURSOR));
            }

            // draw text
            canvas.drawText(chars, ViewConstants.HPADDING, baseline, textColor, style);
            baseline += getText().getLineHeight();
        }
        /*
         * if (end < entryLength) { int x = style.stringWidth(new String(buffer,
         * start, end)); g.setColor(Color.red); g.drawString("\u00bb", x,
         * baseline - lineHeight()); }
         */
    }

    @Override
    protected boolean enter() {
        textContent.breakBlock(cursor);
        cursor.lineDown();
        cursor.home();
        markDamaged();
        return true;
    }

    /**
     * Sets the number of lines to display
     */
    public void setNoLines(final int noLines) {
        textContent.setNoDisplayLines(noLines);
    }

    @Override
    public void setSize(final Size size) {
        super.setSize(size);
        textContent.setNoDisplayLines(size.getHeight() / style.getLineHeight());
    }

    @Override
    public void setMaximumSize(final Size size) {
        final int lines = Math.max(1, size.getHeight() / getText().getLineHeight());
        setNoLines(lines);
        final int width = Math.max(180, size.getWidth() - ViewConstants.HPADDING);
        setWidth(width);
        LOG.debug(lines + " x " + width);
        invalidateLayout();
    }

    @Override
    protected void align() {
    }

}
