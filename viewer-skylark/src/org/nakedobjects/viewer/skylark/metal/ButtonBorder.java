package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.UserAction;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;


public class ButtonBorder extends AbstractBorder {
    private final int buttonHeight;
    private final UserAction[] actions;
    private final int buttonSetWidth;
    private static final int TEXT_PADDING = 12;
    private static final int BUTTON_SPACING = 5;
    
    public ButtonBorder(UserAction[] actions, View view) {
        super(view);
        
        this.actions = actions;
        buttonHeight = VPADDING + Style.NORMAL.getHeight() + VPADDING;
        bottom = HPADDING * 2 + buttonHeight + HPADDING * 2;

        int width = 0;
        for (int i = 0; i < actions.length; i++) {
            String text = actions[i].getName(getView());
	        width += TEXT_PADDING + Style.NORMAL.stringWidth(text) + TEXT_PADDING + BUTTON_SPACING;
        }
        buttonSetWidth = width - BUTTON_SPACING;
    }
    
    public void draw(Canvas canvas) {
        int width = getSize().getWidth();
        int x = width / 2 - buttonSetWidth / 2;
        int y = getSize().getHeight() - bottom;

        // draw dividing line
        canvas.drawLine(0, y, width, y, Style.SECONDARY1);

        // draw buttons
        y  += VPADDING + 2;
        for (int i = 0; i < actions.length; i++) {
	        String text = actions[i].getName(getView());
	        int buttonWidth = TEXT_PADDING + Style.NORMAL.stringWidth(text) + TEXT_PADDING;
	        canvas.drawRectangle(x + 1, y + 1, buttonWidth - 1, buttonHeight, Style.WHITE);
			canvas.drawRectangle(x, y, buttonWidth - 1, buttonHeight, Style.SECONDARY1);
			canvas.drawText(text, x + 1 + TEXT_PADDING, y + VPADDING + Style.NORMAL.getAscent(), Style.BLACK, Style.NORMAL);
	        x += BUTTON_SPACING + buttonWidth;
        }
        
        // draw rest
        super.draw(canvas);
    }
    
    
    public void firstClick(Click click) {
        int y = click.getLocation().getY();
        if(y > getSize().getHeight() - bottom) {
            for (int i = 0; i < actions.length; i++) {
                
            }
        }
        super.firstClick(click);
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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