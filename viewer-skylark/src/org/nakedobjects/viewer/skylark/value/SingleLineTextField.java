package org.nakedobjects.viewer.skylark.value;

import org.nakedobjects.utility.NakedObjectRuntimeException;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.text.TextContent;


public class SingleLineTextField extends TextField {
    private static final int LIMIT = 20;
    private int offset = 0;

    public SingleLineTextField(Content content, ViewSpecification specification, ViewAxis axis, boolean showLines, int width) {
        super(content, specification, axis, showLines, width, TextContent.NO_WRAPPING);
    }

    protected void align() {
        String line = textContent.getText(0);
        if(line != null) {
            int maxWidth = getMaxWidth();
            int leftLimit = offset + LIMIT; 
            int rightLimit = offset + maxWidth - LIMIT;
            
            if(cursor.getCharacter() > line.length()) {
                cursor.end();
            }
            
            int cursorPosition = style.stringWidth( line.substring(0, cursor.getCharacter()));
            if(cursorPosition > rightLimit) {
                offset = offset + (cursorPosition - rightLimit);
                offset = Math.min(style.stringWidth(line), offset);            
            } else if(cursorPosition < leftLimit) {
                offset = offset - (leftLimit - cursorPosition);
                offset = Math.max(0, offset);
            }
        }
    }
    
    protected void drawHighlight(Canvas canvas, int maxWidth) {
        int baseline = getBaseline();
        int top = baseline - style.getAscent();

        int from = selection.from().getCharacter();
        int to = selection.to().getCharacter();

        String line = textContent.getText(0);
        if(line != null) {
            int start = style.stringWidth(line.substring(0, from));
            int end = style.stringWidth(line.substring(0, to));
            canvas.drawSolidRectangle(start + (HPADDING), top, end - start, style.getLineHeight(), Style.PRIMARY3);
        }
    }

    protected void drawLines(Canvas canvas, Color color, int width) {
        int baseline = getBaseline();
        canvas.drawLine(HPADDING, baseline, HPADDING + width, baseline, color);
    }

    protected void drawText(Canvas canvas, Color textColor, int width) {
        String[] lines = textContent.getDisplayLines();
        if (lines.length > 1) {
            throw new NakedObjectRuntimeException(
                    "Single line text field should contain a string that contains no line breaks; contains " + lines.length);
        }

        String chars = lines[0];
        if (chars == null) {
            throw new NakedObjectRuntimeException();
        }
        if (chars.endsWith("\n")) {
            throw new RuntimeException();
        }

        int baseline = getBaseline();

        // draw cursor
        if (hasFocus() && canChangeValue()) {
            int at = Math.min(cursor.getCharacter(), chars.length());
            int pos =  style.stringWidth(chars.substring(0, at)) - offset;
            canvas.drawLine(pos + 1, (baseline + style.getDescent()), pos + 1, 
                    baseline - style.getAscent(), Style.PRIMARY1);
        }

        // draw text
        canvas.drawText(chars, HPADDING - offset, baseline, textColor, style);
        baseline += getText().getLineHeight();
    }

    public void setRequiredSize(Size size) {
        int width = Math.max(180, size.getWidth() - HPADDING);
        setMaxWidth(width);
        invalidateLayout();
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */