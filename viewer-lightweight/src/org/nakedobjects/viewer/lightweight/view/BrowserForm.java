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
package org.nakedobjects.viewer.lightweight.view;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.viewer.lightweight.AbstractCompositeView;
import org.nakedobjects.viewer.lightweight.Canvas;
import org.nakedobjects.viewer.lightweight.DragSource;
import org.nakedobjects.viewer.lightweight.DragTarget;
import org.nakedobjects.viewer.lightweight.InternalView;
import org.nakedobjects.viewer.lightweight.Padding;
import org.nakedobjects.viewer.lightweight.Size;
import org.nakedobjects.viewer.lightweight.Style;
import org.nakedobjects.viewer.lightweight.View;
import org.nakedobjects.viewer.lightweight.util.StackLayout;
import org.nakedobjects.viewer.lightweight.util.ViewFactory;


/**
 * This is a simple form that appears in the RHS of the object browser
 */
public class BrowserForm extends AbstractCompositeView implements InternalView, DragSource,
    DragTarget {
    private static final int MINIMUM_WIDTH = 100;
    private BrowserFilter browserFilter;

    public BrowserForm(BrowserFilter browserFilter) {
        this.browserFilter = browserFilter;
        setLayout(new StackLayout());
    }

    public Padding getPadding() {
        Padding padding = super.getPadding();

        // labels
        int width = 0;
        Fields flds = new Fields();

        while (flds.hasMore()) {
            int labelWidth = Style.LABEL.stringWidth(flds.next().getName() + ":") + 10;
            width = Math.max(width, labelWidth);
        }

        padding.extendLeft(width);

        return padding;
    }

    public Size getRequiredSize() {
        Size size = super.getSize();
        size.ensureWidth(MINIMUM_WIDTH);

        return size;
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        // labels
        Fields flds = new Fields();
        View[] components = getComponents();

        int top = getPadding().getTop();
        int i = 0;

        while (flds.hasMore()) {
            Field field = flds.next();
            int baseline = top + components[i].getBaseline();
            canvas.drawText(field.getName() + ":", HPADDING, baseline, Style.IN_FOREGROUND,
                Style.LABEL);
            top += components[i].getBounds().getHeight();
            i++;
        }
    }

    public String toString() {
        // TODO Auto-generated method stub
        return "xxx";
    }

    protected void init(NakedObject object) {
        Fields flds = new Fields();

        while (flds.hasMore()) {
            Field field = flds.next();
            addView(ViewFactory.getViewFactory().createInternalView(field.get(getObject()), field,
                    true));
        }
    }

    private class Fields {
        Field[] fields = getObject().getNakedClass().getFields();
        int i = 0;

        private boolean hasMore() {
            while (i < fields.length && !browserFilter.isInTree(fields[i])) {
                i++;
            }

            return i < fields.length;
        }

        private Field next() {
            return fields[i++];
        }
    }
}
