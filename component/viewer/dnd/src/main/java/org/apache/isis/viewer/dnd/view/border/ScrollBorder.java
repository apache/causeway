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

package org.apache.isis.viewer.dnd.view.border;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Offset;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.interaction.SimpleInternalDrag;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.ContentDrag;
import org.apache.isis.viewer.dnd.view.DragEvent;
import org.apache.isis.viewer.dnd.view.DragStart;
import org.apache.isis.viewer.dnd.view.InternalDrag;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAreaType;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.base.AbstractViewDecorator;
import org.apache.isis.viewer.dnd.view.base.NullView;
import org.apache.isis.viewer.dnd.view.option.UserActionAbstract;

/**
 * A scroll border provides a window on a larger view, providing scrollbars as a
 * way of moving the visible part of that view around the actual visible viewing
 * area. To achieve this the view is divided up into five main areas, not all of
 * which are used. In the centre is the viewing area of the underlying view. At
 * the bottom and to the right... At the top and to the left are headers that
 */
public class ScrollBorder extends AbstractViewDecorator {
    private static ScrollBarRender render;
    private static final int CENTER = 3;
    private static final int NORTH = 1;
    private static final int SOUTH = 5;
    private static final int CORNER = 0;
    private static final int SCROLLBAR_WIDTH = 16;
    private static final int WEST = 2;
    private static final int EAST = 4;

    public static void setRender(final ScrollBarRender render) {
        ScrollBorder.render = render;
    }

    private final ScrollBar horizontalScrollBar = new ScrollBar();
    private final ScrollBar verticalScrollBar = new ScrollBar();
    protected int bottom;
    protected int left;
    private View leftHeader;
    protected int right;
    private Size size = new Size();
    protected int top;
    private View topHeader;
    private int dragArea = CENTER;
    private int offsetToThumbEdge;

    public ScrollBorder(final View view) {
        this(view, new NullView(), new NullView());
    }

    /**
     * Note - the leftHeader, if it is specified, view must be the same height
     * as the content view and the rightHeader, if it is specified, must be the
     * same width.
     */
    public ScrollBorder(final View content, final View leftHeader, final View topHeader) {
        super(content);
        bottom = right = SCROLLBAR_WIDTH;
        horizontalScrollBar.setPostion(0);
        verticalScrollBar.setPostion(0);
        setLeftHeader(leftHeader);
        setTopHeader(topHeader);
    }

    public void setTopHeader(final View topHeader) {
        this.topHeader = topHeader;
        topHeader.setParent(getView());
        top = topHeader.getRequiredSize(new Size()).getHeight();
    }

    public void setLeftHeader(final View leftHeader) {
        this.leftHeader = leftHeader;
        leftHeader.setParent(getView());
        left = leftHeader.getRequiredSize(new Size()).getWidth();
    }

    private int adjust(final Click click) {
        return adjust(click.getLocation());
    }

    private int adjust(final ContentDrag drag) {
        return adjust(drag.getTargetLocation());
    }

    private int adjust(final Location location) {
        final Bounds contentArea = viewportArea();
        final Offset offset = offset();
        final int yOffset = offset.getDeltaY();
        final int xOffset = offset.getDeltaX();
        if (contentArea.contains(location)) {
            location.subtract(left, top);
            location.add(xOffset, yOffset);
            return CENTER;
        } else {
            final int x = location.getX();
            final int y = location.getY();

            if (x > contentArea.getX2() && y >= contentArea.getY() && y <= contentArea.getY2()) {
                // vertical scrollbar
                location.subtract(0, contentArea.getY());
                return EAST;
            } else if (y > contentArea.getY2() && x >= contentArea.getX() && x <= contentArea.getX2()) {
                // horzontal scrollbar
                location.subtract(contentArea.getX(), 0);
                return SOUTH;
            } else if (y < contentArea.getY() && x >= contentArea.getX() && x <= contentArea.getX2()) {
                // top border
                location.subtract(left, 0);
                location.add(xOffset, 0);
                return NORTH;
            } else if (x < contentArea.getX() && y >= contentArea.getY() && y <= contentArea.getY2()) {
                // left border
                location.subtract(0, top);
                location.add(0, yOffset);
                return WEST;
            } else {
                // ignore;
                location.setX(-1);
                location.setY(-1);
                return CORNER;
            }
        }

    }

    protected Bounds viewportArea() {
        return new Bounds(left, top, getSize().getWidth() - left - right, getSize().getHeight() - top - bottom);
    }

