package org.nakedobjects.viewer.skylark.table;

import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.utility.NakedObjectRuntimeException;
import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.CollectionContent;
import org.nakedobjects.viewer.skylark.Drag;
import org.nakedobjects.viewer.skylark.DragStart;
import org.nakedobjects.viewer.skylark.InternalDrag;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Shape;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;
import org.nakedobjects.viewer.skylark.special.ResizeDrag;


class TableBorder extends AbstractBorder {

    private int resizeColumn;

    public TableBorder(View view) {
        super(view);
        top = VPADDING + Style.LABEL.getTextHeight() + VPADDING;
    }

    public Drag dragStart(DragStart drag) {
        if (isOverColumnBorder(drag.getLocation())) {
            TableAxis axis = ((TableAxis) getViewAxis());
            resizeColumn = axis.getColumnBorderAt(drag.getLocation().getX());
            Bounds resizeArea = new Bounds(getView().getAbsoluteLocation(), getSize());
            resizeArea.translate(getView().getPadding().getLeft(), getView().getPadding().getTop());
            resizeArea.translate(0, -top);
            if (resizeColumn == 0) {
                resizeArea.setWidth(axis.getHeaderOffset());
            } else {
                resizeArea.translate(axis.getLeftEdge(resizeColumn - 1), 0);
                resizeArea.setWidth(axis.getColumnWidth(resizeColumn - 1));
            }

            Size minimumSize = new Size(70, 0);
            return new ResizeDrag(this, resizeArea, ResizeDrag.RIGHT, minimumSize, null);
        } else if (drag.getLocation().getY() <= getTop()) {
            return null;
        } else {
            return super.dragStart(drag);
        }
    }

    public void dragTo(InternalDrag drag) {
        if (drag.getOverlay() == null) {
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

        TableAxis axis = ((TableAxis) getViewAxis());
        int x = axis.getHeaderOffset() - 2;

        if(((CollectionContent) getContent()).getOrderByElement()) {
            drawOrderIndicator(canvas, axis,  x - 10);
        }
        
        int y = VPADDING + Style.LABEL.getAscent();


        canvas.drawLine(0, top - 1, getSize().getWidth() - 1, top - 1, Style.SECONDARY2);
        canvas.drawLine(x, 0, x, getSize().getHeight() - 1, Style.SECONDARY2);
        x++;
        int columns = axis.getColumnCount();
        NakedObjectField fieldSortOrder = ((CollectionContent) getContent()).getFieldSortOrder();
        for (int i = 0; i < columns; i++) {
            if (fieldSortOrder == axis.getFieldForColumn(i)) {
                drawOrderIndicator(canvas, axis, x + axis.getColumnWidth(i) - 10);
            }

            canvas.drawLine(x, 0, x, getSize().getHeight() - 1, Style.SECONDARY2);
            Canvas headerCanvas = canvas.createSubcanvas(x, 0, axis.getColumnWidth(i) - 1, top);
            headerCanvas.drawText(axis.getColumnName(i), HPADDING, y, Style.SECONDARY1, Style.LABEL);
            x += axis.getColumnWidth(i);
        }
        canvas.drawLine(x, 0, x, getSize().getHeight() - 1, Style.SECONDARY2);
        canvas.drawRectangle(0, getTop(), getSize().getWidth() - 1, getSize().getHeight() - top - 1, Style.SECONDARY2);
    }

    private void drawOrderIndicator(Canvas canvas, TableAxis axis, int x) {
        Shape arrow;
        arrow = new Shape();
        if (((CollectionContent) getContent()).getReverseSortOrder()) {
            arrow.addVertex(0, 7);
            arrow.addVertex(3, 0);
            arrow.addVertex(6, 7);
        } else {
            arrow.addVertex(0, 0);
            arrow.addVertex(6, 0);
            arrow.addVertex(3, 7);
        }
        // canvas.drawRectangle(x + axis.getColumnWidth(i) - 10, 3, 7, 8, Style.SECONDARY3);
        canvas.drawShape(arrow, x, 3, Style.SECONDARY2);
    }

    public void firstClick(Click click) {
        if (click.getLocation().getY() <= top) {
            TableAxis axis = ((TableAxis) getViewAxis());

            int column = axis.getColumnAt(click.getLocation().getX()) - 1;
            if (column == -2) {
                super.firstClick(click);
            } else if (column == -1) {
                ((CollectionContent) getContent()).setOrderByElement();
                invalidateContent();
            } else {
                NakedObjectField field = axis.getFieldForColumn(column);
                ((CollectionContent) getContent()).setOrderByField(field);
                invalidateContent();
            }
        } else {
            super.firstClick(click);
        }
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
        TableAxis axis = ((TableAxis) getViewAxis());
        int column = axis.getColumnBorderAt(click.getLocation().getX()) - 1;
        if (column == -2) {
            super.secondClick(click);
        } else if (column == -1) {
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
    }

    public String toString() {
        return wrappedView.toString() + "/TableBorder";
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