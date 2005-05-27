package org.nakedobjects.viewer.skylark.table;

import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.basic.IconGraphic;
import org.nakedobjects.viewer.skylark.basic.ObjectTitleText;
import org.nakedobjects.viewer.skylark.basic.TitleText;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;


public class TableRowBorder extends AbstractBorder {
    private int baseline;
    private IconGraphic icon;
    private TitleText title;

    public TableRowBorder(View wrappedRow) {
        super(wrappedRow);

        icon = new IconGraphic(this, Style.NORMAL);
        title = new ObjectTitleText(this, Style.NORMAL);
        baseline = icon.getBaseline();

        left = requiredTitleWidth();

        ((TableAxis) wrappedRow.getViewAxis()).ensureOffset(left);
    }

    public void debugDetails(StringBuffer b) {
        b.append("RowBorder " + left + " pixels");
    }

    public void draw(Canvas canvas) {
        int bl = getBaseline();

        int width = ((TableAxis) getViewAxis()).getHeaderOffset();
        Canvas subcanvas = canvas.createSubcanvas(0, 0, width, getSize().getHeight());
        icon.draw(subcanvas, 1, bl);
        title.draw(subcanvas, icon.getSize().getWidth() + HPADDING, bl);

        int y = getSize().getHeight() - 1;
        canvas.drawLine(0, y, getSize().getWidth(), y, Style.SECONDARY2);

        // components
        super.draw(canvas);
    }

    public int getBaseline() {
        return baseline;
    }

    protected int getLeft() {
        return ((TableAxis) wrappedView.getViewAxis()).getHeaderOffset();
    }

    protected int requiredTitleWidth() {
        return icon.getSize().getWidth() + HPADDING + title.getSize().getWidth();
    }

    public String toString() {
        return wrappedView.toString() + "/RowBorder";
    }

    public ViewAreaType viewAreaType(Location mouseLocation) {
        if (mouseLocation.getX() <= left) {
            return ViewAreaType.CONTENT;
        } else if (mouseLocation.getX() >= getSize().getWidth() - right) {
            return ViewAreaType.VIEW;
        } else {
            return super.viewAreaType(mouseLocation);
        }
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