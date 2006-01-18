package org.nakedobjects.viewer.skylark.value;

import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.UserActionSet;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.ValueContent;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;

import java.awt.event.KeyEvent;


public class PasswordField extends AbstractField {
    protected static final Text style = Style.NORMAL;
    private boolean isSaved;
    private int maxTextWidth;
    private String password;
    private int width;
    private boolean identified;

    public PasswordField(Content content, ViewSpecification design, ViewAxis axis, int width) {
        super(content, design, axis);

        setMaxTextWidth(width);

        this.width = style.charWidth('O') + 2;
 //       height = style.getTextHeight() + style.getDescent();

        password = text();
    }

    public boolean canFocus() {
        return canChangeValue();
    }

    public void contentMenuOptions(UserActionSet options) {
        options.add(new ClearValueOption());
        super.contentMenuOptions((options));
        options.setColor(Style.VALUE_MENU);
    }

    private void delete() {
        isSaved = false;
        password = password.substring(0, password.length() - 1);
        markDamaged();
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        Color color = identified ? Style.IDENTIFIED : Style.SECONDARY2;
        color = hasFocus() ? Style.PRIMARY1 : color;
        int baseline = getBaseline();
        canvas.drawLine(HPADDING, baseline , HPADDING + getSize().getWidth(), baseline, color);
        
        int length = password.length();
        int x = 3;
        for (int i = 0; i < length; i++) {
            canvas.drawSolidOval(x, 1, width, width, hasFocus() ? Color.YELLOW : Color.LIGHT_GRAY);
            x += width + 2;
        }
        x = 3;
        for (int i = 0; i < length; i++) {
            canvas.drawOval(x, 1 , width, width, hasFocus() ? Color.BLACK : Color.GRAY);
            x += width + 2;
        }
        
        if (hasFocus() && canChangeValue()) {
            canvas.drawLine(x, (baseline + style.getDescent()), x, 0,
                    Style.PRIMARY1);
        }
    }

    public void editComplete() {
        if (canChangeValue() && !isSaved) {
            isSaved = true;
            initiateSave();
        }
    }

    public void escape() {
        password = "";
        isSaved = false;
        markDamaged();
    }



    public void entered() {
        if(canChangeValue()) {
            getViewManager().showTextCursor();
            identified = true;
            markDamaged();
        }
    }

    public void exited() {
        if (canChangeValue()) {
            getViewManager().showArrowCursor();
            identified = false;
            markDamaged();
        }
    }
    
    public void focusLost() {
        editComplete();
    }

    public Size getRequiredSize() {
        int width = HPADDING + maxTextWidth + HPADDING;
        int height = style.getTextHeight() + VPADDING;
        height = Math.max(height, Style.defaultFieldHeight());

        return new Size(width, height);
    }

    public void keyPressed(final int keyCode, final int modifiers) {
        if (!canChangeValue()) {
            return;
        }

        if (keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_SHIFT || keyCode == KeyEvent.VK_ALT) {
            return;
        }

        switch (keyCode) {
        case KeyEvent.VK_LEFT:
            delete();
            break;
        case KeyEvent.VK_DELETE:
            delete();
            break;
        case KeyEvent.VK_BACK_SPACE:
            delete();
            break;
        case KeyEvent.VK_TAB:
            editComplete();
            break;
        case KeyEvent.VK_ENTER:
            editComplete();
            getParent().keyPressed(keyCode, modifiers);
            break;
        case KeyEvent.VK_ESCAPE:
            escape();
            break;

        default:
            break;
        }
    }

    public void keyTyped(char keyCode) {
        password += keyCode;
        isSaved = false;
        markDamaged();
    }

    protected void save() {
        ValueContent content = (ValueContent) getContent();
        content.parseTextEntry(password);
    }

    /**
     * Set the maximum width of the field, as a number of characters
     */
    public void setMaxTextWidth(int noCharacters) {
        maxTextWidth = style.charWidth('o') * noCharacters;
    }

    /**
     * Set the maximum width of the field, as a number of pixels
     */
    public void setMaxWidth(int width) {
        maxTextWidth = width;
    }

    private String text() {
        String text;
        ValueContent content = (ValueContent) getContent();
        text = content.getNaked().titleString();
        return text;
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