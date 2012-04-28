/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.viewer.dnd.table;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Shape;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.DragEvent;
import org.apache.isis.viewer.dnd.view.DragStart;
import org.apache.isis.viewer.dnd.view.InternalDrag;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAreaType;
import org.apache.isis.viewer.dnd.view.ViewConstants;
import org.apache.isis.viewer.dnd.view.base.AbstractView;
import org.apache.isis.viewer.dnd.view.border.ResizeDrag;
import org.apache.isis.viewer.dnd.view.collection.CollectionContent;

public class TableHeader extends AbstractView {
    private final TableAxis axis;
    private final int height;
    private int resizeColumn;

    public TableHeader(final Content content, final TableAxis axis) {
        super(content, null);
        this.axis = axis;
        height = ViewConstants.VPADDING + Toolkit.getText(ColorsAndFonts.TEXT_LABEL).getTextHeight() + ViewConstants.VPADDING;
    }

    @Override
    public void firstClick(final Click click) {
        if (click.getLocation().getY() <= height) {
            final int column = axis.getColumnAt(click.getLocation().getX()) - 1;
            if (column == -2) {
                super.firstClick(click);
            } else if (column == -1) {
                ((CollectionContent) getContent()).setOrderByElement();
                invalidateContent();
            } else {
                final ObjectAssociation field = axis.getFieldForColumn(column);
                ((CollectionContent) getContent()).setOrderByField(field);
                invalidateContent();
            }
        } else {
            super.firstClick(click);
        }
    }

    @Override
    public void invalidateContent() {
        getParent().invalidateContent();
    }

    @Override
    public Size getRequiredSize(final Size availableSpace) {
        return new Size(-1, height);
    }

    @Override
    public DragEvent dragStart(final DragStart drag) {
        if (isOverColumnBorder(drag.getLocation())) {
            resizeColumn = axis.getColumnBorderAt(drag.getLocation().getX());
            final Bounds resizeArea = new Bounds(getView().getAbsoluteLocation(), getSize());
            resizeArea.translate(getView().getPadding().getLeft(), getView().getPadding().getTop());
            if (resizeColumn == 0) {
                resizeArea.setWidth(axis.getHeaderOffset());
            } else {
                resizeArea.translate(axis.getLeftEdge(resizeColumn - 1), 0);
                resizeArea.setWidth(axis.getColumnWidth(resizeColumn - 1));
            }

            final Size minimumSize = new Size(70, 0);
            return new ResizeDrag(this, resizeArea, ResizeDrag.RIGHT, minimumSize, null);
        } else if (drag.getLocation().getY() <= height) {
            return null;
        } else {
            return super.dragStart(drag);
        }
    }

    @Override
    public void dragTo(final InternalDrag drag) {
        if (drag.getOverlay() == null) {
            throw new IsisException("No overlay for drag: " + drag);
        }
        int newWidth = drag.getOverlay().getSize().getWidth();
        newWidth = Math.max(70, newWidth);
        getViewManager().getSpy().addAction("Resize column to " + newWidth);

        if (resizeColumn == 0) {
            axis.setOffset(newWidth);
        } else {
            axis.setWidth(resizeColumn - 1, newWidth);
        }
        axis.invalidateLayout();
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas.createSubcanvas());

        final int y = ViewConstants.VPADDING + Toolkit.getText(ColorsAndFonts.TEXT_LABEL).getAscent();

        int x = axis.getHeaderOffset() - 2;

        if (((CollectionContent) getContent()).getOrderByElement()) {
            drawOrderIndicator(canvas, axis, x - 10);
        }

        final Color secondary1 = Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY1);
        canvas.drawLine(0, 0, getSize().getWidth() - 1, 0, secondary1);
        canvas.drawLine(0, height - 1, getSize().getWidth() - 1, height - 1, secondary1);
        canvas.drawLine(x, 0, x, getSize().getHeight() - 1, secondary1);
        x++;
        final int columns = axis.getColumnCount();
        final ObjectAssociation fieldSortOrder = ((CollectionContent) getContent()).getFieldSortOrder();
        for (int i = 0; i < columns; i++) {
            if (fieldSortOrder == axis.getFieldForColumn(i)) {
                drawOrderIndicator(canvas, axis, x + axis.getColumnWidth(i) - 10);
            }

            canvas.drawLine(0, 0, 0, getSize().getHeight() - 1, secondary1);
            canvas.drawLine(x, 0, x, getSize().getHeight() - 1, secondary1);
            final Canvas headerCanvas = canvas.createSubcanvas(x, 0, axis.getColumnWidth(i) - 1, height);
            headerCanvas.drawText(axis.getColumnName(i), ViewConstants.HPADDING, y, secondary1, Toolkit.getText(ColorsAndFonts.TEXT_LABEL));
            x += axis.getColumnWidth(i);
        }
        // Color secondary2 = Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2);
        // canvas.drawLine(x, 0, x, getSize().getHeight() - 1, secondary2);
        // canvas.drawRectangle(0, height, getSize().getWidth() - 1,
        // getSize().getHeight() - height - 1, secondary2);
    }

    private void drawOrderIndicator(final Canvas canvas, final TableAxis axis, final int x) {
        Shape arrow;
        arrow = new Shape();
        if (((CollectionContent) getContent()).getReverseSortOrder()) {
            arrow.addPoint(0, 7);
            arrow.addPoint(3, 0);
            arrow.addPoint(6, 7);
        } else {
            arrow.addPoint(0, 0);
            arrow.addPoint(6, 0);
            arrow.addPoint(3, 7);
        }
        // canvas.drawRectangle(x + axis.getColumnWidth(i) - 10, 3, 7, 8,
        // Toolkit.getColor("secondary3"));
        canvas.drawShape(arrow, x, 3, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2));
    }

    @Override
    public View identify(final Location location) {
        getViewManager().getSpy().addTrace("Identify over column " + location);
        if (isOverColumnBorder(location)) {
            getViewManager().getSpy().addAction("Identified over column ");
            return getView();
        }
        return super.identify(location);
    }

    private boolean isOverColumnBorder(final Location at) {
        final int x = at.getX();
        return axis.getColumnBorderAt(x) >= 0;
    }

    @Override
    public void mouseMoved(final Location at) {
        if (isOverColumnBorder(at)) {
            getFeedbackManager().showResizeRightCursor();
        } else {
            super.mouseMoved(at);
            getFeedbackManager().showDefaultCursor();
        }
    }

    @Override
    public void secondClick(final Click click) {
        if (isOverColumnBorder(click.getLocation())) {
            final int column = axis.getColumnBorderAt(click.getLocation().getX()) - 1;
            if (column == -1) {
                final View[] subviews = getSubviews();
                for (final View row : subviews) {
                    axis.ensureOffset(((TableRowBorder) row).requiredTitleWidth());
                }

            } else {
                final View[] subviews = getSubviews();
                int max = 0;
                for (final View row : subviews) {
                    final View cell = row.getSubviews()[column];
                    max = Math.max(max, cell.getRequiredSize(new Size()).getWidth());
                }
                axis.setWidth(column, max);
            }
            axis.invalidateLayout();
        } else {
            super.secondClick(click);
        }
    }

    @Override
    public String toString() {
        return "TableHeader";
    }

    @Override
    public ViewAreaType viewAreaType(final Location at) {
        final int x = at.getX();

        if (axis.getColumnBorderAt(x) >= 0) {
            return ViewAreaType.INTERNAL;
        } else {
            return super.viewAreaType(at);
        }
    }
}
