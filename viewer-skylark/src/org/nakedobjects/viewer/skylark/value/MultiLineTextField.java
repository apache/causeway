package org.nakedobjects.viewer.skylark.value;

import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.text.CursorPosition;
import org.nakedobjects.viewer.skylark.text.TextContent;

import org.apache.log4j.Logger;

public class MultiLineTextField extends TextField {
    private static final Logger LOG = Logger.getLogger(MultiLineTextField.class);
    
    public MultiLineTextField(Content content, ViewSpecification specification, ViewAxis axis, boolean showLines, int width) {
        super(content, specification, axis, showLines, width, TextContent.WRAPPING);
    }

    
    
    private boolean multiline;
    private boolean wrapping;

    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }
    
    public void setWrapping(boolean wrapping) {
        this.wrapping = wrapping;
    }

    protected void drawLines(Canvas canvas, Color color, int width) {
        int baseline = getBaseline() + 1;
          int noDisplayLines = textContent.getNoDisplayLines();
          for (int line = 0; line < noDisplayLines; line++) {
              canvas.drawLine(HPADDING, baseline, HPADDING + width, baseline, color);
              baseline += getText().getLineHeight();
          }
  }

    protected void drawHighlight(Canvas canvas, int maxWidth) {
        int baseline = getBaseline();
        int top = baseline - style.getAscent();

        CursorPosition from = selection.from();
        CursorPosition to = selection.to();
/**
        for (int i = displayFromLine; i <= displayToLine; i++) {
            if ((i >= from.getLine()) && (i <= to.getLine())) {
                String line = textContent.getText(i);
                int start = 0;
                int end = style.stringWidth(line);

                if (from.getLine() == i) {
                    int at = Math.min(from.getCharacter(), line.length());
                    start = style.stringWidth(line.substring(0, at));
                }

                if (to.getLine() == i) {
                    int at = Math.min(to.getCharacter(), line.length());
                    end = style.stringWidth(line.substring(0, at));
                }

                canvas.drawSolidRectangle(start + (HPADDING), top, end - start, lineHeight(), Style.PRIMARY3);
            }

            top += lineHeight();
        }
 */
        	}


    protected void drawText(Canvas canvas, Color textColor, int width) {

        int baseline = getBaseline();
        //LOG.debug(displayFromLine + " -> " + displayToLine);
        
        String[] lines = textContent.getDisplayLines();
        for (int i = 0; i < lines.length; i++) {
            String chars = lines[i];
            if(chars == null) {
                throw new NakedObjectRuntimeException();
            }
            if (chars.endsWith("\n")) { throw new RuntimeException(); }

            // draw cursor
            if (hasFocus() && (cursor.getLine() == i) && canChangeValue()) {
                int at = Math.min(cursor.getCharacter(), chars.length());
                int pos = style.stringWidth(chars.substring(0, at));
                canvas.drawLine(pos + (HPADDING), (baseline + style.getDescent()), pos + (HPADDING),
                        baseline - style.getAscent(), Style.PRIMARY1);
            }

            // draw text
            canvas.drawText(chars, HPADDING, baseline, textColor, style);
            baseline += getText().getLineHeight();
        }
/*
        if (end < entryLength) {
            int x = style.stringWidth(new String(buffer, start, end));
            g.setColor(Color.red);
            g.drawString("\u00bb", x, baseline - lineHeight());
        }
        */
    }




    protected void enter() {
        if (multiline) {
            textContent.breakBlock(cursor);
            cursor.lineDown();
            cursor.home();
            markDamaged();
        } else {
            super.enter();
        }
    }

    /**
     * Sets the number of lines to display
     */
    public void setNoLines(int noLines) {
        textContent.setNoDisplayLines(noLines);
    }

    
    public void setRequiredSize(Size size) {
        int lines = Math.max(1, size.getHeight() / getText().getLineHeight());
        setNoLines(lines);
	    int width = Math.max(180, size.getWidth() - HPADDING);
        setMaxWidth(width);
        LOG.debug(lines + " x " + width);
        invalidateLayout();
    }

    protected void align() {}

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/