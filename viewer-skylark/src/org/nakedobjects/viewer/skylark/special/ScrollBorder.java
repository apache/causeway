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
import org.nakedobjects.viewer.skylark.Offset;
import org.nakedobjects.viewer.skylark.SimpleInternalDrag;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.basic.NullView;
import org.nakedobjects.viewer.skylark.core.AbstractView;
import org.nakedobjects.viewer.skylark.core.AbstractViewDecorator;


/**
 * A scroll border provides a window on a larger view, providing scrollbars as a way of moving the
 * visible part of that view around the actual visible viewing area. To achieve this the view is
 * divided up into five main areas, not all of which are used. In the centre is the viewing area of
 * the underlying view. At the bottom and to the right... At the top and to the left are headers
 * that
 */
public class ScrollBorder extends AbstractViewDecorator {
    private static final int CENTER = 3;
    private static final int NORTH = 1;
    private static final int SOUTH = 5;
    private static final int CORNER = 0;
    private static final int SCROLLBAR_WIDTH = 16;
    private static final int WEST = 2;
    private static final int EAST = 4;

    protected int bottom;
    private int horizontalMaximum;
    private int horizontalMinimum;
    private int horizontalScrollPosition = 0;
    private int horizontalVisibleAmount;
    protected int left;

    private View leftHeader;
    protected int right;
    private Size size = new Size();
    protected int top;
    private View topHeader;
    private int verticalMaximum;
    private int verticalMinimum;
    private int verticalScrollPosition = 0;
    private int verticalVisibleAmount;
    private int dragArea;
    private int offsetToThumbEdge;

    public ScrollBorder(View view) {
        this(view, new NullView(), new NullView());
    }

    /**
     * Note - the leftHeader, if it is specified, view must be the same height as the content view
     * and the rightHeader, if it is specified, must be the same width.
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
        Bounds contentArea = contentArea();
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

    protected Bounds contentArea() {
        
     /*   
        int availableHeight = getSize().getHeight() - top;
        int contentHeight = wrappedView.getRequiredSize().getHeight();
  
        int width = getSize().getWidth() - left;
  //      int height = getSize().getHeight() - top;
     
        bottom =  availableHeight > contentHeight ? 0 : 16;
        return new Bounds(left, top, width - right, availableHeight - (availableHeight > contentHeight ? 0 : bottom));

        */
        