    @Override
    protected void debugDetails(final DebugBuilder debug) {
        super.debugDetails(debug);
        debug.append("\n           Top header: " + (topHeader == null ? "none" : topHeader.toString()));
        debug.append("\n           Left header: " + (leftHeader == null ? "none" : leftHeader.toString()));

        debug.append("\n           Vertical scrollbar ");
        debug.append("\n             offset " + top);
        debug.append("\n             position " + verticalScrollBar.getPosition());
        debug.append("\n             minimum " + verticalScrollBar.getMinimum());
        debug.append("\n             maximum " + verticalScrollBar.getMaximum());
        debug.append("\n             visible amount " + verticalScrollBar.getVisibleAmount());

        debug.append("\n           Horizontal scrollbar ");
        debug.append("\n             offset " + left);
        debug.append("\n             position " + horizontalScrollBar.getPosition());
        debug.append("\n             minimum " + horizontalScrollBar.getMinimum());
        debug.append("\n             maximum " + horizontalScrollBar.getMaximum());
        debug.append("\n             visible amount " + horizontalScrollBar.getVisibleAmount());
        debug.append("\n           Viewport area " + viewportArea());
        debug.appendln("\n           Offset " + offset());
    }

    @Override
    public void drag(final InternalDrag drag) {
        switch (dragArea) {
        case NORTH:
            drag.getLocation().subtract(offset().getDeltaX(), top);
            topHeader.drag(drag);
            break;

        case WEST:
            drag.getLocation().subtract(left, offset().getDeltaY());
            leftHeader.drag(drag);
            break;

        case CENTER:
            drag.getLocation().subtract(offset());
            wrappedView.drag(drag);
            break;

        case SOUTH:
            final int x = drag.getLocation().getX() - left;
            horizontalScrollBar.setPostion(x - offsetToThumbEdge);
            markDamaged();
            break;

        case EAST:
            final int y = drag.getLocation().getY() - top;
            verticalScrollBar.setPostion(y - offsetToThumbEdge);
            markDamaged();
            break;

        default:
            return;
        }
    }

    @Override
    public DragEvent dragStart(final DragStart drag) {
        final int area = adjust(drag);
        dragArea = area;
        switch (dragArea) {
        case NORTH:
            return topHeader.dragStart(drag);

        case WEST:
            return leftHeader.dragStart(drag);

        case CENTER:
            return wrappedView.dragStart(drag);

        case SOUTH:
            return dragStartSouth(drag);

        case EAST:
            return dragStartEast(drag);

        default:
            return null;
        }
    }

    @Override
    public void dragCancel(final InternalDrag drag) {
        switch (dragArea) {
        case NORTH:
            drag.getLocation().subtract(offset().getDeltaX(), top);
            topHeader.dragCancel(drag);
            break;

        case WEST:
            drag.getLocation().subtract(left, offset().getDeltaY());
            leftHeader.dragCancel(drag);
            break;

        case CENTER:
            drag.getLocation().subtract(offset());
            wrappedView.dragCancel(drag);
            break;
        }
    }

    @Override
    public void dragTo(final InternalDrag drag) {
        switch (dragArea) {
        case NORTH:
            drag.getLocation().subtract(offset().getDeltaX(), top);
            topHeader.dragTo(drag);
            break;

        case WEST:
            drag.getLocation().subtract(left, offset().getDeltaY());
            leftHeader.dragTo(drag);
            break;

        case CENTER:
            drag.getLocation().subtract(offset());
            wrappedView.dragTo(drag);
            break;

        case SOUTH:
        case EAST:
        default:
            // ignore

        }
    }

    @Override
    public View dragFrom(final Location location) {
        adjust(location);
        switch (dragArea) {
        case NORTH:
            return topHeader.dragFrom(location);

        case WEST:
            return leftHeader.dragFrom(location);

        case CENTER:
            return wrappedView.dragFrom(location);
        }

        return null;
    }

    @Override
    public void dragIn(final ContentDrag drag) {
        adjust(drag);
        switch (dragArea) {
        case NORTH:
            topHeader.dragIn(drag);
            break;

        case WEST:
            leftHeader.dragIn(drag);
            break;

        case CENTER:
            wrappedView.dragIn(drag);
            break;

        case SOUTH:
        case EAST:
        default:
            System.out.println(this + " ignored");

            // ignore
        }
    }

    @Override
    public void dragOut(final ContentDrag drag) {
        adjust(drag);
        switch (dragArea) {
        case NORTH:
            topHeader.dragOut(drag);
            break;

        case WEST:
            leftHeader.dragOut(drag);
            break;

        case CENTER:
            wrappedView.dragOut(drag);
            break;

        case SOUTH:
        case EAST:
        default:
            // ignore
        }
    }

