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
import org.nakedobjects.viewer.skylark.basic.ObjectTitleText;
import org.nakedobjects.viewer.skylark.basic.TitleText;
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

            ViewSpecification cellSpec;

            if (content instanceof OneToManyField) {
                return null;
            } else if (content instanceof ValueField) {
                cellSpec = factory.getValueFieldSpecification((ValueContent) content);
            } else if (content instanceof ObjectContent) {
                cellSpec = factory.getIconizedSubViewSpecification((ObjectContent) content);
            } else {
                throw new NakedObjectRuntimeException();
            }

            return cellSpec.createView(content, axis);
        }

        public View decorateSubview(View cell) {
//            return new TableCellResizeBorder(cell);
            return cell;
        }
    }
}


class RowBorder extends AbstractBorder {
    private static final int HANDLE_WIDTH = 20;
    private int baseline;
    private IconGraphic icon;
    private TitleText title;

    public RowBorder(View wrappedRow) {
        super(wrappedRow);

        icon = new IconGraphic(this, Style.NORMAL);
        title = new ObjectTitleText(this, Style.NORMAL);
        baseline = icon.getBaseline();

        left = icon.getSize().getWidth() + HPADDING + title.getSize().getWidth();
        
        ((TableColumnAxis) wrappedRow.getViewAxis()).ensureOffset(left);

        right = HANDLE_WIDTH;
    }

    protected int getLeft() {
        return ((TableColumnAxis) wrappedView.getViewAxis()).getHeaderOffset();
    }
    
    public void debugDetails(StringBuffer b) {
        b.append("RowBorder " + left + " pixels");
    }

    public void draw(Canvas canvas) {
        int bl = getBaseline();
        icon.draw(canvas, 1, bl);
        title.draw(canvas, icon.getSize().getWidth() + HPADDING, bl);
        int y = getSize().getHeight() - 1;
        canvas.drawLine(0, y, getSize().getWidth(), y, Style.SECONDARY2);
         
 /*
        int l = getLeft() - 1;
        canvas.drawLine(0, 0, l, 0, Style.SECONDARY2);
        canvas.drawLine(l, 0, l, getSize().getHeight(), Style.SECONDARY2);
        l--;
        canvas.drawLine(l, 0, l, getSize().getHeight(), Style.SECONDARY2);
     */
        
        // components
       super.draw(canvas);
    }

    public int getBaseline() {
        return baseline;
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

    public Size getRequiredSize(View row) {
        int height = 0;
        int width = 0;
        View[] cells = row.getSubviews();
        TableColumnAxis axis = ((TableColumnAxis) row.getViewAxis());

        int maxBaseline = 0;
        for (int i = 0; i < cells.length; i++) {
            View cell = cells[i];
            maxBaseline = Math.max(maxBaseline, cell.getBaseline());
        }

        for (int i = 0; i < cells.length; i++) {
            View cell = cells[i];
            Size s = cell.getRequiredSize();
            int b = cell.getBaseline();
            height = Math.max(height, s.getHeight()+ (b < maxBaseline ? maxBaseline - b: 0));
            width += axis.getColumnWidth(i);
        }

        return new Size(width, height);
    }

    public void layout(View row) {
        int x = 0;
        int y = 0;
        
        View[] cells = row.getSubviews();
        TableColumnAxis axis = ((TableColumnAxis) row.getViewAxis());

        int maxBaseline = 0;
        for (int i = 0; i < cells.length; i++) {
            View cell = cells[i];
            maxBaseline = Math.max(maxBaseline, cell.getBaseline());
        }
        
        for (int i = 0; i < cells.length; i++) {
            View cell = cells[i];
            Size s = cell.getRequiredSize();
            int b = cell.getBaseline();
            
            s.setWidth(axis.getColumnWidth(i));
            cell.setSize(s);
            cell.setLocation(new Location(x, y + (b < maxBaseline ? maxBaseline - b: 0)));
            x += s.getWidth();
        }

        Padding padding = row.getPadding();
        Size size = new Size(padding.getLeftRight(), y + padding.getTopBottom());
        row.setSize(size);
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
