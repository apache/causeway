package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewState;
import org.nakedobjects.viewer.skylark.core.AbstractView;


public abstract class TitleText {
     private View view;
    private Text style;

    public TitleText(View view, Text style) {
        this.view = view;
         this.style = style;
    }

    public Size getSize() {
        int height = View.VPADDING + style.getHeight() + View.VPADDING;
        int width = View.HPADDING + style.stringWidth(title()) + View.HPADDING;

        return new Size(width, height);
    }

    public void draw(Canvas canvas, int x, int baseline) {
        Color color;
   
        if (getState().canDrop()) {
            color = Style.VALID;
        } else if (getState().cantDrop()) {
            color = Style.INVALID;
        } else if (getState().isObjectIdentified()) {
            color = Style.PRIMARY1;
 /*       } else if (getState().isRootViewIdentified()) {
            color = Style.PRIMARY2;
    */    } else {
            color = Style.BLACK;
        }

        int maxWidth = view.getSize().getWidth() - view.getPadding().getLeft() - view.getPadding().getRight();

         String text = title();
        int xt = x + View.HPADDING;
        int yt = baseline;

        if (style.stringWidth(text) > maxWidth) {
            int elip = style.stringWidth("...");

            do {
                int last = text.lastIndexOf(' ');

                if (last == -1) {
                    for (int i = text.length() - 1; i > 5; i--) {
                        if ((style.stringWidth(text.substring(0, i)) + elip) < maxWidth) {
                            text = text.substring(0, i);

                            break;
                        }
                    }

                    break;
                }

                text = text.substring(0, last);
            } while ((style.stringWidth(text) + elip) > maxWidth);

            text += "...";
        }

        canvas.drawText(text, xt, yt, color, style);

        if (AbstractView.DEBUG) {
        	int x2 = style.stringWidth(text) - 1;
            canvas.drawRectangle(xt, yt - style.getAscent(), x2, style.getHeight() - 1, Color.DEBUG_DRAW_BOUNDS);
            canvas.drawLine(xt, yt - style.getAscent() - style.getHeight() / 2, xt +x2,  yt - style.getAscent() - style.getHeight() / 2, Color.DEBUG_DRAW_BOUNDS);
           canvas.drawLine(xt, baseline, xt + x2, baseline, Color.DEBUG_BASELINE);
        }
    }

    private ViewState getState() {
        return view.getState();
    }

    protected abstract String title();
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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