              return new Bounds(left, top, getSize().getWidth() - left - right, getSize().getHeight() - top - bottom);
    }

    protected void debugDetails(StringBuffer b) {
        super.debugDetails(b);
        b.append("\n           Vertical scrollbar ");
        b.append("\n             offset " + top);
        b.append("\n             position " + verticalScrollPosition);
        b.append("\n             minimum " + verticalMinimum);
        b.append("\n             maximum " + verticalMaximum);
        b.append("\n             visible amount " + verticalVisibleAmount);

        b.append("\n           Horizontal scrollbar ");
        b.append("\n             offset " + left);
        b.append("\n             position " + horizontalScrollPosition);
        b.append("\n             minimum " + horizontalMinimum);
        b.append("\n             maximum " + horizontalMaximum);
        b.append("\n             visible amount " + horizontalVisibleAmount);
        b.append("\n           Offset " + offset());
        b.append("\n           Content area " + contentArea());
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
        if (y > verticalScrollPosition && y < verticalScrollPosition + verticalVisibleAmount) {
            // offset is the distance from the left/top of the thumb to the pointer
            offsetToThumbEdge = y - verticalScrollPosition;
            return new SimpleInternalDrag(this, new Offset(super.getAbsoluteLocation()));
        } else {
            return null;
        }
    }

    private Drag dragStartSouth(DragStart drag) {
        Location location = drag.getLocation();
        int x = location.getX();
        if (x > horizontalScrollPosition && x < horizontalScrollPosition + horizontalVisibleAmount) {
            offsetToThumbEdge = x - horizontalScrollPosition;
            return new SimpleInternalDrag(this, new Offset(super.getAbsoluteLocation()));
        } else {
            return null;
        }
    }

    private int adjust(DragStart drag) {
        return adjust(drag.getLocation());
    }

    public void draw(Canvas canvas) {
        Bounds contents = contentArea();
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

        drawScrollBars(canvas, contentWidth, contentHeight);

        Canvas contentCanvas = canvas.createSubcanvas(left, top, contentWidth, contentHeight);
        contentCanvas.offset(-x, -y);

        if (AbstractView.debug) {
            canvas.drawRectangle(contents.getX(), contents.getY(), contents.getWidth(), contents.getHeight(),
                    Color.DEBUG_DRAW_BOUNDS);
        }

        //        drawContent(canvas, contentWidth, contentHeight);
        wrappedView.draw(contentCanvas);

        if (AbstractView.debug) {
            Size size = getSize();
            canvas.drawRectangle(0, 0, size.getWidth() - 1, size.getHeight() - 1, Color.DEBUG_VIEW_BOUNDS);
            canvas.drawLine(0, size.getHeight() / 2, size.getWidth() - 1, size.getHeight() / 2, Color.DEBUG_VIEW_BOUNDS);
            canvas.drawLine(0, getBaseline(), size.getWidth() - 1, getBaseline(), Color.DEBUG_BASELINE);
        }

    }

    private void drawContent(Canvas canvas, int contentWidth, int contentHeight) {
        Offset offset = offset();
        int x = offset.getDeltaX();
        int y = offset.getDeltaY();
        Canvas subCanvas = canvas.createSubcanvas(left, top, contentWidth, contentHeight);
        subCanvas.offset(-x, -y);
        wrappedView.draw(subCanvas);

        if (AbstractView.debug) {
            subCanvas.drawRectangle(0, 0, contentWidth, contentHeight, Color.DEBUG_DRAW_BOUNDS);
        }
    }

    private void drawScrollBars(Canvas canvas, int contentWidth, int contentHeight) {
        Color color = Style.PRIMARY2;

        // horizontal scrollbar
        if (bottom > 0 && (horizontalScrollPosition > left || horizontalVisibleAmount < contentWidth)) {
            canvas.drawSolidRectangle(left, contentHeight + top + 1, contentWidth, SCROLLBAR_WIDTH - 2, Style.SECONDARY3);
            canvas.drawSolidRectangle(left + horizontalScrollPosition, contentHeight + top + 1, horizontalVisibleAmount,
                    SCROLLBAR_WIDTH - 3, color);
            canvas.drawRectangle(left, contentHeight + top, contentWidth, SCROLLBAR_WIDTH - 1, Style.SECONDARY2);
            canvas.drawRectangle(left + horizontalScrollPosition, contentHeight + top + 1, horizontalVisibleAmount,
                    SCROLLBAR_WIDTH - 3, Style.SECONDARY1);
        }

        // vertical scrollbar
        if (right > 0 && (verticalScrollPosition > top || verticalVisibleAmount < contentHeight)) {
            canvas.drawSolidRectangle(contentWidth + left + 1, top, SCROLLBAR_WIDTH - 2, contentHeight, Style.SECONDARY3);
            canvas.drawSolidRectangle(contentWidth + left + 1, top + verticalScrollPosition, SCROLLBAR_WIDTH - 3,
                    verticalVisibleAmount, color);
            canvas.drawRectangle(contentWidth + left, top, SCROLLBAR_WIDTH - 1, contentHeight, Style.SECONDARY2);
            canvas.drawRectangle(contentWidth + left + 1, top + verticalScrollPosition, SCROLLBAR_WIDTH - 3,
                    verticalVisibleAmount, Style.SECONDARY1);
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
            firstClickSouth(click);
            break;

        case EAST:
            firstClickEast(click);
            break;

        default:
            break;
        }
    }

    private void firstClickEast(Click click) {
        Location location2 = click.getLocation();
        int y = location2.getY();
        if (click.button3()) {
            setVerticalPostion(y - verticalVisibleAmount / 2);
        } else if (click.button1()) {
            if (y < verticalScrollPosition) {
                setVerticalPostion(verticalScrollPosition - verticalVisibleAmount);
            } else if (y > verticalScrollPosition + verticalVisibleAmount) {
                setVerticalPostion(verticalScrollPosition + verticalVisibleAmount);
            }
        }
    }

    private void firstClickSouth(Click click) {
        Location location1 = click.getLocation();
        int x = location1.getX();
        if (click.button3()) {
            setHorizontalPostion(x - horizontalVisibleAmount / 2);
        } else if (click.button1()) {
            if (x < horizontalScrollPosition) {
                setHorizontalPostion(horizontalScrollPosition - horizontalVisibleAmount);
            } else if (x > horizontalScrollPosition + horizontalVisibleAmount) {
                setHorizontalPostion(horizontalScrollPosition + horizontalVisibleAmount);
            }
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
        getViewManager().getSpy().addTrace(this, "non border area", contentArea());

        int area = adjust(location);
        switch (area) {
        case NORTH:
           	return topHeader.identify(location);

        case WEST:
            return leftHeader.identify(location);

        case CENTER:
            return wrappedView.identify(location);

        case SOUTH:
            getViewManager().getSpy().addTrace(this, "over scroll bar area", contentArea());
            return getView();

        case EAST:
            getViewManager().getSpy().addTrace(this, "over scroll bar area", contentArea());
            return getView();

        default:
            return null;
        }
    }
    
    public void limitBoundsWithin(Bounds bounds) {
        super.limitBoundsWithin(bounds);
        if(verticalScrollPosition > verticalMaximum) {
            verticalScrollPosition = verticalMaximum;
        }
        if(horizontalScrollPosition > horizontalMaximum) {
            horizontalScrollPosition = horizontalMaximum;
        }
    }

    public void markDamaged(Bounds bounds) {
        /* TODO this only works for the main content area, not for the headers.
        how do we figure out which area to adjust for?
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
      //      location.add(offset());
      //      location.move(-left, -top);
            wrappedView.mouseMoved(location);
            break;

        case SOUTH:
        case EAST:
        default:
            break;
        }    
    }

    private Offset offset() {
        Bounds contents = contentArea();
        int x = horizontalScrollPosition * wrappedView.getRequiredSize().getWidth() / contents.getWidth();
        int y = verticalScrollPosition * wrappedView.getRequiredSize().getHeight() / contents.getHeight();
        return new Offset(x, y);
    }

    protected boolean overContent(Location location) {
        return contentArea().contains(location);
    }

    public void reset() {
        horizontalScrollPosition = 0;
        verticalScrollPosition = 0;
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
        int position = (y < midpoint) ? verticalMinimum : verticalMaximum;
        setVerticalPostion(position);
    }

    private void secondClickSouth(Click click) {
        int x = click.getLocation().getX();
        int midpoint = (getSize().getWidth() - left - right) / 2;
        int position = (x < midpoint) ? horizontalMinimum : horizontalMaximum;
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
        horizontalScrollPosition = Math.min(position, horizontalMaximum);
        horizontalScrollPosition = Math.max(horizontalScrollPosition, horizontalMinimum);
        markDamaged();
    }

    public void setRequiredSize(Size size) {
        Size wrappedSize = new Size(size);
        wrappedSize.contract(left, top);
        wrappedView.setRequiredSize(wrappedSize);
    }

    public void setSize(Size size) {
        // TODO need to restore the offset after size change - see limitBounds
        //      float verticalRatio = ((float) verticalScrollPosition) /
        // contentArea().getHeight();

        Size requiredSize = wrappedView.getRequiredSize();

        int availableHeight2 = size.getHeight() - top;
        int contentHeight2 = requiredSize.getHeight();
        right =  availableHeight2 >= contentHeight2 ? 0 : SCROLLBAR_WIDTH;

        int availableWidth2 = size.getWidth() - left;
        int contentWidth2 = requiredSize.getWidth();
        bottom =  availableWidth2 >= contentWidth2 ? 0 : SCROLLBAR_WIDTH;


        this.size = new Size(size);
      //  this.size.contract(16, 16);
        
        Bounds boundsOfVisibleArea = contentArea();
        Size contentSize = requiredSize;

        int availableHeight = boundsOfVisibleArea.getHeight();
        int contentHeight = Math.max(availableHeight, contentSize.getHeight());
        verticalVisibleAmount = availableHeight * availableHeight / contentHeight;
        verticalMaximum = availableHeight - verticalVisibleAmount;
       
        int availableWidth = boundsOfVisibleArea.getWidth();
        int contentWidth = Math.max(availableWidth, contentSize.getWidth());
        horizontalVisibleAmount = availableWidth * availableWidth / contentWidth;
        horizontalMaximum = availableWidth - horizontalVisibleAmount;

        if(leftHeader != null) {
            leftHeader.setSize(new Size(left, contentHeight));
        }

        if(topHeader != null) {
            topHeader.setSize(new Size(contentWidth, top));
        }
        
        wrappedView.setSize(requiredSize);
    }

    public void setVerticalPostion(final int position) {
        getViewManager().addSpyAction("Move to vertical position " + position);
        verticalScrollPosition = Math.min(position, verticalMaximum);
        verticalScrollPosition = Math.max(verticalScrollPosition, verticalMinimum);
        markDamaged();
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
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */