package org.nakedobjects.viewer.skylark.table;

import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Drag;
import org.nakedobjects.viewer.skylark.DragStart;
import org.nakedobjects.viewer.skylark.InternalDrag;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.core.AbstractView;
import org.nakedobjects.viewer.skylark.special.ResizeDrag;


public class TableHeader extends AbstractView {
    private int height;
    private int resizeColumn;

    public TableHeader(ViewAxis axis) {
        super(null, null, axis);
        height = VPADDING + Style.LABEL.getTextHeight() + VPADDING;
    }

    public Size getRequiredSize() {
        return new Size(-1,  height);
    }
    
    public Drag dragStart(DragStart drag) {
        if (isOverColumnBorder(drag.getLocation())) {
            TableAxis axis = ((TableAxis) getViewAxis());
            resizeColumn = axis.getColumnBorderAt(drag.getLocation().getX());
            Bounds resizeArea = new Bounds(getView().getAbsoluteLocation(), getSize());
            resizeArea.translate(getView().getPadding().getLeft(), getView().getPadding().getTop());
            if (resizeColumn == 0) {
                resizeArea.setWidth(axis.getHeaderOffset());
            } else {
                resizeArea.translate(axis.getLeftEdge(resizeColumn - 1), 0);
                resizeArea.setWidth(axis.getColumnWidth(resizeColumn - 1));
            }

            Size minimumSize = new Size(70, 0);
            return new ResizeDrag(this, resizeArea, ResizeDrag.RIGHT, minimumSize, null);
        } else if (drag.getLocation().getY() <= height) {
            return null;
        } else {
            return super.dragStart(drag);
        }
    }

    public void dragTo(InternalDrag drag) {
         if(drag.getOverlay() == null) {
            throw new NakedObjectRuntimeException("No overlay for drag: " + drag);
        }
        int newWidth = drag.getOverlay().getSize().getWidth();
        newWidth = Math.max(70, newWidth);
        getViewManager().getSpy().addAction("Resize column to " + newWidth);

        TableAxis axis = ((TableAxis) getViewAxis());
        if (resizeColumn == 0) {
            axis.setOffset(newWidth);
        } else {
            axis.setWidth(resizeColumn - 1, newWidth);
        }
        axis.invalidateLayout();
    }

    public void draw(Canvas canvas) {
        super.draw(canvas.createSubcanvas());

        int y = VPADDING + Style.LABEL.getAscent();

        TableAxis axis = ((TableAxis) getViewAxis());

        int x = axis.getHeaderOffset() - 2;
        canvas.drawLine(0, height - 1, getSize().getWidth() - 1, height - 1, Style.BLACK);
        canvas.drawLine(x, 0, x, getSize().getHeight() - 1, Style.BLACK);
        x++;
        int columns = axis.getColumnCount();
        for (int i = 0; i < columns; i++) {
            canvas.drawLine(x, 0, x, getSize().getHeight() - 1, Style.BLACK);
            Canvas headerCanvas = canvas.createSubcanvas(x, 0, axis.getColumnWidth(i) - 1, height);
            headerCanvas.drawText(axis.getColumnName(i), HPADDING, y, Style.SECONDARY1, Style.LABEL);
            x += axis.getColumnWidth(i);
        }
        canvas.drawLine(x, 0, x, getSize().getHeight() - 1, Style.SECONDARY2);
        canvas.drawRectangle(0, height, getSize().getWidth() - 1, getSize().getHeight() - height - 1, Style.SECONDARY2);
    }

    public View identify(Location location) {
        getViewManager().getSpy().addTrace("Identify over column " + location);
        if (isOverColumnBorder(location)) {
            getViewManager().getSpy().addAction("Identified over column ");
            return getView();
        }
        return super.identify(location);
    }

    private boolean isOverColumnBorder(Location at) {
        int x = at.getX();
        TableAxis axis = ((TableAxis) getViewAxis());
        return axis.getColumnBorderAt(x) >= 0;
    }

    public void mouseMoved(Location at) {
        if (isOverColumnBorder(at)) {
            getViewManager().showResizeRightCursor();
        } else {
            super.mouseMoved(at);
            getViewManager().showDefaultCursor();
        }
    }

    public void secondClick(Click click) {
        if (isOverColumnBorder(click.getLocation())) {
            TableAxis axis = ((TableAxis) getViewAxis());
            int column = axis.getColumnBorderAt(click.getLocation().getX()) - 1;
            if (column == -1) {
                View[] subviews = getSubviews();
                for (int i = 0; i < subviews.length; i++) {
                    View row = subviews[i];
                    axis.ensureOffset(((TableRowBorder) row).requiredTitleWidth());
                }

            } else {
                View[] subviews = getSubviews();
                int max = 0;
                for (int i = 0; i < subviews.length; i++) {
                    View row = subviews[i];
                    View cell = row.getSubviews()[column];
                    max = Math.max(max, cell.getRequiredSize().getWidth());
                }
                axis.setWidth(column, max);
            }
            axis.invalidateLayout();
        } else {
            super.secondClick(click);
        }
    }

    public String toString() {
        return "TableHeader";
    }

    public ViewAreaType viewAreaType(Location at) {
        int x = at.getX();
        TableAxis axis = ((TableAxis) getViewAxis());

        if (axis.getColumnBorderAt(x) >= 0) {
            return ViewAreaType.INTERNAL;
        } else {
            return super.viewAreaType(at);
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