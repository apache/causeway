package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.CompositeViewBuilder;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.OneToManyField;
import org.nakedobjects.viewer.skylark.Padding;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.ValueContent;
import org.nakedobjects.viewer.skylark.ValueField;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.basic.IconGraphic;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;
import org.nakedobjects.viewer.skylark.core.AbstractBuilderDecorator;
import org.nakedobjects.viewer.skylark.core.AbstractCompositeViewSpecification;
import org.nakedobjects.viewer.skylark.util.ViewFactory;


public class TableRowSpecification extends AbstractCompositeViewSpecification {
    public TableRowSpecification() {
        builder = new RowLayout(new ObjectFieldBuilder(new Cells()));
    }

    public View createView(Content content, ViewAxis axis) {
        View view = super.createView(content, axis);

        return new RowBorder(view);
    }

    public String getName() {
        return "Table Row";
    }

    public boolean isReplaceable() {
        return false;
    }

    public boolean isSubView() {
        return true;
    }

    private static class Cells implements SubviewSpec {
        public View createSubview(Content content, ViewAxis axis) {
            ViewFactory factory = ViewFactory.getViewFactory();

            ViewSpecification spec;

            if (content instanceof OneToManyField) {
                return null;
            } else if (content instanceof ValueField) {
                spec = factory.getValueFieldSpecification((ValueContent) content);
            } else if (content instanceof ObjectContent) {
                spec = factory.getIconizedSubViewSpecification((ObjectContent) content);
            } else {
                throw new NakedObjectRuntimeException();
            }

            return spec.createView(content, axis);
        }

        public View decorateSubview(View view) {
            return new TableCellResizeBorder(view);
        }
    }
}


class RowBorder extends AbstractBorder {
    private static final int HANDLE_WIDTH = 20;
    private int baseline;
    private IconGraphic icon;

    public RowBorder(View wrappedView) {
        super(wrappedView);

        icon = new IconGraphic(this, Style.NORMAL);

        baseline = icon.getBaseline();

        left = icon.getSize().getWidth();
        
        ((TableColumnAxis) wrappedView.getViewAxis()).setOffset(left);

        right = HANDLE_WIDTH;
    }

    public void debugDetails(StringBuffer b) {
        b.append("RowBorder " + left + " pixels");
    }

    public void draw(Canvas canvas) {
        // icon & title
        icon.draw(canvas, 1, baseline + 1);

        // components
       super.draw(canvas);
    }

    public int getBaseline() {
        return baseline + 1;
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


class RowLayout extends AbstractBuilderDecorator {
    public RowLayout(CompositeViewBuilder design) {
        super(design);
    }

    public Size getRequiredSize(View view) {
        int height = 0;
        int width = 0;
        View[] views = view.getSubviews();
        int[] widths = ((TableColumnAxis) view.getViewAxis()).getWidths();

        for (int i = 0; i < views.length; i++) {
            View v = views[i];
            Size s = v.getRequiredSize();
            height = Math.max(height, s.getHeight());
            width += widths[i] + 4;
        }

        return new Size(width, height);
    }

    public void layout(View view) {
        int x = 0;
        int y = 0;
        View[] views = view.getSubviews();
        int[] widths = ((TableColumnAxis) view.getViewAxis()).getWidths();

        for (int i = 0; i < views.length; i++) {
            View v = views[i];
            Size s = v.getRequiredSize();
            s.setWidth(widths[i]);
            v.setSize(s);
            v.setLocation(new Location(x, y));
            x += s.getWidth() + 4;
        }

        Padding padding = view.getPadding();
        Size size = new Size(padding.getLeftRight(), y + padding.getTopBottom());
        view.setSize(size);
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