    private DragEvent dragStartEast(final DragStart drag) {
        final Location location = drag.getLocation();
        final int y = location.getY();
        if (verticalScrollBar.isOnThumb(y)) {
            // offset is the distance from the left/top of the thumb to the
            // pointer
            offsetToThumbEdge = y - verticalScrollBar.getPosition();
            return new SimpleInternalDrag(this, new Offset(super.getAbsoluteLocation()));
        } else {
            return null;
        }
    }

    private DragEvent dragStartSouth(final DragStart drag) {
        final Location location = drag.getLocation();
        final int x = location.getX();
        if (horizontalScrollBar.isOnThumb(x)) {
            offsetToThumbEdge = x - horizontalScrollBar.getPosition();
            return new SimpleInternalDrag(this, new Offset(super.getAbsoluteLocation()));
        } else {
            return null;
        }
    }

    private int adjust(final DragStart drag) {
        return adjust(drag.getLocation());
    }

    @Override
    public void draw(final Canvas canvas) {
        final Bounds contents = viewportArea();
        final Offset offset = offset();
        final int x = offset.getDeltaX();
        final int y = offset.getDeltaY();

        final int contentWidth = contents.getWidth();
        final int contentHeight = contents.getHeight();

        final Canvas headerCanvasLeft = canvas.createSubcanvas(0, top, left, contentHeight);
        headerCanvasLeft.offset(0, -y);
        leftHeader.draw(headerCanvasLeft);

        final Canvas headerCanvasRight = canvas.createSubcanvas(left, 0, contentWidth, top);
        headerCanvasRight.offset(-x, 0);
        topHeader.draw(headerCanvasRight);

        final Color thumbColor = Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY2);
        drawVerticalScrollBar(canvas, contentWidth, contentHeight, thumbColor);
        drawHorizontalScrollBar(canvas, contentWidth, contentHeight, thumbColor);

        final Canvas contentCanvas = canvas.createSubcanvas(left, top, contentWidth, contentHeight);
        contentCanvas.offset(-x, -y);

        if (Toolkit.debug) {
            canvas.drawRectangle(contents.getX(), contents.getY(), contents.getWidth(), contents.getHeight(), Toolkit.getColor(ColorsAndFonts.COLOR_DEBUG_BOUNDS_DRAW));
        }

        // drawContent(canvas, contentWidth, contentHeight);
        wrappedView.draw(contentCanvas);

