package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.object.control.Permission;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Size;
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
	        Color color = actions[i].disabled(getView()).isVetoed() ? Style.DISABLED_MENU : Style.BLACK;
	        Color border = actions[i].disabled(getView()).isVetoed() ? Style.DISABLED_MENU : Style.SECONDARY2;
	        int buttonWidth = TEXT_PADDING + Style.NORMAL.stringWidth(text) + TEXT_PADDING;
	        canvas.drawRectangle(x + 1, y + 1, buttonWidth - 1, buttonHeight, Style.WHITE);
			canvas.drawRectangle(x, y, buttonWidth - 1, buttonHeight, border);
			canvas.drawText(text, x + 1 + TEXT_PADDING, y + VPADDING + Style.NORMAL.getAscent(), color, Style.NORMAL);
	        x += BUTTON_SPACING + buttonWidth;
        }
        
        // draw rest
        super.draw(canvas);
    }
    
    
    public void firstClick(Click click) {
        UserAction action = overButton(click.getMouseLocationRelativeToView());
        if(action == null) {
	        super.firstClick(click);
        } else {
	        if(action.disabled(getView()).isAllowed()) {
	            action.execute(getWorkspace(), getView(), getLocation());
	        }
        }
    }
    
    public void secondClick(Click click) {
        UserAction action = overButton(click.getMouseLocationRelativeToView());
        if(action == null) {
	        super.secondClick(click);
        }
    }
   
    
    public void thirdClick(Click click) {
        UserAction action = overButton(click.getMouseLocationRelativeToView());
        if(action == null) {
	        super.thirdClick(click);
        }
    }
    

    public Size getRequiredSize() {
        Size size = super.getRequiredSize();
        
        int totalButtonWidth = BUTTON_SPACING;
        for (int i = 0; i < actions.length; i++) {
	        String text = actions[i].getName(getView());
	        int buttonWidth = TEXT_PADDING + Style.NORMAL.stringWidth(text) + TEXT_PADDING;
	        totalButtonWidth += BUTTON_SPACING + buttonWidth;
        }
        
        size.ensureWidth(totalButtonWidth);
        return size;
    }
    
    public void mouseMoved(Location at) {
        UserAction action = overButton(at);
        if(action != null) {
	        getViewManager().setStatus("");
            Permission disabled = action.disabled(getView());
            if(disabled.isVetoed()) {
                getViewManager().setStatus(disabled.getReason());
            }
        }
        super.mouseMoved(at);
    }
    
    /**
     * Finds the action button under the pointer; returning null if none.
     * @param location
     */
    private UserAction overButton(Location location) {
        int yy = location.getY(); 
        int xx = location.getX();
        if(yy > getSize().getHeight() - bottom) {
            
            int width = getSize().getWidth();
            int x = width / 2 - buttonSetWidth / 2;
            int y = getSize().getHeight() - bottom;
          
            for (int i = 0; i < actions.length; i++) {
    	        String text = actions[i].getName(getView());
    	        int buttonWidth = TEXT_PADDING + Style.NORMAL.stringWidth(text) + TEXT_PADDING;
    	        if(xx > x && xx < x + buttonWidth && yy > y && yy < y + buttonHeight) {
    	            return actions[i];
                }
    	        x += BUTTON_SPACING + buttonWidth;
            }
        } 
        return null;
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