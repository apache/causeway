package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.utility.ToString;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewState;
import org.nakedobjects.viewer.skylark.core.AbstractView;


public abstract class TitleText {
    private final View view;
    private final Text style;
    private final int ellipsisWidth;

    public TitleText(final View view, final Text style) {
        this.view = view;
        this.style = style;

        ellipsisWidth = style.stringWidth("...");
    }

    public Size getSize() {
        int height = View.VPADDING + style.getTextHeight() + View.VPADDING;
        int width = View.HPADDING + style.stringWidth(title()) + View.HPADDING;
        return new Size(width, height);
    }

    /**
     * Draw this TitleText's text stating from the specified x coordination and on the specified
     * baseline.
     */
    public void draw(final Canvas canvas, final int x, final int baseline) {
        draw(canvas, x, baseline, -1);
    }

    /**
     * Draw this TitleText's text stating from the specified x coordination and on the specified
     * baseline. If a maximum width is specified (ie it is positive) then the text drawn will not
     * extend past that width.
     * 
     * @param maxWidth
     *                 the maximum width to display the text within; if negative no limit is imposed
     */
    public void draw(final Canvas canvas, final int x, final int baseline, final int maxWidth) {
        Color color;
        ViewState state = view.getState();
        if (state.canDrop()) {
            color = Style.VALID;
        } else if (state.cantDrop()) {
            color = Style.INVALID;
        } else if (state.isObjectIdentified()) {
            color = Style.PRIMARY1;
        } else {
            color = Style.BLACK;
        }

        final int xt = x + View.HPADDING;
        final int yt = baseline;

        String text = title();
        if (maxWidth > 0 && style.stringWidth(text) > maxWidth) {
            int lastCharacterWithinAllowedWidth = 0;
            for(int textWidth = ellipsisWidth; textWidth <= maxWidth;) {
                char character = text.charAt(lastCharacterWithinAllowedWidth);
                textWidth += style.charWidth(character);
                lastCharacterWithinAllowedWidth++;
            }
            
            int space = text.lastIndexOf(' ', lastCharacterWithinAllowedWidth - 1);
            if(space > 0) {
	            while (space >= 0) {
	                char character = text.charAt(space - 1);
	                if(Character.isLetterOrDigit(character)) {
	                    break;
	                }
	                space --;
	            }
	            
                text = text.substring(0, space);
            } else {
                text = text.substring(0, lastCharacterWithinAllowedWidth - 1);
            }
            text += "...";
        }

        canvas.drawText(text, xt, yt, color, style);

        if (AbstractView.debug) {
            int x2 = style.stringWidth(text) - 1;
            canvas.drawRectangle(xt, yt - style.getAscent(), x2, style.getTextHeight() - 1, Color.DEBUG_DRAW_BOUNDS);
            canvas.drawLine(xt, yt - style.getAscent() - style.getTextHeight() / 2, xt + x2, yt - style.getAscent()
                    - style.getTextHeight() / 2, Color.DEBUG_DRAW_BOUNDS);
            canvas.drawLine(xt, baseline, xt + x2, baseline, Color.DEBUG_BASELINE);
        }
    }

    protected abstract String title();
    
    public String toString() {
        ToString str = new ToString(this);
        str.append("style", style);
        return str.toString();
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */
