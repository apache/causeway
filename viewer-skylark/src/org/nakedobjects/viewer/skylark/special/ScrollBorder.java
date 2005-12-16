package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.ContentDrag;
import org.nakedobjects.viewer.skylark.Drag;
import org.nakedobjects.viewer.skylark.DragStart;
import org.nakedobjects.viewer.skylark.InternalDrag;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOption;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.Offset;
import org.nakedobjects.viewer.skylark.SimpleInternalDrag;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.basic.NullView;
import org.nakedobjects.viewer.skylark.core.AbstractView;
import org.nakedobjects.viewer.skylark.core.AbstractViewDecorator;


/**
 * A scroll border provides a window on a larger view, providing scrollbars as a way of moving the visible
 * part of that view around the actual visible viewing area. To achieve this the view is divided up into five
 * main areas, not all of which are used. In the centre is the viewing area of the underlying view. At the
 * bottom and to the right... At the top and to the left are headers that
 */
public class ScrollBorder extends AbstractViewDecorator {
    private static final int CENTER = 3;
    private static final int NORTH = 1;
    private static final int SOUTH = 5;
    private static final int CORNER = 0;
    private static final int SCROLLBAR_WIDTH = 16;
    private static final int WEST = 2;
    private static final int EAST = 4;

    private ScrollBar horizontalScrollBar = new ScrollBar();
    private ScrollBar verticalScrollBar = new ScrollBar();
    protected int bottom;
    protected int left;
    private View leftHeader;
    protected int right;
    private Size size = new Size();
    protected int top;
    private View topHeader;
    private int dragArea;
    private int offsetToThumbEdge;

    public ScrollBorder(View view) {
        this(view, new NullView(), new NullView());
    }

    /**
     * Note - the leftHeader, if it is specified, view must be the same height as the content view and the
     * rightHeader, if it is specified, must be the same width.
     */
    public ScrollBorder(View content, View leftHeader, View topHeader) {
        super(content);
        bottom = right = SCROLLBAR_WIDTH;
        setHorizontalPostion(0);
        setVerticalPostion(0);
        setLeftHeader(leftHeader);
        setTopHeader(topHeader);
    }

    public void setTopHeader(View topHeader) {
        this.topHeader = topHeader;
        topHeader.setParent(getView());
        top = topHeader.getRequiredSize().getHeight();
    }

    public void setLeftHeader(View leftHeader) {
        this.leftHeader = leftHeader;
        leftHeader.setParent(getView());
        left = leftHeader.getRequiredSize().getWidth();
    }

    private int adjust(Click click) {
        return adjust(click.getLocation());
    }

    private int adjust(ContentDrag drag) {
        return adjust(drag.getTargetLocation());
    }

    private int adjust(Location location) {
        Bounds contentArea = viewportArea();
        if (contentArea.contains(location)) {
            location.subtract(left, top);
            location.add(offset());
            return CENTER;
        } else {
            int x = location.getX();
            int y = location.getY();

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
                location.add(offset().getDeltaX(), 0);
                return NORTH;
            } else if (x < contentArea.getX() && y >= contentArea.getY() && y <= contentArea.getY2()) {
                // left border
                location.subtract(0, top);
                location.add(0, offset().getDeltaY());
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

    protected void debugDetails(StringBuffer b) {
        super.debugDetails(b);
        b.append("\n           Vertical scrollbar ");
        b.append("\n             offset " + top);
        b.append("\n             position " + verticalScrollBar.getPosition());
        b.append("\n             minimum " + verticalScrollBar.getMinimum());
        b.append("\n             maximum " + verticalScrollBar.getMaximum());
        b.append("\n             visible amount " + verticalScrollBar.getVisibleAmount());

        b.append("\n           Horizontal scrollbar ");
        b.append("\n             offset " + left);
        b.append("\n             position " + horizontalScrollBar.getPosition());
        b.append("\n             minimum " + horizontalScrollBar.getMinimum());
        b.append("\n             maximum " + horizontalScrollBar.getMaximum());
        b.append("\n             visible amount " + horizontalScrollBar.getVisibleAmount());
        b.append("\n           Viewport area " + viewportArea());
        b.append("\n           Offset " + offset());
    }

    public void drag(InternalDrag drag) {
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
            int x = drag.getLocation().getX() - left;
            setHorizontalPostion(x - offsetToThumbEdge);
            break;

        case EAST:
            int y = drag.getLocation().getY() - top;
            setVerticalPostion(y - offsetToThumbEdge);
            break;

        default:
            return;
        }
    }

    public Drag dragStart(DragStart drag) {
        int area = adjust(drag);
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

    public void dragCancel(InternalDrag drag) {
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

    public void dragTo(InternalDrag drag) {
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

    public View dragFrom(Location location) {
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

    public void dragIn(ContentDrag drag) {
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
        // ignore
        }
    }

    public void dragOut(ContentDrag drag) {
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

    private Drag dragStartEast(DragStart drag) {
        Location location = drag.getLocation();
        int y = location.getY();
        if (verticalScrollBar.isOnThumb(y)) {
            // offset is the distance from the left/top of the thumb to the pointer
            offsetToThumbEdge = y - verticalScrollBar.getPosition();
            return new SimpleInternalDrag(this, new Offset(super.getAbsoluteLocation()));
        } else {
            return null;
        }
    }

    private Drag dragStartSouth(DragStart drag) {
        Location location = drag.getLocation();
        int x = location.getX();
        if (horizontalScrollBar.isOnThumb(x)) {
            offsetToThumbEdge = x - horizontalScrollBar.getPosition();
            return new SimpleInternalDrag(this, new Offset(super.getAbsoluteLocation()));
        } else {
            return null;
        }
    }

    private int adjust(DragStart drag) {
        return adjust(drag.getLocation());
    }

    public void draw(Canvas canvas) {
        Bounds contents = viewportArea();
        Offset offset = offset();
        int x = offset.getDeltaX();
        int y = offset.getDeltaY();

        int contentWidth = contents.getWidth();
        int contentHeight = contents.getHeight();

        Canvas headerCanvasLeft = canvas.createSubcanvas(0, top, left, contentHeight);
        headerCanvasLeft.offset(0, -y);
        leftHeader.draw(headerCanvasLeft);

        Canvas headerCanvasRight = canvas.createSubcanvas(left, 0, contentWidth, top);
        headerCanvasRight.offset(-x, 0);
        topHeader.draw(headerCanvasRight);

        Color color = Style.PRIMARY2;
        drawHorizontalScrollBar(canvas, contentWidth, contentHeight, color);
        drawVerticalScrollBar(canvas, contentWidth, contentHeight, color);

        Canvas contentCanvas = canvas.createSubcanvas(left, top, contentWidth, contentHeight);
        contentCanvas.offset(-x, -y);

        if (AbstractView.debug) {
            canvas.drawRectangle(contents.getX(), contents.getY(), contents.getWidth(), contents.getHeight(),
                    Color.DEBUG_DRAW_BOUNDS);
        }

        // drawContent(canvas, contentWidth, contentHeight);
        wrappedView.draw(contentCanvas);

        if (AbstractView.debug) {
            Size size = getSize();
            canvas.drawRectangle(0, 0, size.getWidth() - 1, size.getHeight() - 1, Color.DEBUG_VIEW_BOUNDS);
            canvas.drawLine(0, size.getHeight() / 2, size.getWidth() - 1, size.getHeight() / 2, Color.DEBUG_VIEW_BOUNDS);
            canvas.drawLine(0, getBaseline(), size.getWidth() - 1, getBaseline(), Color.DEBUG_BASELINE);
        }

    }

    private void drawVerticalScrollBar(Canvas canvas, int contentWidth, int contentHeight, Color color) {
        int verticalVisibleAmount = verticalScrollBar.getVisibleAmount();
        int verticalScrollPosition = verticalScrollBar.getPosition();
        if (right > 0 && (verticalScrollPosition > top || verticalVisibleAmount < contentHeight)) {
            canvas.drawSolidRectangle(contentWidth + left + 1, top, SCROLLBAR_WIDTH - 2, contentHeight, Style.SECONDARY3);
            canvas.drawSolidRectangle(contentWidth + left + 1, top + verticalScrollPosition, SCROLLBAR_WIDTH - 3,
                    verticalVisibleAmount, color);
            canvas.drawRectangle(contentWidth + left, top, SCROLLBAR_WIDTH - 1, contentHeight, Style.SECONDARY2);
            canvas.drawRectangle(contentWidth + left + 1, top + verticalScrollPosition, SCROLLBAR_WIDTH - 3,
                    verticalVisibleAmount, Style.SECONDARY1);
        }
    }

    // TODO merge these two methods
/*    private void drawScrollBar(Canvas canvas, int contentWidth, int contentHeight, Color color) {
        int y = top;
        int x = contentWidth + left;
        int verticalVisibleAmount = verticalScrollBar.getVisibleAmount();
        int verticalScrollPosition = verticalScrollBar.getPosition();

        int height = verticalVisibleAmount;
        if (right > 0 && (verticalScrollPosition > y || height < contentHeight)) {
            canvas.drawSolidRectangle(x + 1, y, SCROLLBAR_WIDTH - 2, contentHeight, Style.SECONDARY3);
            canvas.drawSolidRectangle(x + 1, y + verticalScrollPosition, SCROLLBAR_WIDTH - 3, height, color);
            canvas.drawRectangle(x, y, SCROLLBAR_WIDTH - 1, contentHeight, Style.SECONDARY2);
            canvas.drawRectangle(x + 1, y + verticalScrollPosition, SCROLLBAR_WIDTH - 3, height, Style.SECONDARY1);
        }
    }
*/
    
    private void drawHorizontalScrollBar(Canvas canvas, int contentWidth, int contentHeight, Color color) {
        int horizontalScrollPosition = horizontalScrollBar.getPosition();
        int horizontalVisibleAmount = horizontalScrollBar.getVisibleAmount();
        if (bottom > 0 && (horizontalScrollPosition > left || horizontalVisibleAmount < contentWidth)) {
            canvas.drawSolidRectangle(left, contentHeight + top + 1, contentWidth, SCROLLBAR_WIDTH - 2, Style.SECONDARY3);
            canvas.drawSolidRectangle(left + horizontalScrollPosition, contentHeight + top + 1, horizontalVisibleAmount,
                    SCROLLBAR_WIDTH - 3, color);
            canvas.drawRectangle(left, contentHeight + top, contentWidth, SCROLLBAR_WIDTH - 1, Style.SECONDARY2);
            canvas.drawRectangle(left + horizontalScrollPosition, contentHeight + top + 1, horizontalVisibleAmount,
                    SCROLLBAR_WIDTH - 3, Style.SECONDARY1);
        }
    }

    public void firstClick(Click click) {
        int area = adjust(click);
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
            // TODO allow modified click to move thumb to the pointer, rather than paging.
            horizontalScrollBar.firstClick(click);
            break;

        case EAST:
            verticalScrollBar.firstClick(click);
            break;

        default:
            break;
        }
    }

    public Location getAbsoluteLocation() {
        Location location = super.getAbsoluteLocation();
        location.subtract(offset());
        return location;
    }

    public Bounds getBounds() {
        return new Bounds(getLocation(), getSize());
    }

    public Size getRequiredSize() {
        Size size = wrappedView.getRequiredSize();
        size.extend(left, top);
        size.extend(SCROLLBAR_WIDTH, SCROLLBAR_WIDTH);
        return size;
    }

    public Size getSize() {
        return new Size(size);
    }

    public View identify(Location location) {
        getViewManager().getSpy().addTrace(this, "mouse location within border", location);
        getViewManager().getSpy().addTrace(this, "non border area", viewportArea());

        int area = adjust(location);
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

    public void limitBoundsWithin(Bounds bounds) {
        super.limitBoundsWithin(bounds);
        verticalScrollBar.limit();
        horizontalScrollBar.limit();
    }

    public void markDamaged(Bounds bounds) {
        /*
         * TODO this only works for the main content area, not for the headers. how do we figure out which
         * area to adjust for?
         */
        Offset offset = offset();
        bounds.translate(-offset.getDeltaX(), -offset.getDeltaY());
        bounds.translate(left, top);
        super.markDamaged(bounds);
    }

    public void mouseMoved(Location location) {
        int area = adjust(location);
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
        Bounds contents = viewportArea();
        int x = horizontalScrollBar.getPosition() * wrappedView.getRequiredSize().getWidth() / contents.getWidth();
        int y = verticalScrollBar.getPosition() * wrappedView.getRequiredSize().getHeight() / contents.getHeight();
        return new Offset(x, y);
    }

    protected boolean overContent(Location location) {
        return viewportArea().contains(location);
    }

    public void reset() {
        horizontalScrollBar.reset();
        verticalScrollBar.reset();
    }

    /**
     * Moves the scrollbar to beginning or the end when a double click occurs on that side.
     */
    public void secondClick(Click click) {
        int area = adjust(click);
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
            secondClickSouth(click);
            break;

        case EAST:
            secondClickEast(click);
            break;

        default:
            break;
        }
    }

    private void secondClickEast(Click click) {
        int y = click.getLocation().getY();
        int midpoint = (getSize().getHeight() - top - bottom) / 2;
        int position = (y < midpoint) ? verticalScrollBar.getMinimum() : verticalScrollBar.getMaximum();
        setVerticalPostion(position);
    }

    private void secondClickSouth(Click click) {
        int x = click.getLocation().getX();
        int midpoint = (getSize().getWidth() - left - right) / 2;
        int position = (x < midpoint) ? horizontalScrollBar.getMinimum() : horizontalScrollBar.getMaximum();
        setHorizontalPostion(position);
    }

    public void thirdClick(Click click) {
        int area = adjust(click);
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

    public void setBounds(Bounds bounds) {
        setLocation(bounds.getLocation());
        setSize(bounds.getSize());
    }

    public void setHorizontalPostion(final int position) {
        getViewManager().addSpyAction("Move to horizontal position " + position);
        horizontalScrollBar.setHorizontalPostion(position);
        markDamaged();
    }

    public void setRequiredSize(Size size) {
        Size wrappedSize = new Size(size);
        wrappedSize.contract(left, top);
        wrappedView.setRequiredSize(wrappedSize);
    }

    public void setSize(Size size) {
        // TODO need to restore the offset after size change - see limitBounds
        // float verticalRatio = ((float) verticalScrollPosition) /
        // contentArea().getHeight();

        this.size = new Size(size);

        Size contentSize = wrappedView.getRequiredSize();
        wrappedView.setSize(contentSize);

        
        int availableHeight2 = size.getHeight() - top;
        int contentHeight2 = contentSize.getHeight();
        right = availableHeight2 >= contentHeight2 ? 0 : SCROLLBAR_WIDTH;

        int availableWidth2 = size.getWidth() - left;
        int contentWidth2 = contentSize.getWidth();
        bottom = availableWidth2 >= contentWidth2 ? 0 : SCROLLBAR_WIDTH;


        Bounds viewport = viewportArea();

        
        int viewportHeight = viewport.getHeight();
        int maxContentHeight = Math.max(viewportHeight, contentSize.getHeight());

        verticalScrollBar.setSize(viewportHeight, maxContentHeight);
        if (leftHeader != null) {
            leftHeader.setSize(new Size(left, maxContentHeight));
        }

        int viewportWidth = viewport.getWidth();
        int maxContentWidth = Math.max(viewportWidth, contentSize.getWidth());
        
        horizontalScrollBar.setSize(viewportWidth, maxContentWidth);
        if (topHeader != null) {
            topHeader.setSize(new Size(maxContentWidth, top));
        }
        
  //     verticalScrollBar.limit();
    }

    public void setVerticalPostion(final int position) {
        getViewManager().addSpyAction("Move to vertical position " + position);
        verticalScrollBar.setHorizontalPostion(position);
        markDamaged();
    }

    public int getVerticalPosition() {
        return verticalScrollBar.getPosition();
    }

    public int getHorizontalPosition() {
        return horizontalScrollBar.getPosition();
    }
    
    public ViewAreaType viewAreaType(Location location) {
        int area = adjust(location);
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
    
    public void viewMenuOptions(MenuOptionSet menuOptions) {
        super.viewMenuOptions(menuOptions);

        menuOptions.add(MenuOptionSet.VIEW, new MenuOption("Reset scroll border") {

            public void execute(Workspace workspace, View view, Location at) {
                reset();
                invalidateLayout();
            }
        });
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