        if (Toolkit.debug) {
            final Size size = getSize();
            canvas.drawRectangle(0, 0, size.getWidth(), size.getHeight(), Toolkit.getColor(ColorsAndFonts.COLOR_DEBUG_BOUNDS_VIEW));
            canvas.drawLine(0, size.getHeight() / 2, size.getWidth() - 1, size.getHeight() / 2, Toolkit.getColor(ColorsAndFonts.COLOR_DEBUG_BOUNDS_VIEW));
            canvas.drawLine(0, getBaseline(), size.getWidth() - 1, getBaseline(), Toolkit.getColor(ColorsAndFonts.COLOR_DEBUG_BASELINE));
        }

    }

    // TODO merge these two methods
    private void drawVerticalScrollBar(final Canvas canvas, final int contentWidth, final int contentHeight, final Color color) {
        final int verticalVisibleAmount = verticalScrollBar.getVisibleAmount();
        final int verticalScrollPosition = verticalScrollBar.getPosition();
        if (right > 0 && (verticalScrollPosition > top || verticalVisibleAmount < contentHeight)) {
            final int x = contentWidth + left;
            render.draw(canvas, false, x, top, SCROLLBAR_WIDTH, contentHeight, verticalScrollPosition, verticalVisibleAmount);
            /*
             * canvas.drawSolidRectangle(x + 1, top, SCROLLBAR_WIDTH - 1,
             * contentHeight,
             * Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3));
             * canvas.drawSolidRectangle(x + 1, top + verticalScrollPosition,
             * SCROLLBAR_WIDTH - 2, verticalVisibleAmount, color);
             * canvas.drawRectangle(x, top, SCROLLBAR_WIDTH, contentHeight,
             * Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2));
             * canvas.drawRectangle(x + 1, top + verticalScrollPosition,
             * SCROLLBAR_WIDTH - 2, verticalVisibleAmount, Toolkit
             * .getColor(ColorsAndFonts.COLOR_SECONDARY1));
             * 
             * DrawingUtil.drawHatching(canvas, x + 3, top +
             * verticalScrollPosition + 4, SCROLLBAR_WIDTH - 6,
             * verticalVisibleAmount - 8,
             * Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY1),
             * Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY3));
             */
        }
    }

    private void drawHorizontalScrollBar(final Canvas canvas, final int contentWidth, final int contentHeight, final Color color) {
        final int horizontalScrollPosition = horizontalScrollBar.getPosition();
        final int horizontalVisibleAmount = horizontalScrollBar.getVisibleAmount();
        if (bottom > 0 && (horizontalScrollPosition > left || horizontalVisibleAmount < contentWidth)) {
            final int x = 0; // left + horizontalScrollPosition;
            final int y = contentHeight + top;
            render.draw(canvas, true, x, y, contentWidth, SCROLLBAR_WIDTH, horizontalScrollPosition, horizontalVisibleAmount);
            /*
             * canvas.drawSolidRectangle(left, y + 1, contentWidth,
             * SCROLLBAR_WIDTH - 1,
             * Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3));
             * canvas.drawSolidRectangle(x, y + 1, horizontalVisibleAmount,
             * SCROLLBAR_WIDTH - 2, color); canvas.drawRectangle(left, y,
             * contentWidth, SCROLLBAR_WIDTH,
             * Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2 ));
             * canvas.drawRectangle(x, y + 1, horizontalVisibleAmount,
             * SCROLLBAR_WIDTH - 2,
             * Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY1));
             * 
             * DrawingUtil.drawHatching(canvas, x + 5, y + 3,
             * horizontalVisibleAmount - 10, SCROLLBAR_WIDTH - 6, Toolkit
             * .getColor(ColorsAndFonts.COLOR_PRIMARY1),
             * Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY3));
             */
        }
    }

    @Override
    public void firstClick(final Click click) {
        final int area = adjust(click);
        switch (area) {
        case NORTH:
            topHeader.firstClick(click);
            break;

        case WEST:
            leftHeader.firstClick(click);
            break;

        case CENTER:
            wrappedView.firstClick(click);
            break;

        case SOUTH:
            // TODO allow modified click to move thumb to the pointer, rather
            // than paging.
            horizontalScrollBar.firstClick(click.getLocation().getX(), click.button3());
            break;

        case EAST:
            verticalScrollBar.firstClick(click.getLocation().getY(), click.button3());
            break;

        default:
            break;
        }
    }

    @Override
    public Location getAbsoluteLocation() {
        final Location location = super.getAbsoluteLocation();
        location.subtract(offset());
        return location;
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(getLocation(), getSize());
    }

    @Override
    public Size getRequiredSize(final Size maximumSize) {
        final Size size = wrappedView.getRequiredSize(new Size(maximumSize));
        if (size.getWidth() > maximumSize.getWidth()) {
            size.extendHeight(SCROLLBAR_WIDTH);
        }
        if (size.getHeight() > maximumSize.getHeight()) {
            size.extendWidth(SCROLLBAR_WIDTH);
        }
        size.extend(left, top);
        size.limitSize(maximumSize);
        return size;
    }

    @Override
    public Size getSize() {
        return new Size(size);
    }

    @Override
    public View identify(final Location location) {
        getViewManager().getSpy().addTrace(this, "mouse location within border", location);
        getViewManager().getSpy().addTrace(this, "non border area", viewportArea());

        final int area = adjust(location);
        switch (area) {
        case NORTH:
            return topHeader.identify(location);

        case WEST:
            return leftHeader.identify(location);

        case CENTER:
            return wrappedView.identify(location);

        case SOUTH:
            getViewManager().getSpy().addTrace(this, "over scroll bar area", viewportArea());
            return getView();

        case EAST:
            getViewManager().getSpy().addTrace(this, "over scroll bar area", viewportArea());
            return getView();

        default:
            return null;
        }
    }

    @Override
    public void limitBoundsWithin(final Size size) {
        super.limitBoundsWithin(size);
        verticalScrollBar.limit();
        horizontalScrollBar.limit();
    }

    @Override
    public void markDamaged(final Bounds bounds) {
        /*
         * TODO this only works for the main content area, not for the headers.
         * how do we figure out which area to adjust for?
         */
        final Offset offset = offset();
        bounds.translate(-offset.getDeltaX(), -offset.getDeltaY());
        bounds.translate(left, top);
        super.markDamaged(bounds);
    }

    @Override
    public void mouseMoved(final Location location) {
        final int area = adjust(location);
        switch (area) {
        case NORTH:
            topHeader.mouseMoved(location);
            break;

        case WEST:
            leftHeader.mouseMoved(location);
            break;

        case CENTER:
            // location.add(offset());
            // location.move(-left, -top);
            wrappedView.mouseMoved(location);
            break;

        case SOUTH:
        case EAST:
        default:
            break;
        }
    }

    private Offset offset() {
        final Bounds contents = viewportArea();
        final int width = contents.getWidth();
        final int x = width == 0 ? 0 : horizontalScrollBar.getPosition() * wrappedView.getRequiredSize(Size.createMax()).getWidth() / width;
        final int height = contents.getHeight();
        final int y = height == 0 ? 0 : verticalScrollBar.getPosition() * wrappedView.getRequiredSize(Size.createMax()).getHeight() / height;
        return new Offset(x, y);
    }

    protected boolean overContent(final Location location) {
        return viewportArea().contains(location);
    }

    public void reset() {
        horizontalScrollBar.reset();
        verticalScrollBar.reset();
    }

    /**
     * Moves the scrollbar to beginning or the end when a double click occurs on
     * that side.
     */
    @Override
    public void secondClick(final Click click) {
        final int area = adjust(click);
        switch (area) {
        case NORTH:
            topHeader.secondClick(click);
            break;

        case WEST:
            leftHeader.secondClick(click);
            break;

        case CENTER:
            wrappedView.secondClick(click);
            break;

        case SOUTH:
            horizontalScrollBar.secondClick(click.getLocation().getX());
            break;

        case EAST:
            verticalScrollBar.secondClick(click.getLocation().getY());
            break;

        default:
            break;
        }
    }

    @Override
    public void thirdClick(final Click click) {
        final int area = adjust(click);
        switch (area) {
        case NORTH:
            topHeader.thirdClick(click);
            break;

        case WEST:
            leftHeader.thirdClick(click);
            break;

        case CENTER:
            wrappedView.thirdClick(click);
            break;

        case SOUTH:
        case EAST:
        default:
            // ignore
            break;
        }
    }

    @Override
    public void setBounds(final Bounds bounds) {
        setLocation(bounds.getLocation());
        setSize(bounds.getSize());
    }

    @Override
    public void setSize(final Size size) {
        // TODO need to restore the offset after size change - see limitBounds
        // float verticalRatio = ((float) verticalScrollPosition) /
        // contentArea().getHeight();

        this.size = new Size(size);

        final Size contentSize = wrappedView.getRequiredSize(Size.createMax());
        wrappedView.setSize(contentSize);

        final int availableHeight2 = size.getHeight() - top;
        final int contentHeight2 = contentSize.getHeight();
        right = availableHeight2 >= contentHeight2 ? 0 : SCROLLBAR_WIDTH;

        final int availableWidth2 = size.getWidth() - left;
        final int contentWidth2 = contentSize.getWidth();
        bottom = availableWidth2 >= contentWidth2 ? 0 : SCROLLBAR_WIDTH;

        final Bounds viewport = viewportArea();

        final int viewportHeight = viewport.getHeight();
        final int maxContentHeight = Math.max(viewportHeight, contentSize.getHeight());

        verticalScrollBar.setSize(viewportHeight, maxContentHeight);
        if (leftHeader != null) {
            leftHeader.setSize(new Size(left, maxContentHeight));
        }

        final int viewportWidth = viewport.getWidth();
        final int maxContentWidth = Math.max(viewportWidth, contentSize.getWidth());

        horizontalScrollBar.setSize(viewportWidth, maxContentWidth);
        if (topHeader != null) {
            topHeader.setSize(new Size(maxContentWidth, top));
        }
    }

    public int getVerticalPosition() {
        return verticalScrollBar.getPosition();
    }

    public int getHorizontalPosition() {
        return horizontalScrollBar.getPosition();
    }

    @Override
    public ViewAreaType viewAreaType(final Location location) {
        final int area = adjust(location);
        switch (area) {
        case NORTH:
            return topHeader.viewAreaType(location);

        case WEST:
            return leftHeader.viewAreaType(location);

        case CENTER:
            return wrappedView.viewAreaType(location);

        case SOUTH:
        case EAST:
        default:
            return ViewAreaType.INTERNAL;
        }
    }

    @Override
    public void viewMenuOptions(final UserActionSet menuOptions) {
        super.viewMenuOptions(menuOptions);
        menuOptions.add(new UserActionAbstract("Reset scroll border", ActionType.DEBUG) {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                reset();
                invalidateLayout();
            }
        });
    }
}
