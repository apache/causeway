package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;


public class Button extends AbstractControlView {
    private static final int TEXT_PADDING = 12;
    private final int buttonHeight;
    private boolean over;
    private boolean pressed;

    public Button(ButtonAction action, View target) {
        super(action, target);
        this.buttonHeight = 4 + Style.NORMAL.getTextHeight() + 4;
    }

    public void draw(Canvas canvas) {
        int x = 0;
        int y = 0;

        View target = getParent();
        String text = action.getName(target);
        boolean vetoed = action.disabled(target).isVetoed();
        Color color = vetoed ? Style.DISABLED_MENU : Style.BLACK;
        Color border = vetoed ? Style.SECONDARY3 : Style.SECONDARY2;
        Text style = Style.NORMAL;
        int buttonWidth = TEXT_PADDING + style.stringWidth(text) + TEXT_PADDING;
        canvas.clearBackground(this, Style.SECONDARY3);
        canvas.drawRectangle(x, y, buttonWidth, buttonHeight, over & !vetoed ? Style.PRIMARY1 : Style.BLACK);
        canvas.draw3DRectangle(x + 1, y + 1, buttonWidth - 2, buttonHeight - 2, border, !pressed);
        canvas.draw3DRectangle(x + 2, y + 2, buttonWidth - 4, buttonHeight - 4, border, !pressed);
        if (((ButtonAction) action).isDefault()) {
            canvas.drawRectangle(x + 3, y + 3, buttonWidth - 6, buttonHeight - 6, border);
        }
        canvas.drawText(text, x + TEXT_PADDING, y + buttonHeight / 2 + style.getMidPoint(), color, style);
    }

    public void entered() {
        over = true;
        pressed = false;
        markDamaged();
        super.entered();
    }

    public void exited() {
        over = false;
        pressed = false;
        markDamaged();
        super.exited();
    }

    public Size getRequiredSize() {
        String text = action.getName(getView());
        int buttonWidth = TEXT_PADDING + Style.NORMAL.stringWidth(text) + TEXT_PADDING;
        return new Size(buttonWidth, buttonHeight);
    }

    public void mouseDown(Click click) {
        View target = getParent();
        boolean vetoed = action.disabled(target).isVetoed();
        if (!vetoed) {
            pressed = true;
            markDamaged();
        }
    }

    public void mouseUp(Click click) {
        View target = getParent();
        boolean vetoed = action.disabled(target).isVetoed();
        if (!vetoed) {
            pressed = false;
            markDamaged();
        }
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */