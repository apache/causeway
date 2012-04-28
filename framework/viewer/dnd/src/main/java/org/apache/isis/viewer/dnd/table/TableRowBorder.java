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

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Offset;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.interaction.ViewDragImpl;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.DragEvent;
import org.apache.isis.viewer.dnd.view.DragStart;
import org.apache.isis.viewer.dnd.view.Placement;
import org.apache.isis.viewer.dnd.view.SubviewDecorator;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAreaType;
import org.apache.isis.viewer.dnd.view.ViewAxis;
import org.apache.isis.viewer.dnd.view.ViewConstants;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;
import org.apache.isis.viewer.dnd.view.base.DragViewOutline;
import org.apache.isis.viewer.dnd.view.base.IconGraphic;
import org.apache.isis.viewer.dnd.view.collection.CollectionContent;
import org.apache.isis.viewer.dnd.view.text.ObjectTitleText;
import org.apache.isis.viewer.dnd.view.text.TitleText;

// REVIEW can we use ObjectBorder to provide the basic functionality
public class TableRowBorder extends AbstractBorder {
    public static class Factory implements SubviewDecorator {
        @Override
        public ViewAxis createAxis(final Content content) {
            final TableAxis axis = new TableAxisImpl((CollectionContent) content);
            return axis;
        }

        @Override
        public View decorate(final Axes axes, final View view) {
            return new TableRowBorder(axes, view);
        }
    }

    private static final int BORDER = 13;

    private final int baseline;
    private final IconGraphic icon;
    private final TitleText title;

    private final TableAxis axis;

    public TableRowBorder(final Axes axes, final View wrappedRow) {
        super(wrappedRow);

        final Text text = Toolkit.getText(ColorsAndFonts.TEXT_NORMAL);
        icon = new IconGraphic(this, text);
        title = new ObjectTitleText(this, text);
        baseline = icon.getBaseline();

        left = requiredTitleWidth() + BORDER;

        axis = axes.getAxis(TableAxis.class);
        axis.ensureOffset(left);
    }

    @Override
    public void debugDetails(final DebugBuilder debug) {
        debug.appendln("RowBorder " + left + " pixels");
        debug.appendln("Axis", axis);
    }

    @Override
    public DragEvent dragStart(final DragStart drag) {
        final int x = drag.getLocation().getX();
        final int left = axis.getHeaderOffset();
        ;
        if (x < left - BORDER) {
            return Toolkit.getViewFactory().createDragContentOutline(this, drag.getLocation());
        } else if (x < left) {
            final View dragOverlay = new DragViewOutline(getView());
            return new ViewDragImpl(this, new Offset(drag.getLocation()), dragOverlay);
        } else {
            return super.dragStart(drag);
        }
    }

    @Override
    public void draw(final Canvas canvas) {
        final int baseline = getBaseline();

        final int width = axis.getHeaderOffset();
        final Size s = getSize();
        final Canvas subcanvas = canvas.createSubcanvas(0, 0, width, s.getHeight());
        int offset = ViewConstants.HPADDING;
        icon.draw(subcanvas, offset, baseline);
        offset += icon.getSize().getWidth() + ViewConstants.HPADDING + 0 + ViewConstants.HPADDING;
        title.draw(subcanvas, offset, baseline, getLeft() - offset);

        final int columns = axis.getColumnCount();
        int x = -1;
        x += axis.getHeaderOffset();
        final Color secondary1 = Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY1);
        canvas.drawLine(x - 1, 0, x - 1, s.getHeight() - 1, secondary1);
        canvas.drawLine(x, 0, x, s.getHeight() - 1, secondary1);
        for (int i = 0; i < columns; i++) {
            x += axis.getColumnWidth(i);
            canvas.drawLine(x, 0, x, s.getHeight() - 1, secondary1);
        }
        canvas.drawLine(0, 0, 0, s.getHeight() - 1, secondary1);

        final int y = s.getHeight() - 1;
        final Color secondary2 = Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2);
        canvas.drawLine(0, y, s.getWidth(), y, secondary2);

        if (getState().isObjectIdentified()) {
            final int xExtent = width - 1;
            canvas.drawLine(xExtent - BORDER, top, xExtent - BORDER, top + s.getHeight() - 1, secondary2);
            canvas.drawSolidRectangle(xExtent - BORDER + 1, top, BORDER - 2, s.getHeight() - 2 * top - 1, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3));
        }

        // components
        super.draw(canvas);
    }

    @Override
    public int getBaseline() {
        return baseline;
    }

    @Override
    protected int getLeft() {
        return axis.getHeaderOffset();
    }

    protected int requiredTitleWidth() {
        return ViewConstants.HPADDING + icon.getSize().getWidth() + ViewConstants.HPADDING + title.getSize().getWidth() + ViewConstants.HPADDING;
    }

    @Override
    public void entered() {
        getState().setContentIdentified();
        getState().setViewIdentified();
        wrappedView.entered();
        markDamaged();
    }

    @Override
    public void exited() {
        getState().clearObjectIdentified();
        getState().clearViewIdentified();
        wrappedView.exited();
        markDamaged();
    }

    @Override
    public void secondClick(final Click click) {
        final int left = axis.getHeaderOffset();
        ;
        final int x = click.getLocation().getX();
        if (x <= left) {
            final Location location = getAbsoluteLocation();
            location.translate(click.getLocation());
            getWorkspace().objectActionResult(getContent().getAdapter(), new Placement(this));
        } else {
            super.secondClick(click);
        }
    }

    @Override
    public String toString() {
        return "RowBorder/" + wrappedView;
    }

    @Override
    public ViewAreaType viewAreaType(final Location mouseLocation) {
        if (mouseLocation.getX() <= left) {
            return ViewAreaType.CONTENT;
        } else if (mouseLocation.getX() >= getSize().getWidth() - right) {
            return ViewAreaType.VIEW;
        } else {
            return super.viewAreaType(mouseLocation);
        }
    }
}
