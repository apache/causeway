package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.object.reflect.PojoAdapter;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Image;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.basic.ActionDialogSpecification;
import org.nakedobjects.viewer.skylark.util.ImageFactory;


public class NakedErrorSmallViewSpecification implements ViewSpecification {

    public boolean canDisplay(Content content) {
        return content.isObject() && content.getNaked() instanceof PojoAdapter
                && ((PojoAdapter) content.getNaked()).getObject() instanceof Exception;
    }

    public String getName() {
        return "Compact Naked Error";
    }

    public View createView(Content content, ViewAxis axis) {
        // TODO extract the 'close window' action
        ButtonAction actions[] = new ButtonAction[] { new ActionDialogSpecification.CloseAction() };
        return new DialogBorder(new ButtonBorder(actions, new SmallErrorView(content, this, null)), false);
    }

    public boolean isOpen() {
        return true;
    }

    public boolean isReplaceable() {
        return false;
    }

    public boolean isSubView() {
        return false;
    }
}

class SmallErrorView extends AbstractErrorView {
    private static Image errorIcon;
    {
        errorIcon = ImageFactory.getInstance().createIcon("error", 32, null);
        if (errorIcon == null) {
            errorIcon = ImageFactory.getInstance().createFallbackIcon(32, null);
        }
    }

    protected SmallErrorView(Content content, ViewSpecification specification, ViewAxis axis) {
        super(content, specification, axis);
    }

    public Size getRequiredSize() {
        Size size = new Size();
        size.extend(20, 20);

        size.extendWidth(errorIcon.getWidth());
        size.extendWidth(20);

        size.extendHeight(Style.TITLE.getAscent());

        size.extendHeight(30);
        size.extendWidth(Style.NORMAL.stringWidth(message));
        size.extendHeight(Style.NORMAL.getTextHeight());

        size.extend(40, 20);
        return size;
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        int left = 20;
        int top = 20;

        canvas.clearBackground(this, Style.WHITE);

        canvas.drawIcon(errorIcon, left, top);

        left += errorIcon.getWidth() + 20;
        top += Style.TITLE.getAscent();
        canvas.drawText(name, left, top, Color.BLACK, Style.TITLE);

        top += 30;
        canvas.drawText(message, left, top, Color.BLACK, Style.NORMAL);
    }

    public ViewAreaType viewAreaType(Location mouseLocation) {
        return ViewAreaType.VIEW;
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